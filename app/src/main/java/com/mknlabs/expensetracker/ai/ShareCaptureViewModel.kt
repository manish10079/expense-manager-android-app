package com.mknlabs.expensetracker.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShareCaptureViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ocrReceiptDeduper: OcrReceiptDeduper,
) : ViewModel() {

    fun save(
        item: AiParsedReviewItem,
        fingerprint: String,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            transactionRepository.upsertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    note = item.note,
                    createdAt = item.createdAt ?: now,
                    amountMinor = item.amountMinor,
                    transactionTypeId = item.typeId,
                    paymentTypeId = item.paymentTypeId,
                    categoryId = item.categoryId,
                    contentHash = null,
                    syncState = SyncState.PENDING_UPLOAD,
                    isDeleted = false,
                    updatedAt = now,
                    sourceRecurringRuleId = null,
                    fundId = null,
                )
            )
            if (fingerprint.isNotBlank()) {
                ocrReceiptDeduper.rememberSaved(fingerprint)
            }
            onDone()
        }
    }
}
