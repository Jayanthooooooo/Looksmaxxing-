package com.example.data.repo

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.core.AppDispatchers
import com.example.core.Logger
import com.example.core.Outcome
import com.example.data.local.datastore.PreferencesDataStore
import com.example.domain.model.SessionState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val preferencesDataStore: PreferencesDataStore,
    private val dispatchers: AppDispatchers
) {
    private val credentialManager = CredentialManager.create(context)

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun observeSessionState(): Flow<SessionState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.combine(preferencesDataStore.onboardingCompletedFlow) { user, onboardingDone ->
        if (user == null) {
            SessionState.SignedOut
        } else if (!onboardingDone) {
            SessionState.NeedsOnboarding(step = 1)
        } else {
            SessionState.Ready(user.uid)
        }
    }.flowOn(dispatchers.io)

    suspend fun signInWithGoogleInteractive(activityContext: Context): Outcome<String> = withContext(dispatchers.io) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return@withContext Outcome.Error("Google Sign-In configuration missing: default_web_client_id not found")
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        try {
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val uid = authResult.user?.uid ?: return@withContext Outcome.Error("Sign in failed: missing UID")
                Outcome.Success(uid)
            } else {
                Outcome.Error("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Logger.w("Google Sign-In flow cancelled: ${e.message}")
            Outcome.Error("Sign-in was cancelled")
        } catch (e: Exception) {
            Logger.e("Google Sign-In failed", e)
            Outcome.Error(e.localizedMessage ?: "Google Sign-In failed")
        }
    }

    suspend fun attemptAutoSignIn(): Outcome<String?> = withContext(dispatchers.io) {
        if (auth.currentUser != null) {
            return@withContext Outcome.Success(auth.currentUser?.uid)
        }
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return@withContext Outcome.Success(null)
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                Outcome.Success(authResult.user?.uid)
            } else {
                Outcome.Success(null)
            }
        } catch (e: Exception) {
            Outcome.Success(null)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Outcome<String> = withContext(dispatchers.io) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return@withContext Outcome.Error("Account creation failed")
            try {
                user.sendEmailVerification().await()
            } catch (e: Exception) {
                Logger.w("Failed to send email verification: ${e.message}")
            }
            Outcome.Success(user.uid)
        } catch (e: FirebaseAuthWeakPasswordException) {
            Outcome.Error("Password is too weak. Please use at least 8 characters.")
        } catch (e: FirebaseAuthUserCollisionException) {
            Outcome.Error("An account with this email already exists.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Outcome.Error("The email address is improperly formatted.")
        } catch (e: Exception) {
            Logger.e("Sign up failed", e)
            Outcome.Error(e.localizedMessage ?: "Sign up failed")
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Outcome<String> = withContext(dispatchers.io) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return@withContext Outcome.Error("Sign in failed")
            Outcome.Success(user.uid)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Outcome.Error("Incorrect email or password.")
        } catch (e: Exception) {
            Logger.e("Sign in failed", e)
            Outcome.Error(e.localizedMessage ?: "Sign in failed")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            auth.sendPasswordResetEmail(email).await()
            // Always return neutral confirmation to prevent account enumeration
            Outcome.Success(Unit)
        } catch (e: Exception) {
            // Still return success to prevent account enumeration
            Logger.w("Password reset completed (neutral response)")
            Outcome.Success(Unit)
        }
    }

    suspend fun reloadUser(): Outcome<Boolean> = withContext(dispatchers.io) {
        try {
            val user = auth.currentUser ?: return@withContext Outcome.Success(false)
            user.reload().await()
            Outcome.Success(user.isEmailVerified)
        } catch (e: Exception) {
            Outcome.Error(e.localizedMessage ?: "Could not reload user")
        }
    }

    suspend fun signOut(): Outcome<Unit> = withContext(dispatchers.io) {
        try {
            auth.signOut()
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Logger.w("Failed to clear credential manager state: ${e.message}")
            }
            preferencesDataStore.clearSessionKeys()
            Outcome.Success(Unit)
        } catch (e: Exception) {
            Outcome.Error(e.localizedMessage ?: "Sign out failed")
        }
    }
}
