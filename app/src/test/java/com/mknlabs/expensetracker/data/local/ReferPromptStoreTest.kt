package com.mknlabs.expensetracker.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferPromptStoreTest {

    @Test
    fun hidesUntilSevenDaysFromFirstOpen() {
        val day = 86_400_000L
        val firstOpen = 1_000L
        assertFalse(ReferPromptStore.shouldShow(0L, nowMillis = firstOpen + 6 * day, firstOpenAtMillis = firstOpen))
        assertTrue(ReferPromptStore.shouldShow(0L, nowMillis = firstOpen + 7 * day, firstOpenAtMillis = firstOpen))
    }

    @Test
    fun hidesUntilSevenDaysAfterLastShow() {
        val day = 86_400_000L
        val firstOpen = 1_000L
        val shown = firstOpen + 7 * day
        assertFalse(ReferPromptStore.shouldShow(shown, nowMillis = shown + 6 * day, firstOpenAtMillis = firstOpen))
        assertTrue(ReferPromptStore.shouldShow(shown, nowMillis = shown + 7 * day, firstOpenAtMillis = firstOpen))
    }
}
