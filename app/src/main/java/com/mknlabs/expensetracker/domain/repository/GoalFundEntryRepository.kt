package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.GoalFundEntry
import kotlinx.coroutines.flow.Flow

interface GoalFundEntryRepository {

    fun observeEntriesByGoalId(goalId: String): Flow<List<GoalFundEntry>>

    suspend fun insertEntry(entry: GoalFundEntry)

    suspend fun deleteEntry(id: String)
}
