package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.data.local.room.dao.RecurringRuleDao
import com.mknlabs.expensetracker.domain.repository.RecurringRuleRepository as DomainRecurringRuleRepository
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Inject

class RecurringRuleRepository @Inject constructor(
    private val dao: RecurringRuleDao
) : DomainRecurringRuleRepository {

    override fun observeActiveRecurringRules(): Flow<List<RecurringTransactionRule>> {
        return dao.observeActiveRecurringRules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActiveRules(): List<RecurringTransactionRule> = withContext(Dispatchers.IO) {
        dao.getActiveRules().map { it.toDomain() }
    }

    override suspend fun getActiveByTransactionId(transactionId: String): RecurringTransactionRule? = withContext(Dispatchers.IO) {
        dao.getActiveByTransactionId(transactionId)?.toDomain()
    }

    override suspend fun upsertRule(rule: RecurringTransactionRule): RecurringTransactionRule = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val resolved = rule.copy(
            id = rule.id.ifBlank { UUID.randomUUID().toString() },
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )
        dao.upsert(resolved.toEntity())
        resolved
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        dao.updateEnabled(
            id = id,
            enabled = enabled,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteRule(id: String) = withContext(Dispatchers.IO) {
        dao.softDelete(
            id = id,
            syncState = SyncState.PENDING_DELETE.name,
            updatedAt = System.currentTimeMillis()
        )
    }
}
