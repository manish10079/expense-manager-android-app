package com.mkn0079.expensetracker.utils

import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class DateFormatOption(
    val pattern: String,
    val previewLabel: String
)

data class TimeFormatOption(
    val id: String,
    val label: String,
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
        label = "12-hour",
        previewLabel = "02:30 PM",
        uses24HourClock = false
    ),
    TimeFormatOption(
        id = "24-hour",
        label = "24-hour",
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

fun getTimeFormatPreviewLabel(timeFormat: String): String {
    return supportedTimeFormats.firstOrNull { it.id == timeFormat }?.label ?: timeFormat
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
    referenceTimestamp: Long? = null
): Long {
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = selectedDateMillis
    }
    val referenceCalendar = referenceTimestamp?.let { timestamp ->
        Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
    }
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
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

private fun formatWithPattern(timestamp: Long, pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
}
