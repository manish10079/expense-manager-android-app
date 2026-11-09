package com.mknlabs.expensetracker.widget.ui

import android.content.Intent
import android.content.res.Configuration
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
import com.mknlabs.expensetracker.widget.model.WidgetUiState
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun widgetIsNightMode(forceDark: Boolean? = null): Boolean {
    if (forceDark != null) return forceDark
    val uiMode = LocalContext.current.resources.configuration.uiMode
    return uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

@Composable
internal fun Widget2x2SummaryContent(
    uiState: WidgetUiState,
    forceDark: Boolean? = null
) {
    val dark = widgetIsNightMode(forceDark)
    val colors = if (dark) Widget2x2Colors.Dark else Widget2x2Colors.Light
    val symbol = uiState.currencySymbol
    val month = uiState.monthLabel.ifBlank { currentMonthSummaryLabel() }
    val income = uiState.monthIncomeMinor
    val expense = uiState.monthExpenseMinor
    val net = income - expense
    val netPrefix = if (net >= 0) "+" else "-"
    val openApp = Intent().setClass(LocalContext.current, MainActivity::class.java)

    val cardBg = if (dark) R.drawable.widget_2x2_bg_dark else R.drawable.widget_2x2_bg_light
    val chipBg = if (dark) R.drawable.widget_2x2_chip_dark else R.drawable.widget_2x2_chip_light
    val camIcon = if (dark) R.drawable.widget_ic_camera else R.drawable.widget_ic_camera_light

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(cardBg))
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Text(
                text = month.uppercase(Locale.US),
                style = TextStyle(
                    color = ColorProvider(colors.muted),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "Net Saved",
                style = TextStyle(
                    color = ColorProvider(colors.muted),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            Text(
                text = "$netPrefix${formatWidgetAmount(symbol, abs(net))}",
                style = TextStyle(
                    color = ColorProvider(colors.title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(6.dp))
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(chipBg))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Income",
                        style = TextStyle(
                            color = ColorProvider(colors.income),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = formatWidgetAmount(symbol, income),
                        style = TextStyle(
                            color = ColorProvider(colors.title),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Expense",
                        style = TextStyle(
                            color = ColorProvider(colors.expense),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = formatWidgetAmount(symbol, expense),
                        style = TextStyle(
                            color = ColorProvider(colors.title),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(GlanceModifier.height(6.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(26.dp)
                        .background(ImageProvider(R.drawable.widget_2x2_log))
                        .clickable(actionStartActivity(openApp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            provider = ImageProvider(R.drawable.widget_ic_mic_outline),
                            contentDescription = "Log",
                            modifier = GlanceModifier.size(12.dp)
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = "Log",
                            style = TextStyle(
                                color = ColorProvider(colors.onBrand),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(GlanceModifier.width(6.dp))
                Box(
                    modifier = GlanceModifier
                        .size(26.dp)
                        .background(ImageProvider(chipBg))
                        .clickable(actionStartActivity(openApp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(camIcon),
                        contentDescription = "Scan",
                        modifier = GlanceModifier.size(14.dp)
                    )
                }
            }
        }
    }
}

internal fun formatWidgetAmount(symbol: String, minor: Long): String {
    val major = minor / 100L
    return "$symbol${"%,d".format(Locale.US, major)}"
}

internal fun currentMonthSummaryLabel(): String {
    val cal = Calendar.getInstance()
    val month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) ?: ""
    return "$month Summary"
}
