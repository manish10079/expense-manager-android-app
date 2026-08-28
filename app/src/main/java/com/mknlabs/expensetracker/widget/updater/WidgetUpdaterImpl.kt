package com.mknlabs.expensetracker.widget.updater

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import com.mknlabs.expensetracker.widget.ui.ExpenseHomeWidget

/**
 * Abstraction for refreshing widget instances.
 *
 * Every widget feature uses this class to trigger updates.
 */
internal interface WidgetUpdater {
    /** Refresh all instances of the Expense Home Widget. */
    suspend fun refreshAll(context: Context)
}

internal class WidgetUpdaterImpl : WidgetUpdater {

    override suspend fun refreshAll(context: Context) {
        ExpenseHomeWidget().updateAll(context)
    }
}
