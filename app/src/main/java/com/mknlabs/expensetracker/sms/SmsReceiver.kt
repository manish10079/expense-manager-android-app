package com.mknlabs.expensetracker.sms

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.mknlabs.expensetracker.data.local.SmsLearningStore
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.utils.USAGE_RANKING_WINDOW_MS
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
    lateinit var smsLearningStore: SmsLearningStore

    @Inject
    lateinit var categoryRepository: CategoryRepository

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
                    userOverrides = userOverrides
                ) ?: return@launch

                if (smsRepository.isDuplicate(parsed)) return@launch

                // Fetch top-3 frequently used categories for the detected type,
                // ranked by usage in the LAST 60 DAYS (matches the Add
                // Transaction pickers). Falls back to sort_order-ranked defaults
                // if the user has no recent history.
                val frequentCategories = categoryRepository.getFrequentlyUsedCategories(
                    transactionTypeId = parsed.transactionTypeId,
                    limit = 3,
                    sinceMillis = System.currentTimeMillis() - USAGE_RANKING_WINDOW_MS
                )

                SmsNotificationManager.showImportNotification(
                    context.applicationContext,
                    parsed,
                    frequentCategories
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
