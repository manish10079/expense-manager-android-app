package com.mknlabs.expensetracker.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper to check internet connectivity status.
 *
 * Hilt-managed singleton: injected into ViewModels that need
 * to check network availability before making cloud API calls.
 */
@Singleton
class ConnectivityHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Checks if the device has an active internet connection.
     *
     * @return true if the device is connected to the internet,
     *         false otherwise (offline or no validated network).
     */
    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
