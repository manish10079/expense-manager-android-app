package com.mknlabs.expensetracker.widget.actions

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.widget.di.WidgetEntryPoint
import com.mknlabs.expensetracker.widget.model.WidgetState
import com.mknlabs.expensetracker.widget.repository.WidgetRepositoryImpl
import com.mknlabs.expensetracker.widget.voice.WidgetVoiceSessionStore

internal class WidgetSaveCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val sessionStore = WidgetVoiceSessionStore.getInstance(context)
        val parsed = sessionStore.getParsedTransaction()

        if (parsed == null) {
            Log.e(TAG, "No parsed transaction to save")
            sessionStore.setError(com.mknlabs.expensetracker.R.string.msg_voice_error_empty_input)
            return
        }

        sessionStore.setState(WidgetState.Saving)

        try {
            val entryPoint = WidgetEntryPoint.get(context)
            val repository = WidgetRepositoryImpl(
                database = ExpenseTrackerDatabase.getInstance(context),
                transactionRepository = entryPoint.transactionRepository()
            )
            val result = repository.saveTransaction(parsed)
            if (result.isSuccess) {
                Log.d(TAG, "Transaction saved successfully")
                sessionStore.clearSession()
            } else {
                Log.e(TAG, "Save failed", result.exceptionOrNull())
                sessionStore.setError(com.mknlabs.expensetracker.R.string.msg_voice_error_audio)
            }
            entryPoint.widgetUpdater().refreshAll(context)
        } catch (e: Exception) {
            Log.e(TAG, "Save error", e)
            sessionStore.setError(com.mknlabs.expensetracker.R.string.msg_voice_error_audio)
        }
    }
    companion object { private const val TAG = "WidgetSaveCb" }
}
