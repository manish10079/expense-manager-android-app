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
import com.mknlabs.expensetracker.domain.repository.AuthRepository

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val firebaseAuth: FirebaseAuth,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val isNewUser = inputData.getBoolean("is_new_user", false)

        // Stabilization: Give Firebase Auth a moment to restore the session if it was just launched
        kotlinx.coroutines.delay(1000)

        var currentUser = firebaseAuth.currentUser
        android.util.Log.d("SyncWorker", "Starting sync work. isNewUser: $isNewUser, isAnon: ${currentUser?.isAnonymous}")

        var isPendingNewUser = isNewUser
        if (currentUser == null) {
            val localProfile = com.mknlabs.expensetracker.data.local.UserProfileDataStore.getUserProfileFlow(applicationContext).first()
            if (localProfile.authProvider == "anonymous") {
                android.util.Log.i("SyncWorker", "Offline guest user detected. Attempting catch-up anonymous sign-in.")
                val signInResult = authRepository.signInAnonymously()
                if (signInResult.isSuccess) {
                    currentUser = firebaseAuth.currentUser
                    isPendingNewUser = true
                    android.util.Log.i("SyncWorker", "Catch-up anonymous sign-in successful")
                } else {
                    android.util.Log.e("SyncWorker", "Failed catch-up anonymous sign-in", signInResult.exceptionOrNull())
                    // Re-enroll the periodic chain before retrying so WorkManager
                    // keeps the 15-minute cadence even during retry backoff.
                    schedulePeriodic(applicationContext)
                    return Result.retry()
                }
            }
        }

        // 1. Sync profile for any authenticated user (free, premium, guest/anonymous)
        // This will update the local UserTier if it changed on the server
        val profileSyncResult = syncRepository.syncUserProfile(isPendingNewUser)
        if (profileSyncResult.isFailure) {
            schedulePeriodic(applicationContext)
            return Result.retry()
        }

        // If not signed in, profile sync is a no-op and we're done.
        if (firebaseAuth.currentUser == null) {
            // Re-enroll periodic chain so the next scheduled run still fires correctly.
            schedulePeriodic(applicationContext)
            return Result.success()
        }

        // 2. Re-read settings AFTER profile sync to get the latest tier
        val settings = AppSettingsDataStore.getAppSettingsFlow(applicationContext).first()
        if (settings.userTier != UserTier.PREMIUM || !settings.isCloudSyncEnabled) {
            schedulePeriodic(applicationContext)
            return Result.success()
        }

        val showNotifications = settings.cloudSecurityEnabled
        if (showNotifications) {
            com.mknlabs.expensetracker.notifications.NotificationHelper.showSyncInProgressNotification(applicationContext)
        }

        var notificationHandled = false
        try {
            // 3. Handshake (Register/Check Device Limit)
            val handshakeResult = syncRepository.registerCurrentDevice()

            if (handshakeResult.isFailure) {
                val error = handshakeResult.exceptionOrNull()
                android.util.Log.e("SyncWorker", "Device registration failed", error)
                if (showNotifications) {
                    com.mknlabs.expensetracker.notifications.NotificationHelper.showSyncFailedNotification(applicationContext)
                    notificationHandled = true
                }
                // If the failure is due to device limit, we stop trying
                return if (error?.message?.contains("limit reached") == true) {
                    Result.failure()
                } else {
                    schedulePeriodic(applicationContext)
                    Result.retry()
                }
            }

            // 4. Perform actual data sync
            val syncResult = syncRepository.syncTransactions()

            val success = syncResult.isSuccess
            if (!success) {
                android.util.Log.e("SyncWorker", "Transaction sync failed", syncResult.exceptionOrNull())
                if (showNotifications) {
                    val isNetworkError = syncResult.exceptionOrNull() is java.io.IOException ||
                            syncResult.exceptionOrNull()?.message?.contains("network", ignoreCase = true) == true
                    if (isNetworkError) {
                        com.mknlabs.expensetracker.notifications.NotificationHelper.showSyncPendingNotification(applicationContext)
                    } else {
                        com.mknlabs.expensetracker.notifications.NotificationHelper.showSyncFailedNotification(applicationContext)
                    }
                    notificationHandled = true
                }
            } else {
                com.mknlabs.expensetracker.notifications.NotificationHelper.cancelSyncInProgressNotification(applicationContext)
                notificationHandled = true
            }

            return if (success) Result.success() else Result.retry()
        } finally {
            // Safety net: if the notification was posted but none of the
            // normal code paths cancelled/replaced it (e.g. an uncaught
            // exception, or the coroutine was cancelled by the system),
            // cancel it now so it never lingers in the shade.
            if (showNotifications && !notificationHandled) {
                com.mknlabs.expensetracker.notifications.NotificationHelper.cancelSyncInProgressNotification(applicationContext)
            }
            // Always re-enroll the periodic chain.
            schedulePeriodic(applicationContext)
        }
    }

    companion object {
        private const val SYNC_WORK_NAME = "periodic_cloud_sync"

        /**
         * Enrolls (or re-enrolls) the 15-minute periodic sync chain.
         * Uses [ExistingPeriodicWorkPolicy.UPDATE] (notification spec WorkManager
         * requirement) so the period's spec stays current; UPDATE updates the
         * stored request in place and never cancels an in-flight run, so an
         * already-running period is not reset by accident. This replaces the old
         * chained OneTimeWorkRequest approach that accumulated exponential
         * backoff after multi-day offline periods.
         */
        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        }

        /**
         * Triggers an immediate sync, cancelling any currently pending or retrying
         * work (including a stuck backoff chain) via [ExistingWorkPolicy.REPLACE].
         * After the immediate run completes, [doWork] automatically re-enrolls the
         * periodic chain via [schedulePeriodic].
         */
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

            // REPLACE cancels any stuck retrying periodic or one-time worker that has
            // accumulated exponential backoff after the device was offline for days.
            WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }
    }
}
