package com.mknlabs.expensetracker.ui.viewmodels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.AiUsageTracker
import com.mknlabs.expensetracker.ai.aiUsageDataStore
import com.mknlabs.expensetracker.ai.cloud.FirebaseGeminiParser
import com.mknlabs.expensetracker.ai.offline.OfflineVoiceParser
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserType
import com.mknlabs.expensetracker.ui.components.VoiceSheetState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the voice input sheet.
 */
data class VoiceInputUiState(
    val sheetState: VoiceSheetState = VoiceSheetState.LISTENING,
    val transcript: String = "",
    val parsedTransaction: ParsedVoiceTransaction? = null,
    val errorMessageResId: Int? = null,
    /** Which parser produced the result (null if not yet parsed). */
    val parserType: VoiceParserType? = null,
    /** Remaining AI parses today (null if not checked). */
    val remainingAiParses: Int? = null
)

/**
 * ViewModel for voice transaction input.
 *
 * Manages the UI state machine: LISTENING → PROCESSING → RESULT / ERROR.
 * The actual SpeechRecognizer is managed in the Composable layer (needs
 * Android lifecycle), and recognized text is passed here for parsing.
 *
 * Parser selection logic:
 * 1. Check internet availability
 * 2. If online AND within daily limit → use Gemini AI parser
 * 3. If offline OR limit reached → use offline parser
 *
 * Follows GEMINI.md: @HiltViewModel, StateFlow, no hardcoded strings.
 */
@HiltViewModel
class VoiceAddViewModel @Inject constructor(
    application: Application,
    private val offlineParser: OfflineVoiceParser,
    private val firebaseGeminiParser: FirebaseGeminiParser
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "VoiceAddViewModel"
    }

    /** Lazily created — avoids Hilt DataStore injection issues. */
    private val aiUsageTracker by lazy {
        val dataStore = getApplication<Application>()
            .aiUsageDataStore
        AiUsageTracker(dataStore)
    }

    private val _uiState = MutableStateFlow(VoiceInputUiState())
    val uiState: StateFlow<VoiceInputUiState> = _uiState.asStateFlow()

    /**
     * Called when speech recognition produces partial results.
     * Updates the live transcript in the UI.
     */
    fun onPartialResult(text: String) {
        Log.d(TAG, "onPartialResult: '$text'")
        _uiState.value = _uiState.value.copy(transcript = text)
    }

    /**
     * Called when speech recognition completes with final text.
     * Transitions to PROCESSING, runs the parser, then to RESULT or ERROR.
     */
    fun onSpeechResult(text: String) {
        Log.d(TAG, "onSpeechResult: text='$text'")
        if (text.isBlank()) {
            Log.w(TAG, "onSpeechResult: blank text → ERROR")
            _uiState.value = _uiState.value.copy(
                sheetState = VoiceSheetState.ERROR,
                errorMessageResId = R.string.msg_voice_error_empty_input
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.PROCESSING,
            transcript = text
        )
        Log.d(TAG, "onSpeechResult: state → PROCESSING, launching parse coroutine")

        viewModelScope.launch {
            val isOnline = isInternetAvailable()
            val hasRemainingAiParses = aiUsageTracker.hasRemainingParses.first()
            Log.d(TAG, "onSpeechResult: isOnline=$isOnline, hasRemainingAiParses=$hasRemainingAiParses")

            // Decision: use Gemini AI only if online AND within daily limit
            if (isOnline && hasRemainingAiParses) {
                Log.d(TAG, "onSpeechResult: attempting Gemini AI parse...")
                // Try Gemini AI parser via Firebase AI Logic
                try {
                    val geminiResult = firebaseGeminiParser.parseAsync(text)
                    Log.d(TAG, "onSpeechResult: Gemini result=$geminiResult")
                    if (geminiResult is VoiceParseResult.Success) {
                        // Record usage
                        aiUsageTracker.recordUsage()
                        val remaining = aiUsageTracker.remainingParses.first()
                        Log.d(TAG, "onSpeechResult: Gemini SUCCESS — amount=${geminiResult.transaction.amountMinor}, remainingAiParses=$remaining")
                        _uiState.value = _uiState.value.copy(
                            sheetState = VoiceSheetState.RESULT,
                            parsedTransaction = geminiResult.transaction,
                            parserType = VoiceParserType.GEMINI,
                            remainingAiParses = remaining
                        )
                        return@launch
                    } else {
                        Log.w(TAG, "onSpeechResult: Gemini returned Failed — falling through to offline parser")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onSpeechResult: Gemini exception — falling through to offline parser", e)
                }
            } else {
                Log.d(TAG, "onSpeechResult: skipping Gemini (online=$isOnline, remaining=$hasRemainingAiParses) — using offline parser")
            }

            // Fallback: use offline parser (no internet OR limit reached OR Gemini failed)
            Log.d(TAG, "onSpeechResult: running offline parser...")
            val offlineResult = offlineParser.parse(text)
            val remaining = aiUsageTracker.remainingParses.first()
            Log.d(TAG, "onSpeechResult: offline result=$offlineResult, remainingAiParses=$remaining")

            when (offlineResult) {
                is VoiceParseResult.Success -> {
                    Log.d(TAG, "onSpeechResult: offline SUCCESS — amount=${offlineResult.transaction.amountMinor}")
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.RESULT,
                        parsedTransaction = offlineResult.transaction,
                        parserType = VoiceParserType.OFFLINE,
                        remainingAiParses = remaining
                    )
                }
                is VoiceParseResult.Failed -> {
                    Log.e(TAG, "onSpeechResult: offline FAILED — errorResId=${offlineResult.errorMessageResId}")
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.ERROR,
                        errorMessageResId = offlineResult.errorMessageResId
                    )
                }
            }
        }
    }

    /**
     * Checks if the device has an active internet connection.
     */
    private fun isInternetAvailable(): Boolean {
        val app = getApplication<Application>()
        val connectivityManager = app.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        Log.d(TAG, "isInternetAvailable: $hasInternet")
        return hasInternet
    }

    /**
     * Called when an error occurs in the SpeechRecognizer itself
     * (e.g., no permission, network error, audio error).
     */
    fun onRecognizerError(errorResId: Int) {
        Log.e(TAG, "onRecognizerError: errorResId=$errorResId")
        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.ERROR,
            errorMessageResId = errorResId
        )
    }

    /**
     * Reset to listening state for retry.
     */
    fun resetToListening() {
        Log.d(TAG, "resetToListening: state → LISTENING")
        _uiState.value = VoiceInputUiState(
            sheetState = VoiceSheetState.LISTENING
        )
    }

    /**
     * Dismiss the sheet entirely.
     */
    fun dismiss() {
        Log.d(TAG, "dismiss")
        _uiState.value = VoiceInputUiState()
    }
}
