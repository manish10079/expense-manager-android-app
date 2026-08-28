package com.mknlabs.expensetracker.widget.ui

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.actions.WidgetCancelCallback

/**
 * Listening state — pixel-perfect glassmorphic UI.
 *
 * Uses ImageProvider(R.drawable.bg_cancel_button) for cancel button with border.
 *
 * Spec:
 *  - Mic: solid purple circle
 *  - "Listening..." #FFFFFF, 16sp bold
 *  - "Speak your transaction" #A0A5C0, 12sp
 *  - Cancel: #211218 bg, #7F1D1D border, #EF4444 text, bold 14sp
 */
@Composable
internal fun WidgetListeningContent() {
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
            // ── Waveform bars ──
            Row(
                modifier = GlanceModifier.height(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val barData = listOf(14, 24, 30, 32, 26, 18, 12)
                barData.forEachIndexed { index, h ->
                    Box(
                        modifier = GlanceModifier
                            .width(4.dp)
                            .height(h.dp)
                            .background(ColorProvider(Color.parseColor("#59A275E3")))
                    ) {}
                    if (index < barData.lastIndex) {
                        Spacer(modifier = GlanceModifier.width(4.dp))
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // ── Mic indicator ──
            Box(
                modifier = GlanceModifier
                    .size(64.dp)
                    .background(ColorProvider(Color.parseColor("#9A51F5"))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎙", style = TextStyle(fontSize = 28.sp))
            }

            Spacer(modifier = GlanceModifier.height(14.dp))

            Text(
                text = "Listening...",
                style = TextStyle(
                    color = ColorProvider(Color.WHITE),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "Speak your transaction",
                style = TextStyle(
                    color = ColorProvider(Color.parseColor("#A0A5C0")),
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(16.dp))

            // ── Cancel button (ImageProvider drawable with border) ──
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(ImageProvider(R.drawable.bg_cancel_button))
                    .clickable(actionRunCallback<WidgetCancelCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕  Cancel",
                    style = TextStyle(
                        color = ColorProvider(Color.parseColor("#EF4444")),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
