package com.mknlabs.expensetracker.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
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

/**
 * Widget processing state: progress indicator + "Analyzing transaction..."
 */
@Composable
internal fun WidgetProcessingContent() {
    val surfaceColor = ColorProvider(R.color.widget_surface)
    val primaryColor = ColorProvider(R.color.widget_primary)
    val textColor = ColorProvider(R.color.widget_text)

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
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⏳", style = TextStyle(fontSize = 24.sp))
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            Text(
                text = "Analyzing...",
                style = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "Processing your transaction",
                style = TextStyle(color = textColor, fontSize = 11.sp)
            )
        }
    }
}
