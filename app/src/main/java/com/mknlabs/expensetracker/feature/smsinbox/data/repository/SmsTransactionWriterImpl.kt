package com.mknlabs.expensetracker.feature.smsinbox.data.repository

import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsTransactionWriter
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.sms.ParsedSms
import com.mknlabs.expensetracker.sms.SmsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Adapts the inbox's port onto the app's existing transaction path.
 *
 * Deliberately a thin delegate: [SmsRepository.saveFromSms] is the same call the
 * notification's one-tap save makes, so a detection filed from the inbox and one filed
 * from the shade produce identical transactions — and keep flowing through the normal
 * repository write (content hash, sync state, large-transaction alert) unchanged.
 */
@Singleton
class SmsTransactionWriterImpl @Inject constructor(
    private val smsRepository: SmsRepository,
    private val transactionDao: TransactionDao,
    private val appPreferencesRepository: AppPreferencesRepository
) : SmsTransactionWriter {

    override suspend fun transactionExistsFor(amountMinor: Long, occurredAt: Long): Boolean =
        transactionDao.existsByAmountAndTimestamp(amountMinor, occurredAt)

    override suspend fun saveDetectionAsTransaction(
        parsed: ParsedSms,
        note: String,
        categoryId: Int
    ): Transaction {
        // The configured default, falling back to the app-wide constant when the user
        // has never chosen one.
        val paymentTypeId = appPreferencesRepository
            .observeAppSettings()
            .first()
            .defaultPaymentTypeId
            .takeIf { it != 0 }
            ?: DEFAULT_PAYMENT_TYPE_ID

        return smsRepository.saveFromSms(
            parsed = parsed,
            note = note,
            categoryId = categoryId,
            paymentTypeId = paymentTypeId
        )
    }
}
