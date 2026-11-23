package com.mknlabs.expensetracker.voice.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * High-performance audio recorder producing 16kHz 16-bit Mono WAV audio files
 * suitable for offline on-device Whisper transcription.
 */
@Singleton
class WavAudioRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "WavAudioRecorder"
        const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val pcmTempFile: File
        get() = File(context.cacheDir, "voice_recording_temp.pcm")

    private val wavOutputFile: File
        get() = File(context.cacheDir, "voice_recording.wav")

    /**
     * Start capturing microphone audio to cache.
     */
    fun startRecording(): Result<Unit> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure(SecurityException("RECORD_AUDIO permission not granted"))
        }

        // A previous session can still be marked as recording after its owner went
        // away (screen dismissed mid-teardown, another screen's ViewModel, etc.).
        // Returning success here used to leave the new session with no capture at
        // all: the caller believed it was recording while no read loop produced
        // audio, so the amplitude stayed flat and the session ended in "no speech".
        // Release the stale session instead, so this caller always gets a live
        // recorder it owns.
        if (_isRecording.value) {
            Log.w(TAG, "startRecording() while a previous session was still active; releasing it")
            cancelRecording()
        }

        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return Result.failure(IllegalStateException("Invalid AudioRecord buffer size"))
        }

        val bufferSize = minBufferSize.coerceAtLeast(SAMPLE_RATE * 2)

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                return Result.failure(IllegalStateException("Failed to initialize AudioRecord"))
            }

            audioRecord = record
            record.startRecording()
            _isRecording.value = true
            _amplitude.value = 0f

            // Clean previous temp files
            pcmTempFile.delete()
            wavOutputFile.delete()

            recordingJob = scope.launch {
                val buffer = ShortArray(bufferSize / 2)
                val byteBuffer = ByteArray(bufferSize)

                try {
                    FileOutputStream(pcmTempFile).use { outputStream ->
                        while (isActive && _isRecording.value) {
                            val shortsRead = record.read(buffer, 0, buffer.size)
                            if (shortsRead > 0) {
                                // Write to PCM file in Little-Endian byte order
                                var byteIdx = 0
                                var sumSquares = 0.0
                                for (i in 0 until shortsRead) {
                                    val sample = buffer[i]
                                    byteBuffer[byteIdx++] = (sample.toInt() and 0xFF).toByte()
                                    byteBuffer[byteIdx++] = ((sample.toInt() shr 8) and 0xFF).toByte()
                                    sumSquares += sample.toDouble() * sample.toDouble()
                                }
                                outputStream.write(byteBuffer, 0, byteIdx)

                                // Calculate RMS amplitude (0.0 to 1.0)
                                val rms = sqrt(sumSquares / shortsRead)
                                val normalized = (rms / 32767.0).toFloat().coerceIn(0f, 1f)
                                _amplitude.value = normalized
                            }
                        }
                        outputStream.flush()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error while recording PCM audio", e)
                }
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            cleanup()
            return Result.failure(e)
        }
    }

    /**
     * Stop capturing audio, assemble standard 44-byte WAV header, and return output file.
     */
    suspend fun stopRecording(): File? = withContext(Dispatchers.IO) {
        if (!_isRecording.value) return@withContext null

        _isRecording.value = false
        _amplitude.value = 0f

        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "AudioRecord stop error", e)
        }

        recordingJob?.cancel()
        recordingJob?.join()
        recordingJob = null

        audioRecord?.release()
        audioRecord = null

        val pcm = pcmTempFile
        if (!pcm.exists() || pcm.length() == 0L) {
            pcm.delete()
            return@withContext null
        }

        val wav = wavOutputFile
        try {
            WavUtils.convertPcmToWav(pcm, wav, SAMPLE_RATE, WavUtils.CHANNELS)
            pcm.delete()
            return@withContext wav
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert PCM to WAV", e)
            pcm.delete()
            wav.delete()
            return@withContext null
        }
    }

    /**
     * Cancel ongoing recording and delete temporary files.
     */
    fun cancelRecording() {
        _isRecording.value = false
        _amplitude.value = 0f
        cleanup()
    }

    private fun cleanup() {
        try {
            recordingJob?.cancel()
            recordingJob = null
            audioRecord?.stop()
        } catch (_: Exception) {}
        try {
            audioRecord?.release()
            audioRecord = null
        } catch (_: Exception) {}

        pcmTempFile.delete()
        wavOutputFile.delete()
    }
}
