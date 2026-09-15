package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.RecurringFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Guards the single source of truth for recurring-series date math (shared by
 * the worker and installment planning). Time-zone independent: every value is
 * produced and asserted through the default [Calendar].
 */
class RecurringScheduleCalculatorTest {

    private fun at(year: Int, month: Int, day: Int, hour: Int = 10): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.timeInMillis

    private fun fields(millis: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = millis }

    private fun assertDate(expected: Calendar, actualMillis: Long) {
        val actual = fields(actualMillis)
        assertEquals(expected.get(Calendar.YEAR), actual.get(Calendar.YEAR))
        assertEquals(expected.get(Calendar.MONTH), actual.get(Calendar.MONTH))
        assertEquals(expected.get(Calendar.DAY_OF_MONTH), actual.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun dailyStepsOneDay() {
        val jan15 = at(2024, 1, 15)
        assertDate(
            Calendar.getInstance().apply { timeInMillis = jan15; add(Calendar.DAY_OF_YEAR, 1) },
            RecurringScheduleCalculator.nextOccurrence(jan15, RecurringFrequency.Daily, jan15)
        )
    }

    @Test
    fun weeklyStepsSevenDays() {
        val jan15 = at(2024, 1, 15)
        val next = RecurringScheduleCalculator.nextOccurrence(jan15, RecurringFrequency.Weekly, jan15)
        val dayOfYearDelta = fields(next).get(Calendar.DAY_OF_YEAR) - fields(jan15).get(Calendar.DAY_OF_YEAR)
        assertEquals(7, dayOfYearDelta)
        assertDate(fields(at(2024, 1, 22)), next)
    }

    @Test
    fun monthlyRoundTripsWithinSafeAnchorDay() {
        val anchor = at(2024, 1, 15)
        val feb = RecurringScheduleCalculator.nextOccurrence(anchor, RecurringFrequency.Monthly, anchor)
        assertDate(at(2024, 2, 15).let { fields(it) }, feb)
        assertDate(fields(anchor), RecurringScheduleCalculator.previousOccurrence(feb, RecurringFrequency.Monthly, anchor))
    }

    @Test
    fun monthlyFromEndOfMonthLandsOnFeb28NotMarch() {
        // Jan 31 + 1 month would overflow to Mar 2/3 in plain Calendar math;
        // the preferred-day pin must land it on a real February date.
        val anchor = at(2024, 1, 31)
        val feb = RecurringScheduleCalculator.nextOccurrence(anchor, RecurringFrequency.Monthly, anchor)
        assertDate(fields(at(2024, 2, 28)), feb)
    }

    @Test
    fun monthlyPinsDay31SeriesToThe28thEveryMonth() {
        val anchor = at(2024, 1, 31)
        val dates = RecurringScheduleCalculator.occurrencesFrom(anchor, RecurringFrequency.Monthly, 4)
        val day = dates.map { fields(it).get(Calendar.DAY_OF_MONTH) }
        // Slot 1 is the anchor itself; every stepped slot is pinned to <= 28.
        assertEquals(listOf(31, 28, 28, 28), day)
    }

    @Test
    fun yearlyStepsOneYear() {
        val anchor = at(2024, 3, 15)
        assertDate(fields(at(2025, 3, 15)), RecurringScheduleCalculator.nextOccurrence(anchor, RecurringFrequency.Yearly, anchor))
    }

    @Test
    fun occurrencesFromReturnsCountSlotsInOrderIncludingFirst() {
        val anchor = at(2024, 1, 15)
        val dates = RecurringScheduleCalculator.occurrencesFrom(anchor, RecurringFrequency.Monthly, 3)
        assertEquals(3, dates.size)
        assertEquals(anchor, dates.first())
        assertTrue(dates[0] < dates[1] && dates[1] < dates[2])
        assertDate(fields(at(2024, 2, 15)), dates[1])
        assertDate(fields(at(2024, 3, 15)), dates[2])
    }

    @Test
    fun occurrencesFromNonPositiveCountIsEmpty() {
        val anchor = at(2024, 1, 15)
        assertTrue(RecurringScheduleCalculator.occurrencesFrom(anchor, RecurringFrequency.Monthly, 0).isEmpty())
        assertTrue(RecurringScheduleCalculator.occurrencesFrom(anchor, RecurringFrequency.Monthly, -3).isEmpty())
    }

    @Test
    fun previousIsInverseOfNextWithinSafeAnchorDay() {
        val anchor = at(2024, 5, 20)
        var cursor = anchor
        repeat(6) {
            cursor = RecurringScheduleCalculator.nextOccurrence(cursor, RecurringFrequency.Monthly, anchor)
        }
        repeat(6) {
            cursor = RecurringScheduleCalculator.previousOccurrence(cursor, RecurringFrequency.Monthly, anchor)
        }
        assertDate(fields(anchor), cursor)
    }
}
