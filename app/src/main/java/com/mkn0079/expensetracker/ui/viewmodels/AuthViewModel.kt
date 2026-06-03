package com.mkn0079.expensetracker.ui.viewmodels

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.domain.repository.AuthRepository
import com.mkn0079.expensetracker.utils.GoogleAuthHelper
import com.mkn0079.expensetracker.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val isNewUser: Boolean = false) : AuthState()
    object ResetEmailSent : AuthState()
    object MagicLinkSent : AuthState()
    data class Error(@StringRes val messageRes: Int) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val googleAuthHelper: GoogleAuthHelper,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _cooldownSeconds = MutableStateFlow(0)
    val cooldownSeconds: StateFlow<Int> = _cooldownSeconds.asStateFlow()

    private var cooldownJob: kotlinx.coroutines.Job? = null
    private var guestSignInSessionId: Long = 0L

    val currentUser = authRepository.currentUser

    fun signInWithGoogle() {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error(R.string.error_no_internet)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            googleAuthHelper.getGoogleIdToken()
                .onSuccess { idToken ->
                    if (idToken != null) {
                        authRepository.signInWithGoogle(idToken)
                            .onSuccess { isNewUser -> _authState.value = AuthState.Success(isNewUser) }
                            .onFailure { error ->
                                _authState.value = AuthState.Error(mapFirebaseError(error.message))
                            }
                    } else {
                        _authState.value = AuthState.Idle
                    }
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(R.string.error_auth_generic_fail)
                }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error(R.string.error_no_internet)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess { isNewUser -> _authState.value = AuthState.Success(isNewUser) }
                .onFailure { error ->
                    _authState.value = AuthState.Error(mapFirebaseError(error.message))
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
                .onSuccess { isNewUser -> _authState.value = AuthState.Success(isNewUser) }
                .onFailure { error ->
                    _authState.value = AuthState.Error(mapFirebaseError(error.message))
                }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error(R.string.error_no_internet)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.sendPasswordResetEmail(email)
                .onSuccess { _authState.value = AuthState.ResetEmailSent }
                .onFailure { error ->
                    _authState.value = AuthState.Error(mapFirebaseError(error.message))
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
                    _authState.value = AuthState.Error(mapFirebaseError(error.message))
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
                        _authState.value = AuthState.Error(mapFirebaseError(error.message))
                    }
            } else {
                _authState.value = AuthState.Error(R.string.error_auth_generic_fail)
            }
        }
    }

    fun startGuestSignIn() {
        val sessionId = System.currentTimeMillis()
        guestSignInSessionId = sessionId

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

    private fun mapFirebaseError(message: String?): Int {
        val error = message ?: ""
        return when {
            error.contains("user-not-found") -> R.string.error_auth_user_not_found
            error.contains("wrong-password") -> R.string.error_auth_wrong_password
            error.contains("invalid-credential") -> R.string.error_auth_invalid_credentials
            error.contains("email-already-in-use") -> R.string.error_auth_email_already_in_use
            error.contains("user-disabled") -> R.string.error_auth_user_disabled
            error.contains("too-many-requests") -> R.string.error_auth_too_many_requests
            error.contains("weak-password") -> R.string.error_auth_weak_password
            else -> R.string.error_auth_generic_fail
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signOut() {
        authRepository.signOut()
    }
}
