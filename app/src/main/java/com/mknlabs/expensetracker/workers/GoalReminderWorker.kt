package com.mknlabs.expensetracker.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.domain.repository.GoalRepository
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.utils.daysUntilTimestamp
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Weekly savings-goal nudge (plan §Goals): surfaces the most actionable active goal —
 * one whose deadline is within the next 7 days if any, otherwise the goal closest to
 * completion — and posts a notification with its progress. Gated by the master
 * notifications toggle so it follows the user's reminder preferences.
 */
@HiltWorker
class GoalReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val goalRepository: GoalRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val appSettings = appPreferencesRepository.observeAppSettings().first()
            // Dedicated "Savings Goal Reminders" toggle — independent from the
            // daily-reminder master toggle (plan §Goals).
            if (!appSettings.goalRemindersEnabled) {
                return@withContext Result.success()
            }

            val activeGoals = goalRepository.observeAllGoals().first().filter { !it.isCompleted }
            if (activeGoals.isEmpty()) {
                return@withContext Result.success()
            }

            val now = System.currentTimeMillis()
            val weekMillis = TimeUnit.DAYS.toMillis(7)

        // Priority 1: a goal with a deadline within the next 7 days (closest first).
        val dueSoon = activeGoals
            .filter { it.deadlineAt != null && it.deadlineAt > now && it.deadlineAt - now <= weekMillis }
            .minByOrNull { it.deadlineAt ?: 0L }

        // Priority 2: the goal nearest completion (most motivating nudge).
        val target = dueSoon ?: activeGoals.maxByOrNull {
            if (it.targetAmountMinor > 0) {
                it.currentAmountMinor.toFloat() / it.targetAmountMinor
            } else {
                0f
            }
        } ?: return@withContext Result.success()

        val progressPct = if (target.targetAmountMinor > 0) {
            ((target.currentAmountMinor.toFloat() / target.targetAmountMinor) * 100).toInt().coerceIn(0, 99)
        } else {
            0
        }

        val message = if (dueSoon != null) {
            val daysLeft = daysUntilTimestamp(dueSoon.deadlineAt!!, now)
            when {
                daysLeft <= 0 -> applicationContext.getString(
                    R.string.msg_goal_reminder_due_today,
                    target.name,
                    progressPct
                )

                daysLeft == 1L -> applicationContext.getString(
                    R.string.msg_goal_reminder_due_soon_1,
                    target.name,
                    progressPct
                )

                else -> applicationContext.getString(
                    R.string.msg_goal_reminder_due_soon,
                    daysLeft,
                    target.name,
                    progressPct
                )
            }
        } else {
            applicationContext.getString(
                R.string.msg_goal_reminder_progress,
                progressPct,
                target.name
            )
        }

            NotificationHelper.showGoalReminderNotification(applicationContext, message)
            Result.success()
        } catch (e: Exception) {
            Log.e("GoalReminderWorker", "Error posting goal reminder", e)
            Result.retry()
        }
    }
}
