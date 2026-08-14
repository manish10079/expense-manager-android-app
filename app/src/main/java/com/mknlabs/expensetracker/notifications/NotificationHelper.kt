package com.mknlabs.expensetracker.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.MainActivity
import com.mknlabs.expensetracker.R

object NotificationHelper {

    const val CHANNEL_DAILY_REMINDERS = "daily_reminders"
    const val CHANNEL_BUDGET_ALERTS = "budget_alerts"
    const val CHANNEL_RECURRING = "recurring_transactions"
    const val CHANNEL_SMS_IMPORT = "sms_import"
    const val CHANNEL_GOAL_REMINDERS = "goal_reminders"
    
    const val EXTRA_NAV_DESTINATION = "nav_destination"
    const val DESTINATION_ADD_TRANSACTION = "add_transaction"
    const val DESTINATION_SMS_CHANGE = "sms_change"
    const val DESTINATION_GOALS = "goals"
    const val DESTINATION_BUDGET = "budget"

    /** Notification IDs 1-4 are in use; Smart SMS Import takes 5 (plan §8); goals take 6; recurring take 7; budget group summary takes 8. */
    const val NOTIFICATION_ID_SMS_IMPORT = 5
    const val NOTIFICATION_ID_GOAL_REMINDER = 6
    const val NOTIFICATION_ID_RECURRING_UPDATED = 7
    const val NOTIFICATION_ID_BUDGET_SUMMARY = 8

    /**
     * Every budget alert groups under one collapsible shade entry. Children get
     * a per-category ID (base + categoryId) so multiple categories never
     * overwrite each other; a single summary notification at
     * [NOTIFICATION_ID_BUDGET_SUMMARY] carries the group.
     */
    const val GROUP_KEY_BUDGET_ALERTS = "budget_alerts_group"

    /** Marker extra riding on every budget-alert child (not the summary). */
    const val EXTRA_BUDGET_CATEGORY_ID = "budget.category_id"

