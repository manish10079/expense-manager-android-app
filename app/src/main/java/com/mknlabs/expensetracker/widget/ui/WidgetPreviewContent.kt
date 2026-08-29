package com.mknlabs.expensetracker.widget.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.size
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
                                Image(
                                    provider = ImageProvider(R.drawable.ic_shopping_basket),
                                    contentDescription = "Category Icon",
                                    modifier = GlanceModifier.size(16.dp)
                                )
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                Text(
                                    text = parsedTransaction.categoryName,
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(8.dp))
                        }

                        if (!parsedTransaction.merchant.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_storefront),
                                    contentDescription = "Merchant Icon",
                                    modifier = GlanceModifier.size(16.dp)
                                )
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                Text(
                                    text = parsedTransaction.merchant,
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            Spacer(modifier = GlanceModifier.height(8.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_calendar),
                                contentDescription = "Calendar Icon",
                                modifier = GlanceModifier.size(16.dp)
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = formatWidgetDate(parsedTransaction.createdAt),
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFD1D5DB)),
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
                                color = ColorProvider(Color(0xFFA855F7)),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        if (parsedTransaction.note.isNotBlank()) {
                            Spacer(modifier = GlanceModifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_check_document),
                                    contentDescription = "Note Icon",
                                    modifier = GlanceModifier.size(14.dp)
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                Text(
                                    text = parsedTransaction.note,
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFFD1D5DB)),
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
                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(44.dp)
                        .background(ImageProvider(R.drawable.bg_cancel_button))
                        .clickable(actionRunCallback<WidgetCancelCallback>()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_cancel_x),
                        contentDescription = "Cancel Icon",
                        modifier = GlanceModifier.size(16.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "Cancel",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFEF4444)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                // ── Save button (bg_save_button.xml) ──
                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(44.dp)
                        .background(ImageProvider(R.drawable.bg_save_button))
                        .clickable(actionRunCallback<WidgetSaveCallback>()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_checkmark),
                        contentDescription = "Save Icon",
                        modifier = GlanceModifier.size(16.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "Save",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
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
