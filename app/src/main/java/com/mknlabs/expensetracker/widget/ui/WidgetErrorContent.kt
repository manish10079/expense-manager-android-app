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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.actions.WidgetRetryCallback

/**
 * Widget error state: friendly message + retry button.
 */
@Composable
internal fun WidgetErrorContent(
    errorMessageResId: Int
) {
    val surfaceColor = ColorProvider(R.color.widget_surface)
    val errorColor = ColorProvider(R.color.widget_error)
    val textColor = ColorProvider(R.color.widget_text)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(errorColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚠️", style = TextStyle(fontSize = 24.sp))
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = resolveErrorMessage(errorMessageResId),
                style = TextStyle(color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(errorColor)
                    .clickable(actionRunCallback<WidgetRetryCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Try Again",
                    style = TextStyle(color = ColorProvider(android.R.color.white), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

private fun resolveErrorMessage(errorMessageResId: Int): String {
    return when (errorMessageResId) {
        R.string.msg_voice_error_empty_input -> "Didn't catch that. Please try again."
        R.string.msg_voice_error_no_permission -> "Microphone permission needed."
        R.string.msg_voice_error_audio -> "Audio recording error. Please try again."
        R.string.msg_voice_error_network -> "Network error. Please check your connection."
        R.string.msg_voice_error_timeout -> "No speech detected. Please try again."
        else -> "Something went wrong. Please try again."
    }
}
