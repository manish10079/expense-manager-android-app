package com.mknlabs.expensetracker.sms

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mknlabs.expensetracker.MainActivity
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits
import java.util.concurrent.atomic.AtomicInteger
import androidx.core.app.RemoteInput

/**
 * Builds and shows the Smart SMS Import notification (plan §8) and transports
 * the [ParsedSms] payload between the receivers and the app via PendingIntent
 * extras — the payload is ephemeral and NEVER persisted (plan D2).
 *
 * Notifications post to a UNIQUE per-SMS ID (derived from the SMS timestamp)
 * on the high-importance `sms_import` channel, so consecutive detections
 * STACK in the shade instead of overwriting the previous one — and they all
 * join one [GROUP_KEY_SMS_IMPORT] group under a single collapsible summary
 * ("N transactions · total") at the fixed summary ID. With three actions:
 *  - [Add]    → [SmsActionReceiver], filing the detection through the inbox use
 *               case with an optionally typed note: the app never opens, and the
 *               inbox row is marked ADDED so the bell badge drops too.
 *  - [Ignore] → [SmsActionReceiver]: records the decision and clears the card.
 *  - [Edit]   → app opens targeting [NotificationHelper.DESTINATION_SMS_INBOX]
 *               on this very row, with the correction dialog already open.
 *
 * Tapping the notification body opens the inbox on the row as well. Every path
 * carries the inbox row's id rather than trusting the payload in the shade, so
 * a decision made here and a decision made in the app are recorded identically.
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
    const val EXTRA_OPEN_CATEGORY_ID = "sms.open_category_id"
    const val EXTRA_OPEN_TRANSACTION_TYPE_ID = "sms.open_transaction_type_id"

    const val KEY_TEXT_REPLY = "extra_sms_note"

    /**
     * The inbox row a notification belongs to. Carried in the action intents that
     * mutate that row, and in the posted notification's own extras (the group
     * summary uses it to tell a child from the summary itself).
     */
    const val EXTRA_DETECTION_ID = "sms.detection_id"

    /** "Edit" wants the correction dialog up on arrival, not just the row on screen. */
    const val EXTRA_OPEN_EDITOR = "sms.open_editor"

    const val ACTION_SMS_SAVE = "com.mknlabs.expensetracker.action.SMS_SAVE"

    /** Files the detection as a transaction without opening the app. */
    const val ACTION_SMS_ADD = "com.mknlabs.expensetracker.action.SMS_ADD"

    /** Dismisses the detection and clears its notification. */
    const val ACTION_SMS_IGNORE = "com.mknlabs.expensetracker.action.SMS_IGNORE"

    /** Notification ID rides in the action intent so the receiver can cancel it directly. */
    const val EXTRA_NOTIFICATION_ID = "sms.notification_id"

    /**
     * Groups every pending SMS import notification under one collapsible shade
     * entry. Children keep unique per-SMS IDs; a single summary notification at
     * the fixed [NotificationHelper.NOTIFICATION_ID_SMS_IMPORT] ID carries the
     * group. Grouping requires every child to set this key too.
     */
    const val GROUP_KEY_SMS_IMPORT = "sms_import_group"

    fun Intent.putNotificationId(notificationId: Int): Intent = apply {
        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
    }

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
        detectionId: String,
        notificationId: Int
    ) {
        // The id is resolved by the caller — from the SMS timestamp, so it stays stable
        // across process restarts and a second detection never replaces the first in
        // the shade — and handed in rather than recomputed here: the same value rides
        // in every action intent AND is stored on the inbox row, and deriving it twice
        // would drift whenever the SMS carries no usable timestamp.
        val requestBase = requestBaseFor(notificationId)

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

        val merchantName = parsed.merchant?.takeIf { it.isNotBlank() }

        // The title states the two things a decision needs — how much, and to whom —
        // e.g. "₹450 spent at Swiggy". Without a merchant there is nothing better than
        // the bank line itself, e.g. "₹520 Debited · HDFC Bank".
        val titleText = merchantName?.let { merchant ->
            context.getString(
                if (parsed.transactionTypeId == INCOME_TYPE_ID) {
                    R.string.notification_format_sms_import_title_received
                } else {
                    R.string.notification_format_sms_import_title_spent
                },
                amountText,
                merchant
            )
        } ?: context.getString(
            R.string.notification_format_sms_import_content,
            amountText,
            verb,
            senderText
        )

        // The suggestion is the subtitle: the amount is already the title, so this is
        // the only other thing worth knowing before tapping Add.
        val contentText = context.getString(
            R.string.notification_format_sms_import_suggested,
            categoryName
        )

        // Tapping the card opens the inbox on this row (never the Add screen: the row
        // still has to be filed, and that is exactly what the inbox is for).
        val openPendingIntent = activityPendingIntent(
            context,
            requestCode = requestBase + SLOT_OPEN,
            intent = openInboxIntent(context, detectionId, openEditor = false)
        )

        // RemoteInput for adding a note inline in the notification shade
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(context.getString(R.string.label_add_note_optional))
            .build()

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SMS_IMPORT)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(titleText)
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText("$titleText\n$contentText")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            // Read back off the POSTED notification: the amount keeps the group
            // summary's total honest, and the detection id is how the summary tells a
            // child apart from the summary itself.
            .addExtras(
                Bundle().apply {
                    putLong(EXTRA_AMOUNT_MINOR, parsed.amountMinor)
                    putString(EXTRA_DETECTION_ID, detectionId)
                }
            )
            // Grouping: all pending SMS imports collapse under one summary entry
            // (children alert normally so every new detection still heads-up).
            .setGroup(GROUP_KEY_SMS_IMPORT)
            .setGroupSummary(false)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)

        // [Add] Files the detection without opening the app, with a note that can be
        // typed inline (plan §14 Q2) — the app never has to come to the foreground.
        val addIntent = Intent(context, SmsActionReceiver::class.java).apply {
            action = ACTION_SMS_ADD
            // §2: the notification ID lets the receiver dismiss this exact card.
            putNotificationId(notificationId)
            putExtra(EXTRA_DETECTION_ID, detectionId)
            // The row is the source of truth, but the payload still rides along so a
            // detection purged by retention between posting and tapping is still
            // saved rather than silently dropped.
            putParsedSms(parsed)
        }
        // MUTABLE because RemoteInput results are written back into the intent.
        val addPendingIntent = PendingIntent.getBroadcast(
            context,
            requestBase + SLOT_ADD,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_notification_wallet,
                context.getString(R.string.notification_action_add),
                addPendingIntent
            )
                .addRemoteInput(remoteInput)
                .build()
        )

        // [Ignore] The decision is recorded, not just the card removed — ignoring in
        // the shade and ignoring in the app must leave the same state behind.
        val ignorePendingIntent = PendingIntent.getBroadcast(
            context,
            requestBase + SLOT_IGNORE,
            Intent(context, SmsActionReceiver::class.java).apply {
                action = ACTION_SMS_IGNORE
                putNotificationId(notificationId)
                putExtra(EXTRA_DETECTION_ID, detectionId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_notification_wallet,
                context.getString(R.string.notification_action_ignore),
                ignorePendingIntent
            ).build()
        )

        // [Edit] Straight into the inbox's correction dialog for this row, so the
        // amount, note and category can be fixed before anything is written.
        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_notification_wallet,
                context.getString(R.string.notification_action_edit),
                activityPendingIntent(
                    context,
                    requestCode = requestBase + SLOT_EDIT,
                    intent = openInboxIntent(context, detectionId, openEditor = true)
                )
            ).build()
        )

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
                // Keep the group summary's count and total in sync with the newly
                // stacked child — deferred for the same reason a cancellation is: a
                // rebuild issued in the same breath can read a snapshot that does not
                // contain the new child yet, and would then cancel the summary instead
                // of updating it.
                scheduleGroupSummaryRefresh(context)
            } catch (e: SecurityException) {
                // POST_NOTIFICATIONS not granted — the SMS import silently skips.
            }
        }
    }

    /**
     * Rebuilds the group summary for the pending SMS imports. The summary lives
     * at the fixed [NotificationHelper.NOTIFICATION_ID_SMS_IMPORT] ID while
     * every child keeps its unique per-SMS ID, so the shade shows ONE
     * collapsible entry for the whole batch. Called whenever a child is posted
     * or dismissed; cancels itself when the last child is gone.
     */
    fun refreshGroupSummary(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        val children = runCatching {
            nm.activeNotifications.filter { child ->
                // Children carry the group key AND their detection id; the summary
                // carries the group key alone. The detection id is the only marker that
                // is always present, so it is what tells the two apart.
                child.notification.group == GROUP_KEY_SMS_IMPORT &&
                    child.notification.extras?.getString(EXTRA_DETECTION_ID) != null
            }
        }.getOrDefault(emptyList())

        if (children.isEmpty()) {
            nm.cancel(NotificationHelper.NOTIFICATION_ID_SMS_IMPORT)
            return
        }

        val totalMinor = children.sumOf {
            it.notification.extras?.getLong(EXTRA_AMOUNT_MINOR) ?: 0L
        }
        val totalText = formatCurrencyValue(
            amount = totalMinor.toMajorUnits(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences
        )
        val summaryText = context.getString(
            R.string.notification_format_sms_import_summary,
            children.size,
            totalText
        )

        // Tapping the summary opens the app without any SMS prefill payload.
        val contentIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_SUMMARY,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // The summary expands to a per-transaction list (InboxStyle) mirroring
        // the BigText lines of each child notification.
        val inboxStyle = NotificationCompat.InboxStyle()
        for (child in children) {
            // One line per transaction, using the same amount/merchant heading the
            // child itself shows (the subtitle is only a category suggestion, which
            // would read as noise in a list of transactions).
            val line = child.notification.extras
                ?.getString(EXTRA_NOTIFICATION_TITLE)
                ?.takeIf { it.isNotBlank() }
                ?: child.notification.extras
                    ?.getString(Notification.EXTRA_TEXT)
                    ?.takeIf { it.isNotBlank() }
            if (line != null) inboxStyle.addLine(line)
        }

        val summary = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SMS_IMPORT)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_sms_import))
            .setContentText(summaryText)
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY_SMS_IMPORT)
            .setGroupSummary(true)
            .setOnlyAlertOnce(true)
            .build()

        runCatching {
            nm.notify(NotificationHelper.NOTIFICATION_ID_SMS_IMPORT, summary)
        }
    }

    /**
     * Cancels the SMS import notification for [notificationId]. Defaults to the
     * legacy fixed ID for callers that don't carry a per-SMS ID (the Change-sheet
     * save path); live notifications always pass their unique ID instead.
     */
    fun cancel(context: Context, notificationId: Int = NotificationHelper.NOTIFICATION_ID_SMS_IMPORT) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Rebuilds the group summary once the notification service has caught up with a
     * cancellation.
     *
     * [cancel] is dispatched asynchronously, so a [refreshGroupSummary] issued in the
     * same breath still sees the cancelled child in the active-notification snapshot —
     * and would leave the summary claiming a transaction the user has already decided
     * on. The receivers can refresh inline (their database write already separates the
     * two); callers that cancel and rebuild from one thread use this instead.
     */
    fun scheduleGroupSummaryRefresh(context: Context) {
        val appContext = context.applicationContext
        Handler(Looper.getMainLooper()).postDelayed(
            { refreshGroupSummary(appContext) },
            SUMMARY_SETTLE_MS
        )
    }

    /**
     * §3 — Synchronous, immediate dismissal. Invoked on the main thread in the
     * receiver's [SmsActionReceiver.onReceive] BEFORE any database/network
     * work, using the notification ID carried in the action intent (§2). This
     * is the documented pattern every messaging app relies on.
     */
    fun cancelImmediately(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * §4A — OEM fallback for the heads-up / RemoteInput "ghost card" bug.
     *
     * On some Android versions and OEM skins (Samsung One UI, Xiaomi
     * HyperOS/MIUI, OPPO ColorOS, Vivo FuntouchOS) a plain [cancelImmediately]
     * is ignored while the RemoteInput is still in its "sending" state —
     * SystemUI only resets that stuck view via [onNotificationUpdateOrReset],
     * i.e. when the notification is UPDATED, not cancelled.
     *
     * So we re-post the SAME ID with a lowered-priority, explicitly
     * non-ongoing, RemoteInput-free notification. Verified empirically on API
     * 34: a 1 ms auto-timeout is TOO fast (the removal races the update
     * propagation to SystemUI, leaving the ghost), and a back-to-back
     * notify()+cancel() has the same race — but a re-post that SystemUI gets
     * time to render clears the stuck row cleanly (a new SMS re-posting the
     * same ID was confirmed to clear it instantly).
     *
     * So we re-notify and let a longer [setTimeoutAfter] auto-remove the row
     * AFTER SystemUI has processed the update and reset the stuck view. On
     * API < 26 (no timeout support) we fall back to a direct cancel. When the
     * immediate cancel already succeeded this is effectively a no-op.
     */
    fun forceDismiss(context: Context, notificationId: Int) {
        val nm = NotificationManagerCompat.from(context)
        val reset = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SMS_IMPORT)
            .setSmallIcon(R.drawable.ic_notification_wallet)
            .setContentTitle(context.getString(R.string.notification_title_sms_import))
            .setPriority(NotificationCompat.PRIORITY_MIN) // heads-up override: drop to min
            .setOngoing(false)                            // §4B: never ongoing → cancel honored
            .setAutoCancel(true)
            .setTimeoutAfter(FORCE_DISMISS_TIMEOUT_MS)    // §4A: update, then auto-remove
            // Stay inside the group while the ghost-card reset is in flight.
            .setGroup(GROUP_KEY_SMS_IMPORT)
            .setGroupSummary(false)
            .build()
        nm.notify(notificationId, reset)
        // Do NOT cancel immediately — the timeout removes the row after SystemUI
        // has processed the update. Below API 26 (no timeout support) cancel now.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            nm.cancel(notificationId)
        }
    }

    /**
     * Opens the app on the detected-SMS inbox, focused on [detectionId]. Both the
     * card tap and the [Edit] action use this — the only difference is whether the
     * correction dialog is already up when the row lands.
     */
    private fun openInboxIntent(context: Context, detectionId: String, openEditor: Boolean): Intent {
        return Intent(context, MainActivity::class.java).apply {
            // singleTop + SINGLE_TOP: reuse the existing MainActivity via
            // onNewIntent when the app is alive in the background, instead of
            // CLEAR_TASK which force-restarts the activity and replays the splash.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(NotificationHelper.EXTRA_NAV_DESTINATION, NotificationHelper.DESTINATION_SMS_INBOX)
            putExtra(EXTRA_DETECTION_ID, detectionId)
            if (openEditor) putExtra(EXTRA_OPEN_EDITOR, true)
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

    private const val INCOME_TYPE_ID = 1
    private const val EXPENSE_TYPE_ID = 2

    /**
     * Action slots inside [requestBaseFor]'s block of four. The content intent keeps the
     * top slot it has always used; the three action buttons each take their own so
     * "Add" and "Ignore" can never resolve onto one another's extras.
     */
    private const val SLOT_ADD = 0
    private const val SLOT_IGNORE = 1
    private const val SLOT_EDIT = 2
    private const val SLOT_OPEN = 3

    /**
     * Request code for the group summary's Open intent. Children always use
     * codes >= 4, and the reminder notifications use {0, 2, 3, 4, 6}, so 1 is
     * unique across every MainActivity PendingIntent in the app.
     */
    private const val REQUEST_CODE_SUMMARY = 1

    /** Offset above the fixed IDs (1-6) used by the other channels. */
    private const val FALLBACK_BASE_ID = 1000

    /**
     * `Notification.EXTRA_TITLE`. Spelled out rather than referenced through the
     * platform constant because the field is only public from API 24, while the SMS
     * import notification runs on older releases too.
     */
    private const val EXTRA_NOTIFICATION_TITLE = "android.title"

    /** Monotonic fallback when an SMS carries no usable timestamp. */
    private val fallbackCounter = AtomicInteger(0)

    /**
     * Stable, unique notification ID for one SMS event. The SMS timestamp is
     * the same key used for duplicate suppression, so the ID survives process
     * restarts and the Save/Open actions always resolve the right notification.
     */
    // Public so the receiver that recorded the detection can persist the same id on
    // its inbox row, letting the notification's actions resolve that row later.
    fun notificationIdFor(smsTimestamp: Long): Int {
        if (smsTimestamp > 0) return (smsTimestamp and 0x7FFFFFFFL).toInt()
        return FALLBACK_BASE_ID + fallbackCounter.incrementAndGet()
    }

    /**
     * PendingIntent request-code block for one notification. Request codes must
     * differ between stacked notifications, otherwise FLAG_UPDATE_CURRENT would
     * make every notification's Save/Open actions collapse onto the most recent
     * SMS's extras. The 28-bit fold keeps the code positive with 2 bits of
     * headroom for the action slots; the +1 offset guarantees the block starts
     * at >= 4 so it can never collide with the fixed codes (0, 2, 3, 4, 6) the
     * daily/missed-entry reminders use for their MainActivity PendingIntents.
     */
    private fun requestBaseFor(notificationId: Int): Int =
        ((notificationId and 0x0FFFFFFF) + 1) * 4

    /**
     * How long the §4A reset notification stays before auto-dismissing. Long
     * enough for SystemUI to process the update and reset the stuck
     * RemoteInputView, short enough to be imperceptible to the user.
     */
    private const val FORCE_DISMISS_TIMEOUT_MS = 500L

    /**
     * How long to let the notification service settle before rebuilding the summary
     * after a cancellation. Long enough for the cancel to land, short enough to be
     * imperceptible in the shade.
     */
    private const val SUMMARY_SETTLE_MS = 400L
}
