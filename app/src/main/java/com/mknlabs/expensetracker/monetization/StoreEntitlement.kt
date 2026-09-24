package com.mknlabs.expensetracker.monetization

/**
 * The store's own view of the currently active `premium` entitlement.
 *
 * Exists because the app cannot derive a subscription's dates locally: only the store knows
 * when the next charge is, and RevenueCat reports it on the entitlement itself rather than
 * through Firestore. A `null` [StoreEntitlement] means there is no active store entitlement
 * at all — which is a different thing from an entitlement that simply has no end date
 * ([expirationDateMillis] `null`, i.e. lifetime access).
 *
 * Plain data with no RevenueCat type in it, so a ViewModel can be tested against a
 * hand-written value instead of the SDK.
 */
data class StoreEntitlement(
    /**
     * Epoch millis when access ends, or `null` for lifetime access.
     *
     * For a renewing subscription this is the next renewal; for a cancelled-but-still-active
     * one it is the end of access, which is why [willRenew] must be read alongside it before
     * the date is described as a renewal.
     */
    val expirationDateMillis: Long?,

    /** Whether the store will charge again at [expirationDateMillis]. False once cancelled. */
    val willRenew: Boolean,

    /**
     * Whether the store has reported a problem collecting payment
     * (`EntitlementInfo.billingIssueDetectedAt`).
     *
     * Access is usually still active while this is true, so the card must say so instead of
     * promising a renewal that is not going to happen.
     */
    val hasBillingIssue: Boolean,
)
