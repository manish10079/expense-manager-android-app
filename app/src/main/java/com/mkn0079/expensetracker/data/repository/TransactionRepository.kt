package com.mkn0079.expensetracker.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.toDomain
import com.mkn0079.expensetracker.data.local.room.toEntity
import com.mkn0079.expensetracker.data.local.room.query.HomeRecentTransactionRow
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.domain.repository.RecentTransaction
import com.mkn0079.expensetracker.domain.repository.TransactionSummary
import com.mkn0079.expensetracker.domain.repository.TransactionRepository as DomainTransactionRepository
import com.mkn0079.expensetracker.models.SyncState
import com.mkn0079.expensetracker.models.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class TransactionRepository(context: Context) : DomainTransactionRepository {

    private val database = ExpenseTrackerDatabase.getInstance(context)
    private val dao = database.transactionDao()
    private val recurringRuleDao = database.recurringRuleDao()

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

    override suspend fun getTransactionById(id: String): Transaction? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun upsertTransaction(transaction: Transaction): Transaction {
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

    override suspend fun softDeleteTransaction(id: String) {
        dao.softDelete(
            id = id,
            syncState = SyncState.PENDING_DELETE.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteAllTransactions() {
        database.withTransaction {
            recurringRuleDao.deleteAll()
            dao.deleteAll()
        }
    }

    override suspend fun checkBudgetAndNotify(
        context: android.content.Context,
        transaction: Transaction
    ) {
        if (transaction.transactionTypeId != 2) return // Only for expenses
        
        val settings = AppSettingsDataStore.getAppSettingsFlow(context).first()
        if (!settings.budgetLimitAlertsEnabled) return

        val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        val monthStr = sdf.format(java.util.Date(transaction.createdAt))
        
        // Find if there's a budget for this category and month
        val budgetEntity = database.budgetDao().getActiveByCategoryAndMonthStart(
            categoryId = transaction.categoryId,
            monthStart = getStartOfMonthTimestamp(transaction.createdAt)
        )

        if (budgetEntity != null) {
            val currentSpending = dao.getMonthlyCategorySpending(transaction.categoryId, monthStr)
            if (currentSpending > budgetEntity.limitMinor) {
                // Get category name for message
                val categoryName = database.categoryDao().getById(transaction.categoryId)?.name ?: "Unknown"
                val message = com.mkn0079.expensetracker.notifications.DynamicNotificationEngine.generateBudgetExceededMessage(categoryName)
                com.mkn0079.expensetracker.notifications.NotificationHelper.showBudgetExceededNotification(context, message)
            }
        }
    }

    private fun getStartOfMonthTimestamp(timestamp: Long): Long {
        return java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
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
