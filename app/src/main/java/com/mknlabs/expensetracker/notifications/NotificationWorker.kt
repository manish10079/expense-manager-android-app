package com.mknlabs.expensetracker.notifications

import android.content.Context
import androidx.work.*
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.firstName
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val appSettings = AppSettingsDataStore.getAppSettingsFlow(applicationContext).first()

        if (!appSettings.notificationsEnabled) {
            return Result.success()
        }

        // Schedule-only runs never show a notification: they just compute the
        // next window's random time and arm the chain. Used by the heartbeat's
        // safety-net re-arm and by startDailyReminders' first arm.
        if (inputData.getBoolean(INPUT_SCHEDULE_ONLY, false)) {
            scheduleNextRandomNotification(applicationContext, appSettings)
            return Result.success()
        }

        // Personalize the reminder title with the user's first name — but only when
        // they have actually set a name. Guests (default "Guest User") keep the
        // generic title instead of an odd "Time to log an expense, Guest?".
        val profile = UserProfileDataStore.getUserProfileFlow(applicationContext).first()
        val firstName = if (profile.fullName.isNotBlank() && profile.fullName != defaultUserProfile.fullName) {
            profile.firstName()
        } else {
            null
        }

        // Determine phase
        val daysSinceInstall = (System.currentTimeMillis() - appSettings.installDateMillis) / (1000 * 60 * 60 * 24)
        val isZomatoStyle = daysSinceInstall >= 3

        // Generate and show message
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        var finalMessage = DynamicNotificationEngine.generateReminderMessage(applicationContext, isZomatoStyle)

        // Missed Entry Logic for evening runs — follows the user-configured evening window.
        val eveningStart = appSettings.reminderEveningStartHour.coerceIn(0, 23)
        val eveningEnd = appSettings.reminderEveningEndHour.coerceIn(0, 23)
        if (appSettings.missedEntryReminderEnabled &&
            hour in eveningStart..eveningEnd &&
            eveningStart <= eveningEnd
        ) {
            val database = com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase.getInstance(applicationContext)
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayStr = sdf.format(java.util.Date())
            val count = database.transactionDao().getTodayTransactionCount(todayStr)

            if (count == 0) {
                finalMessage = DynamicNotificationEngine.generateMissedEntryMessage(applicationContext)
                NotificationHelper.showMissedEntryNotification(applicationContext, finalMessage)
            } else {
                NotificationHelper.showReminderNotification(applicationContext, finalMessage, firstName)
            }
        } else {
            NotificationHelper.showReminderNotification(applicationContext, finalMessage, firstName)
        }

        // Schedule next random notification
        scheduleNextRandomNotification(applicationContext, appSettings)

        return Result.success()
    }

    private fun scheduleNextRandomNotification(context: Context, appSettings: AppSettings) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val targetWindow = nextWindowFor(hour, appSettings)

        val nextCalendar = Calendar.getInstance()
        if (targetWindow.first <= hour) {
            nextCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        nextCalendar.set(Calendar.HOUR_OF_DAY, targetWindow.first)
        nextCalendar.set(Calendar.MINUTE, 0)
        nextCalendar.set(Calendar.SECOND, 0)

        // Add random jitter within the window (e.g. 0 to 5 hours). The window is
        // user-configurable, so guard against a degenerate (start >= end) config.
        val windowSizeMinutes = ((targetWindow.second - targetWindow.first) * 60).coerceAtLeast(1)
        val randomMinutes = Random.nextInt(0, windowSizeMinutes)

        val delayMillis = (nextCalendar.timeInMillis + (randomMinutes * 60 * 1000)) - System.currentTimeMillis()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(maxOf(0, delayMillis), TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "DailyReminderWork",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * The next reminder window after [hour]: the morning window if we're before
     * it, the evening window if we're in/between the windows, otherwise the next
     * day's morning window. Window hours come from the user's settings.
     */
    private fun nextWindowFor(hour: Int, settings: AppSettings): Pair<Int, Int> {
        val morningStart = settings.reminderMorningStartHour.coerceIn(0, 23)
        val morningEnd = settings.reminderMorningEndHour.coerceIn(0, 23)
        val eveningStart = settings.reminderEveningStartHour.coerceIn(0, 23)
        val eveningEnd = settings.reminderEveningEndHour.coerceIn(0, 23)

        return when {
            hour < morningStart -> Pair(morningStart, morningEnd)   // morning today
            hour < eveningStart -> Pair(eveningStart, eveningEnd)   // evening today
            else -> Pair(morningStart, morningEnd)                  // morning tomorrow
        }
    }

    companion object {
        /** Input-data flag: run without showing a notification, just arm the next window. */
        const val INPUT_SCHEDULE_ONLY = "input_schedule_only"

        /**
         * Fires a sample daily reminder immediately (Settings → Notifications →
         * "Send test notification") so users can preview the message style and
         * confirm notification permission without waiting for a real window.
         */
        fun enqueueTest(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .addTag("daily_reminder")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "TestReminderWork",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
