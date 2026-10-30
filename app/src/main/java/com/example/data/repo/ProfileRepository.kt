package com.example.data.repo

import com.example.core.AppDispatchers
import com.example.core.Logger
import com.example.core.Outcome
import com.example.data.local.datastore.PreferencesDataStore
import com.example.data.remote.FirebaseModule
import com.example.domain.model.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val firebaseModule: FirebaseModule,
    private val preferencesDataStore: PreferencesDataStore,
    private val dispatchers: AppDispatchers
) {
    suspend fun getProfile(uid: String): Outcome<UserProfile> = withContext(dispatchers.io) {
        try {
            val doc = firebaseModule.firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                return@withContext Outcome.Success(UserProfile(uid = uid))
            }
            @Suppress("UNCHECKED_CAST")
            val profileMap = doc.get("profile") as? Map<String, Any?>
            val displayName = profileMap?.get("displayName") as? String ?: ""
            val birthYear = (profileMap?.get("birthYear") as? Number)?.toInt()
            val skinType = profileMap?.get("skinType") as? String ?: "not_sure"
            @Suppress("UNCHECKED_CAST")
            val concerns = (profileMap?.get("concerns") as? List<String>) ?: emptyList()
            val heightCm = (profileMap?.get("heightCm") as? Number)?.toFloat()
            val referenceRangePref = profileMap?.get("referenceRangePref") as? String ?: "prefer_not_to_say"

            Outcome.Success(
                UserProfile(
                    uid = uid,
                    displayName = displayName,
                    birthYear = birthYear,
                    skinType = skinType,
                    concerns = concerns,
                    heightCm = heightCm,
                    referenceRangePref = referenceRangePref
                )
            )
        } catch (e: Exception) {
            Logger.e("Failed to get profile from Firestore", e)
            Outcome.Error(e.localizedMessage ?: "Failed to get profile")
        }
    }

    suspend fun saveProfile(profile: UserProfile): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            val profileData = mutableMapOf<String, Any?>(
                "displayName" to profile.displayName,
                "skinType" to profile.skinType,
                "concerns" to profile.concerns,
                "referenceRangePref" to profile.referenceRangePref
            )
            profile.birthYear?.let { profileData["birthYear"] = it }
            profile.heightCm?.let { profileData["heightCm"] = it }

            val updatePayload = mapOf(
                "profile" to profileData,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firebaseModule.firestore.collection("users").document(profile.uid)
                .set(updatePayload, SetOptions.merge()).await()
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed to save profile", e)
            Outcome.Error(e.localizedMessage ?: "Failed to save profile")
        }
    }
}
