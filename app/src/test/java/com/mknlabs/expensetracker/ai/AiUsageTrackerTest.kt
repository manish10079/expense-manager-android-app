package com.mknlabs.expensetracker.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class AiUsageTrackerTest {

    @Test
    fun freeOrGuestDailyLimit_isFour() {
        assertEquals(4, AiUsageTracker.FREE_OR_GUEST_DAILY_LIMIT)
    }

    @Test
    fun proDailyLimit_isTen() {
        assertEquals(10, AiUsageTracker.PRO_DAILY_LIMIT)
    }
}
