package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE is_deleted = 0 ORDER BY is_completed ASC, created_at DESC")
    fun observeAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE is_deleted = 0")
    suspend fun getActiveGoals(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): GoalEntity?

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Query("UPDATE goals SET is_deleted = 1, sync_state = 'PENDING_DELETE', updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: String, updatedAt: Long)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM goals WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<GoalEntity>

    @Query("UPDATE goals SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)
}
