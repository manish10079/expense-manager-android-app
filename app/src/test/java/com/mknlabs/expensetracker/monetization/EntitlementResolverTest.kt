package com.mknlabs.expensetracker.monetization

import com.mknlabs.expensetracker.models.UserTier
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the rules that decide whether a user is Pro.
 *
 * These matters because the three sources disagree: the store entitlement is current, a
 * ProPass grant is time-limited, and the local tier mirror can be arbitrarily stale. The
 * case worth protecting above all others is the first one — a subscriber whose cached
 * local expiry has lapsed but whose store entitlement is live must **stay** Pro, or a
 * renewal the app has not learned about yet locks them out of what they paid for.
 */
class EntitlementResolverTest {

    private val now = 1_000_000L

    private fun isPremium(
        appSettingsTier: UserTier = UserTier.FREE,
        accountTier: String = "FREE",
        proExpiryTimestamp: Long = 0L,
        revenueCatEntitlementActive: Boolean = false,
    ) = EntitlementResolver.isPremium(
        appSettingsTier = appSettingsTier,
        accountTier = accountTier,
        proExpiryTimestamp = proExpiryTimestamp,
        revenueCatEntitlementActive = revenueCatEntitlementActive,
        now = now,
    )

    // --- The store entitlement outranks everything local --------------------------------

    @Test
    fun `a live store entitlement is Pro`() {
        assertTrue(isPremium(revenueCatEntitlementActive = true))
    }

    @Test
    fun `a live store entitlement survives a lapsed local expiry`() {
        // The whole point: local expiry is a cache of a fact only the store knows, so it
        // must never be allowed to override the store's own verdict.
        assertTrue(
            isPremium(
                accountTier = "PREMIUM",
                proExpiryTimestamp = now - 1,
                revenueCatEntitlementActive = true,
            )
        )
    }

    @Test
    fun `a live store entitlement overrides a locally FREE tier`() {
        assertTrue(
            isPremium(
                appSettingsTier = UserTier.FREE,
                accountTier = "FREE",
                proExpiryTimestamp = 0L,
                revenueCatEntitlementActive = true,
            )
        )
    }

    // --- ProPass grants, which are time-limited ----------------------------------------

    @Test
    fun `an unexpired ProPass grant is Pro`() {
        assertTrue(isPremium(accountTier = "PREMIUM", proExpiryTimestamp = now + 60_000))
    }

    @Test
    fun `an expired ProPass grant is not Pro`() {
        assertFalse(isPremium(accountTier = "PREMIUM", proExpiryTimestamp = now - 1))
    }

    @Test
    fun `a grant expiring exactly now counts as expired`() {
        // Boundary pinned deliberately: the expiry instant is inclusive of expiry, not of
        // validity, so access never outlives the grant by a moment.
        assertFalse(isPremium(accountTier = "PREMIUM", proExpiryTimestamp = now))
    }

    @Test
    fun `a legacy grant with no recorded expiry is Pro`() {
        // `0` means "no expiry recorded", which predates time-limited grants. Kept as Pro
        // so existing holders are not silently downgraded.
        assertTrue(isPremium(accountTier = "PREMIUM", proExpiryTimestamp = 0L))
    }

    @Test
    fun `an expiry of one millisecond in the past is expired`() {
        // Guards the `in 1..<now` range from swallowing a small-but-real timestamp.
        assertFalse(isPremium(accountTier = "PREMIUM", proExpiryTimestamp = 1L))
    }

    // --- The legacy local mirror -------------------------------------------------------

    @Test
    fun `the legacy local tier alone still grants Pro`() {
        assertTrue(isPremium(appSettingsTier = UserTier.PREMIUM, accountTier = "FREE"))
    }

    @Test
    fun `an expired grant invalidates the legacy local mirror too`() {
        // Preserves the behaviour that predates this change: once a grant is known to have
        // expired, the locally mirrored PREMIUM flag must not keep granting access.
        assertFalse(
            isPremium(
                appSettingsTier = UserTier.PREMIUM,
                accountTier = "PREMIUM",
                proExpiryTimestamp = now - 1,
            )
        )
    }

    // --- No source at all ---------------------------------------------------------------

    @Test
    fun `no source means not Pro`() {
        assertFalse(isPremium())
    }

    @Test
    fun `a non-PREMIUM account tier is not Pro`() {
        assertFalse(isPremium(accountTier = "FREE", proExpiryTimestamp = now + 60_000))
    }
}
