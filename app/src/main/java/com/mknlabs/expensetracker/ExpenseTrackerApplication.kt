package com.mknlabs.expensetracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.workers.RecurringTransactionWorker
import com.mknlabs.expensetracker.notifications.AppLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ExpenseTrackerApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {

        super.onCreate()

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

        // Register App Lifecycle Observer for security lock
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}