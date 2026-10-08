package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RateAppPromptTest {

    private val now = 1_700_000_000_000L
    private val day = 86_400_000L

    private fun show(
        tx: Int = 0,
        firstOpen: Long = now,
        completed: Boolean = false,
        dismissCount: Int = 0,
        lastDismiss: Long = 0L,
        at: Long = now
    ) = RateAppPrompt.shouldShow(
        transactionCount = tx,
        firstOpenAtMillis = firstOpen,
        nowMillis = at,
        completed = completed,
        dismissCount = dismissCount,
        lastDismissAtMillis = lastDismiss
    )

    @Test
    fun `shows on seven days or ten transactions whichever first`() {
        assertFalse(show(tx = 9, firstOpen = now - 6 * day))
        assertTrue(show(tx = 0, firstOpen = now - 7 * day))
        assertTrue(show(tx = 10, firstOpen = now))
    }

    @Test
    fun `second ask waits fourteen days after dismiss`() {
        assertFalse(
            show(
                tx = 10,
                dismissCount = 1,
                lastDismiss = now - 13 * day,
                at = now
            )
        )
        assertTrue(
            show(
                tx = 10,
                dismissCount = 1,
                lastDismiss = now - 14 * day,
                at = now
            )
        )
    }

    @Test
    fun `stops after two dismisses or a play submit`() {
        assertFalse(show(tx = 10, dismissCount = 2, lastDismiss = now - 30 * day))
        assertFalse(show(tx = 10, completed = true))
    }
}
