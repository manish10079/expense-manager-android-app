package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.dao.FavoriteTransactionDao
import com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity
import com.mknlabs.expensetracker.domain.repository.FavoriteTransactionRepository
import com.mknlabs.expensetracker.models.FavoriteTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteTransactionRepositoryImpl @Inject constructor(
    private val dao: FavoriteTransactionDao
) : FavoriteTransactionRepository {

    override fun getAllFavorites(): Flow<List<FavoriteTransaction>> {
        return dao.getAllFavorites().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveFavorite(favorite: FavoriteTransaction) {
        dao.insertFavorite(FavoriteTransactionEntity.fromDomain(favorite))
    }

    override suspend fun removeFavorite(favorite: FavoriteTransaction) {
        dao.deleteFavorite(FavoriteTransactionEntity.fromDomain(favorite))
    }

    override suspend fun removeFavoriteById(id: String) {
        dao.deleteFavoriteById(id)
    }

    override suspend fun togglePin(id: String, isPinned: Boolean) {
        dao.updatePinnedState(id, isPinned)
    }
}
