package com.mknlabs.expensetracker.feature.paywall.ui

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.monetization.PurchaseState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pins the mapping from a billing outcome to the copy the paywall shows.
 *
 * These are pure `@StringRes` ids, so the whole decision table is assertable without an
 * Android context. The two rules worth protecting are that a dismissal is never reported
 * as an error, and that a restore which found nothing never claims success.
 */
class PaywallCopyTest {

    // --- Silent outcomes ---------------------------------------------------------------

    @Test
    fun `idle reports nothing`() {
        assertNull(PurchaseState.Idle.messageRes())
    }

    @Test
    fun `in progress reports nothing because the UI already shows progress`() {
        assertNull(PurchaseState.InProgress(PurchaseState.Operation.Purchase).messageRes())
        assertNull(PurchaseState.InProgress(PurchaseState.Operation.Restore).messageRes())
    }

    @Test
    fun `a dismissed purchase sheet is never reported as an error`() {
        assertNull(PurchaseState.Cancelled(PurchaseState.Operation.Purchase).messageRes())
        assertNull(PurchaseState.Cancelled(PurchaseState.Operation.Restore).messageRes())
    }

    // --- Successes ---------------------------------------------------------------------

    @Test
    fun `a completed purchase reports success`() {
        assertEquals(
            R.string.msg_paywall_purchase_success,
            PurchaseState.Completed(PurchaseState.Operation.Purchase).messageRes()
        )
    }

    @Test
    fun `a restore that granted the entitlement reports success`() {
        assertEquals(
            R.string.msg_paywall_restore_success,
            purchaseMessageRes(
                PurchaseState.Completed(PurchaseState.Operation.Restore),
                isPremium = true
            )
        )
    }

    @Test
    fun `a restore that found nothing says so instead of claiming success`() {
        assertEquals(
            R.string.msg_paywall_restore_nothing_found,
            purchaseMessageRes(
                PurchaseState.Completed(PurchaseState.Operation.Restore),
                isPremium = false
            )
        )
    }

    @Test
    fun `a completed purchase is never reworded as a restore`() {
        // The restore branch keys on the operation, not on the entitlement, so a purchase
        // made by a user who was already premium still reads as a purchase.
        assertEquals(
            R.string.msg_paywall_purchase_success,
            purchaseMessageRes(
                PurchaseState.Completed(PurchaseState.Operation.Purchase),
                isPremium = true
            )
        )
    }

    // --- Non-failures that still need copy ---------------------------------------------

    @Test
    fun `a pending store payment is explained rather than reported as a failure`() {
        assertEquals(
            R.string.msg_paywall_payment_pending,
            PurchaseState.PaymentPending(PurchaseState.Operation.Purchase).messageRes()
        )
    }

    // --- Failures ----------------------------------------------------------------------

    @Test
    fun `every failure reason maps to its own message`() {
        val reasons = PurchaseState.FailureReason.entries
        val messages = reasons.map { it.messageRes() }

        // Catches a copy-paste mistake where two reasons share one string: the user would
        // be told the wrong thing, and nothing else in the build would notice.
        assertEquals(reasons.size, messages.distinct().size)
        assertEquals(reasons.size, messages.count { it != 0 })
    }

    @Test
    fun `a receipt already in use has dedicated copy, not a generic error`() {
        // This is the expected outcome when a second account tries to restore the original
        // purchaser's subscription, so it must not fall back to the generic message.
        val message = PurchaseState.FailureReason.ReceiptAlreadyInUse.messageRes()
        assertEquals(R.string.paywall_error_receipt_in_use, message)
        assertEquals(
            message,
            PurchaseState.Failed(
                PurchaseState.Operation.Restore,
                PurchaseState.FailureReason.ReceiptAlreadyInUse
            ).messageRes()
        )
    }

    @Test
    fun `a failure is reported the same way whether or not the user is premium`() {
        val state = PurchaseState.Failed(
            PurchaseState.Operation.Purchase,
            PurchaseState.FailureReason.Network
        )
        assertEquals(
            R.string.paywall_error_network,
            purchaseMessageRes(state, isPremium = false)
        )
        assertEquals(
            R.string.paywall_error_network,
            purchaseMessageRes(state, isPremium = true)
        )
    }
}
