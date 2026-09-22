package com.mknlabs.expensetracker.feature.calendar.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [showsTodayRing] answers one question: should a day cell draw the ring that keeps today
 * identifiable once the selection has moved elsewhere.
 *
 * The grid fills the selected cell with the same accent colour the ring is drawn in, so the two
 * marks can never usefully coexist — cell by cell:
 *
 * | isToday | selected | ring |
 * |---------|----------|------|
 * | yes     | yes      | no   |
 * | yes     | no       | yes  |
 * | no      | yes      | no   |
 * | no      | no       | no   |
 */
class CalendarDayRingTest {

    @Test
    fun `today keeps its ring once another day is selected`() {
        assertTrue(
            "this is the whole point: choosing another day must not erase today's position",
            showsTodayRing(isToday = true, isSelected = false)
        )
    }

    @Test
    fun `the selected today draws no ring`() {
        assertFalse(
            "a ring drawn over the selection's own fill would be invisible, so it is skipped",
            showsTodayRing(isToday = true, isSelected = true)
        )
    }

    @Test
    fun `a selected day that is not today draws no ring`() {
        assertFalse(showsTodayRing(isToday = false, isSelected = true))
    }

    @Test
    fun `an ordinary day draws no ring`() {
        assertFalse(showsTodayRing(isToday = false, isSelected = false))
    }

    @Test
    fun `the ring is drawn only for today, whatever the selection`() {
        val selections = listOf(true, false)

        selections.forEach { isSelected ->
            assertTrue(
                "today must be marked whenever it is not the selection",
                isSelected || showsTodayRing(isToday = true, isSelected = isSelected)
            )
            assertFalse(
                "no other day may ever draw it",
                showsTodayRing(isToday = false, isSelected = isSelected)
            )
        }
    }
}
