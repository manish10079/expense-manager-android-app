package com.mknlabs.expensetracker.notifications

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Manual Firebase Analytics tracking for LOCAL notifications (the FCM-driven
 * `notification_open` / `notification_receive` events only cover push, and
 * this app's reminders are all posted by WorkManager — see plan §5.2).
 *
 * Custom events (prefixed `local_notification_`) are logged so each alert's
 * lifecycle is queryable in Firebase: posted, tapped, and swiped away.
 * Every call is wrapped in runCatching so analytics can never crash or
 * interrupt notification delivery.
 */
object NotificationAnalytics {

    // ---- Analytics type labels (one per NotificationHelper.show* method) ----
    const val TYPE_DAILY_REMINDER = "daily_reminder"
    const val TYPE_MISSED_ENTRY = "missed_entry"
    const val TYPE_BUDGET_ALERT = "budget_alert"
    const val TYPE_BUDGET_SUMMARY = "budget_summary"
    const val TYPE_GOAL_REMINDER = "goal_reminder"
    const val TYPE_GENERIC = "generic"
    const val TYPE_LARGE_TRANSACTION = "large_transaction"
    const val TYPE_WEEKLY_SUMMARY = "weekly_summary"
    const val TYPE_GOAL_MILESTONE = "goal_milestone"
    const val TYPE_RECURRING_BILL = "recurring_bill"

    // ---- Event names ----
    const val EVENT_SHOWN = "local_notification_shown"
    const val EVENT_OPENED = "local_notification_opened"
    const val EVENT_DISMISSED = "local_notification_dismissed"

    // ---- Params ----
    const val PARAM_TYPE = "notification_type"
    const val PARAM_ID = "notification_id"

    /** Extra key carried on notification intents so taps can be attributed. */
    const val EXTRA_NOTIFICATION_TYPE = "extra_notification_type"

    /** Posted to the notification shade. */
    fun logShown(context: Context, type: String, notificationId: Int) {
        log(context, EVENT_SHOWN, type, notificationId)
    }

    /** User tapped the notification and the deep-link was handled. */
    fun logOpened(context: Context, type: String) {
        log(context, EVENT_OPENED, type, notificationId = 0)
    }

    /** User swiped the notification away (or "Clear all"). */
    fun logDismissed(context: Context, type: String, notificationId: Int) {
        log(context, EVENT_DISMISSED, type, notificationId)
    }

    private fun log(context: Context, event: String, type: String, notificationId: Int) {
        runCatching {
            val firebase = FirebaseAnalytics.getInstance(context.applicationContext)
            firebase.logEvent(event, Bundle().apply {
                putString(PARAM_TYPE, type)
                putInt(PARAM_ID, notificationId)
            })
        }
    }
}
