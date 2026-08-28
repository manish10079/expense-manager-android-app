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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.actions.WidgetStartRecordingCallback

/**
 * Idle state — pixel-perfect glassmorphic mic UI.
 *
 * Spec:
 *  - Waveform: #A275E3 at 35% opacity bars
 *  - Mic: 64dp circle, #9A51F5→#6B25C6 gradient (solid: #9A51F5)
 *  - Title: "Tap to speak", #FFFFFF, SemiBold, 16sp
 *  - Subtitle: "Add expense with voice", #A0A5C0, 12sp
 */
@Composable
internal fun WidgetIdleContent(
    todaySpendingText: String,
    currencySymbol: String
) {
    val textColor = ColorProvider(R.color.widget_text_primary)       // #FFFFFF
    val subtitleColor = ColorProvider(R.color.widget_text_subtitle)  // #A0A5C0
    val waveformColor = ColorProvider(R.color.widget_waveform)       // #59A275E3
    val micColor = ColorProvider(R.color.widget_mic_start)           // #9A51F5

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(actionRunCallback<WidgetStartRecordingCallback>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ── Waveform bars (decorative) ──
            Row(
                modifier = GlanceModifier.height(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 7 vertical bars with varying heights
                val barData = listOf(16, 28, 36, 40, 32, 24, 18)
                barData.forEachIndexed { index, h ->
                    Box(
                        modifier = GlanceModifier
                            .width(5.dp)
                            .height(h.dp)
                            .background(waveformColor)
                    ) {}
                    if (index < barData.lastIndex) {
                        Spacer(modifier = GlanceModifier.width(5.dp))
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // ── Mic button — 64dp circle (solid #9A51F5) ──
            Box(
                modifier = GlanceModifier
                    .size(64.dp)
                    .background(micColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎙",
                    style = TextStyle(fontSize = 28.sp)
                )
            }

            Spacer(modifier = GlanceModifier.height(14.dp))

            // ── Title: "Tap to speak" ──
            Text(
                text = "Tap to speak",
                style = TextStyle(
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // ── Subtitle: "Add expense with voice" ──
            Text(
                text = "Add expense with voice",
                style = TextStyle(
                    color = subtitleColor,
                    fontSize = 12.sp
                )
            )

            // ── Today's spending (if available) ──
            if (todaySpendingText.isNotBlank()) {
                Spacer(modifier = GlanceModifier.height(14.dp))
                Text(
                    text = todaySpendingText,
                    style = TextStyle(
                        color = subtitleColor,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
