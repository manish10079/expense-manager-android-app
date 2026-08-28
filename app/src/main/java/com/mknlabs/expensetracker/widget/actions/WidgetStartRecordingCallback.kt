package com.mknlabs.expensetracker.widget.actions

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.mknlabs.expensetracker.widget.service.VoiceCaptureService
import com.mknlabs.expensetracker.widget.voice.WidgetVoiceSessionStore

internal class WidgetStartRecordingCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d(TAG, "Starting recording")
        val sessionStore = WidgetVoiceSessionStore.getInstance(context)
        val sessionId = sessionStore.startNewSession()
        VoiceCaptureService.start(context, sessionId)
    }
    companion object { private const val TAG = "WidgetStartRecCb" }
}
