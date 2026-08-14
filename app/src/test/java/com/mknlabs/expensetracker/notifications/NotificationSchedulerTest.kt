package com.mknlabs.expensetracker.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationSchedulerTest {

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun sundayAt20of(instantMillis: Long): Calendar =
        Calendar.getInstance().apply {
            timeInMillis = instantMillis
            assertEquals(Calendar.SUNDAY, get(Calendar.DAY_OF_WEEK))
            assertEquals(20, get(Calendar.HOUR_OF_DAY))
            assertEquals(0, get(Calendar.MINUTE))
        }

    @Test
    fun `monday noon lands on the upcoming sunday 8pm`() {
        val now = at(2026, Calendar.AUGUST, 17, 12, 0) // Monday
        val delay = NotificationScheduler.nextWeeklyRunDelayMillis(now, 20L * 60 * 60 * 1000)

        assertTrue(delay > TimeUnit.DAYS.toMillis(6))
        assertTrue(delay < TimeUnit.DAYS.toMillis(7))
        sundayAt20of(now + delay)
    }

    @Test
    fun `sunday morning lands on the same day 8pm`() {
        val now = at(2026, Calendar.AUGUST, 23, 10, 0) // Sunday
        val delay = NotificationScheduler.nextWeeklyRunDelayMillis(now, 20L * 60 * 60 * 1000)

        assertTrue(delay > 0)
        assertTrue(delay < TimeUnit.DAYS.toMillis(1))
        sundayAt20of(now + delay)
    }

    @Test
    fun `sunday after 8pm rolls to next sunday`() {
        val now = at(2026, Calendar.AUGUST, 23, 21, 0) // Sunday, after the time
        val delay = NotificationScheduler.nextWeeklyRunDelayMillis(now, 20L * 60 * 60 * 1000)

        assertTrue(delay > TimeUnit.DAYS.toMillis(6))
        assertTrue(delay < TimeUnit.DAYS.toMillis(7))
        sundayAt20of(now + delay)
    }

    @Test
    fun `midnight millis-of-day targets sunday 12am`() {
        val now = at(2026, Calendar.AUGUST, 23, 10, 0) // Sunday
        val delay = NotificationScheduler.nextWeeklyRunDelayMillis(now, 0L)

        val target = Calendar.getInstance().apply { timeInMillis = now + delay }
        assertEquals(Calendar.SUNDAY, target.get(Calendar.DAY_OF_WEEK))
        assertEquals(0, target.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, target.get(Calendar.MINUTE))
        assertTrue(delay > 0)
    }
}
