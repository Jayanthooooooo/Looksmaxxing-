package com.example.face

import com.example.face.measure.MeasurementCalculator
import com.example.face.quality.CaptureQualityEvaluator
import com.example.face.quality.Point3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceScanTest {

    private val evaluator = CaptureQualityEvaluator()
    private val calculator = MeasurementCalculator()

    @Test
    fun testPoseGatePassAndFail() {
        // yaw 3 degrees passes front scan
        val passEval = evaluator.evaluatePose(yaw = 3.0f, pitch = 2.0f, roll = 1.0f, isSideScan = false)
        assertTrue("Front scan with yaw=3 should pass", passEval.isReady)

        // yaw 20 degrees fails front scan
        val failEval = evaluator.evaluatePose(yaw = 20.0f, pitch = 2.0f, roll = 1.0f, isSideScan = false)
        assertFalse("Front scan with yaw=20 should fail", failEval.isReady)
        assertEquals("Look directly at the camera.", failEval.flag)
    }

    @Test
    fun testLightingGateLowLumaFails() {
        // luma 40 fails (< 70 min)
        val darkEval = evaluator.evaluateLighting(meanLuma = 40.0f, leftRightAsymmetry = 0.05f)
        assertFalse("Lighting with mean luma 40 should fail", darkEval.isReady)
        assertTrue("Flag should explain dark lighting", darkEval.flag!!.contains("dark"))

        // optimal luma 120 passes
        val goodEval = evaluator.evaluateLighting(meanLuma = 120.0f, leftRightAsymmetry = 0.05f)
        assertTrue("Lighting with mean luma 120 should pass", goodEval.isReady)
    }

    @Test
    fun testStillnessGateDriftFails() {
        // 0.05 drift fails (> 0.015 max drift)
        val xHistory = listOf(0.50f, 0.55f)
        val yHistory = listOf(0.50f, 0.50f)
        val unsteadyEval = evaluator.evaluateStillness(xHistory, yHistory)
        assertFalse("Drift of 0.05 should fail stillness gate", unsteadyEval.isReady)
        assertEquals("Hold phone steady.", unsteadyEval.flag)

        // 0.005 drift passes
        val steadyX = listOf(0.500f, 0.505f)
        val steadyY = listOf(0.500f, 0.502f)
        val steadyEval = evaluator.evaluateStillness(steadyX, steadyY)
        assertTrue("Drift of 0.005 should pass stillness gate", steadyEval.isReady)
    }

    @Test
    fun testMeasurementCalculatorNullUpperThird() {
        // Create canonical 468 landmark points
        val landmarks = mutableMapOf<Int, Point3D>()
        for (i in 0 until 468) {
            landmarks[i] = Point3D(0f, 0f, 0f)
        }
        // Essential landmarks
        landmarks[1] = Point3D(0f, -10f, 15f)     // nose tip
        landmarks[2] = Point3D(0f, 10f, 5f)       // subnasale
        landmarks[9] = Point3D(0f, -40f, 0f)      // nasion
        landmarks[10] = Point3D(0f, -90f, 0f)     // forehead
        landmarks[13] = Point3D(0f, 35f, 0f)      // stomion upper
        landmarks[14] = Point3D(0f, 40f, 0f)      // stomion lower
        landmarks[33] = Point3D(-50f, -20f, 0f)   // eye outer left
        landmarks[46] = Point3D(-45f, -35f, 0f)
        landmarks[55] = Point3D(-20f, -35f, 0f)
        landmarks[61] = Point3D(-30f, 40f, 0f)    // cheilion left
        landmarks[102] = Point3D(-15f, 10f, 0f)
        landmarks[132] = Point3D(-50f, 60f, 0f)
        landmarks[133] = Point3D(-20f, -20f, 0f)  // eye inner left
        landmarks[145] = Point3D(-35f, -15f, 0f)
        landmarks[151] = Point3D(0f, -50f, 0f)    // glabella
        landmarks[152] = Point3D(0f, 90f, 0f)     // menton (chin)
        landmarks[159] = Point3D(-35f, -25f, 0f)
        landmarks[234] = Point3D(-70f, 0f, 0f)    // zygion left (cheek)
        landmarks[263] = Point3D(50f, -20f, 0f)   // eye outer right
        landmarks[291] = Point3D(30f, 40f, 0f)    // cheilion right
        landmarks[331] = Point3D(15f, 10f, 0f)
        landmarks[361] = Point3D(50f, 60f, 0f)
        landmarks[362] = Point3D(20f, -20f, 0f)   // eye inner right
        landmarks[454] = Point3D(70f, 0f, 0f)     // zygion right (cheek)

        val result = calculator.calculate(landmarks)
        assertTrue("Calculation should succeed", result.isSuccess)
        val metrics = result.getOrNull()!!.metrics

        // Upper third must be null without trichion landmark, labeled "Not measured"
        val upperThird = metrics["vertical_thirds_upper"]
        assertNull("Upper third value must be null", upperThird?.value)
        assertEquals("Not measured", upperThird?.rangePositionText)

        // Non-null metrics are properly calculated
        val facialIndex = metrics["facial_index"]
        assertTrue("Facial index must be calculated", facialIndex?.value != null && facialIndex.value!! > 0f)

        val symmetry = metrics["facial_symmetry_index"]
        assertTrue("Symmetry index must be calculated", symmetry?.value != null && symmetry.value!! > 0f)

        // Overall score must be valid
        val overallScore = result.getOrNull()!!.overallFaceScore
        assertTrue("Overall score must be non-null and > 0", overallScore in 10f..99f)
    }
}
