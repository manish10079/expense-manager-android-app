package com.mknlabs.expensetracker.notifications

import android.content.Context
import androidx.work.*
import com.mknlabs.expensetracker.workers.GoalReminderWorker
import com.mknlabs.expensetracker.workers.ReminderHeartbeatWorker
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    /**
     * Arms daily reminders with TWO layers:
     *
     * 1. A periodic heartbeat ([ReminderHeartbeatWorker], every hour, KEEP) that
     *    acts as a safety net: whenever the one-time reminder chain dies (crash,
     *    cancellation, force-stop), the next heartbeat re-arms the next window's
     *    reminder. Periodic work is re-scheduled by WorkManager on app launch,
     *    so reminders recover automatically without the user opening settings.
     *
     * 2. An immediate schedule-only arm of [NotificationWorker] so the very
     *    first reminder lands in the next configured window right after the
     *    user enables reminders (no hour-long wait for the first heartbeat).
     */
    fun startDailyReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val heartbeatRequest = PeriodicWorkRequestBuilder<ReminderHeartbeatWorker>(1, TimeUnit.HOURS)
            .addTag("reminder_heartbeat")
            .build()
        workManager.enqueueUniquePeriodicWork(
            "ReminderHeartbeatWork",
            ExistingPeriodicWorkPolicy.KEEP,
            heartbeatRequest
        )

        val armRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workDataOf(NotificationWorker.INPUT_SCHEDULE_ONLY to true))
            .addTag("daily_reminder")
            .build()
        workManager.enqueueUniqueWork(
            "DailyReminderWork",
            ExistingWorkPolicy.KEEP,
            armRequest
        )
    }

    fun stopDailyReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork("DailyReminderWork")
        workManager.cancelUniqueWork("ReminderHeartbeatWork")
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
