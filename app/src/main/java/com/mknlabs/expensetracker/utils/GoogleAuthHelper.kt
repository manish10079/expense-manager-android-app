package com.mknlabs.expensetracker.utils

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.mknlabs.expensetracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Two-stage Google Sign-In helper using Credential Manager per official Google docs.
 *
 * **Stage 1 — Silent / Auto sign-in** (returning users):
 * [GetGoogleIdOption] with `filterByAuthorizedAccounts=true` and `autoSelectEnabled=true`.
 * Shows no UI when an authorized account already exists.
 *
 * **Stage 2 — Bottom sheet with all accounts** (fallback on [NoCredentialException]):
 * [GetGoogleIdOption] with `filterByAuthorizedAccounts=false`.
 * Shows the Credential Manager bottom sheet with ALL Google accounts on the device.
 */
@Singleton
class GoogleAuthHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)
    private val webClientId = context.getString(R.string.default_web_client_id)

    // ── Stage 1: Silent / auto sign-in ────────────────────────────────────

    /**
     * Builds a [GetCredentialRequest] for Stage 1 — silent automatic sign-in.
     * Only authorized accounts are considered; auto-selects if exactly one exists.
     * Shows no UI when an authorized account already exists.
     */
    private fun buildSilentRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    // ── Stage 2: Bottom sheet with all accounts ──────────────────────────

    /**
     * Builds a [GetCredentialRequest] for Stage 2 — bottom sheet showing
     * ALL Google accounts on the device. Uses [GetGoogleIdOption] with
     * `filterByAuthorizedAccounts=false` per Google's recommended flow.
     */
    private fun buildAllAccountsRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Two-stage Google sign-in per official Google Credential Manager docs.
     *
     * @param context       Activity or application context.
     * @param silent        If `true`, only Stage 1 runs — never shows UI.
     *                      If `false`, Stage 1 runs first (silent), then
     *                      Stage 2 (bottom sheet) on failure.
     * @return `Result.success(idToken)` on success, `Result.success(null)` on
     *         cancellation, or `Result.failure(exception)` on error.
     */
    suspend fun getGoogleIdToken(
        context: Context,
        silent: Boolean = false
    ): Result<String?> {
        if (silent) {
            // ── Silent mode: Stage 1 only (auto-select authorized account, no UI) ──
            Log.d("AUTH", "GoogleAuthHelper: Silent mode — Stage 1 auto sign-in")
            val stage1 = tryStage(context, buildSilentRequest())
            return stage1 ?: Result.failure(NoCredentialException())
        }

        // ── Explicit sign-in: Stage 1 → Stage 2 (bottom sheet) ──
        Log.d("AUTH", "GoogleAuthHelper: Explicit sign-in — Stage 1 (silent)")
        val stage1 = tryStage(context, buildSilentRequest())
        if (stage1 != null) return stage1

        // Stage 1 failed (NoCredentialException) → show bottom sheet with all accounts
        Log.d("AUTH", "GoogleAuthHelper: Stage 1 failed — launching Stage 2 (bottom sheet with all accounts)")
        val stage2 = tryStage(context, buildAllAccountsRequest())
        return stage2 ?: Result.failure(NoCredentialException())
    }

    /**
     * Attempts a single credential retrieval with the given [request].
     * Returns `Result<String?>` on success/cancel/error, or `null` on
     * [NoCredentialException] so the caller can try the next stage.
     */
    private suspend fun tryStage(
        context: Context,
        request: GetCredentialRequest
    ): Result<String?>? {
        return try {
            val result = credentialManager.getCredential(context = context, request = request)
            Log.d("AUTH", "GoogleAuthHelper: Credential received successfully")
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Result.success(credential.idToken)
        } catch (e: GetCredentialCancellationException) {
            Log.w("AUTH", "GoogleAuthHelper: User cancelled the Google sign-in prompt: ${e.message}")
            Result.success(null)
        } catch (e: NoCredentialException) {
            Log.w("AUTH", "GoogleAuthHelper: No credential / account found on device: ${e.message}")
            null
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("AUTH", "GoogleAuthHelper: Malformed Google ID token exception: ${e.message}", e)
            Result.failure(e)
        } catch (e: UnknownHostException) {
            Log.e("AUTH", "GoogleAuthHelper: Network unreachable while attempting Google sign-in: ${e.message}", e)
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e("AUTH", "GoogleAuthHelper: GetCredentialException [type=${e.type}, errorMessage=${e.errorMessage}]", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AUTH", "GoogleAuthHelper: Unexpected Google Sign-In failure: [class=${e.javaClass.name}, message=${e.message}]", e)
            Result.failure(e)
        }
    }

    private suspend fun clearCredentialStateSafely() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) { /* best-effort cleanup */ }
    }

    suspend fun signOut() {
        clearCredentialStateSafely()
    }
}
