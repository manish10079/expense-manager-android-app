package com.mknlabs.expensetracker.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mknlabs.expensetracker.domain.repository.RecurringRuleRepository
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val recurringRuleRepository: RecurringRuleRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("RecurringWorker", "doWork: Started processing recurring transactions")
        try {
            val activeRules = recurringRuleRepository.getActiveRules().filter { it.isEnabled }
            Log.d("RecurringWorker", "doWork: Found ${activeRules.size} active rules")
            
            val now = System.currentTimeMillis()
            var transactionsAddedCount = 0
            val advanceNotificationWindow = now + TimeUnit.HOURS.toMillis(48)

            activeRules.forEach { rule ->
                Log.d("RecurringWorker", "doWork: Processing rule ${rule.id} for transaction ${rule.transactionId}")
                
                // 1. Check for 48-hour alerts
                if (rule.nextRunAt in (now + 1)..advanceNotificationWindow && 
                    rule.lastNotifiedOccurrenceAt != rule.nextRunAt) {
                    
                    val originalTransaction = transactionRepository.getTransactionById(rule.transactionId)
                    if (originalTransaction != null) {
                        val amountStr = String.format("%.2f", originalTransaction.amountMinor / 100.0)
                        NotificationHelper.showGenericNotification(
                            context = appContext,
                            title = appContext.getString(com.mknlabs.expensetracker.R.string.notification_title_upcoming_bill),
                            message = appContext.getString(
                                com.mknlabs.expensetracker.R.string.notification_format_upcoming_bill,
                                amountStr,
                                originalTransaction.note
                            )
                        )
                        // Mark as notified for this specific occurrence
                        recurringRuleRepository.upsertRule(rule.copy(lastNotifiedOccurrenceAt = rule.nextRunAt))
                    }
                }

                // 2. Process due transactions
                processRule(rule, now) {
                    transactionsAddedCount++
                    Log.d("RecurringWorker", "doWork: Added a transaction for rule ${rule.id}. Total so far: $transactionsAddedCount")
                }
            }

            if (transactionsAddedCount > 0) {
                Log.d("RecurringWorker", "doWork: Successfully added $transactionsAddedCount total transactions")
                NotificationHelper.showGenericNotification(
                    context = appContext,
                    title = "Recurring Transactions Updated",
                    message = "Successfully added $transactionsAddedCount recurring transaction${if (transactionsAddedCount > 1) "s" else ""}."
                )
            } else {
                Log.d("RecurringWorker", "doWork: No transactions were due for addition")
            }

            // Schedule next check
            scheduleNext(appContext)

            Result.success()
        } catch (e: Exception) {
            Log.e("RecurringWorker", "Error processing recurring rules", e)
            Result.retry()
        }
    }

    private suspend fun processRule(
        rule: RecurringTransactionRule,
        referenceTime: Long,
        onTransactionAdded: () -> Unit
    ) {
        var currentRule = rule
        val originalTransaction = transactionRepository.getTransactionById(rule.transactionId) ?: return

        // We process as long as nextRunAt is in the past and we have remaining installments
        while (currentRule.nextRunAt <= referenceTime && (currentRule.remainingCount ?: 1) > 0) {
            
            // 1. Create the new transaction
            val newTransactionDate = currentRule.nextRunAt
            val newTransaction = originalTransaction.copy(
                id = "", // Will be generated by repo
                createdAt = newTransactionDate,
                updatedAt = System.currentTimeMillis(),
                sourceRecurringRuleId = currentRule.id
            )
            
            val savedTransaction = transactionRepository.upsertTransaction(newTransaction)
            onTransactionAdded()

            // 2. Update the rule for the next occurrence
            val nextInfo = calculateNextRun(currentRule.nextRunAt, currentRule.frequency, originalTransaction.createdAt)
            
            currentRule = currentRule.copy(
                lastRunAt = currentRule.nextRunAt,
                nextRunAt = nextInfo,
                remainingCount = currentRule.remainingCount?.minus(1),
                updatedAt = System.currentTimeMillis()
            )
            
            recurringRuleRepository.upsertRule(currentRule)

            // If no more installments are left, disable the rule and stop
            if (currentRule.remainingCount != null && currentRule.remainingCount <= 0) {
                recurringRuleRepository.setEnabled(currentRule.id, false)
                break
            }
        }
    }

    private fun calculateNextRun(
        currentRunAt: Long,
        frequency: RecurringFrequency,
        baseAnchor: Long
    ): Long {
        val baseCalendar = Calendar.getInstance().apply { timeInMillis = baseAnchor }
        val nextCalendar = Calendar.getInstance().apply { timeInMillis = currentRunAt }

        when (frequency) {
            RecurringFrequency.Daily -> nextCalendar.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.Weekly -> nextCalendar.add(Calendar.WEEK_OF_YEAR, 1)
            RecurringFrequency.Monthly -> {
                val preferredDay = baseCalendar.get(Calendar.DAY_OF_MONTH).coerceIn(1, 28)
                nextCalendar.add(Calendar.MONTH, 1)
                val maxDay = nextCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                nextCalendar.set(Calendar.DAY_OF_MONTH, min(preferredDay, maxDay))
            }
            RecurringFrequency.Yearly -> {
                nextCalendar.add(Calendar.YEAR, 1)
                val preferredDay = baseCalendar.get(Calendar.DAY_OF_MONTH).coerceIn(1, 28)
                val maxDay = nextCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                nextCalendar.set(Calendar.DAY_OF_MONTH, min(preferredDay, maxDay))
            }
        }
        return nextCalendar.timeInMillis
    }

    companion object {
        fun scheduleNext(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<RecurringTransactionWorker>()
                .setInitialDelay(1, TimeUnit.HOURS) // Check every hour
                .addTag("RecurringTransactionWork")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "RecurringTransactionWork",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun enqueueImmediate(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<RecurringTransactionWorker>()
                .addTag("RecurringTransactionWork")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "RecurringTransactionWork",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
