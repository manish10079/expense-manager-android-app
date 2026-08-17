package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the home Savings Goals card hero number: the total saved across all
 * ACTIVE (non-completed) goals. Completed goals must be excluded so the number
 * only reflects what the user is still working towards.
 *
 * Note: this test class exercises the pure aggregation helper
 * [activeGoalsSavedMinor] rather than the ViewModel itself — HomeViewModel
 * requires an Android [android.app.Application] and the project has no
 * Robolectric/Mockito, so the pure-function extraction is the testable seam.
 */
class HomeViewModelTest {

    @Test
    fun `activeGoalsSavedMinor sums only incomplete goals`() {
        val goals = listOf(
            goal("g1", current = 5_000, completed = false),
            goal("g2", current = 12_500, completed = false),
            goal("g3", current = 9_999, completed = true)
        )

        assertEquals(17_500, activeGoalsSavedMinor(goals))
    }

    @Test
    fun `activeGoalsSavedMinor is zero when there are no goals at all`() {
        assertEquals(0L, activeGoalsSavedMinor(emptyList()))
    }

    @Test
    fun `activeGoalsSavedMinor excludes all completed goals`() {
        val goals = listOf(
            goal("g1", current = 3_000, completed = true),
            goal("g2", current = 7_000, completed = true)
        )

        assertEquals(0L, activeGoalsSavedMinor(goals))
    }

    @Test
    fun `activeGoalsSavedMinor handles a single active goal with zero saved`() {
        val goals = listOf(
            goal("g1", current = 0, completed = false)
        )

        assertEquals(0L, activeGoalsSavedMinor(goals))
    }



    private fun goal(
        id: String,
        current: Long,
        completed: Boolean
    ): Goal {
        return Goal(
            id = id,
            name = "Test Goal",
            targetAmountMinor = 10_000,
            currentAmountMinor = current,
            deadlineAt = null,
            iconKey = "savings",
            colorHex = "#7B61FF",
            isCompleted = completed,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.PENDING_UPLOAD
        )
    }
}
