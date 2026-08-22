package com.mknlabs.expensetracker.utils

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mknlabs.expensetracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    /**
     * Builds a [GetCredentialRequest] for the given [filterFlag] and [autoSelect].
     */
    private fun buildGoogleIdRequest(
        context: Context,
        filterByAuthorizedAccounts: Boolean,
        autoSelectEnabled: Boolean
    ): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(autoSelectEnabled)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    /**
     * Attempts a single credential retrieval. Returns the ID token on success,
     * null on user cancellation, or throws on failure.
     */
    private suspend fun tryGetCredential(
        context: Context,
        filterByAuthorizedAccounts: Boolean,
        autoSelectEnabled: Boolean
    ): String? {
        val request = buildGoogleIdRequest(context, filterByAuthorizedAccounts, autoSelectEnabled)
        val result = credentialManager.getCredential(context = context, request = request)
        Log.d("AUTH", "GoogleAuthHelper: Credential received (filterByAuthorized=$filterByAuthorizedAccounts)")
        val credential = result.credential
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        return googleIdTokenCredential.idToken
    }

    suspend fun getGoogleIdToken(
        context: Context,
        autoSelect: Boolean = false,
        filterByAuthorized: Boolean = false
    ): Result<String?> {
        // Two-step strategy to avoid the "Add Account" screen on first click:
        // 1. First try with filterByAuthorizedAccounts=true (fast path for returning users).
        // 2. If no authorized accounts exist yet, retry with false to show the full picker.
        val filterSteps = if (filterByAuthorized) {
            listOf(true)
        } else {
            listOf(true, false)
        }

        // --- First attempt: normal two-step strategy ---
        val firstAttempt = attemptCredentialRetrieval(context, filterSteps, autoSelect)
        if (firstAttempt != null) return firstAttempt

        // --- Retry: clear stale credential state, then retry all steps ---
        // Credential Manager can get into a stale state after clearCredentialState()
        // (e.g. on sign-out), causing NoCredentialException even when Google
        // accounts exist on the device. Clearing and retrying resets this.
        Log.d("AUTH", "GoogleAuthHelper: First attempt failed, clearing credential state and retrying")
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) { /* best-effort cleanup */ }

        // Small delay to let Play Services re-sync account state
        kotlinx.coroutines.delay(300)

        val retryResult = attemptCredentialRetrieval(context, filterSteps, autoSelect)
        if (retryResult != null) return retryResult

        // Both attempts exhausted — no credentials available
        Log.e("AUTH", "GoogleAuthHelper: No Google accounts found on device after retry")
        return Result.failure(NoCredentialException())
    }

    /**
     * Runs through the given [filterSteps] once, returning a Result on success/cancel/error,
     * or null if all steps threw [NoCredentialException] (caller should retry or fail).
     */
    private suspend fun attemptCredentialRetrieval(
        context: Context,
        filterSteps: List<Boolean>,
        autoSelect: Boolean
    ): Result<String?>? {
        for ((index, filterFlag) in filterSteps.withIndex()) {
            try {
                val idToken = tryGetCredential(
                    context = context,
                    filterByAuthorizedAccounts = filterFlag,
                    autoSelectEnabled = autoSelect && filterFlag
                )
                return Result.success(idToken)
            } catch (e: GetCredentialCancellationException) {
                Log.d("AUTH", "GoogleAuth: User cancelled the Google Sign-In request")
                return Result.success(null)
            } catch (e: NoCredentialException) {
                if (index < filterSteps.lastIndex) {
                    Log.d("AUTH", "GoogleAuth: No authorized accounts, retrying with all accounts")
                    continue
                }
                // All steps in this attempt threw NoCredentialException — let caller retry
                Log.d("AUTH", "GoogleAuth: NoCredentialException on this attempt")
                return null
            } catch (e: Exception) {
                Log.e("AUTH", "GoogleAuth: Google Sign-In failed: ${e.javaClass.simpleName}")
                return Result.failure(e)
            }
        }
        return null
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
