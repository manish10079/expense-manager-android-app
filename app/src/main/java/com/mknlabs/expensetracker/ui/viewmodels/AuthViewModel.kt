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
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
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
                
                // We use autoSelect=true and silent=true for the retry attempts
                // so we don't flash the "No Accounts" error state if it's still syncing.
                googleAuthHelper.getGoogleIdToken(context = context, autoSelect = true, filterByAuthorized = false)
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

    fun signInWithGoogle(context: Context? = null, autoSelect: Boolean = false, silent: Boolean = false) {
        android.util.Log.d("AUTH", "Starting Google sign in, silent = $silent, autoSelect = $autoSelect")
        if (!networkMonitor.isConnected()) {
            if (!silent) {
                _authState.value = AuthState.Error(R.string.error_no_internet)
            }
            return
        }

        val targetContext = context ?: this.context

        viewModelScope.launch {
            if (!silent) {
                _authState.value = AuthState.Loading
            }
            
            googleAuthHelper.getGoogleIdToken(context = targetContext, autoSelect = autoSelect, filterByAuthorized = silent)
                .onSuccess { idToken ->
                    android.util.Log.d("AUTH", "Credential received, ID Token exists: ${idToken != null}")
                    if (idToken != null) {
                        android.util.Log.d("AUTH", "Google ID Token = $idToken")
                        authRepository.signInWithGoogle(idToken)
                            .onSuccess { isNewUser -> 
                                android.util.Log.d("AUTH", "Firebase sign in success, isNewUser = $isNewUser")
                                _authState.value = AuthState.Success(isNewUser) 
                            }
                            .onFailure { error ->
                                android.util.Log.e("AUTH", "Firebase sign in failed", error)
                                if (!silent) {
                                    _authState.value = AuthState.Error(mapFirebaseError(error))
                                }
                            }
                    } else {
                        android.util.Log.d("AUTH", "Sign in cancelled or idToken is null")
                        if (!silent) {
                            _authState.value = AuthState.Idle
                        }
                    }
                }
                .onFailure { error ->
                    android.util.Log.e("AUTH", "Google Auth Helper failed", error)
                    if (!silent) {
                        if (error is NoCredentialException) {
                            android.util.Log.d("AUTH", "No Google accounts found on device")
                            _authState.value = AuthState.NoGoogleAccounts
                        } else {
                            _authState.value = AuthState.Error(mapGoogleAuthError(error))
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
        // Redirect to the main signInWithGoogle logic with silent flag set to true
        signInWithGoogle(context = context, autoSelect = true, silent = true)
    }

    fun signInWithEmail(email: String, password: String) {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error(R.string.error_no_internet)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
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
            _authState.value = AuthState.Loading
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
            _authState.value = AuthState.Loading
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
            _authState.value = AuthState.Loading
            
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
            _authState.value = AuthState.Loading
            
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
            _authState.value = AuthState.Loading
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
            android.util.Log.w("AuthVM", "Mapping Firebase Auth Exception: $errorCode - ${error.message}")
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

    private fun mapGoogleAuthError(error: Throwable): Int {
        val message = error.message ?: ""
        return when {
            message.contains("DEVELOPER_ERROR") || 
            message.contains("10:") || 
            message.contains("12500") -> {
                R.string.error_auth_cloned_or_unauthorized
            }
            else -> R.string.error_auth_generic_fail
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
