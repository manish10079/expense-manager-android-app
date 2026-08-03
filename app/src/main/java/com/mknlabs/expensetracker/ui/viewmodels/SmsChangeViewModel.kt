package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.data.local.SmsLearningStore
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.sms.ParsedSms
import com.mknlabs.expensetracker.sms.SmsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for the Smart SMS Import "Change" bottom sheet (plan §8 / Phase 4).
 *
 * The sheet lets the user override the detected category and add an optional
 * note before saving — without going through the full Add Transaction flow.
 * The payload is ephemeral ([ParsedSms] lives only in [uiState]; it is never
 * persisted, plan D2).
 */
data class SmsChangeUiState(
    val parsed: ParsedSms? = null,
    val selectedCategoryId: Int = 0,
    val note: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val saveError: Boolean = false
)

@HiltViewModel
class SmsChangeViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val smsLearningStore: SmsLearningStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmsChangeUiState())
    val uiState: StateFlow<SmsChangeUiState> = _uiState.asStateFlow()

    /**
     * Starts a fresh sheet session for [parsed]. Resets any previous selection,
     * note, and saved/error flags — the suggested category becomes the default.
     */
    fun load(parsed: ParsedSms) {
        _uiState.value = SmsChangeUiState(
            parsed = parsed,
            selectedCategoryId = parsed.categoryId
        )
    }

    fun onCategorySelected(categoryId: Int) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note.take(NOTE_MAX_LENGTH)) }
    }

    /**
     * Saves with the selected category + note through [SmsRepository.saveFromSms]
     * (which routes through the single `upsertTransaction` path, so Room flows
     * refresh every screen automatically). Payment method = the user's configured
     * default (plan §14 Q1), matching the one-tap Save receiver.
     *
     * Dedup is re-checked (defense-in-depth): if the transaction was already
     * imported while the sheet was open, nothing is written but the sheet still
     * closes as "saved".
     */
    fun save() {
        val state = _uiState.value
        val parsed = state.parsed ?: return
        if (state.isSaving || state.isSaved) return

        _uiState.update { it.copy(isSaving = true, saveError = false) }
        viewModelScope.launch {
            try {
                if (!smsRepository.isDuplicate(parsed)) {
                    val paymentTypeId = appPreferencesRepository
                        .observeAppSettings()
                        .first()
                        .defaultPaymentTypeId
                    smsRepository.saveFromSms(
                        parsed = parsed,
                        note = state.note.trim(),
                        categoryId = state.selectedCategoryId,
                        paymentTypeId = paymentTypeId
                    )
                    recordLearningOverride(parsed, state.selectedCategoryId)
                }
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                // Keep the sheet open so the user can retry.
                _uiState.update { it.copy(isSaving = false, saveError = true) }
            }
        }
    }

    fun consumeSaveError() {
        _uiState.update { it.copy(saveError = false) }
    }

    /**
     * Learning system (plan §10): when the user explicitly corrects a detected
     * category (and a merchant was detected), remember the mapping so the next
     * identical SMS is suggested with the user's choice. Best-effort — a failed
     * write must never fail the save itself.
     */
    private suspend fun recordLearningOverride(parsed: ParsedSms, selectedCategoryId: Int) {
        if (selectedCategoryId == parsed.categoryId) return
        val merchant = parsed.merchant ?: return
        try {
            smsLearningStore.setOverride(merchant, selectedCategoryId)
        } catch (e: Exception) {
            // Best-effort: a failed learning write must never fail the save.
            android.util.Log.w("SmsChangeViewModel", "Failed to record merchant override", e)
        }
    }

    private companion object {
        const val NOTE_MAX_LENGTH = 200
    }
}
