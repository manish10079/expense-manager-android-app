package com.mknlabs.expensetracker.widget.actions

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.mknlabs.expensetracker.widget.service.VoiceCaptureService
import com.mknlabs.expensetracker.widget.voice.WidgetVoiceSessionStore

internal class WidgetRetryCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d(TAG, "Retry called")
        val sessionStore = WidgetVoiceSessionStore.getInstance(context)
        sessionStore.clearSession()
        val sessionId = sessionStore.startNewSession()
        VoiceCaptureService.start(context, sessionId)
    }
    companion object { private const val TAG = "WidgetRetryCb" }
}
