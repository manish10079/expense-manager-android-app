package com.mknlabs.expensetracker.monetization

import com.revenuecat.purchases.PurchasesErrorCode

/**
 * Observable outcome of a purchase or restore attempt, for the paywall UI.
 *
 * <b>This model intentionally contains no user-facing text.</b> The UI maps
 * [FailureReason] to a `@StringRes` id and resolves it with `stringResource(...)`, so the
 * copy stays translatable and no English lives in the billing layer. RevenueCat's own
 * `message` / `underlyingErrorMessage` are English developer diagnostics: `BillingManager`
 * logs them and never places them in this state, because surfacing them would leak
 * untranslated text into the UI.
 *
 * [Completed] does not by itself mean the user is now premium. Entitlement truth is always
 * read from `BillingManager.customerInfo`, which is refreshed from the store's
 * `CustomerInfo` after every attempt.
 */
sealed class PurchaseState {

    /** No attempt in flight and nothing to report. The resting state. */
    object Idle : PurchaseState()

    /** An attempt is running; the UI should disable its buy and restore actions. */
    data class InProgress(val operation: Operation) : PurchaseState()

    /** The store finished the flow. Read `customerInfo` for the resulting entitlement. */
    data class Completed(val operation: Operation) : PurchaseState()

    /**
     * The user dismissed the Play purchase sheet. Not an error, so it is deliberately not
     * a [Failed] — the UI should simply re-enable its actions, with no error message.
     */
    data class Cancelled(val operation: Operation) : PurchaseState()

    /**
     * The store accepted a non-immediate payment method (bank transfer, cash, ...). The
     * entitlement arrives asynchronously once the payment settles, so this is also not a
     * [Failed]; the UI should explain the delay rather than report a failure.
     */
    data class PaymentPending(val operation: Operation) : PurchaseState()

    /** The attempt failed. [reason] is the cause the UI turns into translated copy. */
    data class Failed(val operation: Operation, val reason: FailureReason) : PurchaseState()

    /** Which action produced this state, so the UI can word its confirmation correctly. */
    enum class Operation {
        Purchase,
        Restore,
    }

    /**
     * Typed, string-free cause of a [Failed].
     *
     * The UI owns the wording: each value maps to exactly one entry in `strings.xml`.
     */
    enum class FailureReason {
        /** No usable connection to the store or RevenueCat. */
        Network,

        /** Play Billing itself failed — service unavailable, timeout, store-side error. */
        StoreProblem,

        /** The device, user, or app build is not permitted to transact. */
        NotAllowed,

        /**
         * The product is not purchasable. Usually it is not live in Play Console yet, or
         * this tester has not been opted in on the device.
         */
        ProductUnavailable,

        /** Already owned on this store account. */
        AlreadyOwned,

        /**
         * The store receipt is already attached to a different app user id.
         *
         * With this project's restore behaviour this is the *expected* answer when a
         * second account on the same device tries to restore the original purchaser's
         * subscription, and the UI should say so rather than showing a generic failure.
         */
        ReceiptAlreadyInUse,

        /** The receipt was rejected as invalid, or is missing. */
        InvalidReceipt,

        /** RevenueCat rejected the credentials — normally a wrong or rotated API key. */
        InvalidCredentials,

        /** Bad or contradictory arguments, or a RevenueCat configuration problem. */
        Configuration,

        /** RevenueCat was never configured — normally a missing `revenueCatApiKey`. */
        BillingNotConfigured,

        /** Anything unrecognised, including error codes added by a future SDK. */
        Unknown,
    }
}

/**
 * Maps a RevenueCat error code to its translatable cause.
 *
 * `PurchasesErrorCode` is a foreign enum that grows between SDK upgrades, so the final
 * `else` is intentional: an unrecognised code degrades to
 * [PurchaseState.FailureReason.Unknown] instead of crashing the paywall.
 *
 * [PurchasesErrorCode.PurchaseCancelledError] and [PurchasesErrorCode.PaymentPendingError]
 * have no reason here on purpose — [toPurchaseState] turns those into their own states
 * rather than failures.
 */
internal fun PurchasesErrorCode.toFailureReason(): PurchaseState.FailureReason = when (this) {
    PurchasesErrorCode.NetworkError ->
        PurchaseState.FailureReason.Network

    PurchasesErrorCode.StoreProblemError ->
        PurchaseState.FailureReason.StoreProblem

    PurchasesErrorCode.PurchaseNotAllowedError,
    PurchasesErrorCode.InsufficientPermissionsError ->
        PurchaseState.FailureReason.NotAllowed

    PurchasesErrorCode.ProductNotAvailableForPurchaseError ->
        PurchaseState.FailureReason.ProductUnavailable

    PurchasesErrorCode.ProductAlreadyPurchasedError ->
        PurchaseState.FailureReason.AlreadyOwned

    PurchasesErrorCode.ReceiptAlreadyInUseError ->
        PurchaseState.FailureReason.ReceiptAlreadyInUse

    PurchasesErrorCode.InvalidReceiptError,
    PurchasesErrorCode.MissingReceiptFileError ->
        PurchaseState.FailureReason.InvalidReceipt

    PurchasesErrorCode.InvalidCredentialsError ->
        PurchaseState.FailureReason.InvalidCredentials

    PurchasesErrorCode.ConfigurationError,
    PurchasesErrorCode.PurchaseInvalidError ->
        PurchaseState.FailureReason.Configuration

    else ->
        PurchaseState.FailureReason.Unknown
}

/**
 * Converts a caught RevenueCat error into the terminal [PurchaseState] for [operation].
 *
 * Cancellation and pending payment are classified here rather than at each call site so
 * that purchase and restore behave identically, and so the whole decision table is
 * testable with plain values — no Android context and no RevenueCat error objects needed.
 */
internal fun PurchasesErrorCode.toPurchaseState(
    userCancelled: Boolean,
    operation: PurchaseState.Operation,
): PurchaseState = when {
    userCancelled || this == PurchasesErrorCode.PurchaseCancelledError ->
        PurchaseState.Cancelled(operation)

    this == PurchasesErrorCode.PaymentPendingError ->
        PurchaseState.PaymentPending(operation)

    else ->
        PurchaseState.Failed(operation, toFailureReason())
}
