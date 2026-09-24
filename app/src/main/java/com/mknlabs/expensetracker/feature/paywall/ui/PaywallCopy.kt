package com.mknlabs.expensetracker.feature.paywall.ui

import androidx.annotation.StringRes
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.monetization.PurchaseState

/**
 * Turns a [PurchaseState] into the resource id of the message the paywall should show,
 * or null when there is nothing to say.
 *
 * <b>Why this lives here, not in the billing layer.</b> `PurchaseState` carries a typed,
 * string-free [PurchaseState.FailureReason] precisely so the wording can be decided by the
 * screen that shows it. This file is that decision, and it is a pure function over ids —
 * no `Context`, no `stringResource` — which makes the whole mapping assertable in a JVM
 * test and keeps the billing engine free of user-facing language.
 *
 * `null` is the deliberate answer for three cases:
 *  - [PurchaseState.Idle] — nothing has happened.
 *  - [PurchaseState.InProgress] — the UI already shows progress; a message would be noise.
 *  - [PurchaseState.Cancelled] — the user dismissed the Play sheet. Reporting that back as
 *    an error is the classic billing-UI bug this state exists to prevent.
 *
 * Both `when` blocks are exhaustive over their enums with no `else`, so adding a failure
 * reason or an operation is a compile error here rather than a silently unmapped string.
 */
@StringRes
internal fun PurchaseState.messageRes(): Int? = when (this) {
    is PurchaseState.Idle -> null
    is PurchaseState.InProgress -> null
    is PurchaseState.Cancelled -> null
    is PurchaseState.PaymentPending -> R.string.msg_paywall_payment_pending
    is PurchaseState.Completed -> when (operation) {
        PurchaseState.Operation.Purchase -> R.string.msg_paywall_purchase_success
        // A restore is only a success if something actually came back. The screen decides
        // which of the two restore messages applies by reading `isPremium` afterwards, so
        // this branch is the "something was found" case only.
        PurchaseState.Operation.Restore -> R.string.msg_paywall_restore_success
    }
    is PurchaseState.Failed -> reason.messageRes()
}

/**
 * The message for an outcome, including the one case the state cannot express alone.
 *
 * A restore that finds nothing is reported by the billing layer as a *completed* restore —
 * "there was nothing to restore" is not a failure. Telling the user "your purchase was
 * restored" when nothing was found would be false, so `isPremium` is consulted here: a
 * completed restore that left the user without the entitlement is the nothing-found case.
 */
@StringRes
internal fun purchaseMessageRes(state: PurchaseState, isPremium: Boolean): Int? = when {
    state is PurchaseState.Completed &&
        state.operation == PurchaseState.Operation.Restore &&
        !isPremium -> R.string.msg_paywall_restore_nothing_found

    else -> state.messageRes()
}

/**
 * The copy for a failed attempt, keyed on the reason alone.
 *
 * Exhaustive by construction: a new [PurchaseState.FailureReason] will not compile until
 * it has its own string, which is the point of keeping the reason a string-free enum.
 */
@StringRes
internal fun PurchaseState.FailureReason.messageRes(): Int = when (this) {
    PurchaseState.FailureReason.Network -> R.string.paywall_error_network
    PurchaseState.FailureReason.StoreProblem -> R.string.paywall_error_store_problem
    PurchaseState.FailureReason.NotAllowed -> R.string.paywall_error_not_allowed
    PurchaseState.FailureReason.ProductUnavailable -> R.string.paywall_error_product_unavailable
    PurchaseState.FailureReason.AlreadyOwned -> R.string.paywall_error_already_owned
    PurchaseState.FailureReason.ReceiptAlreadyInUse -> R.string.paywall_error_receipt_in_use
    PurchaseState.FailureReason.InvalidReceipt -> R.string.paywall_error_invalid_receipt
    PurchaseState.FailureReason.InvalidCredentials -> R.string.paywall_error_invalid_credentials
    PurchaseState.FailureReason.Configuration -> R.string.paywall_error_configuration
    PurchaseState.FailureReason.BillingNotConfigured -> R.string.paywall_error_billing_not_configured
    PurchaseState.FailureReason.Unknown -> R.string.paywall_error_unknown
}
