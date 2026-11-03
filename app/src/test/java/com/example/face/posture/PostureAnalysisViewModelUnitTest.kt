package com.example.face.posture

import android.app.Application
import com.example.LumoraApp
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.results.PostureAnalysisViewModel
import com.example.ui.screens.results.PostureUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeNullExtractor : PoseLandmarkExtractor {
    override suspend fun extractPoseLandmarks(bitmap: Bitmap): PosturePoseInput? = null
}

class FakeValidExtractor : PoseLandmarkExtractor {
    override suspend fun extractPoseLandmarks(bitmap: Bitmap): PosturePoseInput {
        return PosturePoseInput(
            ear = PostureLandmarkPoint(180f, 130f, 0.9f),
            shoulder = PostureLandmarkPoint(100f, 220f, 0.9f),
            hip = PostureLandmarkPoint(100f, 480f, 0.9f)
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = LumoraApp::class, sdk = [36])
class PostureAnalysisViewModelUnitTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val app = ApplicationProvider.getApplicationContext<LumoraApp>()
        val testAppDispatchers = com.example.core.AppDispatchers(
            main = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )
        app.container.dispatchers = testAppDispatchers
        app.container.postureRepository.dispatchers = testAppDispatchers
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModelNeverProducesResultWhenExtractionFails() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val nullViewModel = PostureAnalysisViewModel(app, FakeNullExtractor())
        val mockBitmap = Bitmap.createBitmap(200, 400, Bitmap.Config.ARGB_8888)
        nullViewModel.processSideBitmap(mockBitmap)
        var nullAttempts = 0
        while ((nullViewModel.uiState.value as? PostureUiState.Success)?.currentBitmap == null && nullAttempts < 50) {
            Thread.sleep(50)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            testDispatcher.scheduler.advanceUntilIdle()
            nullAttempts++
        }

        val nullState = nullViewModel.uiState.value as? PostureUiState.Success
        assertNotNull("Actual state: ${nullViewModel.uiState.value}", nullState)
        assertEquals("Not enough data", nullState!!.analysis.errorMessage)
        assertNull(nullState.analysis.band)
        assertNotNull(nullState.analysis.retakeTip)

        val validViewModel = PostureAnalysisViewModel(app, FakeValidExtractor())
        testDispatcher.scheduler.advanceUntilIdle()

        validViewModel.processSideBitmap(mockBitmap)
        var attempts = 0
        while ((validViewModel.uiState.value as? PostureUiState.Success)?.currentBitmap == null && attempts < 50) {
            Thread.sleep(50)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            testDispatcher.scheduler.advanceUntilIdle()
            attempts++
        }

        val validState = validViewModel.uiState.value as? PostureUiState.Success
        assertNotNull("validState is null! Actual state: ${validViewModel.uiState.value}", validState)
        assertNull(validState!!.analysis.errorMessage)
        assertNotNull(validState.analysis.band)
    }
}
