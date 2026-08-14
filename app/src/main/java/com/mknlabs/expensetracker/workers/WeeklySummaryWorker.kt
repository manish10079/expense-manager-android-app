package com.mknlabs.expensetracker.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toAmountFormatPreferences
import com.mknlabs.expensetracker.utils.toMajorUnits
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Weekly spending summary (notification spec category 4, Free tier). Runs on
 * Sundays and compares the last 7 days against the 7 days before that, posting
 * a recap with the delta and the top expense category. Local by design — Free
 * users have nothing on Firestore, so the server cannot compute this (plan
 * §5.2; a cloud path is an optional later phase for Pro users). Gated by the
 * weeklySummaryEnabled setting.
 */
@HiltWorker
class WeeklySummaryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val appPreferencesRepository: AppPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val appSettings = appPreferencesRepository.observeAppSettings().first()
            if (!appSettings.weeklySummaryEnabled) {
                return@withContext Result.success()
            }

            val now = System.currentTimeMillis()
            val weekMillis = TimeUnit.DAYS.toMillis(7)
            val thisWeekEnd = now
            val thisWeekStart = now - weekMillis
            val prevWeekStart = thisWeekStart - weekMillis

            val thisWeek = transactionDao.getRangeSummary(thisWeekStart, thisWeekEnd)
            val prevWeek = transactionDao.getRangeSummary(prevWeekStart, thisWeekStart)

            // Nothing logged recently — nothing to summarize.
            if (thisWeek.expenseMinor <= 0 && thisWeek.incomeMinor <= 0) {
                return@withContext Result.success()
            }

            val amountFormat = appSettings.toAmountFormatPreferences()
            val currencyId = appSettings.currencyId
            val expenseText = formatCurrencyValue(
                thisWeek.expenseMinor.toMajorUnits(),
                currencyId,
                amountFormat
            )

            val deltaPct = if (prevWeek.expenseMinor > 0) {
                (((thisWeek.expenseMinor - prevWeek.expenseMinor).toDouble() / prevWeek.expenseMinor) * 100)
                    .toInt()
            } else {
                0
            }

            val baseMessage = when {
                prevWeek.expenseMinor <= 0 || thisWeek.expenseMinor == prevWeek.expenseMinor -> applicationContext.getString(
                    R.string.notification_format_weekly_summary_flat,
                    expenseText
                )

                thisWeek.expenseMinor > prevWeek.expenseMinor -> applicationContext.getString(
                    R.string.notification_format_weekly_summary_up,
                    expenseText,
                    deltaPct
                )

                else -> applicationContext.getString(
                    R.string.notification_format_weekly_summary_down,
                    expenseText,
                    kotlin.math.abs(deltaPct)
                )
            }

            val topCategory = transactionDao.getTopExpenseCategory(thisWeekStart, thisWeekEnd)
            val topCategoryName = topCategory?.let { categoryDao.getById(it.categoryId)?.name }
                ?.takeIf { it.isNotBlank() }

            val message = if (topCategoryName != null) {
                baseMessage + " " + applicationContext.getString(
                    R.string.notification_format_weekly_summary_top_category,
                    topCategoryName
                )
            } else {
                baseMessage
            }

            NotificationHelper.showWeeklySummaryNotification(applicationContext, message)
            Result.success()
        } catch (e: Exception) {
            Log.e("WeeklySummaryWorker", "Error generating weekly summary", e)
            Result.retry()
        }
    }
}
