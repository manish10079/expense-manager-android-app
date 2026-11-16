package com.mknlabs.expensetracker.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.StringRes
import com.mknlabs.expensetracker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages streaming speech recognition for voice-based transaction entry.
 * Acts as a lightweight, zero-disk fallback when offline Whisper model
 * cannot be downloaded or the device has low RAM.
 */
@Singleton
class VoiceRecognitionManager @Inject constructor() {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _errorResId = MutableStateFlow<Int?>(null)
    val errorResId: StateFlow<Int?> = _errorResId.asStateFlow()

    var onResultListener: ((String) -> Unit)? = null
    var onPartialResultListener: ((String) -> Unit)? = null

    /**
     * Start listening for voice input.
     */
    fun startListening(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorResId.value = R.string.msg_voice_error_recognizer
            _error.value = "Speech recognition not available on this device"
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }

        val locale = Locale.getDefault()
        val languageTag = locale.toLanguageTag()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your transaction...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        _recognizedText.value = ""
        _audioAmplitude.value = 0f
        _error.value = null
        _errorResId.value = null

        speechRecognizer?.startListening(intent)
        _isListening.value = true
    }

    /**
     * Stop listening.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
        _audioAmplitude.value = 0f
    }

    /**
     * Cancel listening and clean up.
     */
    fun cancel() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
        _audioAmplitude.value = 0f
    }

    /**
     * Clean up resources.
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
        _audioAmplitude.value = 0f
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _error.value = null
                _errorResId.value = null
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB typically ranges from -2f to 10f in Android SpeechRecognizer
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _audioAmplitude.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                _audioAmplitude.value = 0f
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _audioAmplitude.value = 0f
                val resId = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> R.string.msg_voice_error_timeout
                    SpeechRecognizer.ERROR_AUDIO -> R.string.msg_voice_error_audio
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> R.string.msg_voice_error_no_permission
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> R.string.msg_voice_error_network
                    else -> R.string.msg_voice_error_recognizer
                }
                _errorResId.value = resId
                _error.value = "Speech recognition error code: $error"
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty().trim()
                _recognizedText.value = text
                _isListening.value = false
                _audioAmplitude.value = 0f
                onResultListener?.invoke(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty().trim()
                if (text.isNotBlank()) {
                    _recognizedText.value = text
                    onPartialResultListener?.invoke(text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
