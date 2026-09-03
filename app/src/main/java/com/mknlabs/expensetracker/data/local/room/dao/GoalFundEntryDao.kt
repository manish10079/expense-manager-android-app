package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.GoalFundEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalFundEntryDao {

    @Query("SELECT * FROM goal_fund_entries WHERE goal_id = :goalId ORDER BY funded_at DESC")
    fun observeEntriesByGoalId(goalId: String): Flow<List<GoalFundEntryEntity>>

    @Query("SELECT * FROM goal_fund_entries WHERE goal_id = :goalId ORDER BY funded_at DESC")
    suspend fun getEntriesByGoalId(goalId: String): List<GoalFundEntryEntity>

    @Query("SELECT * FROM goal_fund_entries ORDER BY funded_at DESC")
    suspend fun getAllEntries(): List<GoalFundEntryEntity>

    @Upsert
    suspend fun upsert(entry: GoalFundEntryEntity)

    @Query("SELECT * FROM goal_fund_entries WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<GoalFundEntryEntity>

    @Query("UPDATE goal_fund_entries SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    @Query("DELETE FROM goal_fund_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM goal_fund_entries WHERE goal_id = :goalId")
    suspend fun deleteByGoalId(goalId: String)
}
