package com.mknlabs.expensetracker.domain.repository

import androidx.paging.PagingData
import com.mknlabs.expensetracker.models.SortType
import com.mknlabs.expensetracker.models.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeActiveTransactions(): Flow<List<Transaction>>

    fun observeHomeSummary(
        currentMonthStartMillis: Long,
        currentMonthEndMillis: Long,
        previousMonthStartMillis: Long,
        previousMonthEndMillis: Long,
        todayStartMillis: Long,
        todayEndMillis: Long
    ): Flow<TransactionSummary>

    fun observeRecentTransactions(limit: Int): Flow<List<RecentTransaction>>

    fun observeActiveTransactionCount(): Flow<Int>

    suspend fun getTransactionById(id: String): Transaction?

    suspend fun upsertTransaction(transaction: Transaction): Transaction

    suspend fun softDeleteTransaction(id: String)

    suspend fun softDeleteTransactions(ids: List<String>)

    suspend fun deleteAllTransactions()

    // ============================================================================
    // Paging 3 API
    // ============================================================================

    /**
     * Paging 3 stream of active transactions matching [query].
     *
     * Every filter and the sort order are applied in SQL, so pages, totals and
     * "select all in view" all agree. The returned flow is cold; the caller
     * (ViewModel) is expected to `cachedIn(viewModelScope)` it and consume it with
     * `LazyPagingItems`.
     */
    fun getTransactionsPaging(query: TransactionQuery): Flow<PagingData<Transaction>>

    /**
     * Ids of every active transaction matching [query].
     *
     * Used by "select all N in this view", which must cover rows that are not
     * loaded yet — so this is a dedicated id projection rather than a page walk.
     */
    suspend fun getTransactionIds(query: TransactionQuery): List<String>

    /**
     * Income, expense and row count for the whole filtered result set, re-emitted
     * whenever the transactions table changes.
     *
     * This is what makes the summary card reflect the entire query rather than only
     * the pages that happen to be loaded in memory, and it keeps the totals and
     * "select all N in this view" correct after an add, edit or delete.
     */
    fun observeTransactionTotals(query: TransactionQuery): Flow<TransactionTotals>

    /** Income + expense sum for a time range (for summary cards). */
    suspend fun getRangeSummary(startMillis: Long, endMillis: Long): TransactionSummary

    /** Check whether any active transaction exists in a time range. */
    suspend fun hasTransactionsInRange(startMillis: Long, endMillis: Long): Boolean
}

/**
 * Immutable description of "which transactions, in what order" the Transactions
 * screen is currently showing. Translating the screen's filter state into this
 * value lets the repository build one SQL query that covers filtering, ordering
 * and counting consistently.
 *
 * [startMillis] / [endMillis] are half-open bounds on `occurred_at`
 * (`endMillis` exclusive). Unbounded is expressed with [NO_START] / [NO_END]
 * rather than nulls so the SQL builder can skip the clause.
 */
data class TransactionQuery(
    val startMillis: Long = NO_START,
    val endMillis: Long = NO_END,
    val search: String? = null,
    /** Categories whose name matches [search] (advanced search); OR-ed into the search clause. */
    val searchCategoryIds: List<Int> = emptyList(),
    /** Payment methods whose name matches [search] (advanced search); OR-ed into the search clause. */
    val searchPaymentTypeIds: List<Int> = emptyList(),
    val transactionTypeIds: List<Int> = listOf(1, 2),
    val categoryIds: List<Int> = emptyList(),
    val paymentTypeIds: List<Int> = emptyList(),
    val minAmountMinor: Long? = null,
    val maxAmountMinor: Long? = null,
    val sort: SortType = SortType.NEWEST
) {
    companion object {
        const val NO_START = Long.MIN_VALUE
        const val NO_END = Long.MAX_VALUE
    }
}

data class TransactionSummary(
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val highlightedExpenseMinor: Long,
    val previousMonthIncomeMinor: Long,
    val previousMonthExpenseMinor: Long
)

/** Income/expense totals plus the row count for a filtered transaction query. */
data class TransactionTotals(
    val incomeMinor: Long,
    val expenseMinor: Long,
    val totalCount: Int
)

data class RecentTransaction(
    val transaction: Transaction,
    val paymentTypeName: String
)