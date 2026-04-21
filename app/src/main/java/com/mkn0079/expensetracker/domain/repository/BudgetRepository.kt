package com.mkn0079.expensetracker.domain.repository

import com.mkn0079.expensetracker.models.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeActiveBudgets(): Flow<List<Budget>>

    suspend fun upsertBudget(budget: Budget): Budget

    suspend fun deleteBudget(id: String)
}
