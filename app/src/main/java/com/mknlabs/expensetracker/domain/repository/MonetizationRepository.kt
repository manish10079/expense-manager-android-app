package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import kotlinx.coroutines.flow.Flow

interface MonetizationRepository {
    /**
     * Unified reactive stream for the user's current effective tier.
     */
    val userTier: Flow<UserTier>

    /**
     * Emits true if ads should be shown, false if the user is premium or has an active pass.
     */
    val isAdsEnabled: Flow<Boolean>

    /**
     * True when the user's Pro comes from a **store** purchase rather than a ProPass grant.
     *
     * [userTier] says *whether* someone is Pro, which is not enough for screens that have to
     * offer the right next step: a subscriber has something to manage in the store, while a
     * ProPass holder has a time-limited grant and no subscription — so the one thing worth
     * offering them is the store's plans. Sourced from the `premium` entitlement, which only
     * a completed store purchase can activate.
     */
    val hasActiveStoreSubscription: Flow<Boolean>

    /**
     * The store's view of the active `premium` entitlement, or null when there is none.
     *
     * Carries what [hasActiveStoreSubscription] cannot: the end date and whether it renews,
     * so a screen can state a subscriber's real renewal instead of inventing one from a
     * locally mirrored timestamp.
     */
    val storeEntitlement: Flow<StoreEntitlement?>

    /**
     * Emits the timestamp (ms) when the current temporary ad-free pass expires.
     */
    val globalAdAccessExpiry: Flow<Long>

    /**
     * Observes the access status for a specific feature or option.
     */
    fun observeAccessStatus(feature: Feature, optionId: String? = null): Flow<AccessStatus>

    /**
     * Grants temporary access to a feature (e.g., after watching an ad).
     */
    suspend fun grantTemporaryAccess(feature: Feature, optionId: String? = null, durationMillis: Long)
}
