package com.example.data.repo

import com.example.core.AiConfig
import com.example.core.AppDispatchers
import com.example.core.Logger
import com.example.core.Outcome
import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatThreadEntity
import com.example.data.local.datastore.PreferencesDataStore
import com.example.data.remote.FirebaseModule
import com.example.domain.model.ScanResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CoachRepository(
    private val chatDao: ChatDao,
    private val firebaseModule: FirebaseModule,
    private val preferencesDataStore: PreferencesDataStore,
    private val dispatchers: AppDispatchers
) {

    fun observeThreads(): Flow<List<ChatThreadEntity>> = chatDao.observeAllThreads()

    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>> = chatDao.observeMessages(threadId)

    suspend fun createThread(title: String): String = withContext(dispatchers.io) {
        val threadId = "thread_${UUID.randomUUID()}"
        val thread = ChatThreadEntity(
            id = threadId,
            title = title.take(40),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertOrUpdateThread(thread)
        threadId
    }

    suspend fun deleteThread(threadId: String): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            chatDao.deleteMessagesForThread(threadId)
            chatDao.deleteThread(threadId)
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Outcome.Error(e.localizedMessage ?: "Failed to delete thread")
        }
    }

    suspend fun clearAllThreads(): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            chatDao.clearAllMessages()
            chatDao.clearAllThreads()
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Outcome.Error(e.localizedMessage ?: "Failed to clear history")
        }
    }

    suspend fun reportReply(messageId: String, reason: String, note: String): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            chatDao.markReported(messageId)
            val reportData = mapOf(
                "messageId" to messageId,
                "reason" to reason,
                "note" to note,
                "timestamp" to System.currentTimeMillis()
            )
            firebaseModule.firestore.collection("replyReports").add(reportData)
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Logger.w("Report reply Firestore sync failed, local mark set: ${e.message}")
            Outcome.Success(Unit)
        }
    }

    fun streamCoachResponse(
        threadId: String,
        userPrompt: String,
        latestScan: ScanResult?,
        isPro: Boolean
    ): Flow<String> = flow {
        // 1. Persist user message
        val userMsgId = "msg_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        val userMsg = ChatMessageEntity(
            id = userMsgId,
            threadId = threadId,
            role = "user",
            text = userPrompt,
            createdAt = now
        )
        chatDao.insertMessage(userMsg)

        // Ensure thread exists / update timestamp
        val threadTitle = if (chatDao.getThread(threadId) == null) userPrompt.take(35) else (chatDao.getThread(threadId)?.title ?: "Conversation")
        chatDao.insertOrUpdateThread(
            ChatThreadEntity(id = threadId, title = threadTitle, updatedAt = now)
        )

        // 2. Build system instruction with scan context
        val scanContext = if (latestScan != null) {
            """
            LATEST USER SCAN CONTEXT:
            - Overall Face Score: ${latestScan.qualityReport.score}/100
            - Symmetry Index: ${latestScan.indices["symmetry"] ?: 80}
            - Proportions Index: ${latestScan.indices["proportions"] ?: 80}
            - Skin Clarity: ${latestScan.indices["clarity"] ?: 80}
            - Key Metrics: ${latestScan.metrics.entries.joinToString("; ") { "${it.value.name}: ${it.value.value} ${it.value.unit} (${it.value.rangePositionText})" }}
            - Skin Descriptors: ${latestScan.skinResult?.descriptors?.entries?.joinToString("; ") { "${it.key}: ${it.value.severity}" } ?: "N/A"}
            """.trimIndent()
        } else {
            "NO RECENT SCAN DATA AVAILABLE. Provide general guidance."
        }

        val systemPrompt = """
            You are the Lumora AI Beauty & Grooming Coach.
            Your job is to deliver helpful, objective, and supportive advice on facial aesthetics, grooming, skincare, posture, and style.
            
            RULES:
            1. Never provide medical diagnoses or medical advice.
            2. Never use shaming or negative language (e.g. "flaw", "ugly", "bad").
            3. Strictly distinguish between unchangeable bone structure (e.g. jaw angle, eye distance) and changeable presentation factors (grooming, skincare, posture, hairstyle, lighting).
            4. Keep responses concise, well-structured, and empathetic.
            
            $scanContext
        """.trimIndent()

        val assistantMsgId = "msg_${UUID.randomUUID()}"
        val accumulatedText = StringBuilder()

        try {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = AiConfig.TEXT_MODEL,
                systemInstruction = content { text(systemPrompt) },
                generationConfig = generationConfig {
                    temperature = 0.4f
                }
            )

            // Fetch last 10 messages for conversation context
            val historyEntities = chatDao.getLastTenMessages(threadId).reversed()
            val promptContent = content {
                historyEntities.forEach { h ->
                    if (h.role == "user") text("User: ${h.text}") else text("Coach: ${h.text}")
                }
                text("User: $userPrompt")
            }

            model.generateContentStream(promptContent).collect { chunk ->
                val textChunk = chunk.text.orEmpty()
                accumulatedText.append(textChunk)
                emit(accumulatedText.toString())
            }

            val finalReply = if (accumulatedText.isNotBlank()) accumulatedText.toString() else "I understand your question. Let's focus on simple habits to support your appearance consistency."
            val assistantMsg = ChatMessageEntity(
                id = assistantMsgId,
                threadId = threadId,
                role = "assistant",
                text = finalReply,
                createdAt = System.currentTimeMillis()
            )
            chatDao.insertMessage(assistantMsg)

        } catch (e: CancellationException) {
            if (accumulatedText.isNotBlank()) {
                val assistantMsg = ChatMessageEntity(
                    id = assistantMsgId,
                    threadId = threadId,
                    role = "assistant",
                    text = accumulatedText.toString(),
                    createdAt = System.currentTimeMillis()
                )
                chatDao.insertMessage(assistantMsg)
            }
            throw e
        } catch (e: Exception) {
            Logger.e("Coach streaming generation error", e)
            val errorReply = when {
                e.message?.contains("App Check", ignoreCase = true) == true ->
                    "Unable to connect to AI server (App Check attestation failure). Please verify network and backend configuration."
                e.message?.contains("quota", ignoreCase = true) == true ->
                    "Coach API quota limit reached. Please try again shortly."
                else -> "I couldn't complete that response right now. Please tap retry to try again."
            }
            val assistantMsg = ChatMessageEntity(
                id = assistantMsgId,
                threadId = threadId,
                role = "assistant",
                text = errorReply,
                createdAt = System.currentTimeMillis()
            )
            chatDao.insertMessage(assistantMsg)
            emit(errorReply)
        }
    }.flowOn(dispatchers.io)
}
