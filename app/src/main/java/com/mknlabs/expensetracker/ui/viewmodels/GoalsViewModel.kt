package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.GoalFundEntryRepository
import com.mknlabs.expensetracker.domain.repository.GoalRepository
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.GoalFundEntry
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.ui.theme.DEFAULT_GOAL_COLOR_HEX
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val goalFundEntryRepository: GoalFundEntryRepository
) : ViewModel() {

    val goals: StateFlow<List<Goal>> = goalRepository.observeAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /** Tracks which goal's fund history is currently expanded (null = all collapsed). */
    private val _expandedGoalHistoryId = MutableStateFlow<String?>(null)
    val expandedGoalHistoryId: StateFlow<String?> = _expandedGoalHistoryId.asStateFlow()

    /** Cache of fund entries per goal, keyed by goal ID. */
    private val _fundEntries = MutableStateFlow<Map<String, List<GoalFundEntry>>>(emptyMap())
    val fundEntries: StateFlow<Map<String, List<GoalFundEntry>>> = _fundEntries.asStateFlow()

    fun toggleGoalHistory(goalId: String) {
        _expandedGoalHistoryId.value = if (_expandedGoalHistoryId.value == goalId) null else goalId
        // Load entries when expanding
        if (_expandedGoalHistoryId.value == goalId) {
            loadFundEntries(goalId)
        }
    }

    private fun loadFundEntries(goalId: String) {
        viewModelScope.launch {
            goalFundEntryRepository.observeEntriesByGoalId(goalId).collect { entries ->
                _fundEntries.value = _fundEntries.value.toMutableMap().apply {
                    put(goalId, entries)
                }
            }
        }
    }

    fun addGoal(
        name: String,
        targetAmount: Double,
        deadlineAtMillis: Long? = null,
        iconKey: String = "savings"
    ) {
        viewModelScope.launch {
            val newGoal = Goal(
                id = UUID.randomUUID().toString(),
                name = name,
                targetAmountMinor = (targetAmount * 100).toLong(),
                currentAmountMinor = 0,
                deadlineAt = deadlineAtMillis,
                iconKey = iconKey,
                colorHex = DEFAULT_GOAL_COLOR_HEX,
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncState = SyncState.PENDING_UPLOAD
            )
            goalRepository.upsertGoal(newGoal)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
            // Clean up expanded state if this goal was expanded
            if (_expandedGoalHistoryId.value == id) {
                _expandedGoalHistoryId.value = null
            }
            _fundEntries.value = _fundEntries.value.toMutableMap().apply {
                remove(id)
            }
        }
    }

    fun fundGoal(id: String, amount: Double) {
        viewModelScope.launch {
            val goal = goalRepository.getGoalById(id) ?: return@launch
            val newCurrentAmount = goal.currentAmountMinor + (amount * 100).toLong()
            val updatedGoal = goal.copy(
                currentAmountMinor = newCurrentAmount,
                // A goal that reaches (or overshoots) its target is automatically completed.
                isCompleted = newCurrentAmount >= goal.targetAmountMinor,
                updatedAt = System.currentTimeMillis(),
                syncState = SyncState.PENDING_UPLOAD
            )
            goalRepository.upsertGoal(updatedGoal)

            // Record the fund entry
            goalFundEntryRepository.insertEntry(
                GoalFundEntry(
                    id = UUID.randomUUID().toString(),
                    goalId = id,
                    amountMinor = (amount * 100).toLong(),
                    note = "",
                    fundedAt = System.currentTimeMillis(),
                    syncState = SyncState.PENDING_UPLOAD
                )
            )

            // Refresh the cached entries if this goal's history is expanded
            if (_expandedGoalHistoryId.value == id) {
                loadFundEntries(id)
            }
        }
    }

    fun updateGoal(
        id: String,
        name: String,
        targetAmount: Double,
        deadlineAtMillis: Long?,
        iconKey: String = "savings"
    ) {
        viewModelScope.launch {
            val goal = goalRepository.getGoalById(id) ?: return@launch
            val newTargetMinor = (targetAmount * 100).toLong()
            val updatedGoal = goal.copy(
                name = name,
                targetAmountMinor = newTargetMinor,
                deadlineAt = deadlineAtMillis,
                iconKey = iconKey,
                // Re-evaluate completion: raising the target above the saved amount re-opens it.
                isCompleted = goal.currentAmountMinor >= newTargetMinor,
                updatedAt = System.currentTimeMillis(),
                syncState = SyncState.PENDING_UPLOAD
            )
            goalRepository.upsertGoal(updatedGoal)
        }
    }
}
