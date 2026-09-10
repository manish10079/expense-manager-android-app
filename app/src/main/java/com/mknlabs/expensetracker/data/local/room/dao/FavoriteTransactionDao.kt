package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTransactionDao {
    @Query("SELECT * FROM favorite_transactions ORDER BY is_pinned DESC, title ASC")
    fun getAllFavorites(): Flow<List<FavoriteTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteTransactionEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteTransactionEntity)

    @Query("DELETE FROM favorite_transactions WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("UPDATE favorite_transactions SET is_pinned = :isPinned WHERE id = :id")
    suspend fun updatePinnedState(id: String, isPinned: Boolean)
}
