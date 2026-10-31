package com.example.face.measure

import com.example.domain.model.ChangeabilityTag
import com.example.domain.model.MetricValue
import com.example.domain.model.RangePosition

data class ReferenceBand(
    val id: String,
    val name: String,
    val description: String,
    val unit: String,
    val canonicalValue: Float,
    val typicalMin: Float,
    val typicalMax: Float,
    val noiseFloor: Float,
    val ideal: Float,
    val tolerance: Float
)

object ScoreWeights {
    const val SYMMETRY = 0.20f
    const val PROPORTIONS = 0.15f
    const val EYES = 0.15f
    const val NOSE = 0.10f
    const val LIPS = 0.10f
    const val JAW_CHIN = 0.15f
    const val SKIN = 0.15f

    init {
        val sum = SYMMETRY + PROPORTIONS + EYES + NOSE + LIPS + JAW_CHIN + SKIN
        require(kotlin.math.abs(sum - 1.0f) < 0.001f) { "ScoreWeights must sum to 1.0, got $sum" }
    }
}

enum class BandCategory(val label: String) {
    BELOW_TYPICAL("Below typical band"),
    TYPICAL("Within typical band"),
    ABOVE_TYPICAL("Above typical band")
}

data class BandPosition(
    val category: BandCategory,
    val rangePosition: RangePosition,
    val relativePosition: Float,
    val displayLabel: String,
    val citation: String = "Farkas et al., 1985; Porras et al., 2017"
)

data class NoiseFloorCheck(
    val metricId: String,
    val metricName: String,
    val delta: Float,
    val noiseFloor: Float,
    val isSignificant: Boolean,
    val description: String
)

data class CalculatedMetrics(
    val metrics: Map<String, MetricValue>,
    val proportionIndex: Int,
    val symmetryIndex: Int,
    val groupScores: Map<String, Float>,
    val overallFaceScore: Float,
    val confidence: Float,
    val uncertainty: Float
)
