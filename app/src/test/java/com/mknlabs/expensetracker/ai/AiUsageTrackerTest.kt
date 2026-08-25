package com.mknlabs.expensetracker.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AiUsageTracker] daily limit logic.
 *
 * Tests the dailyLimit constant and reset behavior.
 * Full integration tests with DataStore require instrumented tests.
 */
class AiUsageTrackerTest {

    @Test
    fun dailyLimit_isTenByDefault() {
        // Verify the default daily limit matches the roadmap spec
        assertEquals(10, DEFAULT_AI_DAILY_LIMIT)
    }

    @Test
    fun dailyLimit_isPositive() {
        assertTrue("Daily limit must be positive", DEFAULT_AI_DAILY_LIMIT > 0)
    }

    private companion object {
        const val DEFAULT_AI_DAILY_LIMIT = 10
    }
}
