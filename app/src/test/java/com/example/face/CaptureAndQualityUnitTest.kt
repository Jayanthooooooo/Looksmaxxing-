package com.example.face

import android.graphics.Rect
import com.example.face.quality.CaptureQualityEvaluator
import com.example.face.quality.GateEvaluation
import com.example.face.quality.LandmarkAggregator
import com.example.face.quality.OneEuroFilter
import com.example.face.quality.Point3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CaptureAndQualityUnitTest {

    @Test
    fun testOneEuroFilterSmoothing() {
        val filter = OneEuroFilter(minCutoff = 1.0, beta = 0.007, dCutoff = 1.0)
        val initial = filter.filter(100.0, 0L)
        assertEquals(100.0, initial, 0.001)

        // Consecutive identical values
        val second = filter.filter(100.0, 33L)
        assertEquals(100.0, second, 0.001)

        // Step change (should be smoothed)
        val third = filter.filter(110.0, 66L)
        assertTrue("Output should be smoothed between 100 and 110", third > 100.0 && third < 110.0)
    }

    @Test
    fun testLandmarkAggregatorBurstMedian() {
        val aggregator = LandmarkAggregator()

        // Create an 8-frame burst for landmark 1 with an outlier spike in frame 4
        val burst = listOf(
            mapOf(1 to Point3D(10f, 20f, 0f)),
            mapOf(1 to Point3D(10.2f, 20.1f, 0f)),
            mapOf(1 to Point3D(9.8f, 19.9f, 0f)),
            mapOf(1 to Point3D(10.1f, 20.2f, 0f)),
            mapOf(1 to Point3D(50.0f, 80.0f, 0f)), // Outlier spike!
            mapOf(1 to Point3D(10.0f, 20.0f, 0f)),
            mapOf(1 to Point3D(10.1f, 20.0f, 0f)),
            mapOf(1 to Point3D(9.9f, 20.1f, 0f))
        )

        val aggregated = aggregator.aggregateMedianBurst(burst)
        val medianPoint = aggregated[1]
        org.junit.Assert.assertNotNull(medianPoint)

        // The median should reject the outlier 50.0/80.0 and remain close to 10.0 / 20.0
        assertTrue("Median X should filter out outlier", medianPoint!!.x in 9.9f..10.2f)
        assertTrue("Median Y should filter out outlier", medianPoint.y in 19.9f..20.2f)
    }

    @Test
    fun testCaptureQualityEvaluatorPose() {
        val evaluator = CaptureQualityEvaluator()

        // Front scan within boundaries (|yaw| <= 8, |pitch| <= 8, |roll| <= 6)
        assertTrue(evaluator.evaluatePose(0f, 0f, 0f, isSideScan = false).isReady)
        assertTrue(evaluator.evaluatePose(7f, -5f, 4f, isSideScan = false).isReady)

        // Front scan exceeding bounds
        assertFalse(evaluator.evaluatePose(12f, 0f, 0f, isSideScan = false).isReady)
        assertFalse(evaluator.evaluatePose(0f, 10f, 0f, isSideScan = false).isReady)
        assertFalse(evaluator.evaluatePose(0f, 0f, 8f, isSideScan = false).isReady)

        // Side scan (requires yaw 70-90 deg)
        assertTrue(evaluator.evaluatePose(75f, 0f, 0f, isSideScan = true).isReady)
        assertTrue(evaluator.evaluatePose(-80f, 0f, 0f, isSideScan = true).isReady)
        assertFalse(evaluator.evaluatePose(45f, 0f, 0f, isSideScan = true).isReady)
        assertFalse(evaluator.evaluatePose(10f, 0f, 0f, isSideScan = true).isReady)
    }

    @Test
    fun testCaptureQualityEvaluatorDistance() {
        val evaluator = CaptureQualityEvaluator()
        val frameWidth = 1000
        val frameHeight = 1000

        // Optimal face width: 50% of frame width
        val optimalBox = Rect(250, 200, 750, 700)
        assertTrue(evaluator.evaluateDistance(optimalBox, frameWidth, frameHeight).isReady)

        // Too far: 20% of frame width
        val tooFarBox = Rect(400, 400, 600, 600)
        assertFalse(evaluator.evaluateDistance(tooFarBox, frameWidth, frameHeight).isReady)

        // Too close: 85% of frame width
        val tooCloseBox = Rect(50, 50, 900, 900)
        assertFalse(evaluator.evaluateDistance(tooCloseBox, frameWidth, frameHeight).isReady)
    }

    @Test
    fun testCaptureQualityScoreComputation() {
        val evaluator = CaptureQualityEvaluator()

        // All gates ready
        val ready = GateEvaluation(true)
        val notReady = GateEvaluation(false, "Flag")

        val perfectReport = evaluator.computeOverallQualityReport(ready, ready, ready, ready, ready, faceCount = 1)
        assertEquals(100, perfectReport.score)
        assertTrue(perfectReport.flags.isEmpty())

        // Multiple faces
        val multiFaceReport = evaluator.computeOverallQualityReport(ready, ready, ready, ready, ready, faceCount = 2)
        assertEquals(0, multiFaceReport.score)
        assertTrue(multiFaceReport.flags.any { it.contains("More than one face") })

        // No face
        val noFaceReport = evaluator.computeOverallQualityReport(ready, ready, ready, ready, ready, faceCount = 0)
        assertEquals(0, noFaceReport.score)
        assertTrue(noFaceReport.flags.any { it.contains("couldn't find a face") })

        // One gate failing
        val lightingFailReport = evaluator.computeOverallQualityReport(ready, ready, notReady, ready, ready, faceCount = 1)
        assertEquals(75, lightingFailReport.score)
    }
}
