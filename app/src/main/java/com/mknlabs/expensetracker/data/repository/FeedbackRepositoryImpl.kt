package com.mknlabs.expensetracker.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.mknlabs.expensetracker.data.local.appSettingsDataStore
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import com.mknlabs.expensetracker.domain.repository.FeedbackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configurationRepository: ConfigurationRepository
) : FeedbackRepository {

    companion object {
        private const val TAG = "FeedbackRepository"
        private const val DEFAULT_GOOGLE_SHEETS_FEEDBACK_URL =
            "https://script.google.com/macros/s/AKfycbzkGCpJ-So3bHesY2UjfYco5ISzRpIxn0B-bt-7L72Cn056euzkWdhDt9IFE-YHxT3T/exec"
        private const val CONNECT_TIMEOUT_MS = 10000
        private const val READ_TIMEOUT_MS = 10000
    }

    private object Keys {
        val lastFeedbackTimestamp = longPreferencesKey("last_feedback_timestamp")
    }

    override suspend fun submitFeedback(userId: String, email: String, feedback: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val configuredUrl = configurationRepository.googleSheetsFeedbackUrl.value.trim()
                val webhookUrl = when {
                    configuredUrl.isNotBlank() && !configuredUrl.contains("AKfycbxZhIQ") -> configuredUrl
                    else -> DEFAULT_GOOGLE_SHEETS_FEEDBACK_URL
                }

                if (webhookUrl.isBlank()) {
                    return@withContext Result.failure(IllegalStateException("Feedback webhook URL is not configured"))
                }

                Log.d(TAG, "Submitting feedback to Google Sheets: $webhookUrl")
                sendToGoogleSheets(webhookUrl, userId, email, feedback)
                Log.d(TAG, "Feedback submitted successfully to Google Sheets")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to submit feedback to Google Sheets", e)
                Result.failure(e)
            }
        }
    }

    private fun sendToGoogleSheets(webhookUrl: String, userId: String, email: String, feedback: String) {
        val cleanUrl = webhookUrl.trim()
        val url = URL(cleanUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = CONNECT_TIMEOUT_MS
        conn.readTimeout = READ_TIMEOUT_MS
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
        val responseBody = if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
            val redirectUrl = conn.getHeaderField("Location")
                ?: throw IOException("Google Sheets returned HTTP $responseCode without Location header")
            val redirectConn = URL(redirectUrl).openConnection() as HttpURLConnection
            redirectConn.connectTimeout = CONNECT_TIMEOUT_MS
            redirectConn.readTimeout = READ_TIMEOUT_MS
            redirectConn.requestMethod = "GET"
            val finalCode = redirectConn.responseCode
            val body = redirectConn.inputStream.bufferedReader().use { it.readText() }
            if (finalCode !in 200..299) {
                throw IOException("Google Sheets returned HTTP $finalCode on redirect: $body")
            }
            body
        } else if (responseCode in 200..299) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            throw IOException("Google Sheets returned HTTP $responseCode")
        }

        Log.d(TAG, "Google Sheets response: $responseBody")
        if (responseBody.contains("\"status\":\"error\"") || responseBody.contains("Sorry, the file you have requested does not exist")) {
            throw IOException("Google Sheets submission failed: $responseBody")
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
