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
