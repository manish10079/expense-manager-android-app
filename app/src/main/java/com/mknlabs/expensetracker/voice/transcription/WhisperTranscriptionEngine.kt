package com.mknlabs.expensetracker.voice.transcription

import android.util.Log
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device offline speech recognition engine using OpenAI Whisper Tiny INT8 via sherpa-onnx.
 */
@Singleton
class WhisperTranscriptionEngine @Inject constructor(
    private val modelManager: OfflineVoiceModelManager,
) {
    companion object {
        private const val TAG = "WhisperEngine"
        private const val SAMPLE_RATE = 16000
    }

    private var recognizer: OfflineRecognizer? = null
    private val mutex = Mutex()

    /**
     * Initializes the native Sherpa-ONNX Whisper recognizer if not already initialized.
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.Default) {
        mutex.withLock {
            if (recognizer != null) return@withContext Result.success(Unit)

            val modelDir = modelManager.getModelDirectory()
                ?: return@withContext Result.failure(IllegalStateException("Whisper model files not found on device"))

            val encoderFile = File(modelDir, OfflineVoiceModelManager.ENCODER_FILE)
            val decoderFile = File(modelDir, OfflineVoiceModelManager.DECODER_FILE)
            val tokensFile = File(modelDir, OfflineVoiceModelManager.TOKENS_FILE)

            if (!encoderFile.exists() || !decoderFile.exists() || !tokensFile.exists()) {
                return@withContext Result.failure(IllegalStateException("Missing model files in ${modelDir.absolutePath}"))
            }

            try {
                Log.d(TAG, "Initializing Sherpa-ONNX Whisper OfflineRecognizer...")
                val whisperConfig = OfflineWhisperModelConfig(
                    encoder = encoderFile.absolutePath,
                    decoder = decoderFile.absolutePath,
                    language = "",
                    task = "transcribe",
                )

                val modelConfig = OfflineModelConfig(
                    whisper = whisperConfig,
                    tokens = tokensFile.absolutePath,
                    modelType = "whisper",
                    numThreads = Runtime.getRuntime().availableProcessors().coerceIn(1, 4),
                    debug = false,
                )

                val featConfig = FeatureConfig(
                    sampleRate = SAMPLE_RATE,
                    featureDim = 80,
                )

                val recognizerConfig = OfflineRecognizerConfig(
                    featConfig = featConfig,
                    modelConfig = modelConfig,
                )

                recognizer = OfflineRecognizer(
                    assetManager = null,
                    config = recognizerConfig,
                )
                Log.d(TAG, "Sherpa-ONNX Whisper OfflineRecognizer initialized successfully.")
                Result.success(Unit)
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to initialize OfflineRecognizer", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Transcribes a 16kHz mono WAV file to clean punctuated text.
     */
    suspend fun transcribe(wavFile: File): Result<String> = withContext(Dispatchers.Default) {
        if (!wavFile.exists() || wavFile.length() <= 44L) {
            return@withContext Result.failure(IllegalArgumentException("WAV file is empty or missing"))
        }

        initialize().onFailure { return@withContext Result.failure(it) }

        mutex.withLock {
            val rec = recognizer
                ?: return@withContext Result.failure(IllegalStateException("Recognizer not initialized"))

            try {
                val samples = readWavSamples(wavFile)
                if (samples.isEmpty()) {
                    return@withContext Result.failure(IllegalArgumentException("No audio samples in WAV file"))
                }

                val stream = rec.createStream()
                try {
                    stream.acceptWaveform(samples, SAMPLE_RATE)
                    rec.decode(stream)
                    val result = rec.getResult(stream)
                    val text = result.text.trim()
                    Log.d(TAG, "Whisper transcription result: '$text'")
                    Result.success(text)
                } finally {
                    stream.release()
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Transcription failed", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Extracts normalized float samples (-1.0f to 1.0f) from a standard 16-bit PCM WAV file.
     */
    private fun readWavSamples(wavFile: File): FloatArray {
        val bytes = wavFile.readBytes()
        if (bytes.size <= 44) return FloatArray(0)

        val numSamples = (bytes.size - 44) / 2
        val samples = FloatArray(numSamples)
        var byteIdx = 44
        for (i in 0 until numSamples) {
            val low = bytes[byteIdx++].toInt() and 0xFF
            val high = bytes[byteIdx++].toInt()
            val sampleShort = (high shl 8) or low
            samples[i] = sampleShort / 32768.0f
        }
        return samples
    }

    /**
     * Releases native ONNX runtime resources.
     */
    fun release() {
        try {
            recognizer?.release()
        } catch (_: Exception) {}
        recognizer = null
    }
}
