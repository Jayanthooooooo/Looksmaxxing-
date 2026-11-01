package com.example.face.shape

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceShapeEngineUnitTest {

    @Test
    fun testLowQualityOrMissingDataReturnsError() {
        val resultNull = FaceShapeEngine.classify(null)
        assertNotNull(resultNull.errorMessage)
        assertTrue(resultNull.topMatches.isEmpty())

        val resultLowConfidence = FaceShapeEngine.classify(
            FacialMeasurements(
                faceWidthCheekbones = 100f,
                jawWidth = 80f,
                faceHeight = 150f,
                foreheadWidth = 85f,
                chinAngle = 90f,
                photoConfidence = 0.3f
            )
        )
        assertNotNull(resultLowConfidence.errorMessage)
    }

    @Test
    fun testOvalClassification() {
        // Oval: hwRatio ~ 1.55, jwRatio ~ 0.80, fwRatio ~ 0.85
        val measurements = FacialMeasurements(
            faceWidthCheekbones = 100f,
            jawWidth = 80f,
            faceHeight = 155f,
            foreheadWidth = 85f,
            chinAngle = 80f,
            photoConfidence = 0.9f
        )
        val result = FaceShapeEngine.classify(measurements)
        assertTrue(result.errorMessage == null)
        assertTrue(result.topMatches.isNotEmpty())
        assertEquals(FaceShapeType.OVAL, result.topMatches.first().shape)
    }

    @Test
    fun testSquareClassification() {
        // Square: hwRatio ~ 1.30, jwRatio ~ 0.94 (jaw width close to cheek width)
        val measurements = FacialMeasurements(
            faceWidthCheekbones = 100f,
            jawWidth = 94f,
            faceHeight = 130f,
            foreheadWidth = 92f,
            chinAngle = 110f,
            photoConfidence = 0.9f
        )
        val result = FaceShapeEngine.classify(measurements)
        assertTrue(result.errorMessage == null)
        assertEquals(FaceShapeType.SQUARE, result.topMatches.first().shape)
    }

    @Test
    fun testRoundClassification() {
        // Round: hwRatio ~ 1.25, jwRatio ~ 0.90
        val measurements = FacialMeasurements(
            faceWidthCheekbones = 100f,
            jawWidth = 90f,
            faceHeight = 125f,
            foreheadWidth = 88f,
            chinAngle = 100f,
            photoConfidence = 0.9f
        )
        val result = FaceShapeEngine.classify(measurements)
        assertTrue(result.errorMessage == null)
        assertEquals(FaceShapeType.ROUND, result.topMatches.first().shape)
    }
}
