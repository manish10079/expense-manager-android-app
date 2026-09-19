package com.mknlabs.expensetracker.sms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.mknlabs.expensetracker.data.constants.currencyMap
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.SmsLearningStore
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.NewSmsDetection
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.RecordSmsOutcome
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxRepository
import com.mknlabs.expensetracker.feature.smsinbox.domain.usecase.RecordDetectedSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
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
    lateinit var recordDetectedSms: RecordDetectedSmsUseCase

    @Inject
    lateinit var smsInboxRepository: SmsInboxRepository

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

                // Read user's selected currency for international SMS support.
                val appSettings = AppSettingsDataStore.getAppSettingsFlow(context).first()
                val currencySymbol = currencyMap[appSettings.currencyId]?.currencySymbol ?: "₹"

                // A multipart SMS arrives as one broadcast with several PDUs —
                // concatenate them so a split message is parsed exactly once
                // (never N notifications / N transactions for a single SMS).
                val sender = messages.firstOrNull()?.displayOriginatingAddress.orEmpty()
                val smsTimestamp = messages.firstOrNull()?.timestampMillis ?: 0L
                val fullBody = messages.joinToString("") {
                    it.displayMessageBody?.trim().orEmpty()
                }
                if (fullBody.isBlank()) return@launch

                // Carriers/OEMs can deliver SMS_RECEIVED_ACTION more than once for
                // the same PDU — never show a second notification for one SMS event.
                // Sweep stale keys (excluding the one just added) keeps the set bounded.
                if (smsTimestamp > 0) {
                    if (!notifiedKeys.add(smsTimestamp)) return@launch
                    val cutoff = System.currentTimeMillis() - NOTIFY_WINDOW_MS
                    notifiedKeys.removeIf { it != smsTimestamp && it < cutoff }
                }

                val parsed = SmsParser.parse(
                    body = fullBody,
                    sender = sender,
                    smsTimestamp = smsTimestamp,
                    userOverrides = userOverrides,
                    currencySymbol = currencySymbol
                ) ?: return@launch

                // Durable capture FIRST. The inbox row is the only copy of this
                // detection that survives a dismissed notification, a reboot, or the
                // user ignoring the shade entirely — so it is written before any
                // branch below is allowed to bail out. Returns a Duplicate outcome
                // (never a second row) when this message was already captured.
                val recordOutcome = recordDetectedSms(
                    NewSmsDetection(
                        sender = parsed.sender,
                        body = parsed.body,
                        amountMinor = parsed.amountMinor,
                        transactionTypeId = parsed.transactionTypeId,
                        categoryId = parsed.categoryId,
                        merchant = parsed.merchant,
                        confidence = parsed.confidence,
                        detectedAt = parsed.smsTimestamp,
                        notificationCreatedAt = System.currentTimeMillis()
                    )
                )

                // The amount and timestamp are already accounted for, so there is
                // nothing left for the user to decide.
                if (smsRepository.isDuplicate(parsed)) return@launch

                val detectionId = when (recordOutcome) {
                    is RecordSmsOutcome.Recorded -> recordOutcome.detection.id
                    is RecordSmsOutcome.Duplicate -> recordOutcome.existing.id
                }

                // The shade ID is derived ONCE and stored on the row before the card is
                // posted. Everything else follows from that pairing: the card's
                // Add/Ignore/Edit actions resolve the row by id, tapping it opens that
                // row, and the in-app paths know which card to clear. Deriving the id
                // twice would drift whenever the SMS carries no usable timestamp.
                val notificationId = SmsNotificationManager.notificationIdFor(parsed.smsTimestamp)
                smsInboxRepository.attachNotification(detectionId, notificationId)

                SmsNotificationManager.showImportNotification(
                    context.applicationContext,
                    parsed,
                    detectionId,
                    notificationId
                )
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

    companion object {
        /** Re-notify window for one SMS event; entries older than this are pruned. */
        private const val NOTIFY_WINDOW_MS = 10L * 60L * 1000L

        /** SMS timestamps already notified in this process — duplicate deliveries are dropped. */
        private val notifiedKeys = ConcurrentHashMap.newKeySet<Long>()
    }
}
