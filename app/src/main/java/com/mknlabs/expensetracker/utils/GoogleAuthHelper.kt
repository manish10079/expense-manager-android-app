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

    suspend fun getGoogleIdToken(
        context: Context,
        autoSelect: Boolean = false,
        filterByAuthorized: Boolean = false
    ): Result<String?> {
        val credentialManager = CredentialManager.create(context)

        // Two-step strategy to avoid the "Add Account" screen on first click:
        // 1. First try with filterByAuthorizedAccounts=true (fast path for returning users).
        // 2. If no authorized accounts exist yet, retry with false to show the full picker.
        val filterSteps = if (filterByAuthorized) {
            // Caller explicitly requested authorized-only; honour that.
            listOf(true)
        } else {
            // Try authorized first, then fall back to all accounts.
            listOf(true, false)
        }

        for ((index, filterFlag) in filterSteps.withIndex()) {
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterFlag)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(autoSelect && filterFlag)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                Log.d("AUTH", "GoogleAuthHelper: Credential received (filterByAuthorized=$filterFlag)")
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                return Result.success(googleIdTokenCredential.idToken)

            } catch (e: GetCredentialCancellationException) {
                Log.d("GoogleAuth", "User cancelled the Google Sign-In request")
                return Result.success(null)
            } catch (e: NoCredentialException) {
                if (index < filterSteps.lastIndex) {
                    // No authorized accounts yet — retry with the full picker.
                    Log.d("GoogleAuth", "No authorized accounts, retrying with all accounts")
                    continue
                }
                Log.e("GoogleAuth", "No Google accounts found on device")
                return Result.failure(e)
            } catch (e: Exception) {
                // Log only the class name — the raw message may embed the user's email
                Log.e("GoogleAuth", "Google Sign-In failed: ${e.javaClass.simpleName}")
                return Result.failure(e)
            }
        }

        // Should be unreachable, but satisfy the compiler.
        return Result.failure(NoCredentialException())
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
