package com.mknlabs.expensetracker.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.domain.repository.RecurringRuleRepository
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toAmountFormatPreferences
import com.mknlabs.expensetracker.utils.toMajorUnits
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.min

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val recurringRuleRepository: RecurringRuleRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("RecurringWorker", "doWork: Started processing recurring transactions")
        try {
            val activeRules = recurringRuleRepository.getActiveRules().filter { it.isEnabled }
            Log.d("RecurringWorker", "doWork: Found ${activeRules.size} active rules")
            
            val now = System.currentTimeMillis()
            var transactionsAddedCount = 0
            val addedTransactionNotes = mutableListOf<String>()

            val appSettings = appPreferencesRepository.observeAppSettings().first()
            val billAlertsEnabled = appSettings.billRemindersEnabled

            activeRules.forEach { rule ->
                Log.d("RecurringWorker", "doWork: Processing rule ${rule.id} for transaction ${rule.transactionId}")
                
                // 1. Advance alerts at 7 / 3 / 1 days before and on the due date
                //    (notification spec category 7) — gated by the global
                //    bill-reminders toggle AND the per-rule notificationsEnabled
                //    mute (spec: both must be true for generation).
                if (billAlertsEnabled && rule.notificationsEnabled) {
                    maybeFireAdvanceAlert(rule, now, appSettings)
                }

                // 1.5 Re-create occurrences that were scheduled but never
                //     materialized (missed backfills from interrupted runs / restores)
                backfillMissedOccurrences(rule, now) { note ->
                    transactionsAddedCount++
                    addedTransactionNotes += note
                    Log.d("RecurringWorker", "doWork: Backfilled a missed occurrence for rule ${rule.id} (${note}). Total so far: $transactionsAddedCount")
                }

                // 2. Process due transactions
                processRule(rule, now) { note ->
                    transactionsAddedCount++
                    addedTransactionNotes += note
                    Log.d("RecurringWorker", "doWork: Added a transaction for rule ${rule.id} (${note}). Total so far: $transactionsAddedCount")
                }
            }

            if (transactionsAddedCount > 0) {
                Log.d("RecurringWorker", "doWork: Successfully added $transactionsAddedCount total transactions")
                // Name the added transaction(s) so the user knows what was added.
                val notes = addedTransactionNotes.filter { it.isNotBlank() }.distinct()
                val notesLabel = notes.joinToString(separator = "\n") { note ->
                    if (notes.size > 1) "\u2022 $note" else note
                }
                NotificationHelper.showGenericNotification(
                    context = appContext,
                    title = appContext.getString(com.mknlabs.expensetracker.R.string.notification_title_recurring_updated),
                    message = appContext.resources.getQuantityString(
                        com.mknlabs.expensetracker.R.plurals.notification_format_recurring_updated,
                        transactionsAddedCount,
                        transactionsAddedCount,
                        notesLabel
                    ),
                    notificationId = NotificationHelper.NOTIFICATION_ID_RECURRING_UPDATED
                )
            } else {
                Log.d("RecurringWorker", "doWork: No transactions were due for addition")
            }

            // Keep the periodic heartbeat armed (KEEP makes this a no-op if it
            // is already scheduled — e.g. by Application.onCreate).
            schedulePeriodic(appContext)

            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) {
            // The worker was stopped (e.g. an immediate run replaced it, or the
            // app/OS cancelled it). Never swallow cancellation: returning retry()
            // here makes WorkManager retry a cancelled worker forever. The
            // periodic heartbeat re-arms processing.
            throw e
        } catch (e: Exception) {
            Log.e("RecurringWorker", "Error processing recurring rules", e)
            Result.retry()
        }
    }

    /**
     * Fires the upcoming-bill alert for the first not-yet-notified advance
     * window (7 / 3 / 1 days before, then the due date).
     * [RecurringTransactionRule.lastNotifiedWindowDays] remembers which window
     * already fired for the current occurrence, so each window heads-up exactly
     * once per billing cycle; [processRule] resets it when the occurrence
     * advances. Once the due moment passes, only the due-date alert may fire.
     */
    private suspend fun maybeFireAdvanceAlert(
        rule: RecurringTransactionRule,
        now: Long,
        appSettings: AppSettings
    ) {
        val remainingMillis = rule.nextRunAt - now
        val lastNotified = rule.lastNotifiedWindowDays ?: Int.MAX_VALUE

        val windowDays = when {
            // At/over the due moment: only the due-date alert, exactly once.
            remainingMillis <= 0L -> if (lastNotified <= 0) null else 0

            else -> ADVANCE_WINDOW_DAYS.firstOrNull { days ->
                days < lastNotified && remainingMillis <= TimeUnit.DAYS.toMillis(days.toLong())
            }
        } ?: return

        val originalTransaction = transactionRepository.getTransactionById(rule.transactionId) ?: return
        val amountFormat = appSettings.toAmountFormatPreferences()
        val currencyId = appSettings.currencyId
        val amountStr = formatCurrencyValue(
            originalTransaction.amountMinor.toMajorUnits(),
            currencyId,
            amountFormat
        )

        // Message always shows the accurate countdown, even when a skipped run
        // falls back to a larger trigger window.
        val daysLeft = ceil(remainingMillis.toDouble() / TimeUnit.DAYS.toMillis(1)).toLong()
        val message = when {
            daysLeft <= 0 -> appContext.getString(
                R.string.notification_format_upcoming_bill_due_today,
                amountStr,
                originalTransaction.note
            )

            daysLeft == 1L -> appContext.getString(
                R.string.notification_format_upcoming_bill_tomorrow,
                amountStr,
                originalTransaction.note
            )

            else -> appContext.getString(
                R.string.notification_format_upcoming_bill_days,
                amountStr,
                originalTransaction.note,
                daysLeft
            )
        }

        NotificationHelper.showUpcomingBillNotification(appContext, message, rule.id, windowDays)
        recurringRuleRepository.upsertRule(rule.copy(lastNotifiedWindowDays = windowDays))
    }

    private suspend fun processRule(
        rule: RecurringTransactionRule,
        referenceTime: Long,
        onTransactionAdded: (note: String) -> Unit
    ) {
        var currentRule = rule
        val originalTransaction = transactionRepository.getTransactionById(rule.transactionId) ?: return

        // We process as long as nextRunAt is in the past and we have remaining installments
        while (currentRule.nextRunAt <= referenceTime && (currentRule.remainingCount ?: 1) > 0) {
            
            // 1. Create the new transaction
            val newTransactionDate = currentRule.nextRunAt
            // Deterministic ID to avoid duplication across devices/runs
            val newTransactionId = "${currentRule.id}_${newTransactionDate}"

            // The id is deterministic, so an existing row means a previous run
            // already inserted it (crash after insert, or a concurrent run). Skip
            // the insert — but still advance the rule below so we never stall —
            // and only count/notify genuinely new transactions.
            if (transactionRepository.getTransactionById(newTransactionId) == null) {
                val newTransaction = originalTransaction.copy(
                    id = newTransactionId,
                    createdAt = newTransactionDate,
                    updatedAt = System.currentTimeMillis(),
                    sourceRecurringRuleId = currentRule.id
                )
                
                transactionRepository.upsertTransaction(newTransaction)
                onTransactionAdded(originalTransaction.note)
            }

            // 2. Update the rule for the next occurrence
            val nextInfo = calculateNextRun(currentRule.nextRunAt, currentRule.frequency, originalTransaction.createdAt)
            
            currentRule = currentRule.copy(
                lastRunAt = currentRule.nextRunAt,
                nextRunAt = nextInfo,
                remainingCount = currentRule.remainingCount?.minus(1),
                // The advance-window marker belongs to the old occurrence — the
                // new occurrence starts un-notified.
                lastNotifiedWindowDays = null,
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

    /**
     * Re-creates an occurrence that was scheduled but never materialized as a
     * transaction. When a previous run was interrupted (or rules were restored
     * from a stale backup/sync), the rule can advance past a due date without the
     * corresponding transaction surviving — e.g. nextRunAt = Sep 13 with no
     * Aug 13 transaction. Walks back from nextRunAt and materializes any
     * past-due slot whose deterministic id is missing, stopping at the first
     * slot that already exists (everything before it was processed normally), at
     * the series anchor (the first occurrence), or after [MAX_BACKFILL_SLOTS].
     *
     * The rule itself is untouched: the missed slot was already counted when
     * nextRunAt was advanced, so re-creating the transaction only fills a data
     * gap. Idempotent — once a slot's transaction exists, later runs stop
     * immediately. Soft-deleted transactions count as existing, so a deletion by
     * the user is never resurrected.
     */
    private suspend fun backfillMissedOccurrences(
        rule: RecurringTransactionRule,
        now: Long,
        onTransactionAdded: (note: String) -> Unit
    ) {
        val originalTransaction = transactionRepository.getTransactionById(rule.transactionId) ?: return
        val anchor = originalTransaction.createdAt
        var candidateDate = rule.nextRunAt

        repeat(MAX_BACKFILL_SLOTS) {
            candidateDate = onePeriodBefore(candidateDate, rule.frequency, anchor)

            // Not due yet, or before the series' first occurrence — nothing to backfill.
            if (candidateDate > now || candidateDate <= anchor) return

            val candidateId = "${rule.id}_${candidateDate}"
            // Slot already exists — earlier slots were processed normally. Stop.
            if (transactionRepository.getTransactionById(candidateId) != null) return

            transactionRepository.upsertTransaction(
                originalTransaction.copy(
                    id = candidateId,
                    createdAt = candidateDate,
                    updatedAt = System.currentTimeMillis(),
                    sourceRecurringRuleId = rule.id
                )
            )
            onTransactionAdded(originalTransaction.note)
            Log.d("RecurringWorker", "backfill: re-created missing occurrence $candidateDate for rule ${rule.id}")
        }
    }

    /**
     * Inverse of [calculateNextRun]: the occurrence date one period before
     * [currentRunAt], anchored to the same preferred day as [baseAnchor].
     */
    private fun onePeriodBefore(
        currentRunAt: Long,
        frequency: RecurringFrequency,
        baseAnchor: Long
    ): Long {
        val baseCalendar = Calendar.getInstance().apply { timeInMillis = baseAnchor }
        val prevCalendar = Calendar.getInstance().apply { timeInMillis = currentRunAt }

        when (frequency) {
            RecurringFrequency.Daily -> prevCalendar.add(Calendar.DAY_OF_YEAR, -1)
            RecurringFrequency.Weekly -> prevCalendar.add(Calendar.WEEK_OF_YEAR, -1)
            RecurringFrequency.Monthly -> {
                val preferredDay = baseCalendar.get(Calendar.DAY_OF_MONTH).coerceIn(1, 28)
                prevCalendar.add(Calendar.MONTH, -1)
                val maxDay = prevCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                prevCalendar.set(Calendar.DAY_OF_MONTH, min(preferredDay, maxDay))
            }
            RecurringFrequency.Yearly -> {
                prevCalendar.add(Calendar.YEAR, -1)
                val preferredDay = baseCalendar.get(Calendar.DAY_OF_MONTH).coerceIn(1, 28)
                val maxDay = prevCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                prevCalendar.set(Calendar.DAY_OF_MONTH, min(preferredDay, maxDay))
            }
        }
        return prevCalendar.timeInMillis
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
        /** Upper bound on how many missed slots a single backfill pass may re-create. */
        private const val MAX_BACKFILL_SLOTS = 3

        /** Advance-alert windows (days before the due date), furthest-out first. */
        private val ADVANCE_WINDOW_DAYS = listOf(7, 3, 1, 0)

        /**
         * Durable heartbeat for recurring-rule processing.
         *
         * The previous implementation chained one-time work by re-enqueuing itself
         * every hour. That chain silently died whenever a run was cancelled (every
         * app launch and every transaction save REPLACE-cancels an in-flight run),
         * force-stopped, or crashed — leaving rules unprocessed until the next time
         * the user opened the app, which is why occurrences got skipped.
         * WorkManager keeps periodic work armed across process death, force-stops
         * and device reboots, so rules are always processed. Processing is
         * idempotent (deterministic transaction ids), so more frequent runs are safe.
         */
        fun schedulePeriodic(context: Context) {
            val periodicRequest = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(15, TimeUnit.MINUTES)
                .addTag("RecurringTransactionWork")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "RecurringTransactionPeriodicWork",
                // UPDATE per the notification spec's WorkManager requirement
                // (updates the stored spec in place, never cancels a run).
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        }

        /** Fire-and-forget immediate run, used right after a rule is saved. */
        fun enqueueImmediate(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<RecurringTransactionWorker>()
                .addTag("RecurringTransactionWork")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "RecurringTransactionWork",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
