package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTransactionDao {
    // A tombstone is already invisible to the UI, but it stays on disk until the push
    // has carried the deletion to Firestore and the 30-day purge drops it.
    @Query("SELECT * FROM favorite_transactions WHERE is_deleted = 0 ORDER BY is_pinned DESC, title ASC")
    fun getAllFavorites(): Flow<List<FavoriteTransactionEntity>>

    @Query("SELECT * FROM favorite_transactions WHERE transaction_id = :transactionId LIMIT 1")
    suspend fun findByTransactionId(transactionId: String): FavoriteTransactionEntity?

    // IGNORE: the transaction_id unique index makes re-favoriting the same
    // transaction a no-op (keeps the original id, pin, and createdAt). A row that was
    // un-favorited is revived by [reviveFavoriteByTransactionId] first, so the insert
    // never lands on a tombstone the unique index is still holding.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(favorite: FavoriteTransactionEntity)

    // A pulled row replaces whatever is local for that id, tombstone included.
    @Upsert
    suspend fun upsert(favorite: FavoriteTransactionEntity)

    @Query("UPDATE favorite_transactions SET is_deleted = 0, sync_state = 'PENDING_UPLOAD', updated_at = :updatedAt WHERE transaction_id = :transactionId")
    suspend fun reviveFavoriteByTransactionId(transactionId: String, updatedAt: Long)

    @Query("UPDATE favorite_transactions SET is_deleted = 1, sync_state = 'PENDING_DELETE', updated_at = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: String, updatedAt: Long)

    // Clearing a star in the edit screen clears the template of the transaction
    // being saved: that screen knows the transaction, never the favorite's id.
    @Query("UPDATE favorite_transactions SET is_deleted = 1, sync_state = 'PENDING_DELETE', updated_at = :updatedAt WHERE transaction_id = :transactionId")
    suspend fun softDeleteByTransactionId(transactionId: String, updatedAt: Long)

    @Query("UPDATE favorite_transactions SET is_pinned = :isPinned, sync_state = 'PENDING_UPLOAD', updated_at = :updatedAt WHERE id = :id")
    suspend fun updatePinnedState(id: String, isPinned: Boolean, updatedAt: Long)

    @Query("SELECT * FROM favorite_transactions WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<FavoriteTransactionEntity>

    @Query("UPDATE favorite_transactions SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    @Query("DELETE FROM favorite_transactions WHERE is_deleted = 1 AND sync_state = 'SYNCED' AND updated_at < :threshold")
    suspend fun purgeOldDeleted(threshold: Long)
}
