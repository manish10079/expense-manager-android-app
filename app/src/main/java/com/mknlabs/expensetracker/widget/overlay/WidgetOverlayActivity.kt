package com.mknlabs.expensetracker.widget.overlay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.core.ui.theme.isDark
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import java.util.Locale

class WidgetOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getStringExtra(EXTRA_OVERLAY_UI_MODE) ?: OverlayUiMode.TEXT
        setContent {
            ExpenseTrackerTheme {
                WidgetOverlaySheet(
                    mode = mode,
                    onClose = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetOverlaySheet(
    mode: String,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dark = MaterialTheme.colorScheme.isDark
    val overlaySheet = if (dark) Color(0xF20B1220) else Color.White.copy(alpha = 0.80f)
    val onOverlaySheet = if (dark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = overlaySheet,
        contentColor = onOverlaySheet
    ) {
        when (mode) {
            OverlayUiMode.VOICE -> OverlayVoiceContent(onClose = onClose)
            OverlayUiMode.SCAN -> OverlayScanContent(onClose = onClose)
            else -> OverlayTextContent(onClose = onClose)
        }
    }
}

@Composable
private fun OverlayVoiceContent(onClose: () -> Unit) {
    val context = LocalContext.current
    val inspection = LocalInspectionMode.current
    var transcript by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Listening…") }
    var recognizerRef by remember { mutableStateOf<SpeechRecognizer?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) onClose()
    }
    DisposableEffect(inspection) {
        if (inspection) {
            onDispose { }
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                onDispose { }
            } else {
                val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        status = "Listening…"
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        status = "Processing…"
                    }
                    override fun onError(error: Int) {
                        status = "Couldn’t catch that"
                    }
                    override fun onResults(results: Bundle?) {
                        transcript = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        status = "Done"
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        transcript = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                recognizerRef = recognizer
                recognizer.startListening(intent)
                onDispose {
                    recognizerRef = null
                    recognizer.cancel()
                    recognizer.destroy()
                }
            }
        }
    }
    OverlayVoiceSheet(
        transcript = transcript,
        isProcessing = status == "Processing…",
        onCancel = onClose,
        onDone = { recognizerRef?.stopListening() ?: onClose() }
    )
}

@Composable
private fun OverlayScanContent(onClose: () -> Unit) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { onClose() }
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        pendingCameraUri = null
        if (saved) onClose()
    }
    OverlayScanSheet(
        onCamera = {
            val photo = File(context.cacheDir, "widget_scan_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photo
            )
            pendingCameraUri = uri
            takePicture.launch(uri)
        },
        onPick = {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onClose = onClose
    )
}
