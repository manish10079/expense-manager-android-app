package com.mknlabs.expensetracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.mknlabs.expensetracker.widget.ui.DonutBreakdownWidget
import com.mknlabs.expensetracker.widget.ui.ExpenseHomeWidget

internal suspend fun shiftWidgetMonth(
    context: Context,
    glanceId: GlanceId,
    delta: Int,
    target: String
) {
    val app = context.applicationContext
    val appWidgetId = GlanceAppWidgetManager(app).getAppWidgetId(glanceId)
    if (delta > 0 && WidgetMonthPrefs.offset(app, target, appWidgetId) >= 0) {
        return
    }
    WidgetMonthPrefs.add(app, target, appWidgetId, delta)
    when (target) {
        WidgetMonthPrefs.TARGET_SUMMARY -> ExpenseHomeWidget().updateAll(app)
        WidgetMonthPrefs.TARGET_DONUT -> DonutBreakdownWidget().updateAll(app)
    }
}

class SummaryMonthPrevAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        shiftWidgetMonth(context, glanceId, -1, WidgetMonthPrefs.TARGET_SUMMARY)
    }
}

class SummaryMonthNextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        shiftWidgetMonth(context, glanceId, 1, WidgetMonthPrefs.TARGET_SUMMARY)
    }
}

class DonutMonthPrevAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        shiftWidgetMonth(context, glanceId, -1, WidgetMonthPrefs.TARGET_DONUT)
    }
}

class DonutMonthNextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: androidx.glance.action.ActionParameters) {
        shiftWidgetMonth(context, glanceId, 1, WidgetMonthPrefs.TARGET_DONUT)
    }
}
