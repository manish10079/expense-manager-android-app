package com.mknlabs.expensetracker.utils

/**
 * Auto Play-store rating sheet: first ask after 7 days **or** 10 transactions
 * (whichever comes first); if skipped, one more ask after 14 days; never after
 * they open Play or two skips.
 */
object RateAppPrompt {

    const val MIN_USAGE_DAYS = 7
    const val MIN_TRANSACTIONS = 10
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
        val enoughUse = transactionCount >= MIN_TRANSACTIONS || daysUsed >= MIN_USAGE_DAYS
        if (!enoughUse) return false
        if (dismissCount == 0) return true
        if (lastDismissAtMillis <= 0L) return false
        val daysSinceDismiss = (nowMillis - lastDismissAtMillis) / DAY_MS
        return daysSinceDismiss >= RETRY_AFTER_DAYS
    }
}
