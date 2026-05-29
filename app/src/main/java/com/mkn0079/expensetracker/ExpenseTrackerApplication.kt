package com.mkn0079.expensetracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mkn0079.expensetracker.notifications.NotificationHelper
import com.mkn0079.expensetracker.workers.RecurringTransactionWorker
import com.mkn0079.expensetracker.notifications.AppLifecycleObserver
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
        com.mkn0079.expensetracker.data.local.AppLockPreferences.initialize(this)

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // Schedule Recurring Transactions processing
        RecurringTransactionWorker.scheduleNext(this)

        // Schedule Periodic Cloud Sync
        com.mkn0079.expensetracker.workers.SyncWorker.schedule(this)

        // Register App Lifecycle Observer for security lock
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}