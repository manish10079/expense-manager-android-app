package com.mknlabs.expensetracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
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
 * Handles the notification's [Save] action — a true one-tap save without
 * opening the app (plan §8):
 *  - transaction type + suggested category from the detected SMS,
 *  - note typed via RemoteInput (plan §14 Q2),
 *  - payment method = the user's configured default (plan §14 Q1).
 *
 * Dismissal strategy (per the RemoteInput dismissal spec):
 *  §3: The notification is cancelled SYNCHRONOUSLY at the top of onReceive,
 *      on the main thread, BEFORE any database/network work — using the
 *      notification ID passed in the action intent (§2). This is the
 *      documented pattern messaging apps rely on.
 *  §4A: After the async save completes, an OEM fallback (forceDismiss)
 *      re-posts a min-priority, non-ongoing update that auto-dismisses after
 *      500 ms — giving SystemUI time to reset the RemoteInputView "sending"
 *      ghost card on ROMs where a plain cancel() is ignored while a reply is
 *      in flight.
 *  Dedup: an in-memory atomic set keyed on the SMS timestamp blocks repeated
 *      taps / RemoteInput re-submissions, so one tap = one transaction.
 */
@AndroidEntryPoint
class SmsActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsRepository: SmsRepository

    @Inject
    lateinit var appPreferencesRepository: AppPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SmsNotificationManager.ACTION_SMS_SAVE) return
        val parsed = intent.toParsedSms() ?: return

        // ── §3: dismiss IMMEDIATELY, synchronously, before any async work ──
        // The notification ID rides in the action intent (§2). Cancelling on the
        // main thread right now makes the card leave the shade instantly on every
        // ROM — never wait for the DB write or a network call.
        val notificationId = intent.getIntExtra(
            SmsNotificationManager.EXTRA_NOTIFICATION_ID,
            NotificationHelper.NOTIFICATION_ID_SMS_IMPORT
        )
        SmsNotificationManager.cancelImmediately(context, notificationId)

        // Extract the inline note BEFORE the async work (spec §3 step 2).
        val note = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(SmsNotificationManager.KEY_TEXT_REPLY)
            ?.toString()
            .orEmpty()
            .trim()

        // Use the SMS timestamp as a unique key for this transaction event.
        // Drop any broadcast that is already handled, then opportunistically sweep
        // stale keys (excluding the one just added, so a concurrent add for the
        // same SMS can never be undone) to keep the set bounded.
        val key = parsed.smsTimestamp
        if (!processingKeys.add(key)) return   // already being handled — drop duplicate
        val cutoff = System.currentTimeMillis() - DEDUP_WINDOW_MS
        processingKeys.removeIf { it != key && it < cutoff }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var failed = false
            try {
                // Secondary guard: skip if an identical amount+timestamp is already in DB.
                if (smsRepository.isDuplicate(parsed)) return@launch

                val paymentTypeId = appPreferencesRepository
                    .observeAppSettings()
                    .first()
                    .defaultPaymentTypeId

                smsRepository.saveFromSms(parsed, note = note, paymentTypeId = paymentTypeId)
            } catch (e: Exception) {
                failed = true
                android.util.Log.w("SmsActionReceiver", "Failed to save SMS transaction", e)
            } finally {
                pendingResult.finish()
                if (!failed) {
                    // §4A OEM fallback: re-post a 1 ms-timeout, min-priority,
                    // non-ongoing update and cancel it. If the synchronous cancel
                    // above was honored, this is a no-op; if a ROM ignored it
                    // (RemoteInput "sending" ghost), the update resets the stuck
                    // row and the timeout removes it.
                    SmsNotificationManager.forceDismiss(context, notificationId)
                } else {
                    processingKeys.remove(key)
                }
            }
        }
    }

    companion object {
        /** How long one SMS event stays deduplicated; older keys are pruned. */
        private const val DEDUP_WINDOW_MS = 24L * 60L * 60L * 1000L

        /** Tracks SMS timestamps already handled to prevent duplicate saves. */
        private val processingKeys = ConcurrentHashMap.newKeySet<Long>()
    }
}
