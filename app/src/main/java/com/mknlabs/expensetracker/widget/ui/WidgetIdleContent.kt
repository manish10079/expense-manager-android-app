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
import com.mknlabs.expensetracker.widget.actions.WidgetStartRecordingCallback

@Composable
internal fun WidgetIdleContent(
    todaySpendingText: String,
    currencySymbol: String
) {
    val surfaceColor = ColorProvider(R.color.widget_surface)
    val primaryColor = ColorProvider(R.color.widget_primary)
    val textColor = ColorProvider(R.color.widget_text)
    val subtitleColor = ColorProvider(R.color.widget_subtitle)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(16.dp)
            .clickable(actionRunCallback<WidgetStartRecordingCallback>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (todaySpendingText.isNotBlank()) {
                Text(
                    text = todaySpendingText,
                    style = TextStyle(color = subtitleColor, fontSize = 11.sp)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
            }

            // Mic button — purple primary matching app theme
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎙",
                    style = TextStyle(fontSize = 28.sp, color = ColorProvider(android.R.color.white))
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            Text(
                text = "Tap to Speak",
                style = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
        }
    }
}
