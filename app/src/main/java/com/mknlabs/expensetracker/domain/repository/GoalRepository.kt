package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeAllGoals(): Flow<List<Goal>>
    suspend fun getGoalById(id: String): Goal?
    suspend fun upsertGoal(goal: Goal)
    suspend fun deleteGoal(id: String)
}
