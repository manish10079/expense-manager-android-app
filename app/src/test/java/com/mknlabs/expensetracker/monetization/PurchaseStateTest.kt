package com.mknlabs.expensetracker.monetization

import com.revenuecat.purchases.PurchasesErrorCode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Covers the RevenueCat error -> [PurchaseState] mapping that feeds the paywall UI.
 *
 * Two things are locked down here:
 *  1. Cancellation and pending payment classify as their own states, never as failures.
 *     Showing an "error" when the user simply dismissed the Play sheet, or when the store
 *     accepted a delayed payment, would be wrong copy for a successful interaction.
 *  2. The reason table, including [PurchaseState.FailureReason.ReceiptAlreadyInUse] — the
 *     answer this app returns when a second account tries to restore the original
 *     purchaser's subscription, which the UI words differently from a generic failure.
 *
 * The mapping takes plain values, so it needs no Android context and no RevenueCat error
 * objects. An unrecognised code must degrade to [PurchaseState.FailureReason.Unknown]
 * rather than throw, because `PurchasesErrorCode` gains members across SDK upgrades.
 */
class PurchaseStateTest {

    // --- cancellation and pending payment are not failures ---

    @Test
    fun `the userCancelled flag wins over the error code`() {
        val state = PurchasesErrorCode.UnknownError.toPurchaseState(
            userCancelled = true,
            operation = PurchaseState.Operation.Purchase
        )

        assertEquals(PurchaseState.Cancelled(PurchaseState.Operation.Purchase), state)
    }

    @Test
    fun `a cancellation code classifies as cancelled even without the flag`() {
        val state = PurchasesErrorCode.PurchaseCancelledError.toPurchaseState(
            userCancelled = false,
            operation = PurchaseState.Operation.Restore
        )

        assertEquals(PurchaseState.Cancelled(PurchaseState.Operation.Restore), state)
    }

    @Test
    fun `a pending payment is reported as pending and not as a failure`() {
        val state = PurchasesErrorCode.PaymentPendingError.toPurchaseState(
            userCancelled = false,
            operation = PurchaseState.Operation.Purchase
        )

        assertEquals(PurchaseState.PaymentPending(PurchaseState.Operation.Purchase), state)
    }

    // --- failures keep the reason and the operation ---

    @Test
    fun `operational failures map to their own reason`() {
        assertReason(PurchasesErrorCode.NetworkError, PurchaseState.FailureReason.Network)
        assertReason(PurchasesErrorCode.StoreProblemError, PurchaseState.FailureReason.StoreProblem)
        assertReason(
            PurchasesErrorCode.ProductNotAvailableForPurchaseError,
            PurchaseState.FailureReason.ProductUnavailable
        )
        assertReason(PurchasesErrorCode.ProductAlreadyPurchasedError, PurchaseState.FailureReason.AlreadyOwned)
        assertReason(PurchasesErrorCode.InvalidReceiptError, PurchaseState.FailureReason.InvalidReceipt)
        assertReason(PurchasesErrorCode.MissingReceiptFileError, PurchaseState.FailureReason.InvalidReceipt)
        assertReason(PurchasesErrorCode.InvalidCredentialsError, PurchaseState.FailureReason.InvalidCredentials)
    }

    @Test
    fun `permission and configuration codes map as the paywall assumes`() {
        assertReason(PurchasesErrorCode.PurchaseNotAllowedError, PurchaseState.FailureReason.NotAllowed)
        assertReason(PurchasesErrorCode.InsufficientPermissionsError, PurchaseState.FailureReason.NotAllowed)
        assertReason(PurchasesErrorCode.ConfigurationError, PurchaseState.FailureReason.Configuration)
        assertReason(PurchasesErrorCode.PurchaseInvalidError, PurchaseState.FailureReason.Configuration)
    }

    @Test
    fun `a receipt held by another account is its own reason`() {
        // Expected under this project's restore behaviour: a second app account on the same
        // device cannot take the original purchaser's receipt, and gets this reason.
        assertReason(
            PurchasesErrorCode.ReceiptAlreadyInUseError,
            PurchaseState.FailureReason.ReceiptAlreadyInUse
        )
    }

    @Test
    fun `a failure records which operation was running`() {
        assertEquals(
            PurchaseState.Failed(PurchaseState.Operation.Purchase, PurchaseState.FailureReason.Network),
            PurchasesErrorCode.NetworkError.toPurchaseState(
                userCancelled = false,
                operation = PurchaseState.Operation.Purchase
            )
        )
        assertEquals(
            PurchaseState.Failed(PurchaseState.Operation.Restore, PurchaseState.FailureReason.Network),
            PurchasesErrorCode.NetworkError.toPurchaseState(
                userCancelled = false,
                operation = PurchaseState.Operation.Restore
            )
        )
    }

    // --- forward compatibility ---

    @Test
    fun `only the expected codes fall back to unknown`() {
        // Pins the entire table at once. A new PurchasesErrorCode from an SDK upgrade lands
        // in Unknown and fails here, so classifying it becomes a decision instead of a
        // silent default. PurchaseCancelledError and PaymentPendingError belong in this set:
        // toPurchaseState turns those into their own states and never reports them as failures.
        val unmapped = PurchasesErrorCode.entries
            .filter { it.toFailureReason() == PurchaseState.FailureReason.Unknown }
            .toSet()

        assertEquals(
            setOf(
                PurchasesErrorCode.UnknownError,
                PurchasesErrorCode.PurchaseCancelledError,
                PurchasesErrorCode.UnexpectedBackendResponseError,
                PurchasesErrorCode.InvalidAppUserIdError,
                PurchasesErrorCode.OperationAlreadyInProgressError,
                PurchasesErrorCode.UnknownBackendError,
                PurchasesErrorCode.InvalidAppleSubscriptionKeyError,
                PurchasesErrorCode.IneligibleError,
                PurchasesErrorCode.PaymentPendingError,
                PurchasesErrorCode.InvalidSubscriberAttributesError,
                PurchasesErrorCode.LogOutWithAnonymousUserError,
                PurchasesErrorCode.UnsupportedError,
                PurchasesErrorCode.EmptySubscriberAttributesError,
                PurchasesErrorCode.CustomerInfoError,
                PurchasesErrorCode.SignatureVerificationError,
                PurchasesErrorCode.TestStoreSimulatedPurchaseError
            ),
            unmapped
        )
    }

    private fun assertReason(code: PurchasesErrorCode, expected: PurchaseState.FailureReason) {
        assertEquals(
            PurchaseState.Failed(PurchaseState.Operation.Purchase, expected),
            code.toPurchaseState(userCancelled = false, operation = PurchaseState.Operation.Purchase)
        )
    }
}
