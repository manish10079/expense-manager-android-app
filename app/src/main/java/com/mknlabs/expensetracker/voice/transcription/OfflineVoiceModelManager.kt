package com.mknlabs.expensetracker.voice.transcription

import android.app.ActivityManager
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

        /** Minimum free internal storage required to download the model pack (500 MB). */
        const val MIN_STORAGE_REQUIRED_BYTES = 500L * 1024 * 1024

        /** Minimum total device RAM required for offline Whisper (3.4 GB to safely cover 4 GB RAM phones). */
        const val MIN_RAM_REQUIRED_BYTES = 3400L * 1024 * 1024

        private val DOWNLOAD_URLS = listOf(
            TOKENS_FILE to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-tokens.txt",
            ENCODER_FILE to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-encoder.int8.onnx",
            DECODER_FILE to "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-decoder.int8.onnx",
        )
    }

    /**
     * Checks if the device has at least 3.4 GB total RAM and is not in low-RAM/low-memory mode.
     */
    fun hasSufficientRamForWhisper(): Boolean {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        if (actManager.isLowRamDevice || memInfo.lowMemory) {
            return false
        }
        return memInfo.totalMem >= MIN_RAM_REQUIRED_BYTES
    }

    /**
     * Checks if there is at least 500 MB of usable internal storage available.
     */
    fun hasSufficientStorageForDownload(): Boolean {
        return context.filesDir.usableSpace >= MIN_STORAGE_REQUIRED_BYTES
    }

    /**
     * Determines whether offline Whisper should be used based on model presence, device RAM, and free storage.
     */
    fun canUseOfflineWhisper(): Boolean {
        if (!hasSufficientRamForWhisper()) {
            Log.d(TAG, "Device does not meet RAM requirements for Whisper. Fallback to streaming SpeechRecognizer.")
            return false
        }
        if (getModelDirectory() != null) {
            // Model is already downloaded and verified on disk
            return true
        }
        val hasStorage = hasSufficientStorageForDownload()
        if (!hasStorage) {
            Log.d(TAG, "Device has less than 500 MB free storage. Fallback to streaming SpeechRecognizer.")
        }
        return hasStorage
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

        if (!hasSufficientStorageForDownload()) {
            Log.w(TAG, "Cannot start model download: insufficient free storage (< 500 MB)")
            _modelStatus.value = VoiceModelStatus.Error("Insufficient storage space to download offline model")
            return
        }

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
                    var currentUrl = urlString
                    var connection: HttpURLConnection? = null
                    var redirects = 0

                    while (true) {
                        val url = URL(currentUrl)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connectTimeout = 30_000
                        conn.readTimeout = 60_000
                        conn.instanceFollowRedirects = true
                        val code = conn.responseCode
                        if (code in 300..399) {
                            val newLoc = conn.getHeaderField("Location")
                            conn.disconnect()
                            if (newLoc.isNullOrBlank() || redirects++ >= 5) {
                                throw IllegalStateException("HTTP $code redirect failed for $fileName")
                            }
                            currentUrl = newLoc
                            continue
                        }
                        if (code !in 200..299) {
                            conn.disconnect()
                            throw IllegalStateException("HTTP $code while downloading $fileName")
                        }
                        connection = conn
                        break
                    }

                    val activeConnection = connection ?: throw IllegalStateException("Could not establish connection for $fileName")
                    val fileLength = activeConnection.contentLength.coerceAtLeast(1)
                    activeConnection.inputStream.use { input ->
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
                    activeConnection.disconnect()

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
