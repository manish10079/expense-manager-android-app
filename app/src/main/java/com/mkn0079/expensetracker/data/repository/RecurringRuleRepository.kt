package com.mkn0079.expensetracker.data.repository

import android.content.Context
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.toDomain
import com.mkn0079.expensetracker.data.local.room.toEntity
import com.mkn0079.expensetracker.domain.repository.RecurringRuleRepository as DomainRecurringRuleRepository
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RecurringRuleRepository(context: Context) : DomainRecurringRuleRepository {

    private val dao = ExpenseTrackerDatabase.getInstance(context).recurringRuleDao()

    override fun observeActiveRecurringRules(): Flow<List<RecurringTransactionRule>> {
        return dao.observeActiveRecurringRules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActiveRules(): List<RecurringTransactionRule> {
        return dao.getActiveRules().map { it.toDomain() }
    }

    override suspend fun getActiveByTransactionId(transactionId: String): RecurringTransactionRule? {
        return dao.getActiveByTransactionId(transactionId)?.toDomain()
    }

    override suspend fun upsertRule(rule: RecurringTransactionRule): RecurringTransactionRule {
        val now = System.currentTimeMillis()
        val resolved = rule.copy(
            id = rule.id.ifBlank { UUID.randomUUID().toString() },
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )
        dao.upsert(resolved.toEntity())
        return resolved
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) {
        dao.updateEnabled(
            id = id,
            enabled = enabled,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteRule(id: String) {
        dao.softDelete(
            id = id,
            syncState = SyncState.PENDING_DELETE.name,
            updatedAt = System.currentTimeMillis()
        )
    }
}
