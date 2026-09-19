package com.mknlabs.expensetracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.FileDetectionResult
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.FileSmsDetectionAsTransactionUseCase
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.IgnoreSmsDetectionsUseCase
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.sms.SmsNotificationManager.toParsedSms
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Handles the detected-SMS notification's action buttons:
 *  - [Add]    → files the detection through the inbox's own use case, so the row is
 *               marked ADDED and the bell badge drops in the same breath. The app never
 *               has to open; a note can be typed inline (plan §14 Q2) and the user's
 *               configured default payment method is applied.
 *  - [Ignore] → records the decision through the same use case the inbox uses.
 *  - [Save]   → the pre-inbox one-tap save, kept for the payload-carrying intents that
 *               were already sitting in the shade before this version shipped (and as
 *               the fallback when the row itself is gone).
 *
 * Every path resolves the detection by the id in the action intent rather than trusting
 * the payload, so a decision taken in the shade and one taken in the app cannot drift.
 *
 * Dismissal strategy (per the RemoteInput dismissal spec):
 *  §3: The notification is cancelled SYNCHRONOUSLY at the top of onReceive, on the main
 *      thread, BEFORE any database/network work — using the notification ID passed in
 *      the action intent (§2). This is the documented pattern messaging apps rely on.
 *  §4A: After the async save completes, an OEM fallback (forceDismiss) re-posts a
 *      min-priority, non-ongoing update that auto-dismisses after 500 ms — giving
 *      SystemUI time to reset the RemoteInputView "sending" ghost card on ROMs where a
 *      plain cancel() is ignored while a reply is in flight.
 *  Dedup: an in-memory atomic set keyed on the SMS timestamp blocks repeated taps /
 *      RemoteInput re-submissions, so one tap = one transaction.
 */
