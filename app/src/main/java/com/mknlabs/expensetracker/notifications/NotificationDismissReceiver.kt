package com.mknlabs.expensetracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fired via each notification's [android.app.Notification.Builder.setDeleteIntent]
 * when the user dismisses it (swipe away or "Clear all"). Logs a dismissed
 * event so analytics can distinguish alerts that were ignored from those that
 * were acted on. Only fires on genuine user dismissal — programmatic cancels
 * (e.g. budget tier replacement) never trigger it.
 */
class NotificationDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_NOTIFICATION_DISMISSED) return
        NotificationAnalytics.logDismissed(
            context,
            intent.getStringExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE) ?: "unknown",
            intent.getIntExtra(NotificationAnalytics.PARAM_ID, 0)
        )
    }

    companion object {
        const val ACTION_NOTIFICATION_DISMISSED =
            "com.mknlabs.expensetracker.action.NOTIFICATION_DISMISSED"
    }
}
