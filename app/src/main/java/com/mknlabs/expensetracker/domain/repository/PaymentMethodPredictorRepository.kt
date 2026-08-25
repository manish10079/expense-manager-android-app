package com.mknlabs.expensetracker.domain.repository

/**
 * Abstraction for automatic payment method prediction from merchant text input.
 *
 * Implementations:
 * - [com.mknlabs.expensetracker.ai.PaymentMethodPredictor] (offline, DataStore-backed, Hilt-injectable)
 *
 * Works by learning from user transaction history: when a user saves a
 * transaction with a merchant and a payment method, the association is
 * remembered and auto-filled on the next transaction at the same merchant.
 */
interface PaymentMethodPredictorRepository {

    /**
     * Predicts the best payment method ID for [merchantText].
     *
     * @param merchantText The merchant name or description to analyze.
     * @return Predicted payment method ID, or null if no prediction available.
     */
    suspend fun predict(merchantText: String): Int?

    /**
     * Records that [merchantText] was paid with [paymentMethodId].
     * Called after a transaction is saved to learn from user behavior.
     *
     * @param merchantText The merchant name from the transaction.
     * @param paymentMethodId The payment method the user selected.
     */
    suspend fun learn(merchantText: String, paymentMethodId: Int)

    /**
     * Removes a previously learned association for [merchantText].
     *
     * @param merchantText The merchant name to forget.
     */
    suspend fun forget(merchantText: String)
}
