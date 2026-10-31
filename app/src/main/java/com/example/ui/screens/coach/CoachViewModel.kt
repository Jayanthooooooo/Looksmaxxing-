package com.example.ui.screens.coach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AiConfig
import com.example.data.local.datastore.PreferencesDataStore
import com.example.data.repo.CoachRepository
import com.example.domain.model.ScanResult
import com.example.ui.screens.main.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CoachUiState(
    val currentThreadId: String = "default_thread",
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val messagesUsedToday: Int = 0,
    val dailyLimit: Int = AiConfig.FREE_DAILY_MESSAGE_LIMIT,
    val errorMessage: String? = null,
    val lastUserPrompt: String? = null
)

class CoachViewModel(
    private val coachRepository: CoachRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    private val _roomMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _streamingMessage = MutableStateFlow<ChatMessage?>(null)

    private var currentJob: Job? = null

    init {
        loadTodayCount()
        observeThreadMessages("default_thread")
    }

    private fun todayDateStr(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    }

    private fun loadTodayCount() {
        viewModelScope.launch {
            preferencesDataStore.coachDailyCountFlow.collectLatest { (date, count) ->
                val today = todayDateStr()
                val realCount = if (date == today) count else 0
                _uiState.value = _uiState.value.copy(messagesUsedToday = realCount)
            }
        }
    }

    fun selectThread(threadId: String) {
        _uiState.value = _uiState.value.copy(currentThreadId = threadId)
        observeThreadMessages(threadId)
    }

    private fun updateCombinedMessages() {
        val roomMsgs = _roomMessages.value
        val streamingMsg = _streamingMessage.value
        val combined = if (streamingMsg != null) {
            roomMsgs.filter { it.id != streamingMsg.id } + streamingMsg
        } else {
            roomMsgs
        }
        _uiState.value = _uiState.value.copy(messages = combined)
    }

    private fun observeThreadMessages(threadId: String) {
        viewModelScope.launch {
            coachRepository.observeMessages(threadId).collect { entities ->
                val uiMsgs = entities.map { e ->
                    ChatMessage(
                        id = e.id,
                        role = e.role,
                        text = e.text,
                        isStreaming = false,
                        isError = false
                    )
                }
                _roomMessages.value = uiMsgs
                updateCombinedMessages()
            }
        }
    }

    fun sendMessage(
        userPrompt: String,
        latestScan: ScanResult?,
        isPro: Boolean
    ) {
        val state = _uiState.value
        val limit = if (isPro) 100 else AiConfig.FREE_DAILY_MESSAGE_LIMIT
        _uiState.value = state.copy(dailyLimit = limit, lastUserPrompt = userPrompt)

        if (state.messagesUsedToday >= limit && !isPro) {
            _uiState.value = _uiState.value.copy(errorMessage = "Daily message limit reached. Upgrade to Pro for more responses.")
            return
        }

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)
            val newCount = preferencesDataStore.incrementCoachCount(todayDateStr())
            _uiState.value = _uiState.value.copy(messagesUsedToday = newCount)

            var lastChunkText = ""
            _streamingMessage.value = ChatMessage(id = "streaming", role = "assistant", text = "Thinking...", isStreaming = true)
            updateCombinedMessages()

            try {
                coachRepository.streamCoachResponse(
                    threadId = state.currentThreadId,
                    userPrompt = userPrompt,
                    latestScan = latestScan,
                    isPro = isPro
                ).collect { chunkText ->
                    lastChunkText = chunkText
                    _streamingMessage.value = ChatMessage(id = "streaming", role = "assistant", text = chunkText, isStreaming = true)
                    updateCombinedMessages()
                }

                // Check if final response is an error message
                val isErrorMsg = isKnownErrorMessage(lastChunkText)
                if (isErrorMsg) {
                    _streamingMessage.value = ChatMessage(id = "streaming_error", role = "assistant", text = lastChunkText, isStreaming = false, isError = true)
                    updateCombinedMessages()
                } else if (lastChunkText.isNotBlank()) {
                    coachRepository.saveAssistantMessage(state.currentThreadId, lastChunkText)
                    _streamingMessage.value = null
                    updateCombinedMessages()
                } else {
                    _streamingMessage.value = null
                    updateCombinedMessages()
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    val errText = e.localizedMessage ?: "Failed to generate AI response"
                    _streamingMessage.value = ChatMessage(id = "streaming_error", role = "assistant", text = errText, isStreaming = false, isError = true)
                    updateCombinedMessages()
                }
            } finally {
                _uiState.value = _uiState.value.copy(isGenerating = false)
            }
        }
    }

    fun retryLastMessage(latestScan: ScanResult?, isPro: Boolean) {
        val prompt = _uiState.value.lastUserPrompt
        if (!prompt.isNullOrBlank()) {
            sendMessage(prompt, latestScan, isPro)
        }
    }


    fun stopGeneration() {
        val currentStreamingText = _streamingMessage.value?.text.orEmpty()
        currentJob?.cancel()
        currentJob = null
        viewModelScope.launch {
            if (currentStreamingText.isNotBlank() && !isKnownErrorMessage(currentStreamingText) && currentStreamingText != "Thinking...") {
                coachRepository.saveAssistantMessage(_uiState.value.currentThreadId, currentStreamingText)
            }
            _streamingMessage.value = null
            _uiState.value = _uiState.value.copy(isGenerating = false)
            updateCombinedMessages()
        }
    }

    fun deleteThread(threadId: String) {
        viewModelScope.launch {
            coachRepository.deleteThread(threadId)
            if (_uiState.value.currentThreadId == threadId) {
                selectThread("default_thread")
            }
        }
    }

    fun clearAllThreads() {
        viewModelScope.launch {
            coachRepository.clearAllThreads()
            selectThread("default_thread")
        }
    }

    fun reportReply(messageId: String, reason: String, note: String) {
        viewModelScope.launch {
            coachRepository.reportReply(messageId, reason, note)
        }
    }

    private fun isKnownErrorMessage(text: String): Boolean {
        return text.startsWith("AI key missing") ||
                text.startsWith("Invalid API Key") ||
                text.startsWith("Bad Request") ||
                text.startsWith("Model Not Found") ||
                text.startsWith("Rate limit reached") ||
                text.startsWith("AI Server Error") ||
                text.startsWith("AI Service Error") ||
                text.startsWith("Network connection failed")
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this.isNullOrBlank()
    }
}
