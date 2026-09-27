package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeActiveCategories(): Flow<List<CategoryType>>

    fun observeAllCategories(): Flow<List<CategoryType>>

    fun observeActiveCustomCategories(): Flow<List<CategoryType>>

    /**
     * Creates a category the user made, optionally with a colour of their own.
     *
     * [colorHex] is `#RRGGBB`, or null for "no colour of its own" — which is the ordinary
     * case and not an error: a category without one resolves from the palette by id. The
     * parameter is threaded through rather than added later so that the reactivation path
     * inside the implementation can name it in the same change that introduces it; see the
     * comment there for what happens if it does not.
     */
    suspend fun createCustomCategory(
        name: String,
        iconKey: String,
        transactionTypeId: Int,
        colorHex: String? = null
    )

    suspend fun deleteCustomCategory(id: Int)

    suspend fun getFrequentlyUsedCategories(
        transactionTypeId: Int,
        limit: Int,
        sinceMillis: Long
    ): List<CategoryType>
}
