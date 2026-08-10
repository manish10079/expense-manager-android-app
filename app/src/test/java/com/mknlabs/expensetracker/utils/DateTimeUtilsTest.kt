package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateTimeUtilsTest {

    @Test
    fun datePickerSelectionToLocalDateTimestamp_keepsSelectedDayInNegativeOffsetTimezone() {
        withDefaultTimeZone("America/Los_Angeles") {
            val pickerSelection = utcDateMillis(year = 2026, month = Calendar.APRIL, dayOfMonth = 1)

            val normalizedTimestamp = datePickerSelectionToLocalDateTimestamp(pickerSelection)

            assertEquals("2026-04-01", formatDate(normalizedTimestamp, "yyyy-MM-dd"))
        }
    }

    @Test
    fun localDateTimestampToDatePickerSelection_roundTripsInPositiveOffsetTimezone() {
        withDefaultTimeZone("Pacific/Kiritimati") {
            val localTimestamp = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, Calendar.APRIL)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val roundTrippedTimestamp = datePickerSelectionToLocalDateTimestamp(
                localDateTimestampToDatePickerSelection(localTimestamp)
            )

            assertEquals("2026-04-01", formatDate(roundTrippedTimestamp, "yyyy-MM-dd"))
        }
    }

    @Test
    fun datePickerSelectionToLocalDateTimestamp_worksInPositiveOffsetTimezoneWithLocalInput() {
        // This test verifies the fix for the bug reported by the user.
        withDefaultTimeZone("Asia/Karachi") { // UTC+5
            val localTimestamp = Calendar.getInstance().apply {
                set(2026, Calendar.APRIL, 2, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            // Pass isInputUtc = false because we are giving it a local timestamp
            val resultTimestamp = datePickerSelectionToLocalDateTimestamp(
                selectedDateMillis = localTimestamp,
                isInputUtc = false
            )
            
            assertEquals("2026-04-02", formatDate(resultTimestamp, "yyyy-MM-dd"))
        }
    }

    @Test
    fun formatDate_twoDigitYearPattern_rendersLastTwoDigits() {
        // "MMM" renders localized output, so pin both locale and timezone.
        withDefaultLocale(java.util.Locale.US) {
            withDefaultTimeZone("Asia/Karachi") {
                val timestamp = Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2026)
                    set(Calendar.MONTH, Calendar.APRIL)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // "12 Dec 26" style: day + short month + last two digits of the year.
                assertEquals("01 Apr 26", formatDate(timestamp, "dd MMM yy"))
            }
        }
    }

    @Test
    fun daysUntilTimestamp_laterToday_isZero() {
        // A deadline later today (even after midnight) counts as 0 whole days -> "Due today".
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val laterToday = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.timeInMillis

        assertEquals(0L, daysUntilTimestamp(laterToday, now))
    }

    @Test
    fun daysUntilTimestamp_tomorrow_isOne() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tomorrow = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        assertEquals(1L, daysUntilTimestamp(tomorrow, now))
    }

    @Test
    fun daysUntilTimestamp_pastDeadline_isNegative() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        assertEquals(-1L, daysUntilTimestamp(yesterday, now))
    }

    @Test
    fun daysUntilTimestamp_weekAway_isSeven() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val inAWeek = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis

        assertEquals(7L, daysUntilTimestamp(inAWeek, now))
    }

    private fun utcDateMillis(
        year: Int,
        month: Int,
        dayOfMonth: Int
    ): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun withDefaultLocale(
        locale: java.util.Locale,
        block: () -> Unit
    ) {
        val originalLocale = java.util.Locale.getDefault()
        java.util.Locale.setDefault(locale)
        try {
            block()
        } finally {
            java.util.Locale.setDefault(originalLocale)
        }
    }

    private fun withDefaultTimeZone(
        timeZoneId: String,
        block: () -> Unit
    ) {
        val originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(timeZoneId))
        try {
            block()
        } finally {
            TimeZone.setDefault(originalTimeZone)
        }
    }
}
