package com.mknlabs.expensetracker.sms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mknlabs.expensetracker.MainActivity
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits
import java.math.BigDecimal
import androidx.core.app.RemoteInput

/**
 * Builds and shows the Smart SMS Import notification (plan §8) and transports
 * the [ParsedSms] payload between the receivers and the app via PendingIntent
 * extras — the payload is ephemeral and NEVER persisted (plan D2).
 *
 * Notification ID 5 on the high-importance `sms_import` channel, with three
 * actions:
 *  - [Save]   → [SmsActionReceiver] (one-tap save, app never opens)
 *  - [Change] → app opens targeting [NotificationHelper.DESTINATION_SMS_CHANGE]
 *               (the lightweight category sheet, Phase 4)
 *  - [Open]   → full Add Transaction screen prefilled via the existing draft
 *               mechanism (amount + note = sender · SMS body)
 */
object SmsNotificationManager {

    /** ParsedSms transport keys — ride in PendingIntent extras only. */
    const val EXTRA_AMOUNT_MINOR = "sms.amount_minor"
    const val EXTRA_SENDER = "sms.sender"
    const val EXTRA_BODY = "sms.body"
    const val EXTRA_SMS_TIMESTAMP = "sms.timestamp"
    const val EXTRA_TRANSACTION_TYPE_ID = "sms.transaction_type_id"
    const val EXTRA_CATEGORY_ID = "sms.category_id"
    const val EXTRA_MERCHANT = "sms.merchant"
    const val EXTRA_CONFIDENCE = "sms.confidence"

    /** "Open" action prefill keys — consumed by MainActivity/MainScreen. */
    const val EXTRA_OPEN_AMOUNT = "sms.open_amount"
    const val EXTRA_OPEN_NOTE = "sms.open_note"

    const val KEY_TEXT_REPLY = "extra_sms_note"

    const val ACTION_SMS_SAVE = "com.mknlabs.expensetracker.action.SMS_SAVE"

    /** Convenience aliases used by SmsActionReceiver for confirmation notification. */
    const val CHANNEL_ID = NotificationHelper.CHANNEL_SMS_IMPORT
    const val CONFIRMATION_NOTIFICATION_ID = NotificationHelper.NOTIFICATION_ID_SMS_CONFIRMATION

    fun Intent.putParsedSms(parsed: ParsedSms): Intent = apply {
        putExtra(EXTRA_AMOUNT_MINOR, parsed.amountMinor)
        putExtra(EXTRA_SENDER, parsed.sender)
        putExtra(EXTRA_BODY, parsed.body)
        putExtra(EXTRA_SMS_TIMESTAMP, parsed.smsTimestamp)
        putExtra(EXTRA_TRANSACTION_TYPE_ID, parsed.transactionTypeId)
        putExtra(EXTRA_CATEGORY_ID, parsed.categoryId)
        putExtra(EXTRA_MERCHANT, parsed.merchant)
        putExtra(EXTRA_CONFIDENCE, parsed.confidence.name)
    }

    fun Intent.toParsedSms(): ParsedSms? {
        if (!hasExtra(EXTRA_AMOUNT_MINOR) || !hasExtra(EXTRA_SMS_TIMESTAMP)) return null
        val confidenceName = getStringExtra(EXTRA_CONFIDENCE) ?: return null
        val confidence = runCatching { SmsConfidence.valueOf(confidenceName) }.getOrNull() ?: return null
        return ParsedSms(
            amountMinor = getLongExtra(EXTRA_AMOUNT_MINOR, 0L),
            sender = getStringExtra(EXTRA_SENDER) ?: "",
            body = getStringExtra(EXTRA_BODY) ?: "",
            smsTimestamp = getLongExtra(EXTRA_SMS_TIMESTAMP, 0L),
            transactionTypeId = getIntExtra(EXTRA_TRANSACTION_TYPE_ID, EXPENSE_TYPE_ID),
            categoryId = getIntExtra(EXTRA_CATEGORY_ID, 0),
            merchant = getStringExtra(EXTRA_MERCHANT),
            confidence = confidence
        )
    }

