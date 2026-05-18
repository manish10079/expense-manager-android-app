package com.mkn0079.expensetracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mkn0079.expensetracker.notifications.NotificationHelper
import com.mkn0079.expensetracker.workers.RecurringTransactionWorker
import com.mkn0079.expensetracker.notifications.AppLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        // Initialize Mobile Ads SDK
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            MobileAds.initialize(this@ExpenseTrackerApplication) {}
        }

        // Initialize security preferences early
        com.mkn0079.expensetracker.data.local.AppLockPreferences.initialize(this)

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannels(this)

        // Schedule Recurring Transactions processing
        RecurringTransactionWorker.scheduleNext(this)

        // Register App Lifecycle Observer for security lock
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }
}