package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction
import com.mknlabs.expensetracker.domain.repository.VoiceParseResult
import com.mknlabs.expensetracker.domain.repository.VoiceParserRepository
import com.mknlabs.expensetracker.ui.components.VoiceSheetState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * UI state for the voice input sheet.
 */
data class VoiceInputUiState(
    val sheetState: VoiceSheetState = VoiceSheetState.LISTENING,
    val transcript: String = "",
    val parsedTransaction: ParsedVoiceTransaction? = null,
    val errorMessageResId: Int? = null
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
    private val voiceParserRepository: VoiceParserRepository
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

        // Parse synchronously (rule-based, fast)
        when (val result = voiceParserRepository.parse(text)) {
            is VoiceParseResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    sheetState = VoiceSheetState.RESULT,
                    parsedTransaction = result.transaction
                )
            }
            is VoiceParseResult.Failed -> {
                _uiState.value = _uiState.value.copy(
                    sheetState = VoiceSheetState.ERROR,
                    errorMessageResId = result.errorMessageResId
                )
            }
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
