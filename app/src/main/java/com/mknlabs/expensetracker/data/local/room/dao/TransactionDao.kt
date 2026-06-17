package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity
import com.mknlabs.expensetracker.data.local.room.query.HomeRecentTransactionRow
import com.mknlabs.expensetracker.data.local.room.query.HomeSummaryRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY occurred_at DESC")
    fun observeActiveTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY occurred_at DESC")
    suspend fun getActiveTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Query(
        """
        SELECT
            COALESCE(
                SUM(
                    CASE
                        WHEN transaction_type_id = 1
                         AND strftime('%Y-%m', occurred_at / 1000, 'unixepoch', 'localtime') =
                             strftime('%Y-%m', 'now', 'localtime')
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
                         AND strftime('%Y-%m', occurred_at / 1000, 'unixepoch', 'localtime') =
                             strftime('%Y-%m', 'now', 'localtime')
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
                         AND strftime('%Y-%m-%d', occurred_at / 1000, 'unixepoch', 'localtime') =
                             strftime('%Y-%m-%d', 'now', 'localtime')
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
                         AND strftime('%Y-%m', occurred_at / 1000, 'unixepoch', 'localtime') =
                              strftime('%Y-%m', 'now', '-1 month', 'localtime')
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
                         AND strftime('%Y-%m', occurred_at / 1000, 'unixepoch', 'localtime') =
                              strftime('%Y-%m', 'now', '-1 month', 'localtime')
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
    fun observeHomeSummary(): Flow<HomeSummaryRow>

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
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateRecurringSourceReference(
        id: String,
        sourceRecurringRuleId: String?,
        contentHash: String?,
        updatedAt: Long
    )

    @Query("SELECT * FROM transactions WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<TransactionEntity>

    @Query("UPDATE transactions SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    @Query("UPDATE transactions SET sync_state = :syncState")
    suspend fun updateSyncStatesForAll(syncState: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
