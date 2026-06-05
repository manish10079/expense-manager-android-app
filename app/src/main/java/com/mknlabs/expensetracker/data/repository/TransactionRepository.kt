package com.mknlabs.expensetracker.data.repository

import androidx.room.withTransaction
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.data.local.room.query.HomeRecentTransactionRow
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.dao.RecurringRuleDao
import com.mknlabs.expensetracker.domain.repository.RecentTransaction
import com.mknlabs.expensetracker.domain.repository.TransactionSummary
import com.mknlabs.expensetracker.domain.repository.TransactionRepository as DomainTransactionRepository
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val database: ExpenseTrackerDatabase,
    private val dao: TransactionDao,
    private val recurringRuleDao: RecurringRuleDao
) : DomainTransactionRepository {

    override fun observeActiveTransactions(): Flow<List<Transaction>> {
        return dao.observeActiveTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeHomeSummary(): Flow<TransactionSummary> {
        return dao.observeHomeSummary().map { row ->
            TransactionSummary(
                totalIncomeMinor = row.incomeMinor,
                totalExpenseMinor = row.expenseMinor,
                highlightedExpenseMinor = row.highlightedExpenseMinor,
                previousMonthIncomeMinor = row.previousMonthIncomeMinor,
                previousMonthExpenseMinor = row.previousMonthExpenseMinor
            )
        }
    }

    override fun observeRecentTransactions(limit: Int): Flow<List<RecentTransaction>> {
        return dao.observeRecentTransactions(limit).map { rows ->
            rows.map(HomeRecentTransactionRow::toRecentTransaction)
        }
    }

    override fun observeActiveTransactionCount(): Flow<Int> {
        return dao.observeActiveTransactionCount()
    }

    override suspend fun getTransactionById(id: String): Transaction? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomain()
    }

    override suspend fun upsertTransaction(transaction: Transaction): Transaction = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = transaction.id.takeIf { it.isNotBlank() }?.let { dao.getById(it) }
        val resolved = transaction.copy(
            id = transaction.id.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            contentHash = TransactionContentHashBuilder.build(transaction),
            syncState = if (existing == null) SyncState.LOCAL_ONLY else SyncState.PENDING_UPLOAD,
            updatedAt = now
        )
        dao.upsert(resolved.toEntity())
        resolved
    }

    override suspend fun softDeleteTransaction(id: String) = withContext(Dispatchers.IO) {
        dao.softDelete(
            id = id,
            syncState = SyncState.PENDING_DELETE.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun softDeleteTransactions(ids: List<String>) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        database.withTransaction {
            ids.forEach { id ->
                dao.softDelete(
                    id = id,
                    syncState = SyncState.PENDING_DELETE.name,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
    }

    override suspend fun deleteAllTransactions() = withContext(Dispatchers.IO) {
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
