package com.mknlabs.expensetracker.ui.viewmodels

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.data.local.MonetizationDataStore
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.mknlabs.expensetracker.utils.GoogleAuthHelper
import com.mknlabs.expensetracker.utils.NetworkMonitor
import com.mknlabs.expensetracker.utils.ProfilePhotoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.net.UnknownHostException
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore

enum class AuthLoadingType {
    GOOGLE,
    EMAIL,
    MAGIC_LINK,
    GUEST,
    GENERIC
}

sealed class AuthState {
    object Idle : AuthState()
    data class Loading(val type: AuthLoadingType = AuthLoadingType.GENERIC) : AuthState()
    data class Success(val isNewUser: Boolean = false) : AuthState()
    object ResetEmailSent : AuthState()
    object MagicLinkSent : AuthState()
    object NoGoogleAccounts : AuthState()
    data class EmailVerificationRequired(
        val isLoading: Boolean = false,
        val errorRes: Int? = null,
        val isResendSuccess: Boolean = false
    ) : AuthState()
    data class Error(@StringRes val messageRes: Int) : AuthState()
}

sealed class UpdatePasswordState {
    object Idle : UpdatePasswordState()
    object Loading : UpdatePasswordState()
    object Success : UpdatePasswordState()
    data class Error(@StringRes val messageRes: Int) : UpdatePasswordState()
}

/**
 * Snapshot of a returning (existing) user's profile pulled from Firestore.
 * Used by [OnboardingScreen] to decide which setup steps to skip on a fresh
 * install when the user already has a cloud profile.
 */
data class ReturningUserProfile(
    val fullName: String,
    val gender: String,
    val financialGoal: String
) {
    val hasName: Boolean get() = fullName.isNotBlank() && fullName != "Guest User"
    val hasGender: Boolean get() = gender.isNotBlank()
    val hasGoal: Boolean get() = financialGoal.isNotBlank()
    val isComplete: Boolean get() = hasName && hasGender && hasGoal
}

/**
 * The onboarding step a returning user should land on after signing in.
 * Kept in the ViewModel layer so the routing decision is unit-testable and
 * free of any UI (page-index) concerns.
 */
enum class ReturningUserStep {
    /** Financial goal not set yet — show the goal page. */
    FINANCIAL_GOAL,

    /** Name/gender missing — show the setup (name/gender) page. */
    SETUP_PROFILE,

    /** Everything is already filled — show the "Welcome back" page. */
    WELCOME_BACK
}

/**
 * Maps a returning user's cloud profile to the onboarding step to show.
 */
