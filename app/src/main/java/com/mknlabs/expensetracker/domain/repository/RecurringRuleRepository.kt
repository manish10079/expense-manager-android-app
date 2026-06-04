package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.RecurringTransactionRule
import kotlinx.coroutines.flow.Flow

interface RecurringRuleRepository {
    fun observeActiveRecurringRules(): Flow<List<RecurringTransactionRule>>

    suspend fun getActiveRules(): List<RecurringTransactionRule>

    suspend fun getActiveByTransactionId(transactionId: String): RecurringTransactionRule?

    suspend fun upsertRule(rule: RecurringTransactionRule): RecurringTransactionRule

    suspend fun setEnabled(id: String, enabled: Boolean)

    suspend fun deleteRule(id: String)
}
