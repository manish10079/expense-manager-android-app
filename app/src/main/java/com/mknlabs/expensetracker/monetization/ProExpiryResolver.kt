package com.mknlabs.expensetracker.monetization

import com.mknlabs.expensetracker.models.UserTier

/** What the Pro expiry monitor should do about the local Pro mirrors it watches. */
sealed interface ProExpiryAction {

    /** Nothing is due: no grant is running, and no mirror needs clearing. */
    data object None : ProExpiryAction

    /**
     * The locally recorded grant has lapsed and nothing else is granting Pro, so both mirrors
     * may be cleared.
     */
    data object Downgrade : ProExpiryAction

    /**
     * A locally recorded grant is still running: mirror Pro locally, then re-check when
     * [expiryMillis] arrives.
     */
    data class Hold(val expiryMillis: Long) : ProExpiryAction
}

/**
 * The single decision point for the "downgrade a user when their Pro expires" monitor.
 *
 * Extracted from `MainViewModel` so every branch is unit-testable. The monitor itself is
 * bound to DataStore, a real clock and an Android `Context`, so none of its conditions could
 * be exercised — and one of them was wrong in a way that quietly rewrote paying users:
 * deciding from `accountTier` and `proExpiryTimestamp` alone downgraded a subscriber to FREE
 * the moment an old ProPass expiry passed, because only the `redeemProPass` Cloud Function
 * ever writes `accountTier = "PREMIUM"`. A store subscription never does.
 *
 * That is why [resolvedTier] is an input. It comes from `EntitlementResolver`, where a live
 * store entitlement outranks every local field, so a subscription can never be undone here.
 *
 * The expiry comparison deliberately mirrors `EntitlementResolver`: `proExpiryTimestamp` in
 * `1..<now` counts as lapsed, `0` means "permanent grant, never expires", and the instant
 * equals `now` belongs to neither — so a grant can never be both live for the feature gates
 * and expired for this monitor.
 */
object ProExpiryResolver {

    /**
     * @param accountTier the server-authoritative tier on the profile, `"PREMIUM"` only after
     *   a ProPass redemption.
     * @param proExpiryTimestamp when that grant ends; `0` for a permanent grant.
     * @param resolvedTier the effective tier, which already accounts for the store.
     * @param now the current time, passed in so the boundaries are testable.
     */
    fun action(
        accountTier: String,
        proExpiryTimestamp: Long,
        resolvedTier: UserTier,
        now: Long,
    ): ProExpiryAction {
        // No locally recorded grant: a subscription needs no expiry monitor, since the store
        // reports its own state and the resolver reads it directly.
        if (accountTier != EntitlementResolver.PREMIUM_TIER) return ProExpiryAction.None

        if (proExpiryTimestamp > now) return ProExpiryAction.Hold(proExpiryTimestamp)

        // The same `in 1..<now` window `EntitlementResolver` treats as expired, so the two
        // rules can never disagree about one instant. It covers `0` (a permanent grant,
        // which is Pro forever) and `now` itself, so neither can be a downgrade.
        val isLapsed = proExpiryTimestamp in 1..<now
        if (!isLapsed) return ProExpiryAction.None

        // Lapsed. Downgrade only when nothing else grants Pro — otherwise this would clear
        // the mirrors of a subscriber who is still paying.
        return if (resolvedTier == UserTier.FREE) ProExpiryAction.Downgrade else ProExpiryAction.None
    }
}