fun resolveReturningUserStep(profile: ReturningUserProfile): ReturningUserStep = when {
    profile.isComplete -> ReturningUserStep.WELCOME_BACK
    !profile.hasGoal -> ReturningUserStep.FINANCIAL_GOAL
    !profile.hasName || !profile.hasGender -> ReturningUserStep.SETUP_PROFILE
    // Unreachable fallback (hasGoal && hasName && hasGender == isComplete) kept
    // for safety — never show the auth page again for a signed-in returning user.
    else -> ReturningUserStep.FINANCIAL_GOAL
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val googleAuthHelper: GoogleAuthHelper,
    private val networkMonitor: NetworkMonitor,
    private val syncRepository: com.mknlabs.expensetracker.domain.repository.SyncRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _updatePasswordState = MutableStateFlow<UpdatePasswordState>(UpdatePasswordState.Idle)
    val updatePasswordState: StateFlow<UpdatePasswordState> = _updatePasswordState.asStateFlow()

    private val _cooldownSeconds = MutableStateFlow(0)
    val cooldownSeconds: StateFlow<Int> = _cooldownSeconds.asStateFlow()

    private var cooldownJob: kotlinx.coroutines.Job? = null
    private var guestSignInSessionId: Long = 0L

    val currentUser = authRepository.currentUser

    private val _verificationExpiry = MutableStateFlow<Long?>(null)
    val verificationExpiry: StateFlow<Long?> = _verificationExpiry.asStateFlow()

    private val _returningUserProfile = MutableStateFlow<ReturningUserProfile?>(null)
    /** Non-null when we successfully fetched an existing user's Firestore profile during onboarding. */
    val returningUserProfile: StateFlow<ReturningUserProfile?> = _returningUserProfile.asStateFlow()

    /**
     * Fetches the user's existing profile from Firestore immediately after sign-in.
     * Used by [OnboardingScreen] to skip already-completed setup steps on a fresh install.
     *
     * On failure the profile is set to an empty one (never null) so onboarding
     * falls through to the normal goal/setup pages instead of getting stuck on
     * the auth screen.
     */
    fun fetchReturningUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val profile = syncRepository.fetchUserProfileFromCloud(uid)
                _returningUserProfile.value = ReturningUserProfile(
                    fullName = profile?.fullName.orEmpty(),
                    gender = profile?.gender.orEmpty(),
                    financialGoal = profile?.financialGoal.orEmpty()
                )
            } catch (e: Exception) {
                android.util.Log.e("AuthVM", "fetchReturningUserProfile failed", e)
                _returningUserProfile.value = ReturningUserProfile("", "", "")
            }
        }
    }

    fun resetReturningUserProfile() {
        _returningUserProfile.value = null
    }

    fun loadVerificationExpiry() {
        val uid = authRepository.currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                val expiry = snapshot.getLong("verificationExpiry")
                _verificationExpiry.value = expiry
            } catch (e: Exception) {
                android.util.Log.e("AuthVM", "Failed to fetch verification expiry from Firestore", e)
            }
        }
    }

    private suspend fun updateVerificationExpiryInFirestore(uid: String, expiryTime: Long) {
        try {
            val userDoc = FirebaseFirestore.getInstance().collection("users").document(uid)
            val data = mapOf("verificationExpiry" to expiryTime)
            userDoc.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
            _verificationExpiry.value = expiryTime
        } catch (e: Exception) {
            android.util.Log.e("AuthVM", "Failed to update verification expiry in Firestore", e)
        }
    }

    var shouldAttemptAutoSignInAfterReturn = false

    /**
     * Specifically used when returning from the "Add Account" system screen.
     * Retries a few times because system sync can be slow.
     */
    fun attemptAutoSignInAfterReturn(context: Context) {
        viewModelScope.launch {
            repeat(3) { attempt ->
                val delayTime = when(attempt) {
                    0 -> 1000L
                    1 -> 3000L
                    else -> 5000L
                }
                kotlinx.coroutines.delay(delayTime)

                // Stage 1 only (silent=true) — don't flash UI while syncing
                googleAuthHelper.getGoogleIdToken(context = context, silent = true)
                    .onSuccess { idToken ->
                        if (idToken != null) {
                            authRepository.signInWithGoogle(idToken)
                                .onSuccess { isNewUser ->
                                    _authState.value = AuthState.Success(isNewUser)
                                    return@launch // Success! Exit the loop.
                                }
                        }
                    }
                    .onFailure { error ->
                        // Only on the LAST attempt, if it still fails, we set the visible error state
                        if (attempt == 2 && error is NoCredentialException) {
                            _authState.value = AuthState.NoGoogleAccounts
                        }
                    }
            }
        }
    }

    fun signInWithGoogle(context: Context? = null, silent: Boolean = false) {
        android.util.Log.d("AUTH", "Starting Google sign-in (silent=$silent)")
        if (!networkMonitor.isConnected()) {
            if (!silent) {
                _authState.value = AuthState.Error(R.string.error_no_internet)
            }
            return
        }

        val targetContext = context ?: this.context

        viewModelScope.launch {
            if (!silent) {
                _authState.value = AuthState.Loading(AuthLoadingType.GOOGLE)
            }

            // Two-stage flow: Stage 1 (silent) → Stage 2 (full picker) if not silent
            googleAuthHelper.getGoogleIdToken(context = targetContext, silent = silent)
                .onSuccess { idToken ->
                    android.util.Log.d("AUTH", "Credential received, ID Token exists: ${idToken != null}")
                    if (idToken != null) {
                        authRepository.signInWithGoogle(idToken)
                            .onSuccess { isNewUser ->
                                android.util.Log.d("AUTH", "Firebase sign-in success, isNewUser=$isNewUser")
                                _authState.value = AuthState.Success(isNewUser)
                            }
                            .onFailure { error ->
                                android.util.Log.e("AUTH", "Firebase Google credential sign-in failed in AuthViewModel: class=[${error.javaClass.name}], message=[${error.message}]", error)
                                if (!silent) {
                                    _authState.value = AuthState.Error(mapFirebaseError(error))
                                }
                            }
                    } else {
                        android.util.Log.w("AUTH", "Google Sign-In cancelled or returned null token")
                        if (!silent) {
                            _authState.value = AuthState.Idle
                        }
                    }
                }
                .onFailure { error ->
                    android.util.Log.e("AUTH", "GoogleAuthHelper token retrieval failed in AuthViewModel: class=[${error.javaClass.name}], message=[${error.message}]", error)
                    if (!silent) {
                        _authState.value = when (error) {
                            is NoCredentialException -> AuthState.NoGoogleAccounts
                            is GoogleIdTokenParsingException -> AuthState.Error(R.string.error_auth_token_parse)
                            is UnknownHostException -> AuthState.Error(R.string.error_no_internet)
                            else -> AuthState.Error(mapGoogleAuthError(error))
                        }
                    }
                }
        }
    }

    /**
     * Attempts to sign in silently using authorized accounts and auto-selection.
     */
    fun trySilentSignIn(context: Context? = null) {
        if (authRepository.isUserLoggedIn()) {
            return
        }
        signInWithGoogle(context = context, silent = true)
    }

    fun signInWithEmail(email: String, password: String) {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error(R.string.error_no_internet)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading(AuthLoadingType.EMAIL)
            authRepository.signInWithEmail(email, password)
                .onSuccess { isNewUser -> 
                    val user = authRepository.currentUser.value
                    if (user != null && !user.isEmailVerified) {
                        val uid = user.uid
                        try {
                            val snapshot = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
                            var expiry = snapshot.getLong("verificationExpiry")
                            val now = System.currentTimeMillis()
                            
                            if (expiry == null || now > (expiry ?: 0L)) {
                                authRepository.sendEmailVerification()
                                expiry = now + 72L * 60 * 60 * 1000
                                updateVerificationExpiryInFirestore(uid, expiry)
                            } else {
                                _verificationExpiry.value = expiry
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("AuthVM", "Error checking verification status during sign in", e)
                            authRepository.sendEmailVerification()
                            val expiry = System.currentTimeMillis() + 72L * 60 * 60 * 1000
                            updateVerificationExpiryInFirestore(uid, expiry)
                        }
                        _authState.value = AuthState.EmailVerificationRequired()
                    } else {
                        _authState.value = AuthState.Success(isNewUser)
                    }
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(mapFirebaseError(error))
                }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error(R.string.error_no_internet)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading(AuthLoadingType.EMAIL)
            authRepository.signUpWithEmail(email, password)
                .onSuccess {
                    val uid = authRepository.currentUser.value?.uid
                    if (uid != null) {
                        val expiryTime = System.currentTimeMillis() + 72 * 60 * 60 * 1000
                        updateVerificationExpiryInFirestore(uid, expiryTime)
                    }
                    _authState.value = AuthState.EmailVerificationRequired()
                }
                .onFailure { error ->
                    val isCollision = error is com.google.firebase.auth.FirebaseAuthUserCollisionException || 
                                     (error.message?.contains("already in use") == true) ||
                                     (error.message?.contains("already exists") == true)
                    
                    if (isCollision) {
                        android.util.Log.i("AuthVM", "Email already in use, trying to sign in instead")
                        signInWithEmail(email, password)
                    } else {
                        _authState.value = AuthState.Error(mapFirebaseError(error))
                    }
                }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error(R.string.error_enter_email)
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error(R.string.error_invalid_email)
            return
        }
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error(R.string.error_no_internet)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading(AuthLoadingType.EMAIL)
            authRepository.sendPasswordResetEmail(email)
                .onSuccess { _authState.value = AuthState.ResetEmailSent }
                .onFailure { error ->
                    _authState.value = AuthState.Error(mapFirebaseError(error))
                }
        }
    }

    fun sendMagicLink(email: String) {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error(R.string.error_no_internet)
            return
        }

        if (_cooldownSeconds.value > 0) return

        viewModelScope.launch {
            _authState.value = AuthState.Loading(AuthLoadingType.MAGIC_LINK)
            
            // 1. Store email locally for verification when user clicks the link
            AppSettingsDataStore.updateAppSettings(context) { it.copy(pendingAuthEmail = email) }
            
            // 2. Send the link
            authRepository.sendMagicLink(email)
                .onSuccess { 
                    _authState.value = AuthState.MagicLinkSent
                    startCooldown()
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(mapFirebaseError(error))
                }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _cooldownSeconds.value = 60
            while (_cooldownSeconds.value > 0) {
                kotlinx.coroutines.delay(1000)
                _cooldownSeconds.value -= 1
            }
        }
    }

    fun completeMagicLinkSignIn(emailLink: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading(AuthLoadingType.MAGIC_LINK)
            
            // 1. Retrieve the email we stored earlier
            val pendingEmail = AppSettingsDataStore.getAppSettingsFlow(context).first().pendingAuthEmail
            
            if (pendingEmail != null) {
                authRepository.completeSignInWithLink(pendingEmail, emailLink)
                    .onSuccess { isNewUser ->
                        _authState.value = AuthState.Success(isNewUser)
                        // Clear the pending email
                        AppSettingsDataStore.updateAppSettings(context) { it.copy(pendingAuthEmail = null) }
                    }
                    .onFailure { error ->
                        _authState.value = AuthState.Error(mapFirebaseError(error))
                    }
            } else {
                _authState.value = AuthState.Error(R.string.error_auth_generic_fail)
            }
        }
    }

    fun startGuestSignIn() {
        val sessionId = System.currentTimeMillis()
        guestSignInSessionId = sessionId
        android.util.Log.d("AuthVM", "Starting guest sign-in session: $sessionId")

        viewModelScope.launch {
            _authState.value = AuthState.Loading(AuthLoadingType.GUEST)
            authRepository.signInAnonymously()
                .onSuccess { isNewUser ->
                    if (guestSignInSessionId == sessionId) {
                        _authState.value = AuthState.Success(isNewUser)
                    }
                }
                .onFailure {
                    if (guestSignInSessionId == sessionId) {
                        _authState.value = AuthState.Error(R.string.error_auth_generic_fail)
                    }
                }
        }
    }

    fun cancelGuestSignIn() {
        guestSignInSessionId = 0L
    }

    private fun mapFirebaseError(error: Throwable): Int {
        if (error is FirebaseAuthException) {
            val errorCode = error.errorCode
            android.util.Log.w("AuthVM", "Mapping Firebase Auth Exception: $errorCode")
            return when (errorCode) {
                "ERROR_USER_NOT_FOUND" -> R.string.error_auth_user_not_found
                "ERROR_WRONG_PASSWORD" -> R.string.error_auth_wrong_password
                "ERROR_INVALID_CREDENTIAL" -> R.string.error_auth_invalid_credentials
                "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_CREDENTIAL_ALREADY_IN_USE", "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> R.string.error_auth_email_already_in_use
                "ERROR_USER_DISABLED" -> R.string.error_auth_user_disabled
                "ERROR_TOO_MANY_REQUESTS" -> R.string.error_auth_too_many_requests
                "ERROR_WEAK_PASSWORD" -> R.string.error_auth_weak_password
                else -> R.string.error_auth_generic_fail
            }
        }

        val message = error.message ?: ""
        return when {
            message.contains("user-not-found") -> R.string.error_auth_user_not_found
            message.contains("wrong-password") -> R.string.error_auth_wrong_password
            message.contains("invalid-credential") -> R.string.error_auth_invalid_credentials
            message.contains("email-already-in-use") || message.contains("already exists") || message.contains("already-in-use") -> R.string.error_auth_email_already_in_use
            message.contains("user-disabled") -> R.string.error_auth_user_disabled
            message.contains("too-many-requests") -> R.string.error_auth_too_many_requests
            message.contains("weak-password") -> R.string.error_auth_weak_password
            else -> R.string.error_auth_generic_fail
        }
    }

    private fun mapGoogleAuthError(error: Throwable): Int = when {
        error is GoogleIdTokenParsingException -> R.string.error_auth_token_parse
        error is UnknownHostException -> R.string.error_no_internet
        error is NoCredentialException -> R.string.error_auth_no_google_accounts
        else -> {
            val message = error.message ?: ""
            when {
                message.contains("DEVELOPER_ERROR") ||
                message.contains("10:") ||
                message.contains("12500") -> R.string.error_auth_cloned_or_unauthorized
                else -> R.string.error_auth_generic_fail
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signOut() {
        viewModelScope.launch {
            // 1. Delete profile photo if any
            try {
                val currentProfile = UserProfileDataStore.getUserProfileFlow(context).first()
                ProfilePhotoManager.deleteManagedPhoto(currentProfile.photoUri)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Failed to delete managed profile photo: ${e.message}", e)
            }

            // 2. Clear local profile info (sets to defaultUserProfile)
            try {
                UserProfileDataStore.clearAll(context)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Failed to clear UserProfileDataStore: ${e.message}", e)
            }

            // 3. Reset Tier and Ad Access
            try {
                AppSettingsDataStore.updateUserTier(context, com.mknlabs.expensetracker.models.UserTier.FREE)
                MonetizationDataStore.updateGlobalAdAccessExpiry(context, 0L)
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Failed to reset Tier/Ad Access: ${e.message}", e)
            }

            // 4. Reset App Settings specific fields: lastSyncTimeMillis = 0L, isCloudSyncEnabled = false
            try {
                AppSettingsDataStore.updateAppSettings(context) { settings ->
                    settings.copy(
                        lastSyncTimeMillis = 0L,
                        isCloudSyncEnabled = false,
                        pendingAuthEmail = null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Failed to reset app settings: ${e.message}", e)
            }

            // 6. Sign out from Firebase Auth
            authRepository.signOut()

            // 7. Sign out from Google Auth Helper
            try {
                googleAuthHelper.signOut()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Google Auth sign out failed: ${e.message}", e)
            }
        }
    }

    /**
     * Signs out from Firebase and Google only, without clearing the local
     * UserProfileDataStore. Used when an unverified signup is cancelled so
     * the user's existing profile (name, etc.) is preserved.
     */
    fun signOutFirebaseOnly() {
        viewModelScope.launch {
            authRepository.signOut()
            try {
                googleAuthHelper.signOut()
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Firebase-only sign out failed: ${e.message}", e)
            }
        }
    }

    fun resendVerificationEmail() {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.EmailVerificationRequired(errorRes = R.string.error_no_internet)
            return
        }
        viewModelScope.launch {
            val expiry = _verificationExpiry.value
            val now = System.currentTimeMillis()
            if (expiry != null && now < expiry) {
                _authState.value = AuthState.EmailVerificationRequired(
                    errorRes = R.string.error_link_still_valid
                )
                return@launch
            }

            _authState.value = AuthState.EmailVerificationRequired(isLoading = true)
            authRepository.sendEmailVerification()
                .onSuccess {
                    val uid = authRepository.currentUser.value?.uid
                    if (uid != null) {
                        val newExpiry = System.currentTimeMillis() + 72 * 60 * 60 * 1000
                        updateVerificationExpiryInFirestore(uid, newExpiry)
                    }
                    _authState.value = AuthState.EmailVerificationRequired(isResendSuccess = true)
                }
                .onFailure { error ->
                    _authState.value = AuthState.EmailVerificationRequired(errorRes = mapFirebaseError(error))
                }
        }
    }

    fun checkEmailVerificationStatus() {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.EmailVerificationRequired(errorRes = R.string.error_no_internet)
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.EmailVerificationRequired(isLoading = true)
            authRepository.reloadUser()
                .onSuccess {
                    val user = authRepository.currentUser.value
                    if (user != null && user.isEmailVerified) {
                        _authState.value = AuthState.Success(isNewUser = false)
                    } else {
                        val expiry = _verificationExpiry.value
                        val now = System.currentTimeMillis()
                        if (expiry == null || now > (expiry ?: 0L)) {
                            authRepository.sendEmailVerification()
                            val newExpiry = now + 72L * 60 * 60 * 1000
                            val uid = user?.uid
                            if (uid != null) {
                                updateVerificationExpiryInFirestore(uid, newExpiry)
                            }
                            _authState.value = AuthState.EmailVerificationRequired(
                                errorRes = R.string.error_email_not_verified_expired_sent
                            )
                        } else {
                            _authState.value = AuthState.EmailVerificationRequired(errorRes = R.string.error_email_not_verified)
                        }
                    }
                }
                .onFailure { error ->
                    _authState.value = AuthState.EmailVerificationRequired(errorRes = mapFirebaseError(error))
                }
        }
    }

    fun updatePassword(currentPassword: String, newPassword: String) {
        if (!networkMonitor.isConnected()) {
            _updatePasswordState.value = UpdatePasswordState.Error(R.string.error_no_internet)
            return
        }

        viewModelScope.launch {
            _updatePasswordState.value = UpdatePasswordState.Loading
            authRepository.updatePassword(currentPassword, newPassword)
                .onSuccess {
                    _updatePasswordState.value = UpdatePasswordState.Success
                }
                .onFailure { error ->
                    val errorRes = mapFirebaseError(error)
                    val finalErrorRes = if (errorRes == R.string.error_auth_generic_fail) {
                        R.string.error_password_update_failed
                    } else {
                        errorRes
                    }
                    _updatePasswordState.value = UpdatePasswordState.Error(finalErrorRes)
                }
        }
    }

    fun resetUpdatePasswordState() {
        _updatePasswordState.value = UpdatePasswordState.Idle
    }
}
