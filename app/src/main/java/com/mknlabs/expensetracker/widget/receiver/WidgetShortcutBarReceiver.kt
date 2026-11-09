package com.mknlabs.expensetracker.widget.receiver

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.mknlabs.expensetracker.widget.ui.WidgetShortcutBarWidget

internal class WidgetShortcutBarReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WidgetShortcutBarWidget()
}
