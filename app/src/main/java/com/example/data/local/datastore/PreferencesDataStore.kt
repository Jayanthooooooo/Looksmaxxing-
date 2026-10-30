package com.example.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lumora_preferences")

class PreferencesDataStore(private val context: Context) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        val MIRROR_PREVIEW = booleanPreferencesKey("mirror_preview")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val HIDE_NUMBERS = booleanPreferencesKey("hide_numbers")
        val BLOCK_SCREENSHOTS = booleanPreferencesKey("block_screenshots")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

        // Consent cache
        val CONSENT_ON_DEVICE = booleanPreferencesKey("consent_on_device")
        val CONSENT_CLOUD_AI = booleanPreferencesKey("consent_cloud_ai")
        val CONSENT_PHOTO_BACKUP = booleanPreferencesKey("consent_photo_backup")
        val CONSENT_ANALYTICS = booleanPreferencesKey("consent_analytics")
        val CONSENT_VERSION = intPreferencesKey("consent_version")
        val CONSENT_TIMESTAMP = longPreferencesKey("consent_timestamp")

        // Photo retention mode
        val PHOTO_RETENTION_MODE = stringPreferencesKey("photo_retention_mode") // "DONT_KEEP", "KEEP_ON_DEVICE", "BACKUP_30", "BACKUP_90", "BACKUP_365", "BACKUP_FOREVER"

        // Entitlement cache
        val ENTITLEMENT_TIER = stringPreferencesKey("entitlement_tier") // "FREE", "PRO"
        val ENTITLEMENT_VERIFIED = booleanPreferencesKey("entitlement_verified")
        val SIMULATE_PRO = booleanPreferencesKey("simulate_pro")

        // Last seen legal versions
        val LAST_SEEN_TERMS_VERSION = intPreferencesKey("last_seen_terms_version")
        val LAST_SEEN_PRIVACY_VERSION = intPreferencesKey("last_seen_privacy_version")
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "SYSTEM" }
    val mirrorPreviewFlow: Flow<Boolean> = context.dataStore.data.map { it[MIRROR_PREVIEW] ?: false }
    val hapticsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[HAPTICS_ENABLED] ?: true }
    val hideNumbersFlow: Flow<Boolean> = context.dataStore.data.map { it[HIDE_NUMBERS] ?: false }
    val blockScreenshotsFlow: Flow<Boolean> = context.dataStore.data.map { it[BLOCK_SCREENSHOTS] ?: false }
    val appLockEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[APP_LOCK_ENABLED] ?: false }
    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }

    val consentOnDeviceFlow: Flow<Boolean> = context.dataStore.data.map { it[CONSENT_ON_DEVICE] ?: false }
    val consentCloudAiFlow: Flow<Boolean> = context.dataStore.data.map { it[CONSENT_CLOUD_AI] ?: false }
    val consentPhotoBackupFlow: Flow<Boolean> = context.dataStore.data.map { it[CONSENT_PHOTO_BACKUP] ?: false }
    val consentAnalyticsFlow: Flow<Boolean> = context.dataStore.data.map { it[CONSENT_ANALYTICS] ?: false }

    val photoRetentionModeFlow: Flow<String> = context.dataStore.data.map { it[PHOTO_RETENTION_MODE] ?: "DONT_KEEP" }

    val entitlementTierFlow: Flow<String> = context.dataStore.data.map { it[ENTITLEMENT_TIER] ?: "FREE" }
    val entitlementVerifiedFlow: Flow<Boolean> = context.dataStore.data.map { it[ENTITLEMENT_VERIFIED] ?: false }
    val simulateProFlow: Flow<Boolean> = context.dataStore.data.map { it[SIMULATE_PRO] ?: false }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setMirrorPreview(enabled: Boolean) {
        context.dataStore.edit { it[MIRROR_PREVIEW] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTICS_ENABLED] = enabled }
    }

    suspend fun setHideNumbers(hide: Boolean) {
        context.dataStore.edit { it[HIDE_NUMBERS] = hide }
    }

    suspend fun setBlockScreenshots(block: Boolean) {
        context.dataStore.edit { it[BLOCK_SCREENSHOTS] = block }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }

    suspend fun saveConsents(onDevice: Boolean, cloudAi: Boolean, photoBackup: Boolean, analytics: Boolean, version: Int) {
        context.dataStore.edit {
            it[CONSENT_ON_DEVICE] = onDevice
            it[CONSENT_CLOUD_AI] = cloudAi
            it[CONSENT_PHOTO_BACKUP] = photoBackup
            it[CONSENT_ANALYTICS] = analytics
            it[CONSENT_VERSION] = version
            it[CONSENT_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    suspend fun setPhotoRetentionMode(mode: String) {
        context.dataStore.edit { it[PHOTO_RETENTION_MODE] = mode }
    }

    suspend fun setEntitlement(tier: String, verified: Boolean) {
        context.dataStore.edit {
            it[ENTITLEMENT_TIER] = tier
            it[ENTITLEMENT_VERIFIED] = verified
        }
    }

    suspend fun setSimulatePro(simulate: Boolean) {
        context.dataStore.edit { it[SIMULATE_PRO] = simulate }
    }

    suspend fun clearSessionKeys() {
        context.dataStore.edit {
            it.remove(CONSENT_ON_DEVICE)
            it.remove(CONSENT_CLOUD_AI)
            it.remove(CONSENT_PHOTO_BACKUP)
            it.remove(CONSENT_ANALYTICS)
            it.remove(ONBOARDING_COMPLETED)
            it.remove(ENTITLEMENT_TIER)
            it.remove(ENTITLEMENT_VERIFIED)
            it.remove(SIMULATE_PRO)
        }
    }
}
