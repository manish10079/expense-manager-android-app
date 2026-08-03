package com.mknlabs.expensetracker.sms

import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity
import com.mknlabs.expensetracker.data.local.room.query.HomeRecentTransactionRow
import com.mknlabs.expensetracker.data.local.room.query.HomeSummaryRow
import com.mknlabs.expensetracker.domain.repository.RecentTransaction
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.domain.repository.TransactionSummary
import com.mknlabs.expensetracker.models.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsRepositoryTest {

    private fun parsedSms(
        amountMinor: Long = 52_000L,
        smsTimestamp: Long = 1_700_000_000_000L
    ) = ParsedSms(
        amountMinor = amountMinor,
        sender = "HDFC Bank",
        body = "Rs 520 debited from A/c XX1234 via UPI. - HDFC Bank",
        smsTimestamp = smsTimestamp,
        transactionTypeId = 2,
        categoryId = 1,
        merchant = "swiggy",
        confidence = SmsConfidence.HIGH
    )

    @Test
    fun isDuplicate_delegatesToDao_withParsedAmountAndTimestamp() = runTest {
        val dao = FakeTransactionDao(duplicates = setOf(52_000L to 1_700_000_000_000L))
        val repository = SmsRepository(dao, FakeTransactionRepository())

        assertTrue(repository.isDuplicate(parsedSms()))
    }

    @Test
    fun isDuplicate_returnsFalse_whenNoMatchingTransaction() = runTest {
        val dao = FakeTransactionDao(duplicates = emptySet())
        val repository = SmsRepository(dao, FakeTransactionRepository())

        assertFalse(repository.isDuplicate(parsedSms()))
    }

    @Test
    fun saveFromSms_buildsTransactionFromParsedSms_andDelegatesUpsert() = runTest {
        val dao = FakeTransactionDao(duplicates = emptySet())
        val txRepo = FakeTransactionRepository()
        val repository = SmsRepository(dao, txRepo)

        val saved = repository.saveFromSms(parsedSms())

        // The repository was asked to save a fresh transaction (blank id)...
        assertEquals(1, txRepo.upserted.size)
        val transaction = txRepo.upserted.single()
        assertEquals("", transaction.id)
        assertEquals(52_000L, transaction.amountMinor)
        assertEquals(1_700_000_000_000L, transaction.createdAt)
        assertEquals(2, transaction.transactionTypeId)
        assertEquals(1, transaction.categoryId)
        assertEquals(DEFAULT_PAYMENT_TYPE_ID, transaction.paymentTypeId)
        assertEquals("", transaction.note)
        // ...and the persisted result (id assigned by the repository) is returned.
        assertEquals("generated-id", saved.id)
        assertEquals(saved, txRepo.upserted.single().copy(id = "generated-id"))
    }

    @Test
    fun saveFromSms_allowsCategoryOverride_forChangeFlow() = runTest {
        val dao = FakeTransactionDao(duplicates = emptySet())
        val txRepo = FakeTransactionRepository()
        val repository = SmsRepository(dao, txRepo)

        repository.saveFromSms(parsedSms(), categoryId = 22)

        assertEquals(22, txRepo.upserted.single().categoryId)
    }

    @Test
    fun saveFromSms_passesThroughNoteAndPaymentTypeId() = runTest {
        val dao = FakeTransactionDao(duplicates = emptySet())
        val txRepo = FakeTransactionRepository()
        val repository = SmsRepository(dao, txRepo)

        repository.saveFromSms(parsedSms(), note = "Team lunch", paymentTypeId = 3)

        assertEquals("Team lunch", txRepo.upserted.single().note)
        assertEquals(3, txRepo.upserted.single().paymentTypeId)
    }

    /** Fake DAO: only the dedup query is implemented; anything else fails loudly. */
    private class FakeTransactionDao(
        private val duplicates: Set<Pair<Long, Long>>
    ) : TransactionDao {
        override suspend fun existsByAmountAndTimestamp(amountMinor: Long, createdAt: Long): Boolean =
            amountMinor to createdAt in duplicates

        override fun observeActiveTransactions(): Flow<List<TransactionEntity>> = error("unexpected")
        override suspend fun getActiveTransactions(): List<TransactionEntity> = error("unexpected")
        override suspend fun getAllTransactions(): List<TransactionEntity> = error("unexpected")
        override suspend fun getById(id: String): TransactionEntity? = error("unexpected")
        override fun observeHomeSummary(): Flow<HomeSummaryRow> = error("unexpected")
        override fun observeRecentTransactions(limit: Int): Flow<List<HomeRecentTransactionRow>> = error("unexpected")
        override fun observeActiveTransactionCount(): Flow<Int> = error("unexpected")
        override suspend fun countAll(): Int = error("unexpected")
        override suspend fun getMonthlyCategorySpending(categoryId: Int, monthStr: String): Long = error("unexpected")
        override suspend fun getTodayTransactionCount(dayStr: String): Int = error("unexpected")
        override suspend fun upsert(transaction: TransactionEntity) = error("unexpected")
        override suspend fun upsertAll(transactions: List<TransactionEntity>) = error("unexpected")
        override suspend fun softDelete(id: String, syncState: String, updatedAt: Long) = error("unexpected")
        override suspend fun restore(id: String, syncState: String, updatedAt: Long) = error("unexpected")
        override suspend fun updateRecurringSourceReference(
            id: String,
            sourceRecurringRuleId: String?,
            contentHash: String?,
            syncState: String,
            updatedAt: Long
        ) = error("unexpected")

        override suspend fun getUnsynced(): List<TransactionEntity> = error("unexpected")
        override suspend fun updateSyncStates(ids: List<String>, syncState: String) = error("unexpected")
        override suspend fun deleteAll() = error("unexpected")
        override suspend fun purgeOldDeleted(threshold: Long) = error("unexpected")
    }

    /** Fake domain repository: captures upserted transactions. */
    private class FakeTransactionRepository : TransactionRepository {
        val upserted = mutableListOf<Transaction>()

        override fun observeActiveTransactions(): Flow<List<Transaction>> = error("unexpected")
        override fun observeHomeSummary(): Flow<TransactionSummary> = error("unexpected")
        override fun observeRecentTransactions(limit: Int): Flow<List<RecentTransaction>> = error("unexpected")
        override fun observeActiveTransactionCount(): Flow<Int> = error("unexpected")
        override suspend fun getTransactionById(id: String): Transaction? = error("unexpected")
        override suspend fun upsertTransaction(transaction: Transaction): Transaction {
            upserted += transaction
            // Mimics the real repository: blank ids are assigned before persisting.
            return transaction.copy(id = transaction.id.ifBlank { "generated-id" })
        }

        override suspend fun softDeleteTransaction(id: String) = error("unexpected")
        override suspend fun softDeleteTransactions(ids: List<String>) = error("unexpected")
        override suspend fun deleteAllTransactions() = error("unexpected")
    }
}
