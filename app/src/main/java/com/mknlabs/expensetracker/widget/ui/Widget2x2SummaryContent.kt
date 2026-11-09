package com.mknlabs.expensetracker.widget.ui

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import com.mknlabs.expensetracker.widget.overlay.OverlayUiMode
import com.mknlabs.expensetracker.widget.overlay.widgetOverlayAction
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
    val budgetCap = uiState.monthBudgetCapMinor
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
                .padding(8.dp)
        ) {
            Text(
                text = month.uppercase(Locale.US),
                style = TextStyle(
                    color = ColorProvider(colors.muted),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(GlanceModifier.height(4.dp))
            Widget2x2MetricStack(
                label = "Net Saved",
                amount = "$netPrefix${formatWidgetAmount(symbol, abs(net))}",
                labelColor = colors.muted,
                amountColor = colors.title,
                chipBg = chipBg
            )
            Spacer(GlanceModifier.height(6.dp))
            Widget2x2MetricStack(
                label = "Income",
                amount = formatWidgetAmount(symbol, income),
                labelColor = colors.income,
                amountColor = colors.title,
                chipBg = chipBg,
                trailingIcon = R.drawable.widget_ic_mic_outline,
                trailingClick = widgetOverlayAction(LocalContext.current, OverlayUiMode.VOICE)
            )
            Spacer(GlanceModifier.height(6.dp))
            Widget2x2MetricStack(
                label = "Expense",
                amount = formatWidgetAmount(symbol, expense),
                labelColor = colors.expense,
                amountColor = colors.title,
                chipBg = chipBg,
                trailingIcon = camIcon,
                trailingClick = widgetOverlayAction(LocalContext.current, OverlayUiMode.SCAN)
            )
            if (budgetCap > 0L) {
                val left = budgetCap - expense
                Spacer(GlanceModifier.height(6.dp))
                Widget2x2MetricStack(
                    label = "Left",
                    amount = formatWidgetAmount(symbol, abs(left)),
                    labelColor = colors.muted,
                    amountColor = if (left >= 0) colors.income else colors.expense,
                    chipBg = chipBg
                )
            }

        }
    }
}

@Composable
private fun Widget2x2MetricStack(
    label: String,
    amount: String,
    labelColor: Color,
    amountColor: Color,
    chipBg: Int,
    trailingIcon: Int? = null,
    trailingClick: Action? = null
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ImageProvider(chipBg))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(labelColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            Text(
                text = amount,
                style = TextStyle(
                    color = ColorProvider(amountColor),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        if (trailingIcon != null && trailingClick != null) {
            Box(
                modifier = GlanceModifier
                    .size(28.dp)
                    .background(ImageProvider(R.drawable.widget_2x2_log))
                    .clickable(trailingClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(trailingIcon),
                    contentDescription = label,
                    modifier = GlanceModifier.size(14.dp)
                )
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
