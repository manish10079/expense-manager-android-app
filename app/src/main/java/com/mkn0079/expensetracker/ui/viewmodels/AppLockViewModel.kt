package com.mkn0079.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppLockState {
    object Loading : AppLockState()
    object Locked : AppLockState()
    object Unlocked : AppLockState()
}

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AppLockState>(AppLockState.Loading)
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    init {
        // Immediate pessimistic check: if lock is enabled, start as Locked
        if (securityRepository.isLockEnabled() && securityRepository.hasPin()) {
            _state.value = AppLockState.Locked
        }
        checkLockState()
        observeForegroundEvents()
    }

    private fun observeForegroundEvents() {
        viewModelScope.launch {
            securityRepository.appForegroundEvents.collectLatest {
                checkLockState()
            }
        }
    }

    fun checkLockState() {
        viewModelScope.launch {
            val isEnabled = securityRepository.isLockEnabled()
            val hasPin = securityRepository.hasPin()
            val shouldLock = securityRepository.shouldRequireUnlock()

            _state.value = if (isEnabled && hasPin && shouldLock) {
                AppLockState.Locked
            } else {
                AppLockState.Unlocked
            }
        }
    }

    fun unlock() {
        securityRepository.markUnlocked()
        _state.value = AppLockState.Unlocked
    }

    fun forceLock() {
        if (securityRepository.isLockEnabled() && securityRepository.hasPin()) {
            _state.value = AppLockState.Locked
        }
    }
}
