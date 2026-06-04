package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeActiveBudgets(): Flow<List<Budget>>

    suspend fun upsertBudget(budget: Budget): Budget

    suspend fun deleteBudget(id: String)
}
