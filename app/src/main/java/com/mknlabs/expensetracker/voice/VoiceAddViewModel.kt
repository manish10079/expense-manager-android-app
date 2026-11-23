package com.mknlabs.expensetracker.voice

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.functions.FirebaseFunctionsException
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.AiUsageTracker
import com.mknlabs.expensetracker.ai.aiUsageDataStore
import com.mknlabs.expensetracker.ai.cloud.AiParseApiService
import com.mknlabs.expensetracker.ai.cloud.AiParseRequest
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.ai.cloud.UserContextProvider
import com.mknlabs.expensetracker.ai.offline.OfflineVoiceParser
import com.mknlabs.expensetracker.core.ui.components.VoiceSheetState
import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserType
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import com.mknlabs.expensetracker.voice.audio.WavAudioRecorder
import com.mknlabs.expensetracker.voice.transcription.OfflineVoiceModelManager
import com.mknlabs.expensetracker.voice.transcription.VoiceModelStatus
import com.mknlabs.expensetracker.voice.transcription.WhisperTranscriptionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class VoiceInputUiState(
    val sheetState: VoiceSheetState = VoiceSheetState.LISTENING,
    val transcript: String = "",
    val audioAmplitude: Float = 0f,
    val downloadProgress: Float = 0f,
    val parsedTransaction: ParsedVoiceTransaction? = null,
    val reviewItems: List<AiParsedReviewItem> = emptyList(),
    val errorMessageResId: Int? = null,
    val parserType: VoiceParserType? = null,
    val remainingAiParses: Int? = null,
)

