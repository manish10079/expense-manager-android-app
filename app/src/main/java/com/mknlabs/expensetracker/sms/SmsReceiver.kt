package com.mknlabs.expensetracker.sms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.mknlabs.expensetracker.data.local.SmsLearningStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Listens for incoming SMS (plan §5). Deliberately thin — no parsing logic
 * lives here (plan §12): it delegates to [SmsParser], dedups via [SmsRepository],
 * and shows the notification via [SmsNotificationManager].
 *
 * No-ops when RECEIVE_SMS was denied, so the feature simply stays off (plan D5).
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsRepository: SmsRepository

    @Inject
    lateinit var smsLearningStore: SmsLearningStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (!hasSmsPermission(context)) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                // One read per broadcast: learning overrides (plan §10) shape
                // the category suggestion before the static rules kick in.
                val userOverrides = smsLearningStore.observeOverrides().first()
                for (message in messages) {
                    val body = message.displayMessageBody?.trim().orEmpty()
                    if (body.isBlank()) continue

                    val parsed = SmsParser.parse(
                        body = body,
                        sender = message.displayOriginatingAddress.orEmpty(),
                        smsTimestamp = message.timestampMillis,
                        userOverrides = userOverrides
                    ) ?: continue

                    if (smsRepository.isDuplicate(parsed)) continue
                    SmsNotificationManager.showImportNotification(context.applicationContext, parsed)
                }
            } catch (e: Exception) {
                android.util.Log.w("SmsReceiver", "Failed to process SMS broadcast", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
