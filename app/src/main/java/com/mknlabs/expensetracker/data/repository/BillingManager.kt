package com.mknlabs.expensetracker.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.data.local.MonetizationDataStore
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.mknlabs.expensetracker.domain.repository.BillingRepository
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import com.mknlabs.expensetracker.monetization.SubscriptionOffer
import com.mknlabs.expensetracker.monetization.SubscriptionOfferMapper
import com.mknlabs.expensetracker.monetization.discountPercentOf
import com.mknlabs.expensetracker.monetization.toPurchaseState
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.PurchasesTransactionException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitLogIn
import com.revenuecat.purchases.awaitLogOut
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import com.revenuecat.purchases.awaitRestore
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreProduct
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
 * RevenueCat syncs subscription data to Firestore through the RevenueCat Firebase
 * Extension, which is the <b>only</b> writer of that data — this class never writes it.
 * Everything lands in the top-level `rc_customers` collection:
 *   - Document id is `{uid}` — the Firebase User ID, which is also the RevenueCat
 *     app user id, because this class logs in with `Purchases.logIn(firebaseUid)`
 *   - The document holds that customer's latest entitlement snapshot
 * When the extension is also configured for events, subscription lifecycle events
 * (`INITIAL_PURCHASE`, `RENEWAL`, `CANCELLATION`, `EXPIRATION`, ...) are appended to
 * the top-level `rc_events` collection as a server-side audit trail.
 *
 * Both collections are <b>read-only to clients</b> (`firestore.rules`): the extension
 * writes with the Admin SDK, which bypasses security rules and App Check. That is why
 * they read fine even though no write rule grants the app access.
 *
 * `rc_customers` is deliberately NOT nested under `users/{uid}`: Firestore rules are
 * additive, so the recursive `users/{uid}/{document=**}` grant would make any nested
 * path client-writable, and entitlement data must never be client-writable.
 *
 * This enables server-side authorization and client-side entitlement checks without
 * exposing the RevenueCat API key to the client.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val subscriptionSyncDiagnostics: SubscriptionSyncDiagnostics
) : BillingRepository {

    companion object {
        private const val TAG = "BillingManager"

        /**
         * The entitlement the paywall sells. Must equal the entitlement identifier
         * configured in the RevenueCat dashboard exactly; it is `premium`.
         */
        const val PREMIUM_ENTITLEMENT_ID = "premium"

        /**
         * Entitlement that grants an ad-free experience on its own, without the Pro feature
         * set. Must match the entitlement identifier in the RevenueCat dashboard exactly.
         *
         * Kept in the billing layer because RevenueCat entitlement identifiers are billing
         * vocabulary — the monetization layer must not need to know them.
         */
        const val AD_FREE_ENTITLEMENT_ID = "ad_free_global"
    }

    /**
     * Scope for the derived flows below. This class is a `@Singleton` that lives for the
     * process, so an eagerly-started flow is never left collecting against a dead scope.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * The default offering's packages by identifier.
     *
     * Kept so [purchase] can turn the paywall's offer id back into the SDK object
     * RevenueCat needs, without the UI ever holding an SDK type.
     */
    @Volatile
    private var packagesById: Map<String, Package> = emptyMap()

    /**
     * True once an offerings fetch has finished, successfully or not.
     *
     * Separates "still loading" from "loaded and empty" for the paywall, which otherwise
     * cannot tell a slow network from an offering that was never published.
     */
    private val _isOffersLoaded = MutableStateFlow(false)
    override val isOffersLoaded: StateFlow<Boolean> = _isOffersLoaded.asStateFlow()

    // RevenueCat customer info state
    private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
    val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

    // Offerings state
    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    // Purchase/restore outcome state. The UI renders loading, cancellation, pending
    // payment and failure from this; it carries no text, so the paywall owns the wording.
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    // Declared after `_offerings` and `_customerInfo` on purpose: these initializers read
    // those flows, and a property declared above them would observe a null at construction.

    /**
     * The default offering's plans in UI-ready form, so no RevenueCat type reaches the UI.
     *
     * Order is the dashboard's own package order — the paywall renders plans exactly as
     * they were arranged, so no ordering policy is invented here.
     */
    override val offers: StateFlow<List<SubscriptionOffer>> = _offerings
        .map { offerings ->
            offerings?.current?.availablePackages.orEmpty().map { pkg ->
                val pricing = planPricing(pkg.product)

                SubscriptionOfferMapper.from(
                    packageIdentifier = pkg.identifier,
                    packageTypeName = pkg.packageType.name,
                    storeFormattedPrice = pricing.priceText,
                    discountPercent = pricing.discountPercent,
                    strikethroughPriceText = pricing.strikethroughPriceText,
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /**
     * Whether the `premium` entitlement is active.
     *
     * Derived from `CustomerInfo`, which is refreshed after every purchase, restore and
     * login — so this is the one source of entitlement truth the paywall reads, rather
     * than inferring it from a completed purchase.
     */
    override val isPremium: StateFlow<Boolean> = _customerInfo
        .map { info ->
            info?.entitlements?.all?.get(PREMIUM_ENTITLEMENT_ID)?.isActive == true
        }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * The store's view of the active `premium` entitlement, mapped off the same
     * `CustomerInfo` snapshot as [isPremium].
     *
     * `CustomerInfo` is refreshed after every purchase, restore and login, so this is
     * current rather than cached; a date parsed out of Firestore would lag a webhook, and a
     * renewal the extension had not yet seen would read as the wrong end date.
     *
     * Null means "no active entitlement", which the membership card treats as "say nothing
     * about a renewal" — never as "renews never".
     */
    override val storeEntitlement: StateFlow<StoreEntitlement?> = _customerInfo
        .map { info ->
            info?.entitlements?.all?.get(PREMIUM_ENTITLEMENT_ID)
                ?.takeIf { it.isActive }
                ?.let { entitlement ->
                    StoreEntitlement(
                        // Null for lifetime access, which the card must not date.
                        expirationDateMillis = entitlement.expirationDate?.time,
                        // A cancelled subscription stays active until it expires; only
                        // `willRenew` distinguishes "renews" from "ends".
                        willRenew = entitlement.willRenew,
                        hasBillingIssue = entitlement.billingIssueDetectedAt != null
                    )
                }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * Whether ads should be suppressed: the `premium` entitlement *or* the standalone
     * ad-free entitlement being active.
     *
     * Narrower entitlements are not inferred from each other — a user who bought only ad
     * removal gets no Pro features, and this flow is the one the ads path reads.
     */
    override val isAdFree: StateFlow<Boolean> = _customerInfo
        .map { info ->
            val active = info?.entitlements?.all.orEmpty()
                .filterValues { it.isActive }
                .keys
            PREMIUM_ENTITLEMENT_ID in active || AD_FREE_ENTITLEMENT_ID in active
        }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * The store's management page for this subscriber, straight from RevenueCat.
     *
     * Null whenever `CustomerInfo` has none — most often because there is no active
     * subscription to manage, which is exactly when the paywall should not offer the link.
     */
    override val managementUrl: StateFlow<String?> = _customerInfo
        .map { info -> info?.managementURL?.toString() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * True once `Purchases.configure()` has succeeded.
     *
     * Guarded with `@Volatile` because it is written during construction and read from
     * whichever thread calls [purchasePackage] or [restorePurchases]. Every RevenueCat
     * call would otherwise throw if the API key were missing, so the public entry points
     * report [PurchaseState.FailureReason.BillingNotConfigured] instead of crashing.
     */
    @Volatile
    private var isConfigured = false

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
                PurchasesConfiguration.Builder(context, apiKey)
                    .build()
            )
            isConfigured = true

            // Verbose RevenueCat logs are a development aid only — in a release build
            // they would write purchase and customer detail into logcat. R8 folds
            // BuildConfig.DEBUG to false, so the whole branch is stripped from release.
            if (BuildConfig.DEBUG) {
                Purchases.logLevel = LogLevel.DEBUG
            }

            // Set updated listener to get real-time CustomerInfo updates
            Purchases.sharedInstance.updatedCustomerInfoListener = UpdatedCustomerInfoListener { customerInfo ->
                Log.d(TAG, "Received updated CustomerInfo from RevenueCat")
                updateCustomerInfo(customerInfo)
            }

            // Fetch initial offerings
            fetchOfferings()

            // Read the store's CustomerInfo as soon as configuration succeeds. RevenueCat
            // only delivers it through its listener, a login, or a purchase/restore, so
            // without this read a subscriber who is already signed in can spend the whole
            // session with no snapshot at all — which is how the membership screen came to
            // describe a ProPass grant for a user who is paying for a subscription.
            refreshCustomerInfo()

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

        // Latch the store's verdict where the settings layer can read it without holding a
        // BillingRepository: a store subscription never writes accountTier = "PREMIUM"
        // locally, so this is the only way AppSettingsDataStore can learn that the user is
        // actually paying. A null snapshot means "not loaded yet", not "no entitlement", so
        // it is ignored rather than overwriting the last verdict with false.
        if (info != null) {
            val premiumActive = info.entitlements.all.get(PREMIUM_ENTITLEMENT_ID)?.isActive == true
            CoroutineScope(Dispatchers.IO).launch {
                MonetizationDataStore.setPremiumEntitlementActive(context, premiumActive)
            }
        }

        // Debug-only: log the SDK's entitlements next to the snapshot the RevenueCat
        // Firebase Extension wrote to Firestore, so the sync path can be confirmed from
        // logcat during sandbox testing. Costs one Firestore read per CustomerInfo change
        // and prints customer identifiers, so it must never run in a release build.
        if (BuildConfig.DEBUG) {
            val appUserId = authRepository.currentUser.value?.uid
            CoroutineScope(Dispatchers.IO).launch {
                subscriptionSyncDiagnostics.logSyncCheck(appUserId, info)
            }
        }
    }

    fun fetchOfferings() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val offerings = Purchases.sharedInstance.awaitOfferings()
                Log.d(TAG, "Fetched ${offerings.all.size} offerings")
                // Cached so the paywall can start a purchase from an offer id alone.
                packagesById = offerings.current?.availablePackages.orEmpty()
                    .associateBy { it.identifier }
                _offerings.update { offerings }
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error fetching offerings: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception fetching offerings", e)
            } finally {
                // Marked even on failure: the paywall must stop claiming to load and
                // offer a retry, instead of spinning forever on an unreachable network.
                _isOffersLoaded.value = true
            }
        }
    }

    fun getCustomerInfo(): CustomerInfo? = _customerInfo.value

    /**
     * Re-reads the customer's entitlements, refreshing them from the store when the SDK's
     * snapshot is stale.
     *
     * The SDK delivers customer info through its listener or as the result of a login,
     * purchase or restore — and through nothing else. A subscriber who is already signed in
     * can therefore go an entire session with no snapshot, which is what left the
     * membership screen describing a ProPass grant for a user who is paying for a
     * subscription. Anything that has to state where Pro comes from reads the store here.
     *
     * [CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT] returns the cached snapshot only while
     * it is still current, so a stale entitlement is never presented as fact. A failed
     * fetch leaves the previous snapshot in place — an old answer beats a blank one.
     */
    fun refreshCustomerInfo() {
        if (!isConfigured) {
            Log.w(TAG, "CustomerInfo refresh requested before RevenueCat was configured")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val customerInfo = Purchases.sharedInstance.awaitCustomerInfo(
                    fetchPolicy = CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT
                )
                updateCustomerInfo(customerInfo)
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error refreshing CustomerInfo: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception refreshing CustomerInfo", e)
            }
        }
    }

    fun getOfferings(): Offerings? = _offerings.value

    /**
     * Returns [purchaseState] to [PurchaseState.Idle] once the UI has finished reacting to
     * a terminal outcome (shown its snackbar, dismissed its sheet, ...).
     *
     * Without this the last outcome would still be current on the next recomposition or
     * screen visit, so the paywall would re-announce a stale success or error.
     */
    fun acknowledgePurchaseState() {
        _purchaseState.value = PurchaseState.Idle
    }

    /**
     * Starts a purchase and mirrors its outcome into [purchaseState].
     *
     * [PurchaseState.InProgress] is published synchronously, so the UI can disable its
     * actions before the Play sheet appears. RevenueCat's diagnostic messages are logged
     * only — the state deliberately carries a typed reason instead of SDK text.
     */
    fun purchasePackage(activity: Activity, pkg: Package) {
        val operation = PurchaseState.Operation.Purchase

        if (!isConfigured) {
            Log.e(TAG, "Purchase attempted before RevenueCat was configured")
            _purchaseState.value = PurchaseState.Failed(
                operation,
                PurchaseState.FailureReason.BillingNotConfigured
            )
            return
        }

        _purchaseState.value = PurchaseState.InProgress(operation)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val params = PurchaseParams.Builder(activity, pkg).build()
                val result = Purchases.sharedInstance.awaitPurchase(params)
                Log.d(TAG, "Successfully purchased package: ${pkg.identifier}")
                updateCustomerInfo(result.customerInfo)
                _purchaseState.value = PurchaseState.Completed(operation)
            } catch (e: PurchasesTransactionException) {
                // Covers the dismiss-the-sheet case, which is not an error.
                Log.d(TAG, "Purchase did not complete: ${e.message}")
                _purchaseState.value = e.code.toPurchaseState(e.userCancelled, operation)
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error purchasing package: ${e.message}")
                _purchaseState.value = e.code.toPurchaseState(
                    userCancelled = false,
                    operation = operation
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during purchase", e)
                _purchaseState.value = PurchaseState.Failed(
                    operation,
                    PurchaseState.FailureReason.Unknown
                )
            }
        }
    }

    /**
     * Restores previous purchases and mirrors the outcome into [purchaseState].
     *
     * A restore that finds nothing is still [PurchaseState.Completed] — "no purchases to
     * restore" is not a failure, so the UI decides by reading `customerInfo` afterwards.
     * A subscription held by another app user id surfaces as
     * [PurchaseState.FailureReason.ReceiptAlreadyInUse].
     */
    fun restorePurchases() {
        val operation = PurchaseState.Operation.Restore

        if (!isConfigured) {
            Log.e(TAG, "Restore attempted before RevenueCat was configured")
            _purchaseState.value = PurchaseState.Failed(
                operation,
                PurchaseState.FailureReason.BillingNotConfigured
            )
            return
        }

        _purchaseState.value = PurchaseState.InProgress(operation)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val customerInfo = Purchases.sharedInstance.awaitRestore()
                Log.d(TAG, "Successfully restored purchases")
                updateCustomerInfo(customerInfo)
                _purchaseState.value = PurchaseState.Completed(operation)
            } catch (e: PurchasesException) {
                Log.e(TAG, "Error restoring purchases: ${e.message}")
                _purchaseState.value = e.code.toPurchaseState(
                    userCancelled = false,
                    operation = operation
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception restoring purchases", e)
                _purchaseState.value = PurchaseState.Failed(
                    operation,
                    PurchaseState.FailureReason.Unknown
                )
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
     * Starts the purchase for a paywall offer id.
     *
     * Translates the id back into the SDK package the store needs. An id with no matching
     * package — a stale paywall after the offering changed — reports
     * [PurchaseState.FailureReason.ProductUnavailable] rather than throwing.
     */
    override fun purchase(activity: Activity, offerId: String) {
        val pkg = packagesById[offerId]
        if (pkg == null) {
            Log.e(TAG, "No package matches offer id: $offerId")
            _purchaseState.value = PurchaseState.Failed(
                PurchaseState.Operation.Purchase,
                PurchaseState.FailureReason.ProductUnavailable
            )
            return
        }
        purchasePackage(activity, pkg)
    }

    override fun restore() = restorePurchases()

    override fun refreshEntitlement() = refreshCustomerInfo()

    override fun refreshOffers() {
        _isOffersLoaded.value = false
        fetchOfferings()
    }

    override fun acknowledgePurchase() = acknowledgePurchaseState()

    /**
     * Clean up resources
     */
    fun cleanup() {
        Purchases.sharedInstance.updatedCustomerInfoListener = null
    }

    /**
     * The price a plan card shows for one product, plus the store's own discount for it.
     *
     * [discountPercent] and [strikethroughPriceText] are set only when Play reports a real
     * discount on the option that would actually be purchased, and both come from that
     * option's own `pricingPhases`: the phase charged first, and the full price it settles
     * into. Nothing is inferred from the monthly plan or from the package's length, so no
     * card can advertise a saving the store would not honour.
     *
     * When there is no such discount the base plan price is shown as-is, with no badge and
     * nothing struck through.
     */
    private fun planPricing(product: StoreProduct): PlanPricing {
        // `StoreProduct.price` is the base plan price for a Google subscription, and is the
        // fallback for every case below — including a product Play returned no options for.
        val basePlanPriceText = product.price.formatted
        val noDiscount = PlanPricing(basePlanPriceText, null, null)

        val option = product.defaultOption ?: return noDiscount
        val fullPhase = option.fullPricePhase ?: return noDiscount
        val discountedPhase = option.introPhase ?: return noDiscount
        // A free phase is a free trial, which the first phase being zero describes better
        // than a struck-through price beside a non-zero one would.
        if (option.freePhase != null) return noDiscount

        val percent = discountPercentOf(
            fullPriceMicros = fullPhase.price.amountMicros,
            discountedPriceMicros = discountedPhase.price.amountMicros,
        ) ?: return noDiscount

        return PlanPricing(
            priceText = discountedPhase.price.formatted,
            strikethroughPriceText = fullPhase.price.formatted,
            discountPercent = percent,
        )
    }
}

/** The paywall pricing for one product, all of it store-supplied text. */
private data class PlanPricing(
    val priceText: String,
    val strikethroughPriceText: String?,
    val discountPercent: Int?,
)