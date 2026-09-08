package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.CalculatorHistoryStore
import com.mknlabs.expensetracker.domain.repository.CalculatorHistoryRepository
import com.mknlabs.expensetracker.models.CalculatorHistoryEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Data-layer [CalculatorHistoryRepository] backed by [CalculatorHistoryStore]. */
@Singleton
class CalculatorHistoryRepositoryImpl @Inject constructor(
    private val store: CalculatorHistoryStore
) : CalculatorHistoryRepository {

    override fun observeHistory(): Flow<List<CalculatorHistoryEntry>> = store.observeHistory()

    override suspend fun addEntry(expression: String, result: String) {
        store.addEntry(expression, result)
    }

    override suspend fun clearHistory() {
        store.clearHistory()
    }
}
