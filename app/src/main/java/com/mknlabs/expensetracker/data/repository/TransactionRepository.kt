package com.mknlabs.expensetracker.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.data.local.room.query.HomeRecentTransactionRow
import com.mknlabs.expensetracker.data.local.room.query.RangeSummaryRow
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.dao.RecurringRuleDao
import com.mknlabs.expensetracker.domain.repository.RecentTransaction
import com.mknlabs.expensetracker.domain.repository.TransactionQuery
import com.mknlabs.expensetracker.domain.repository.TransactionSummary
import com.mknlabs.expensetracker.domain.repository.TransactionTotals
import com.mknlabs.expensetracker.domain.repository.TransactionRepository as DomainTransactionRepository
import com.mknlabs.expensetracker.domain.usecase.CheckLargeTransactionUseCase
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val database: ExpenseTrackerDatabase,
    private val dao: TransactionDao,
    private val recurringRuleDao: RecurringRuleDao,
    private val checkLargeTransactionUseCase: CheckLargeTransactionUseCase
) : DomainTransactionRepository {

    companion object {
        /**
         * Paging 3 configuration: 30 items per page, prefetch 10 items before end.
         * This ensures smooth scrolling with minimal memory usage.
         */
        private const val PAGE_SIZE = 30
        private const val PREFETCH_DISTANCE = 10
        private const val INITIAL_LOAD_SIZE = 60 // 2x page size for faster initial load
    }

    // ============================================================================
    // Paging 3 Implementation
    // ============================================================================

    override fun getTransactionsPaging(query: TransactionQuery): Flow<PagingData<Transaction>> {
        return Pager(
            config = pagingConfig(),
            pagingSourceFactory = {
                dao.getTransactionsPagingSource(
                    TransactionQuerySql.toSQLiteQuery(TransactionQuerySql.paging(query))
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getTransactionIds(query: TransactionQuery): List<String> =
        withContext(Dispatchers.IO) {
            dao.getTransactionIds(TransactionQuerySql.toSQLiteQuery(TransactionQuerySql.ids(query)))
        }

    override fun observeTransactionTotals(query: TransactionQuery): Flow<TransactionTotals> {
        return dao.observeTransactionTotals(
            TransactionQuerySql.toSQLiteQuery(TransactionQuerySql.totals(query))
        ).map { row ->
            TransactionTotals(
                incomeMinor = row.incomeMinor,
                expenseMinor = row.expenseMinor,
                totalCount = row.totalCount
            )
        }
    }

    private fun pagingConfig() = PagingConfig(
        pageSize = PAGE_SIZE,
        prefetchDistance = PREFETCH_DISTANCE,
        initialLoadSize = INITIAL_LOAD_SIZE,
        enablePlaceholders = false
    )

    override fun observeActiveTransactions(): Flow<List<Transaction>> {
        return dao.observeActiveTransactions().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeHomeSummary(
        currentMonthStartMillis: Long,
        currentMonthEndMillis: Long,
        previousMonthStartMillis: Long,
        previousMonthEndMillis: Long,
        todayStartMillis: Long,
        todayEndMillis: Long
    ): Flow<TransactionSummary> {
        return dao.observeHomeSummary(
            currentMonthStartMillis = currentMonthStartMillis,
            currentMonthEndMillis = currentMonthEndMillis,
            previousMonthStartMillis = previousMonthStartMillis,
            previousMonthEndMillis = previousMonthEndMillis,
            todayStartMillis = todayStartMillis,
            todayEndMillis = todayEndMillis
        ).map { row ->
            TransactionSummary(
                totalIncomeMinor = row.incomeMinor,
                totalExpenseMinor = row.expenseMinor,
                highlightedExpenseMinor = row.highlightedExpenseMinor,
                previousMonthIncomeMinor = row.previousMonthIncomeMinor,
                previousMonthExpenseMinor = row.previousMonthExpenseMinor
            )
        }.flowOn(Dispatchers.IO)
    }

    override fun observeRecentTransactions(limit: Int): Flow<List<RecentTransaction>> {
        return dao.observeRecentTransactions(limit).map { rows ->
            rows.map(HomeRecentTransactionRow::toRecentTransaction)
        }.flowOn(Dispatchers.IO)
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
            syncState = SyncState.PENDING_UPLOAD,
            updatedAt = now
        )
        dao.upsert(resolved.toEntity())
        // Repo-level hook (spec category 3): fires for every write path — manual
        // save, duplicate, undo-restore, SMS import, and recurring auto-add /
        // backfill. Batch imports (backup restore, cloud pull, legacy import)
        // bypass the repository deliberately, so they stay silent.
        checkLargeTransactionUseCase(resolved)
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

    override suspend fun getRangeSummary(startMillis: Long, endMillis: Long): TransactionSummary =
        withContext(Dispatchers.IO) {
            val row = dao.getRangeSummary(startMillis, endMillis)
            TransactionSummary(
                totalIncomeMinor = row.incomeMinor,
                totalExpenseMinor = row.expenseMinor,
                highlightedExpenseMinor = 0L,
                previousMonthIncomeMinor = 0L,
                previousMonthExpenseMinor = 0L
            )
        }

    override suspend fun hasTransactionsInRange(startMillis: Long, endMillis: Long): Boolean =
        withContext(Dispatchers.IO) {
            dao.hasTransactionsInRange(startMillis, endMillis)
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
