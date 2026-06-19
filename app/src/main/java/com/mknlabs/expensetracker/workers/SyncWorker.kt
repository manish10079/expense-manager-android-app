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
        val isNewUser = inputData.getBoolean("is_new_user", false)
        
        // Stabilization: Give Firebase Auth a moment to restore the session if it was just launched
        kotlinx.coroutines.delay(1000)
        
        val currentUser = firebaseAuth.currentUser
        android.util.Log.d("SyncWorker", "Starting sync work. isNewUser: $isNewUser, user: ${currentUser?.uid}, isAnon: ${currentUser?.isAnonymous}")
        
        // 1. Sync profile for any authenticated user (free, premium, guest/anonymous)
        // This will update the local UserTier if it changed on the server
        val profileSyncResult = syncRepository.syncUserProfile(isNewUser)
        if (profileSyncResult.isFailure) {
            return Result.retry()
        }

        // If not signed in, profile sync is a no-op and we're done.
        if (firebaseAuth.currentUser == null) {
            return Result.success()
        }

        // 2. Re-read settings AFTER profile sync to get the latest tier
        val settings = AppSettingsDataStore.getAppSettingsFlow(applicationContext).first()
        if (settings.userTier != UserTier.PREMIUM || !settings.isCloudSyncEnabled) {
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

        fun startImmediate(context: Context, isNewUser: Boolean = false) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = Data.Builder()
                .putBoolean("is_new_user", isNewUser)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeRequest)
        }
    }
}
