package com.mknlabs.expensetracker.data.repository

import androidx.room.withTransaction
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.data.local.room.dao.FundDao
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.domain.repository.FundRepository
import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.FundWithProgress
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FundRepositoryImpl @Inject constructor(
    private val fundDao: FundDao,
    private val transactionDao: TransactionDao,
    private val database: ExpenseTrackerDatabase
) : FundRepository {

    override fun observeActiveFunds(): Flow<List<Fund>> =
        fundDao.observeActiveFunds()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override fun observeFundProgress(): Flow<List<FundWithProgress>> =
        combine(fundDao.observeActiveFunds(), fundDao.observeSpentByFund()) { funds, spend ->
            val spentByFund = spend.associate { it.fundId to it.spentMinor }
            funds.map { entity ->
                FundWithProgress(entity.toDomain(), spentByFund[entity.id] ?: 0L)
            }
        }.flowOn(Dispatchers.IO)

    override fun observeTransactionsByFund(fundId: String): Flow<List<Transaction>> =
        transactionDao.observeTransactionsByFund(fundId)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun getFundById(id: String): Fund? = withContext(Dispatchers.IO) {
        fundDao.getById(id)?.toDomain()
    }

    override suspend fun createFund(
        fund: Fund,
        incomeCategoryId: Int,
        incomePaymentTypeId: Int
    ): Fund = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val resolved = fund.copy(
            id = fund.id.ifBlank { UUID.randomUUID().toString() },
            createdAt = if (fund.createdAt == 0L) now else fund.createdAt,
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )

        // The money arriving, booked as income and linked back to the fund. Its amount is the
        // fund's ceiling, and the date it landed is the fund's start date.
        val incomeBase = Transaction(
            id = UUID.randomUUID().toString(),
            note = resolved.name,
            createdAt = if (resolved.startDate != 0L) resolved.startDate else now,
            amountMinor = resolved.amountMinor,
            transactionTypeId = 1,
            paymentTypeId = incomePaymentTypeId,
            categoryId = incomeCategoryId,
            syncState = SyncState.PENDING_UPLOAD,
            updatedAt = now,
            fundId = resolved.id
        )
        val income = incomeBase.copy(
            contentHash = TransactionContentHashBuilder.build(incomeBase)
        )

        database.withTransaction {
            fundDao.upsert(resolved.toEntity())
            transactionDao.upsert(income.toEntity())
        }
        resolved
    }

    override suspend fun updateFund(fund: Fund): Fund = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val resolved = fund.copy(updatedAt = now, syncState = SyncState.PENDING_UPLOAD)
        fundDao.upsert(resolved.toEntity())
        resolved
    }

    override suspend fun updateFundAmount(id: String, amountMinor: Long): Fund? =
        withContext(Dispatchers.IO) {
            val existing = fundDao.getById(id) ?: return@withContext null
            val now = System.currentTimeMillis()
            val resolved = existing.copy(
                amountMinor = amountMinor,
                updatedAt = now,
                syncState = SyncState.PENDING_UPLOAD
            )
            database.withTransaction {
                fundDao.upsert(resolved)
                // The linked income row is the money that filled this bucket, so it has to
                // move with it — otherwise the arrival would disagree with the fund's ceiling
                // on this device and, after a sync, on every other one.
                transactionDao.getLinkedIncomeByFund(id).forEach { entity ->
                    val domain = entity.toDomain().copy(
                        amountMinor = amountMinor,
                        updatedAt = now,
                        syncState = SyncState.PENDING_UPLOAD
                    )
                    transactionDao.upsert(
                        domain.copy(contentHash = TransactionContentHashBuilder.build(domain)).toEntity()
                    )
                }
            }
            resolved.toDomain()
        }

    override suspend fun setArchived(id: String, archived: Boolean) = withContext(Dispatchers.IO) {
        val existing = fundDao.getById(id) ?: return@withContext
        fundDao.upsert(
            existing.copy(
                isArchived = archived,
                updatedAt = System.currentTimeMillis(),
                syncState = SyncState.PENDING_UPLOAD
            )
        )
    }

    override suspend fun deleteFund(id: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            // Unlink first: the spending stays, it just stops pointing at a bucket that is
            // gone. This is the behaviour a foreign key would have given, done here because
            // the column carries no constraint (see MIGRATION_19_20).
            fundDao.clearTransactionLinks(id, now)
            fundDao.softDelete(
                id = id,
                syncState = SyncState.PENDING_DELETE.name,
                updatedAt = now
            )
        }
    }
}
