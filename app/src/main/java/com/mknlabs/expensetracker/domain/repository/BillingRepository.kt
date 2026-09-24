package com.mknlabs.expensetracker.domain.repository

import android.app.Activity
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import com.mknlabs.expensetracker.monetization.SubscriptionOffer
import kotlinx.coroutines.flow.StateFlow

/**
 * The billing surface the paywall depends on.
 *
 * Exists so the paywall's ViewModel can be unit-tested with a hand-written fake, as the
 * project's testing rules require, instead of against the concrete RevenueCat manager —
 * which cannot be constructed in a JVM test because it configures the SDK in its
 * constructor.
 *
 * <b>No RevenueCat type appears in this contract.</b> Offers arrive as
 * [SubscriptionOffer] and outcomes as [PurchaseState]; both are plain data, which keeps
 * the SDK an implementation detail of the data layer and stops SDK identifiers reaching
 * the UI.
 *
 * `Activity` is the one platform type here, and it is unavoidable: RevenueCat's purchase
 * call requires the hosting Activity for the Play sheet's result. It is a parameter only,
 * never retained.
 */
interface BillingRepository {

    /** The default offering's plans, for display. Empty until offerings load. */
    val offers: StateFlow<List<SubscriptionOffer>>

    /**
     * True once an offerings fetch has finished, whether it succeeded or failed.
     *
     * Needed because an empty [offers] list is otherwise ambiguous: the paywall would show
     * "loading" forever on a dead network instead of a retry.
     */
    val isOffersLoaded: StateFlow<Boolean>

    /** The outcome of the most recent purchase or restore attempt. */
    val purchaseState: StateFlow<PurchaseState>

    /** Whether the `premium` entitlement is active right now. */
    val isPremium: StateFlow<Boolean>

    /**
     * The store's view of the active `premium` entitlement — its end date, whether it will
     * renew, and whether payment collection has failed — or null when there is no active
     * entitlement.
     *
     * [isPremium] answers *whether* someone is Pro; this answers *until when*, which only
     * the store knows. Read here rather than from Firestore so a renewal is described
     * correctly the moment RevenueCat reports it, without waiting for a sync.
     */
    val storeEntitlement: StateFlow<StoreEntitlement?>

    /**
     * Whether the user should see ads.
     *
     * Deliberately *wider* than [isPremium]: true when either the `premium` entitlement or
     * the standalone ad-free entitlement is active, so an ad-free-only purchase is honored
     * without also granting the Pro feature set. Ad logic reads this; feature gates read
     * [isPremium].
     */
    val isAdFree: StateFlow<Boolean>

    /**
     * The store's own URL for managing the active subscription, or null when this user has
     * nothing to manage.
     *
     * Comes from RevenueCat's `CustomerInfo.managementURL`, which is the store's
     * authoritative page for the subscription. The paywall opens this rather than building
     * a billing URL of its own, so a store-side change cannot leave the app pointing at a
     * dead link.
     */
    val managementUrl: StateFlow<String?>

    /** Starts the purchase of [offerId] as an [SubscriptionOffer.id]. */
    fun purchase(activity: Activity, offerId: String)

    /** Restores previous purchases for the signed-in app user id. */
    fun restore()

    /** Re-fetches offerings, returning [isOffersLoaded] to false while in flight. */
    fun refreshOffers()

    /** Returns [purchaseState] to idle once the UI has shown its outcome. */
    fun acknowledgePurchase()
}
