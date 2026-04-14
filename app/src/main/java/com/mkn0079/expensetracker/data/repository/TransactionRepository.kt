package com.mkn0079.expensetracker.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.toDomain
import com.mkn0079.expensetracker.data.local.room.toEntity
import com.mkn0079.expensetracker.data.local.room.query.HomeRecentTransactionRow
import com.mkn0079.expensetracker.models.SyncState
import com.mkn0079.expensetracker.models.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class TransactionSummary(
    val totalIncomeMinor: Long,
    val totalExpenseMinor: Long,
    val highlightedExpenseMinor: Long
)

data class RecentTransaction(
    val transaction: Transaction,
    val paymentTypeName: String
)

class TransactionRepository(context: Context) {

    private val database = ExpenseTrackerDatabase.getInstance(context)
    private val dao = database.transactionDao()
    private val recurringRuleDao = database.recurringRuleDao()

    fun observeActiveTransactions(): Flow<List<Transaction>> {
        return dao.observeActiveTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observeHomeSummary(): Flow<TransactionSummary> {
        return dao.observeHomeSummary().map { row ->
            TransactionSummary(
                totalIncomeMinor = row.incomeMinor,
                totalExpenseMinor = row.expenseMinor,
                highlightedExpenseMinor = row.highlightedExpenseMinor
            )
        }
    }

    fun observeRecentTransactions(limit: Int): Flow<List<RecentTransaction>> {
        return dao.observeRecentTransactions(limit).map { rows ->
            rows.map(HomeRecentTransactionRow::toRecentTransaction)
        }
    }

    fun observeActiveTransactionCount(): Flow<Int> {
        return dao.observeActiveTransactionCount()
    }

    suspend fun getTransactionById(id: String): Transaction? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun upsertTransaction(transaction: Transaction): Transaction {
        val now = System.currentTimeMillis()
        val existing = transaction.id.takeIf { it.isNotBlank() }?.let { dao.getById(it) }
        val resolved = transaction.copy(
            id = transaction.id.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            contentHash = TransactionContentHashBuilder.build(transaction),
            syncState = if (existing == null) SyncState.LOCAL_ONLY else SyncState.PENDING_UPLOAD,
            updatedAt = now
        )
        dao.upsert(resolved.toEntity())
        return resolved
    }

    suspend fun softDeleteTransaction(id: String) {
        dao.softDelete(
            id = id,
            syncState = SyncState.PENDING_DELETE.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun deleteAllTransactions() {
        database.withTransaction {
            recurringRuleDao.deleteAll()
            dao.deleteAll()
        }
    }
}

private fun HomeRecentTransactionRow.toRecentTransaction(): RecentTransaction {
    return RecentTransaction(
        transaction = Transaction(
            id = id,
            note = note,
            createdAt = occurredAt,
            amountMinor = amountMinor,
            transactionTypeId = transactionTypeId,
            paymentTypeId = paymentMethodId,
            categoryId = categoryId,
            contentHash = contentHash,
            syncState = syncState,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
            sourceRecurringRuleId = sourceRecurringRuleId
        ),
        paymentTypeName = paymentMethodName
    )
}
