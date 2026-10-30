package com.example.data.repo

import com.example.core.AppDispatchers
import com.example.core.Logger
import com.example.core.Outcome
import com.example.data.local.datastore.PreferencesDataStore
import com.example.data.remote.FirebaseModule
import com.example.domain.model.ConsentState
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ConsentRepository(
    private val firebaseModule: FirebaseModule,
    private val preferencesDataStore: PreferencesDataStore,
    private val dispatchers: AppDispatchers
) {
    val consentStateFlow: Flow<ConsentState> = combine(
        preferencesDataStore.consentOnDeviceFlow,
        preferencesDataStore.consentCloudAiFlow,
        preferencesDataStore.consentPhotoBackupFlow,
        preferencesDataStore.consentAnalyticsFlow
    ) { onDevice, cloudAi, photoSync, analytics ->
        ConsentState(
            version = 1,
            onDeviceAnalysis = onDevice,
            cloudAiAnalysis = cloudAi,
            cloudPhotoBackup = photoSync,
            analytics = analytics
        )
    }

    suspend fun saveConsents(uid: String, state: ConsentState): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            // Save to DataStore locally
            preferencesDataStore.saveConsents(
                onDevice = state.onDeviceAnalysis,
                cloudAi = state.cloudAiAnalysis,
                photoBackup = state.cloudPhotoBackup,
                analytics = state.analytics,
                version = state.version
            )

            // Save to Firestore under users/{uid}
            val consentData = mapOf(
                "version" to state.version,
                "at" to FieldValue.serverTimestamp(),
                "onDevice" to state.onDeviceAnalysis,
                "cloudAI" to state.cloudAiAnalysis,
                "photoSync" to state.cloudPhotoBackup,
                "analytics" to state.analytics
            )

            val payload = mapOf(
                "consent" to consentData,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firebaseModule.firestore.collection("users").document(uid)
                .set(payload, SetOptions.merge()).await()

            Outcome.Success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to save consents", e)
            Outcome.Error(e.localizedMessage ?: "Failed to save consents")
        }
    }
}