    /** Offset above every fixed ID (1-8) for per-category budget alert IDs. */
    private const val BUDGET_NOTIFICATION_ID_BASE = 100

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDERS,
                context.getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_reminders_desc)
            }

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                context.getString(R.string.notification_channel_budget),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_budget_desc)
            }

            // DEFAULT (not LOW) so auto-added recurring transactions are actually
            // noticed by the user. Note: importance is user-settable on existing
            // installs; this only affects channels created from now on.
            val recurringChannel = NotificationChannel(
                CHANNEL_RECURRING,
                context.getString(R.string.notification_channel_recurring),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_recurring_desc)
            }

            // High importance so the Smart SMS Import notification heads-up.
            val smsImportChannel = NotificationChannel(
                CHANNEL_SMS_IMPORT,
                context.getString(R.string.notification_channel_sms_import),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_sms_import_desc)
            }

            // Dedicated channel so Savings Goal nudges are independent from the
            // daily-reminder channel — muting one never mutes the other.
            val goalChannel = NotificationChannel(
                CHANNEL_GOAL_REMINDERS,
                context.getString(R.string.notification_channel_goal_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_goal_reminders_desc)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(recurringChannel)
            notificationManager.createNotificationChannel(smsImportChannel)
            notificationManager.createNotificationChannel(goalChannel)
        }
    }

    /** Whether the app can currently post notifications (API 33+: the POST_NOTIFICATIONS permission). */
    fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /** Opens the system settings page for this app's notifications. */
    fun openAppNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }

    /**
     * Android only applies channel importance when the channel is first created,
     * so settings changes never reach users who installed the app earlier. On
     * every versionCode change this deletes and re-creates the channels so the
     * current settings apply to everyone (one-time per update).
     */
    fun resetChannelsIfVersionChanged(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val prefs = context.getSharedPreferences(CHANNEL_PREFS_NAME, Context.MODE_PRIVATE)
        val lastVersion = prefs.getInt(KEY_LAST_CHANNEL_RESET_VERSION, -1)
        val currentVersion = BuildConfig.VERSION_CODE
        if (lastVersion == currentVersion) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            CHANNEL_DAILY_REMINDERS,
            CHANNEL_BUDGET_ALERTS,
            CHANNEL_RECURRING,
            CHANNEL_SMS_IMPORT,
            CHANNEL_GOAL_REMINDERS
        ).forEach { notificationManager.deleteNotificationChannel(it) }
        createNotificationChannels(context)

        prefs.edit().putInt(KEY_LAST_CHANNEL_RESET_VERSION, currentVersion).apply()
    }

    fun showReminderNotification(context: Context, message: String, userName: String? = null) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_ADD_TRANSACTION)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (userName.isNullOrBlank()) {
            context.getString(R.string.notification_title_daily_reminder)
        } else {
            context.getString(R.string.notification_title_daily_reminder_personalized, userName.trim())
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(1, builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission
            }
        }
    }

    /**
     * Shows a budget alert for one category. Each category posts to its own
     * stable ID ([BUDGET_NOTIFICATION_ID_BASE] + categoryId), so alerts for
     * different categories STACK instead of overwriting each other — and they
     * all join [GROUP_KEY_BUDGET_ALERTS] under one collapsible summary.
     * Updating an existing category (e.g. spending grew from 85% to 95%)
     * refreshes that same notification in place without re-heads-upping.
     * Tapping opens the Budget screen.
     */
    fun showBudgetAlert(context: Context, message: String, categoryId: Int) {
        val notificationId = budgetAlertIdFor(categoryId)

        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_BUDGET)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Marker extra distinguishing children from the group summary (which
        // carries neither the category id nor a content line).
        val markerExtras = Bundle().apply {
            putInt(EXTRA_BUDGET_CATEGORY_ID, categoryId)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_budget_alert))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addExtras(markerExtras)
            .setOnlyAlertOnce(true)
            .setGroup(GROUP_KEY_BUDGET_ALERTS)
            .setGroupSummary(false)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
                // Keep the group summary's category list in sync with the newly
                // posted/updated child.
                refreshBudgetGroupSummary(context)
            } catch (e: SecurityException) { }
        }
    }

    /**
     * Rebuilds the collapsible summary for pending budget alerts. The summary
     * lives at the fixed [NOTIFICATION_ID_BUDGET_SUMMARY] ID while each child
     * keeps its per-category ID; the shade shows ONE entry per group of alerts
     * that expands to the per-category lines. Cancels itself when the last
     * child is gone.
     */
    private fun refreshBudgetGroupSummary(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        val children = runCatching {
            nm.activeNotifications.filter { child ->
                // Children carry the group key AND the category-id marker; the
                // summary notification has neither.
                child.notification.group == GROUP_KEY_BUDGET_ALERTS &&
                    child.notification.extras?.containsKey(EXTRA_BUDGET_CATEGORY_ID) == true
            }
        }.getOrDefault(emptyList())

        if (children.isEmpty()) {
            nm.cancel(NOTIFICATION_ID_BUDGET_SUMMARY)
            return
        }

        val summaryText = context.resources.getQuantityString(
            R.plurals.notification_format_budget_summary,
            children.size,
            children.size
        )

        // Tapping the summary opens the Budget screen.
        val contentIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_BUDGET_SUMMARY,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_NAV_DESTINATION, DESTINATION_BUDGET)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // The summary expands to a per-category list (InboxStyle) mirroring the
        // content lines of each child notification.
        val inboxStyle = NotificationCompat.InboxStyle()
        for (child in children) {
            val line = child.notification.extras
                ?.getString(Notification.EXTRA_TEXT)
                ?.takeIf { it.isNotBlank() }
            if (line != null) inboxStyle.addLine(line)
        }

        val summary = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_budget_alert))
            .setContentText(summaryText)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY_BUDGET_ALERTS)
            .setGroupSummary(true)
            .setOnlyAlertOnce(true)
            .build()

        runCatching {
            nm.notify(NOTIFICATION_ID_BUDGET_SUMMARY, summary)
        }
    }

    /** Stable, unique notification ID per budget category (never collides with the fixed IDs 1-8). */
    private fun budgetAlertIdFor(categoryId: Int): Int =
        BUDGET_NOTIFICATION_ID_BASE + (categoryId and 0x00FFFFFF)

    /** PendingIntent request code for the budget group summary's Open intent. */
    private const val REQUEST_CODE_BUDGET_SUMMARY = 9

    fun showMissedEntryNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_ADD_TRANSACTION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_missed_today))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(3, builder.build())
            } catch (e: SecurityException) { }
        }
    }

    fun showGoalReminderNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_GOALS)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 6, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_GOAL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.title_goal_reminder))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID_GOAL_REMINDER, builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission
            }
        }
    }

    fun showGenericNotification(context: Context, title: String, message: String, notificationId: Int = 4) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, "home")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_RECURRING)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
            } catch (e: SecurityException) { }
        }
    }

    private const val CHANNEL_PREFS_NAME = "notification_channel_settings"
    private const val KEY_LAST_CHANNEL_RESET_VERSION = "last_channel_reset_version_code"
}
