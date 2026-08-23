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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.mknlabs.expensetracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Two-stage Google Sign-In helper using Credential Manager (2026 best practices).
 *
 * **Stage 1 — Silent / Auto sign-in** (returning users):
 * [GetGoogleIdOption] with `filterByAuthorizedAccounts=true` and `autoSelectEnabled=true`.
 * Shows no UI when an authorized account already exists.
 *
 * **Stage 2 — Full account picker** (fallback on [NoCredentialException]):
 * [GetSignInWithGoogleOption] — the official Google Sign-In button flow.
 * Shows ALL Google accounts on the device, fixing the "No accounts detected"
 * issue that occurs when the Play Store re-signs the APK.
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

    // ── Stage 2: Full account picker ──────────────────────────────────────

    /**
     * Builds a [GetCredentialRequest] for Stage 2 — official Google Sign-In
     * button flow. Shows ALL Google accounts available on the device.
     */
    private fun buildFullPickerRequest(): GetCredentialRequest {
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Two-stage Google sign-in.
     *
     * @param context       Activity or application context.
     * @param silent        If `true`, only Stage 1 runs — never shows UI.
     *                      If `false`, Stage 2 (full picker) is launched on failure.
     * @return `Result.success(idToken)` on success, `Result.success(null)` on
     *         cancellation, or `Result.failure(exception)` on error.
     */
    suspend fun getGoogleIdToken(
        context: Context,
        silent: Boolean = false
    ): Result<String?> {
        // ── Stage 1: Try silent sign-in with authorized accounts ──────
        Log.d("AUTH", "GoogleAuthHelper: Stage 1 — silent sign-in (filterByAuthorized=true)")
        val stage1 = tryStage(context, buildSilentRequest())
        if (stage1 != null) return stage1

        // ── Stage 2: Full account picker (only for explicit sign-in) ──
        if (silent) {
            Log.d("AUTH", "GoogleAuthHelper: Silent mode — skipping Stage 2")
            return Result.failure(NoCredentialException())
        }

        Log.d("AUTH", "GoogleAuthHelper: Stage 2 — full account picker via GetSignInWithGoogleOption")
        clearCredentialStateSafely()

        val stage2 = tryStage(context, buildFullPickerRequest())
        if (stage2 != null) return stage2

        // Both stages exhausted
        Log.e("AUTH", "GoogleAuthHelper: No Google accounts found after both stages")
        return Result.failure(NoCredentialException())
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
            Log.d("AUTH", "GoogleAuthHelper: User cancelled the sign-in request")
            Result.success(null)
        } catch (e: NoCredentialException) {
            Log.d("AUTH", "GoogleAuthHelper: NoCredentialException — need next stage")
            null
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("AUTH", "GoogleAuthHelper: GoogleIdTokenParsingException — malformed token")
            Result.failure(e)
        } catch (e: UnknownHostException) {
            Log.e("AUTH", "GoogleAuthHelper: Network error — cannot reach Google servers")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AUTH", "GoogleAuthHelper: Unexpected error: ${e.javaClass.simpleName}")
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
