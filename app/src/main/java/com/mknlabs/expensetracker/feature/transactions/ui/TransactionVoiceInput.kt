package com.mknlabs.expensetracker.feature.transactions.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.mknlabs.expensetracker.core.ui.components.VoiceSheetState
import com.mknlabs.expensetracker.voice.VoiceAddViewModel
import com.mknlabs.expensetracker.voice.VoiceInputUiState

/**
 * Everything the Add/Edit screen needs to talk to the voice-input runtime: what the sheet
 * should show, whether it is open, and the three actions the UI can raise.
 */
internal class TransactionVoiceInput(
    val uiState: VoiceInputUiState,
    val isSheetVisible: Boolean,
    val onMicClick: () -> Unit,
    val onSheetDismiss: () -> Unit,
    val onSheetRetry: () -> Unit
)

/**
 * Owns the `SpeechRecognizer`, the microphone permission and the sheet's visibility, and hands
 * the Add/Edit screen a plain value it can pass straight down to its content.
 *
 * <b>Why this is a file of its own.</b> `android.speech.SpeechRecognizer` (and `RecognizerIntent`
 * / `RecognitionListener`) do not exist in the Compose Preview renderer. A class whose bytecode
 * references them cannot be loaded there at all, so Android Studio's tooling throws
 * `NoClassDefFoundError: android/speech/SpeechRecognizer` while reflectively inspecting the
 * preview's declaring class — before a single frame is composed, and regardless of any runtime
 * guard such as `LocalInspectionMode`. Keeping these imports out of the screen's file is what
 * keeps the screen's preview renderable, so do not fold this back in.
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
            voiceViewModel.resetToListening()
            isSheetVisible = true
        } else {
            voiceViewModel.onRecognizerError(R.string.msg_voice_error_no_permission)
            isSheetVisible = true
        }
    }

    LaunchedEffect(autoStartVoice) {
        if (autoStartVoice) {
            onVoiceAutoStarted()
            // The amount field is auto-focused and the keyboard is shown on
            // screen entry. An open IME can make the SpeechRecognizer fail
            // immediately ("try again" error), so dismiss both before the
            // voice sheet starts listening — same as the mic button tap.
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            // Give the keyboard time to fully dismiss before the
            // SpeechRecognizer starts — otherwise it races with the
            // animation and fails with the "try again" error.
            kotlinx.coroutines.delay(300)
            if (micPermissionGranted) {
                voiceViewModel.resetToListening()
                isSheetVisible = true
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // SpeechRecognizer — created once, started/stopped with the sheet
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    DisposableEffect(speechRecognizer) {
        onDispose { speechRecognizer.destroy() }
    }
    LaunchedEffect(isSheetVisible, voiceUiState.sheetState) {
        if (isSheetVisible && voiceUiState.sheetState == VoiceSheetState.LISTENING) {
            Log.d("VoiceInput", "Starting speech recognizer, sheetVisible=$isSheetVisible, sheetState=${voiceUiState.sheetState}")
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d("VoiceInput", "onReadyForSpeech: params=$params")
                }
                override fun onBeginningOfSpeech() {
                    Log.d("VoiceInput", "onBeginningOfSpeech")
                }
                override fun onRmsChanged(rmsdB: Float) {
                    // Too frequent to log — intentionally silent
                }
                override fun onBufferReceived(buffer: ByteArray?) {
                    Log.d("VoiceInput", "onBufferReceived: ${buffer?.size ?: 0} bytes")
                }
                override fun onEndOfSpeech() {
                    Log.d("VoiceInput", "onEndOfSpeech: currentViewModelTranscript='${voiceUiState.transcript}'")
                    // Do NOT call onSpeechResult here — voiceUiState.transcript is stale
                    // (captured at LaunchedEffect launch time). The real result arrives in onResults.
                }
                override fun onError(error: Int) {
                    val errorLabel = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "ERROR_NO_MATCH"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "ERROR_SPEECH_TIMEOUT"
                        SpeechRecognizer.ERROR_NETWORK -> "ERROR_NETWORK"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ERROR_NETWORK_TIMEOUT"
                        SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
                        SpeechRecognizer.ERROR_CLIENT -> "ERROR_CLIENT"
                        SpeechRecognizer.ERROR_SERVER -> "ERROR_SERVER"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_RECOGNIZER_BUSY"
                        else -> "ERROR_UNKNOWN($error)"
                    }
                    Log.e("VoiceInput", "onError: $errorLabel (code=$error)")
                    val errorResId = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> R.string.msg_voice_error_empty_input
                        SpeechRecognizer.ERROR_NETWORK,
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> R.string.msg_voice_error_network
                        SpeechRecognizer.ERROR_AUDIO -> R.string.msg_voice_error_audio
                        else -> R.string.msg_voice_error_recognizer
                    }
                    voiceViewModel.onRecognizerError(errorResId)
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    val confidenceScores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                    Log.d("VoiceInput", "onResults: text='$text', matchCount=${matches?.size ?: 0}, confidence=${confidenceScores?.firstOrNull()}")
                    voiceViewModel.onSpeechResult(text)
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    Log.d("VoiceInput", "onPartialResults: text='$text'")
                    voiceViewModel.onPartialResult(text)
                }
                override fun onEvent(eventType: Int, params: Bundle?) {
                    Log.d("VoiceInput", "onEvent: eventType=$eventType")
                }
            })
            speechRecognizer.startListening(intent)
        }
    }

    return TransactionVoiceInput(
        uiState = voiceUiState,
        isSheetVisible = isSheetVisible,
        onMicClick = {
            voiceViewModel.resetToListening()
            if (micPermissionGranted) {
                isSheetVisible = true
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onSheetDismiss = {
            voiceViewModel.dismiss()
            isSheetVisible = false
        },
        onSheetRetry = { voiceViewModel.resetToListening() }
    )
}
