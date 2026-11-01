package com.example.face.shape

enum class FaceShapeType(val displayName: String) {
    OVAL("Oval"),
    ROUND("Round"),
    SQUARE("Square"),
    RECTANGLE("Rectangle / Oblong"),
    HEART("Heart"),
    DIAMOND("Diamond")
}

enum class ConfidenceLevel {
    HIGH, MEDIUM, LOW
}

data class FaceShapeMatch(
    val shape: FaceShapeType,
    val score: Float,
    val confidence: ConfidenceLevel,
    val reasoning: String
)

data class FaceShapeAnalysisResult(
    val topMatches: List<FaceShapeMatch>,
    val errorMessage: String? = null
)

data class FacialMeasurements(
    val faceWidthCheekbones: Float,
    val jawWidth: Float,
    val faceHeight: Float,
    val foreheadWidth: Float,
    val chinAngle: Float,
    val photoConfidence: Float
)

object FaceShapeEngine {
    fun classify(measurements: FacialMeasurements?): FaceShapeAnalysisResult {
        if (measurements == null || measurements.photoConfidence < 0.5f || measurements.faceHeight <= 0f || measurements.faceWidthCheekbones <= 0f) {
            return FaceShapeAnalysisResult(
                topMatches = emptyList(),
                errorMessage = "Not enough data, retake a straight, evenly lit photo"
            )
        }

        val hwRatio = measurements.faceHeight / measurements.faceWidthCheekbones
        val jwRatio = measurements.jawWidth / measurements.faceWidthCheekbones
        val fwRatio = measurements.foreheadWidth / measurements.faceWidthCheekbones
        val fjRatio = measurements.foreheadWidth / if (measurements.jawWidth > 0f) measurements.jawWidth else 1f

        val scores = mutableMapOf<FaceShapeType, Float>()

        val ovalScore = gaussian(hwRatio, 1.55f, 0.12f) * 0.4f +
                gaussian(jwRatio, 0.80f, 0.08f) * 0.3f +
                gaussian(fwRatio, 0.85f, 0.08f) * 0.3f
        scores[FaceShapeType.OVAL] = ovalScore

        val roundScore = gaussian(hwRatio, 1.25f, 0.10f) * 0.5f +
                gaussian(jwRatio, 0.90f, 0.08f) * 0.3f +
                gaussian(fwRatio, 0.88f, 0.08f) * 0.2f
        scores[FaceShapeType.ROUND] = roundScore

        val squareScore = gaussian(hwRatio, 1.30f, 0.12f) * 0.3f +
                gaussian(jwRatio, 0.94f, 0.06f) * 0.5f +
                gaussian(fwRatio, 0.92f, 0.08f) * 0.2f
        scores[FaceShapeType.SQUARE] = squareScore

        val rectScore = gaussian(hwRatio, 1.75f, 0.15f) * 0.6f +
                gaussian(jwRatio, 0.88f, 0.08f) * 0.2f +
                gaussian(fwRatio, 0.88f, 0.08f) * 0.2f
        scores[FaceShapeType.RECTANGLE] = rectScore

        val heartScore = gaussian(hwRatio, 1.50f, 0.12f) * 0.3f +
                gaussian(jwRatio, 0.70f, 0.08f) * 0.4f +
                gaussian(fjRatio, 1.20f, 0.15f) * 0.3f
        scores[FaceShapeType.HEART] = heartScore

        val diamondScore = gaussian(hwRatio, 1.50f, 0.12f) * 0.3f +
                gaussian(jwRatio, 0.72f, 0.08f) * 0.35f +
                gaussian(fwRatio, 0.75f, 0.08f) * 0.35f
        scores[FaceShapeType.DIAMOND] = diamondScore

        val sorted = scores.entries.sortedByDescending { it.value }
        val top1 = sorted[0]
        val top2 = sorted[1]

        val conf1 = when {
            top1.value > 0.75f && measurements.photoConfidence > 0.8f -> ConfidenceLevel.HIGH
            top1.value > 0.55f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        val conf2 = when {
            top2.value > 0.60f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        val matches = listOf(
            FaceShapeMatch(
                shape = top1.key,
                score = top1.value.coerceIn(0f, 1f),
                confidence = conf1,
                reasoning = generateReasoning(top1.key, hwRatio, jwRatio, fwRatio)
            ),
            FaceShapeMatch(
                shape = top2.key,
                score = top2.value.coerceIn(0f, 1f),
                confidence = conf2,
                reasoning = generateReasoning(top2.key, hwRatio, jwRatio, fwRatio)
            )
        )

        return FaceShapeAnalysisResult(topMatches = matches)
    }

    private fun gaussian(value: Float, mean: Float, sigma: Float): Float {
        val diff = value - mean
        return kotlin.math.exp(- (diff * diff) / (2 * sigma * sigma))
    }

    private fun generateReasoning(shape: FaceShapeType, hw: Float, jw: Float, fw: Float): String {
        return when (shape) {
            FaceShapeType.OVAL -> "Balanced height-to-width ratio with gently tapered jaw."
            FaceShapeType.ROUND -> "Compact proportions with equal width and height dimensions."
            FaceShapeType.SQUARE -> "Strong mandibular width relative to cheekbones with defined angles."
            FaceShapeType.RECTANGLE -> "Elongated vertical proportions with balanced width."
            FaceShapeType.HEART -> "Prominent forehead with a graceful taper toward a narrower chin."
            FaceShapeType.DIAMOND -> "Distinctly prominent cheekbones with narrower forehead and jawline."
        }
    }
}
