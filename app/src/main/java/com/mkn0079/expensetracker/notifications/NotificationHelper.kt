package com.mkn0079.expensetracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mkn0079.expensetracker.MainActivity
import com.mkn0079.expensetracker.R

object NotificationHelper {

    const val CHANNEL_DAILY_REMINDERS = "daily_reminders"
    const val CHANNEL_BUDGET_ALERTS = "budget_alerts"
    
    const val EXTRA_NAV_DESTINATION = "nav_destination"
    const val DESTINATION_ADD_TRANSACTION = "add_transaction"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDERS,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic reminders to log your expenses."
            }

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you exceed your monthly budget."
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(budgetChannel)
        }
    }

    fun showReminderNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_ADD_TRANSACTION)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Fallback until a better icon is chosen
            .setContentTitle("Expense Tracker")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(1, builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission
            }
        }
    }

    fun showBudgetExceededNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Potentially add nav to a budget screen here if it exists.
            // For now, sticking to Transactions or Home.
            putExtra(EXTRA_NAV_DESTINATION, "home") 
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Budget Alert!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(2, builder.build())
            } catch (e: SecurityException) { }
        }
    }

    fun showMissedEntryNotification(context: Context, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_ADD_TRANSACTION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Missed Today?")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(3, builder.build())
            } catch (e: SecurityException) { }
        }
    }

    fun showTestNotification(context: Context) {
        val message = DynamicNotificationEngine.generateReminderMessage(true)
        showReminderNotification(context, "[TEST] $message")
    }

    fun showTestBudgetNotification(context: Context) {
        val message = DynamicNotificationEngine.generateBudgetExceededMessage("Shopping")
        showBudgetExceededNotification(context, "[TEST] $message")
    }
}
