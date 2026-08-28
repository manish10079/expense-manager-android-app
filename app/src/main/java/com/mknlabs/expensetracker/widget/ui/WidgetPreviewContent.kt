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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.actions.WidgetCancelCallback
import com.mknlabs.expensetracker.widget.actions.WidgetSaveCallback
import com.mknlabs.expensetracker.widget.model.WidgetParsedTransaction

@Composable
internal fun WidgetPreviewContent(
    parsedTransaction: WidgetParsedTransaction
) {
    val surfaceColor = ColorProvider(R.color.widget_surface)
    val primaryColor = ColorProvider(R.color.widget_primary)
    val successColor = ColorProvider(R.color.widget_success)
    val errorColor = ColorProvider(R.color.widget_error)
    val textColor = ColorProvider(R.color.widget_text)
    val subtitleColor = ColorProvider(R.color.widget_subtitle)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            // Amount — purple accent matching app
            Text(
                text = parsedTransaction.amountText,
                style = TextStyle(color = primaryColor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = GlanceModifier.height(6.dp))

            if (parsedTransaction.categoryName.isNotBlank()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📁", style = TextStyle(fontSize = 13.sp))
                    Text(text = parsedTransaction.categoryName, style = TextStyle(color = subtitleColor, fontSize = 12.sp))
                }
            }

            if (!parsedTransaction.merchant.isNullOrBlank()) {
                Spacer(modifier = GlanceModifier.height(3.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🏪", style = TextStyle(fontSize = 13.sp))
                    Text(text = parsedTransaction.merchant, style = TextStyle(color = subtitleColor, fontSize = 12.sp))
                }
            }

            if (parsedTransaction.note.isNotBlank()) {
                Spacer(modifier = GlanceModifier.height(3.dp))
                Text(text = parsedTransaction.note, style = TextStyle(color = subtitleColor, fontSize = 11.sp))
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(38.dp)
                        .background(errorColor)
                        .clickable(actionRunCallback<WidgetCancelCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Cancel", style = TextStyle(color = ColorProvider(android.R.color.white), fontSize = 13.sp, fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(38.dp)
                        .background(successColor)
                        .clickable(actionRunCallback<WidgetSaveCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Save", style = TextStyle(color = ColorProvider(android.R.color.white), fontSize = 13.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
