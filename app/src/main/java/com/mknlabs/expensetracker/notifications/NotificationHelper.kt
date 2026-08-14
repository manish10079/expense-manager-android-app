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
    const val CHANNEL_WEEKLY_REPORTS = "weekly_reports"
    
    const val EXTRA_NAV_DESTINATION = "nav_destination"
    const val DESTINATION_ADD_TRANSACTION = "add_transaction"
    const val DESTINATION_SMS_CHANGE = "sms_change"
    const val DESTINATION_GOALS = "goals"
    const val DESTINATION_BUDGET = "budget"
    const val DESTINATION_ANALYTICS = "analytics"

    /** Notification IDs 1-4 are in use; Smart SMS Import takes 5 (plan §8); goals take 6; recurring take 7; budget group summary takes 8. */
    const val NOTIFICATION_ID_SMS_IMPORT = 5
    const val NOTIFICATION_ID_GOAL_REMINDER = 6
    const val NOTIFICATION_ID_RECURRING_UPDATED = 7
    const val NOTIFICATION_ID_BUDGET_SUMMARY = 8
    const val NOTIFICATION_ID_LARGE_TRANSACTION = 9
    const val NOTIFICATION_ID_WEEKLY_SUMMARY = 10

    /** Offset above every fixed ID for per-goal milestone alerts (goal id hash + base). */
    private const val GOAL_MILESTONE_ID_BASE = 2000

    /** Offset above every fixed ID for per-rule upcoming-bill alerts (rule id hash + window offset). */
    private const val RECURRING_BILL_NOTIFICATION_ID_BASE = 3000

    /**
     * Every budget alert groups under one collapsible shade entry. Children get
     * a per-category ID (base + categoryId + tier offset) so multiple categories
     * never overwrite each other; a single summary notification at
     * [NOTIFICATION_ID_BUDGET_SUMMARY] carries the group.
     */
    const val GROUP_KEY_BUDGET_ALERTS = "budget_alerts_group"

    /** Marker extra riding on every budget-alert child (not the summary). */
    const val EXTRA_BUDGET_CATEGORY_ID = "budget.category_id"

    /**
     * Threshold tiers for budget alerts (notification spec §2): warn at 75%,
     * warn again at 90%, then reached at 100% and exceeded over the limit.
     * Each tier posts to its own ID so crossing a threshold heads-ups the user
     * instead of silently updating the previous warning; [idOffset] spaces the
     * IDs apart per tier (REACHED and EXCEEDED share the top slot — the user is
     * already aware once they hit 100%, so going over only refreshes the alert).
     */
    enum class BudgetAlertTier(val idOffset: Int) {
        WARNING_75(0),
        WARNING_90(100),
        REACHED(200),
        EXCEEDED(200)
    }

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

            // Default importance: weekly reports are informative, not urgent
            // (notification spec: Default for Weekly Reports).
            val weeklyChannel = NotificationChannel(
                CHANNEL_WEEKLY_REPORTS,
                context.getString(R.string.notification_channel_weekly_reports),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_weekly_reports_desc)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(recurringChannel)
            notificationManager.createNotificationChannel(smsImportChannel)
            notificationManager.createNotificationChannel(goalChannel)
            notificationManager.createNotificationChannel(weeklyChannel)
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
            CHANNEL_GOAL_REMINDERS,
            CHANNEL_WEEKLY_REPORTS
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
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_DAILY_REMINDER)
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
            .setDeleteIntent(dismissPendingIntent(context, 1, NotificationAnalytics.TYPE_DAILY_REMINDER))

        postNotification(context, 1, builder, NotificationAnalytics.TYPE_DAILY_REMINDER)
    }

    /**
     * Shows a budget alert for one category. Each category posts to its own
     * stable ID ([BUDGET_NOTIFICATION_ID_BASE] + categoryId + tier offset), so
     * alerts for different categories STACK instead of overwriting each other
     * — and they all join [GROUP_KEY_BUDGET_ALERTS] under one collapsible
     * summary. Posting a higher [tier] cancels the lower-tier alert for the
     * same category (one budget notification per category in the shade), so
     * each threshold crossing (75% → 90% → 100%/exceeded) heads-up is fresh
     * while repeated saves within a tier stay silent. Tapping opens the
     * Budget screen.
     */
    fun showBudgetAlert(context: Context, message: String, categoryId: Int, tier: BudgetAlertTier) {
        val notificationId = budgetAlertIdFor(categoryId, tier)

        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_BUDGET)
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_BUDGET_ALERT)
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
            .setDeleteIntent(dismissPendingIntent(context, notificationId, NotificationAnalytics.TYPE_BUDGET_ALERT))

        with(NotificationManagerCompat.from(context)) {
            try {
                // Replacing a lower tier (e.g. the 75% warning when 90% is
                // crossed) removes the stale one so the shade never holds two
                // budget alerts for the same category.

                cancelLowerBudgetTiers(this, categoryId, tier)
                notify(notificationId, builder.build())
                NotificationAnalytics.logShown(context, NotificationAnalytics.TYPE_BUDGET_ALERT, notificationId)
                // Keep the group summary's category list in sync with the newly
                // posted/updated child.
                refreshBudgetGroupSummary(context)
            } catch (e: SecurityException) { }
        }
    }

    /** Removes any lower-tier alert still posted for the same category. */
    private fun cancelLowerBudgetTiers(nm: NotificationManagerCompat, categoryId: Int, tier: BudgetAlertTier) {
        for (lowerTier in BudgetAlertTier.entries) {
            if (lowerTier.idOffset < tier.idOffset) {
                nm.cancel(budgetAlertIdFor(categoryId, lowerTier))
            }
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
            // One alert per category: if a stale lower-tier child is still
            // visible while its replacement posts, keep only the highest tier
            // (larger notification id = higher tier offset).
            .groupBy { child -> child.notification.extras?.getInt(EXTRA_BUDGET_CATEGORY_ID, -1) ?: -1 }
            .values
            .mapNotNull { group -> group.maxByOrNull { it.id } }
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
            NotificationAnalytics.logShown(context, NotificationAnalytics.TYPE_BUDGET_SUMMARY, NOTIFICATION_ID_BUDGET_SUMMARY)
        }
    }

    /**
     * Stable, unique notification ID per budget category + tier (never collides
     * with the fixed IDs 1-8). The tier offset makes each threshold crossing a
     * distinct alert while keeping all tiers of one category close together.
     */
    private fun budgetAlertIdFor(categoryId: Int, tier: BudgetAlertTier): Int =
        BUDGET_NOTIFICATION_ID_BASE + (categoryId and 0x00FFFFFF) + tier.idOffset

    /** PendingIntent request code for the budget group summary's Open intent. */
    private const val REQUEST_CODE_BUDGET_SUMMARY = 9

    fun showMissedEntryNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_ADD_TRANSACTION)
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_MISSED_ENTRY)
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
            .setDeleteIntent(dismissPendingIntent(context, 3, NotificationAnalytics.TYPE_MISSED_ENTRY))

        postNotification(context, 3, builder, NotificationAnalytics.TYPE_MISSED_ENTRY)
    }

    fun showGoalReminderNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_GOALS)
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_GOAL_REMINDER)
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
            .setDeleteIntent(dismissPendingIntent(context, NOTIFICATION_ID_GOAL_REMINDER, NotificationAnalytics.TYPE_GOAL_REMINDER))

        postNotification(context, NOTIFICATION_ID_GOAL_REMINDER, builder, NotificationAnalytics.TYPE_GOAL_REMINDER)
    }

    fun showGenericNotification(context: Context, title: String, message: String, notificationId: Int = 4) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via onNewIntent
            // when the app is alive in the background, instead of CLEAR_TASK which
            // force-restarts the activity and replays the splash screen.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_HOME)
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_GENERIC)
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
            .setDeleteIntent(dismissPendingIntent(context, notificationId, NotificationAnalytics.TYPE_GENERIC))

        postNotification(context, notificationId, builder, NotificationAnalytics.TYPE_GENERIC)
    }

    /**
     * Large-expense heads-up (notification spec category 3). Fires at save-time
     * when a single expense crosses the user's configured threshold. Posts to
     * the budget channel (financial alert); a fixed ID means consecutive large
     * transactions replace each other rather than stacking.
     */
    fun showLargeTransactionNotification(context: Context, categoryName: String, amountText: String, thresholdText: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_HOME)
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_LARGE_TRANSACTION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID_LARGE_TRANSACTION, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = context.getString(
            R.string.notification_format_large_transaction,
            amountText,
            categoryName,
            thresholdText
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_large_transaction))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDeleteIntent(dismissPendingIntent(context, NOTIFICATION_ID_LARGE_TRANSACTION, NotificationAnalytics.TYPE_LARGE_TRANSACTION))

        postNotification(context, NOTIFICATION_ID_LARGE_TRANSACTION, builder, NotificationAnalytics.TYPE_LARGE_TRANSACTION)
    }

    /**
     * Weekly spending summary (notification spec category 4). Posts to the
     * dedicated Weekly Reports channel; tapping opens the Analytics screen.
     */
    fun showWeeklySummaryNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_ANALYTICS)
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_WEEKLY_SUMMARY)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID_WEEKLY_SUMMARY, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_WEEKLY_REPORTS)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_weekly_summary))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDeleteIntent(dismissPendingIntent(context, NOTIFICATION_ID_WEEKLY_SUMMARY, NotificationAnalytics.TYPE_WEEKLY_SUMMARY))

        postNotification(context, NOTIFICATION_ID_WEEKLY_SUMMARY, builder, NotificationAnalytics.TYPE_WEEKLY_SUMMARY)
    }

    /**
     * Savings-goal milestone / achieved / behind-schedule alert (notification
     * spec category 6). Each goal posts to its own stable ID so alerts from
     * different goals stack; tapping opens the Goals screen.
     */
    fun showGoalMilestoneNotification(context: Context, title: String, message: String, goalId: String) {
        val notificationId = GOAL_MILESTONE_ID_BASE + (goalId.hashCode() and 0x00FFFFFF)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_GOALS)
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_GOAL_MILESTONE)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_GOAL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setDeleteIntent(dismissPendingIntent(context, notificationId, NotificationAnalytics.TYPE_GOAL_MILESTONE))

        postNotification(context, notificationId, builder, NotificationAnalytics.TYPE_GOAL_MILESTONE)
    }

    /**
     * Upcoming-bill advance alert (notification spec category 7). Each rule
     * posts to its own stable ID so different bills stack; the per-window
     * offset means the 7→3→1→due transitions each heads-up fresh, and posting a
     * closer window cancels the earlier one for that bill (one alert per bill
     * in the shade, mirroring the budget-tier pattern).
     */
    fun showUpcomingBillNotification(context: Context, message: String, ruleId: String, windowDays: Int) {
        val windowOffset = when (windowDays) {
            7 -> 0
            3 -> 1
            1 -> 2
            else -> 3 // due date
        }
        val notificationId = recurringBillIdFor(ruleId, windowOffset)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_HOME)
            putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, NotificationAnalytics.TYPE_RECURRING_BILL)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_RECURRING)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_upcoming_bill))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDeleteIntent(dismissPendingIntent(context, notificationId, NotificationAnalytics.TYPE_RECURRING_BILL))

        with(NotificationManagerCompat.from(context)) {
            try {
                // A closer window replaces the earlier one for the same bill.
                for (offset in 0 until windowOffset) {
                    cancel(recurringBillIdFor(ruleId, offset))
                }
                notify(notificationId, builder.build())
                NotificationAnalytics.logShown(context, NotificationAnalytics.TYPE_RECURRING_BILL, notificationId)
            } catch (e: SecurityException) { }
        }
    }

    private fun recurringBillIdFor(ruleId: String, windowOffset: Int): Int =
        RECURRING_BILL_NOTIFICATION_ID_BASE + (ruleId.hashCode() and 0x00FFFFFF) + windowOffset

    /**
     * Posts [builder] and logs an analytics "shown" event, keeping the
     * permission-safe pattern used by every show* method in one place.
     */
    private fun postNotification(
        context: Context,
        notificationId: Int,
        builder: NotificationCompat.Builder,
        analyticsType: String
    ) {
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
                NotificationAnalytics.logShown(context, analyticsType, notificationId)
            } catch (e: SecurityException) {
                // Handle missing permission
            }
        }
    }

    /**
     * Delete intent for the dismiss receiver — lets analytics know when the
     * user swiped the alert away. Only fires on genuine user dismissal.
     */
    private fun dismissPendingIntent(context: Context, notificationId: Int, analyticsType: String): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            notificationId,
            Intent(context, NotificationDismissReceiver::class.java).apply {
                action = NotificationDismissReceiver.ACTION_NOTIFICATION_DISMISSED
                putExtra(NotificationAnalytics.EXTRA_NOTIFICATION_TYPE, analyticsType)
                putExtra(NotificationAnalytics.PARAM_ID, notificationId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private const val DESTINATION_HOME = "home"

    private const val CHANNEL_PREFS_NAME = "notification_channel_settings"
    private const val KEY_LAST_CHANNEL_RESET_VERSION = "last_channel_reset_version_code"
}
