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
import com.mknlabs.expensetracker.utils.toMinorUnits
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
) : ViewModel() {

    fun parse(
        text: String,
        onResult: (List<AiParsedReviewItem>) -> Unit,
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            onResult(emptyList())
            return
        }
        viewModelScope.launch {
            val items = runCatching {
                val isPro = monetizationRepository.userTier.first() == UserTier.PREMIUM
                if (!aiUsageTracker.hasRemainingParses(isPro).first()) return@runCatching emptyList()
                val ctx = userContextProvider.buildContext()
                val response = aiParseApiService.parseExpenseText(
                    AiParseRequest(
                        text = trimmed,
                        locale = ctx.locale,
                        currency = ctx.currency,
                        expenseCategories = ctx.allExpenseCategories,
                        incomeCategories = ctx.allIncomeCategories,
                        paymentMethods = ctx.allPaymentMethods,
                    )
                )
                val mapped = mapResponse(response)
                if (mapped.isNotEmpty()) aiUsageTracker.recordUsage(isPro)
                mapped
            }.getOrDefault(emptyList())
            onResult(items)
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
            )
        }
    }
}
