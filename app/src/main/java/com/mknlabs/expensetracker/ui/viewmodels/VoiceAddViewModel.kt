package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.AiUsageTracker
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import com.mknlabs.expensetracker.domain.repository.VoiceParserType
import com.mknlabs.expensetracker.ui.components.VoiceSheetState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

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
 * Follows GEMINI.md: @HiltViewModel, StateFlow, no hardcoded strings.
 */
@HiltViewModel
class VoiceAddViewModel @Inject constructor(
    @Named("offline") private val offlineParser: VoiceParserRepository,
    private val aiUsageTracker: AiUsageTracker
) : ViewModel() {

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
            // 1. Always try offline parser first (fast, free)
            val offlineResult = offlineParser.parse(text)

            if (offlineResult is VoiceParseResult.Success &&
                offlineResult.transaction.confidence != com.mknlabs.expensetracker.domain.models.VoiceConfidence.LOW
            ) {
                // Offline parser gave a good result — use it
                val remaining = aiUsageTracker.remainingParses.first()
                _uiState.value = _uiState.value.copy(
                    sheetState = VoiceSheetState.RESULT,
                    parsedTransaction = offlineResult.transaction,
                    parserType = VoiceParserType.OFFLINE,
                    remainingAiParses = remaining
                )
                return@launch
            }

            // 2. Offline result is LOW confidence — try Gemini if within limit
            val hasRemaining = aiUsageTracker.hasRemainingParses.first()
            if (!hasRemaining) {
                // Limit reached — use offline result anyway
                val remaining = aiUsageTracker.remainingParses.first()
                _uiState.value = _uiState.value.copy(
                    sheetState = VoiceSheetState.RESULT,
                    parsedTransaction = offlineResult.transactionOrFallback(),
                    parserType = VoiceParserType.OFFLINE,
                    remainingAiParses = remaining
                )
                return@launch
            }

            // 3. Try Gemini AI parser
            try {
                val geminiParser = tryGeminiParser()
                if (geminiParser != null) {
                    val geminiResult = geminiParser.parse(text)
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
                }
            } catch (_: Exception) {
                // Gemini failed — fall through to offline result
            }

            // 4. Fallback to offline result
            val remaining = aiUsageTracker.remainingParses.first()
            _uiState.value = _uiState.value.copy(
                sheetState = VoiceSheetState.RESULT,
                parsedTransaction = offlineResult.transactionOrFallback(),
                parserType = VoiceParserType.OFFLINE,
                remainingAiParses = remaining
            )
        }
    }

    /**
     * Attempts to get the Gemini parser via Hilt injection.
     * Returns null if not available (e.g., not injected).
     */
    private fun tryGeminiParser(): VoiceParserRepository? {
        return try {
            val clazz = Class.forName("com.mknlabs.expensetracker.ai.cloud.GeminiVoiceParser")
            val constructor = clazz.constructors.first()
            val apiServiceClass = Class.forName("com.mknlabs.expensetracker.ai.cloud.GeminiApiService")
            val functionsClass = Class.forName("com.google.firebase.functions.FirebaseFunctions")
            val functions = functionsClass.getMethod("getInstance").invoke(null)
            val apiService = apiServiceClass.constructors.first().newInstance(functions)
            constructor.newInstance(apiService) as? VoiceParserRepository
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extracts the transaction from a Success result, or creates a fallback.
     */
    private fun VoiceParseResult.transactionOrFallback(): ParsedVoiceTransaction {
        return when (this) {
            is VoiceParseResult.Success -> transaction
            is VoiceParseResult.Failed -> ParsedVoiceTransaction(
                amountMinor = 0,
                transactionTypeId = 2,
                categoryId = 23,
                note = _uiState.value.transcript.trim(),
                confidence = com.mknlabs.expensetracker.domain.models.VoiceConfidence.LOW
            )
        }
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
