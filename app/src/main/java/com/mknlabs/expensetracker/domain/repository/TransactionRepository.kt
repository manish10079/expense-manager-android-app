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
    
    suspend fun checkBudgetAndNotify(context: android.content.Context, transaction: Transaction)
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