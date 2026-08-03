package com.mknlabs.expensetracker.sms

import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.Transaction
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persistence glue for Smart SMS imports (plan §13 Phase 2).
 *
 * Two responsibilities, both routed through existing code:
 *  - Duplicate detection: [isDuplicate] delegates to the [TransactionDao]
 *    `existsByAmountAndTimestamp` query (plan D7).
 *  - Saving: [saveFromSms] builds a [Transaction] from a [ParsedSms] and goes
 *    through the single save path [TransactionRepository.upsertTransaction]
 *    (plan §3), so every Room flow (Home/List/Analytics) refreshes automatically.
 *
 * No new DB table and no SMS content is ever persisted (plan D1/D2).
 */
@Singleton
class SmsRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val transactionRepository: TransactionRepository
) {

    /**
     * True when a transaction with the same amount and SMS timestamp already
     * exists. Called before showing the notification so the same SMS (carrier
     * double-send, double-tap, process death between parse and save) is never
     * imported twice.
     */
    suspend fun isDuplicate(parsed: ParsedSms): Boolean =
        transactionDao.existsByAmountAndTimestamp(parsed.amountMinor, parsed.smsTimestamp)

    /**
     * Saves an SMS-parsed transaction without opening the app (one-tap Save).
     *
     * [note] defaults to empty (plan §14 Q2). [categoryId] defaults to the
     * detected suggestion — override it for the "Change" flow.
     * [paymentTypeId] defaults to the app-wide [DEFAULT_PAYMENT_TYPE_ID]; the
     * notification receiver should pass the user's configured
     * `AppSettings.defaultPaymentTypeId` when available (plan §14 Q1).
     *
     * Returns the persisted [Transaction] (id and contentHash assigned by the
     * repository).
     */
    suspend fun saveFromSms(
        parsed: ParsedSms,
        note: String = "",
        categoryId: Int = parsed.categoryId,
        paymentTypeId: Int = DEFAULT_PAYMENT_TYPE_ID
    ): Transaction {
        return transactionRepository.upsertTransaction(
            Transaction(
                id = "",
                note = note,
                createdAt = parsed.smsTimestamp,
                amountMinor = parsed.amountMinor,
                transactionTypeId = parsed.transactionTypeId,
                paymentTypeId = paymentTypeId,
                categoryId = categoryId
            )
        )
    }
}
