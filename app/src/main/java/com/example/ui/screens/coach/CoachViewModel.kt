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
    val errorMessage: String? = null
)

class CoachViewModel(
    private val coachRepository: CoachRepository,
    private val preferencesDataStore: PreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

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

    private fun observeThreadMessages(threadId: String) {
        viewModelScope.launch {
            coachRepository.observeMessages(threadId).collect { entities ->
                val uiMsgs = entities.map { e ->
                    ChatMessage(
                        id = e.id,
                        role = e.role,
                        text = e.text,
                        isStreaming = false
                    )
                }
                _uiState.value = _uiState.value.copy(messages = uiMsgs)
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
        _uiState.value = state.copy(dailyLimit = limit)

        if (state.messagesUsedToday >= limit && !isPro) {
            _uiState.value = _uiState.value.copy(errorMessage = "Daily message limit reached. Upgrade to Pro for more responses.")
            return
        }

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)
            val newCount = preferencesDataStore.incrementCoachCount(todayDateStr())
            _uiState.value = _uiState.value.copy(messagesUsedToday = newCount)

            var streamingText = ""
            val tempAssistantId = "temp_${System.currentTimeMillis()}"

            try {
                coachRepository.streamCoachResponse(
                    threadId = state.currentThreadId,
                    userPrompt = userPrompt,
                    latestScan = latestScan,
                    isPro = isPro
                ).collect { chunkText ->
                    streamingText = chunkText
                    val currentList = _uiState.value.messages.filter { it.id != tempAssistantId }.toMutableList()
                    currentList.add(ChatMessage(id = tempAssistantId, role = "assistant", text = streamingText, isStreaming = true))
                    _uiState.value = _uiState.value.copy(messages = currentList)
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to generate response")
                }
            } finally {
                _uiState.value = _uiState.value.copy(isGenerating = false)
            }
        }
    }

    fun stopGeneration() {
        currentJob?.cancel()
        currentJob = null
        _uiState.value = _uiState.value.copy(isGenerating = false)
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
}
