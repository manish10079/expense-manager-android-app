package com.mknlabs.expensetracker.utils

import java.util.Calendar
import kotlin.random.Random

/** Home card: unread detections sitting more than a day, shown once per day at a random time. */
object SmsInboxStaleRemind {

    const val STALE_AFTER_MS = 24L * 60 * 60 * 1000
    const val MIN_DELAY_MS = 15L * 60 * 1000
    const val MAX_DELAY_MS = 4L * 60 * 60 * 1000

    fun startOfLocalDay(nowMillis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun endOfLocalDay(nowMillis: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

    fun isEligible(
        unreadCount: Int,
        oldestUnreadCreatedAt: Long?,
        nowMillis: Long,
        dismissedDayStart: Long,
    ): Boolean {
        if (unreadCount <= 0) return false
        if (oldestUnreadCreatedAt == null || oldestUnreadCreatedAt <= 0L) return false
        if (nowMillis - oldestUnreadCreatedAt < STALE_AFTER_MS) return false
        return dismissedDayStart != startOfLocalDay(nowMillis)
    }

    fun needsSchedule(
        eligible: Boolean,
        scheduledShowAt: Long,
        nowMillis: Long,
    ): Boolean {
        if (!eligible) return false
        if (scheduledShowAt <= 0L) return true
        return scheduledShowAt < startOfLocalDay(nowMillis)
    }

    fun pickShowAt(
        nowMillis: Long,
        random: Random = Random.Default,
    ): Long {
        val remaining = (endOfLocalDay(nowMillis) - nowMillis).coerceAtLeast(0L)
        if (remaining <= MIN_DELAY_MS) {
            return nowMillis + remaining / 2
        }
        val window = minOf(MAX_DELAY_MS, remaining)
        val span = (window - MIN_DELAY_MS).coerceAtLeast(1L)
        return nowMillis + MIN_DELAY_MS + random.nextLong(span)
    }

    fun shouldShowCard(
        unreadCount: Int,
        oldestUnreadCreatedAt: Long?,
        nowMillis: Long,
        scheduledShowAt: Long,
        dismissedDayStart: Long,
    ): Boolean {
        if (!isEligible(unreadCount, oldestUnreadCreatedAt, nowMillis, dismissedDayStart)) {
            return false
        }
        if (scheduledShowAt <= 0L) return false
        return nowMillis >= scheduledShowAt
    }
}
