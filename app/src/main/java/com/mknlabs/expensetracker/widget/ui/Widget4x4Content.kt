package com.mknlabs.expensetracker.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
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
import com.mknlabs.expensetracker.widget.model.WidgetRecentItem
import com.mknlabs.expensetracker.widget.model.WidgetUiState
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun Widget4x4Content(
    uiState: WidgetUiState,
    forceDark: Boolean? = null
) {
    val dark = widgetIsNightMode(forceDark)
    val colors = if (dark) Widget2x2Colors.Dark else Widget2x2Colors.Light
    val symbol = uiState.currencySymbol
    val monthLong = monthLongLabel()
    val income = uiState.monthIncomeMinor
    val expense = uiState.monthExpenseMinor
    val net = income - expense
    val netPrefix = if (net >= 0) "+" else "-"
    val cap = uiState.monthBudgetCapMinor
    val used = if (cap > 0) (expense.toFloat() / cap.toFloat()).coerceIn(0f, 1f) else 0f
    val usedPct = (used * 100f).toInt()
    val openApp = Intent().setClass(LocalContext.current, MainActivity::class.java)
    val cardBg = if (dark) R.drawable.widget_2x2_bg_dark else R.drawable.widget_2x2_bg_light
    val chipBg = if (dark) R.drawable.widget_2x2_chip_dark else R.drawable.widget_2x2_chip_light
    val recent = uiState.recentItems.take(2)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(cardBg))
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = "$monthLong Overview",
                style = TextStyle(
                    color = ColorProvider(colors.title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(6.dp))
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(chipBg))
                    .padding(8.dp)
            ) {
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Widget4x4Stat(
                        label = "Income",
                        value = "+${formatWidgetAmount(symbol, income)}",
                        valueColor = colors.income,
                        mutedColor = colors.muted,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Widget4x4Stat(
                        label = "Expense",
                        value = "-${formatWidgetAmount(symbol, expense)}",
                        valueColor = colors.expense,
                        mutedColor = colors.muted,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Widget4x4Stat(
                        label = "Net Saved",
                        value = "$netPrefix${formatWidgetAmount(symbol, abs(net))}",
                        valueColor = colors.brand,
                        mutedColor = colors.muted,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
                if (cap > 0) {
                    Spacer(GlanceModifier.height(6.dp))
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Cap ($usedPct% used)",
                            style = TextStyle(
                                color = ColorProvider(colors.muted),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Text(
                            text = "${formatWidgetAmount(symbol, expense)} / ${formatWidgetAmount(symbol, cap)}",
                            style = TextStyle(
                                color = ColorProvider(colors.title),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(GlanceModifier.height(4.dp))
                    val trackWidth = (LocalSize.current.width - 32.dp).coerceAtLeast(8.dp)
                    val fillWidth = (trackWidth * used).coerceAtLeast(if (used > 0f) 8.dp else 0.dp)
                    val trackBg = if (dark) {
                        R.drawable.widget_progress_track_dark
                    } else {
                        R.drawable.widget_progress_track_light
                    }
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(ImageProvider(trackBg)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (used > 0f) {
                            Box(
                                modifier = GlanceModifier
                                    .width(fillWidth)
                                    .height(6.dp)
                                    .background(ImageProvider(R.drawable.widget_progress_gradient))
                            ) {}
                        }
                    }
                }
            }

            Spacer(GlanceModifier.height(6.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Widget4x4Action(
                    title = "Mic Log",
                    icon = R.drawable.widget_ic_mic_outline,
                    chipBg = chipBg,
                    titleColor = colors.title,
                    modifier = GlanceModifier.defaultWeight()
                        .clickable(actionStartActivity(openApp))
                )
                Spacer(GlanceModifier.width(6.dp))
                Widget4x4Action(
                    title = "Scan UPI",
                    icon = R.drawable.widget_ic_camera_on_brand,
                    chipBg = chipBg,
                    titleColor = colors.title,
                    modifier = GlanceModifier.defaultWeight()
                        .clickable(actionStartActivity(openApp))
                )
            }

            Spacer(GlanceModifier.height(6.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = "Refresh",
                    style = TextStyle(
                        color = ColorProvider(colors.brand),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.clickable(actionStartActivity(openApp))
                )
            }
            if (recent.isEmpty()) {
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = "No recent logs",
                    style = TextStyle(
                        color = ColorProvider(colors.muted),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            } else {
                recent.forEach { item ->
                    Spacer(GlanceModifier.height(4.dp))
                    Widget4x4RecentRow(item, colors, chipBg)
                }
            }
        }
    }
}

@Composable
private fun Widget4x4Stat(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    mutedColor: androidx.compose.ui.graphics.Color,
    modifier: GlanceModifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
        Text(
            text = label.uppercase(Locale.US),
            style = TextStyle(
                color = ColorProvider(mutedColor),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(valueColor),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun Widget4x4Action(
    title: String,
    icon: Int,
    chipBg: Int,
    titleColor: androidx.compose.ui.graphics.Color,
    modifier: GlanceModifier
) {
    Row(
        modifier = modifier
            .background(ImageProvider(chipBg))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .background(ImageProvider(R.drawable.widget_2x2_log)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = title,
                modifier = GlanceModifier.size(12.dp)
            )
        }
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(titleColor),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun Widget4x4RecentRow(
    item: WidgetRecentItem,
    colors: Widget2x2Colors,
    chipBg: Int
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ImageProvider(chipBg))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = item.title,
                style = TextStyle(
                    color = ColorProvider(colors.title),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = item.subtitle,
                style = TextStyle(
                    color = ColorProvider(colors.muted),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
        Text(
            text = item.amountText,
            style = TextStyle(
                color = ColorProvider(if (item.isIncome) colors.income else colors.title),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

private fun monthLongLabel(): String {
    val cal = Calendar.getInstance()
    return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) ?: ""
}
