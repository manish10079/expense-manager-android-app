package com.mknlabs.expensetracker.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.models.UpdateConfigStatus
import com.mknlabs.expensetracker.domain.models.UpdateInfo
import com.mknlabs.expensetracker.domain.repository.UpdateRepository
import com.mknlabs.expensetracker.domain.update.PlayAppUpdateGateway
import com.mknlabs.expensetracker.domain.update.UpdateChecker
import com.mknlabs.expensetracker.domain.update.UpdateDecision
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the in-app update dialog. */
sealed interface UpdateUiState {
    /** Play availability has not answered yet. */
    data object Checking : UpdateUiState

    /** No Play update, or an optional update already declined this launch. */
    data object Hidden : UpdateUiState

    data class UpdateAvailable(
        val info: UpdateInfo,
        val force: Boolean
    ) : UpdateUiState
}

/**
 * Dialog is shown only when Play In-App Updates reports a store build.
 * Remote Config supplies force and copy.
 *
 * Optional updates show once per launch. Force updates cannot be dismissed.
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val updateChecker: UpdateChecker,
    private val playAppUpdateGateway: PlayAppUpdateGateway,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Checking)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var optionalShownThisLaunch = false
    private var playUpdateAvailable: Boolean? = null

    init {
        updateRepository.updateInfo
            .onEach { info -> onUpdateInfo(info) }
            .launchIn(viewModelScope)
        updateRepository.fetchAndActivate()
        refreshPlayAvailability()
    }

    fun refreshPlayAvailability() {
        viewModelScope.launch {
            playUpdateAvailable = playAppUpdateGateway.isUpdateAvailable()
            onUpdateInfo(updateRepository.updateInfo.value)
        }
    }

    private fun onUpdateInfo(info: UpdateInfo) {
        val play = playUpdateAvailable
        if (play == null) {
            _uiState.value = UpdateUiState.Checking
            return
        }
        if (!play) {
            _uiState.value = UpdateUiState.Hidden
            return
        }
        if (info.configStatus == UpdateConfigStatus.Unknown ||
            info.configStatus == UpdateConfigStatus.Loading
        ) {
            _uiState.value = UpdateUiState.Checking
            return
        }
        when (val decision = updateChecker.evaluate(play, info)) {
            is UpdateDecision.UpdateAvailable -> {
                if (decision.force) {
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
        if (current is UpdateUiState.UpdateAvailable && current.force) return
        _uiState.value = UpdateUiState.Hidden
    }
}
