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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.widget.actions.WidgetCancelCallback
import com.mknlabs.expensetracker.widget.actions.WidgetSaveCallback
import com.mknlabs.expensetracker.widget.model.WidgetParsedTransaction

/**
 * Preview state — pixel-perfect glassmorphic expense card + buttons.
 *
 * Spec:
 *  Card: #161829 @ 60% opacity, 16dp radius, 1dp #2A2D48 border, 12dp padding
 *  Left:  category #4ADE80 icon + "Groceries" #FFFFFF 14sp
 *         merchant #FB923C icon + "Big Bazaar" #FFFFFF 14sp
 *         calendar #9CA3AF icon + date #D1D5DB 12sp
 *  Right: amount #A855F7 bold 26sp
 *         note #9CA3AF icon + text #D1D5DB 12sp
 *  Cancel: #211218 bg, #EF4444 text, bold 14sp
 *  Save:   #6D28D9 bg, #FFFFFF text, bold 14sp
 */
@Composable
internal fun WidgetPreviewContent(
    parsedTransaction: WidgetParsedTransaction
) {
    val textColor = ColorProvider(R.color.widget_text_primary)       // #FFFFFF
    val amountColor = ColorProvider(R.color.widget_text_amount)      // #A855F7
    val detailColor = ColorProvider(R.color.widget_text_detail)      // #D1D5DB
    val cancelTextColor = ColorProvider(R.color.widget_cancel_text)  // #EF4444
    val cancelBg = ColorProvider(R.color.widget_cancel_bg)           // #211218
    val detailsBg = ColorProvider(R.color.widget_details_card)       // #99161829
    val saveColor = ColorProvider(R.color.widget_save_start)         // #6D28D9

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // ════════════════════════════════════════
            //  EXPENSE DETAILS CARD
            //  #161829 @ 60% opacity, 12dp inner padding
            // ════════════════════════════════════════
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(detailsBg)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // ── Left column ──
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        if (parsedTransaction.categoryName.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🛒",
                                    style = TextStyle(fontSize = 14.sp)
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                Text(
                                    text = parsedTransaction.categoryName,
                                    style = TextStyle(color = textColor, fontSize = 14.sp)
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(8.dp))
                        }

                        if (!parsedTransaction.merchant.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🏪",
                                    style = TextStyle(fontSize = 14.sp)
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                Text(
                                    text = parsedTransaction.merchant,
                                    style = TextStyle(color = textColor, fontSize = 14.sp)
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(8.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📅",
                                style = TextStyle(fontSize = 14.sp)
                            )
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = formatWidgetDate(parsedTransaction.createdAt),
                                style = TextStyle(color = detailColor, fontSize = 12.sp)
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(12.dp))

                    // ── Right column ──
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = parsedTransaction.amountText,
                            style = TextStyle(
                                color = amountColor,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        if (parsedTransaction.note.isNotBlank()) {
                            Spacer(modifier = GlanceModifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📝",
                                    style = TextStyle(fontSize = 12.sp)
                                )
                                Spacer(modifier = GlanceModifier.width(4.dp))
                                Text(
                                    text = parsedTransaction.note,
                                    style = TextStyle(color = detailColor, fontSize = 12.sp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // ════════════════════════════════════════
            //  ACTION BUTTONS
            //  Cancel: #211218 bg, #EF4444 text
            //  Save:   #6D28D9 bg, #FFFFFF text
            // ════════════════════════════════════════
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                // ── Cancel button (pill) ──
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(44.dp)
                        .background(cancelBg)
                        .clickable(actionRunCallback<WidgetCancelCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕  Cancel",
                        style = TextStyle(
                            color = cancelTextColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                // ── Save button (pill) ──
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(44.dp)
                        .background(saveColor)
                        .clickable(actionRunCallback<WidgetSaveCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓  Save",
                        style = TextStyle(
                            color = ColorProvider(android.R.color.white),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

private fun formatWidgetDate(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val month = cal.getDisplayName(
        java.util.Calendar.MONTH,
        java.util.Calendar.SHORT,
        java.util.Locale.getDefault()
    ) ?: ""
    val year = cal.get(java.util.Calendar.YEAR)
    return "$day $month $year"
}
