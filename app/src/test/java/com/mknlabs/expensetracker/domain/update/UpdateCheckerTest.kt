package com.mknlabs.expensetracker.domain.update

import com.mknlabs.expensetracker.domain.models.UpdateInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    private val checker = UpdateChecker()

    @Test
    fun `no update when remote version code equals installed`() {
        val decision = checker.evaluate(
            currentVersionCode = 223,
            currentVersionName = "2.94.2",
            info = UpdateInfo(latestVersionCode = 223)
        )
        assertEquals(UpdateDecision.NoUpdate, decision)
    }

    @Test
    fun `no update when remote version code is lower`() {
        val decision = checker.evaluate(
            currentVersionCode = 223,
            currentVersionName = "2.94.2",
            info = UpdateInfo(latestVersionCode = 100)
        )
        assertEquals(UpdateDecision.NoUpdate, decision)
    }

    @Test
    fun `no update when no remote values configured`() {
        val decision = checker.evaluate(
            currentVersionCode = 223,
            currentVersionName = "2.94.2",
            info = UpdateInfo()
        )
        assertEquals(UpdateDecision.NoUpdate, decision)
    }

    @Test
    fun `update available when remote version code is higher`() {
        val info = UpdateInfo(latestVersionCode = 224, latestVersion = "2.94.3")
        val decision = checker.evaluate(
            currentVersionCode = 223,
            currentVersionName = "2.94.2",
            info = info
        )
        assertTrue(decision is UpdateDecision.UpdateAvailable)
        assertFalse((decision as UpdateDecision.UpdateAvailable).force)
    }

    @Test
    fun `force update when flag set and newer version exists`() {
        val info = UpdateInfo(latestVersionCode = 224, forceUpdate = true)
        val decision = checker.evaluate(
            currentVersionCode = 223,
            currentVersionName = "2.94.2",
            info = info
        ) as UpdateDecision.UpdateAvailable
        assertTrue(decision.force)
    }

    @Test
    fun `no update when force flag set but versions are equal`() {
        val decision = checker.evaluate(
            currentVersionCode = 223,
            currentVersionName = "2.94.2",
            info = UpdateInfo(latestVersionCode = 223, forceUpdate = true)
        )
        assertEquals(UpdateDecision.NoUpdate, decision)
    }

    @Test
    fun `version name fallback detects newer semver`() {
        val decision = checker.evaluate(
            currentVersionCode = 223,
            currentVersionName = "2.94.2",
            info = UpdateInfo(latestVersion = "2.95.0")
        )
        assertTrue(decision is UpdateDecision.UpdateAvailable)
    }

    @Test
    fun `version name fallback treats equal names as no update`() {
        val decision = checker.evaluate(
            currentVersionCode = 223,
            currentVersionName = "2.94.2",
            info = UpdateInfo(latestVersion = "2.94.2")
        )
        assertEquals(UpdateDecision.NoUpdate, decision)
    }
}