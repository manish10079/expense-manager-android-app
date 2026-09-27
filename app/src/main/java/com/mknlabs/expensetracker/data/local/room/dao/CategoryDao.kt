package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.StoredColorRow
import com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE is_deleted = 0 ORDER BY transaction_type_id ASC, sort_order ASC, name ASC")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY transaction_type_id ASC, sort_order ASC, name ASC")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE is_deleted = 0 ORDER BY transaction_type_id ASC, sort_order ASC, name ASC")
    suspend fun getActiveCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE is_deleted = 0 AND is_system = 0 ORDER BY transaction_type_id ASC, id DESC")
    fun observeActiveCustomCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT COALESCE(MAX(id), 0) FROM categories")
    suspend fun getMaxId(): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countAll(): Int

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name AND transaction_type_id = :transactionTypeId AND is_deleted = 0 AND is_system = 0 LIMIT 1")
    suspend fun findActiveByNameAndType(name: String, transactionTypeId: Int): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name AND transaction_type_id = :transactionTypeId AND is_deleted = 1 AND is_system = 0 LIMIT 1")
    suspend fun findDeletedByNameAndType(name: String, transactionTypeId: Int): CategoryEntity?

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    /**
     * The id and stored colour of every row, and nothing else.
     *
     * Read by the seeder so it can carry a user's colour through the rewrite. A projection rather
     * than `SELECT *` because the colour is the only column the seeder is not allowed to touch, and
     * a query that returns one column says so.
     */
    @Query("SELECT id, color_hex FROM categories")
    suspend fun getStoredColors(): List<StoredColorRow>

    /**
     * Sets, changes or clears one row's colour, and marks it for upload.
     *
     * **No `is_system = 0` guard, unlike [softDelete].** Recolouring a built-in category is a
     * first-class thing the user may do — that is what makes the palette a starting point rather
     * than a cage — while deleting one is not. The two operations differ on purpose, and the guard
     * is the difference.
     *
     * A targeted `UPDATE` rather than a read-modify-write: a `copy` of the row would have to name
     * every other column to avoid carrying a stale one back over a concurrent change, and this
     * touches exactly the three that should move.
     */
    @Query("UPDATE categories SET color_hex = :colorHex, updated_at = :updatedAt, sync_state = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun updateColorHex(id: Int, colorHex: String?, updatedAt: Long)

    @Query("UPDATE categories SET is_deleted = 1, sync_state = 'PENDING_DELETE', updated_at = :updatedAt WHERE id = :id AND is_system = 0")
    suspend fun softDelete(id: Int, updatedAt: Long)

    @Query("SELECT * FROM categories WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<CategoryEntity>

    @Query("UPDATE categories SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<Int>, syncState: String)

    @Query("""
        SELECT c.* FROM categories c
        LEFT JOIN transactions t ON t.category_id = c.id AND t.is_deleted = 0
            AND t.created_at >= :sinceMillis
        WHERE c.is_deleted = 0 AND c.transaction_type_id = :transactionTypeId
        GROUP BY c.id
        ORDER BY COUNT(t.id) DESC, c.sort_order ASC, c.name ASC
        LIMIT :limit
    """)
    suspend fun getFrequentlyUsedCategories(
        transactionTypeId: Int,
        limit: Int,
        sinceMillis: Long
    ): List<CategoryEntity>
}
