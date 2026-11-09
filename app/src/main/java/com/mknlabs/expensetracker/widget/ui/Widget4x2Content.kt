package com.mknlabs.expensetracker.widget.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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

@Composable
internal fun Widget4x2Content(
    uiState: WidgetUiState,
    forceDark: Boolean? = null
) {
    val dark = widgetIsNightMode(forceDark)
    val colors = if (dark) Widget2x2Colors.Dark else Widget2x2Colors.Light
    val symbol = uiState.currencySymbol
    val month = monthShortLabel()
    val income = uiState.monthIncomeMinor
    val expense = uiState.monthExpenseMinor
    val openApp = Intent().setClass(LocalContext.current, MainActivity::class.java)
    val cardBg = if (dark) R.drawable.widget_2x2_bg_dark else R.drawable.widget_2x2_bg_light
    val chipBg = if (dark) R.drawable.widget_2x2_chip_dark else R.drawable.widget_2x2_chip_light

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
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Transaction",
                    style = TextStyle(
                        color = ColorProvider(colors.title),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(GlanceModifier.height(8.dp))
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(chipBg))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "$month Income".uppercase(Locale.US),
                        style = TextStyle(
                            color = ColorProvider(colors.muted),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "+${formatWidgetAmount(symbol, income)}",
                        style = TextStyle(
                            color = ColorProvider(colors.income),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "$month Expense".uppercase(Locale.US),
                        style = TextStyle(
                            color = ColorProvider(colors.muted),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "-${formatWidgetAmount(symbol, expense)}",
                        style = TextStyle(
                            color = ColorProvider(colors.expense),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(GlanceModifier.height(8.dp))
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(chipBg))
                    .padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp)
                    .clickable(widgetOverlayAction(LocalContext.current, OverlayUiMode.TEXT)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPI screenshot? Grab the words.",
                    style = TextStyle(
                        color = ColorProvider(colors.muted),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Image(
                    provider = ImageProvider(R.drawable.widget_ic_arrow_right),
                    contentDescription = "Open",
                    modifier = GlanceModifier
                        .size(32.dp)
                        .background(ImageProvider(R.drawable.widget_2x2_log))
                        .padding(8.dp)
                )
            }

            Spacer(GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Widget4x2ActionCard(
                    title = "Mic",
                    subtitle = "Speak and log",
                    icon = R.drawable.widget_ic_mic_outline,
                    chipBg = chipBg,
                    titleColor = colors.title,
                    mutedColor = colors.muted,
                    modifier = GlanceModifier.defaultWeight()
                        .clickable(widgetOverlayAction(LocalContext.current, OverlayUiMode.VOICE))
                )
                Spacer(GlanceModifier.width(6.dp))
                Widget4x2ActionCard(
                    title = "Scan",
                    subtitle = "Image or PDF OCR",
                    icon = R.drawable.widget_ic_camera_on_brand,
                    chipBg = chipBg,
                    titleColor = colors.title,
                    mutedColor = colors.muted,
                    modifier = GlanceModifier.defaultWeight()
                        .clickable(widgetOverlayAction(LocalContext.current, OverlayUiMode.SCAN))
                )
            }
        }
    }
}

@Composable
private fun Widget4x2ActionCard(
    title: String,
    subtitle: String,
    icon: Int,
    chipBg: Int,
    titleColor: androidx.compose.ui.graphics.Color,
    mutedColor: androidx.compose.ui.graphics.Color,
    modifier: GlanceModifier
) {
    Row(
        modifier = modifier
            .background(ImageProvider(chipBg))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .size(32.dp)
                .background(ImageProvider(R.drawable.widget_2x2_log)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = title,
                modifier = GlanceModifier.size(16.dp)
            )
        }
        Spacer(GlanceModifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = TextStyle(
                    color = ColorProvider(titleColor),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subtitle,
                style = TextStyle(
                    color = ColorProvider(mutedColor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

private fun monthShortLabel(): String {
    val cal = Calendar.getInstance()
    return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) ?: ""
}
