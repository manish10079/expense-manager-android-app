package com.mknlabs.expensetracker.domain.repository

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the mapping from the server's `details.reason` onto the typed refusal.
 *
 * The reason strings are a wire contract between `redeemProPass` and this build, so they are
 * written out as literals rather than referenced through the constants: a rename on either
 * side has to fail here, not silently demote every refusal to [RedemptionError.Unknown].
 */
class RedemptionErrorTest {

    @Test
    fun `a subscription refusal carries the expiry the server sent`() {
        assertEquals(
            RedemptionError.SubscriptionActive(1_790_306_107_150L),
            RedemptionError.fromReason("SUBSCRIPTION_ACTIVE", 1_790_306_107_150L)
        )
    }

    @Test
    fun `a subscription refusal without an expiry reports zero rather than guessing one`() {
        assertEquals(
            RedemptionError.SubscriptionActive(0L),
            RedemptionError.fromReason("SUBSCRIPTION_ACTIVE")
        )
    }

    @Test
    fun `each coupon refusal is told apart from the others`() {
        assertEquals(RedemptionError.InvalidCode, RedemptionError.fromReason("INVALID_CODE"))
        assertEquals(RedemptionError.Inactive, RedemptionError.fromReason("INACTIVE"))
        assertEquals(RedemptionError.Expired, RedemptionError.fromReason("EXPIRED"))
        assertEquals(RedemptionError.LimitReached, RedemptionError.fromReason("LIMIT_REACHED"))
        assertEquals(RedemptionError.AlreadyRedeemed, RedemptionError.fromReason("ALREADY_REDEEMED"))
    }

    @Test
    fun `a missing or unfamiliar reason is unknown rather than a borrowed sentence`() {
        assertEquals(RedemptionError.Unknown, RedemptionError.fromReason(null))
        assertEquals(RedemptionError.Unknown, RedemptionError.fromReason(""))
        assertEquals(RedemptionError.Unknown, RedemptionError.fromReason("SOMETHING_ELSE"))
    }
}
