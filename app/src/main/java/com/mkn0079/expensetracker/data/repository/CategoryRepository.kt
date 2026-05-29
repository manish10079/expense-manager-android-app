package com.mkn0079.expensetracker.data.repository

import android.content.Context
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.toDomain
import com.mkn0079.expensetracker.data.local.room.toEntity
import com.mkn0079.expensetracker.domain.repository.CategoryRepository as DomainCategoryRepository
import com.mkn0079.expensetracker.models.CategoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.mkn0079.expensetracker.models.SyncState

class CategoryRepository(context: Context) : DomainCategoryRepository {

    private val dao = ExpenseTrackerDatabase.getInstance(context).categoryDao()

    override fun observeActiveCategories(): Flow<List<CategoryType>> {
        return dao.observeActiveCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeActiveCustomCategories(): Flow<List<CategoryType>> {
        return dao.observeActiveCustomCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createCustomCategory(
        name: String,
        iconKey: String,
        transactionTypeId: Int
    ) {
        val now = System.currentTimeMillis()
        val nextId = dao.getMaxId() + 1
        dao.upsert(
            CategoryType(
                id = nextId,
                name = name,
                iconKey = iconKey,
                transactionTypeId = transactionTypeId,
                isSystem = false,
                sortOrder = nextId,
                isDeleted = false,
                createdAt = now,
                updatedAt = now,
                syncState = SyncState.PENDING_UPLOAD
            ).toEntity()
        )
    }

    override suspend fun deleteCustomCategory(id: Int) {
        dao.softDelete(id = id, updatedAt = System.currentTimeMillis())
        dao.updateSyncState(id = id, syncState = SyncState.PENDING_DELETE.name)
    }
}
