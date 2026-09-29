package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.domain.repository.CategoryRepository as DomainCategoryRepository
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.utils.normalizeColorHexOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val dao: CategoryDao
) : DomainCategoryRepository {

    override fun observeActiveCategories(): Flow<List<CategoryType>> {
        return dao.observeActiveCategories().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeAllCategories(): Flow<List<CategoryType>> {
        return dao.observeAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeActiveCustomCategories(): Flow<List<CategoryType>> {
        return dao.observeActiveCustomCategories().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun createCustomCategory(
        name: String,
        iconKey: String,
        transactionTypeId: Int,
        colorHex: String?
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val color = normalizeColorHexOrNull(colorHex)

        // Check if a deleted category with the same name and type exists
        val deleted = dao.findDeletedByNameAndType(name, transactionTypeId)
        if (deleted != null) {
            // Reactivate the deleted row instead of creating a duplicate.
            //
            // `copy` carries every field it does not name, so anything left out here is
            // silently inherited from the row the user deleted. `colorHex` is named for
            // exactly that reason: without it, recreating a category the user had previously
            // deleted would resurrect the old row's colour and throw away the one they just
            // picked, which reads as "the picker works, except sometimes".
            dao.upsert(
                deleted.copy(
                    iconKey = iconKey,
                    colorHex = color,
                    isDeleted = false,
                    updatedAt = now,
                    syncState = SyncState.PENDING_UPLOAD
                )
            )
            return@withContext
        }

        val nextId = dao.getMaxId() + 1
        dao.upsert(
            CategoryType(
                id = nextId,
                name = name,
                iconKey = iconKey,
                transactionTypeId = transactionTypeId,
                colorHex = color,
                isSystem = false,
                sortOrder = nextId,
                isDeleted = false,
                createdAt = now,
                updatedAt = now,
                syncState = SyncState.PENDING_UPLOAD
            ).toEntity()
        )
    }

    override suspend fun updateCategoryColor(id: Int, colorHex: String?) = withContext(Dispatchers.IO) {
        // Normalised on the way in, exactly as the create path does it, so the column only ever
        // holds a canonical `#RRGGBB` or null whatever the caller supplies. The seeder copies this
        // value straight back onto the row on every launch, so an unnormalised write would be
        // re-persisted nightly rather than corrected.
        dao.updateColorHex(
            id = id,
            colorHex = normalizeColorHexOrNull(colorHex),
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteCustomCategory(id: Int) = withContext(Dispatchers.IO) {
        dao.softDelete(id = id, updatedAt = System.currentTimeMillis())
    }

    override suspend fun getFrequentlyUsedCategories(
        transactionTypeId: Int,
        limit: Int,
        sinceMillis: Long
    ): List<CategoryType> = withContext(Dispatchers.IO) {
        dao.getFrequentlyUsedCategories(transactionTypeId, limit, sinceMillis).map { it.toDomain() }
    }
}
