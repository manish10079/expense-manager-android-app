package com.mknlabs.expensetracker.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.core.ui.components.reviewCategoryId
import com.mknlabs.expensetracker.core.ui.components.reviewPaymentId
import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
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
            val catId = if (item.categoryId > 0) item.categoryId else reviewCategoryId(item).let { if (it > 0) it else 20 }
            val paymentId = if (item.paymentTypeId > 0) item.paymentTypeId else reviewPaymentId(item).let { if (it > 0) it else DEFAULT_PAYMENT_TYPE_ID }
            transactionRepository.upsertTransaction(
                Transaction(
                    id = UUID.randomUUID().toString(),
                    note = item.note,
                    createdAt = item.createdAt ?: now,
                    amountMinor = item.amountMinor,
                    transactionTypeId = item.typeId,
                    paymentTypeId = paymentId,
                    categoryId = catId,
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

    fun saveAll(
        items: List<AiParsedReviewItem>,
        fingerprint: String,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            items.forEach { item ->
                val catId = if (item.categoryId > 0) item.categoryId else reviewCategoryId(item).let { if (it > 0) it else 20 }
                val paymentId = if (item.paymentTypeId > 0) item.paymentTypeId else reviewPaymentId(item).let { if (it > 0) it else DEFAULT_PAYMENT_TYPE_ID }
                transactionRepository.upsertTransaction(
                    Transaction(
                        id = UUID.randomUUID().toString(),
                        note = item.note,
                        createdAt = item.createdAt ?: now,
                        amountMinor = item.amountMinor,
                        transactionTypeId = item.typeId,
                        paymentTypeId = paymentId,
                        categoryId = catId,
                        contentHash = null,
                        syncState = SyncState.PENDING_UPLOAD,
                        isDeleted = false,
                        updatedAt = now,
                        sourceRecurringRuleId = null,
                        fundId = null,
                    )
                )
            }
            if (fingerprint.isNotBlank()) {
                ocrReceiptDeduper.rememberSaved(fingerprint)
            }
            onDone()
        }
    }
}
