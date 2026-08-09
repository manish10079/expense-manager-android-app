package com.mknlabs.expensetracker.notifications

import android.content.Context
import androidx.work.*
import com.mknlabs.expensetracker.workers.GoalReminderWorker
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object NotificationScheduler {

    fun startDailyReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        // Check if already scheduled
        val workInfos = workManager.getWorkInfosForUniqueWork("DailyReminderWork").get()
        if (workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }) {
            return
        }

        // Schedule first run (e.g. within 1 hour or immediate)
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(Random.nextLong(5, 60), TimeUnit.MINUTES)
            .addTag("daily_reminder")
            .build()

        workManager.enqueueUniqueWork(
            "DailyReminderWork",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }

    fun stopDailyReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("DailyReminderWork")
    }

    fun startGoalReminders(context: Context) {
        // Weekly savings-goal nudge, first run after 1 day so new goals get noticed soon.
        val workRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.DAYS)
            .addTag("goal_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "GoalReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun stopGoalReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("GoalReminderWork")
    }
}
