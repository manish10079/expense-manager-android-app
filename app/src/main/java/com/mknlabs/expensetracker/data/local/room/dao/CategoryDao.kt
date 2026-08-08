package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE is_deleted = 0 ORDER BY transaction_type_id ASC, sort_order ASC, name ASC")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

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

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

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
