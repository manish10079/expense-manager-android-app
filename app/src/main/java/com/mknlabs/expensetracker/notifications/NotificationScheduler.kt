package com.mknlabs.expensetracker.notifications

import android.content.Context
import androidx.work.*
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.workers.GoalReminderWorker
import com.mknlabs.expensetracker.workers.ReminderHeartbeatWorker
import com.mknlabs.expensetracker.workers.WeeklySummaryWorker
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    /**
     * Arms daily reminders with TWO layers:
     *
     * 1. A periodic heartbeat ([ReminderHeartbeatWorker], every hour, UPDATE)
     *    that acts as a safety net: whenever the one-time reminder chain dies
     *    (crash, cancellation, force-stop), the next heartbeat re-arms the next
     *    window's reminder. Periodic work is re-scheduled by WorkManager on app
     *    launch, so reminders recover automatically without the user opening
     *    settings.
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
            // UPDATE (notification spec WorkManager requirement): keeps the
            // period's spec current; unlike CANCEL_AND_REENQUEUE it never
            // cancels an in-flight run.
            ExistingPeriodicWorkPolicy.UPDATE,
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
        // Daily cadence: the worker detects milestone crossings (25/50/75%),
        // goal achieved and behind-schedule transitions on a daily basis, while
        // the legacy weekly nudge stays gated to once per week internally.
        val workRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .addTag("goal_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "GoalReminderWork",
            // UPDATE per the notification spec's WorkManager requirement.
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun stopGoalReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("GoalReminderWork")
    }

    /**
     * Arms the Sunday weekly summary. A 7-day periodic request with an initial
     * delay until the next Sunday at [AppSettingsDataStore.weeklySummaryTimeMillis]
     * (spec: Sunday 8 PM) — periodic work survives reboot/process death and is
     * re-scheduled on app launch, so no self-chaining fragility.
     */
    suspend fun startWeeklySummary(context: Context) {
        val appSettings = AppSettingsDataStore.getAppSettingsFlow(context).first()
        val delayMillis = nextWeeklyRunDelayMillis(System.currentTimeMillis(), appSettings.weeklySummaryTimeMillis)

        val workRequest = PeriodicWorkRequestBuilder<WeeklySummaryWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag("weekly_summary")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WeeklySummaryWork",
            // UPDATE per the notification spec's WorkManager requirement.
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun stopWeeklySummary(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("WeeklySummaryWork")
    }

    /**
     * Milliseconds until the next Sunday at [millisOfDay] (e.g. 8 PM) strictly
     * after [nowMillis]. Pure so it is unit-testable.
     */
    internal fun nextWeeklyRunDelayMillis(nowMillis: Long, millisOfDay: Long): Long {
        val target = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, (millisOfDay / 3_600_000L).toInt())
            set(Calendar.MINUTE, ((millisOfDay % 3_600_000L) / 60_000L).toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // Advance to the upcoming Sunday (Calendar.SUNDAY == 1).
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
            // Today is Sunday but the time already passed — roll to next week.
            if (timeInMillis <= nowMillis) {
                add(Calendar.DAY_OF_MONTH, 7)
            }
        }
        return target.timeInMillis - nowMillis
    }
}
