package com.mknlabs.expensetracker.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.actions.WidgetCancelCallback

@Composable
internal fun WidgetListeningContent() {
    val surfaceColor = ColorProvider(R.color.widget_surface)
    val errorColor = ColorProvider(R.color.widget_error)
    val textColor = ColorProvider(R.color.widget_text)
    val subtitleColor = ColorProvider(R.color.widget_subtitle)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(errorColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔴", style = TextStyle(fontSize = 22.sp))
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            Text(
                text = "Listening...",
                style = TextStyle(color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = "Speak your transaction",
                style = TextStyle(color = subtitleColor, fontSize = 11.sp)
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(errorColor)
                    .clickable(actionRunCallback<WidgetCancelCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    style = TextStyle(color = ColorProvider(android.R.color.white), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
