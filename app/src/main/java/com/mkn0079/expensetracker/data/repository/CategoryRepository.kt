package com.mkn0079.expensetracker.data.repository

import android.content.Context
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.toDomain
import com.mkn0079.expensetracker.data.local.room.toEntity
import com.mkn0079.expensetracker.models.CategoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(context: Context) {

    private val dao = ExpenseTrackerDatabase.getInstance(context).categoryDao()

    fun observeActiveCategories(): Flow<List<CategoryType>> {
        return dao.observeActiveCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observeActiveCustomCategories(): Flow<List<CategoryType>> {
        return dao.observeActiveCustomCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createCustomCategory(
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
                updatedAt = now
            ).toEntity()
        )
    }

    suspend fun deleteCustomCategory(id: Int) {
        dao.softDelete(id = id, updatedAt = System.currentTimeMillis())
    }
}