    fun showImportNotification(
        context: Context,
        parsed: ParsedSms,
        frequentCategories: List<CategoryType>
    ) {
        // The parsed amount is in the SMS's own currency (₹/INR by parser design),
        // so we format with the app default rather than the user's display currency
        // — showing the display currency would be a misleading non-conversion.
        val amountText = formatCurrencyValue(
            amount = parsed.amountMinor.toMajorUnits(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences
        )
        val verb = context.getString(
            if (parsed.transactionTypeId == INCOME_TYPE_ID) {
                R.string.sms_verb_credited
            } else {
                R.string.sms_verb_debited
            }
        )
        val categoryName = categoryMap[parsed.categoryId]?.name
            ?: context.getString(R.string.label_other)
        val senderText = parsed.sender.ifBlank {
            context.getString(R.string.sms_sender_unknown)
        }

        // e.g. "₹520 Debited · HDFC Bank"
        val contentText = context.getString(
            R.string.notification_format_sms_import_content,
            amountText,
            verb,
            senderText
        )
        val suggestedLine = context.getString(
            R.string.notification_format_sms_import_suggested,
            categoryName
        )

        val openPendingIntent = activityPendingIntent(
            context,
            requestCode = REQUEST_CODE_OPEN,
            intent = openActivityIntent(context, parsed)
        )

        // RemoteInput for adding a note inline in the notification shade
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(context.getString(R.string.label_add_note_optional))
            .build()

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SMS_IMPORT)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_sms_import))
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText("$contentText\n$suggestedLine")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)

        // Show up to 3 action buttons for frequently chosen categories
        // Tapping any of these opens the remote input to write a note and saves.
        val categoriesToShow = frequentCategories.take(3)
        categoriesToShow.forEachIndexed { index, category ->
            val saveIntent = Intent(context, SmsActionReceiver::class.java).apply {
                action = ACTION_SMS_SAVE
                // Put parsed SMS data, but update the categoryId to this action's category
                putParsedSms(parsed.copy(categoryId = category.id))
            }
            val savePendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_SAVE + index,
                saveIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val action = NotificationCompat.Action.Builder(
                R.drawable.ic_notification_wallet,
                category.name,
                savePendingIntent
            )
            .addRemoteInput(remoteInput)
            .build()

            builder.addAction(action)
        }

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NotificationHelper.NOTIFICATION_ID_SMS_IMPORT, builder.build())
            } catch (e: SecurityException) {
                // POST_NOTIFICATIONS not granted — the SMS import silently skips.
            }
        }
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID_SMS_IMPORT)
    }

    private fun openActivityIntent(context: Context, parsed: ParsedSms): Intent {
        return Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via
            // onNewIntent when the app is alive in the background, instead of
            // CLEAR_TASK which force-restarts the activity and replays the splash.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NotificationHelper.EXTRA_NAV_DESTINATION, NotificationHelper.DESTINATION_ADD_TRANSACTION)
            // Plain digits (no grouping) — AddTransactionScreen parses with toDoubleOrNull().
            putExtra(EXTRA_OPEN_AMOUNT, editableAmount(parsed.amountMinor))
            putExtra(
                EXTRA_OPEN_NOTE,
                context.getString(
                    R.string.notification_format_sms_open_note,
                    parsed.sender,
                    parsed.body
                )
            )
        }
    }

    private fun activityPendingIntent(context: Context, requestCode: Int, intent: Intent): PendingIntent {
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** e.g. 52_000 minor → "520"; 15_000_00 minor → "15000"; 52_050 minor → "520.5". */
    private fun editableAmount(amountMinor: Long): String {
        return BigDecimal.valueOf(amountMinor.toMajorUnits())
            .stripTrailingZeros()
            .toPlainString()
    }

    private const val INCOME_TYPE_ID = 1
    private const val EXPENSE_TYPE_ID = 2
    private const val REQUEST_CODE_SAVE = 100
    private const val REQUEST_CODE_CHANGE = 101
    private const val REQUEST_CODE_OPEN = 102
}
