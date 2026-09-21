package com.mknlabs.expensetracker.data.repository

import android.app.Activity
import android.app.Application
import android.util.Log
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitLogOut
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
                    .build()
            )

            // Set log level for debugging
            Purchases.logLevel = LogLevel.DEBUG

            // Set updated listener to get real-time CustomerInfo updates
            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                Log.d(TAG, "Received updated CustomerInfo from RevenueCat")
                updateCustomerInfo(customerInfo)
            }

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
                val result = Purchases.sharedInstance.awaitLogIn(uid)
                Log.d(TAG, "Logged in to RevenueCat with ID: $uid (new user: ${result.created})")
                updateCustomerInfo(result.customerInfo)
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error logging in to RevenueCat: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception during RevenueCat login", e)
            }
        }
    }

    private fun logOutFromRevenueCat() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val customerInfo = Purchases.sharedInstance.awaitLogOut()
                Log.d(TAG, "Logged out from RevenueCat")
                updateCustomerInfo(customerInfo)
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error logging out from RevenueCat: ${e.message}")
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

    fun fetchOfferings() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val offerings = Purchases.sharedInstance.awaitOfferings()
                Log.d(TAG, "Fetched ${offerings.all.size} offerings")
                _offerings.update { offerings }
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error fetching offerings: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching offerings", e)
            }
        }
    }

    fun getCustomerInfo(): CustomerInfo? = _customerInfo.value

    fun getOfferings(): Offerings? = _offerings.value

    fun purchasePackage(activity: Activity, pkg: Package) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val params = PurchaseParams.Builder(activity, pkg).build()
                val result = Purchases.sharedInstance.awaitPurchase(params)
                Log.d(TAG, "Successfully purchased package: ${pkg.identifier}")
                updateCustomerInfo(result.customerInfo)
            } catch (e: PurchasesTransactionException) {
                if (e.userCancelled) {
                    Log.d(TAG, "User cancelled purchase")
                } else {
                    Log.e(TAG, "Error purchasing package: ${e.message}")
                }
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error purchasing package: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception during purchase", e)
            }
        }
    }

    fun restorePurchases() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val customerInfo = Purchases.sharedInstance.awaitRestore()
                Log.d(TAG, "Successfully restored purchases")
                updateCustomerInfo(customerInfo)
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error restoring purchases: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception restoring purchases", e)
            }
        }
    }

    /**
     * Check if user has active entitlement for given identifier
     */
    fun hasEntitlement(entitlementIdentifier: String): Boolean {
        return _customerInfo.value?.entitlements?.all?.get(entitlementIdentifier)?.isActive == true
    }

    /**
     * Get all active entitlement identifiers
     */
    fun getActiveEntitlements(): Set<String> {
        return _customerInfo.value?.entitlements?.all
            ?.filterValues { it.isActive }
            ?.keys ?: emptySet()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        Purchases.sharedInstance.updatedCustomerInfoListener = null
    }
}