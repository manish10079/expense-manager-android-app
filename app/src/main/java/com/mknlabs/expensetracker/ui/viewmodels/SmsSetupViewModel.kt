package com.mknlabs.expensetracker.ui.viewmodels

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.utils.DeviceVendorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Drives the Smart SMS Import setup cards on Home (GEMINI.md: Route-owned ViewModel,
 * pure UI state, no business logic in composables):
 *  - [SmsSetupUiState.showSmsPermissionCard] — RECEIVE_SMS denied after the one-time
 *    prompt (which never re-asks by design) → deep link to App Settings instead of a
 *    silent forever-off state.
 *  - [SmsSetupUiState.showMiuiSetupCard] — Xiaomi device with MIUI Autostart disabled
 *    (or unverifiable) → guidance + deep links to Autostart/battery settings.
 *
 * The MIUI Security Center provider call is a binder IPC, so it runs on
 * [Dispatchers.IO]; the decision functions themselves are pure and unit-tested in
 * `DeviceVendorUtilsTest`.
 */
@HiltViewModel
class SmsSetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsSetupUiState())
    val uiState: StateFlow<SmsSetupUiState> = _uiState.asStateFlow()

    /**
     * Re-evaluates both cards from live device + persisted settings state. Call on
     * every relevant state change (permission result, app-settings flow emission).
     */
    fun refresh(
        smsPermissionGranted: Boolean,
        smsPromptAlreadyShown: Boolean,
        smsPermissionCardDismissed: Boolean,
        smsMiuiSetupAcknowledged: Boolean,
        isBenchmarkBuild: Boolean
    ) {
        viewModelScope.launch {
            val isMiui = DeviceVendorUtils.isMiuiDevice()
            val autostartAllowed = withContext(Dispatchers.IO) {
                if (isMiui) DeviceVendorUtils.isMiuiAutostartAllowed(context) else null
            }
            _uiState.value = SmsSetupUiState(
                smsPermissionGranted = smsPermissionGranted,
                showSmsPermissionCard = DeviceVendorUtils.shouldShowSmsPermissionCard(
                    smsPermissionGranted = smsPermissionGranted,
                    promptAlreadyShown = smsPromptAlreadyShown,
                    cardDismissed = smsPermissionCardDismissed,
                    benchmarkBuild = isBenchmarkBuild
                ),
                isMiuiDevice = isMiui,
                miuiAutostartAllowed = autostartAllowed,
                showMiuiSetupCard = DeviceVendorUtils.shouldShowMiuiSetupCard(
                    isMiuiDevice = isMiui,
                    miuiAutostartAllowed = autostartAllowed,
                    smsPermissionGranted = smsPermissionGranted,
                    acknowledged = smsMiuiSetupAcknowledged,
                    benchmarkBuild = isBenchmarkBuild
                )
            )
        }
    }
}

/** Immutable UI state for the Smart SMS setup cards. */
@Immutable
data class SmsSetupUiState(
    val smsPermissionGranted: Boolean = false,
    val showSmsPermissionCard: Boolean = false,
    val isMiuiDevice: Boolean = false,
    val miuiAutostartAllowed: Boolean? = null,
    val showMiuiSetupCard: Boolean = false
)
