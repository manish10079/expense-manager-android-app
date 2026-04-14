package com.mkn0079.expensetracker.notifications

import java.util.Calendar

object DynamicNotificationEngine {

    private val sarcasticOpeners = listOf(
        "Well well well", "Interesting", "Ahh", "Look at you",
        "Breaking news", "Alert", "Update", "Oh, look who it is"
    )

    private val moneyReactions = listOf(
        "money just flew away 💸", "wallet took a hit 😬", "budget is shaking 😳",
        "that escalated quickly 🚀", "your bank account noticed 👀", "is that a hole in your pocket?"
    )

    private val guiltLines = listOf(
        "Hope it was worth it 😏", "No regrets… right? 😬", "Future you is watching 👀",
        "We won’t judge… much 😌", "This better be important 😄", "Your savings account is crying."
    )

    private val foodLines = listOf(
        "Food again? Respect 🍕", "Eating like a king 👑", "Diet plan left the chat 🍔",
        "Taste > Budget, huh? 😏", "Calories don't count, but cents do."
    )

    private val shoppingLines = listOf(
        "Retail therapy activated 🛍️", "Impulse or planned? 😏", "That looked necessary 😬",
        "Shopping mood ON 💳", "Adding to the collection? 🛍️"
    )

    private val genericLines = listOf(
        "Another expense logged 📊", "Tracking like a pro 😎", "Money well… spent? 😏",
        "Noted 👀", "Keeping it real 📈"
    )

    private val budgetExceededLines = listOf(
        "Your budget just called. It’s quitting. 💸",
        "Expense limit? Never heard of her. 💅",
        "You’re spending like you found a cheat code. 🎮",
        "Budget: Exceeded. Sadness: Imminent. 📉",
        "Your savings are screaming. 😱"
    )

    private val missedEntryLines = listOf(
        "It’s too quiet here... did you stop eating? 🍔",
        "Your wallet is feeling suspiciously heavy. Log something! 💸",
        "The silence is deafening. Where are the transactions? 🕵️",
        "Did you win the lottery? Why no logs today? 🎰",
        "Your tracker is lonely. Give it some data to chew on. 🦴"
    )

    // --- REMINDER SECTION ---

    // --- REMINDER SECTION ---

    private val reminderMorningOpeners = listOf(
        "Good morning! ☀️", "Rise and shine! ☕", "Morning update! 🌅", "Wakey wakey! 🥐"
    )

    private val reminderEveningOpeners = listOf(
        "Day's almost done! 🌙", "Evening check! 🌆", "Dinner time? 🥘", "Tapping out? 🛌"
    )

    private val reminderSarcasticMorning = listOf(
        "Did you buy coffee yet or are you waiting for a sign? ☕",
        "Tracking your breakfast is the best exercise you'll do today. 🥐",
        "The early bird catches the worm, but the smart bird logs the cost. 🐛",
        "Your wallet is awake and it has questions. 💸"
    )

    private val reminderSarcasticEvening = listOf(
        "Your budget survived the day... or did it? 👀",
        "Don't go to sleep with unlogged secrets. Data is watching. 🕵️",
        "That dinner was great, but the tracking will be legendary. 🥘",
        "One small log for you, one giant leap for your savings. 🚀"
    )

    fun generateExpenseMessage(
        userName: String? = null,
        amount: Double,
        category: String
    ): String {
        val opener = sarcasticOpeners.random()
        val reaction = moneyReactions.random()
        val guilt = guiltLines.random()

        val categoryLine = when (category.lowercase()) {
            "food" -> foodLines.random()
            "shopping" -> shoppingLines.random()
            else -> genericLines.random()
        }

        val namePart = userName?.let { "$it, " } ?: ""

        return "$opener 👀 ${namePart}₹$amount spent on $category… $reaction. $categoryLine. $guilt"
    }

    fun generateReminderMessage(isZomatoStyle: Boolean): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isMorning = hour < 14

        return if (isZomatoStyle) {
            if (isMorning) reminderSarcasticMorning.random()
            else reminderSarcasticEvening.random()
        } else {
            if (isMorning) {
                "${reminderMorningOpeners.random()} Don't forget to log your morning expenses!"
            } else {
                "${reminderEveningOpeners.random()} Sparred a minute to log your dinner or travel?"
            }
        }
    }

    fun generateBudgetExceededMessage(category: String): String {
        val opener = sarcasticOpeners.random()
        val core = budgetExceededLines.random()
        return "$opener! You just blew past your $category budget. $core"
    }

    fun generateMissedEntryMessage(): String {
        return missedEntryLines.random()
    }
}
