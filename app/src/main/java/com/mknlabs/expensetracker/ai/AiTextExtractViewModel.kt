package com.mknlabs.expensetracker.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.ai.cloud.AiParseApiService
import com.mknlabs.expensetracker.ai.cloud.AiParseRequest
import com.mknlabs.expensetracker.ai.cloud.AiParseResponse
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.ai.cloud.UserContextProvider
import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.utils.toMinorUnits
import android.util.Log
import com.google.firebase.functions.FirebaseFunctionsException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiTextExtractViewModel @Inject constructor(
    private val aiParseApiService: AiParseApiService,
    private val userContextProvider: UserContextProvider,
    private val categoryDao: CategoryDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val aiUsageTracker: AiUsageTracker,
    private val monetizationRepository: MonetizationRepository,
    private val onDeviceTextExtractor: OnDeviceTextExtractor,
    private val ocrReceiptDeduper: OcrReceiptDeduper,
) : ViewModel() {

    companion object {
        private const val TAG = "AiTextExtractVM"
    }

    fun parseFromDocument(
        uri: android.net.Uri,
        onResult: (DocumentParseOutcome) -> Unit,
    ) {
        viewModelScope.launch {
            when (val ocr = onDeviceTextExtractor.extract(uri)) {
                DocumentOcrResult.PlayServicesUnavailable ->
                    onResult(DocumentParseOutcome.PlayServicesUnavailable)
                is DocumentOcrResult.Text -> {
                    val text = ocr.value
                    Log.i(TAG, "OCR sending to AI:\n$text")
                    if (text.isBlank()) {
                        onResult(DocumentParseOutcome.RawAdd(""))
                    } else {
                        val fingerprint = OcrReceiptFingerprint.of(text)
                        if (ocrReceiptDeduper.wasSavedRecently(fingerprint)) {
                            onResult(DocumentParseOutcome.Duplicate)
                        } else {
                            parse(text) { outcome ->
                                onResult(
                                    when (outcome) {
                                        is TextParseOutcome.Review ->
                                            DocumentParseOutcome.Review(text, outcome.items, fingerprint)
                                        TextParseOutcome.RawAdd ->
                                            DocumentParseOutcome.RawAdd(text, fingerprint)
                                        TextParseOutcome.DailyLimit -> DocumentParseOutcome.DailyLimit
                                        TextParseOutcome.Failed ->
                                            if (text.isNotBlank()) {
                                                DocumentParseOutcome.RawAdd(text, fingerprint)
                                            } else {
                                                DocumentParseOutcome.Failed
                                            }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun parse(
        text: String,
        onResult: (TextParseOutcome) -> Unit,
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            onResult(TextParseOutcome.RawAdd)
            return
        }
        viewModelScope.launch {
            val isPro = monetizationRepository.userTier.first() == UserTier.PREMIUM
            if (!BuildConfig.DEBUG && !aiUsageTracker.hasRemainingParses(isPro).first()) {
                onResult(TextParseOutcome.DailyLimit)
                return@launch
            }
            val outcome = runCatching {
                val ctx = userContextProvider.getUserContext()
                val response = aiParseApiService.parseExpenseText(
                    AiParseRequest(
                        text = trimmed,
                        locale = ctx.locale,
                        currency = ctx.currency,
                        expenseCategories = ctx.allExpenseCategories,
                        incomeCategories = ctx.allIncomeCategories,
                        paymentMethods = ctx.allPaymentMethods,
                        skipQuota = BuildConfig.DEBUG,
                    )
                )
                val mapped = mapResponse(response)
                if (!BuildConfig.DEBUG) {
                    aiUsageTracker.recordUsage(isPro)
                }
                if (mapped.isEmpty()) TextParseOutcome.RawAdd else TextParseOutcome.Review(mapped)
            }.getOrElse { error ->
                Log.e(TAG, "parseExpenseText failed", error)
                if (error is FirebaseFunctionsException &&
                    error.code == FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED
                ) {
                    TextParseOutcome.DailyLimit
                } else {
                    TextParseOutcome.Failed
                }
            }
            onResult(outcome)
        }
    }

    fun rememberOcrSaved(fingerprint: String) {
        if (fingerprint.isBlank()) return
        viewModelScope.launch {
            ocrReceiptDeduper.rememberSaved(fingerprint)
        }
    }

    private suspend fun mapResponse(response: AiParseResponse): List<AiParsedReviewItem> {
        val categories = categoryDao.getActiveCategories()
        val payments = paymentMethodDao.getActivePaymentMethods()
        val defaultPaymentId = payments.firstOrNull { it.id == DEFAULT_PAYMENT_TYPE_ID }?.id
            ?: payments.firstOrNull()?.id
            ?: DEFAULT_PAYMENT_TYPE_ID
        return response.transactions.map { row ->
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
                createdAt = row.createdAt,
            )
        }
    }
}
