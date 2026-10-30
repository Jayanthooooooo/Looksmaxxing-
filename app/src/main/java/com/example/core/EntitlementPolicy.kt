package com.example.core

enum class EntitlementTier {
    FREE,
    PRO
}

enum class EntitlementFeature {
    UNLIMITED_LOCAL_SCANS,
    AI_SKIN_READ,
    COACH_CHAT,
    CLOUD_PHOTO_BACKUP,
    WEEKLY_AI_REPORT,
    PDF_EXPORT,
    ADVANCED_COMPARE
}

data class EntitlementPolicy(
    val freeAiSkinReadsPer7Days: Int = 1,
    val proAiSkinReadsPerDay: Int = 10,
    val freeCoachMsgsPerDay: Int = 5,
    val proCoachMsgsPerDay: Int = 100,
    val allowCloudBackupFree: Boolean = false,
    val allowCloudBackupPro: Boolean = true,
    val allowWeeklyReportFree: Boolean = false,
    val allowWeeklyReportPro: Boolean = true
) {
    fun maxAiSkinReads(tier: EntitlementTier): Int = when (tier) {
        EntitlementTier.FREE -> freeAiSkinReadsPer7Days
        EntitlementTier.PRO -> proAiSkinReadsPerDay
    }

    fun maxCoachMessagesPerDay(tier: EntitlementTier): Int = when (tier) {
        EntitlementTier.FREE -> freeCoachMsgsPerDay
        EntitlementTier.PRO -> proCoachMsgsPerDay
    }

    fun canUseFeature(tier: EntitlementTier, feature: EntitlementFeature): Boolean = when (feature) {
        EntitlementFeature.UNLIMITED_LOCAL_SCANS -> true
        EntitlementFeature.AI_SKIN_READ -> true
        EntitlementFeature.COACH_CHAT -> true
        EntitlementFeature.CLOUD_PHOTO_BACKUP -> tier == EntitlementTier.PRO
        EntitlementFeature.WEEKLY_AI_REPORT -> tier == EntitlementTier.PRO
        EntitlementFeature.PDF_EXPORT -> tier == EntitlementTier.PRO
        EntitlementFeature.ADVANCED_COMPARE -> tier == EntitlementTier.PRO
    }
}
