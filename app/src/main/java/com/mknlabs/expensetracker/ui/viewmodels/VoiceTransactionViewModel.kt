package com.mknlabs.expensetracker.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.utils.AppShortcutManager
import com.mknlabs.expensetracker.voice.VoiceRecognitionManager
import com.mknlabs.expensetracker.voice.VoiceTransactionParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for managing voice-based transaction entry.
 */
@HiltViewModel
class VoiceTransactionViewModel @Inject constructor(
    private val application: Application,
    private val transactionRepository: TransactionRepository,
    private val voiceRecognitionManager: VoiceRecognitionManager,
    private val voiceTransactionParser: VoiceTransactionParser,
    private val appShortcutManager: AppShortcutManager
) : ViewModel() {

    data class UiState(
        val isListening: Boolean = false,
        val recognizedText: String = "",
        val parsedTransaction: VoiceTransactionParser.ParsedTransaction? = null,
        val showConfirmationSheet: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,
        val saveSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Observe voice recognition state
        viewModelScope.launch {
            voiceRecognitionManager.isListening.collect { isListening ->
                _uiState.update { it.copy(isListening = isListening) }
            }
        }

        viewModelScope.launch {
            voiceRecognitionManager.recognizedText.collect { text ->
                if (text.isNotBlank()) {
                    _uiState.update { it.copy(recognizedText = text) }
                    parseVoiceInput(text)
                }
            }
        }

        viewModelScope.launch {
            voiceRecognitionManager.error.collect { error ->
                if (error != null) {
                    _uiState.update { it.copy(error = error) }
                }
            }
        }
    }

    /**
     * Start listening for voice input.
     */
    fun startListening() {
        _uiState.update { it.copy(error = null, recognizedText = "", parsedTransaction = null) }
        voiceRecognitionManager.startListening(application)
    }

    /**
     * Stop listening.
     */
    fun stopListening() {
        voiceRecognitionManager.stopListening()
    }

    /**
     * Parse voice input into transaction data.
     */
    private fun parseVoiceInput(text: String) {
        val parsed = voiceTransactionParser.parse(text)
        if (parsed != null) {
            _uiState.update {
                it.copy(
                    parsedTransaction = parsed,
                    showConfirmationSheet = true
                )
            }
        } else {
            _uiState.update {
                it.copy(error = "Could not parse transaction from voice input. Please try again.")
            }
        }
    }

    /**
     * Confirm and save the parsed transaction.
     */
    fun confirmTransaction(transaction: VoiceTransactionParser.ParsedTransaction) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val now = System.currentTimeMillis()
                val newTransaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    note = transaction.note,
                    createdAt = now,
                    amountMinor = (transaction.amount * 100).toLong(),
                    transactionTypeId = if (transaction.isExpense) 0 else 1,
                    paymentTypeId = 1, // Default payment method
                    categoryId = transaction.categoryId,
                    contentHash = null,
                    syncState = com.mknlabs.expensetracker.models.SyncState.PENDING_UPLOAD,
                    isDeleted = false,
                    updatedAt = now,
                    sourceRecurringRuleId = null
                )

                transactionRepository.upsertTransaction(newTransaction)

                // Refresh dynamic shortcuts after adding transaction
                appShortcutManager.refreshDynamicShortcuts(application)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        showConfirmationSheet = false,
                        parsedTransaction = null,
                        recognizedText = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Failed to save transaction. Please try again."
                    )
                }
            }
        }
    }

    /**
     * Dismiss the confirmation sheet.
     */
    fun dismissConfirmation() {
        _uiState.update {
            it.copy(
                showConfirmationSheet = false,
                parsedTransaction = null,
                recognizedText = ""
            )
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear save success state.
     */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognitionManager.destroy()
    }
}
