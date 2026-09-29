package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.dao.FavoriteTransactionDao
import com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity
import com.mknlabs.expensetracker.domain.repository.FavoriteTransactionRepository
import com.mknlabs.expensetracker.models.FavoriteTransaction
import com.mknlabs.expensetracker.models.SyncState
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
        val now = System.currentTimeMillis()
        // A star cleared earlier leaves a tombstone in the row's place, and the unique
        // index on transaction_id would then swallow the insert below. Reviving the
        // tombstone first is what turns re-favoriting a transaction back into a favorite.
        favorite.transactionId?.let { dao.reviveFavoriteByTransactionId(it, now) }
        dao.insertFavorite(
            FavoriteTransactionEntity.fromDomain(favorite).copy(
                updatedAt = now,
                syncState = SyncState.PENDING_UPLOAD
            )
        )
    }

    override suspend fun removeFavorite(favorite: FavoriteTransaction) {
        dao.softDeleteById(favorite.id, System.currentTimeMillis())
    }

    override suspend fun removeFavoriteById(id: String) {
        dao.softDeleteById(id, System.currentTimeMillis())
    }

    override suspend fun removeFavoriteByTransactionId(transactionId: String) {
        dao.softDeleteByTransactionId(transactionId, System.currentTimeMillis())
    }

    override suspend fun togglePin(id: String, isPinned: Boolean) {
        dao.updatePinnedState(id, isPinned, System.currentTimeMillis())
    }
}
