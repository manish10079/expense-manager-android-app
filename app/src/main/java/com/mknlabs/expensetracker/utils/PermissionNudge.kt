package com.mknlabs.expensetracker.utils

/**
 * OS dialogs twice (first Home, next launch). Then wait 5–10 days and show
 * the in-app sheet on later launches until the missing permissions are granted.
 */
object PermissionNudge {

    const val MAX_ASKS = 2
    const val CARD_INTERVAL_MIN_DAYS = 5
    const val CARD_INTERVAL_MAX_DAYS = 10

    private const val DAY_MS = 86_400_000L

    fun shouldAsk(
        granted: Boolean,
        askCount: Int,
        lastAskAtMillis: Long,
        nowMillis: Long,
        intervalDays: Int,
        canShowRationale: Boolean,
    ): Boolean {
        if (granted) return false
        if (askCount >= MAX_ASKS) return false
        if (askCount == 0) return true
        if (!canShowRationale) return false
        return true
    }

    fun shouldShowSettingsCard(
        missingNotifications: Boolean,
        missingSms: Boolean,
        notificationAskCount: Int,
        smsAskCount: Int,
        lastOsAskAtMillis: Long,
        lastCardAtMillis: Long,
        nextIntervalDays: Int,
        nowMillis: Long,
        notificationOsFinished: Boolean = notificationAskCount >= MAX_ASKS,
        smsOsFinished: Boolean = smsAskCount >= MAX_ASKS,
    ): Boolean {
        if (!missingNotifications && !missingSms) return false
        if (missingNotifications && !notificationOsFinished) return false
        if (missingSms && !smsOsFinished) return false
        val waitDays = nextIntervalDays.coerceIn(CARD_INTERVAL_MIN_DAYS, CARD_INTERVAL_MAX_DAYS)
        if (lastOsAskAtMillis <= 0L) return false
        return nowMillis - lastOsAskAtMillis >= waitDays * DAY_MS
    }
}
