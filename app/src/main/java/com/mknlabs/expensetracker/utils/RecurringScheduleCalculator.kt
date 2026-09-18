package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.RecurringFrequency
import java.util.Calendar
import kotlin.math.min

/**
 * Single source of truth for recurring-series date math.
 *
 * This logic previously existed twice — as `calculateNextRun` and
 * `onePeriodBefore`, both private to `RecurringTransactionWorker`. Anything that
 * needs to walk a schedule (the worker, installment planning, future analytics)
 * now shares one implementation, so the forward and backward walks can never
 * disagree.
 *
 * Month/year stepping is anchored to the series' preferred day-of-month, clamped
 * to 1..28 so a rule anchored on the 31st still lands on a real date in every
 * month instead of silently skipping February.
 */
object RecurringScheduleCalculator {

    /** Highest day-of-month that exists in all months, used as the anchor cap. */
    private const val SAFE_ANCHOR_DAY = 28

    /**
     * The occurrence one period after [currentRunAt].
     *
     * @param baseAnchor the series' first occurrence, whose day-of-month is the
     *   preferred day for every subsequent month/year step.
     */
    fun nextOccurrence(
        currentRunAt: Long,
        frequency: RecurringFrequency,
        baseAnchor: Long
    ): Long {
        val next = calendarAt(currentRunAt)
        when (frequency) {
            RecurringFrequency.Daily -> next.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.Weekly -> next.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.Monthly -> {
                next.add(Calendar.MONTH, 1)
                applyPreferredDay(next, baseAnchor, Calendar.MONTH)
            }
            RecurringFrequency.Yearly -> {
                next.add(Calendar.YEAR, 1)
                applyPreferredDay(next, baseAnchor, Calendar.YEAR)
            }
        }
        return next.timeInMillis
    }

    /**
     * The occurrence one period before [currentRunAt] — the exact inverse of
     * [nextOccurrence], used to walk a schedule backwards when backfilling slots
     * that were scheduled but never materialized.
     */
    fun previousOccurrence(
        currentRunAt: Long,
        frequency: RecurringFrequency,
        baseAnchor: Long
    ): Long {
        val previous = calendarAt(currentRunAt)
        when (frequency) {
            RecurringFrequency.Daily -> previous.add(Calendar.DAY_OF_YEAR, -1)
            RecurringFrequency.Weekly -> previous.add(Calendar.WEEK_OF_YEAR, -1)
            RecurringFrequency.Monthly -> {
                previous.add(Calendar.MONTH, -1)
                applyPreferredDay(previous, baseAnchor, Calendar.MONTH)
            }
            RecurringFrequency.Yearly -> {
                previous.add(Calendar.YEAR, -1)
                applyPreferredDay(previous, baseAnchor, Calendar.YEAR)
            }
        }
        return previous.timeInMillis
    }

    /**
     * The next [count] due dates starting at (and including) [firstDueAt],
     * stepped by [frequency].
     *
     * Used to materialize an installment plan's schedule up front so every slot
     * exists to be paid, skipped, or recorded as overdue — rather than being
     * discovered only when the worker happens to run.
     */
    fun occurrencesFrom(
        firstDueAt: Long,
        frequency: RecurringFrequency,
        count: Int,
        baseAnchor: Long = firstDueAt
    ): List<Long> {
        if (count <= 0) return emptyList()
        val dates = ArrayList<Long>(count)
        var current = firstDueAt
        repeat(count) {
            dates += current
            current = nextOccurrence(current, frequency, baseAnchor)
        }
        return dates
    }

    private fun calendarAt(millis: Long): Calendar =
        Calendar.getInstance().apply { timeInMillis = millis }

    /**
     * Re-pins the day-of-month after a month/year step. Calendar rolls an
     * out-of-range day forward (Jan 31 + 1 month becomes Mar 3), so the day is
     * reset explicitly to the anchor's preferred day, capped to what the target
     * month actually has.
     */
    private fun applyPreferredDay(
        target: Calendar,
        baseAnchor: Long,
        field: Int
    ) {
        val anchorDay = calendarAt(baseAnchor).get(Calendar.DAY_OF_MONTH)
        val preferredDay = when (field) {
            Calendar.MONTH -> anchorDay.coerceIn(1, SAFE_ANCHOR_DAY)
            else -> anchorDay
        }
        val maxDay = target.getActualMaximum(Calendar.DAY_OF_MONTH)
        target.set(Calendar.DAY_OF_MONTH, min(preferredDay, maxDay))
    }
}
