package com.mknlabs.expensetracker.monetization

import com.mknlabs.expensetracker.models.UserTier

/**
 * The single decision point for "is this user Pro?".
 *
 * Three independent sources can grant Pro, and they disagree in ways that matter:
 *
 *  - **A live store entitlement** ([revenueCatEntitlementActive]) — the store's own verdict,
 *    refreshed after every purchase, restore and login. It wins outright: it is the only
 *    source that reflects a real payment, and the only one that cannot go stale.
 *  - **A ProPass grant** — persisted by the `redeemProPass` Cloud Function as
 *    `accountTier = "PREMIUM"` with `proExpiryTimestamp` set to the grant's end, so it is
 *    time-limited and expires by itself.
 *  - **The local `AppSettings.userTier` mirror** — retained for installs that predate the
 *    server-authoritative fields.
 *
 * <b>Why the store entitlement short-circuits instead of being OR-ed into the expiry
 * maths.</b> A subscription's local `proExpiryTimestamp` is a *cache* of a fact only the
 * store knows; a renewal it has not learned about yet would read as expired and lock a
 * paying subscriber out. The entitlement check has no such failure mode, so it is decided
 * first and never weakened by a stale local timestamp.
 *
 * Pure by construction — no Android, no DataStore, no clock of its own — so every
 * combination is unit-testable without a device.
 */
object EntitlementResolver {

    /**
     * The `accountTier` value meaning Pro, as written by the `redeemProPass` function.
     *
     * Shared with [ProExpiryResolver] rather than repeated, so the two rules cannot come to
     * disagree about which tier string counts as Pro.
     */
    internal const val PREMIUM_TIER = "PREMIUM"

    /**
     * @param appSettingsTier the locally mirrored tier.
     * @param accountTier the server-authoritative tier string on the user's profile.
     * @param proExpiryTimestamp when the ProPass grant ends; `0` means "no expiry recorded".
     * @param revenueCatEntitlementActive whether the store reports an active `premium`
     *   entitlement right now.
     * @param now the current time, passed in so the expiry rules are deterministic in tests.
     */
    fun isPremium(
        appSettingsTier: UserTier,
        accountTier: String,
        proExpiryTimestamp: Long,
        revenueCatEntitlementActive: Boolean,
        now: Long,
    ): Boolean {
        // The store is authoritative and current; nothing local can override it.
        if (revenueCatEntitlementActive) return true

        val isExpired = accountTier == PREMIUM_TIER && proExpiryTimestamp in 1..<now

        return !isExpired && (
            appSettingsTier == UserTier.PREMIUM ||
                (accountTier == PREMIUM_TIER &&
                    (proExpiryTimestamp == 0L || proExpiryTimestamp > now))
            )
    }
}
