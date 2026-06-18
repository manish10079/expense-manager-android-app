package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.annotation.StringRes

data class DateFormatOption(
    val pattern: String,
    val previewLabel: String
)

data class TimeFormatOption(
    val id: String,
    @StringRes val labelRes: Int,
    val previewLabel: String,
    val uses24HourClock: Boolean
)

val supportedDateFormats = listOf(
    DateFormatOption(pattern = "dd/MM/yyyy", previewLabel = "31/12/2026"),
    DateFormatOption(pattern = "MM/dd/yyyy", previewLabel = "12/31/2026"),
    DateFormatOption(pattern = "yyyy-MM-dd", previewLabel = "2026-12-31"),
    DateFormatOption(pattern = "dd-MM-yyyy", previewLabel = "31-12-2026"),
    DateFormatOption(pattern = "MM-dd-yyyy", previewLabel = "12-31-2026"),
    DateFormatOption(pattern = "dd MMM", previewLabel = "31 Dec"),
    DateFormatOption(pattern = "dd MMM yyyy", previewLabel = "31 Dec 2026"),
    DateFormatOption(pattern = "dd MMM, yyyy", previewLabel = "31 Dec, 2026"),
    DateFormatOption(pattern = "MMM dd, yyyy", previewLabel = "Dec 31, 2026"),
    DateFormatOption(pattern = "MMMM dd, yyyy", previewLabel = "December 31, 2026"),
    DateFormatOption(pattern = "dd MMMM yyyy", previewLabel = "31 December 2026"),
    DateFormatOption(pattern = "EEE, dd MMM yyyy", previewLabel = "Thu, 31 Dec 2026"),
    DateFormatOption(pattern = "EEEE, dd MMM yyyy", previewLabel = "Thursday, 31 Dec 2026"),
    DateFormatOption(pattern = "dd.MM.yyyy", previewLabel = "31.12.2026")
)

val supportedTimeFormats = listOf(
    TimeFormatOption(
        id = "12-hour",
        labelRes = com.mknlabs.expensetracker.R.string.label_12_hour,
        previewLabel = "02:30 PM",
        uses24HourClock = false
    ),
    TimeFormatOption(
        id = "24-hour",
        labelRes = com.mknlabs.expensetracker.R.string.label_24_hour,
        previewLabel = "14:30",
        uses24HourClock = true
    )
)

fun getTime24Hour(timestamp: Long): String {
    return formatWithPattern(timestamp, "HH:mm")
}

fun getTime12Hour(timestamp: Long): String {
    return formatWithPattern(timestamp, "hh:mm a")
}

fun getHour(timestamp: Long): String {
    return formatWithPattern(timestamp, "HH")
}

fun getMinutes(timestamp: Long): String {
    return formatWithPattern(timestamp, "mm")
}

fun getDay(timestamp: Long): String {
    return formatWithPattern(timestamp, "dd")
}

fun getMonth(timestamp: Long): String {
    return formatWithPattern(timestamp, "MM")
}

fun getMonthName(timestamp: Long): String {
    return formatWithPattern(timestamp, "MMMM")
}

fun getYear(timestamp: Long): String {
    return formatWithPattern(timestamp, "yyyy")
}

fun getFullDate(
    timestamp: Long,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
): String {
    return formatDate(timestamp, dateFormatPattern)
}

fun getDateTime(
    timestamp: Long,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT
): String {
    return "${formatDate(timestamp, dateFormatPattern)}, ${formatTime(timestamp, timeFormat)}"
}

fun getDayName(timestamp: Long): String {
    return formatWithPattern(timestamp, "EEEE")
}

fun getShortDayName(timestamp: Long): String {
    return formatWithPattern(timestamp, "EEE")
}

fun parseDate(dateString: String, pattern: String): Long? {
    return try {
        SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString)?.time
    } catch (e: Exception) {
        null
    }
}

