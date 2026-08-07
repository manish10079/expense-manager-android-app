package com.mknlabs.expensetracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
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
 * The notification is cancelled IMMEDIATELY on receipt (before the async save)
 * so that repeated taps / RemoteInput re-submissions cannot add duplicate
 * transactions, and the user gets instant feedback that the send worked —
 * nothing is re-shown afterwards. An in-memory atomic set guards concurrent
 * broadcast deliveries.
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

        // Use the SMS timestamp as a unique key for this transaction event.
        // If another broadcast for the same SMS arrives concurrently, drop it.
        val key = parsed.smsTimestamp
        if (!processingKeys.add(key)) return   // already being handled — drop duplicate

        // ── Cancel the notification IMMEDIATELY so the user cannot tap again ──
        // This is the primary guard: the action buttons become unavailable the
        // moment the first broadcast is received, long before the DB write completes.
        SmsNotificationManager.cancel(context)

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // Secondary guard: skip if an identical amount+timestamp is already in DB.
                if (smsRepository.isDuplicate(parsed)) return@launch

                val paymentTypeId = appPreferencesRepository
                    .observeAppSettings()
                    .first()
                    .defaultPaymentTypeId

                // Extract inline note typed by the user via RemoteInput
                val remoteInputResults = RemoteInput.getResultsFromIntent(intent)
                val note = remoteInputResults
                    ?.getCharSequence(SmsNotificationManager.KEY_TEXT_REPLY)
                    ?.toString()
                    .orEmpty()
                    .trim()

                smsRepository.saveFromSms(parsed, note = note, paymentTypeId = paymentTypeId)
            } catch (e: Exception) {
                android.util.Log.w("SmsActionReceiver", "Failed to save SMS transaction", e)
            } finally {
                processingKeys.remove(key)
                pendingResult.finish()
            }
        }
    }

    companion object {
        /** Tracks SMS timestamps currently being processed to prevent duplicate saves. */
        private val processingKeys = ConcurrentHashMap.newKeySet<Long>()
    }
}
