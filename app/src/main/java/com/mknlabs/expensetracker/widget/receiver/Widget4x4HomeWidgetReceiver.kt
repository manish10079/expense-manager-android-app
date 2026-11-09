package com.mknlabs.expensetracker.widget.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.mknlabs.expensetracker.widget.ui.Widget4x4HomeWidget

internal class Widget4x4HomeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = Widget4x4HomeWidget()
}
