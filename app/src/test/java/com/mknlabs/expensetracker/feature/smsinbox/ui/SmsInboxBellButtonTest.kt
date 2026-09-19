package com.mknlabs.expensetracker.feature.smsinbox.ui

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The bell's badge rule is the only decision behind the Home icon: a wrong answer here is either a
 * "0" badge on a quiet day or a count that silently lies about what is waiting. Both are pinned
 * below, together with the overflow cap that keeps a large count from widening the pill.
 */
class SmsInboxBellButtonTest {

    @Test
    fun `an empty inbox shows no badge at all`() {
        assertNull(smsInboxBadgeCount(0))
    }

    @Test
    fun `a negative count is treated as empty rather than rendered`() {
        assertNull(smsInboxBadgeCount(-3))
    }

    @Test
    fun `the first unread detection shows a count of one`() {
        assertEquals(1, smsInboxBadgeCount(1))
    }

    @Test
    fun `counts under the cap render exactly`() {
        assertEquals(7, smsInboxBadgeCount(7))
        assertEquals(99, smsInboxBadgeCount(99))
    }

    @Test
    fun `the count itself is never truncated, only its rendering is`() {
        // The badge caps what it draws; the true number must survive for accessibility.
        assertEquals(150, smsInboxBadgeCount(150))
    }

    @Test
    fun `the cap starts one past the largest full count`() {
        assertFalse(isSmsInboxBadgeOverflow(MAX_SMS_INBOX_BADGE_COUNT))
        assertTrue(isSmsInboxBadgeOverflow(MAX_SMS_INBOX_BADGE_COUNT + 1))
    }

    @Test
    fun `a bell with nothing on it rests straight, before and after a ring`() {
        assertEquals(0f, bellRingAngle(0f), TOLERANCE)
        assertEquals(0f, bellRingAngle(1f), TOLERANCE)
    }

    @Test
    fun `an out-of-range ring progress is clamped to rest rather than extrapolated`() {
        assertEquals(0f, bellRingAngle(-1f), TOLERANCE)
        assertEquals(0f, bellRingAngle(2f), TOLERANCE)
        assertEquals(1f, badgePulseScale(-1f), TOLERANCE)
        assertEquals(1f, badgePulseScale(2f), TOLERANCE)
    }

    @Test
    fun `the bell swings both ways, within its cap, and every swing is smaller than the last`() {
        val samples = (1..99).map { bellRingAngle(it / 100f) }

        assertTrue(
            "a one-directional nudge would not read as a ring",
            samples.any { it > 0f } && samples.any { it < 0f }
        )
        assertTrue(
            "the swing must never exceed the stated amplitude",
            samples.all { abs(it) <= SMS_INBOX_BELL_RING_MAX_DEGREES }
        )
        val firstSwing = samples.take(40).maxOf { abs(it) }
        val lastSwing = samples.takeLast(40).maxOf { abs(it) }
        assertTrue("the ring has to decay or it looks like a glitch", lastSwing < firstSwing)
    }

    @Test
    fun `a resting badge is exactly its normal size at both ends of the ring`() {
        assertEquals(1f, badgePulseScale(0f), TOLERANCE)
        assertEquals(1f, badgePulseScale(1f), TOLERANCE)
    }

    @Test
    fun `the badge pulses twice, stays legible, and comes back down between pulses`() {
        val samples = (1..99).map { it / 100f to badgePulseScale(it / 100f) }

        assertTrue("the count has to visibly react", samples.any { it.second > 1.05f })
        assertTrue(
            "the pill is sized from typography; it must not balloon",
            samples.all { it.second <= 1f + SMS_INBOX_BADGE_PULSE_MAX }
        )
        val firstPeak = samples.first { it.first == 0.25f }.second
        val betweenPeaks = samples.first { it.first == 0.5f }.second
        assertTrue(
            "two pulses read as a vibration; one long swell reads as a glitch",
            betweenPeaks < firstPeak
        )
    }

    @Test
    fun `a rising count means a detection just arrived`() {
        assertTrue(isNewDetectionArrival(previousCount = 0, newCount = 1))
        assertTrue(isNewDetectionArrival(previousCount = 2, newCount = 3))
        assertTrue(isNewDetectionArrival(previousCount = 2, newCount = 9))
    }

    @Test
    fun `reading cards must never set the bell off again`() {
        assertFalse(isNewDetectionArrival(previousCount = 3, newCount = 2))
        assertFalse(isNewDetectionArrival(previousCount = 1, newCount = 0))
    }

    @Test
    fun `an unchanged count is a re-emission rather than news`() {
        assertFalse(isNewDetectionArrival(previousCount = 4, newCount = 4))
        assertFalse(isNewDetectionArrival(previousCount = 0, newCount = 0))
    }

    private companion object {
        /** Degrees/scale are compared with a tolerance: the curves are trig, not exact. */
        const val TOLERANCE = 0.001f
    }
}
