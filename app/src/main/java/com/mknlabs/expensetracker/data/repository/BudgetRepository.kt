package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.data.local.room.dao.BudgetDao
import com.mknlabs.expensetracker.domain.repository.BudgetRepository as DomainBudgetRepository
import com.mknlabs.expensetracker.models.Budget
import com.mknlabs.expensetracker.models.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    private val dao: BudgetDao
) : DomainBudgetRepository {

    override fun observeActiveBudgets(): Flow<List<Budget>> {
        return dao.observeActiveBudgets().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun upsertBudget(budget: Budget): Budget = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val resolved = budget.copy(
            id = budget.id.ifBlank { UUID.randomUUID().toString() },
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )
        dao.upsert(resolved.toEntity())
        resolved
    }

    override suspend fun deleteBudget(id: String) = withContext(Dispatchers.IO) {
        dao.softDelete(
            id = id,
            syncState = SyncState.PENDING_DELETE.name,
            updatedAt = System.currentTimeMillis()
        )
    }
}
