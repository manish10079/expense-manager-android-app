package com.mknlabs.expensetracker.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mknlabs.expensetracker.domain.repository.FcmTokenRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FCM entry point (notification plan §5.4). Handles two things:
 *
 * 1. **Token lifecycle** — when Firebase issues a new token (first launch,
 *    refresh, reinstall) it is registered in Firestore so Cloud Functions know
 *    which tokens to push to. [FcmTokenRepository] handles the write.
 *
 * 2. **Data-only messages** — the server sends `data` (never `notification`)
 *    payloads, so every push arrives here and is rendered through
 *    [NotificationHelper.showFcmNotification], keeping channels, formatting
 *    and analytics consistent with the app's local alerts.
 */
@AndroidEntryPoint
class ExpenseTrackerMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenRepository: FcmTokenRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            fcmTokenRepository.registerCurrentDeviceToken(token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        if (data.isEmpty()) return

        NotificationHelper.showFcmNotification(
            context = applicationContext,
            type = data["type"],
            title = data["title"],
            body = data["body"]
        )
    }
}
