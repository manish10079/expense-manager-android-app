package com.mknlabs.expensetracker.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.mknlabs.expensetracker.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Tracks whether a sync-on-available callback is currently registered.
    private var isSyncCallbackRegistered = false

    private val syncOnAvailableCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Network has been restored. Kick off an immediate sync which will cancel
            // any stuck exponential-backoff chain and run fresh via the REPLACE policy.
            android.util.Log.i("NetworkMonitor", "Network restored — triggering immediate sync.")
            SyncWorker.startImmediate(context)
        }
    }

    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Start listening for network-available events. Safe to call multiple times —
     * duplicate registrations are ignored.
     */
    fun registerSyncOnAvailable() {
        if (isSyncCallbackRegistered) return
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build()
            connectivityManager.registerNetworkCallback(request, syncOnAvailableCallback)
            isSyncCallbackRegistered = true
            android.util.Log.d("NetworkMonitor", "Sync-on-available callback registered.")
        } catch (e: Exception) {
            android.util.Log.e("NetworkMonitor", "Failed to register network callback", e)
        }
    }

    /**
     * Stop listening. Call this when the app goes to background to avoid unnecessary
     * background triggers (WorkManager constraints already handle the background case).
     */
    fun unregisterSyncOnAvailable() {
        if (!isSyncCallbackRegistered) return
        try {
            connectivityManager.unregisterNetworkCallback(syncOnAvailableCallback)
            isSyncCallbackRegistered = false
            android.util.Log.d("NetworkMonitor", "Sync-on-available callback unregistered.")
        } catch (e: Exception) {
            android.util.Log.e("NetworkMonitor", "Failed to unregister network callback", e)
        }
    }
}
