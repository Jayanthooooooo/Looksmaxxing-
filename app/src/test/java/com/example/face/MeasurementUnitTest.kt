package com.example.face

import com.example.domain.model.RangePosition
import com.example.face.measure.BandCategory
import com.example.face.measure.MeasurementCalculator
import com.example.face.measure.NoiseFloorEvaluator
import com.example.face.measure.RangePositionMapper
import com.example.face.quality.Point3D
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MeasurementUnitTest {

    private val calculator = MeasurementCalculator()

    private fun createSyntheticFace(
        asymmetryOffset: Float = 0f,
        upperHeight: Float = 50f,
        midHeight: Float = 50f,
        lowerHeight: Float = 50f
    ): Map<Int, Point3D> {
        val yTrichion = -100f
        val yGlabella = yTrichion + upperHeight
        val ySubnasale = yGlabella + midHeight
        val yMenton = ySubnasale + lowerHeight
        val yStomion = ySubnasale + (lowerHeight / 3f)

        return mapOf(
            10 to Point3D(0f, yTrichion),                       // Trichion (forehead top)
            151 to Point3D(0f, yGlabella),                     // Glabella (brow level)
            9 to Point3D(0f, yGlabella + 10f),                 // Nasion
            164 to Point3D(0f, ySubnasale),                    // Subnasale (nose base)
            0 to Point3D(0f, yStomion),                        // Stomion (upper lip)
            17 to Point3D(0f, yStomion + 5f),                  // Stomion (lower lip)
            152 to Point3D(0f, yMenton),                       // Menton (chin tip)

            234 to Point3D(-70f, yGlabella + 20f),             // Zygion Left
            454 to Point3D(70f + asymmetryOffset, yGlabella + 20f), // Zygion Right

            33 to Point3D(-50f, yGlabella + 10f),              // Eye Outer Left
            133 to Point3D(-20f, yGlabella + 10f),             // Eye Inner Left
            362 to Point3D(20f, yGlabella + 10f),              // Eye Inner Right
            263 to Point3D(50f + asymmetryOffset, yGlabella + 10f), // Eye Outer Right

            102 to Point3D(-15f, ySubnasale - 5f),             // Alare Left
            331 to Point3D(15f, ySubnasale - 5f),              // Alare Right

            61 to Point3D(-30f, yStomion),                     // Cheilion Left
            291 to Point3D(30f + asymmetryOffset, yStomion),   // Cheilion Right

            132 to Point3D(-60f, ySubnasale + 20f),            // Gonion Left
            361 to Point3D(60f, ySubnasale + 20f)              // Gonion Right
        )
    }

    @Test
    fun testSymmetricalFaceSymmetryScore() {
        val symmetricFace = createSyntheticFace(asymmetryOffset = 0f)
        val result = calculator.calculate(symmetricFace)

        val symmetryMetric = result.metrics["facial_symmetry_index"]
        assertTrue(symmetryMetric != null)
        assertTrue(
            "Symmetric face should have high symmetry score >= 97, was ${symmetryMetric!!.value}",
            symmetryMetric.value >= 97f
        )
        assertTrue(result.symmetryIndex >= 97)
    }

    @Test
    fun testAsymmetricFaceLowerSymmetryScore() {
        // Shift right landmarks by 25 pixels
        val asymmetricFace = createSyntheticFace(asymmetryOffset = 25f)
        val result = calculator.calculate(asymmetricFace)

        val symmetryMetric = result.metrics["facial_symmetry_index"]
        assertTrue(symmetryMetric != null)
        assertTrue(
            "Asymmetric face should have reduced symmetry score < 90, was ${symmetryMetric!!.value}",
            symmetryMetric.value < 90f
        )
    }

    @Test
    fun testVerticalThirdsCalculation() {
        val balancedFace = createSyntheticFace(upperHeight = 50f, midHeight = 50f, lowerHeight = 50f)
        val result = calculator.calculate(balancedFace)

        val upper = result.metrics["vertical_thirds_upper"]!!.value
        val mid = result.metrics["vertical_thirds_middle"]!!.value
        val lower = result.metrics["vertical_thirds_lower"]!!.value

        assertEquals(33.3f, upper, 0.5f)
        assertEquals(33.3f, mid, 0.5f)
        assertEquals(33.3f, lower, 0.5f)
        assertEquals(100f, upper + mid + lower, 1.0f)
    }

    @Test
    fun testRangePositionMapperTypicalAndOutlier() {
        val typicalEvaluation = RangePositionMapper.evaluate("vertical_thirds_upper", 33.3f)
        assertEquals(BandCategory.TYPICAL, typicalEvaluation.category)
        assertEquals(RangePosition.WITHIN, typicalEvaluation.rangePosition)
        assertTrue(typicalEvaluation.displayLabel.contains("Within typical"))

        val lowEvaluation = RangePositionMapper.evaluate("vertical_thirds_upper", 22.0f)
        assertEquals(BandCategory.BELOW_TYPICAL, lowEvaluation.category)
        assertTrue(lowEvaluation.displayLabel.contains("Below typical"))

        val highEvaluation = RangePositionMapper.evaluate("vertical_thirds_upper", 45.0f)
        assertEquals(BandCategory.ABOVE_TYPICAL, highEvaluation.category)
        assertTrue(highEvaluation.displayLabel.contains("Above typical"))
    }

    @Test
    fun testNoiseFloorEvaluatorSubThresholdAndSignificant() {
        // Metric noise floor for vertical thirds is 1.5%
        val belowNoise = NoiseFloorEvaluator.evaluate(
            metricId = "vertical_thirds_upper",
            baselineValue = 33.0f,
            currentValue = 33.6f
        )
        assertFalse("Delta of 0.6% should not be significant", belowNoise.isSignificant)
        assertTrue(belowNoise.description.contains("within the measurement noise floor"))

        val aboveNoise = NoiseFloorEvaluator.evaluate(
            metricId = "vertical_thirds_upper",
            baselineValue = 33.0f,
            currentValue = 35.5f
        )
        assertTrue("Delta of 2.5% should be significant", aboveNoise.isSignificant)
        assertTrue(aboveNoise.description.contains("Statistically significant"))
    }
}
