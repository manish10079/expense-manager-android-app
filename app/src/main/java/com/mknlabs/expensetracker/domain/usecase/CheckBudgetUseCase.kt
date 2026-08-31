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

        // Spec §2: warn at 75% of the limit, warn again at 90%, then reached at
        // 100% and exceeded beyond it. No alert below 75%.
        if (usageRatio < 0.75) return

        val categoryName = categoryDao.getById(transaction.categoryId)?.name
            ?: context.getString(R.string.label_unknown)

        // Budget amounts are stored in the user's own currency, so format with
        // the app's display currency settings (unlike SMS import, which is INR).
        val amountFormat = settings.toAmountFormatPreferences()
        val currencyId = settings.currencyId

        val messageAndTier = when {
            usageRatio > 1.0 -> {
                val overMinor = currentSpending - limitMinor
                val overText = formatCurrencyValue(overMinor.toMajorUnits(), currencyId, amountFormat)
                DynamicNotificationEngine.generateBudgetOverspentMessage(context, categoryName, overText) to
                    NotificationHelper.BudgetAlertTier.EXCEEDED
            }

            usageRatio == 1.0 -> {
                val limitText = formatCurrencyValue(limitMinor.toMajorUnits(), currencyId, amountFormat)
                DynamicNotificationEngine.generateBudgetReachedMessage(context, categoryName, limitText) to
                    NotificationHelper.BudgetAlertTier.REACHED
            }

            else -> {
                // 75%..100% used — show the live percentage and what's left. The
                // tier (75% vs 90%) decides whether this is a fresh heads-up or
                // a quiet in-place refresh of the previous warning.
                val percentUsed = (usageRatio * 100).toInt().coerceAtMost(99)
                val remainingMinor = limitMinor - currentSpending
                val remainingText = formatCurrencyValue(remainingMinor.toMajorUnits(), currencyId, amountFormat)
                val message = DynamicNotificationEngine.generateBudgetApproachingMessage(
                    context,
                    categoryName,
                    percentUsed,
                    remainingText
                )
                val tier = if (usageRatio >= 0.9) {
                    NotificationHelper.BudgetAlertTier.WARNING_90
                } else {
                    NotificationHelper.BudgetAlertTier.WARNING_75
                }
                message to tier
            }
        }

        NotificationHelper.showBudgetAlert(
            context,
            messageAndTier.first,
            transaction.categoryId,
            messageAndTier.second
        )
    }

    private fun getStartOfMonthTimestamp(timestamp: Long, monthStartDay: Int = 1): Long =
        com.mknlabs.expensetracker.utils.CustomMonthUtils.getStartOfCustomMonth(timestamp, monthStartDay)
}
