package com.example.face.posture

import android.app.Application
import com.example.LumoraApp
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.results.PostureAnalysisViewModel
import com.example.ui.screens.results.PostureUiState
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModelNeverProducesResultWhenExtractionFails() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val nullViewModel = PostureAnalysisViewModel(app, FakeNullExtractor())
        testDispatcher.scheduler.advanceUntilIdle()

        val mockBitmap = Bitmap.createBitmap(200, 400, Bitmap.Config.ARGB_8888)
        nullViewModel.processSideBitmap(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        val nullState = nullViewModel.uiState.value as? PostureUiState.Success
        assertNotNull(nullState)
        assertEquals("Not enough data", nullState!!.analysis.errorMessage)
        assertNull(nullState.analysis.band)
        assertNotNull(nullState.analysis.retakeTip)

        val validViewModel = PostureAnalysisViewModel(app, FakeValidExtractor())
        testDispatcher.scheduler.advanceUntilIdle()

        validViewModel.processSideBitmap(mockBitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        val validState = validViewModel.uiState.value as? PostureUiState.Success
        assertNotNull(validState)
        assertNull(validState!!.analysis.errorMessage)
        assertNotNull(validState.analysis.band)
    }
}
