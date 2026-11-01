package com.example.face.posture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostureEngineUnitTest {

    @Test
    fun testMissingLandmarksReturnsNotEnoughData() {
        val nullResult = PostureEngine.analyze(null)
        assertEquals("Not enough data", nullResult.errorMessage)
        assertNotNull(nullResult.retakeTip)

        val missingEar = PosturePoseInput(
            ear = null,
            shoulder = PostureLandmarkPoint(100f, 200f),
            hip = PostureLandmarkPoint(100f, 400f)
        )
        val missingEarResult = PostureEngine.analyze(missingEar)
        assertEquals("Not enough data", missingEarResult.errorMessage)
        assertTrue(missingEarResult.retakeTip?.contains("Stand side-on") == true)
    }

    @Test
    fun testLowConfidenceReturnsError() {
        val lowConfInput = PosturePoseInput(
            ear = PostureLandmarkPoint(100f, 100f, 0.3f),
            shoulder = PostureLandmarkPoint(100f, 200f, 0.9f),
            hip = PostureLandmarkPoint(100f, 400f, 0.9f)
        )
        val result = PostureEngine.analyze(lowConfInput)
        assertEquals("Not enough data", result.errorMessage)
    }

    @Test
    fun testNeutralPostureClassification() {
        // Ear directly above shoulder (dx close to 0, angle near 90°)
        val input = PosturePoseInput(
            ear = PostureLandmarkPoint(102f, 100f, 0.95f),
            shoulder = PostureLandmarkPoint(100f, 200f, 0.95f),
            hip = PostureLandmarkPoint(100f, 400f, 0.95f)
        )
        val result = PostureEngine.analyze(input)
        assertNull(result.errorMessage)
        assertEquals(PostureBand.NEUTRAL, result.band)
        assertEquals(PostureConfidence.HIGH, result.confidence)
        assertTrue(result.craniovertebralAngle > 50f)
    }

    @Test
    fun testSlightlyForwardPostureClassification() {
        // Moderate forward offset (dx = 45, dy = 45 => angle = 45°, offset = 45/300 = 0.15)
        val input = PosturePoseInput(
            ear = PostureLandmarkPoint(145f, 155f, 0.85f),
            shoulder = PostureLandmarkPoint(100f, 200f, 0.85f),
            hip = PostureLandmarkPoint(100f, 500f, 0.85f)
        )
        val result = PostureEngine.analyze(input)
        assertNull(result.errorMessage)
        assertEquals(PostureBand.SLIGHTLY_FORWARD, result.band)
    }

    @Test
    fun testForwardPostureClassification() {
        // Large forward offset (dx = 150, dy = 50 => angle ~ 18°)
        val input = PosturePoseInput(
            ear = PostureLandmarkPoint(250f, 150f, 0.9f),
            shoulder = PostureLandmarkPoint(100f, 200f, 0.9f),
            hip = PostureLandmarkPoint(100f, 500f, 0.9f)
        )
        val result = PostureEngine.analyze(input)
        assertNull(result.errorMessage)
        assertEquals(PostureBand.FORWARD, result.band)
    }
}