fun getMonthYear(timestamp: Long): String {
    return formatWithPattern(timestamp, "MMM yyyy")
}

fun getCurrentDateLabel(pattern: String = "d MMM, yy"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
}

fun getIsoDate(timestamp: Long): String {
    return formatWithPattern(timestamp, "yyyy-MM-dd")
}

fun getReadableDate(
    timestamp: Long,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT
): String {
    return "${formatDate(timestamp, dateFormatPattern)}, ${formatTime(timestamp, timeFormat)}"
}

fun formatDate(
    timestamp: Long,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
): String {
    return formatWithPattern(timestamp, dateFormatPattern)
}

fun formatDateWithWeekday(
    timestamp: Long,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
): String {
    return "${formatWithPattern(timestamp, "EEEE")}, ${formatDate(timestamp, dateFormatPattern)}"
}

fun getDateFormatPreviewLabel(pattern: String): String {
    return supportedDateFormats.firstOrNull { it.pattern == pattern }?.previewLabel ?: pattern
}

fun getTimeFormatPreviewLabel(timeFormat: String): Int {
    return supportedTimeFormats.firstOrNull { it.id == timeFormat }?.labelRes ?: 0
}

fun formatTime(
    timestamp: Long,
    timeFormat: String = DEFAULT_TIME_FORMAT
): String {
    return if (timeFormat == "24-hour") {
        getTime24Hour(timestamp)
    } else {
        getTime12Hour(timestamp)
    }
}

fun datePickerSelectionToLocalDateTimestamp(
    selectedDateMillis: Long,
    referenceTimestamp: Long? = null,
    isInputUtc: Boolean = true
): Long {
    val dateCalendar = if (isInputUtc) {
        Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    } else {
        Calendar.getInstance()
    }.apply {
        timeInMillis = selectedDateMillis
    }
    val referenceCalendar = referenceTimestamp?.let { timestamp ->
        Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
    }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, referenceCalendar?.get(Calendar.HOUR_OF_DAY) ?: 12)
        set(Calendar.MINUTE, referenceCalendar?.get(Calendar.MINUTE) ?: 0)
        set(Calendar.SECOND, referenceCalendar?.get(Calendar.SECOND) ?: 0)
        set(Calendar.MILLISECOND, referenceCalendar?.get(Calendar.MILLISECOND) ?: 0)
    }.timeInMillis
}

fun localDateTimestampToDatePickerSelection(timestamp: Long): Long {
    val localCalendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.YEAR, localCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, localCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, localCalendar.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun calculateAge(dobMillis: Long): Int {
    val dob = Calendar.getInstance().apply { timeInMillis = dobMillis }
    val today = Calendar.getInstance()
    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}


data class PickerResult(
    val timestamp: Long? = null,
    val error: String? = null
)

/**
 * Centralized logic for calculating a timestamp from wheel selections.
 * Validates days in month and handles AM/PM conversion.
 */
fun validateAndCalculateTimestamp(
    day: Int,
    month: Int,
    year: Int,
    hour: Int,
    minute: Int,
    amPm: String,
    showDate: Boolean,
    showTime: Boolean
): PickerResult {
    val cal = Calendar.getInstance()
    
    if (showDate) {
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (day > maxDays) {
            return PickerResult(error = "Invalid date for selected month.")
        }
        cal.set(Calendar.DAY_OF_MONTH, day)
    }

    if (showTime) {
        // Convert 12h to 24h
        val hour24 = when {
            amPm == "PM" && hour < 12 -> hour + 12
            amPm == "AM" && hour == 12 -> 0
            else -> hour
        }
        cal.set(Calendar.HOUR_OF_DAY, hour24)
        cal.set(Calendar.MINUTE, minute)
    } else if (showDate) {
        // If only date, set to midnight
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
    }
    
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    
    return PickerResult(timestamp = cal.timeInMillis)
}

private fun formatWithPattern(timestamp: Long, pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
}
