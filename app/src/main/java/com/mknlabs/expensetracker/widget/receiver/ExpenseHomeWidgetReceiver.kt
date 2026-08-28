package com.mknlabs.expensetracker.widget.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.mknlabs.expensetracker.widget.ui.ExpenseHomeWidget

/**
 * AppWidgetProvider for the Expense Home Widget.
 * Delegates to [ExpenseHomeWidget] Glance implementation.
 */
internal class ExpenseHomeWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = ExpenseHomeWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled — first widget instance added")
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled — last widget instance removed")
        super.onDisabled(context)
    }

    companion object {
        private const val TAG = "ExpenseHomeWidgetRcvr"
    }
}
