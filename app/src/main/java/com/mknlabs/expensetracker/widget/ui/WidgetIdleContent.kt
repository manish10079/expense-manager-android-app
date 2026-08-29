package com.mknlabs.expensetracker.widget.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
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
import com.mknlabs.expensetracker.widget.actions.WidgetStartRecordingCallback

/**
 * Idle state — pixel-perfect glassmorphic mic UI.
 *
 * Uses ImageProvider(R.drawable.bg_mic_button) for gradient + glow.
 *
 * Spec:
 *  - Waveform: #A275E3 at 35% opacity bars
 *  - Mic: 64dp circle, #9A51F5→#6B25C6 gradient + radial glow
 *  - Title: "Tap to speak", #FFFFFF, SemiBold, 16sp
 *  - Subtitle: "Add expense with voice", #A0A5C0, 12sp
 */
@Composable
internal fun WidgetIdleContent(
    todaySpendingText: String,
    currencySymbol: String
) {
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
            // ── Top Section (Voice Recording Area) ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                // Audio Waveform Background: Symmetrical vertical soundwave bars in semi-transparent light purple (#A275E3, 35% opacity) placed behind the mic button.
                // Row container cannot have more than 10 elements, so we use padding instead of Spacers and 7 bars.
                Row(
                    modifier = GlanceModifier.height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val barData = listOf(16, 28, 40, 48, 40, 28, 16)
                    barData.forEach { h ->
                        Box(
                            modifier = GlanceModifier
                                .width(4.dp)
                                .height(h.dp)
                                .padding(horizontal = 3.dp)
                                .background(ColorProvider(Color(0x59A275E3)))
                        ) {}
                    }
                }

                // Mic Button: 64dp x 64dp circle with gradient + glow + solid white mic icon
                Box(
                    modifier = GlanceModifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.bg_mic_button),
                        contentDescription = null,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                    Image(
                        provider = ImageProvider(R.drawable.widget_ic_mic),
                        contentDescription = "Microphone",
                        modifier = GlanceModifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(14.dp))

            // ── Title: "Tap to speak" ──
            Text(
                text = "Tap to speak",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            // ── Subtitle: "Add expense with voice" ──
            Text(
                text = "Add expense with voice",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFA0A5C0)),
                    fontSize = 12.sp
                )
            )

            // ── Today's spending (if available) ──
            if (todaySpendingText.isNotBlank()) {
                Spacer(modifier = GlanceModifier.height(14.dp))
                Text(
                    text = todaySpendingText,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFA0A5C0)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
