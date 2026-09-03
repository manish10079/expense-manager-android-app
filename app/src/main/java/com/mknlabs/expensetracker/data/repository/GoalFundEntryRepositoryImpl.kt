package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.dao.GoalFundEntryDao
import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.domain.repository.GoalFundEntryRepository
import com.mknlabs.expensetracker.models.GoalFundEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalFundEntryRepositoryImpl @Inject constructor(
    private val goalFundEntryDao: GoalFundEntryDao
) : GoalFundEntryRepository {

    override fun observeEntriesByGoalId(goalId: String): Flow<List<GoalFundEntry>> {
        return goalFundEntryDao.observeEntriesByGoalId(goalId).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun insertEntry(entry: GoalFundEntry) = withContext(Dispatchers.IO) {
        goalFundEntryDao.upsert(entry.toEntity())
    }

    override suspend fun deleteEntry(id: String) = withContext(Dispatchers.IO) {
        goalFundEntryDao.deleteById(id)
    }
}
