package com.mknlabs.expensetracker.widget.ui

import androidx.compose.ui.graphics.Color
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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * Processing state — pixel-perfect glassmorphic UI.
 *
 * Spec:
 *  - Indicator: solid purple circle
 *  - "Analyzing..." #FFFFFF, 16sp bold
 *  - "Processing your transaction" #A0A5C0, 12sp
 */
@Composable
internal fun WidgetProcessingContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
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
                    .size(64.dp)
                    .background(ColorProvider(Color(0xFFA855F7))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⏳", style = TextStyle(fontSize = 28.sp))
            }

            Spacer(modifier = GlanceModifier.height(14.dp))

            Text(
                text = "Analyzing...",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "Processing your transaction",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFA0A5C0)),
                    fontSize = 12.sp
                )
            )
        }
    }
}
