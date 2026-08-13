package com.mknlabs.expensetracker.domain.usecase

import android.content.Context
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.data.local.room.dao.BudgetDao
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.notifications.DynamicNotificationEngine
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toAmountFormatPreferences
import com.mknlabs.expensetracker.utils.toMajorUnits
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CheckBudgetUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: AppPreferencesRepository,
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    suspend operator fun invoke(transaction: Transaction) {
        if (transaction.transactionTypeId != 2) return // Only for expenses

        val settings = preferencesRepository.observeAppSettings().first()
        if (!settings.budgetLimitAlertsEnabled) return

        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthStr = sdf.format(Date(transaction.createdAt))

        val budgetEntity = budgetDao.getActiveByCategoryAndMonthStart(
            categoryId = transaction.categoryId,
            monthStart = getStartOfMonthTimestamp(transaction.createdAt)
        ) ?: return

        val limitMinor = budgetEntity.limitMinor
        if (limitMinor <= 0) return

        val currentSpending = transactionDao.getMonthlyCategorySpending(transaction.categoryId, monthStr)
        if (currentSpending <= 0) return

        val usageRatio = currentSpending.toDouble() / limitMinor

        // No alert below 80% of the limit.
        if (usageRatio < 0.8) return

        val categoryName = categoryDao.getById(transaction.categoryId)?.name
            ?: context.getString(R.string.label_unknown)

        // Budget amounts are stored in the user's own currency, so format with
        // the app's display currency settings (unlike SMS import, which is INR).
        val amountFormat = settings.toAmountFormatPreferences()
        val currencyId = settings.currencyId

        val message = when {
            usageRatio > 1.0 -> {
                val overMinor = currentSpending - limitMinor
                val overText = formatCurrencyValue(overMinor.toMajorUnits(), currencyId, amountFormat)
                DynamicNotificationEngine.generateBudgetOverspentMessage(context, categoryName, overText)
            }

            usageRatio == 1.0 -> {
                val limitText = formatCurrencyValue(limitMinor.toMajorUnits(), currencyId, amountFormat)
                DynamicNotificationEngine.generateBudgetReachedMessage(context, categoryName, limitText)
            }

            else -> {
                // 80%..100% used — show the live percentage and what's left.
                val percentUsed = (usageRatio * 100).toInt().coerceAtMost(99)
                val remainingMinor = limitMinor - currentSpending
                val remainingText = formatCurrencyValue(remainingMinor.toMajorUnits(), currencyId, amountFormat)
                DynamicNotificationEngine.generateBudgetApproachingMessage(
                    context,
                    categoryName,
                    percentUsed,
                    remainingText
                )
            }
        }

        NotificationHelper.showBudgetAlert(context, message, transaction.categoryId)
    }

    private fun getStartOfMonthTimestamp(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
