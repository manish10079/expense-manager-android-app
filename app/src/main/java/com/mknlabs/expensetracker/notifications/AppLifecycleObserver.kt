package com.mknlabs.expensetracker.notifications

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mknlabs.expensetracker.domain.repository.SecurityRepository
import com.mknlabs.expensetracker.utils.NetworkMonitor
import com.mknlabs.expensetracker.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityRepository: SecurityRepository,
    private val networkMonitor: NetworkMonitor
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        // App backgrounded: record time, notify security, and stop the network callback.
        // WorkManager's CONNECTED constraint already handles background sync scheduling.
        securityRepository.markBackgrounded()
        securityRepository.notifyAppBackground()
        networkMonitor.unregisterSyncOnAvailable()
    }

    override fun onStart(owner: LifecycleOwner) {
        // App foregrounded: notify security and trigger an immediate sync attempt.
        // This is the primary fix for the "device offline for days then comes back online"
        // scenario — startImmediate() uses REPLACE, so it always cancels any stuck
        // exponential-backoff chain and runs a fresh sync immediately.
        securityRepository.notifyAppForeground()
        SyncWorker.startImmediate(context)

        // Register the network callback so that if the network is restored while the
        // app is in the foreground, another sync is triggered immediately.
        networkMonitor.registerSyncOnAvailable()
    }
}
