package com.mknlabs.expensetracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.domain.repository.SyncRepository
import com.mknlabs.expensetracker.models.UserTier
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 1. Sync profile for any authenticated user (free, premium, guest/anonymous)
        val profileSyncResult = syncRepository.syncUserProfile()
        if (profileSyncResult.isFailure) {
            return Result.retry()
        }

        // If not signed in, profile sync is a no-op and we're done.
        if (firebaseAuth.currentUser == null) {
            return Result.success()
        }

        // 2. Check Tier - Only Premium users get full Room DB cloud sync
        val settings = AppSettingsDataStore.getAppSettingsFlow(applicationContext).first()
        if (settings.userTier != UserTier.PREMIUM) {
            return Result.success()
        }

        // 3. Handshake (Register/Check Device Limit)
        val handshakeResult = syncRepository.registerCurrentDevice()
        
        if (handshakeResult.isFailure) {
            // If the failure is due to device limit, we stop trying
            return if (handshakeResult.exceptionOrNull()?.message?.contains("limit reached") == true) {
                Result.failure()
            } else {
                Result.retry()
            }
        }

        // 4. Perform actual data sync
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