@AndroidEntryPoint
class SmsActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsRepository: SmsRepository

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    @Inject
    lateinit var fileDetection: FileSmsDetectionAsTransactionUseCase

    @Inject
    lateinit var ignoreDetections: IgnoreSmsDetectionsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            SmsNotificationManager.ACTION_SMS_ADD -> onAdd(context, intent)
            SmsNotificationManager.ACTION_SMS_IGNORE -> onIgnore(context, intent)
            SmsNotificationManager.ACTION_SMS_SAVE -> onSave(context, intent)
            else -> return
        }
    }

    /**
     * Files the detection the notification announced. The row, not the payload, is
     * authoritative — the payload only steps in when retention has already purged the
     * row, so a confirmed payment is never silently dropped.
     */
    private fun onAdd(context: Context, intent: Intent) {
        val detectionId = intent.getStringExtra(SmsNotificationManager.EXTRA_DETECTION_ID)
        val parsed = intent.toParsedSms()
        if (detectionId == null && parsed == null) return

        val notificationId = notificationIdOf(intent)
        SmsNotificationManager.cancelImmediately(context, notificationId)

        val note = noteFrom(intent)
        // Dedup on the SMS timestamp, exactly as the legacy save path does: a double tap
        // (or a re-submitted remote input) must not write two transactions.
        val key = parsed?.smsTimestamp ?: 0L
        if (!markProcessing(key)) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var failed = false
            try {
                val missing = detectionId != null &&
                    fileDetection(detectionId = detectionId, note = note) is FileDetectionResult.NotFound
                if (detectionId == null || missing) {
                    // No row to file (an intent posted by an older version, or a row
                    // removed by retention): fall back to the payload the card carried.
                    parsed?.let { saveFromPayload(it, note) }
                }
            } catch (e: Exception) {
                failed = true
                android.util.Log.w(TAG, "Failed to add detected SMS transaction", e)
            } finally {
                pendingResult.finish()
                if (!failed) {
                    // §4A OEM fallback for the RemoteInput "sending" ghost card.
                    SmsNotificationManager.forceDismiss(context, notificationId)
                } else {
                    processingKeys.remove(key)
                }
                SmsNotificationManager.refreshGroupSummary(context)
            }
        }
    }

    /**
     * Dismisses the detection and clears its card. The decision goes through the inbox
     * use case, so ignoring here leaves exactly the state ignoring in the app does —
     * including taking the card out of the shade.
     */
    private fun onIgnore(context: Context, intent: Intent) {
        val detectionId = intent.getStringExtra(SmsNotificationManager.EXTRA_DETECTION_ID) ?: return

        // Same §3 rule as Add: leave the shade before touching the database.
        val notificationId = notificationIdOf(intent)
        SmsNotificationManager.cancelImmediately(context, notificationId)

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                ignoreDetections(listOf(detectionId))
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Failed to ignore detected SMS", e)
            } finally {
                pendingResult.finish()
                // No RemoteInput is involved in this action, so no ghost-card fallback
                // is needed — only the summary has to catch up with the missing child.
                SmsNotificationManager.refreshGroupSummary(context)
            }
        }
    }

    /**
     * The original one-tap save, driven by the payload in the intent. Still the correct
     * path for a notification posted before the inbox learned to own these actions.
     */
    private fun onSave(context: Context, intent: Intent) {
        val parsed = intent.toParsedSms() ?: return

        // ── §3: dismiss IMMEDIATELY, synchronously, before any async work ──
        val notificationId = notificationIdOf(intent)
        SmsNotificationManager.cancelImmediately(context, notificationId)

        val note = noteFrom(intent)
        if (!markProcessing(parsed.smsTimestamp)) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var failed = false
            try {
                saveFromPayload(parsed, note)
            } catch (e: Exception) {
                failed = true
                android.util.Log.w(TAG, "Failed to save SMS transaction", e)
            } finally {
                pendingResult.finish()
                if (!failed) {
                    // §4A OEM fallback: re-post a min-priority, non-ongoing update that
                    // auto-dismisses, so SystemUI resets a stuck RemoteInputView. If the
                    // synchronous cancel above was honored, this is a no-op.
                    SmsNotificationManager.forceDismiss(context, notificationId)
                } else {
                    processingKeys.remove(parsed.smsTimestamp)
                }
                // Keep the collapsible group summary in sync with the remaining pending
                // SMS imports now that this child is gone.
                SmsNotificationManager.refreshGroupSummary(context)
            }
        }
    }

    /** Saves a detection from the payload alone (no inbox row to file). */
    private suspend fun saveFromPayload(parsed: ParsedSms, note: String) {
        // Secondary guard: skip if an identical amount+timestamp is already in DB.
        if (smsRepository.isDuplicate(parsed)) return

        val paymentTypeId = appPreferencesRepository
            .observeAppSettings()
            .first()
            .defaultPaymentTypeId

        smsRepository.saveFromSms(parsed, note = note, paymentTypeId = paymentTypeId)
    }

    /**
     * Registers [key] as being handled. False means this exact SMS event is already in
     * flight and the broadcast must be dropped. A zero key (no usable timestamp) is
     * never treated as a duplicate.
     */
    private fun markProcessing(key: Long): Boolean {
        if (key == 0L) return true
        if (!processingKeys.add(key)) return false
        // Opportunistically sweep stale keys — excluding the one just added, so a
        // concurrent action for the same SMS can never be undone.
        val cutoff = System.currentTimeMillis() - DEDUP_WINDOW_MS
        processingKeys.removeIf { it != key && it < cutoff }
        return true
    }

    /** The notification ID rides in the action intent (§2) so the receiver can cancel it. */
    private fun notificationIdOf(intent: Intent): Int = intent.getIntExtra(
        SmsNotificationManager.EXTRA_NOTIFICATION_ID,
        NotificationHelper.NOTIFICATION_ID_SMS_IMPORT
    )

    /** Extracted BEFORE the async work (spec §3 step 2). */
    private fun noteFrom(intent: Intent): String = RemoteInput.getResultsFromIntent(intent)
        ?.getCharSequence(SmsNotificationManager.KEY_TEXT_REPLY)
        ?.toString()
        .orEmpty()
        .trim()

    companion object {
        private const val TAG = "SmsActionReceiver"

        /** How long one SMS event stays deduplicated; older keys are pruned. */
        private const val DEDUP_WINDOW_MS = 24L * 60L * 60L * 1000L

        /** Tracks SMS timestamps already handled to prevent duplicate saves. */
        private val processingKeys = ConcurrentHashMap.newKeySet<Long>()
    }
}
