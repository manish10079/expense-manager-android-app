package com.mknlabs.expensetracker.domain.update

import com.mknlabs.expensetracker.domain.models.UpdateInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    private val checker = UpdateChecker()

    @Test
    fun `no update when Play has nothing`() {
        val decision = checker.evaluate(
            playUpdateAvailable = false,
            info = UpdateInfo(forceUpdate = true)
        )
        assertEquals(UpdateDecision.NoUpdate, decision)
    }

    @Test
    fun `optional update when Play has a build`() {
        val info = UpdateInfo(updateTitle = "A New Version Is Available")
        val decision = checker.evaluate(playUpdateAvailable = true, info = info)
        assertTrue(decision is UpdateDecision.UpdateAvailable)
        assertFalse((decision as UpdateDecision.UpdateAvailable).force)
        assertEquals(info, decision.info)
    }

    @Test
    fun `force update when Play has a build and RC flag is set`() {
        val info = UpdateInfo(forceUpdate = true)
        val decision = checker.evaluate(playUpdateAvailable = true, info = info)
            as UpdateDecision.UpdateAvailable
        assertTrue(decision.force)
    }
}
