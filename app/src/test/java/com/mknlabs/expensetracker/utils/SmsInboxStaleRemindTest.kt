package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SmsInboxStaleRemindTest {

    private val now = 1_700_000_000_000L
    private val dayStart = SmsInboxStaleRemind.startOfLocalDay(now)

    @Test
    fun notEligibleWhenNothingUnread() {
        assertFalse(
            SmsInboxStaleRemind.isEligible(
                unreadCount = 0,
                oldestUnreadCreatedAt = now - SmsInboxStaleRemind.STALE_AFTER_MS * 2,
                nowMillis = now,
                dismissedDayStart = 0L,
            ),
        )
    }

    @Test
    fun notEligibleUntilOlderThanOneDay() {
        assertFalse(
            SmsInboxStaleRemind.isEligible(
                unreadCount = 2,
                oldestUnreadCreatedAt = now - SmsInboxStaleRemind.STALE_AFTER_MS + 1_000L,
                nowMillis = now,
                dismissedDayStart = 0L,
            ),
        )
        assertTrue(
            SmsInboxStaleRemind.isEligible(
                unreadCount = 2,
                oldestUnreadCreatedAt = now - SmsInboxStaleRemind.STALE_AFTER_MS,
                nowMillis = now,
                dismissedDayStart = 0L,
            ),
        )
    }

    @Test
    fun laterBlocksSameDay() {
        assertFalse(
            SmsInboxStaleRemind.isEligible(
                unreadCount = 3,
                oldestUnreadCreatedAt = now - SmsInboxStaleRemind.STALE_AFTER_MS * 2,
                nowMillis = now,
                dismissedDayStart = dayStart,
            ),
        )
    }

    @Test
    fun cardWaitsUntilScheduledTime() {
        val oldest = now - SmsInboxStaleRemind.STALE_AFTER_MS * 2
        assertFalse(
            SmsInboxStaleRemind.shouldShowCard(
                unreadCount = 1,
                oldestUnreadCreatedAt = oldest,
                nowMillis = now,
                scheduledShowAt = now + 60_000L,
                dismissedDayStart = 0L,
            ),
        )
        assertTrue(
            SmsInboxStaleRemind.shouldShowCard(
                unreadCount = 1,
                oldestUnreadCreatedAt = oldest,
                nowMillis = now,
                scheduledShowAt = now,
                dismissedDayStart = 0L,
            ),
        )
    }

    @Test
    fun pickShowAtStaysToday() {
        val at = SmsInboxStaleRemind.pickShowAt(now, Random(1))
        assertTrue(at >= now)
        assertTrue(at <= SmsInboxStaleRemind.endOfLocalDay(now))
        assertEquals(dayStart, SmsInboxStaleRemind.startOfLocalDay(at))
    }
}
