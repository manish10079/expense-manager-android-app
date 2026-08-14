package com.mknlabs.expensetracker.notifications

import android.content.Context

/**
 * Tracks whether the user has already been asked for the POST_NOTIFICATIONS
 * permission. Lets the settings screen decide between re-requesting the system
 * dialog (never asked) and jumping straight to the app's notification settings
 * (already denied — the system dialog won't reappear). Not sensitive, so plain
 * SharedPreferences is fine.
 */
object NotificationPermissionPrefs {
    private const val PREFS_NAME = "notification_permission"
    private const val KEY_HAS_REQUESTED = "has_requested_post_notifications"

    fun hasRequested(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_REQUESTED, false)
    }

    fun markRequested(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAS_REQUESTED, true)
            .apply()
    }
}
