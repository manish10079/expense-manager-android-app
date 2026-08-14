package com.mknlabs.expensetracker.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.domain.repository.GoalRepository
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.utils.daysUntilTimestamp
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Daily savings-goal monitor (notification spec category 6):
 * - **Milestones:** posts when a goal crosses 25% / 50% / 75% of its target
 *   (deduped per goal via a tracker — each milestone fires once).
 * - **Achieved:** posts when the saved amount reaches/exceeds the target.
 * - **Behind schedule:** posts once per goal when the contribution velocity
 *   falls below what the deadline requires.
 * - The legacy weekly "nudge" (due-soon priority, else nearest-to-complete)
 *   still fires, but at most once per week.
 *
 * Gated by the dedicated goalRemindersEnabled toggle and the master
 * notifications setting (handled by the caller). The spec's Premium gating for
 * this category is a settings-screen (Phase 5) concern, not enforced here.
 */
@HiltWorker
class GoalReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val goalRepository: GoalRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : CoroutineWorker(appContext, workerParams) {

    private val tracker by lazy {
        applicationContext.getSharedPreferences(TRACKER_PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val appSettings = appPreferencesRepository.observeAppSettings().first()
            // Dedicated "Savings Goal Reminders" toggle — independent from the
            // daily-reminder master toggle (plan §Goals).
            if (!appSettings.goalRemindersEnabled) {
                return@withContext Result.success()
            }

            val activeGoals = goalRepository.observeAllGoals().first().filter { !it.isDeleted }
            if (activeGoals.isEmpty()) {
                return@withContext Result.success()
            }

            val now = System.currentTimeMillis()
            activeGoals.forEach { goal ->
                checkMilestone(goal)
                checkBehindSchedule(goal, now)
            }
            maybeWeeklyNudge(activeGoals, now)

            Result.success()
        } catch (e: Exception) {
            Log.e("GoalReminderWorker", "Error processing goal reminders", e)
            Result.retry()
        }
    }

    /** Fires once per milestone band (25/50/75) and once for achieved (100). */
    private fun checkMilestone(goal: Goal) {
        val target = goal.targetAmountMinor
        if (target <= 0) return

        val achieved = goal.isCompleted || goal.currentAmountMinor >= target
        val progressPct = ((goal.currentAmountMinor.toFloat() / target) * 100).toInt()
        val band = when {
            achieved -> 100
            progressPct >= 75 -> 75
            progressPct >= 50 -> 50
            progressPct >= 25 -> 25
            else -> 0
        }
        if (band <= 0) return

        val key = milestoneKey(goal.id)
        val lastNotified = tracker.getInt(key, 0)
        if (band <= lastNotified) return

        val (titleRes, messageRes) = if (achieved) {
            R.string.title_goal_achieved to R.string.msg_goal_achieved
        } else {
            R.string.title_goal_milestone to R.string.msg_goal_milestone
        }
        val title = applicationContext.getString(titleRes)
        val message = if (achieved) {
            applicationContext.getString(messageRes, goal.name)
        } else {
            applicationContext.getString(messageRes, band, goal.name)
        }

        NotificationHelper.showGoalMilestoneNotification(applicationContext, title, message, goal.id)
        tracker.edit().putInt(key, band).apply()
    }

    /** Fires once when a deadline goal falls behind the required contribution velocity. */
    private fun checkBehindSchedule(goal: Goal, now: Long) {
        val deadline = goal.deadlineAt ?: return
        if (deadline <= now) return // overdue is handled by milestone/nudge
        if (goal.targetAmountMinor <= 0) return
        if (goal.createdAt <= 0) return

        val totalMillis = deadline - goal.createdAt
        val elapsedMillis = now - goal.createdAt
        if (totalMillis <= 0 || elapsedMillis <= 0) return

        val requiredPerDay = goal.targetAmountMinor.toDouble() / (totalMillis / TimeUnit.DAYS.toMillis(1))
        val actualPerDay = goal.currentAmountMinor.toDouble() / (elapsedMillis / TimeUnit.DAYS.toMillis(1))
        val behind = requiredPerDay > 0 && actualPerDay < requiredPerDay

        val key = behindKey(goal.id)
        val wasNotified = tracker.getBoolean(key, false)
        if (behind && !wasNotified) {
            val daysLeft = daysUntilTimestamp(deadline, now)
            val title = applicationContext.getString(R.string.title_goal_behind_schedule)
            val message = applicationContext.getString(R.string.msg_goal_behind_schedule, goal.name, daysLeft)
            NotificationHelper.showGoalMilestoneNotification(applicationContext, title, message, goal.id)
            tracker.edit().putBoolean(key, true).apply()
        } else if (!behind && wasNotified) {
            // Back on track — re-arm so a future slip notifies again.
            tracker.edit().putBoolean(key, false).apply()
        }
    }

    /** Legacy weekly nudge — at most once every 7 days. */
    private fun maybeWeeklyNudge(activeGoals: List<Goal>, now: Long) {
        val lastNudge = tracker.getLong(KEY_LAST_NUDGE, 0L)
        if (now - lastNudge < TimeUnit.DAYS.toMillis(7)) return

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
        } ?: return

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
        tracker.edit().putLong(KEY_LAST_NUDGE, now).apply()
    }

    private fun milestoneKey(goalId: String) = "milestone_$goalId"
    private fun behindKey(goalId: String) = "behind_$goalId"

    private companion object {
        const val TRACKER_PREFS_NAME = "goal_notification_tracker"
        const val KEY_LAST_NUDGE = "last_nudge"
    }
}
