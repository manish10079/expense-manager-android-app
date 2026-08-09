package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.domain.repository.GoalRepository
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: GoalsViewModel
    private lateinit var fakeRepository: FakeGoalRepository

    @Before
    fun setup() {
        fakeRepository = FakeGoalRepository()
        viewModel = GoalsViewModel(goalRepository = fakeRepository)
    }

    @Test
    fun `fundGoal reaching target marks goal completed`() = runTest {
        // Arrange: goal with target 100.00, saved 50.00
        fakeRepository.upsertGoal(goal("g1", targetMinor = 10_000, currentMinor = 5_000))

        // Act: fund another 50.00 -> exactly at target
        viewModel.fundGoal("g1", 50.0)

        // Assert
        val updated = fakeRepository.getGoalById("g1")!!
        assertEquals(10_000, updated.currentAmountMinor)
        assertTrue(updated.isCompleted)
    }

    @Test
    fun `fundGoal overshooting target still completes goal`() = runTest {
        // Arrange: goal with target 100.00, saved 90.00
        fakeRepository.upsertGoal(goal("g1", targetMinor = 10_000, currentMinor = 9_000))

        // Act: fund 50.00 -> overshoots to 140.00
        viewModel.fundGoal("g1", 50.0)

        // Assert
        val updated = fakeRepository.getGoalById("g1")!!
        assertEquals(14_000, updated.currentAmountMinor)
        assertTrue(updated.isCompleted)
    }

    @Test
    fun `fundGoal below target keeps goal incomplete`() = runTest {
        // Arrange: goal with target 100.00, saved 0.00
        fakeRepository.upsertGoal(goal("g1", targetMinor = 10_000, currentMinor = 0))

        // Act: fund 40.00
        viewModel.fundGoal("g1", 40.0)

        // Assert
        val updated = fakeRepository.getGoalById("g1")!!
        assertEquals(4_000, updated.currentAmountMinor)
        assertFalse(updated.isCompleted)
    }

    @Test
    fun `updateGoal raising target above saved amount reopens completed goal`() = runTest {
        // Arrange: goal already completed (saved == target)
        fakeRepository.upsertGoal(goal("g1", targetMinor = 10_000, currentMinor = 10_000, isCompleted = true))

        // Act: raise the target to 200.00
        viewModel.updateGoal("g1", name = "New Car", targetAmount = 200.0, deadlineAtMillis = null, iconKey = "savings")

        // Assert: no longer completed
        val updated = fakeRepository.getGoalById("g1")!!
        assertEquals(20_000, updated.targetAmountMinor)
        assertFalse(updated.isCompleted)
    }

    @Test
    fun `updateGoal with deadline stores it`() = runTest {
        // Arrange
        fakeRepository.upsertGoal(goal("g1", targetMinor = 10_000, currentMinor = 0))
        val deadline = System.currentTimeMillis() + 30L * 86_400_000L

        // Act
        viewModel.updateGoal("g1", name = "New Car", targetAmount = 100.0, deadlineAtMillis = deadline, iconKey = "savings")

        // Assert
        assertEquals(deadline, fakeRepository.getGoalById("g1")!!.deadlineAt)
    }

    @Test
    fun `addGoal stores deadline when provided`() = runTest {
        // Act
        val deadline = System.currentTimeMillis() + 30L * 86_400_000L
        viewModel.addGoal("Vacation", 500.0, deadline)

        // Assert
        val goal = fakeRepository.getAll().single()
        assertEquals("Vacation", goal.name)
        assertEquals(50_000, goal.targetAmountMinor)
        assertEquals(deadline, goal.deadlineAt)
        assertFalse(goal.isCompleted)
    }

    @Test
    fun `addGoal leaves deadline null when not provided`() = runTest {
        // Act
        viewModel.addGoal("Vacation", 500.0, null)

        // Assert
        val goal = fakeRepository.getAll().single()
        assertNull(goal.deadlineAt)
    }

    @Test
    fun `addGoal stores icon when provided`() = runTest {
        // Act
        viewModel.addGoal("Vacation", 500.0, null, iconKey = "pets")

        // Assert
        assertEquals("pets", fakeRepository.getAll().single().iconKey)
    }

    @Test
    fun `updateGoal updates icon`() = runTest {
        // Arrange
        fakeRepository.upsertGoal(goal("g1", targetMinor = 10_000, currentMinor = 0))

        // Act
        viewModel.updateGoal("g1", name = "New Car", targetAmount = 100.0, deadlineAtMillis = null, iconKey = "pets")

        // Assert
        assertEquals("pets", fakeRepository.getGoalById("g1")!!.iconKey)
    }

    private fun goal(
        id: String,
        targetMinor: Long,
        currentMinor: Long,
        isCompleted: Boolean = false
    ): Goal {
        return Goal(
            id = id,
            name = "Test Goal",
            targetAmountMinor = targetMinor,
            currentAmountMinor = currentMinor,
            deadlineAt = null,
            iconKey = "savings",
            colorHex = "#7B61FF",
            isCompleted = isCompleted,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.PENDING_UPLOAD
        )
    }

    private class FakeGoalRepository : GoalRepository {
        private val store = mutableMapOf<String, Goal>()
        private val flow = MutableStateFlow<List<Goal>>(emptyList())

        override fun observeAllGoals(): Flow<List<Goal>> = flow

        override suspend fun getGoalById(id: String): Goal? = store[id]

        override suspend fun upsertGoal(goal: Goal) {
            store[goal.id] = goal
            flow.value = store.values.toList()
        }

        override suspend fun deleteGoal(id: String) {
            store.remove(id)
            flow.value = store.values.toList()
        }

        fun getAll(): List<Goal> = store.values.toList()
    }
}
