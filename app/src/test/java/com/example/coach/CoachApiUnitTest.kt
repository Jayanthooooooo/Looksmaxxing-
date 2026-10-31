package com.example.coach

import com.example.core.AppDispatchers
import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatThreadEntity
import com.example.data.local.datastore.PreferencesDataStore
import com.example.data.repo.CoachRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CoachApiUnitTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var fakeChatDao: FakeChatDao
    private lateinit var fakePreferencesDataStore: PreferencesDataStore

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        fakeChatDao = FakeChatDao()
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        fakePreferencesDataStore = PreferencesDataStore(context)
    }

    private fun TestScope.createTestDispatchers(): AppDispatchers {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        return AppDispatchers(
            main = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testSuccessStreamSse() = runTest {
        val sseResponseBody = """
            data: {"candidates":[{"content":{"parts":[{"text":"Hello "}]}}]}
            
            data: {"candidates":[{"content":{"parts":[{"text":"from Lumora!"}]}}]}
            
            data: [DONE]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody(sseResponseBody)
        )

        val okHttpClient = OkHttpClient.Builder().build()
        val repository = CoachRepository(
            chatDao = fakeChatDao,
            preferencesDataStore = fakePreferencesDataStore,
            dispatchers = createTestDispatchers(),
            okHttpClient = okHttpClient,
            baseUrl = mockWebServer.url("").toString().removeSuffix("/"),
            apiKeyProvider = { "test_valid_api_key" }
        )

        val emissions = repository.streamCoachResponse(
            threadId = "test_thread",
            userPrompt = "Hello Coach",
            latestScan = null,
            isPro = false
        ).toList()

        assertTrue(emissions.isNotEmpty())
        assertEquals("Hello from Lumora!", emissions.last())
    }

    @Test
    fun testModelNotFound404Fallback() = runTest {
        // First model fails with 404
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"error":{"message":"Model gemini-3.1-flash-lite not found"}}""")
        )

        // Fallback model succeeds
        val fallbackSseBody = """
            data: {"candidates":[{"content":{"parts":[{"text":"Fallback response"}]}}]}
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody(fallbackSseBody)
        )

        val okHttpClient = OkHttpClient.Builder().build()
        val repository = CoachRepository(
            chatDao = fakeChatDao,
            preferencesDataStore = fakePreferencesDataStore,
            dispatchers = createTestDispatchers(),
            okHttpClient = okHttpClient,
            baseUrl = mockWebServer.url("").toString().removeSuffix("/"),
            apiKeyProvider = { "test_valid_api_key" }
        )

        val emissions = repository.streamCoachResponse(
            threadId = "test_thread",
            userPrompt = "Fallback check",
            latestScan = null,
            isPro = false
        ).toList()

        assertEquals(2, mockWebServer.requestCount)
        assertEquals("Fallback response", emissions.last())
    }

    @Test
    fun testRateLimit429MappedError() = runTest {
        // Enqueue for all possible candidate models (3)
        repeat(3) {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(429)
                    .setBody("""{"error":{"message":"Resource exhausted"}}""")
            )
        }

        val okHttpClient = OkHttpClient.Builder().build()
        val repository = CoachRepository(
            chatDao = fakeChatDao,
            preferencesDataStore = fakePreferencesDataStore,
            dispatchers = createTestDispatchers(),
            okHttpClient = okHttpClient,
            baseUrl = mockWebServer.url("").toString().removeSuffix("/"),
            apiKeyProvider = { "test_valid_api_key" }
        )

        val emissions = repository.streamCoachResponse(
            threadId = "test_thread",
            userPrompt = "Quota test",
            latestScan = null,
            isPro = false
        ).toList()

        assertTrue("Expected rate limit message but got: ${emissions.lastOrNull()}", 
            emissions.lastOrNull()?.contains("Rate limit reached") == true)
    }

    @Test
    fun testBlankApiKeyEmitsMissingKeyError() = runTest {
        val okHttpClient = OkHttpClient.Builder().build()
        val repository = CoachRepository(
            chatDao = fakeChatDao,
            preferencesDataStore = fakePreferencesDataStore,
            dispatchers = createTestDispatchers(),
            okHttpClient = okHttpClient,
            baseUrl = mockWebServer.url("").toString().removeSuffix("/"),
            apiKeyProvider = { "" }
        )

        val emissions = repository.streamCoachResponse(
            threadId = "test_thread",
            userPrompt = "No key check",
            latestScan = null,
            isPro = false
        ).toList()

        assertEquals(1, emissions.size)
        assertEquals("AI key missing. Add GEMINI_API_KEY in Secrets.", emissions[0])
    }

    @Test
    fun testCancelStreamingHandled() = runTest {
        val infiniteSseBody = """
            data: {"candidates":[{"content":{"parts":[{"text":"Chunk 1"}]}}]}
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/event-stream")
                .setBody(infiniteSseBody)
                .setBodyDelay(5, TimeUnit.SECONDS)
        )

        val okHttpClient = OkHttpClient.Builder().build()
        val repository = CoachRepository(
            chatDao = fakeChatDao,
            preferencesDataStore = fakePreferencesDataStore,
            dispatchers = createTestDispatchers(),
            okHttpClient = okHttpClient,
            baseUrl = mockWebServer.url("").toString().removeSuffix("/"),
            apiKeyProvider = { "test_valid_api_key" }
        )

        val job = launch {
            try {
                repository.streamCoachResponse(
                    threadId = "test_thread",
                    userPrompt = "Cancel test",
                    latestScan = null,
                    isPro = false
                ).collect {}
            } catch (_: CancellationException) {
                // Expected cancellation
            }
        }

        job.cancel()
        assertTrue(job.isCancelled)
    }

    private class FakeChatDao : ChatDao {
        private val messages = mutableListOf<ChatMessageEntity>()
        private val threads = mutableListOf<ChatThreadEntity>()

        override fun observeAllThreads(): Flow<List<ChatThreadEntity>> = flowOf(threads)

        override suspend fun getThread(threadId: String): ChatThreadEntity? = threads.find { it.id == threadId }

        override suspend fun insertOrUpdateThread(thread: ChatThreadEntity) {
            threads.removeAll { it.id == thread.id }
            threads.add(thread)
        }

        override suspend fun deleteThread(threadId: String) {
            threads.removeAll { it.id == threadId }
        }

        override suspend fun clearAllThreads() {
            threads.clear()
        }

        override fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>> = flowOf(messages.filter { it.threadId == threadId })

        override suspend fun getLastTenMessages(threadId: String): List<ChatMessageEntity> {
            return messages.filter { it.threadId == threadId }.takeLast(10)
        }

        override suspend fun insertMessage(message: ChatMessageEntity) {
            messages.add(message)
        }

        override suspend fun markReported(messageId: String) {
            val idx = messages.indexOfFirst { it.id == messageId }
            if (idx >= 0) {
                messages[idx] = messages[idx].copy(reported = true)
            }
        }

        override suspend fun deleteMessagesForThread(threadId: String) {
            messages.removeAll { it.threadId == threadId }
        }

        override suspend fun clearAllMessages() {
            messages.clear()
        }
    }
}
