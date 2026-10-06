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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/** One category's share of what a single fund has spent, for the fund detail breakdown. */
@Immutable
data class FundCategorySlice(
    val categoryId: Int,
    val label: String,
    val amountMinor: Long,
    val fraction: Float,
    val colorHex: String? = null
)

/** One bucket of a fund's spending over time. */
@Immutable
data class FundTimelinePoint(
    val label: String,
    val amountMinor: Long,
    val fraction: Float
)

/** The per-fund breakdown: what the money went on, and when. */
@Immutable
data class FundBreakdown(
    val slices: List<FundCategorySlice> = emptyList(),
    val timeline: List<FundTimelinePoint> = emptyList(),
    val totalSpentMinor: Long = 0L
) {
    val isEmpty: Boolean
        get() = slices.isEmpty() && timeline.isEmpty()
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

    /** Live transactions of the fund currently open, or nothing when none is open. */
    private val selectedTransactions: Flow<List<Transaction>> =
        selectedFundId.flatMapLatest { id ->
            if (id.isNullOrBlank()) flowOf(emptyList())
            else fundRepository.observeTransactionsByFund(id)
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
        note: String
    ) {
        viewModelScope.launch {
            fundRepository.createFund(
                Fund(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    amountMinor = amountMinor,
                    startDate = startDate,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    note = note.trim()
                )
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

private val bucketFormatter = SimpleDateFormat("MMM yy", Locale.getDefault())

/**
 * What a fund's money was spent on, and when.
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

    val buckets = expenses
        .groupBy { monthStartOf(it.createdAt) }
        .toSortedMap()
        .entries
        .toList()
        .takeLast(6)
    val maxBucket = buckets.maxOfOrNull { it.value.sumOf { tx -> tx.amountMinor } }?.coerceAtLeast(1L) ?: 1L
    val timeline = buckets.map { (monthStart, items) ->
        val amountMinor = items.sumOf { it.amountMinor }
        FundTimelinePoint(
            label = bucketFormatter.format(Date(monthStart)),
            amountMinor = amountMinor,
            fraction = amountMinor.toFloat() / maxBucket.toFloat()
        )
    }

    return FundBreakdown(slices = slices, timeline = timeline, totalSpentMinor = total)
}

private fun monthStartOf(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}
