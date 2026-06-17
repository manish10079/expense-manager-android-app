package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.domain.repository.CategoryRepository as DomainCategoryRepository
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val dao: CategoryDao
) : DomainCategoryRepository {

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
    ) = withContext(Dispatchers.IO) {
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

    override suspend fun deleteCustomCategory(id: Int) = withContext(Dispatchers.IO) {
        dao.softDelete(id = id, updatedAt = System.currentTimeMillis())
    }
}
