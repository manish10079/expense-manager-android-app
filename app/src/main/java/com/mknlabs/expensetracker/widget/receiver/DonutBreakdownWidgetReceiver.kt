package com.mknlabs.expensetracker.widget.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.mknlabs.expensetracker.widget.donut.WidgetDonutPrefs
import com.mknlabs.expensetracker.widget.ui.DonutBreakdownWidget

class DonutBreakdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DonutBreakdownWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            WidgetDonutPrefs.delete(context, it)
            com.mknlabs.expensetracker.widget.WidgetMonthPrefs.delete(
                context,
                com.mknlabs.expensetracker.widget.WidgetMonthPrefs.TARGET_DONUT,
                it
            )
        }
        super.onDeleted(context, appWidgetIds)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}
