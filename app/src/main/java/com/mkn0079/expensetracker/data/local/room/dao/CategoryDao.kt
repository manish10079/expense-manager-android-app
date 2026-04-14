package com.mkn0079.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mkn0079.expensetracker.data.local.room.entities.CategoryEntity
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

    @Query("UPDATE categories SET is_deleted = 1, updated_at = :updatedAt WHERE id = :id AND is_system = 0")
    suspend fun softDelete(id: Int, updatedAt: Long)
}
