package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM payment_methods WHERE is_deleted = 0 ORDER BY sort_order ASC, name ASC")
    fun observeActivePaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE is_deleted = 0 ORDER BY sort_order ASC, name ASC")
    suspend fun getActivePaymentMethods(): List<PaymentMethodEntity>

    @Query("SELECT * FROM payment_methods WHERE is_deleted = 0 AND is_system = 0 ORDER BY id DESC")
    fun observeActiveCustomPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT COALESCE(MAX(id), 0) FROM payment_methods")
    suspend fun getMaxId(): Int

    @Query("SELECT COUNT(*) FROM payment_methods")
    suspend fun countAll(): Int

    @Query("SELECT * FROM payment_methods WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PaymentMethodEntity?

    @Upsert
    suspend fun upsert(paymentMethod: PaymentMethodEntity)

    @Upsert
    suspend fun upsertAll(paymentMethods: List<PaymentMethodEntity>)

    @Query("UPDATE payment_methods SET is_deleted = 1, sync_state = 'PENDING_DELETE', updated_at = :updatedAt WHERE id = :id AND is_system = 0")
    suspend fun softDelete(id: Int, updatedAt: Long)

    @Query("SELECT * FROM payment_methods WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<PaymentMethodEntity>

    @Query("UPDATE payment_methods SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<Int>, syncState: String)

    /**
     * Returns the most frequently used payment methods based on transaction count.
     * Used by AI voice parser to personalize Gemini prompts.
     */
    @Query("""
        SELECT pm.* FROM payment_methods pm
        LEFT JOIN transactions t ON t.payment_method_id = pm.id AND t.is_deleted = 0
            AND t.created_at >= :sinceMillis
        WHERE pm.is_deleted = 0
        GROUP BY pm.id
        ORDER BY COUNT(t.id) DESC, pm.sort_order ASC, pm.name ASC
        LIMIT :limit
    """)
    suspend fun getFrequentlyUsedPaymentMethods(
        limit: Int,
        sinceMillis: Long
    ): List<PaymentMethodEntity>
}
