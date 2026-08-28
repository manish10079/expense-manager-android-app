package com.mknlabs.expensetracker.widget.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.mknlabs.expensetracker.MainActivity
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.constants.WidgetConstants
import com.mknlabs.expensetracker.widget.di.WidgetEntryPoint
import com.mknlabs.expensetracker.widget.model.WidgetState
import com.mknlabs.expensetracker.widget.voice.VoiceRecognitionManager
import com.mknlabs.expensetracker.widget.voice.WidgetParseResult
import com.mknlabs.expensetracker.widget.voice.WidgetVoiceProcessor
import com.mknlabs.expensetracker.widget.voice.WidgetVoiceSessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Foreground service for capturing voice input from the widget.
 *
 * Responsibilities:
 * - Start recording immediately after widget tap
 * - Show recording notification
 * - Use VoiceRecognitionManager to listen
 * - Stop automatically when speech ends
 * - Clean up resources correctly
 * - Never leak SpeechRecognizer
 *
 * This service must NOT open MainActivity during normal flow.
 */
internal class VoiceCaptureService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var voiceRecognitionManager: VoiceRecognitionManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        createNotificationChannel()
        voiceRecognitionManager = VoiceRecognitionManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionId = intent?.getStringExtra(WidgetConstants.SESSION_ID_KEY)

        if (sessionId == null) {
            Log.e(TAG, "No session ID — stopping")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d(TAG, "Starting voice capture for session: $sessionId")

        startForeground(WidgetConstants.VOICE_NOTIFICATION_ID, buildRecordingNotification())

        val sessionStore = WidgetVoiceSessionStore.getInstance(this)
        sessionStore.setState(WidgetState.Listening)

        serviceScope.launch {
            try {
                val transcript = withContext(Dispatchers.Main) {
                    voiceRecognitionManager?.recognize(WidgetConstants.SPEECH_TIMEOUT_MS)
                }

                if (transcript.isNullOrBlank()) {
                    Log.w(TAG, "Empty transcript — setting error")
                    sessionStore.setError(R.string.msg_voice_error_empty_input)
                    updateWidget()
                    stopSelf()
                    return@launch
                }

                Log.d(TAG, "Got transcript: '$transcript'")

                sessionStore.setTranscript(transcript)
                sessionStore.setState(WidgetState.Processing)
                updateWidget()

                val entryPoint = WidgetEntryPoint.get(this@VoiceCaptureService)
                val processor = WidgetVoiceProcessor(entryPoint.voiceParserRepository())

                when (val result = processor.process(transcript)) {
                    is WidgetParseResult.Success -> {
                        Log.d(TAG, "Parsed successfully — amount=${result.parsedTransaction.amountMinor}")
                        sessionStore.setParsedTransaction(result.parsedTransaction)
                    }
                    is WidgetParseResult.Failure -> {
                        Log.w(TAG, "Parse failed — errorResId=${result.errorMessageResId}")
                        sessionStore.setError(result.errorMessageResId)
                    }
                }

                updateWidget()

            } catch (e: Exception) {
                Log.e(TAG, "Voice capture error", e)
                sessionStore.setError(R.string.msg_voice_error_audio)
                updateWidget()
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        voiceRecognitionManager?.destroy()
        voiceRecognitionManager = null
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun updateWidget() {
        val entryPoint = WidgetEntryPoint.get(this)
        entryPoint.widgetUpdater().refreshAll(this)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            WidgetConstants.VOICE_CHANNEL_ID,
            getString(R.string.widget_voice_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.widget_voice_channel_desc)
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildRecordingNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(this, WidgetConstants.VOICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.widget_notification_recording_title))
            .setContentText(getString(R.string.widget_notification_recording_text))
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "VoiceCaptureSvc"

        fun start(context: Context, sessionId: String) {
            val intent = Intent(context, VoiceCaptureService::class.java).apply {
                putExtra(WidgetConstants.SESSION_ID_KEY, sessionId)
            }
            context.startForegroundService(intent)
        }
    }
}
