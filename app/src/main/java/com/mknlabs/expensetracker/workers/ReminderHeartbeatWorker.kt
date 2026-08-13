package com.mknlabs.expensetracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.notifications.NotificationWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Periodic safety net for daily spend reminders. The actual reminder fires from
 * the one-time [NotificationWorker] chain, which schedules itself window by
 * window. That chain silently dies when a run is cancelled, crashes, or the app
 * is force-stopped — and unlike periodic work, nothing re-arms it afterwards.
 *
 * This worker runs every hour and, whenever no [NotificationWorker] run is
 * pending/running, enqueues a schedule-only run that re-arms the next
 * configured window. WorkManager re-schedules periodic work on app launch, so
 * reminders recover on their own (plan §Reminders/Phase 2).
 */
@HiltWorker
class ReminderHeartbeatWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appSettings = AppSettingsDataStore.getAppSettingsFlow(applicationContext).first()
        if (!appSettings.notificationsEnabled) {
            return@withContext Result.success()
        }

        val workManager = WorkManager.getInstance(applicationContext)
        val chainState = workManager
            .getWorkInfosForUniqueWorkFlow("DailyReminderWork")
            .first()
        val isArmed = chainState.any {
            it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
        }
        if (isArmed) {
            return@withContext Result.success()
        }

        // Chain died — re-arm the next window (schedule-only: computes the next
        // random time in the correct window, never shows immediately).
        val armRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workDataOf(NotificationWorker.INPUT_SCHEDULE_ONLY to true))
            .addTag("daily_reminder")
            .build()
        workManager.enqueueUniqueWork(
            "DailyReminderWork",
            ExistingWorkPolicy.REPLACE,
            armRequest
        )

        Result.success()
    }
}