@HiltViewModel
class VoiceAddViewModel @Inject constructor(
    application: Application,
    private val offlineParser: OfflineVoiceParser,
    private val aiParseApiService: AiParseApiService,
    private val userContextProvider: UserContextProvider,
    private val categoryDao: CategoryDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val monetizationRepository: MonetizationRepository,
    private val wavAudioRecorder: WavAudioRecorder,
    private val whisperEngine: WhisperTranscriptionEngine,
    private val modelManager: OfflineVoiceModelManager,
    private val voiceRecognitionManager: VoiceRecognitionManager,
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "VoiceAddViewModel"
    }

    private enum class VoiceEngineMode {
        OFFLINE_WHISPER,
        STREAMING_SPEECH_RECOGNIZER,
    }

    private var activeEngineMode = VoiceEngineMode.OFFLINE_WHISPER

    private val aiUsageTracker by lazy {
        AiUsageTracker(getApplication<Application>().aiUsageDataStore)
    }

    private val _uiState = MutableStateFlow(VoiceInputUiState())
    val uiState: StateFlow<VoiceInputUiState> = _uiState.asStateFlow()

    private var modelObservationJob: Job? = null
    private var amplitudeObservationJob: Job? = null
    private var silenceJob: Job? = null

    /**
     * Start the full voice input flow.
     * Evaluates device RAM (>=4GB) and free storage (>=500MB):
     * - Qualified devices use 100% offline multilingual Whisper Tiny.
     * - Low RAM or low storage devices gracefully stream via system SpeechRecognizer.
     */
    fun startVoiceInput() {
        if (modelManager.canUseOfflineWhisper()) {
            activeEngineMode = VoiceEngineMode.OFFLINE_WHISPER
            val modelStatus = modelManager.checkModelStatus()
            when (modelStatus) {
                is VoiceModelStatus.Ready -> {
                    beginAudioRecording()
                }
                is VoiceModelStatus.Downloading -> {
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.DOWNLOADING_MODEL,
                        downloadProgress = modelStatus.progress,
                    )
                    observeModelDownload()
                }
                is VoiceModelStatus.NotInstalled,
                is VoiceModelStatus.Error -> {
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.DOWNLOADING_MODEL,
                        downloadProgress = 0f,
                    )
                    modelManager.ensureModelReady()
                    observeModelDownload()
                }
            }
        } else {
            // Low RAM (<4GB) or Low Storage (<500MB without model):
            // Gracefully stream via system SpeechRecognizer with 0MB storage and 0MB native RAM
            activeEngineMode = VoiceEngineMode.STREAMING_SPEECH_RECOGNIZER
            startStreamingVoiceInput()
        }
    }

    private fun startStreamingVoiceInput() {
        silenceJob?.cancel()
        silenceJob = null
        amplitudeObservationJob?.cancel()
        amplitudeObservationJob = null
        modelObservationJob?.cancel()
        modelObservationJob = null

        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.LISTENING,
            transcript = "",
            audioAmplitude = 0f,
            errorMessageResId = null,
        )

        voiceRecognitionManager.onPartialResultListener = { partialText ->
            onPartialResult(partialText)
        }

        voiceRecognitionManager.onResultListener = { finalText ->
            onSpeechResult(finalText)
        }

        // Live amplitude reactive visualizer in bottom sheet
        amplitudeObservationJob = viewModelScope.launch {
            voiceRecognitionManager.audioAmplitude.collect { amp ->
                _uiState.value = _uiState.value.copy(audioAmplitude = amp)
            }
        }

        // Error observation
        viewModelScope.launch {
            voiceRecognitionManager.errorResId.collect { errResId ->
                if (errResId != null) {
                    val currentTranscript = _uiState.value.transcript.trim().ifBlank {
                        voiceRecognitionManager.recognizedText.value.trim()
                    }
                    if (currentTranscript.isNotBlank()) {
                        Log.d(TAG, "Error received ($errResId) but transcript is available ('$currentTranscript'). Processing...")
                        onSpeechResult(currentTranscript)
                    } else if (_uiState.value.sheetState == VoiceSheetState.LISTENING || _uiState.value.sheetState == VoiceSheetState.PROCESSING) {
                        _uiState.value = _uiState.value.copy(
                            sheetState = VoiceSheetState.ERROR,
                            errorMessageResId = errResId,
                        )
                    }
                }
            }
        }

        voiceRecognitionManager.startListening(getApplication())
    }

    private fun observeModelDownload() {
        modelObservationJob?.cancel()
        modelObservationJob = viewModelScope.launch {
            modelManager.modelStatus.collect { status ->
                when (status) {
                    is VoiceModelStatus.Downloading -> {
                        _uiState.value = _uiState.value.copy(
                            sheetState = VoiceSheetState.DOWNLOADING_MODEL,
                            downloadProgress = status.progress,
                        )
                    }
                    is VoiceModelStatus.Ready -> {
                        modelObservationJob?.cancel()
                        modelObservationJob = null
                        beginAudioRecording()
                    }
                    is VoiceModelStatus.Error -> {
                        Log.w(TAG, "Offline Whisper model download error: ${status.message}. Fallback to streaming SpeechRecognizer.")
                        modelObservationJob?.cancel()
                        modelObservationJob = null
                        activeEngineMode = VoiceEngineMode.STREAMING_SPEECH_RECOGNIZER
                        startStreamingVoiceInput()
                    }
                    is VoiceModelStatus.NotInstalled -> {}
                }
            }
        }
    }

    private fun beginAudioRecording() {
        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.LISTENING,
            transcript = "",
            errorMessageResId = null,
        )

        val result = wavAudioRecorder.startRecording()
        if (result.isFailure) {
            Log.e(TAG, "Failed to start AudioRecord", result.exceptionOrNull())
            _uiState.value = _uiState.value.copy(
                sheetState = VoiceSheetState.ERROR,
                errorMessageResId = R.string.msg_voice_error_audio,
            )
            return
        }

        amplitudeObservationJob?.cancel()
        amplitudeObservationJob = viewModelScope.launch {
            wavAudioRecorder.amplitude.collect { amp ->
                _uiState.value = _uiState.value.copy(audioAmplitude = amp)
            }
        }

        // Automatic silence detection (VAD auto-stop)
        silenceJob?.cancel()
        silenceJob = viewModelScope.launch {
            var hasSpoken = false
            var lastSpeechTime = System.currentTimeMillis()
            val startTime = System.currentTimeMillis()
            // Amplitude is normalized RMS from AudioRecord (rms / 32767) captured with
            // AudioSource.VOICE_RECOGNITION, which is a quiet source: normal speech
            // lands around 0.01-0.05. The old fixed 0.05 gate therefore treated real
            // speech as silence and failed with "say something" after the no-speech
            // timeout. Track the room's noise floor and require clearly-above-noise.
            var noiseFloor = 0.002f
            val minSpeechThreshold = 0.004f
            val maxSpeechThreshold = 0.02f
            val silenceDurationMs = 1500L // 1.5s of silence after speech triggers auto-stop
            val minSpeechDurationMs = 600L // at least 600ms of recording before auto-stop
            val initialSilenceTimeoutMs = 6000L // 6s with no speech at all triggers timeout
            val maxRecordingDurationMs = 20000L // 20s safety limit

            while (isActive && _uiState.value.sheetState == VoiceSheetState.LISTENING) {
                kotlinx.coroutines.delay(100L)
                val now = System.currentTimeMillis()
                val currentAmp = _uiState.value.audioAmplitude
                val speechThreshold = (noiseFloor * 3f).coerceIn(minSpeechThreshold, maxSpeechThreshold)

                if (currentAmp > speechThreshold) {
                    hasSpoken = true
                    lastSpeechTime = now
                } else if (!hasSpoken) {
                    // Learn the room's baseline while nobody is speaking yet.
                    noiseFloor = noiseFloor * 0.9f + currentAmp * 0.1f
                }

                if (hasSpoken) {
                    val speechDuration = now - startTime
                    val silenceDuration = now - lastSpeechTime
                    if (speechDuration >= minSpeechDurationMs && silenceDuration >= silenceDurationMs) {
                        Log.d(TAG, "Silence detected ($silenceDuration ms). Auto-stopping recording.")
                        silenceJob?.cancel()
                        silenceJob = null
                        stopAndProcess()
                        break
                    }
                } else {
                    if (now - startTime >= initialSilenceTimeoutMs) {
                        Log.d(TAG, "Initial silence timeout reached.")
                        silenceJob?.cancel()
                        silenceJob = null
                        wavAudioRecorder.cancelRecording()
                        _uiState.value = _uiState.value.copy(
                            sheetState = VoiceSheetState.ERROR,
                            errorMessageResId = R.string.msg_voice_error_empty_input,
                        )
                        break
                    }
                }

                if (now - startTime >= maxRecordingDurationMs) {
                    Log.d(TAG, "Max recording duration reached (20s). Auto-stopping.")
                    silenceJob?.cancel()
                    silenceJob = null
                    stopAndProcess()
                    break
                }
            }
        }
    }

    /**
     * Stop capturing audio, transcribe with Whisper Tiny on-device, and parse transaction.
     */
    fun stopAndProcess() {
        silenceJob?.cancel()
        silenceJob = null
        amplitudeObservationJob?.cancel()
        amplitudeObservationJob = null

        if (activeEngineMode == VoiceEngineMode.STREAMING_SPEECH_RECOGNIZER) {
            val capturedTranscript = _uiState.value.transcript.trim().ifBlank {
                voiceRecognitionManager.recognizedText.value.trim()
            }
            voiceRecognitionManager.stopListening()

            if (capturedTranscript.isNotBlank()) {
                Log.d(TAG, "stopAndProcess: Processing captured transcript '$capturedTranscript'")
                onSpeechResult(capturedTranscript)
            } else {
                _uiState.value = _uiState.value.copy(
                    sheetState = VoiceSheetState.PROCESSING,
                    audioAmplitude = 0f,
                )
            }
            return
        }

        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.PROCESSING,
            audioAmplitude = 0f,
        )

        viewModelScope.launch {
            val wavFile = wavAudioRecorder.stopRecording()
            if (wavFile == null || !wavFile.exists() || wavFile.length() <= 44L) {
                _uiState.value = _uiState.value.copy(
                    sheetState = VoiceSheetState.ERROR,
                    errorMessageResId = R.string.msg_voice_error_empty_input,
                )
                return@launch
            }

            try {
                val transcriptionResult = whisperEngine.transcribe(wavFile)
                wavFile.delete()

                if (transcriptionResult.isFailure) {
                    Log.e(TAG, "Whisper transcription failed", transcriptionResult.exceptionOrNull())
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.ERROR,
                        errorMessageResId = R.string.msg_voice_error_recognizer,
                    )
                    return@launch
                }

                val transcribedText = transcriptionResult.getOrThrow()
                if (transcribedText.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.ERROR,
                        errorMessageResId = R.string.msg_voice_error_empty_input,
                    )
                    return@launch
                }

                onSpeechResult(transcribedText)
            } catch (e: Exception) {
                Log.e(TAG, "Error in stopAndProcess", e)
                wavFile.delete()
                _uiState.value = _uiState.value.copy(
                    sheetState = VoiceSheetState.ERROR,
                    errorMessageResId = R.string.msg_voice_error_recognizer,
                )
            }
        }
    }

    fun onPartialResult(text: String) {
        _uiState.value = _uiState.value.copy(transcript = text)
    }

    fun onSpeechResult(text: String) {
        val trimmed = text.trim().ifBlank { _uiState.value.transcript.trim() }
        if (trimmed.isBlank()) {
            _uiState.value = _uiState.value.copy(
                sheetState = VoiceSheetState.ERROR,
                errorMessageResId = R.string.msg_voice_error_empty_input,
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.PROCESSING,
            transcript = trimmed,
        )

        viewModelScope.launch {
            val isOnline = isInternetAvailable()
            val isPro = monetizationRepository.userTier.first() == UserTier.PREMIUM
            if (!BuildConfig.DEBUG && !aiUsageTracker.hasRemainingParses(isPro).first()) {
                _uiState.value = _uiState.value.copy(
                    sheetState = VoiceSheetState.ERROR,
                    errorMessageResId = R.string.msg_ai_daily_limit,
                )
                return@launch
            }
            if (isOnline) {
                try {
                    val ctx = userContextProvider.getUserContext()
                    val response = aiParseApiService.parseExpenseText(
                        AiParseRequest(
                            text = text,
                            locale = ctx.locale,
                            currency = ctx.currency,
                            expenseCategories = ctx.allExpenseCategories,
                            incomeCategories = ctx.allIncomeCategories,
                            paymentMethods = ctx.allPaymentMethods,
                        )
                    )
                    val items = mapCloudResponse(response.transactions)
                    aiUsageTracker.recordUsage(isPro)
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.RESULT,
                        parsedTransaction = null,
                        reviewItems = items,
                        parserType = VoiceParserType.GEMINI,
                    )
                    return@launch
                } catch (e: FirebaseFunctionsException) {
                    if (e.code == FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED) {
                        aiUsageTracker.markLimitReached(isPro)
                        _uiState.value = _uiState.value.copy(
                            sheetState = VoiceSheetState.ERROR,
                            errorMessageResId = R.string.msg_ai_daily_limit,
                        )
                        return@launch
                    }
                    Log.e(TAG, "cloud parse failed, using offline", e)
                } catch (e: Exception) {
                    Log.e(TAG, "cloud parse failed, using offline", e)
                }
            }

            val offlineResult = offlineParser.parse(text)
            when (offlineResult) {
                is VoiceParseResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.RESULT,
                        parsedTransaction = offlineResult.transaction,
                        reviewItems = listOf(fromOffline(offlineResult.transaction)),
                        parserType = VoiceParserType.OFFLINE,
                    )
                }
                is VoiceParseResult.Failed -> {
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.ERROR,
                        errorMessageResId = offlineResult.errorMessageResId,
                    )
                }
            }
        }
    }

    private suspend fun mapCloudResponse(
        rows: List<com.mknlabs.expensetracker.ai.cloud.AiParsedTransaction>,
    ): List<AiParsedReviewItem> {
        val categories = categoryDao.getActiveCategories()
        val payments = paymentMethodDao.getActivePaymentMethods()
        val defaultPaymentId = payments.firstOrNull { it.id == DEFAULT_PAYMENT_TYPE_ID }?.id
            ?: payments.firstOrNull()?.id
            ?: DEFAULT_PAYMENT_TYPE_ID
        return rows.map { row ->
            val category = categories.firstOrNull {
                it.transactionTypeId == row.typeId && it.name.equals(row.category, ignoreCase = true)
            }
            val payment = payments.firstOrNull {
                it.name.equals(row.paymentMethod, ignoreCase = true)
            }
            AiParsedReviewItem(
                id = UUID.randomUUID().toString(),
                amount = row.amount,
                amountMinor = row.amount.toMinorUnits(),
                categoryName = category?.name ?: row.category,
                categoryId = category?.id ?: 0,
                paymentName = payment?.name ?: row.paymentMethod,
                paymentTypeId = payment?.id ?: defaultPaymentId,
                note = row.note,
                typeId = row.typeId,
                createdAt = row.createdAt ?: System.currentTimeMillis(),
                fundId = null,
                fundName = "",
                tagIds = emptyList(),
            )
        }
    }

    private fun fromOffline(parsed: ParsedVoiceTransaction): AiParsedReviewItem {
        return AiParsedReviewItem(
            id = UUID.randomUUID().toString(),
            amount = parsed.amountMinor.toMajorUnits(),
            amountMinor = parsed.amountMinor,
            categoryName = "",
            categoryId = parsed.categoryId,
            paymentName = "",
            paymentTypeId = parsed.paymentTypeId ?: DEFAULT_PAYMENT_TYPE_ID,
            note = parsed.note,
            typeId = parsed.transactionTypeId,
            createdAt = System.currentTimeMillis(),
            fundId = null,
            fundName = "",
            tagIds = emptyList(),
        )
    }

    private fun isInternetAvailable(): Boolean {
        val app = getApplication<Application>()
        val connectivityManager =
            app.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun onRecognizerError(errorResId: Int) {
        wavAudioRecorder.cancelRecording()
        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.ERROR,
            errorMessageResId = errorResId,
        )
    }

    fun resetToListening() {
        startVoiceInput()
    }

    fun cancelVoiceInput() {
        silenceJob?.cancel()
        silenceJob = null
        amplitudeObservationJob?.cancel()
        amplitudeObservationJob = null
        modelObservationJob?.cancel()
        modelObservationJob = null
        wavAudioRecorder.cancelRecording()
        whisperEngine.release()
        voiceRecognitionManager.cancel()
        _uiState.value = VoiceInputUiState()
    }

    fun dismiss() {
        cancelVoiceInput()
    }

    override fun onCleared() {
        super.onCleared()
        cancelVoiceInput()
    }
}
