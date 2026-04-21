package com.mkn0079.expensetracker.domain.repository

import com.mkn0079.expensetracker.models.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeActiveCategories(): Flow<List<CategoryType>>

    fun observeActiveCustomCategories(): Flow<List<CategoryType>>

    suspend fun createCustomCategory(
        name: String,
        iconKey: String,
        transactionTypeId: Int
    )

    suspend fun deleteCustomCategory(id: Int)
}
