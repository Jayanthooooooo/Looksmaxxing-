package com.example.domain.model

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class NeedsOnboarding(val step: Int = 1) : SessionState
    data object NeedsConsent : SessionState
    data class Ready(val userId: String) : SessionState
}

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val birthYear: Int? = null,
    val skinType: String = "not_sure", // dry, oily, combination, normal, sensitive, not_sure
    val concerns: List<String> = emptyList(), // acne, marks, redness, dryness, oiliness, texture, dark_circles, fine_lines
    val heightCm: Float? = null,
    val referenceRangePref: String = "prefer_not_to_say" // prefer_not_to_say, female_typical, male_typical, neutral
) {
    val isTeen: Boolean
        get() = birthYear != null && (java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - birthYear) < 18

    val isUnder13: Boolean
        get() = birthYear != null && (java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - birthYear) < 13
}

data class ConsentState(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val onDeviceAnalysis: Boolean = false,
    val cloudAiAnalysis: Boolean = false,
    val cloudPhotoBackup: Boolean = false,
    val analytics: Boolean = false
)
