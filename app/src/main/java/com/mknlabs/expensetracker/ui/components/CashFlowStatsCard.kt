package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.isDark
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkStart
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkCenter
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkEnd
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightStart
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightCenter
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightEnd

/**
 * Home-screen "Cash Flow" stats card: this month's SPENDING / INCOME and the
 * Net Balance, laid out per the product spec (20dp rounded card, 16dp padding,
 * 16dp vertical rhythm, "This Month" pill header, equal-width metric columns,
 * and a Net Balance inner container with a thin dashed underline).
 *
 * [isBalanceHidden] / [onToggleVisibility] preserve the existing privacy
 * auto-hide behavior: amounts are masked with "****" while hidden and the
 * whole card toggles visibility when tapped (same as the previous card).
 */
@Composable
fun CashFlowStatsCard(
    expense: String,
    income: String,
    netBalance: String,
    isBalanceHidden: Boolean = false,
    onToggleVisibility: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme

    // Theme-aware gradient background (same visual language as the previous
    // StatsCard / SmallHomeCard / Today's Spending cards).
    val cardBrush = if (colorScheme.isDark) {
        Brush.linearGradient(listOf(PremiumCardDarkStart, PremiumCardDarkCenter, PremiumCardDarkEnd))
    } else {
        Brush.linearGradient(listOf(PremiumCardLightStart, PremiumCardLightCenter, PremiumCardLightEnd))
    }

    val borderBrush = remember(colorScheme.primary) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.4f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onToggleVisibility)
            .background(brush = cardBrush)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: CASH FLOW title + "This Month" pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_cash_flow),
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = colorScheme.onSurfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_this_month_cash_flow),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Metrics Row: EXPENSE | INCOME (two equal halves)
            Row(modifier = Modifier.fillMaxWidth()) {
                CashFlowMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_spending_cash_flow),
                    amount = if (isBalanceHidden) "****" else expense,
                    labelColor = Color(0xFFE53935),
                    amountColor = Color(0xFFE53935),
                    textAlign = TextAlign.Start
                )
                CashFlowMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_income),
                    amount = if (isBalanceHidden) "****" else income,
                    labelColor = Color(0xFF43A047),
                    amountColor = Color(0xFF43A047),
                    textAlign = TextAlign.End
                )
            }

            // Net Balance inner container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "Net Balance" with a thin dashed underline directly below it
                    Text(
                        text = stringResource(R.string.label_net_balance_cash_flow),
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.drawWithContent {
                            drawContent()
                            val dashWidth = 4.dp.toPx()
                            val dashGap = 3.dp.toPx()
                            val strokeWidth = 1.dp.toPx()
                            val lineY = size.height - strokeWidth / 2f
                            var x = 0f
                            while (x < size.width) {
                                val endX = (x + dashWidth).coerceAtMost(size.width)
                                if (endX > x) {
                                    drawLine(
                                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                        start = Offset(x, lineY),
                                        end = Offset(endX, lineY),
                                        strokeWidth = strokeWidth
                                    )
                                }
                                x += dashWidth + dashGap
                            }
                        }
                    )

                    Text(
                        text = if (isBalanceHidden) "****" else netBalance,
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CashFlowMetric(
    modifier: Modifier = Modifier,
    label: String,
    amount: String,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (textAlign == TextAlign.End) Alignment.End else Alignment.Start
    ) {
        Text(
            text = label,
            color = labelColor,
            textAlign = textAlign,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = amount,
            color = amountColor,
            textAlign = textAlign,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, name = "Cash Flow Stats Card - Dark")
@Composable
private fun CashFlowStatsCardDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            CashFlowStatsCard(
                expense = "₹1,200",
                income = "₹0.00",
                netBalance = "-₹1,200"
            )
        }
    }
}

@Preview(showBackground = true, name = "Cash Flow Stats Card - Light")
@Composable
private fun CashFlowStatsCardLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            CashFlowStatsCard(
                expense = "₹1,200",
                income = "₹0.00",
                netBalance = "-₹1,200"
            )
        }
    }
}