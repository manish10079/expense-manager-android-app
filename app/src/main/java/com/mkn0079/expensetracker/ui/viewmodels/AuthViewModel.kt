package com.mkn0079.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.domain.repository.AuthRepository
import com.mkn0079.expensetracker.utils.GoogleAuthHelper
import com.mkn0079.expensetracker.utils.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String, val isNetworkError: Boolean = false) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleAuthHelper: GoogleAuthHelper,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser = authRepository.currentUser

    fun signInWithGoogle() {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error("No internet connection", isNetworkError = true)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val idToken = googleAuthHelper.getGoogleIdToken()
            if (idToken != null) {
                authRepository.signInWithGoogle(idToken)
                    .onSuccess { _authState.value = AuthState.Success }
                    .onFailure { _authState.value = AuthState.Error(it.message ?: "Authentication failed") }
            } else {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error("No internet connection", isNetworkError = true)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Login failed") }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        if (!networkMonitor.isConnected()) {
            _authState.value = AuthState.Error("No internet connection", isNetworkError = true)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signUpWithEmail(email, password)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Signup failed") }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInAnonymously()
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure { _authState.value = AuthState.Error(it.message ?: "Guest login failed") }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signOut() {
        authRepository.signOut()
    }
}
