package com.mknlabs.expensetracker.utils

/**
 * Auto Play-store rating sheet: never before 7 days of use. After that, a
 * session coin-flip ([SoftPromptSession]) decides whether this launch offers it.
 * If skipped, one more ask after 14 days; never after Play submit or two skips.
 */
object RateAppPrompt {

    const val MIN_USAGE_DAYS = 7
    const val RETRY_AFTER_DAYS = 14
    const val MAX_AUTO_PROMPTS = 2

    private const val DAY_MS = 86_400_000L

    fun shouldShow(
        transactionCount: Int,
        firstOpenAtMillis: Long,
        nowMillis: Long,
        completed: Boolean,
        dismissCount: Int,
        lastDismissAtMillis: Long
    ): Boolean {
        if (completed) return false
        if (dismissCount >= MAX_AUTO_PROMPTS) return false
        val daysUsed = if (firstOpenAtMillis > 0L) {
            (nowMillis - firstOpenAtMillis) / DAY_MS
        } else {
            0L
        }
        if (daysUsed < MIN_USAGE_DAYS) return false
        if (dismissCount == 0) return true
        if (lastDismissAtMillis <= 0L) return false
        val daysSinceDismiss = (nowMillis - lastDismissAtMillis) / DAY_MS
        return daysSinceDismiss >= RETRY_AFTER_DAYS
    }
}

/** ~40% of launches after the 7-day gate, so share/rate are not every open. */
object SoftPromptSession {
    val offerThisLaunch: Boolean = kotlin.random.Random.nextFloat() < 0.4f
}
