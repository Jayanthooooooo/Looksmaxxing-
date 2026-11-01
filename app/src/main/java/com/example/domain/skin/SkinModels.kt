package com.example.domain.skin

enum class ConflictStatus(val displayName: String) {
    FINE_TOGETHER("Fine together"),
    USE_WITH_CARE("Use with care"),
    BETTER_SEPARATED("Better separated")
}

data class SkinIngredient(
    val id: String,
    val name: String,
    val category: String,
    val whatItDoes: String,
    val bestTime: String,
    val startFrequency: String,
    val commonIrritation: String,
    val isTeenRestricted: Boolean = false,
    val isPregnancyRestricted: Boolean = false
)

data class ConflictRule(
    val id1: String,
    val id2: String,
    val status: ConflictStatus,
    val reason: String,
    val saferWay: String
)

data class ConflictCheckResult(
    val ingredient1: SkinIngredient,
    val ingredient2: SkinIngredient,
    val status: ConflictStatus,
    val reason: String,
    val saferWay: String
)

data class RoutineStep(
    val order: Int,
    val stepType: String, // "Cleanse", "Treat", "Moisturise", "Sunscreen"
    val title: String,
    val description: String,
    val isActive: Boolean = false,
    val timeOfDay: String // "AM", "PM", "Both"
)

data class StartSlowPhase(
    val weeks: String,
    val activeName: String,
    val frequency: String,
    val tip: String
)

data class RoutineBuildResult(
    val amSteps: List<RoutineStep>,
    val pmSteps: List<RoutineStep>,
    val activeCount: Int,
    val activeWarning: String? = null,
    val patchTestReminder: String = "Apply a small amount behind ear or on inner forearm for 24h before full face application.",
    val startSlowPhases: List<StartSlowPhase>
)

data class SunscreenGuidance(
    val uvLevel: String,
    val isOutdoors: Boolean,
    val title: String,
    val recommendedSpf: String,
    val reapplyInterval: String,
    val mainTip: String,
    val details: String
)
