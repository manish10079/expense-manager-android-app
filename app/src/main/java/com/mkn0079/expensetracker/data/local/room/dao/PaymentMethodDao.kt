package com.mkn0079.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mkn0079.expensetracker.data.local.room.entities.PaymentMethodEntity
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

    @Upsert
    suspend fun upsert(paymentMethod: PaymentMethodEntity)

    @Upsert
    suspend fun upsertAll(paymentMethods: List<PaymentMethodEntity>)

    @Query("UPDATE payment_methods SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id AND is_system = 0")
    suspend fun softDelete(id: Int, updatedAt: Long)

    @Query("SELECT * FROM payment_methods WHERE sync_state != 'SYNCED' AND sync_state != 'LOCAL_ONLY'")
    suspend fun getUnsynced(): List<PaymentMethodEntity>

    @Query("UPDATE payment_methods SET sync_state = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: Int, syncState: String)
}
