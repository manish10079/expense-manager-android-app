package com.mknlabs.expensetracker.widget.actions

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.mknlabs.expensetracker.widget.di.WidgetEntryPoint
import com.mknlabs.expensetracker.widget.voice.WidgetVoiceSessionStore

internal class WidgetCancelCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d(TAG, "Cancel called")
        WidgetVoiceSessionStore.getInstance(context).clearSession()
        WidgetEntryPoint.get(context).widgetUpdater().refreshAll(context)
    }
    companion object { private const val TAG = "WidgetCancelCb" }
}
