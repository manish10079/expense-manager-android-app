package com.mknlabs.expensetracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.firebase.firestore.FirebaseFirestore
import com.mknlabs.expensetracker.data.local.appSettingsDataStore
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import com.mknlabs.expensetracker.domain.repository.FeedbackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val configurationRepository: ConfigurationRepository
) : FeedbackRepository {

    private object Keys {
        val lastFeedbackTimestamp = longPreferencesKey("last_feedback_timestamp")
    }

    override suspend fun submitFeedback(userId: String, email: String, feedback: String): Result<Unit> {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // 1. Submit to Firestore
                val feedbackData = hashMapOf(
                    "userId" to userId,
                    "email" to email,
                    "feedback" to feedback,
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                firestore.collection("feedbacks").add(feedbackData).await()

                // 2. Submit to Google Sheets via Webhook URL fetched from Firebase Remote Config
                // The URL is never hardcoded in source code - it lives only in Firebase Remote Config
                val webhookUrl = configurationRepository.googleSheetsFeedbackUrl.value
                if (webhookUrl.isNotBlank()) {
                    sendToGoogleSheets(webhookUrl, userId, email, feedback)
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun sendToGoogleSheets(webhookUrl: String, userId: String, email: String, feedback: String) {
        try {
            val url = java.net.URL(webhookUrl)
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; utf-8")
            conn.doOutput = true

            val escapedFeedback = feedback
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")

            val jsonPayload = """
                {
                    "userId": "$userId",
                    "email": "$email",
                    "feedback": "$escapedFeedback"
                }
            """.trimIndent()

            conn.outputStream.use { os ->
                val input = jsonPayload.toByteArray(charset("utf-8"))
                os.write(input, 0, input.size)
            }

            val responseCode = conn.responseCode
            // Follow redirect since Apps Script web apps return a 302 redirect
            if (responseCode == java.net.HttpURLConnection.HTTP_MOVED_TEMP || responseCode == java.net.HttpURLConnection.HTTP_MOVED_PERM) {
                val redirectUrl = conn.getHeaderField("Location")
                if (redirectUrl != null) {
                    val redirectConn = java.net.URL(redirectUrl).openConnection() as java.net.HttpURLConnection
                    redirectConn.requestMethod = "GET"
                    redirectConn.responseCode
                }
            }
        } catch (e: Exception) {
            // Silently ignore Sheets sync errors so the primary Firestore submission is unaffected
        }
    }

    override fun getLastFeedbackTime(): Flow<Long> {
        return context.appSettingsDataStore.data.map { preferences ->
            preferences[Keys.lastFeedbackTimestamp] ?: 0L
        }
    }

    override suspend fun saveLastFeedbackTime(timestamp: Long) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.lastFeedbackTimestamp] = timestamp
        }
    }
}
