package com.mknlabs.expensetracker.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferPromptStoreTest {

    @Test
    fun showsWhenNeverShown() {
        assertTrue(ReferPromptStore.shouldShow(0L, nowMillis = 1_000L))
    }

    @Test
    fun hidesUntilSevenDays() {
        val day = 86_400_000L
        val shown = 1_000L
        assertFalse(ReferPromptStore.shouldShow(shown, nowMillis = shown + 6 * day))
        assertTrue(ReferPromptStore.shouldShow(shown, nowMillis = shown + 7 * day))
    }
}
