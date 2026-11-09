package com.mknlabs.expensetracker.widget.updater

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.mknlabs.expensetracker.widget.ui.DonutBreakdownWidget
import com.mknlabs.expensetracker.widget.ui.ExpenseHomeWidget
import com.mknlabs.expensetracker.widget.ui.Widget4x2HomeWidget
import com.mknlabs.expensetracker.widget.ui.Widget4x4HomeWidget
import com.mknlabs.expensetracker.widget.ui.WidgetShortcutBarWidget

internal interface WidgetUpdater {
    suspend fun refreshAll(context: Context)
}

internal class WidgetUpdaterImpl : WidgetUpdater {

    override suspend fun refreshAll(context: Context) {
        val app = context.applicationContext
        ExpenseHomeWidget().updateAll(app)
        Widget4x2HomeWidget().updateAll(app)
        Widget4x4HomeWidget().updateAll(app)
        DonutBreakdownWidget().updateAll(app)
        WidgetShortcutBarWidget().updateAll(app)
    }
}
