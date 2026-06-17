package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {

    @Query("SELECT * FROM recurring_rules WHERE is_deleted = 0 ORDER BY is_enabled DESC, next_run_at ASC")
    fun observeActiveRecurringRules(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE is_deleted = 0 ORDER BY is_enabled DESC, next_run_at ASC")
    suspend fun getActiveRules(): List<RecurringRuleEntity>

    @Query("SELECT * FROM recurring_rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecurringRuleEntity?

    @Query("SELECT * FROM recurring_rules WHERE transaction_id = :transactionId AND is_deleted = 0 LIMIT 1")
    suspend fun getActiveByTransactionId(transactionId: String): RecurringRuleEntity?

    @Upsert
    suspend fun upsert(rule: RecurringRuleEntity)

    @Query("UPDATE recurring_rules SET is_enabled = :enabled, updated_at = :updatedAt WHERE id = :id AND is_deleted = 0")
    suspend fun updateEnabled(id: String, enabled: Boolean, updatedAt: Long)

    @Query("UPDATE recurring_rules SET is_deleted = 1, sync_state = :syncState, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, syncState: String, updatedAt: Long)

    @Query("SELECT * FROM recurring_rules WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<RecurringRuleEntity>

    @Query("UPDATE recurring_rules SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    @Query("UPDATE recurring_rules SET sync_state = :syncState")
    suspend fun updateSyncStatesForAll(syncState: String)

    @Query("DELETE FROM recurring_rules")
    suspend fun deleteAll()
}
