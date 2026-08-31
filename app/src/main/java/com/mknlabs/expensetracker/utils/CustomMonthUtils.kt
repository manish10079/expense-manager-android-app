package com.mknlabs.expensetracker.utils

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Centralized utility for custom month boundary calculations.
 *
 * When the user sets a custom month start day (1–28), months are
 * redefined: e.g. day 15 → Aug 15 00:00:00.000 to Sep 14 23:59:59.999.
 *
 * Uses [Calendar] for API 24+ compatibility (no java.time desugaring).
 */
object CustomMonthUtils {

    /** Maximum allowed month start day (28 ensures every cycle has ≥28 days). */
    const val MAX_MONTH_START_DAY = 28

    /** Minimum allowed month start day. */
    const val MIN_MONTH_START_DAY = 1

    /**
     * Returns epoch millis for 00:00:00.000 on the custom month start date
     * that contains [timestamp].
     *
     * Example: if [monthStartDay] is 15 and [timestamp] is Aug 20,
     * returns Aug 15 00:00:00.000.
     */
    fun getStartOfCustomMonth(timestamp: Long, monthStartDay: Int): Long {
        val boundedDay = monthStartDay.coerceIn(MIN_MONTH_START_DAY, MAX_MONTH_START_DAY)
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }

        // If current day-of-month < boundedDay, the custom month started last calendar month
        if (cal.get(Calendar.DAY_OF_MONTH) < boundedDay) {
            cal.add(Calendar.MONTH, -1)
        }

        cal.set(Calendar.DAY_OF_MONTH, boundedDay)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.timeInMillis
    }

    /**
     * Returns epoch millis for 23:59:59.999 on the day before the next
     * custom month cycle starts.
     *
     * Example: if [monthStartDay] is 15 and [timestamp] is Aug 20,
     * returns Sep 14 23:59:59.999.
     */
    fun getEndOfCustomMonth(timestamp: Long, monthStartDay: Int): Long {
        val startMillis = getStartOfCustomMonth(timestamp, monthStartDay)
        val cal = Calendar.getInstance().apply {
            timeInMillis = startMillis
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return cal.timeInMillis
    }

    /**
     * Returns a [Pair] of (startMillis, endMillis) for a custom month cycle
     * relative to [nowMillis].
     *
     * @param nowMillis current timestamp (or any reference timestamp).
     * @param monthStartDay custom month start day (1–28).
     * @param monthOffset offset from current cycle: 0 = current, -1 = previous, +1 = next.
     * @return Pair of (startMillis inclusive, endMillis inclusive).
     */
    fun getCustomMonthRange(
        nowMillis: Long = System.currentTimeMillis(),
        monthStartDay: Int,
        monthOffset: Int = 0
    ): Pair<Long, Long> {
        val baseStart = getStartOfCustomMonth(nowMillis, monthStartDay)
        val cal = Calendar.getInstance().apply {
            timeInMillis = baseStart
            add(Calendar.MONTH, monthOffset)
        }
        val start = cal.timeInMillis
        val end = getEndOfCustomMonth(start, monthStartDay)
        return Pair(start, end)
    }

    /**
     * Returns a human-readable label for the custom month cycle containing [timestamp].
     *
     * Examples:
     * - Day 1 → "September 2026"
     * - Day 15 → "Aug 15 – Sep 14, 2026"
     */
    fun getCustomMonthLabel(timestamp: Long, monthStartDay: Int): String {
        val boundedDay = monthStartDay.coerceIn(MIN_MONTH_START_DAY, MAX_MONTH_START_DAY)

        if (boundedDay == 1) {
            // Standard calendar month — use full month name
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val monthNames = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            return "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
        }

        val startCal = Calendar.getInstance().apply {
            timeInMillis = getStartOfCustomMonth(timestamp, monthStartDay)
        }
        val endCal = Calendar.getInstance().apply {
            timeInMillis = getEndOfCustomMonth(timestamp, monthStartDay)
        }

        val shortMonths = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        val startMonth = shortMonths[startCal.get(Calendar.MONTH)]
        val endMonth = shortMonths[endCal.get(Calendar.MONTH)]
        val endYear = endCal.get(Calendar.YEAR)

        return "$startMonth $boundedDay – $endMonth ${endCal.get(Calendar.DAY_OF_MONTH)}, $endYear"
    }

    /**
     * Returns the number of days in the custom month cycle containing [timestamp].
     */
    fun getDaysInCustomMonth(timestamp: Long, monthStartDay: Int): Int {
        val start = getStartOfCustomMonth(timestamp, monthStartDay)
        val end = getEndOfCustomMonth(timestamp, monthStartDay)
        return TimeUnit.MILLISECONDS.toDays(end - start).toInt() + 1
    }
}
