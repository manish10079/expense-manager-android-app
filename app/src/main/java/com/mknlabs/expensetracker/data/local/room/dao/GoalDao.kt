package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY is_completed ASC, created_at DESC")
    fun observeAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): GoalEntity?

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM goals WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<GoalEntity>
}
