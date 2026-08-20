package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeActiveTransactions(): Flow<List<Transaction>>
    
    fun observeHomeSummary(): Flow<TransactionSummary>
    
    fun observeRecentTransactions(limit: Int): Flow<List<RecentTransaction>>
    
    fun observeActiveTransactionCount(): Flow<Int>
    
    suspend fun getTransactionById(id: String): Transaction?
    
    suspend fun upsertTransaction(transaction: Transaction): Transaction
    
    suspend fun softDeleteTransaction(id: String)
    
    suspend fun softDeleteTransactions(ids: List<String>)
    
    suspend fun deleteAllTransactions()

    /**
     * Paged query for the Transactions screen. Returns a page of transactions
     * ordered by occurred_at DESC.
     *
     * @param pageSize Number of items per page.
     * @param pageNumber Zero-based page index.
     */
    suspend fun getActiveTransactionsPaged(pageSize: Int, pageNumber: Int): List<Transaction>

    /**
     * Paged query filtered to a specific time range.
     *
     * @param startMillis Inclusive start of the range (epoch millis).
     * @param endMillis Exclusive end of the range (epoch millis).
     * @param pageSize Number of items per page.
     * @param pageNumber Zero-based page index.
     */
    suspend fun getActiveTransactionsPagedInRange(startMillis: Long, endMillis: Long, pageSize: Int, pageNumber: Int): List<Transaction>

    /** Count of active transactions in a time range. */
    suspend fun countActiveTransactionsInRange(startMillis: Long, endMillis: Long): Int

    /** Count of all active transactions (no time filter). */
    suspend fun countActiveTransactions(): Int

    /** Income + expense sum for a time range (for summary cards). */
    suspend fun getRangeSummary(startMillis: Long, endMillis: Long): TransactionSummary

    /** Check whether any active transaction exists in a time range. */
    suspend fun hasTransactionsInRange(startMillis: Long, endMillis: Long): Boolean
}

data class TransactionSummary(
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val highlightedExpenseMinor: Long,
    val previousMonthIncomeMinor: Long,
    val previousMonthExpenseMinor: Long
)

data class RecentTransaction(
    val transaction: Transaction,
    val paymentTypeName: String
)