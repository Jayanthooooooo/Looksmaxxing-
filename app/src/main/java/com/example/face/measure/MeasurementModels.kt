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
    val noiseFloor: Float
)

enum class BandCategory(val label: String) {
    BELOW_TYPICAL("Below typical band"),
    TYPICAL("Within typical band"),
    ABOVE_TYPICAL("Above typical band")
}

data class BandPosition(
    val category: BandCategory,
    val rangePosition: RangePosition,
    val relativePosition: Float, // 0.0 at min, 0.5 at canonical, 1.0 at max
    val displayLabel: String,
    val citation: String = "Farkas et al., 1985"
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
    val proportionIndex: Int, // 0 - 100 overall adherence to classical harmony
    val symmetryIndex: Int    // 0 - 100 bilateral symmetry score
)
