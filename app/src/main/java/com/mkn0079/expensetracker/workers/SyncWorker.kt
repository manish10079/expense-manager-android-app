package com.mkn0079.expensetracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mkn0079.expensetracker.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 1. Handshake (Register/Check Device Limit)
        val handshakeResult = syncRepository.registerCurrentDevice()
        
        if (handshakeResult.isFailure) {
            // If the failure is due to device limit, we stop trying
            return if (handshakeResult.exceptionOrNull()?.message?.contains("limit reached") == true) {
                Result.failure()
            } else {
                Result.retry()
            }
        }

        // 2. Perform actual data sync
        val syncResult = syncRepository.syncTransactions()
        
        return if (syncResult.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        private const val SYNC_WORK_NAME = "periodic_cloud_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun startImmediate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}
