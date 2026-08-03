package com.mknlabs.expensetracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.sms.SmsNotificationManager.toParsedSms
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles the notification's [Save] action — a true one-tap save without
 * opening the app (plan §8):
 *  - transaction type + suggested category from the detected SMS,
 *  - note stays empty (plan §14 Q2),
 *  - payment method = the user's configured default (plan §14 Q1).
 *
 * After saving, the notification is dismissed; Room flows refresh every screen
 * automatically the next time the app is opened.
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

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                // Re-check dedup: guards a double-tap racing the notification dismiss.
                if (smsRepository.isDuplicate(parsed)) {
                    SmsNotificationManager.cancel(context)
                    return@launch
                }

                val paymentTypeId = appPreferencesRepository
                    .observeAppSettings()
                    .first()
                    .defaultPaymentTypeId

                smsRepository.saveFromSms(parsed, paymentTypeId = paymentTypeId)
                SmsNotificationManager.cancel(context)
            } catch (e: Exception) {
                // Keep the notification visible so the user can retry via Change/Open.
                android.util.Log.w("SmsActionReceiver", "Failed to save SMS transaction", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
