package com.example.domain.model

enum class RangePosition {
    WITHIN,
    NEAR,
    OUTSIDE,
    DESCRIPTIVE
}

enum class ChangeabilityTag(val label: String) {
    STRUCTURE("Structure"),
    SOFT_TISSUE("Soft tissue"),
    PRESENTATION("Presentation"),
    SKIN("Skin")
}

data class MetricValue(
    val id: String,
    val name: String,
    val value: Float,
    val sd: Float,
    val unit: String,
    val rangePosition: RangePosition,
    val rangePositionText: String,
    val reliability: Float,
    val changeability: ChangeabilityTag,
    val typicalMin: Float = 0f,
    val typicalMax: Float = 0f,
    val isProvisional: Boolean = false,
    val sourceNote: String = ""
)

data class QualityReport(
    val score: Int, // 0 - 100
    val flags: List<String>,
    val perGate: Map<String, Boolean>
)

data class SkinDescriptor(
    val severity: String, // none, mild, moderate, marked
    val confidence: String // low, medium, high
)

data class SkinAnalysisResult(
    val clarityIndex: Int, // 0 - 100
    val descriptors: Map<String, SkinDescriptor>,
    val components: Map<String, Float>,
    val needsClinician: Boolean = false,
    val clinicianReason: String? = null,
    val notes: List<String> = emptyList()
)

data class ScanResult(
    val id: String,
    val createdAt: Long,
    val kind: String, // "front" or "side"
    val engineVersion: String,
    val qualityReport: QualityReport,
    val metrics: Map<String, MetricValue>,
    val indices: Map<String, Int>, // "proportions", "symmetry", "clarity"
    val skinResult: SkinAnalysisResult?,
    val photoFileId: String?,
    val note: String = ""
)
