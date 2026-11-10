package com.mknlabs.expensetracker.widget.ui

import android.appwidget.AppWidgetManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.WidgetMonthNavReceiver

@Composable
internal fun WidgetMonthNav(
    label: String,
    labelColor: Color,
    chipBg: Int,
    target: String,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WidgetMonthChevron(
            icon = R.drawable.widget_ic_chevron_left,
            chipBg = chipBg,
            enabled = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID,
            intent = WidgetMonthNavReceiver.intent(context, appWidgetId, target, -1)
        )
        Spacer(GlanceModifier.defaultWeight())
        Text(
            text = label.uppercase(java.util.Locale.US),
            style = TextStyle(
                color = ColorProvider(labelColor),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.defaultWeight())
        WidgetMonthChevron(
            icon = R.drawable.widget_ic_chevron_right,
            chipBg = chipBg,
            enabled = appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID,
            intent = WidgetMonthNavReceiver.intent(context, appWidgetId, target, 1)
        )
    }
}

@Composable
private fun WidgetMonthChevron(
    icon: Int,
    chipBg: Int,
    enabled: Boolean,
    intent: android.content.Intent
) {
    val base = GlanceModifier
        .size(36.dp)
        .background(ImageProvider(chipBg))
    Box(
        modifier = if (enabled) base.clickable(actionSendBroadcast(intent)) else base,
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = null,
            modifier = GlanceModifier.size(16.dp)
        )
    }
}
