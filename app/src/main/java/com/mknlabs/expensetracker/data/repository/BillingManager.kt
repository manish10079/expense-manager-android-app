package com.mknlabs.expensetracker.data.repository

import android.app.Application
import android.util.Log
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.PurchasesUpdatedListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton manager for RevenueCat purchases integration.
 * Handles:
 * - RevenueCat initialization and configuration
 * - Firebase Auth user ID synchronization with RevenueCat
 * - CustomerInfo updates and entitlement management
 * - Offerings and product fetching
 * - Purchase handling and restoration
 *
 * <b>Firebase Extension Integration:</b>
 * RevenueCat automatically syncs subscription data to Firestore via the RevenueCat Firebase Extension.
 * The extension writes to the `users/{uid}/subscriptions` collection, where:
 *   - `uid` is the Firebase User ID (same as used in RevenueCat logIn)
 *   - Each document contains the latest subscription information for that user
 * This enables server-side authorization and client-side entitlement checks without exposing RevenueCat API keys.
 * Ensure the RevenueCat Firebase Extension is installed and configured in your Firebase project.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val app: Application,
    private val authRepository: AuthRepository,
    private val appLockPreferences: AppLockPreferences
) {

    companion object {
        private const val TAG = "BillingManager"
    }

    // RevenueCat customer info state
    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

    // Offerings state
    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    // Initialize RevenueCat on creation
    init {
        initializeRevenueCat()
        setupAuthListener()
    }

    private fun initializeRevenueCat() {
        // Get RevenueCat API key from BuildConfig (loaded from localProperties.properties)
        val apiKey = BuildConfig.REVENUE_CAT_API_KEY

        if (apiKey.isEmpty()) {
            Log.w(TAG, "RevenueCat API key not configured. Set revenueCatApiKey in localProperties.properties")
            return
        }

        try {
            // Configure RevenueCat
            Purchases.configure(
                PurchasesConfiguration.Builder(app, apiKey)
                    .setObserverMode(false)
                    .setUsesAmazonIapV2(false)
                    .build()
            )

            // Set log level for debugging (remove in production)
            Purchases.setLogLevel(LogLevel.DEBUG)

            // Set updated listener to get real-time CustomerInfo updates
            Purchases.sharedInstance().setPurchasesUpdatedListener(purchasesUpdatedListener)

            // Fetch initial offerings
            fetchOfferings()

            Log.d(TAG, "RevenueCat initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing RevenueCat", e)
        }
    }

    private fun setupAuthListener() {
        // Listen to auth state changes and sync with RevenueCat
        CoroutineScope(Dispatchers.Main.immediate).launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    // User signed in - log in to RevenueCat with Firebase UID
                    val uid = user.uid
                    logInToRevenueCat(uid)
                } else {
                    // User signed out - log out from RevenueCat
                    logOutFromRevenueCat()
                }
            }
        }
    }

    private fun logInToRevenueCat(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Purchases.sharedInstance().logIn(uid) { customerInfo, created, error ->
                    if (error != null) {
                        Log.e(TAG, "Error logging in to RevenueCat: ${error.message}", error)
                    } else {
                        Log.d(TAG, "Logged in to RevenueCat with ID: $uid (new user: $created)")
                        updateCustomerInfo(customerInfo)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during RevenueCat login", e)
            }
        }
    }

    private fun logOutFromRevenueCat() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Purchases.sharedInstance().logOut { customerInfo, error ->
                    if (error != null) {
                        Log.e(TAG, "Error logging out from RevenueCat: ${error.message}", error)
                    } else {
                        Log.d(TAG, "Logged out from RevenueCat")
                        updateCustomerInfo(customerInfo)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during RevenueCat logout", e)
            }
        }
    }

    private fun updateCustomerInfo(info: CustomerInfo?) {
        _customerInfo.update { info }
        // Also update offerings in case entitlements changed
        fetchOfferings()
    }

    private val purchasesUpdatedListener: PurchasesUpdatedListener = PurchasesUpdatedListener { customerInfo ->
        Log.d(TAG, "Received updated CustomerInfo from RevenueCat")
        updateCustomerInfo(customerInfo)
    }

    fun fetchOfferings() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Purchases.sharedInstance().getOfferings { offerings, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching offerings: ${error.message}", error)
                    } else {
                        Log.d(TAG, "Fetched ${offerings?.offeringsMap?.size ?: 0} offerings")
                        _offerings.update { offerings }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching offerings", e)
            }
        }
    }

    fun getCustomerInfo(): CustomerInfo? = _customerInfo.value

    fun getOfferings(): Offerings? = _offerings.value

    fun purchasePackage(activity: android.app.Activity, pkg: Package) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Purchases.sharedInstance().purchasePackage(activity, pkg) { transaction, customerInfo, error, userCancelled ->
                    if (error != null) {
                        Log.e(TAG, "Error purchasing package: ${error.message}", error)
                    } else if (userCancelled) {
                        Log.d(TAG, "User cancelled purchase")
                    } else {
                        Log.d(TAG, "Successfully purchased package: ${pkg.packageIdentifier}")
                        updateCustomerInfo(customerInfo)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during purchase", e)
            }
        }
    }

    fun restorePurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Purchases.sharedInstance().restorePurchases { customerInfo, error ->
                    if (error != null) {
                        Log.e(TAG, "Error restoring purchases: ${error.message}", error)
                    } else {
                        Log.d(TAG, "Successfully restored purchases")
                        updateCustomerInfo(customerInfo)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception restoring purchases", e)
            }
        }
    }

    /**
     * Check if user has active entitlement for given identifier
     */
    fun hasEntitlement(entitlementIdentifier: String): Boolean {
        return _customerInfo.value?.entitlements?.all[entitlementIdentifier]?.isActive == true
    }

    /**
     * Get all active entitlement identifiers
     */
    fun getActiveEntitlements(): Set<String> {
        return _customerInfo.value?.entitlements?.all
            ?.filterValues { it.isActive }
            ?.keySet() ?: emptySet()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        Purchases.sharedInstance().setPurchasesUpdatedListener(null)
    }
}