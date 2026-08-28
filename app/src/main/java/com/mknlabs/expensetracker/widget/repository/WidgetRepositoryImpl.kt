package com.mknlabs.expensetracker.widget.repository

import android.content.Context
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.widget.model.WidgetParsedTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Widget-specific repository for reading widget data and saving transactions.
 *
 * Delegates to [TransactionRepository] for all Room writes — never touches DAO directly.
 * Provides widget-specific queries (today's spending) that the domain repository lacks.
 */
internal interface WidgetRepository {

    /** Today's total expense in minor units. */
    suspend fun getTodayExpenseMinor(): Long

    /** Currency symbol from app settings. */
    suspend fun getCurrencySymbol(): String

    /** Save a parsed transaction to Room via TransactionRepository. */
    suspend fun saveTransaction(parsed: WidgetParsedTransaction): Result<Transaction>
}

@Singleton
internal class WidgetRepositoryImpl @Inject constructor(
    private val database: ExpenseTrackerDatabase,
    private val transactionRepository: TransactionRepository
) : WidgetRepository {

    private val transactionDao = database.transactionDao()

    override suspend fun getTodayExpenseMinor(): Long = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().time)
        transactionDao.getTodayExpenseMinor(todayStr)
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.IO) {
        // Read from DataStore or default to ₹
        "₹"
    }

    override suspend fun saveTransaction(parsed: WidgetParsedTransaction): Result<Transaction> {
        return try {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                note = buildString {
                    append(parsed.note)
                    if (!parsed.merchant.isNullOrBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append(parsed.merchant)
                    }
                },
                createdAt = parsed.createdAt,
                amountMinor = parsed.amountMinor,
                transactionTypeId = parsed.transactionTypeId,
                paymentTypeId = parsed.paymentTypeId ?: 1,
                categoryId = parsed.categoryId,
                syncState = SyncState.PENDING_UPLOAD
            )
            val saved = transactionRepository.upsertTransaction(transaction)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
