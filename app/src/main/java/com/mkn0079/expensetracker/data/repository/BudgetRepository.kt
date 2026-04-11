package com.mkn0079.expensetracker.data.repository

import android.content.Context
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.toDomain
import com.mkn0079.expensetracker.data.local.room.toEntity
import com.mkn0079.expensetracker.models.Budget
import com.mkn0079.expensetracker.models.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class BudgetRepository(context: Context) {

    private val dao = ExpenseTrackerDatabase.getInstance(context).budgetDao()

    fun observeActiveBudgets(): Flow<List<Budget>> {
        return dao.observeActiveBudgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun upsertBudget(budget: Budget): Budget {
        val now = System.currentTimeMillis()
        val resolved = budget.copy(
            id = budget.id.ifBlank { UUID.randomUUID().toString() },
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )
        dao.upsert(resolved.toEntity())
        return resolved
    }

    suspend fun deleteBudget(id: String) {
        dao.softDelete(
            id = id,
            syncState = SyncState.PENDING_DELETE.name,
            updatedAt = System.currentTimeMillis()
        )
    }
}
