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
 * Spec:
 *  - Mic: solid purple circle
 *  - "Listening..." #FFFFFF, 16sp bold
 *  - "Speak your transaction" #A0A5C0, 12sp
 *  - Cancel: #211218 bg, #EF4444 text, bold 14sp
 */
@Composable
internal fun WidgetListeningContent() {
    val textColor = ColorProvider(R.color.widget_text_primary)
    val subtitleColor = ColorProvider(R.color.widget_text_subtitle)
    val micColor = ColorProvider(R.color.widget_mic_start)
    val waveformColor = ColorProvider(R.color.widget_waveform)
    val cancelTextColor = ColorProvider(R.color.widget_cancel_text)
    val cancelBg = ColorProvider(R.color.widget_cancel_bg)

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
                            .background(waveformColor)
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
                    .background(micColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎙", style = TextStyle(fontSize = 28.sp))
            }

            Spacer(modifier = GlanceModifier.height(14.dp))

            Text(
                text = "Listening...",
                style = TextStyle(
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "Speak your transaction",
                style = TextStyle(color = subtitleColor, fontSize = 12.sp)
            )

            Spacer(modifier = GlanceModifier.height(16.dp))

            // ── Cancel button (pill) ──
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(cancelBg)
                    .clickable(actionRunCallback<WidgetCancelCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕  Cancel",
                    style = TextStyle(
                        color = cancelTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
