package com.mknlabs.expensetracker.feature.smsinbox.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.CleanupSmsInboxUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Daily retention pass for the detection inbox (Phase 7).
 *
 * Runs against the inbox table only. It never touches `transactions` and never cancels
 * a notification's ability to be acted on — a detection that aged out is simply no
 * longer in the inbox, while the transaction the user filed from it remains untouched
 * and permanent.
 *
 * Failures return [Result.retry] so a device that was off for the scheduled window
 * still gets its cleanup on the next attempt rather than keeping old message text
 * indefinitely.
 */
@HiltWorker
class SmsInboxCleanupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val cleanupSmsInbox: CleanupSmsInboxUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val result = cleanupSmsInbox()
            Log.d(
                TAG,
                "Retention pass complete: expired=${result.expired} purged=${result.purged}"
            )
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Retention pass failed; will retry", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SmsInboxCleanupWorker"

        /**
         * Arms the daily cleanup. `UPDATE` keeps an existing schedule's cadence intact
         * instead of resetting it on every app start, so the pass always runs roughly
         * once a day rather than being perpetually deferred.
         */
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<SmsInboxCleanupWorker>(1, TimeUnit.DAYS)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        private const val WORK_NAME = "SmsInboxCleanupPeriodicWork"
    }
}
