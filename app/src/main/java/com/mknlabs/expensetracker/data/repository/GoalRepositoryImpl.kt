package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.dao.GoalDao
import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.domain.repository.GoalRepository
import com.mknlabs.expensetracker.models.Goal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun observeAllGoals(): Flow<List<Goal>> {
        return goalDao.observeAllGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGoalById(id: String): Goal? = withContext(Dispatchers.IO) {
        goalDao.getById(id)?.toDomain()
    }

    override suspend fun upsertGoal(goal: Goal) = withContext(Dispatchers.IO) {
        goalDao.upsert(goal.toEntity())
    }

    override suspend fun deleteGoal(id: String) = withContext(Dispatchers.IO) {
        goalDao.deleteById(id)
    }
}
