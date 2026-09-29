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

    /**
     * Sets, changes or clears the colour of **any** category, seeded or user-created.
     *
     * This is the one category write that is deliberately not restricted to the user's own rows.
     * [createCustomCategory] and [deleteCustomCategory] are about rows the user owns outright,
     * whereas a colour is a preference *about* a row: recolouring Bills says "this is how I want to
     * see Bills", not "this is a different category". Keeping that distinction is what let the
     * seeder be taught to carry the colour over instead of the picker being confined to custom
     * rows.
     *
     * [colorHex] is `#RRGGBB`, or null to clear the override and return the row to the palette.
     * Anything unparseable is treated as null rather than stored, so a caller cannot park a value on
     * the row that every reader would then have to defend against.
     */
    suspend fun updateCategoryColor(id: Int, colorHex: String?)

    suspend fun deleteCustomCategory(id: Int)

    suspend fun getFrequentlyUsedCategories(
        transactionTypeId: Int,
        limit: Int,
        sinceMillis: Long
    ): List<CategoryType>
}
