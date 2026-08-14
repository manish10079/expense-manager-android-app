package com.mknlabs.expensetracker.domain.usecase

import android.content.Context
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toAmountFormatPreferences
import com.mknlabs.expensetracker.utils.toMajorUnits
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Fires a heads-up when a single expense crosses the user's configured
 * large-transaction threshold (notification spec category 3, Free tier).
 * Local by design — it runs at save-time, so it works offline and for
 * Free-tier users who never sync (see plan §5.2 allocation).
 */
class CheckLargeTransactionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: AppPreferencesRepository,
    private val categoryDao: CategoryDao
) {
    suspend operator fun invoke(transaction: Transaction) {
        if (transaction.transactionTypeId != 2) return // Only for expenses

        val settings = preferencesRepository.observeAppSettings().first()
        if (!settings.largeTransactionAlertsEnabled) return

        val thresholdMinor = settings.largeTransactionThresholdMinor
        if (thresholdMinor <= 0) return
        if (transaction.amountMinor < thresholdMinor) return

        val categoryName = categoryDao.getById(transaction.categoryId)?.name
            ?: context.getString(R.string.label_unknown)

        // Amounts are stored in the user's own currency, so format with the
        // app's display currency settings.
        val amountFormat = settings.toAmountFormatPreferences()
        val currencyId = settings.currencyId
        val amountText = formatCurrencyValue(transaction.amountMinor.toMajorUnits(), currencyId, amountFormat)
        val thresholdText = formatCurrencyValue(thresholdMinor.toMajorUnits(), currencyId, amountFormat)

        NotificationHelper.showLargeTransactionNotification(context, categoryName, amountText, thresholdText)
    }
}
