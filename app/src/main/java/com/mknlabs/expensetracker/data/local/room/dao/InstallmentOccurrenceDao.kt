package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.InstallmentOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentOccurrenceDao {

    /** Installment list for a rule, in plan order. */
    @Query("SELECT * FROM installment_occurrences WHERE rule_id = :ruleId AND is_deleted = 0 ORDER BY installment_index ASC")
    fun observeByRule(ruleId: String): Flow<List<InstallmentOccurrenceEntity>>

    @Query("SELECT * FROM installment_occurrences WHERE rule_id = :ruleId AND is_deleted = 0 ORDER BY installment_index ASC")
    suspend fun getByRule(ruleId: String): List<InstallmentOccurrenceEntity>

    /**
     * Every occurrence of a rule INCLUDING soft-deleted ones. Used when reviving a
     * plan after a REGULAR -> INSTALLMENT conversion so existing slots are reused
     * rather than re-created.
     */
    @Query("SELECT * FROM installment_occurrences WHERE rule_id = :ruleId ORDER BY installment_index ASC")
    suspend fun getAllByRuleIncludingDeleted(ruleId: String): List<InstallmentOccurrenceEntity>

    @Query("SELECT * FROM installment_occurrences WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): InstallmentOccurrenceEntity?

    /** Unpaid installments ordered by due date — drives overdue checks and auto-generation. */
    @Query("SELECT * FROM installment_occurrences WHERE rule_id = :ruleId AND is_deleted = 0 AND status = 'PENDING' ORDER BY due_at ASC")
    suspend fun getPendingByRule(ruleId: String): List<InstallmentOccurrenceEntity>

    @Query("SELECT COUNT(*) FROM installment_occurrences WHERE rule_id = :ruleId AND is_deleted = 0 AND status = 'PAID'")
    suspend fun countPaid(ruleId: String): Int

    @Query("SELECT COUNT(*) FROM installment_occurrences WHERE rule_id = :ruleId AND is_deleted = 0 AND status = 'SKIPPED'")
    suspend fun countSkipped(ruleId: String): Int

    /** Total actually paid, used to derive the remaining balance. */
    @Query("SELECT COALESCE(SUM(amount_minor), 0) FROM installment_occurrences WHERE rule_id = :ruleId AND is_deleted = 0 AND status = 'PAID'")
    suspend fun sumPaidMinor(ruleId: String): Long

    @Upsert
    suspend fun upsert(occurrence: InstallmentOccurrenceEntity)

    @Upsert
    suspend fun upsertAll(occurrences: List<InstallmentOccurrenceEntity>)

    @Query("UPDATE installment_occurrences SET status = :status, paid_at = :paidAt, transaction_id = :transactionId, sync_state = :syncState, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: String,
        paidAt: Long?,
        transactionId: String?,
        syncState: String,
        updatedAt: Long
    )

    /** Soft-delete every occurrence of a rule (INSTALLMENT -> REGULAR conversion). */
    @Query("UPDATE installment_occurrences SET is_deleted = 1, sync_state = :syncState, updated_at = :updatedAt WHERE rule_id = :ruleId")
    suspend fun softDeleteByRule(ruleId: String, syncState: String, updatedAt: Long)

    /** Revive every previously soft-deleted occurrence (REGULAR -> INSTALLMENT conversion). */
    @Query("UPDATE installment_occurrences SET is_deleted = 0, sync_state = :syncState, updated_at = :updatedAt WHERE rule_id = :ruleId")
    suspend fun reviveByRule(ruleId: String, syncState: String, updatedAt: Long)

    /**
     * Includes soft-deleted rows: a conversion's is_deleted flip must propagate
     * to other devices exactly like any other change (the recurring-rules
     * unsynced query does the same), and a revive back to is_deleted = 0 is
     * uploaded too.
     */
    @Query("SELECT * FROM installment_occurrences WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<InstallmentOccurrenceEntity>

    @Query("UPDATE installment_occurrences SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    @Query("DELETE FROM installment_occurrences WHERE is_deleted = 1 AND sync_state = 'SYNCED' AND updated_at < :threshold")
    suspend fun purgeOldDeleted(threshold: Long)

    @Query("DELETE FROM installment_occurrences WHERE rule_id = :ruleId")
    suspend fun deleteByRule(ruleId: String)

    @Query("DELETE FROM installment_occurrences")
    suspend fun deleteAll()
}
