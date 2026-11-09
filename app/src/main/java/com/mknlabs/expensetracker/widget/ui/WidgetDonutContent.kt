package com.mknlabs.expensetracker.widget.ui

import android.content.Intent
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
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
import com.mknlabs.expensetracker.MainActivity
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.donut.WidgetDonutBitmap
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownKind
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownPanel
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownSlice
import java.util.Calendar
import java.util.Locale

@Composable
internal fun WidgetDonutContent(
    panels: List<WidgetBreakdownPanel>,
    forceDark: Boolean? = null
) {
    val dark = widgetIsNightMode(forceDark)
    val colors = if (dark) Widget2x2Colors.Dark else Widget2x2Colors.Light
    val cardBg = if (dark) R.drawable.widget_2x2_bg_dark else R.drawable.widget_2x2_bg_light
    val chipBg = if (dark) R.drawable.widget_2x2_chip_dark else R.drawable.widget_2x2_chip_light
    val openApp = Intent().setClass(LocalContext.current, MainActivity::class.java)
    val month = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) ?: ""
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val density = LocalContext.current.resources.displayMetrics.density
    val donutPx = (80 * density).toInt().coerceAtLeast(120)
    val track = if (dark) AndroidColor.parseColor("#1E293B") else AndroidColor.parseColor("#E2E8F0")

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(cardBg))
            .clickable(actionStartActivity(openApp))
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = "Analytics Breakdown",
                style = TextStyle(
                    color = ColorProvider(colors.title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "$month $year",
                style = TextStyle(
                    color = ColorProvider(colors.muted),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            panels.take(2).forEach { panel ->
                Spacer(GlanceModifier.height(12.dp))
                WidgetDonutPanelRow(
                    panel = panel,
                    colors = colors,
                    chipBg = chipBg,
                    donutPx = donutPx,
                    track = track
                )
            }
        }
    }
}

@Composable
private fun WidgetDonutPanelRow(
    panel: WidgetBreakdownPanel,
    colors: Widget2x2Colors,
    chipBg: Int,
    donutPx: Int,
    track: Int
) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ImageProvider(chipBg))
            .padding(8.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = panel.title,
                style = TextStyle(
                    color = ColorProvider(colors.title),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = panel.trailing,
                style = TextStyle(
                    color = ColorProvider(colors.muted),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(GlanceModifier.height(6.dp))
        if (panel.kind == WidgetBreakdownKind.TOP10) {
            panel.slices.take(5).forEach { slice ->
                WidgetDonutLegendLine(slice, colors, showAmount = true)
            }
        } else {
            val top = panel.slices.maxByOrNull { it.percent }
            val bitmap = WidgetDonutBitmap.draw(panel.slices, donutPx, track)
            Box(
                modifier = GlanceModifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = panel.title,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                        Text(
                            text = "${top?.percent ?: 0}%",
                            style = TextStyle(
                                color = ColorProvider(colors.title),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = top?.label.orEmpty(),
                            style = TextStyle(
                                color = ColorProvider(colors.muted),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
            Spacer(GlanceModifier.height(8.dp))
            panel.slices.take(8).forEach { slice ->
                WidgetDonutLegendLine(slice, colors, showAmount = false)
            }
        }
    }
}

@Composable
private fun WidgetDonutLegendLine(
    slice: WidgetBreakdownSlice,
    colors: Widget2x2Colors,
    showAmount: Boolean
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(WidgetDonutBitmap.dot(slice.colorArgb, 24)),
            contentDescription = null,
            modifier = GlanceModifier.size(8.dp)
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            text = slice.label,
            style = TextStyle(
                color = ColorProvider(colors.muted),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = if (showAmount) slice.amountText else "${slice.percent}%",
            style = TextStyle(
                color = ColorProvider(colors.title),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.width(6.dp))
    }
}
