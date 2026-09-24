package com.mknlabs.expensetracker.monetization

import com.mknlabs.expensetracker.models.UserTier
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the "downgrade when Pro expires" decision, which had no test coverage at all while it
 * lived inside `MainViewModel` — a collector bound to DataStore, a real clock and a `Context`.
 *
 * The case worth the suite is [a lapsed grant under a live subscription is left alone]: that
 * branch was wrong, and its effect was to rewrite a paying subscriber's tier to FREE and push
 * that to Firestore the moment an old ProPass expiry passed.
 */
class ProExpiryResolverTest {

    private val now = 1_700_000_000_000L

    private fun action(
        accountTier: String = TIER_PREMIUM,
        proExpiryTimestamp: Long,
        resolvedTier: UserTier,
    ): ProExpiryAction = ProExpiryResolver.action(
        accountTier = accountTier,
        proExpiryTimestamp = proExpiryTimestamp,
        resolvedTier = resolvedTier,
        now = now
    )

    // --- a grant that is still running ---

    @Test
    fun `a running grant is held until exactly its expiry`() {
        val expiry = now + TEN_DAYS

        assertEquals(
            ProExpiryAction.Hold(expiry),
            action(proExpiryTimestamp = expiry, resolvedTier = UserTier.PREMIUM)
        )
    }

    @Test
    fun `a running grant is held even if the resolved tier looks free`() {
        // Cannot happen through EntitlementResolver (a running grant makes the tier PREMIUM),
        // but the monitor must still not clear the mirrors on the strength of a tier read
        // taken at a moment the entitlement was not loaded.
        val expiry = now + TEN_DAYS

        assertEquals(
            ProExpiryAction.Hold(expiry),
            action(proExpiryTimestamp = expiry, resolvedTier = UserTier.FREE)
        )
    }

    // --- a grant that has lapsed ---

    @Test
    fun `a lapsed grant with nothing else granting pro is downgraded`() {
        assertEquals(
            ProExpiryAction.Downgrade,
            action(proExpiryTimestamp = now - 1_000, resolvedTier = UserTier.FREE)
        )
    }

    @Test
    fun `a lapsed grant under a live subscription is left alone`() {
        // The regression this suite exists for. A subscriber whose profile still carries an
        // old ProPass expiry is Pro by the store's verdict, and must not be rewritten to FREE.
        assertEquals(
            ProExpiryAction.None,
            action(proExpiryTimestamp = now - 1_000, resolvedTier = UserTier.PREMIUM)
        )
    }

    // --- grants that never expire ---

    @Test
    fun `a permanent grant is never downgraded`() {
        assertEquals(
            ProExpiryAction.None,
            action(proExpiryTimestamp = 0L, resolvedTier = UserTier.PREMIUM)
        )
    }

    @Test
    fun `a permanent grant is not downgraded even if the tier reads free`() {
        // `0` means "no expiry recorded", which is Pro permanently, not "expired at epoch" —
        // the reasoning that used to print "01 Jan 1970" on the membership card.
        assertEquals(
            ProExpiryAction.None,
            action(proExpiryTimestamp = 0L, resolvedTier = UserTier.FREE)
        )
    }

    // --- states the monitor has no business touching ---

    @Test
    fun `a subscription-only user is never a downgrade candidate`() {
        // A store subscription never writes this tier, so there is no local grant to expire —
        // and the store reports the subscription's end itself.
        assertEquals(ProExpiryAction.None, action("FREE", now - 1_000, UserTier.FREE))
        assertEquals(ProExpiryAction.None, action("", 0L, UserTier.FREE))
        assertEquals(ProExpiryAction.None, action("", now + TEN_DAYS, UserTier.FREE))
    }

    @Test
    fun `a pro tier with no recorded expiry at all is left alone`() {
        assertEquals(ProExpiryAction.None, action(TIER_PREMIUM, 0L, UserTier.FREE))
    }

    // --- boundaries ---

    @Test
    fun `the expiry instant is decided the same way as the entitlement resolver`() {
        // `in 1..<now` excludes `now` itself, exactly as EntitlementResolver does, so the two
        // rules cannot disagree about one instant. At that instant the gates already read
        // FREE while the mirrors are still held — which is the harmless direction to be wrong
        // in: clearing a mirror a millisecond early for a paying user is not.
        assertEquals(
            ProExpiryAction.None,
            action(proExpiryTimestamp = now, resolvedTier = UserTier.FREE)
        )
        assertEquals(
            ProExpiryAction.Downgrade,
            action(proExpiryTimestamp = now - 1, resolvedTier = UserTier.FREE)
        )
    }

    @Test
    fun `a grant is held while it is in the future by a single millisecond`() {
        assertEquals(
            ProExpiryAction.Hold(now + 1),
            action(proExpiryTimestamp = now + 1, resolvedTier = UserTier.PREMIUM)
        )
    }

    private companion object {
        const val TIER_PREMIUM = "PREMIUM"
        const val TEN_DAYS = 1_000L * 60 * 60 * 24 * 10
    }
}
