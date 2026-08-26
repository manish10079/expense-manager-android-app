package com.mknlabs.expensetracker.ui.viewmodels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
        _uiState.value = _uiState.value.copy(transcript = text)
    }

    /**
     * Called when speech recognition completes with final text.
     * Transitions to PROCESSING, runs the parser, then to RESULT or ERROR.
     */
    fun onSpeechResult(text: String) {
        if (text.isBlank()) {
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

        viewModelScope.launch {
            val isOnline = isInternetAvailable()
            val hasRemainingAiParses = aiUsageTracker.hasRemainingParses.first()

            // Decision: use Gemini AI only if online AND within daily limit
            if (isOnline && hasRemainingAiParses) {
                // Try Gemini AI parser via Firebase AI Logic
                try {
                    val geminiResult = firebaseGeminiParser.parse(text)
                    if (geminiResult is VoiceParseResult.Success) {
                        // Record usage
                        aiUsageTracker.recordUsage()
                        val remaining = aiUsageTracker.remainingParses.first()
                        _uiState.value = _uiState.value.copy(
                            sheetState = VoiceSheetState.RESULT,
                            parsedTransaction = geminiResult.transaction,
                            parserType = VoiceParserType.GEMINI,
                            remainingAiParses = remaining
                        )
                        return@launch
                    }
                } catch (_: Exception) {
                    // Gemini failed — fall through to offline parser
                }
            }

            // Fallback: use offline parser (no internet OR limit reached OR Gemini failed)
            val offlineResult = offlineParser.parse(text)
            val remaining = aiUsageTracker.remainingParses.first()

            when (offlineResult) {
                is VoiceParseResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        sheetState = VoiceSheetState.RESULT,
                        parsedTransaction = offlineResult.transaction,
                        parserType = VoiceParserType.OFFLINE,
                        remainingAiParses = remaining
                    )
                }
                is VoiceParseResult.Failed -> {
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
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Called when an error occurs in the SpeechRecognizer itself
     * (e.g., no permission, network error, audio error).
     */
    fun onRecognizerError(errorResId: Int) {
        _uiState.value = _uiState.value.copy(
            sheetState = VoiceSheetState.ERROR,
            errorMessageResId = errorResId
        )
    }

    /**
     * Reset to listening state for retry.
     */
    fun resetToListening() {
        _uiState.value = VoiceInputUiState(
            sheetState = VoiceSheetState.LISTENING
        )
    }

    /**
     * Dismiss the sheet entirely.
     */
    fun dismiss() {
        _uiState.value = VoiceInputUiState()
    }
}
