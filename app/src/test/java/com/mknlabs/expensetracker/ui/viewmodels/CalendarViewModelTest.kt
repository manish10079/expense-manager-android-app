package com.mknlabs.expensetracker.ui.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Covers the calendar "today" re-anchoring logic behind [CalendarViewModel.refreshToday].
 * The ViewModel requires an Android Application and the project has no
 * Robolectric/Mockito, so the pure [computeTodayRefresh] helper is the testable
 * seam (same convention as HomeViewModelTest).
 */
class CalendarViewModelTest {

    @Test
    fun `same day returns unchanged state`() {
        val today = date(2026, Calendar.AUGUST, 17)
        val result = computeTodayRefresh(
            currentTodayDate = today,
            currentTodayMonthStart = monthStart(today),
            selectedDate = today,
            displayedMonthStart = monthStart(today),
            newToday = today
        )

        assertFalse(result.changed)
        assertEquals(today, result.todayDate)
        assertEquals(today, result.selectedDate)
        assertEquals(monthStart(today), result.displayedMonthStart)
    }

    @Test
    fun `date change while on today re-anchors selection and month`() {
        val oldToday = date(2026, Calendar.AUGUST, 17)
        val newToday = date(2026, Calendar.AUGUST, 18)
        val result = computeTodayRefresh(
            currentTodayDate = oldToday,
            currentTodayMonthStart = monthStart(oldToday),
            selectedDate = oldToday,
            displayedMonthStart = monthStart(oldToday),
            newToday = newToday
        )

        assertTrue(result.changed)
        assertEquals(newToday, result.todayDate)
        assertEquals(monthStart(newToday), result.todayMonthStart)
        assertEquals(newToday, result.selectedDate)
        assertEquals(monthStart(newToday), result.displayedMonthStart)
    }

    @Test
    fun `date change while on a different day keeps chosen selection and month`() {
        val oldToday = date(2026, Calendar.AUGUST, 17)
        val newToday = date(2026, Calendar.AUGUST, 18)
        val chosenDay = date(2026, Calendar.AUGUST, 5)
        val result = computeTodayRefresh(
            currentTodayDate = oldToday,
            currentTodayMonthStart = monthStart(oldToday),
            selectedDate = chosenDay,
            displayedMonthStart = monthStart(oldToday),
            newToday = newToday
        )

        assertTrue(result.changed)
        assertEquals(newToday, result.todayDate)
        assertEquals(chosenDay, result.selectedDate)
        assertEquals(monthStart(oldToday), result.displayedMonthStart)
    }

    @Test
    fun `month rollover while on today moves the displayed month`() {
        val oldToday = date(2026, Calendar.JULY, 31)
        val newToday = date(2026, Calendar.AUGUST, 1)
        val result = computeTodayRefresh(
            currentTodayDate = oldToday,
            currentTodayMonthStart = monthStart(oldToday),
            selectedDate = oldToday,
            displayedMonthStart = monthStart(oldToday),
            newToday = newToday
        )

        assertTrue(result.changed)
        assertEquals(monthStart(newToday), result.displayedMonthStart)
        assertEquals(newToday, result.selectedDate)
    }

    private fun date(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun monthStart(timestamp: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
