package com.mknlabs.expensetracker

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.mknlabs.expensetracker.data.repository.BillingManager
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.mknlabs.expensetracker.domain.repository.FcmTokenRepository
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.workers.RecurringTransactionWorker
import com.mknlabs.expensetracker.notifications.AppLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltAndroidApp
class ExpenseTrackerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var fcmTokenRepository: FcmTokenRepository

    /**
     * RevenueCat bootstrap.
     *
     * [BillingManager] is a `@Singleton` that configures the SDK from its own `init`
     * block, and Hilt builds a singleton lazily — only when something asks for it.
     * Nothing else in the app injects it, so without this field `Purchases.configure()`
     * never runs and every subscription call fails. Holding it here makes the
     * Application the one place that starts billing, at process start, before any
     * screen can ask for offerings. The field is intentionally never read: the
     * construction itself is the side effect, exactly like [appLifecycleObserver].
     */
    @Inject
    lateinit var billingManager: BillingManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {

        super.onCreate()

        // Initialize Firebase App Check BEFORE any other Firebase SDK calls.
        // Debug builds use a debug token (shown in logcat) so development is
        // not blocked by integrity checks. Release builds use Play Integrity
        // which verifies the app is genuine and the device is uncompromised.
        FirebaseApp.initializeApp(this)
        AppCheckInitializer.initialize(this)

        // Initialize security preferences early
        com.mknlabs.expensetracker.data.local.AppLockPreferences.initialize(this)

        // Initialize Notification Channels (and re-create them once per app
        // update so importance changes reach existing installs, plan §Reminders)
        NotificationHelper.createNotificationChannels(this)
        NotificationHelper.resetChannelsIfVersionChanged(this)

        // Schedule Recurring Transactions processing (periodic heartbeat;
        // KEEP makes this a no-op if it is already armed)
        RecurringTransactionWorker.schedulePeriodic(this)

        // Enroll the 15-minute periodic cloud sync (KEEP: no-op if already running)
        com.mknlabs.expensetracker.workers.SyncWorker.schedulePeriodic(this)

        // Arm the daily SMS detection inbox retention pass (30-day policy).
        // UPDATE keeps an existing schedule's cadence instead of resetting it on
        // every launch, so the pass actually runs once a day.
        com.mknlabs.expensetracker.feature.smsinbox.worker.SmsInboxCleanupWorker.schedulePeriodic(this)

        // Register App Lifecycle Observer for security lock
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        // FCM token registration (notification plan §5.4/§5.6): keeps the
        // device's Firestore token doc in sync with auth state. On any
        // signed-in state (app start with restored session, or a fresh
        // sign-in) the current token is upserted; on sign-out the device's
        // doc is deleted so no stale token lingers for the previous user.
        // Token refreshes are handled separately by onNewToken.
        applicationScope.launch {
            authRepository.currentUser.collect { user ->
                    if (user != null) {
                        runCatching {
                            @Suppress("DEPRECATION")
                            val token = FirebaseMessaging.getInstance().token.await()
                            fcmTokenRepository.registerCurrentDeviceToken(token)
                        }
                    } else {
                        fcmTokenRepository.removeCurrentDeviceToken()
                    }
                }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}