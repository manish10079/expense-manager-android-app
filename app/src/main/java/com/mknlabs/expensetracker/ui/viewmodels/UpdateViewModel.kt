package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.domain.models.UpdateInfo
import com.mknlabs.expensetracker.domain.repository.UpdateRepository
import com.mknlabs.expensetracker.domain.update.UpdateChecker
import com.mknlabs.expensetracker.domain.update.UpdateDecision
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/** UI state for the in-app update dialog. */
sealed interface UpdateUiState {
    /** Fetching Remote Config; nothing shown yet. */
    data object Checking : UpdateUiState

    /** No newer version, or an optional update already declined this launch. */
    data object Hidden : UpdateUiState

    data class UpdateAvailable(
        val info: UpdateInfo,
        val force: Boolean
    ) : UpdateUiState
}

/**
 * Drives the in-app update notification dialog.
 *
 * Rules:
 * - Optional updates show **once per app launch** ("Later" or "Update Now"
 *   suppress them for the rest of the launch).
 * - Force updates are re-shown and cannot be dismissed.
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val updateChecker: UpdateChecker
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Checking)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    /** Guards the "once per launch" rule. The ViewModel is Activity-scoped, so it lives for one launch. */
    private var optionalShownThisLaunch = false

    init {
        updateRepository.updateInfo
            .onEach { info -> onUpdateInfo(info) }
            .launchIn(viewModelScope)
        updateRepository.fetchAndActivate()
    }

    private fun onUpdateInfo(info: UpdateInfo) {
        when (val decision = updateChecker.evaluate(
            currentVersionCode = BuildConfig.VERSION_CODE,
            currentVersionName = BuildConfig.VERSION_NAME,
            info = info
        )) {
            is UpdateDecision.UpdateAvailable -> {
                if (decision.force) {
                    // Force updates always show — the user cannot proceed without updating.
                    _uiState.value = UpdateUiState.UpdateAvailable(decision.info, force = true)
                } else if (!optionalShownThisLaunch) {
                    _uiState.value = UpdateUiState.UpdateAvailable(decision.info, force = false)
                }
            }
            UpdateDecision.NoUpdate -> {
                _uiState.value = UpdateUiState.Hidden
            }
        }
    }

    fun onLater() {
        val current = _uiState.value
        if (current is UpdateUiState.UpdateAvailable && current.force) return
        optionalShownThisLaunch = true
        _uiState.value = UpdateUiState.Hidden
    }

    fun onUpdateNow() {
        val current = _uiState.value
        optionalShownThisLaunch = true
        // Force updates stay visible: the user must return from the Play Store
        // with the update actually installed.
        if (current is UpdateUiState.UpdateAvailable && current.force) return
        _uiState.value = UpdateUiState.Hidden
    }
}