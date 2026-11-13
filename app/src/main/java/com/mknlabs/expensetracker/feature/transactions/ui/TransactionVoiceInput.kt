package com.mknlabs.expensetracker.feature.transactions.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.voice.VoiceAddViewModel
import com.mknlabs.expensetracker.voice.VoiceInputUiState

/**
 * Everything the Add/Edit screen needs to talk to the voice-input runtime: what the sheet
 * should show, whether it is open, and the actions the UI can raise.
 */
internal class TransactionVoiceInput(
    val uiState: VoiceInputUiState,
    val isSheetVisible: Boolean,
    val onMicClick: () -> Unit,
    val onSheetDismiss: () -> Unit,
    val onSheetRetry: () -> Unit,
    val onSheetDone: () -> Unit,
)

/**
 * Owns the microphone permission and the sheet's visibility, and hands
 * the Add/Edit screen a plain value it can pass straight down to its content.
 */
@Composable
internal fun rememberTransactionVoiceInput(
    autoStartVoice: Boolean,
    onVoiceAutoStarted: () -> Unit
): TransactionVoiceInput {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val voiceViewModel: VoiceAddViewModel = hiltViewModel()
    val voiceUiState by voiceViewModel.uiState.collectAsStateWithLifecycle()

    var isSheetVisible by rememberSaveable { mutableStateOf(false) }
    val hasMicPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
    }
    var micPermissionGranted by rememberSaveable { mutableStateOf(hasMicPermission) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        micPermissionGranted = granted
        if (granted) {
            voiceViewModel.startVoiceInput()
            isSheetVisible = true
        } else {
            voiceViewModel.onRecognizerError(R.string.msg_voice_error_no_permission)
            isSheetVisible = true
        }
    }

    LaunchedEffect(autoStartVoice) {
        if (autoStartVoice) {
            onVoiceAutoStarted()
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            kotlinx.coroutines.delay(300)
            if (micPermissionGranted) {
                voiceViewModel.startVoiceInput()
                isSheetVisible = true
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    return TransactionVoiceInput(
        uiState = voiceUiState,
        isSheetVisible = isSheetVisible,
        onMicClick = {
            if (micPermissionGranted) {
                voiceViewModel.startVoiceInput()
                isSheetVisible = true
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onSheetDismiss = {
            voiceViewModel.dismiss()
            isSheetVisible = false
        },
        onSheetRetry = { voiceViewModel.startVoiceInput() },
        onSheetDone = { voiceViewModel.stopAndProcess() }
    )
}
