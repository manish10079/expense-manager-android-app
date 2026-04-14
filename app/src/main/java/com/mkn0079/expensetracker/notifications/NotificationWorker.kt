package com.mkn0079.expensetracker.notifications

import android.content.Context
import androidx.work.*
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
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

        // Determine phase
        val daysSinceInstall = (System.currentTimeMillis() - appSettings.installDateMillis) / (1000 * 60 * 60 * 24)
        val isZomatoStyle = daysSinceInstall >= 3

        // Generate and show message
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        var finalMessage = DynamicNotificationEngine.generateReminderMessage(isZomatoStyle)

        // Missed Entry Logic for evening runs (5pm - 10pm)
        if (appSettings.missedEntryReminderEnabled && hour >= 17 && hour <= 22) {
            val database = com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase.getInstance(applicationContext)
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val todayStr = sdf.format(java.util.Date())
            val count = database.transactionDao().getTodayTransactionCount(todayStr)

            if (count == 0) {
                finalMessage = DynamicNotificationEngine.generateMissedEntryMessage()
                NotificationHelper.showMissedEntryNotification(applicationContext, finalMessage)
            } else {
                NotificationHelper.showReminderNotification(applicationContext, finalMessage)
            }
        } else {
            NotificationHelper.showReminderNotification(applicationContext, finalMessage)
        }

        // Schedule next random notification
        scheduleNextRandomNotification(applicationContext)

        return Result.success()
    }

    private fun scheduleNextRandomNotification(context: Context) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val targetWindow = if (hour < 14) {
            // It's currently morning or midday, schedule for evening (5 PM - 10 PM)
            Pair(17, 22)
        } else {
            // It's currently evening or night, schedule for next morning (8 AM - 1 PM)
            Pair(8, 13)
        }

        val nextCalendar = Calendar.getInstance()
        if (targetWindow.first <= hour) {
            nextCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        nextCalendar.set(Calendar.HOUR_OF_DAY, targetWindow.first)
        nextCalendar.set(Calendar.MINUTE, 0)
        nextCalendar.set(Calendar.SECOND, 0)

        // Add random jitter within the window (e.g. 0 to 5 hours)
        val windowSizeMinutes = (targetWindow.second - targetWindow.first) * 60
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
}
