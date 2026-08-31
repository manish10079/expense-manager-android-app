package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity
import com.mknlabs.expensetracker.data.local.room.query.HomeRecentTransactionRow
import com.mknlabs.expensetracker.data.local.room.query.HomeSummaryRow
import com.mknlabs.expensetracker.data.local.room.query.RangeSummaryRow
import com.mknlabs.expensetracker.data.local.room.query.TopCategoryRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY occurred_at DESC")
    fun observeActiveTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY occurred_at DESC")
    suspend fun getActiveTransactions(): List<TransactionEntity>

    /**
     * Paged query for the Transactions screen. Returns a window of [limit] rows
     * starting at [offset], ordered by occurred_at DESC.
     */
    @Query("SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY occurred_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getActiveTransactionsPaged(limit: Int, offset: Int): List<TransactionEntity>

    /**
     * Paged query filtered to a specific time range [startMillis] to [endMillis].
     */
    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
          AND occurred_at >= :startMillis
          AND occurred_at < :endMillis
        ORDER BY occurred_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveTransactionsPagedInRange(startMillis: Long, endMillis: Long, limit: Int, offset: Int): List<TransactionEntity>

    /**
     * Paged query filtered to a specific year. [yearStartMillis] and [yearEndMillis]
     * are the epoch-millis boundaries of the year (inclusive start, exclusive end).
     */
    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
          AND occurred_at >= :yearStartMillis
          AND occurred_at < :yearEndMillis
        ORDER BY occurred_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveTransactionsPagedForYear(yearStartMillis: Long, yearEndMillis: Long, limit: Int, offset: Int): List<TransactionEntity>

    /**
     * Paged query filtered to a specific month. [monthStartMillis] and [monthEndMillis]
     * are the epoch-millis boundaries of the month (inclusive start, exclusive end).
     */
    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
          AND occurred_at >= :monthStartMillis
          AND occurred_at < :monthEndMillis
        ORDER BY occurred_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveTransactionsPagedForMonth(monthStartMillis: Long, monthEndMillis: Long, limit: Int, offset: Int): List<TransactionEntity>

    /**
     * Paged query filtered to a specific day. [dayStartMillis] and [dayEndMillis]
     * are the epoch-millis boundaries of the day (inclusive start, exclusive end).
     */
    @Query("""
        SELECT * FROM transactions
        WHERE is_deleted = 0
          AND occurred_at >= :dayStartMillis
          AND occurred_at < :dayEndMillis
        ORDER BY occurred_at DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveTransactionsPagedForDay(dayStartMillis: Long, dayEndMillis: Long, limit: Int, offset: Int): List<TransactionEntity>

    /** Count of active transactions in a time range. */
    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE is_deleted = 0
          AND occurred_at >= :startMillis
          AND occurred_at < :endMillis
    """)
    suspend fun countActiveTransactionsInRange(startMillis: Long, endMillis: Long): Int

    /** Count of all active transactions (no time filter). */
    @Query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
    suspend fun countActiveTransactions(): Int

    /** Check whether any active transaction exists in a time range. */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM transactions
            WHERE is_deleted = 0
              AND occurred_at >= :startMillis
              AND occurred_at < :endMillis
            LIMIT 1
        )
    """)
    suspend fun hasTransactionsInRange(startMillis: Long, endMillis: Long): Boolean

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    /**
     * Duplicate check for Smart SMS imports (plan D7): true when a transaction
     * with the same amount and SMS timestamp is already imported.
     *
     * Only active rows count — a soft-deleted transaction does NOT block
     * re-import (the user removed it deliberately, so a re-delivered SMS
     * re-imports; plan §9 accepted trade-off).
     */
    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM transactions
            WHERE amount_minor = :amountMinor
              AND created_at = :createdAt
              AND is_deleted = 0
            LIMIT 1
        )
        """
    )
    suspend fun existsByAmountAndTimestamp(amountMinor: Long, createdAt: Long): Boolean

    @Query(
        """
        SELECT
            COALESCE(
                SUM(
                    CASE
                        WHEN transaction_type_id = 1
                         AND occurred_at >= :currentMonthStartMillis
                         AND occurred_at <= :currentMonthEndMillis
                        THEN amount_minor
                        ELSE 0
                    END
                ),
                0
            ) AS income_minor,
            COALESCE(
                SUM(
                    CASE
                        WHEN transaction_type_id != 1
                         AND occurred_at >= :currentMonthStartMillis
                         AND occurred_at <= :currentMonthEndMillis
                        THEN amount_minor
                        ELSE 0
                    END
                ),
                0
            ) AS expense_minor,
            COALESCE(
                SUM(
                    CASE
                        WHEN transaction_type_id = 2
                         AND occurred_at >= :todayStartMillis
                         AND occurred_at <= :todayEndMillis
                        THEN amount_minor
                        ELSE 0
                    END
                ),
                0
            ) AS highlighted_expense_minor,
            COALESCE(
                SUM(
                    CASE
                        WHEN transaction_type_id = 1
                         AND occurred_at >= :previousMonthStartMillis
                         AND occurred_at <= :previousMonthEndMillis
                        THEN amount_minor
                        ELSE 0
                    END
                ),
                0
            ) AS previous_month_income_minor,
            COALESCE(
                SUM(
                    CASE
                        WHEN transaction_type_id != 1
                         AND occurred_at >= :previousMonthStartMillis
                         AND occurred_at <= :previousMonthEndMillis
                        THEN amount_minor
                        ELSE 0
                    END
                ),
                0
            ) AS previous_month_expense_minor
        FROM transactions
        WHERE is_deleted = 0
        """
    )
    fun observeHomeSummary(
        currentMonthStartMillis: Long,
        currentMonthEndMillis: Long,
        previousMonthStartMillis: Long,
        previousMonthEndMillis: Long,
        todayStartMillis: Long,
        todayEndMillis: Long
    ): Flow<HomeSummaryRow>

    @Query(
        """
        SELECT
            t.id,
            t.note,
            t.amount_minor,
            t.occurred_at,
            t.created_at,
            t.updated_at,
            t.transaction_type_id,
            t.category_id,
            t.payment_method_id,
            t.is_deleted,
            t.sync_state,
            t.content_hash,
            t.source_recurring_rule_id,
            COALESCE(pm.name, '') AS payment_method_name
        FROM transactions t
        LEFT JOIN payment_methods pm
            ON pm.id = t.payment_method_id
        WHERE t.is_deleted = 0
        ORDER BY t.occurred_at DESC
        LIMIT :limit
        """
    )
    fun observeRecentTransactions(limit: Int): Flow<List<HomeRecentTransactionRow>>

    @Query("SELECT COUNT(*) FROM transactions WHERE is_deleted = 0")
    fun observeActiveTransactionCount(): Flow<Int>

    /** Today's total expense in minor units, for the widget. */
    @Query("""
        SELECT COALESCE(SUM(amount_minor), 0)
        FROM transactions
        WHERE is_deleted = 0
          AND transaction_type_id != 1
          AND strftime('%Y-%m-%d', occurred_at / 1000, 'unixepoch', 'localtime') = :dayStr
    """)
    suspend fun getTodayExpenseMinor(dayStr: String): Long

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun countAll(): Int

    @Query(
        """
        SELECT COALESCE(SUM(amount_minor), 0)
        FROM transactions
        WHERE category_id = :categoryId
          AND is_deleted = 0
          AND transaction_type_id = 2
          AND strftime('%Y-%m', occurred_at / 1000, 'unixepoch', 'localtime') = :monthStr
        """
    )
    suspend fun getMonthlyCategorySpending(categoryId: Int, monthStr: String): Long

    @Query(
        """
        SELECT COUNT(*)
        FROM transactions
        WHERE is_deleted = 0
          AND strftime('%Y-%m-%d', occurred_at / 1000, 'unixepoch', 'localtime') = :dayStr
        """
    )
    suspend fun getTodayTransactionCount(dayStr: String): Int

    /** Income + expense totals for a timestamp range (weekly summary). */
    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN transaction_type_id = 1 THEN amount_minor ELSE 0 END), 0) AS income_minor,
            COALESCE(SUM(CASE WHEN transaction_type_id != 1 THEN amount_minor ELSE 0 END), 0) AS expense_minor
        FROM transactions
        WHERE is_deleted = 0
          AND occurred_at >= :startMillis
          AND occurred_at < :endMillis
        """
    )
    suspend fun getRangeSummary(startMillis: Long, endMillis: Long): RangeSummaryRow

    /** Highest-spending expense category in a timestamp range (weekly summary). */
    @Query(
        """
        SELECT category_id, SUM(amount_minor) AS total_minor
        FROM transactions
        WHERE is_deleted = 0
          AND transaction_type_id != 1
          AND occurred_at >= :startMillis
          AND occurred_at < :endMillis
        GROUP BY category_id
        ORDER BY total_minor DESC
        LIMIT 1
        """
    )
    suspend fun getTopExpenseCategory(startMillis: Long, endMillis: Long): TopCategoryRow?

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("UPDATE transactions SET is_deleted = 1, sync_state = :syncState, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, syncState: String, updatedAt: Long)

    @Query("UPDATE transactions SET is_deleted = 0, sync_state = :syncState, updated_at = :updatedAt WHERE id = :id")
    suspend fun restore(id: String, syncState: String, updatedAt: Long)

    @Query(
        """
        UPDATE transactions
        SET source_recurring_rule_id = :sourceRecurringRuleId,
            content_hash = :contentHash,
            sync_state = :syncState,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateRecurringSourceReference(
        id: String,
        sourceRecurringRuleId: String?,
        contentHash: String?,
        syncState: String,
        updatedAt: Long
    )

    @Query("SELECT * FROM transactions WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<TransactionEntity>

    @Query("UPDATE transactions SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("DELETE FROM transactions WHERE is_deleted = 1 AND sync_state = 'SYNCED' AND updated_at < :threshold")
    suspend fun purgeOldDeleted(threshold: Long)
}
