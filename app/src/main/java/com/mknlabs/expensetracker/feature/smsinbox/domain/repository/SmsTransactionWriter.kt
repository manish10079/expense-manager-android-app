package com.mknlabs.expensetracker.feature.smsinbox.domain.repository

import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.sms.ParsedSms

/**
 * The only door between the inbox and the ledger.
 *
 * The inbox must be able to turn a detection into a transaction without knowing how
 * transactions are stored, synced, deduplicated or priced by default payment method.
 * Owning this narrow port (rather than injecting `SmsRepository` and `TransactionDao`
 * directly) is what makes the decoupling requirement structural: the feature cannot
 * reach the transaction tables even by accident, and its use cases can be tested
 * without Room, Hilt or a running app.
 *
 * The implementation lives in the data layer and simply delegates to the existing
 * Smart SMS save path, so both ways of filing a detection — from the inbox and from
 * the notification — build a transaction identically.
 */
interface SmsTransactionWriter {

    /**
     * True when a transaction for this amount and occurrence time is already stored.
     * Checked immediately before writing, which is the only moment a double count can
     * actually be introduced.
     */
    suspend fun transactionExistsFor(amountMinor: Long, occurredAt: Long): Boolean

    /**
     * Persists [parsed] as a transaction, applying the user's default payment method.
     * Returns the stored transaction (id assigned by the repository).
     */
    suspend fun saveDetectionAsTransaction(
        parsed: ParsedSms,
        note: String,
        categoryId: Int
    ): Transaction
}
