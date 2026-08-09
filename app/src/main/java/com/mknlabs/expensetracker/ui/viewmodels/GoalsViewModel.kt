package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.GoalRepository
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.ui.theme.DEFAULT_GOAL_COLOR_HEX
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    val goals: StateFlow<List<Goal>> = goalRepository.observeAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
