package com.mknlabs.expensetracker.widget.ui

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
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
 * Uses ImageProvider for drawable backgrounds:
 *  - bg_expense_card.xml: Translucent dark blue with border
 *  - bg_cancel_button.xml: Dark red with border
 *  - bg_save_button.xml: Purple gradient
 *
 * Spec:
 *  Card: #161829 @ 60% opacity, 16dp radius, 1dp #2A2D48 border, 12dp padding
 *  Left:  category #4ADE80 icon + "Groceries" #FFFFFF 14sp
 *         merchant #FB923C icon + "Big Bazaar" #FFFFFF 14sp
 *         calendar #9CA3AF icon + date #D1D5DB 12sp
 *  Right: amount #A855F7 bold 26sp
 *         note #9CA3AF icon + text #D1D5DB 12sp
 *  Cancel: #211218 bg, #7F1D1D border, #EF4444 text, bold 14sp
 *  Save:   #6D28D9→#7C3AED gradient bg, #FFFFFF text, bold 14sp
 */
@Composable
internal fun WidgetPreviewContent(
    parsedTransaction: WidgetParsedTransaction
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // ════════════════════════════════════════
            //  EXPENSE DETAILS CARD
            //  bg_expense_card.xml: #161829 @ 60% opacity, 16dp radius, 1dp #2A2D48 border
            // ════════════════════════════════════════
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ImageProvider(R.drawable.bg_expense_card))
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
                                    style = TextStyle(
                                        color = ColorProvider(Color.WHITE),
                                        fontSize = 14.sp
                                    )
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
                                    style = TextStyle(
                                        color = ColorProvider(Color.WHITE),
                                        fontSize = 14.sp
                                    )
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
                                style = TextStyle(
                                    color = ColorProvider(Color.parseColor("#D1D5DB")),
                                    fontSize = 12.sp
                                )
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
                                color = ColorProvider(Color.parseColor("#A855F7")),
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
                                    style = TextStyle(
                                        color = ColorProvider(Color.parseColor("#D1D5DB")),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // ════════════════════════════════════════
            //  ACTION BUTTONS (ImageProvider drawables)
            // ════════════════════════════════════════
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                // ── Cancel button (bg_cancel_button.xml) ──
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(44.dp)
                        .background(ImageProvider(R.drawable.bg_cancel_button))
                        .clickable(actionRunCallback<WidgetCancelCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕  Cancel",
                        style = TextStyle(
                            color = ColorProvider(Color.parseColor("#EF4444")),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                // ── Save button (bg_save_button.xml) ──
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(44.dp)
                        .background(ImageProvider(R.drawable.bg_save_button))
                        .clickable(actionRunCallback<WidgetSaveCallback>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓  Save",
                        style = TextStyle(
                            color = ColorProvider(Color.WHITE),
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
