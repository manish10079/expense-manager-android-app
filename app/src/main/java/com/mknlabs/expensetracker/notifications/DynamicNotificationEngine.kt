package com.mknlabs.expensetracker.notifications

import android.content.Context
import com.mknlabs.expensetracker.R
import java.util.Calendar

object DynamicNotificationEngine {

    private val sarcasticOpeners = listOf(
        R.string.notification_opener_1, R.string.notification_opener_2,
        R.string.notification_opener_3, R.string.notification_opener_4,
        R.string.notification_opener_5, R.string.notification_opener_6,
        R.string.notification_opener_7, R.string.notification_opener_8
    )

    private val moneyReactions = listOf(
        R.string.notification_reaction_1, R.string.notification_reaction_2,
        R.string.notification_reaction_3, R.string.notification_reaction_4,
        R.string.notification_reaction_5, R.string.notification_reaction_6
    )

    private val guiltLines = listOf(
        R.string.notification_guilt_1, R.string.notification_guilt_2,
        R.string.notification_guilt_3, R.string.notification_guilt_4,
        R.string.notification_guilt_5, R.string.notification_guilt_6
    )

    private val foodLines = listOf(
        R.string.notification_food_1, R.string.notification_food_2,
        R.string.notification_food_3, R.string.notification_food_4,
        R.string.notification_food_5
    )

    private val shoppingLines = listOf(
        R.string.notification_shopping_1, R.string.notification_shopping_2,
        R.string.notification_shopping_3, R.string.notification_shopping_4,
        R.string.notification_shopping_5
    )

    private val genericLines = listOf(
        R.string.notification_generic_1, R.string.notification_generic_2,
        R.string.notification_generic_3, R.string.notification_generic_4,
        R.string.notification_generic_5
    )

    private val budgetExceededLines = listOf(
        R.string.notification_budget_exceeded_1, R.string.notification_budget_exceeded_2,
        R.string.notification_budget_exceeded_3, R.string.notification_budget_exceeded_4,
        R.string.notification_budget_exceeded_5
    )

    private val budgetApproachingLines = listOf(
        R.string.notification_budget_approaching_1, R.string.notification_budget_approaching_2,
        R.string.notification_budget_approaching_3
    )

    private val budgetOverspentLines = listOf(
        R.string.notification_budget_overspent_1, R.string.notification_budget_overspent_2,
        R.string.notification_budget_overspent_3
    )

    private val missedEntryLines = listOf(
        R.string.notification_missed_entry_1, R.string.notification_missed_entry_2,
        R.string.notification_missed_entry_3, R.string.notification_missed_entry_4,
        R.string.notification_missed_entry_5
    )

    private val reminderMorningOpeners = listOf(
        R.string.notification_morning_opener_1, R.string.notification_morning_opener_2,
        R.string.notification_morning_opener_3, R.string.notification_morning_opener_4
    )

    private val reminderEveningOpeners = listOf(
        R.string.notification_evening_opener_1, R.string.notification_evening_opener_2,
        R.string.notification_evening_opener_3, R.string.notification_evening_opener_4
    )

    private val reminderSarcasticMorning = listOf(
        R.string.notification_sarcastic_morning_1, R.string.notification_sarcastic_morning_2,
        R.string.notification_sarcastic_morning_3, R.string.notification_sarcastic_morning_4
    )

    private val reminderSarcasticEvening = listOf(
        R.string.notification_sarcastic_evening_1, R.string.notification_sarcastic_evening_2,
        R.string.notification_sarcastic_evening_3, R.string.notification_sarcastic_evening_4
    )

    fun generateExpenseMessage(
        context: Context,
        userName: String? = null,
        formattedAmount: String,
        category: String
    ): String {
        val opener = context.getString(sarcasticOpeners.random())
        val reaction = context.getString(moneyReactions.random())
        val guilt = context.getString(guiltLines.random())

        val categoryLine = when (category.lowercase()) {
            "food" -> context.getString(foodLines.random())
            "shopping" -> context.getString(shoppingLines.random())
            else -> context.getString(genericLines.random())
        }

        val namePart = userName?.let { "$it, " } ?: ""

        return context.getString(
            R.string.notification_format_expense,
            opener,
            namePart,
            formattedAmount,
            reaction,
            categoryLine,
            guilt
        )
    }

    fun generateReminderMessage(context: Context, isZomatoStyle: Boolean): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isMorning = hour < 14

        return if (isZomatoStyle) {
            if (isMorning) context.getString(reminderSarcasticMorning.random())
            else context.getString(reminderSarcasticEvening.random())
        } else {
            if (isMorning) {
                context.getString(
                    R.string.notification_morning_reminder_generic,
                    context.getString(reminderMorningOpeners.random())
                )
            } else {
                context.getString(
                    R.string.notification_evening_reminder_generic,
                    context.getString(reminderEveningOpeners.random())
                )
            }
        }
    }

    fun generateBudgetExceededMessage(context: Context, category: String): String {
        val opener = context.getString(sarcasticOpeners.random())
        val core = context.getString(budgetExceededLines.random())
        return context.getString(R.string.notification_format_budget_exceeded, opener, category, core)
    }

    fun generateBudgetApproachingMessage(
        context: Context,
        category: String,
        percentUsed: Int,
        remainingAmount: String
    ): String {
        val reaction = context.getString(budgetApproachingLines.random())
        return context.getString(
            R.string.notification_format_budget_approaching,
            reaction,
            "$percentUsed%",
            category,
            remainingAmount
        )
    }

    fun generateBudgetReachedMessage(context: Context, category: String, limit: String): String {
        return context.getString(R.string.notification_format_budget_reached, limit, category)
    }

    fun generateBudgetOverspentMessage(context: Context, category: String, overAmount: String): String {
        val guilt = context.getString(budgetOverspentLines.random())
        return context.getString(R.string.notification_format_budget_overspent, overAmount, category, guilt)
    }

    fun generateMissedEntryMessage(context: Context): String {
        return context.getString(missedEntryLines.random())
    }
}
