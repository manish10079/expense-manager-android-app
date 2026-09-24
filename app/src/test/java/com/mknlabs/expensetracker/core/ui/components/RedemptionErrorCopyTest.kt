package com.mknlabs.expensetracker.core.ui.components

import com.mknlabs.expensetracker.domain.repository.RedemptionError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Pins the copy the redemption dialog shows.
 *
 * The point of the typed refusals is that a user is told which problem they have, so the one
 * thing this table must never do is word two different refusals the same way: that would make
 * the taxonomy invisible and send the user back to guessing what went wrong.
 */
class RedemptionErrorCopyTest {

    /**
     * Every refusal a failed call can produce. Kept as a list rather than a `when` so that
     * adding an error type without copy here is a test that still passes on the old list —
     * the compiler already refuses a missing branch in the mapper itself.
     */
    private val allErrors = listOf(
        RedemptionError.NotSignedIn,
        RedemptionError.InvalidCode,
        RedemptionError.Inactive,
        RedemptionError.Expired,
        RedemptionError.LimitReached,
        RedemptionError.AlreadyRedeemed,
        RedemptionError.SubscriptionActive(1_790_306_107_150L),
        RedemptionError.Network,
        RedemptionError.Unknown,
    )

    @Test
    fun `no refusal is worded the same as another`() {
        val ids = allErrors.map { redemptionErrorMessageRes(it) }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `a subscription refusal is not reported as a bad code`() {
        assertNotEquals(
            redemptionErrorMessageRes(RedemptionError.InvalidCode),
            redemptionErrorMessageRes(RedemptionError.SubscriptionActive(0L))
        )
    }
}
