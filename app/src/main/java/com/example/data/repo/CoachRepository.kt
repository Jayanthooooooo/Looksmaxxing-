package com.example.data.repo

import com.example.core.AiConfig
import com.example.core.AppDispatchers
import com.example.core.Logger
import com.example.core.Outcome
import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatThreadEntity
import com.example.data.local.datastore.PreferencesDataStore
import com.example.domain.model.ScanResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class CoachRepository(
    private val chatDao: ChatDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val dispatchers: AppDispatchers,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(AiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(AiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(AiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build(),
    private val baseUrl: String = "https://generativelanguage.googleapis.com",
    private val apiKeyProvider: () -> String = { com.example.BuildConfig.GEMINI_API_KEY }
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

    suspend fun saveAssistantMessage(threadId: String, text: String): String = withContext(dispatchers.io) {
        val msgId = "msg_${UUID.randomUUID()}"
        val assistantMsg = ChatMessageEntity(
            id = msgId,
            threadId = threadId,
            role = "assistant",
            text = text,
            createdAt = System.currentTimeMillis()
        )
        chatDao.insertMessage(assistantMsg)
        msgId
    }

    suspend fun reportReply(messageId: String, reason: String, note: String): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            chatDao.markReported(messageId)
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Outcome.Error(e.localizedMessage ?: "Failed to report reply")
        }
    }

    fun streamCoachResponse(
        threadId: String,
        userPrompt: String,
        latestScan: ScanResult?,
        isPro: Boolean
    ): Flow<String> = flow {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            val errorMsg = "AI key missing. Add GEMINI_API_KEY in Secrets."
            Logger.e("GEMINI_API_KEY is missing or unconfigured in Secrets.")
            emit(errorMsg)
            return@flow
        }

        // 1. Save user message to Room
        val now = System.currentTimeMillis()
        val userMsg = ChatMessageEntity(
            id = "msg_${UUID.randomUUID()}",
            threadId = threadId,
            role = "user",
            text = userPrompt,
            createdAt = now
        )
        chatDao.insertMessage(userMsg)

        val threadTitle = if (chatDao.getThread(threadId) == null) userPrompt.take(35) else (chatDao.getThread(threadId)?.title ?: "Conversation")
        chatDao.insertOrUpdateThread(
            ChatThreadEntity(id = threadId, title = threadTitle, updatedAt = now)
        )

        // 2. Personalised system instruction
        val scanContext = if (latestScan != null) {
            val overallScore = latestScan.indices["overall"] ?: (
                ((latestScan.indices["proportions"] ?: 70) * 0.4f +
                 (latestScan.indices["symmetry"] ?: 72) * 0.4f +
                 (latestScan.indices["clarity"] ?: 68) * 0.2f).toInt()
            )
            """
            LATEST REAL SCAN CONTEXT:
            - Overall Face Score: $overallScore/100
            - Proportions Index: ${latestScan.indices["proportions"] ?: "N/A"}
            - Symmetry Index: ${latestScan.indices["symmetry"] ?: "N/A"}
            - Skin Clarity Index: ${latestScan.indices["clarity"] ?: "N/A"}
            - Key Metrics: ${latestScan.metrics.entries.take(8).joinToString("; ") { "${it.value.name}: ${it.value.value} ${it.value.unit} (${it.value.rangePositionText})" }}
            - Skin Descriptors: ${latestScan.skinResult?.descriptors?.entries?.joinToString("; ") { "${it.key}: ${it.value.severity}" } ?: "None"}
            """.trimIndent()
        } else {
            "NO RECENT SCAN DATA AVAILABLE."
        }

        val systemPrompt = """
            You are the Lumora AI Beauty & Grooming Coach.
            Deliver supportive advice on facial aesthetics, grooming, skincare, posture, and style.
            
            RULES:
            1. Never provide medical diagnoses or medical advice.
            2. Never use shaming or negative language (e.g. "flaw", "ugly", "bad").
            3. Distinguish between unchangeable bone structure and changeable presentation factors (grooming, skincare, posture, style).
            4. Keep responses concise, well-structured, and empathetic.
            
            $scanContext
        """.trimIndent()

        // 3. Build contents array from last 10 Room messages
        val historyEntities = chatDao.getLastTenMessages(threadId).reversed()
        val contentsJsonArray = JSONArray()
        historyEntities.forEach { h ->
            val roleStr = if (h.role == "user") "user" else "model"
            val msgObj = JSONObject().apply {
                put("role", roleStr)
                put("parts", JSONArray().put(JSONObject().put("text", h.text)))
            }
            contentsJsonArray.put(msgObj)
        }

        val requestBodyJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("contents", contentsJsonArray)
        }

        // 4. Candidate Models Chain
        val cachedModel = preferencesDataStore.workingModelFlow.firstOrNull()
        val modelCandidates = mutableListOf<String>()
        if (!cachedModel.isNullOrBlank() && cachedModel != "MY_GEMINI_API_KEY" && cachedModel != "GEMINI_API_KEY") {
            modelCandidates.add(cachedModel)
        }

        modelCandidates.add(AiConfig.PRIMARY_MODEL)
        modelCandidates.addAll(AiConfig.FALLBACK_MODELS)
        val candidateModels = modelCandidates.distinct()

        var lastErrorMessage: String? = null
        var successfulText: String? = null

        for ((index, model) in candidateModels.withIndex()) {
            val url = "$baseUrl/v1beta/models/$model:streamGenerateContent?alt=sse"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            var call: Call? = null
            try {
                call = okHttpClient.newCall(request)
                val response = call.execute()
                val code = response.code

                if (response.isSuccessful && response.body != null) {
                    preferencesDataStore.setWorkingModel(model)

                    val reader = response.body!!.charStream().buffered()
                    val accumulated = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line ?: break
                        if (currentLine.startsWith("data: ")) {
                            val jsonStr = currentLine.substring(6).trim()
                            if (jsonStr.isNotBlank() && jsonStr != "[DONE]") {
                                val chunkText = parseSseChunkText(jsonStr)
                                if (chunkText.isNotEmpty()) {
                                    accumulated.append(chunkText)
                                    emit(accumulated.toString())
                                }
                            }
                        }
                    }

                    successfulText = accumulated.toString()
                    break
                } else {
                    val errorBodyStr = response.body?.string() ?: ""
                    val googleErrorMsg = parseGoogleErrorMessage(errorBodyStr)
                    Logger.e("Gemini REST API Error ($model, HTTP $code): $googleErrorMsg")

                    lastErrorMessage = mapStatusCodeToMessage(code, googleErrorMsg)

                    if (code == 400 || code == 401 || code == 403) {
                        break
                    }

                    if (code == 404 || code == 429 || code >= 500) {
                        kotlinx.coroutines.delay(1000L * (index + 1))
                    }
                }
            } catch (e: CancellationException) {
                call?.cancel()
                throw e
            } catch (e: Exception) {
                Logger.e("OkHttp Network Exception on model '$model'", e)
                lastErrorMessage = "Network connection failed. Please check internet connection."
                if (index < candidateModels.size - 1) {
                    kotlinx.coroutines.delay(1000L * (index + 1))
                }
            }
        }

        val err = lastErrorMessage
        if (successfulText == null && err != null) {
            emit(err)
        }
    }.flowOn(dispatchers.io)


    suspend fun testAiConnection(): Outcome<String> = withContext(dispatchers.io) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return@withContext Outcome.Error("AI key missing. Add GEMINI_API_KEY in Secrets.")
        }

        val model = preferencesDataStore.workingModelFlow.firstOrNull() ?: AiConfig.PRIMARY_MODEL
        val url = "$baseUrl/v1beta/models/$model:generateContent"
        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", "Say OK")))))
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val text = parseNonStreamResponseText(bodyStr)
                Outcome.Success("Connected ($model): ${text.ifBlank { "OK" }}")
            } else {
                val googleMsg = parseGoogleErrorMessage(bodyStr)
                val mappedErr = mapStatusCodeToMessage(response.code, googleMsg)
                Logger.e("Test AI Connection Error ($model): $mappedErr")
                Outcome.Error("HTTP ${response.code} ($model): $mappedErr")
            }
        } catch (e: Exception) {
            Logger.e("Test AI Connection Exception", e)
            Outcome.Error("Connection Error: ${e.localizedMessage}")
        }
    }

    private fun parseSseChunkText(jsonStr: String): String {
        return try {
            val root = JSONObject(jsonStr)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val p = parts.getJSONObject(i)
                sb.append(p.optString("text", ""))
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseNonStreamResponseText(jsonStr: String): String {
        return parseSseChunkText(jsonStr)
    }

    private fun parseGoogleErrorMessage(errorBodyStr: String): String {
        if (errorBodyStr.isBlank()) return ""
        return try {
            val obj = JSONObject(errorBodyStr)
            val errObj = obj.optJSONObject("error")
            errObj?.optString("message", errorBodyStr) ?: errorBodyStr
        } catch (_: Exception) {
            errorBodyStr
        }
    }

    private fun mapStatusCodeToMessage(code: Int, googleMsg: String): String {
        return when (code) {
            400 -> "Bad Request (400): ${googleMsg.ifBlank { "Invalid request body format." }}"
            401, 403 -> "Invalid API Key (HTTP $code): API key is invalid or Gemini API is not enabled in your project."
            404 -> "Model Not Found (404): The requested AI model is unavailable."
            429 -> "Rate limit reached. Please wait a minute before sending another message."
            in 500..599 -> "AI Server Error ($code): Temporary service disruption. Please try again shortly."
            else -> "AI Service Error ($code): ${googleMsg.ifBlank { "Request failed." }}"
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this.isNullOrBlank() || this == "MY_GEMINI_API_KEY" || this == "GEMINI_API_KEY"
    }
}
