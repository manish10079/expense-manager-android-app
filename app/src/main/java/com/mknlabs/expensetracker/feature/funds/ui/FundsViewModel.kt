package com.mknlabs.expensetracker.feature.funds.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.FundRepository
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.FundStatus
import com.mknlabs.expensetracker.models.FundWithProgress
import com.mknlabs.expensetracker.models.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/** A transaction that puts money in. The row that fills a fund carries this type. */
private const val INCOME_TRANSACTION_TYPE_ID = 1

/** One category's share of what a single fund has spent, for the fund detail breakdown. */
@Immutable
data class FundCategorySlice(
    val categoryId: Int,
    val label: String,
    val amountMinor: Long,
    val fraction: Float,
    val colorHex: String? = null
)

/** The per-fund breakdown: what the money went on. */
@Immutable
data class FundBreakdown(
    val slices: List<FundCategorySlice> = emptyList(),
    val totalSpentMinor: Long = 0L
) {
    val isEmpty: Boolean
        get() = slices.isEmpty()
}

@Immutable
data class FundsUiState(
    val funds: List<FundWithProgress> = emptyList(),
    val selectedFund: FundWithProgress? = null,
    val selectedTransactions: List<Transaction> = emptyList(),
    val selectedBreakdown: FundBreakdown = FundBreakdown(),
    val totalAllocatedMinor: Long = 0L,
    val totalSpentMinor: Long = 0L
) {
    val totalRemainingMinor: Long
        get() = (totalAllocatedMinor - totalSpentMinor).coerceAtLeast(0L)
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FundsViewModel @Inject constructor(
    private val fundRepository: FundRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val selectedFundId = MutableStateFlow<String?>(null)

    /**
     * Live spending linked to the fund currently open, or nothing when none is open.
     *
     * The income row that filled the bucket is booked against the fund too, so it comes back
     * from the same query — but it is the fund being funded, not something the fund paid for.
     * Dropping it here is what keeps the sheet's list a list of what the money went on, and
     * leaves the arrival visible where it belongs, in the fund's allocated amount. The
     * breakdown below filters the same way.
     */
    private val selectedTransactions: Flow<List<Transaction>> =
        selectedFundId.flatMapLatest { id ->
            if (id.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                fundRepository.observeTransactionsByFund(id).map { linked ->
                    linked.filter { it.transactionTypeId != INCOME_TRANSACTION_TYPE_ID }
                }
            }
        }

    val uiState: StateFlow<FundsUiState> = combine(
        fundRepository.observeFundProgress(),
        categoryRepository.observeActiveCategories(),
        selectedFundId,
        selectedTransactions
    ) { funds, categories, openId, transactions ->
        val selected = funds.firstOrNull { it.fund.id == openId }
        val breakdown = if (selected != null) {
            buildBreakdown(transactions, categories, selected.fund)
        } else {
            FundBreakdown()
        }

        // Archived money is not "in play", so the summary counts only the buckets still
        // being spent from — the same rule the status badge applies to each card.
        val live = funds.filter { it.status != FundStatus.Archived }
        FundsUiState(
            funds = funds,
            selectedFund = selected,
            selectedTransactions = transactions,
            selectedBreakdown = breakdown,
            totalAllocatedMinor = live.sumOf { it.fund.amountMinor },
            totalSpentMinor = live.sumOf { it.spentMinor }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FundsUiState()
    )

    fun openFund(fundId: String) {
        selectedFundId.value = fundId
    }

    fun closeFund() {
        selectedFundId.value = null
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
        viewModelScope.launch {
            fundRepository.createFund(
                fund = Fund(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    amountMinor = amountMinor,
                    startDate = startDate,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    note = note.trim()
                ),
                incomeCategoryId = incomeCategoryId
            )
        }
    }

    fun updateFundAmount(fundId: String, amountMinor: Long) {
        viewModelScope.launch {
            fundRepository.updateFundAmount(fundId, amountMinor)
        }
    }

    fun setArchived(fundId: String, archived: Boolean) {
        viewModelScope.launch { fundRepository.setArchived(fundId, archived) }
    }

    fun deleteFund(fundId: String) {
        viewModelScope.launch {
            fundRepository.deleteFund(fundId)
            if (selectedFundId.value == fundId) selectedFundId.value = null
        }
    }
}

/**
 * What a fund's money was spent on.
 *
 * Built from the fund's own linked transactions, which are expenses only — the income row
 * that created the fund is booked against the fund too, so it is filtered out here or it
 * would dwarf every real category.
 */
private fun buildBreakdown(
    transactions: List<Transaction>,
    categories: List<CategoryType>,
    fund: Fund
): FundBreakdown {
    val expenses = transactions.filter { it.transactionTypeId == 2 && !it.isDeleted }
    if (expenses.isEmpty()) return FundBreakdown()

    val categoryMap = categories.associateBy { it.id }
    val total = expenses.sumOf { it.amountMinor }.coerceAtLeast(1L)

    val slices = expenses
        .groupBy { it.categoryId }
        .map { (categoryId, items) -> categoryId to items.sumOf { it.amountMinor } }
        .sortedByDescending { it.second }
        .map { (categoryId, amountMinor) ->
            val category = categoryMap[categoryId]
            FundCategorySlice(
                categoryId = categoryId,
                label = category?.name.orEmpty(),
                amountMinor = amountMinor,
                fraction = amountMinor.toFloat() / total.toFloat(),
                colorHex = category?.colorHex
            )
        }

    return FundBreakdown(slices = slices, totalSpentMinor = total)
}
