package com.mknlabs.expensetracker.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the in-memory "external activity" lock-suppression window.
 *
 * The window is armed by the UI right before launching an external activity
 * (photo/file picker, browser, system settings), honored only if the app returns
 * within [AppLockPreferences.LOCK_SUPPRESSION_TTL_MS] (private, 5 minutes), and
 * consumed (read + cleared) by the lock check on the foreground return.
 */
class AppLockPreferencesTest {

    private val ttlMillis = 5 * 60_000L

    @Test
    fun suppression_isInactiveByDefault() {
        AppLockPreferences.setLockSuppressed(false)
        assertFalse(AppLockPreferences.isLockSuppressionActive())
    }

    @Test
    fun suppression_isActiveRightAfterBeingArmed() {
        AppLockPreferences.setLockSuppressed(true)
        assertTrue(AppLockPreferences.isLockSuppressionActive())
    }

    @Test
    fun suppression_expiresOnceTheTtlHasPassed() {
        AppLockPreferences.setLockSuppressed(true)
        val armedAt = System.currentTimeMillis()

        assertFalse(AppLockPreferences.isLockSuppressionActive(armedAt + ttlMillis))
    }

    @Test
    fun suppression_isStillActiveJustBeforeTheTtl() {
        AppLockPreferences.setLockSuppressed(true)
        val armedAt = System.currentTimeMillis()

        // Margin of 1s (not 1ms): the arm->capture gap above is sub-millisecond,
        // so a full second before expiry is unambiguously still inside the window
        // and can never straddle the millisecond boundary the assertion sits on.
        assertTrue(AppLockPreferences.isLockSuppressionActive(armedAt + ttlMillis - 1_000))
    }

    @Test
    fun setLockSuppressedFalse_clearsTheWindow() {
        AppLockPreferences.setLockSuppressed(true)
        AppLockPreferences.setLockSuppressed(false)

        assertFalse(AppLockPreferences.isLockSuppressionActive())
    }

    @Test
    fun consume_returnsTrueAndClearsAnActiveWindow() {
        AppLockPreferences.setLockSuppressed(true)

        assertTrue(AppLockPreferences.consumeLockSuppression())
        assertFalse(AppLockPreferences.isLockSuppressionActive())
    }

    @Test
    fun consume_returnsFalseWhenNothingIsArmed() {
        AppLockPreferences.setLockSuppressed(false)

        assertFalse(AppLockPreferences.consumeLockSuppression())
    }

    @Test
    fun consume_clearsAStaleWindowWithoutHonoringIt() {
        AppLockPreferences.setLockSuppressed(true)
        val armedAt = System.currentTimeMillis()

        // A stale window (user never returned within the TTL) is dropped...
        assertFalse(AppLockPreferences.consumeLockSuppression(armedAt + ttlMillis))
        // ...and cleared so it cannot linger into the next foreground cycle.
        assertFalse(AppLockPreferences.isLockSuppressionActive())
    }
}
