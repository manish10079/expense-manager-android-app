package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.SecurityRepository
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

    private val _recoveryPerformed = MutableStateFlow(false)
    val recoveryPerformed: StateFlow<Boolean> = _recoveryPerformed.asStateFlow()

    init {
        // Immediate pessimistic check: if lock is enabled, start as Locked
        if (securityRepository.isLockEnabled() && securityRepository.hasPin()) {
            _state.value = AppLockState.Locked
        }
        checkLockState()
        observeForegroundEvents()
        observeBackgroundEvents()
    }

    private fun observeForegroundEvents() {
        viewModelScope.launch {
            securityRepository.appForegroundEvents.collectLatest {
                checkLockState(consumeSuppression = true)
            }
        }
    }

    private fun observeBackgroundEvents() {
        viewModelScope.launch {
            securityRepository.appBackgroundEvents.collectLatest {
                // Suppression is deliberately NOT consumed on the background check:
                // consuming it there would let a "external activity -> launcher detour"
                // (returning much later) bypass the lock entirely. It is only consumed
                // on the foreground (return) check, where the TTL guards staleness.
                checkLockState(consumeSuppression = false)
            }
        }
    }

    fun checkLockState(consumeSuppression: Boolean = false) {
        viewModelScope.launch {
            // A pending external-activity suppression is consumed here on the
            // foreground (return) check. No baseline reset is needed: the next genuine
            // background calls markBackgrounded(), which overwrites the timestamp, so
            // the time spent in the picker/browser never counts toward the auto-lock
            // timeout. (Resetting here would instead create a backgroundedAt <
            // unlockedAt state that trips shouldRequireUnlock's fail-secure crash
            // heuristic on later checks.)
            val lockSuppressed = consumeSuppression && securityRepository.consumeLockSuppression()
            val isEnabled = securityRepository.isLockEnabled()
            val hasPin = securityRepository.hasPin()
            val shouldLock = securityRepository.shouldRequireUnlock()

            _state.value = if (isEnabled && hasPin && shouldLock && !lockSuppressed) {
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

    fun disableLock() {
        viewModelScope.launch {
            securityRepository.disableLock()
            _recoveryPerformed.value = true
            _state.value = AppLockState.Unlocked
        }
    }

    fun consumeRecovery() {
        _recoveryPerformed.value = false
    }
}
