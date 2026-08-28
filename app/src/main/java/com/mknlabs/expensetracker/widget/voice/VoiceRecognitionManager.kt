package com.mknlabs.expensetracker.widget.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reusable speech recognition wrapper.
 *
 * Responsibilities:
 * - Initialize SpeechRecognizer
 * - Request microphone
 * - Listen for speech
 * - Return transcript
 * - Handle timeout, silence, restart, Android errors
 *
 * Exposes simple suspend API. No widget knowledge.
 * Future in-app AI assistant should reuse this manager.
 *
 * Not injected via Hilt — instantiated directly because
 * SpeechRecognizer requires a Context and is not lifecycle-aware.
 */
internal class VoiceRecognitionManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    /**
     * Start listening and return the recognized text.
     *
     * @param timeoutMs Maximum time to listen before auto-stopping.
     * @return Recognized text, or null if recognition failed or timed out.
     */
    suspend fun recognize(timeoutMs: Long = 15000L): String? {
        return withContext(Dispatchers.Main) {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.e(TAG, "Speech recognition not available on this device")
                return@withContext null
            }

            val deferred = CompletableDeferred<String?>()

            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener(deferred))
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)

            // Timeout protection
            val result = withTimeoutOrNull(timeoutMs) {
                deferred.await()
            }

            // Cleanup if timed out
            if (result == null) {
                Log.w(TAG, "Speech recognition timed out after ${timeoutMs}ms")
                try {
                    speechRecognizer?.stopListening()
                    speechRecognizer?.cancel()
                } catch (_: Exception) {}
            }

            result
        }
    }

    /** Stop any active recognition. */
    fun stop() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping recognizer", e)
        }
    }

    /** Release resources. */
    fun destroy() {
        stop()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun createListener(deferred: CompletableDeferred<String?>): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) { /* Volume indicator — unused for now */ }

            override fun onBufferReceived(buffer: ByteArray?) { /* Audio buffer — unused */ }

            override fun onEndOfSpeech() {
                Log.d(TAG, "Speech ended")
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    else -> "Unknown error ($error)"
                }
                Log.e(TAG, "Recognition error: $errorMsg")
                if (!deferred.isCompleted) {
                    deferred.complete(null)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()
                Log.d(TAG, "Recognition result: '$text'")
                if (!deferred.isCompleted) {
                    deferred.complete(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) { /* Not used — one-shot mode */ }

            override fun onEvent(eventType: Int, params: Bundle?) { /* Reserved */ }
        }
    }

    companion object {
        private const val TAG = "VoiceRecognitionMgr"
    }
}
