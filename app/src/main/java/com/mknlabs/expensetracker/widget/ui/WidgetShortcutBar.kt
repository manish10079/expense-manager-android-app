package com.mknlabs.expensetracker.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
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
import com.mknlabs.expensetracker.widget.overlay.OverlayUiMode
import com.mknlabs.expensetracker.widget.overlay.widgetOverlayAction

class WidgetShortcutBarWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetShortcutBarContent()
            }
        }
    }

    companion object {
        val SIZE = DpSize(250.dp, 48.dp)
    }
}

@Composable
internal fun WidgetShortcutBarContent(forceDark: Boolean? = null) {
    val dark = widgetIsNightMode(forceDark)
    val colors = if (dark) Widget2x2Colors.Dark else Widget2x2Colors.Light
    val cardBg = if (dark) R.drawable.widget_2x2_bg_dark else R.drawable.widget_2x2_bg_light
    val chipBg = if (dark) R.drawable.widget_2x2_chip_dark else R.drawable.widget_2x2_chip_light
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(cardBg))
            .padding(8.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth().height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShortcutChip(
                label = "Type",
                modifier = GlanceModifier.defaultWeight().clickable(widgetOverlayAction(LocalContext.current, OverlayUiMode.TEXT)),
                chipBg = chipBg,
                color = colors.title
            )
            Spacer(GlanceModifier.width(6.dp))
            ShortcutChip(
                label = "Talk",
                modifier = GlanceModifier.defaultWeight().clickable(widgetOverlayAction(LocalContext.current, OverlayUiMode.VOICE)),
                chipBg = chipBg,
                color = colors.title
            )
            Spacer(GlanceModifier.width(6.dp))
            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .background(ImageProvider(R.drawable.widget_2x2_log))
                    .clickable(widgetOverlayAction(LocalContext.current, OverlayUiMode.SCAN)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.widget_ic_camera_on_brand),
                    contentDescription = "Scan",
                    modifier = GlanceModifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ShortcutChip(
    label: String,
    modifier: GlanceModifier,
    chipBg: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(ImageProvider(chipBg)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(color),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
