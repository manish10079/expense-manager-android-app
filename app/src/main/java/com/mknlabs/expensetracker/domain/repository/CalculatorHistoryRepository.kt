package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.CalculatorHistoryEntry
import kotlinx.coroutines.flow.Flow

/**
 * Persists completed Normal-mode calculator calculations so users can review
 * (and copy) what they previously computed. Newest entries come first.
 */
interface CalculatorHistoryRepository {

    fun observeHistory(): Flow<List<CalculatorHistoryEntry>>

    /** Records a completed calculation, trimming the oldest entries past the cap. */
    suspend fun addEntry(expression: String, result: String)

    /** Removes every recorded history entry. */
    suspend fun clearHistory()

    /** Removes a specific history entry. */
    suspend fun deleteEntry(timestampMillis: Long)
}
