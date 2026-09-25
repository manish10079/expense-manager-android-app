package com.mknlabs.expensetracker.data.repository

import com.google.firebase.functions.FirebaseFunctionsException
import com.mknlabs.expensetracker.domain.repository.RedemptionError
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins which half of a failed call decides the refusal.
 *
 * The HttpsError code leads where it names the problem itself — a signed-out caller is not a
 * coupon complaint, whatever reason came with it — and the server's `reason` takes over for the
 * refusals that share `failed-precondition`.
 */
class ProPassErrorMappingTest {

    @Test
    fun `a call that never reached the function is a network failure`() {
        assertEquals(RedemptionError.Network, redemptionErrorOf(IOException("offline")))
    }

    @Test
    fun `a failure that is not a failed call is unknown`() {
        assertEquals(RedemptionError.Unknown, redemptionErrorOf(IllegalStateException("boom")))
    }

    @Test
    fun `a signed-out caller is refused as signed out, whatever reason arrived`() {
        assertEquals(
            RedemptionError.NotSignedIn,
            redemptionErrorFrom(FirebaseFunctionsException.Code.UNAUTHENTICATED, "INVALID_CODE", 0L)
        )
        assertEquals(
            RedemptionError.NotSignedIn,
            redemptionErrorFrom(FirebaseFunctionsException.Code.PERMISSION_DENIED, null, 0L)
        )
    }

    @Test
    fun `an unknown code and a spent code are told apart by the code alone`() {
        assertEquals(
            RedemptionError.InvalidCode,
            redemptionErrorFrom(FirebaseFunctionsException.Code.NOT_FOUND, null, 0L)
        )
        assertEquals(
            RedemptionError.AlreadyRedeemed,
            redemptionErrorFrom(FirebaseFunctionsException.Code.ALREADY_EXISTS, null, 0L)
        )
    }

    @Test
    fun `the refusals that share failed-precondition are separated by the server's reason`() {
        assertEquals(
            RedemptionError.Expired,
            redemptionErrorFrom(FirebaseFunctionsException.Code.FAILED_PRECONDITION, "EXPIRED", 0L)
        )
        assertEquals(
            RedemptionError.SubscriptionActive(42L),
            redemptionErrorFrom(
                FirebaseFunctionsException.Code.FAILED_PRECONDITION,
                "SUBSCRIPTION_ACTIVE",
                42L
            )
        )
        assertEquals(
            RedemptionError.Unknown,
            redemptionErrorFrom(FirebaseFunctionsException.Code.FAILED_PRECONDITION, null, 0L)
        )
    }

    @Test
    fun `a running pass is refused with the pass's own expiry`() {
        assertEquals(
            RedemptionError.PassActive(7L),
            redemptionErrorFrom(
                FirebaseFunctionsException.Code.FAILED_PRECONDITION,
                "PASS_ACTIVE",
                7L
            )
        )
        assertEquals(
            RedemptionError.PassActive(0L),
            redemptionErrorFrom(
                FirebaseFunctionsException.Code.FAILED_PRECONDITION,
                "PASS_ACTIVE",
                0L
            )
        )
    }

    @Test
    fun `the blocking expiry is read from whichever key the server sent`() {
        assertEquals(11L, blockingExpiryMillis(mapOf("subscriptionExpiry" to 11L)))
        assertEquals(22L, blockingExpiryMillis(mapOf("passExpiry" to 22L)))
        assertEquals(0L, blockingExpiryMillis(mapOf("reason" to "PASS_ACTIVE")))
        assertEquals(0L, blockingExpiryMillis(null))
    }

    @Test
    fun `an unrelated failure is unknown rather than a guess`() {
        assertEquals(
            RedemptionError.Unknown,
            redemptionErrorFrom(FirebaseFunctionsException.Code.INTERNAL, "EXPIRED", 0L)
        )
        assertEquals(
            RedemptionError.Unknown,
            redemptionErrorFrom(FirebaseFunctionsException.Code.UNAVAILABLE, null, 0L)
        )
    }
}
