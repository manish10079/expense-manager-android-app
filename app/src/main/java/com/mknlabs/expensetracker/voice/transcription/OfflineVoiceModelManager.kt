package com.mknlabs.expensetracker.voice.transcription

import android.content.Context
import android.util.Log
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed interface VoiceModelStatus {
    object NotInstalled : VoiceModelStatus
    data class Downloading(val progress: Float) : VoiceModelStatus
    data class Ready(val modelDir: File) : VoiceModelStatus
    data class Error(val message: String) : VoiceModelStatus
}

/**
 * Manages the Whisper Tiny model delivery lifecycle.
 * In production: Uses Google Play Asset Delivery (fast-follow).
 * In debug / sideload / fallback: Manages local download directly to filesDir.
 */
@Singleton
class OfflineVoiceModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "VoiceModelManager"
        const val PACK_NAME = "whisper_model"
        const val ENCODER_FILE = "tiny-encoder.int8.onnx"
        const val DECODER_FILE = "tiny-decoder.int8.onnx"
        const val TOKENS_FILE = "tiny-tokens.txt"

        private val DOWNLOAD_URLS = listOf(
            TOKENS_FILE to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-tokens.txt",
            ENCODER_FILE to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-encoder.int8.onnx",
            DECODER_FILE to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-decoder.int8.onnx",
        )
    }

    private val assetPackManager: AssetPackManager by lazy {
        AssetPackManagerFactory.getInstance(context)
    }

    private val _modelStatus = MutableStateFlow<VoiceModelStatus>(VoiceModelStatus.NotInstalled)
    val modelStatus: StateFlow<VoiceModelStatus> = _modelStatus.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isListenerRegistered = false

    private val assetPackListener = AssetPackStateUpdateListener { state ->
        if (state.name() == PACK_NAME) {
            when (state.status()) {
                AssetPackStatus.COMPLETED -> {
                    Log.d(TAG, "Play Asset Pack download completed.")
                    checkModelStatus()
                }
                AssetPackStatus.DOWNLOADING -> {
                    val downloaded = state.bytesDownloaded()
                    val total = state.totalBytesToDownload()
                    val progress = if (total > 0) (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
                    Log.d(TAG, "Asset pack downloading: ${(progress * 100).toInt()}%")
                    _modelStatus.value = VoiceModelStatus.Downloading(progress)
                }
                AssetPackStatus.TRANSFERRING,
                AssetPackStatus.PENDING -> {
                    _modelStatus.value = VoiceModelStatus.Downloading(0f)
                }
                AssetPackStatus.FAILED -> {
                    Log.w(TAG, "Asset pack download failed with error code: ${state.errorCode()}")
                    // Fall back to direct in-app download
                    downloadDirectly()
                }
                AssetPackStatus.CANCELED -> {
                    _modelStatus.value = VoiceModelStatus.NotInstalled
                }
                else -> {}
            }
        }
    }

    init {
        checkModelStatus()
    }

    /**
     * Checks if the model exists in the Play Asset Pack directory or internal storage.
     */
    fun checkModelStatus(): VoiceModelStatus {
        // 1. Check Play Asset Pack directory
        try {
            val location = assetPackManager.getPackLocation(PACK_NAME)
            val packPath = location?.assetsPath() ?: location?.path()
            if (packPath != null) {
                val packDir = File(packPath, "whisper")
                if (isValidModelDir(packDir)) {
                    val ready = VoiceModelStatus.Ready(packDir)
                    _modelStatus.value = ready
                    return ready
                }
                if (isValidModelDir(File(packPath))) {
                    val ready = VoiceModelStatus.Ready(File(packPath))
                    _modelStatus.value = ready
                    return ready
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "AssetPackManager query ignored: ${e.message}")
        }

        // 2. Check internal storage fallback: context.filesDir/whisper
        val fallbackDir = File(context.filesDir, "whisper")
        if (isValidModelDir(fallbackDir)) {
            val ready = VoiceModelStatus.Ready(fallbackDir)
            _modelStatus.value = ready
            return ready
        }

        // Model not yet ready
        if (_modelStatus.value !is VoiceModelStatus.Downloading) {
            _modelStatus.value = VoiceModelStatus.NotInstalled
        }
        return _modelStatus.value
    }

    private fun isValidModelDir(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory) return false
        val encoder = File(dir, ENCODER_FILE)
        val decoder = File(dir, DECODER_FILE)
        val tokens = File(dir, TOKENS_FILE)
        return encoder.exists() && encoder.length() > 1_000_000 &&
               decoder.exists() && decoder.length() > 10_000_000 &&
               tokens.exists() && tokens.length() > 10_000
    }

    /**
     * Triggers model download if not already installed or downloading.
     */
    fun ensureModelReady() {
        if (_modelStatus.value is VoiceModelStatus.Ready) return
        if (_modelStatus.value is VoiceModelStatus.Downloading) return

        if (!isListenerRegistered) {
            try {
                assetPackManager.registerListener(assetPackListener)
                isListenerRegistered = true
            } catch (e: Exception) {
                Log.w(TAG, "Could not register AssetPackStateUpdateListener", e)
            }
        }

        _modelStatus.value = VoiceModelStatus.Downloading(0f)

        try {
            assetPackManager.fetch(listOf(PACK_NAME))
                .addOnSuccessListener {
                    Log.d(TAG, "Play Asset Pack fetch request accepted")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Play Asset Pack fetch failed, falling back to direct download: ${e.message}")
                    downloadDirectly()
                }
        } catch (e: Exception) {
            Log.w(TAG, "AssetPack fetch threw exception, fallback to direct download", e)
            downloadDirectly()
        }
    }

    private fun downloadDirectly() {
        scope.launch {
            try {
                val targetDir = File(context.filesDir, "whisper")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                _modelStatus.value = VoiceModelStatus.Downloading(0.05f)

                var completedFiles = 0
                val totalFiles = DOWNLOAD_URLS.size

                for ((fileName, urlString) in DOWNLOAD_URLS) {
                    val destFile = File(targetDir, fileName)
                    if (destFile.exists() && destFile.length() > 1000) {
                        completedFiles++
                        continue
                    }

                    val tempFile = File(targetDir, "$fileName.download")
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 30_000
                    connection.readTimeout = 60_000
                    connection.connect()

                    if (connection.responseCode !in 200..299) {
                        throw IllegalStateException("HTTP ${connection.responseCode} while downloading $fileName")
                    }

                    val fileLength = connection.contentLength.coerceAtLeast(1)
                    connection.inputStream.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(8192)
                            var totalRead = 0L
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                totalRead += read
                                val fileProgress = totalRead.toFloat() / fileLength.toFloat()
                                val overallProgress = (completedFiles + fileProgress) / totalFiles
                                _modelStatus.value = VoiceModelStatus.Downloading(overallProgress.coerceIn(0f, 0.99f))
                            }
                            output.flush()
                        }
                    }

                    if (tempFile.renameTo(destFile)) {
                        completedFiles++
                    } else {
                        tempFile.copyTo(destFile, overwrite = true)
                        tempFile.delete()
                        completedFiles++
                    }
                }

                if (isValidModelDir(targetDir)) {
                    _modelStatus.value = VoiceModelStatus.Ready(targetDir)
                    Log.d(TAG, "Direct Whisper model download succeeded")
                } else {
                    _modelStatus.value = VoiceModelStatus.Error("Model files validation failed after download")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Direct download failed", e)
                _modelStatus.value = VoiceModelStatus.Error(e.localizedMessage ?: "Download failed")
            }
        }
    }

    /**
     * Returns the verified model directory or null if not yet installed.
     */
    fun getModelDirectory(): File? {
        val status = _modelStatus.value
        return if (status is VoiceModelStatus.Ready) {
            status.modelDir
        } else {
            val checked = checkModelStatus()
            if (checked is VoiceModelStatus.Ready) checked.modelDir else null
        }
    }
}
