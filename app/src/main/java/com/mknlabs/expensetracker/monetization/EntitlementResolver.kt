package com.mknlabs.expensetracker.monetization

import com.mknlabs.expensetracker.models.UserTier

/**
 * The single decision point for "is this user Pro?".
 *
 * Independent sources can grant Pro, and they disagree in ways that matter:
 *
 *  - **A live store entitlement** ([revenueCatEntitlementActive]) — the store's own verdict,
 *    refreshed after every purchase, restore and login. It wins outright: it is the only
 *    source that reflects a real payment, and the only one that cannot go stale.
 *  - **A ProPass grant** — persisted by the `redeemProPass` Cloud Function as
 *    `accountTier = "Pro_Pass"` or legacy `"PREMIUM"` with `proExpiryTimestamp` set to the
 *    grant's end, so it is time-limited and expires by itself.
 *
 * A cancelled or expired Play subscription must not stay Pro via `Paid_Subscription` or the
 * local `AppSettings.userTier` mirror. Those are caches of a store fact that is already gone.
 *
 * Pure by construction — no Android, no DataStore, no clock of its own — so every
 * combination is unit-testable without a device.
 */
object EntitlementResolver {

    const val TIER_FREE = "Free"
    const val TIER_PRO_PASS = "Pro_Pass"
    const val TIER_PAID_SUBSCRIPTION = "Paid_Subscription"

    // Legacy support
    internal const val LEGACY_PREMIUM_TIER = "PREMIUM"
    internal const val LEGACY_FREE_TIER = "FREE"

    /** Deprecated alias maintained for backwards compatibility in existing tests/callers. */
    internal const val PREMIUM_TIER = LEGACY_PREMIUM_TIER

    /** Returns true if the given accountTier string represents any active Pro tier. */
    fun isProTier(accountTier: String): Boolean {
        return accountTier == TIER_PRO_PASS ||
            accountTier == TIER_PAID_SUBSCRIPTION ||
            accountTier == LEGACY_PREMIUM_TIER
    }

    /**
     * @param appSettingsTier the locally mirrored tier (ignored unless the store or a pass grants Pro).
     * @param accountTier the server-authoritative tier string on the user's profile.
     * @param proExpiryTimestamp when the ProPass grant ends; `0` means "no expiry recorded".
     * @param revenueCatEntitlementActive whether the store reports an active `premium`
     *   entitlement right now.
     * @param now the current time, passed in so the expiry rules are deterministic in tests.
     * @param isSignedIn false after logout / guest-anonymous: Pro is account-bound, so the
     *   membership card and gates must not keep a previous paid or redeemed grant.
     */
    fun isPremium(
        appSettingsTier: UserTier,
        accountTier: String,
        proExpiryTimestamp: Long,
        revenueCatEntitlementActive: Boolean,
        now: Long,
        isSignedIn: Boolean = true,
    ): Boolean {
        if (!isSignedIn) return false
        if (revenueCatEntitlementActive) return true
        return hasActiveProPassGrant(accountTier, proExpiryTimestamp, now)
    }

    /** True when Pro came from a redeemed pass, not from Google Play. */
    fun hasActiveProPassGrant(accountTier: String, proExpiryTimestamp: Long, now: Long): Boolean {
        val isPass = accountTier == TIER_PRO_PASS || accountTier == LEGACY_PREMIUM_TIER
        if (!isPass) return false
        if (proExpiryTimestamp == 0L) return true
        return proExpiryTimestamp > now
    }
}
