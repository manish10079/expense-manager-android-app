package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.data.constants.defaultAppSettings
import com.mknlabs.expensetracker.data.local.FakePreferencesDataStore
import com.mknlabs.expensetracker.data.local.SmsLearningStore
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity
import com.mknlabs.expensetracker.data.local.room.query.HomeRecentTransactionRow
import com.mknlabs.expensetracker.data.local.room.query.HomeSummaryRow
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.domain.repository.RecentTransaction
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.domain.repository.TransactionSummary
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.AppThemeMode
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.sms.ParsedSms
import com.mknlabs.expensetracker.sms.SmsConfidence
import com.mknlabs.expensetracker.sms.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmsChangeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun parsedSms(
        amountMinor: Long = 52_000L,
        smsTimestamp: Long = 1_700_000_000_000L,
        transactionTypeId: Int = 2,
        categoryId: Int = 1,
        merchant: String? = "swiggy"
    ) = ParsedSms(
        amountMinor = amountMinor,
        sender = "HDFC Bank",
        body = "Rs 520 debited from A/c XX1234 via UPI. - HDFC Bank",
        smsTimestamp = smsTimestamp,
        transactionTypeId = transactionTypeId,
        categoryId = categoryId,
        merchant = merchant,
        confidence = SmsConfidence.HIGH
    )

    @Test
    fun load_startsWithSuggestedCategoryAsDefault() = runTest {
        val viewModel = viewModel()

        viewModel.load(parsedSms(categoryId = 22))

        val state = viewModel.uiState.value
        assertEquals(22, state.selectedCategoryId)
        assertEquals("", state.note)
        assertFalse(state.isSaved)
        assertFalse(state.isSaving)
    }

    @Test
    fun load_resetsPreviousSession_freshStatePerChangeTap() = runTest {
        val viewModel = viewModel()
        viewModel.load(parsedSms(categoryId = 1))
        viewModel.onCategorySelected(22)
        viewModel.onNoteChange("old note")
        viewModel.save()

        viewModel.load(parsedSms(categoryId = 3))

        val state = viewModel.uiState.value
        assertEquals(3, state.selectedCategoryId)
        assertEquals("", state.note)
        assertFalse(state.isSaved)
        assertFalse(state.isSaving)
    }

    @Test
    fun onCategorySelected_updatesSelection() = runTest {
        val viewModel = viewModel()
        viewModel.load(parsedSms())

        viewModel.onCategorySelected(22)

        assertEquals(22, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun onNoteChange_updatesNote_limitedTo200Chars() = runTest {
        val viewModel = viewModel()
        viewModel.load(parsedSms())

        viewModel.onNoteChange("x".repeat(250))

        assertEquals(200, viewModel.uiState.value.note.length)
    }

    @Test
    fun save_persistsSelectedCategoryNoteAndConfiguredPayment() = runTest {
        val txRepo = FakeTransactionRepository()
        val viewModel = viewModel(
            txRepo = txRepo,
            appSettings = defaultAppSettings.copy(defaultPaymentTypeId = 3)
        )
        viewModel.load(parsedSms(categoryId = 1))
        viewModel.onCategorySelected(22)
        viewModel.onNoteChange("  Team lunch  ")

        viewModel.save()

        assertEquals(1, txRepo.upserted.size)
        val transaction = txRepo.upserted.single()
        assertEquals(52_000L, transaction.amountMinor)
        assertEquals(1_700_000_000_000L, transaction.createdAt)
        assertEquals(2, transaction.transactionTypeId)
        assertEquals(22, transaction.categoryId)
        assertEquals(3, transaction.paymentTypeId)
        assertEquals("Team lunch", transaction.note)
        assertTrue(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun save_skipsUpsert_whenAlreadyImported_butStillMarksSaved() = runTest {
        val txRepo = FakeTransactionRepository()
        val dao = FakeTransactionDao(duplicates = setOf(52_000L to 1_700_000_000_000L))
        val viewModel = viewModel(txRepo = txRepo, dao = dao)
        viewModel.load(parsedSms())
        viewModel.onCategorySelected(22)

        viewModel.save()

        assertTrue(txRepo.upserted.isEmpty())
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun save_doesNothing_whenNoParsedSmsLoaded() = runTest {
        val txRepo = FakeTransactionRepository()
        val viewModel = viewModel(txRepo = txRepo)

        viewModel.save()

        assertTrue(txRepo.upserted.isEmpty())
        assertFalse(viewModel.uiState.value.isSaved)
    }

    private fun viewModel(
        txRepo: FakeTransactionRepository = FakeTransactionRepository(),
        dao: FakeTransactionDao = FakeTransactionDao(duplicates = emptySet()),
        appSettings: AppSettings = defaultAppSettings,
        smsLearningStore: SmsLearningStore = SmsLearningStore(FakePreferencesDataStore())
    ) = SmsChangeViewModel(
        smsRepository = SmsRepository(dao, txRepo),
        appPreferencesRepository = FakeAppPreferencesRepository(appSettings),
        smsLearningStore = smsLearningStore
    )

    @Test
    fun save_withChangedCategoryAndMerchant_recordsLearningOverride() = runTest {
        val store = SmsLearningStore(FakePreferencesDataStore())
        val viewModel = viewModel(smsLearningStore = store)
        viewModel.load(parsedSms(categoryId = 1))
        viewModel.onCategorySelected(22) // user corrects Swiggy → Transport

        viewModel.save()

        assertEquals(mapOf("swiggy" to 22), store.observeOverrides().first())
    }

    @Test
    fun save_withUnchangedCategory_doesNotRecordOverride() = runTest {
        val store = SmsLearningStore(FakePreferencesDataStore())
        val viewModel = viewModel(smsLearningStore = store)
        viewModel.load(parsedSms(categoryId = 1))
        // Keep the suggested category.

        viewModel.save()

        assertTrue(store.observeOverrides().first().isEmpty())
    }

    @Test
    fun save_withoutMerchant_doesNotRecordOverride() = runTest {
        val store = SmsLearningStore(FakePreferencesDataStore())
        val viewModel = viewModel(smsLearningStore = store)
        viewModel.load(parsedSms(categoryId = 1, merchant = null))
        viewModel.onCategorySelected(22)

        viewModel.save()

        assertTrue(store.observeOverrides().first().isEmpty())
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
            return transaction.copy(id = transaction.id.ifBlank { "generated-id" })
        }

        override suspend fun softDeleteTransaction(id: String) = error("unexpected")
        override suspend fun softDeleteTransactions(ids: List<String>) = error("unexpected")
        override suspend fun deleteAllTransactions() = error("unexpected")
    }

    private class FakeAppPreferencesRepository(
        private val settings: AppSettings
    ) : AppPreferencesRepository {
        override fun observeAppSettings(): Flow<AppSettings> = flowOf(settings)

        override suspend fun updateCurrency(currencyId: Int) = Unit
        override suspend fun updateDateFormat(dateFormatPattern: String) = Unit
        override suspend fun updateTimeFormat(timeFormat: String) = Unit
        override suspend fun updateThemeMode(themeMode: AppThemeMode) = Unit
        override suspend fun updateCurrencyGroupingStyle(groupingStyle: CurrencyGroupingStyle) = Unit
        override suspend fun updateCurrencyDecimalPlaces(decimalPlaces: Int) = Unit
    }
}
