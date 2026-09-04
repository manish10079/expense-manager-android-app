package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE is_deleted = 0 ORDER BY month_start DESC")
    fun observeActiveBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE is_deleted = 0 ORDER BY month_start DESC")
    suspend fun getActiveBudgets(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE month_start = :monthStart AND is_deleted = 0")
    suspend fun getActiveByMonthStart(monthStart: Long): List<BudgetEntity>

    @Query(
        """
        SELECT * FROM budgets
        WHERE (category_id = :categoryId OR category_ids LIKE '%' || :categoryId || '%')
          AND month_start = :monthStart
          AND is_deleted = 0
        LIMIT 1
        """
    )
    suspend fun getActiveByCategoryAndMonthStart(
        categoryId: Int,
        monthStart: Long
    ): BudgetEntity?

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Query("UPDATE budgets SET is_deleted = 1, sync_state = :syncState, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, syncState: String, updatedAt: Long)

    @Query("SELECT * FROM budgets WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<BudgetEntity>

    @Query("UPDATE budgets SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    /**
     * Updates the month_start for a single budget by id.
     * Used when the user changes the month-start-day setting so that
     * existing budgets align with the new custom-month boundaries.
     */
    @Query("UPDATE budgets SET month_start = :newMonthStart, updated_at = :updatedAt, sync_state = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun updateMonthStart(id: String, newMonthStart: Long, updatedAt: Long)

    @Query("DELETE FROM budgets WHERE is_deleted = 1 AND sync_state = 'SYNCED' AND updated_at < :threshold")
    suspend fun purgeOldDeleted(threshold: Long)
}
