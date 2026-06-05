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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
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

        val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        val monthStr = sdf.format(java.util.Date(transaction.createdAt))
        
        val budgetEntity = budgetDao.getActiveByCategoryAndMonthStart(
            categoryId = transaction.categoryId,
            monthStart = getStartOfMonthTimestamp(transaction.createdAt)
        )

        if (budgetEntity != null) {
            val currentSpending = transactionDao.getMonthlyCategorySpending(transaction.categoryId, monthStr)
            if (currentSpending > budgetEntity.limitMinor) {
                val categoryName = categoryDao.getById(transaction.categoryId)?.name 
                    ?: context.getString(R.string.label_unknown)
                val message = DynamicNotificationEngine.generateBudgetExceededMessage(context, categoryName)
                NotificationHelper.showBudgetNotification(context, message)
            }
        }
    }

    private fun getStartOfMonthTimestamp(timestamp: Long): Long {
        return java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
