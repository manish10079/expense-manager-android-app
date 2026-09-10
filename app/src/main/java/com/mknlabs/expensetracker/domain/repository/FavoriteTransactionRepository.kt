package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.FavoriteTransaction
import kotlinx.coroutines.flow.Flow

interface FavoriteTransactionRepository {
    fun getAllFavorites(): Flow<List<FavoriteTransaction>>
    suspend fun saveFavorite(favorite: FavoriteTransaction)
    suspend fun removeFavorite(favorite: FavoriteTransaction)
    suspend fun removeFavoriteById(id: String)
    suspend fun togglePin(id: String, isPinned: Boolean)
}
