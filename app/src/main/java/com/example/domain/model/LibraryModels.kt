package com.example.domain.model

enum class EvidenceLevel(val label: String, val badgeText: String) {
    STRONG("Strong", "Strong Evidence"),
    MODERATE("Moderate", "Moderate Evidence"),
    LIMITED("Limited", "Limited Evidence"),
    MYTH("Debunked", "Debunked Myth"),
    NOT_RECOMMENDED("Avoid", "Not Recommended")
}

enum class ArticleCost(val label: String) {
    LOW("Low cost"),
    MEDIUM("Medium cost"),
    HIGH("High cost")
}

data class LibraryArticle(
    val id: String,
    val category: String,
    val title: String,
    val summary: String,
    val evidenceLevel: EvidenceLevel,
    val whatToDo: List<String>,
    val timeToSeeResults: String,
    val cost: ArticleCost,
    val risks: List<String>,
    val seeAProfessionalIf: List<String>,
    val changeability: ChangeabilityTag,
    val minAge: Int = 13,
    val tags: List<String> = emptyList(),
    val disclaimer: String = "Educational information, not medical advice."
) {
    val isTeenSafe: Boolean
        get() = minAge <= 17
}

data class GlossaryTerm(
    val id: String,
    val term: String,
    val plainExplanation: String,
    val whatItMeasures: String,
    val limits: String,
    val isCommunityOnly: Boolean = false
)
