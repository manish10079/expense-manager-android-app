package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.FundRepository
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.Fund
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Funds available on Add/Edit Transaction, plus the fund currently linked to the draft.
 *
 * Selection lives here so edit can reseed from the database even when the navigation copy
 * of the transaction omitted fundId, and so creating a fund immediately attaches it.
 */
@HiltViewModel
class AddTransactionFundsViewModel @Inject constructor(
    private val fundRepository: FundRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val catalogue: StateFlow<List<Fund>> = fundRepository.observeActiveFunds()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val extraFund = MutableStateFlow<Fund?>(null)

    val funds: StateFlow<List<Fund>> = combine(catalogue, extraFund) { list, extra ->
        if (extra == null || list.any { it.id == extra.id }) list else list + extra
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedFundId = MutableStateFlow<String?>(null)
    val selectedFundId: StateFlow<String?> = _selectedFundId.asStateFlow()

    private var seededForTxnId: String? = null

    fun seedFromTransaction(transactionId: String, fundIdHint: String?) {
        if (transactionId.isBlank() || seededForTxnId == transactionId) return
        seededForTxnId = transactionId
        _selectedFundId.value = fundIdHint
        viewModelScope.launch {
            val fromDb = transactionRepository.getTransactionById(transactionId)?.fundId
            if (seededForTxnId == transactionId && fromDb != null) {
                _selectedFundId.value = fromDb
            }
            val id = _selectedFundId.value ?: return@launch
            if (catalogue.value.none { it.id == id }) {
                extraFund.value = fundRepository.getFundById(id)
            }
        }
    }

    fun prepareNewDraft() {
        seededForTxnId = ""
        extraFund.value = null
        _selectedFundId.value = null
    }

    fun selectFund(fundId: String?) {
        seededForTxnId = seededForTxnId ?: ""
        _selectedFundId.value = fundId
    }

    fun createFund(
        name: String,
        amountMinor: Long,
        startDate: Long,
        iconKey: String,
        colorHex: String,
        note: String,
        incomeCategoryId: Int = com.mknlabs.expensetracker.domain.repository.FundDefaults.INCOME_CATEGORY_ID
    ) {
        val id = UUID.randomUUID().toString()
        val fund = Fund(
            id = id,
            name = name.trim(),
            amountMinor = amountMinor,
            startDate = startDate,
            iconKey = iconKey,
            colorHex = colorHex,
            note = note.trim()
        )
        extraFund.value = fund
        selectFund(id)
        viewModelScope.launch {
            fundRepository.createFund(
                fund = fund,
                incomeCategoryId = incomeCategoryId
            )
        }
    }
}
