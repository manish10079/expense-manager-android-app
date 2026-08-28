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
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.actions.WidgetRetryCallback

/**
 * Error state — pixel-perfect glassmorphic UI.
 *
 * Spec:
 *  - Error icon: #EF4444 circle
 *  - Message: #FFFFFF 14sp bold
 *  - Subtitle: #A0A5C0 12sp
 *  - Retry: #211218 bg, #EF4444 text
 */
@Composable
internal fun WidgetErrorContent(
    errorMessageResId: Int
) {
    val errorColor = ColorProvider(R.color.widget_error)
    val textColor = ColorProvider(R.color.widget_text_primary)
    val subtitleColor = ColorProvider(R.color.widget_text_subtitle)
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
            Box(
                modifier = GlanceModifier
                    .size(64.dp)
                    .background(errorColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚠️", style = TextStyle(fontSize = 28.sp))
            }

            Spacer(modifier = GlanceModifier.height(14.dp))

            Text(
                text = resolveErrorMessage(errorMessageResId),
                style = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = "Please try again",
                style = TextStyle(color = subtitleColor, fontSize = 12.sp)
            )

            Spacer(modifier = GlanceModifier.height(16.dp))

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(cancelBg)
                    .clickable(actionRunCallback<WidgetRetryCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↻  Try Again",
                    style = TextStyle(
                        color = errorColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

private fun resolveErrorMessage(errorMessageResId: Int): String {
    return when (errorMessageResId) {
        R.string.msg_voice_error_empty_input -> "Didn't catch that"
        R.string.msg_voice_error_no_permission -> "Microphone permission needed"
        R.string.msg_voice_error_audio -> "Audio recording error"
        R.string.msg_voice_error_network -> "Network error"
        R.string.msg_voice_error_timeout -> "No speech detected"
        else -> "Something went wrong"
    }
}
