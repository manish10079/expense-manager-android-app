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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.income
import com.mknlabs.expensetracker.ui.theme.expense
import com.mknlabs.expensetracker.ui.theme.isDark
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkStart
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkCenter
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkEnd
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightStart
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightCenter
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightEnd
import com.mknlabs.expensetracker.utils.getCurrentDateLabel

@Composable
fun StatsCard(
    totalBalance: String,
    previousMonthBalance: String = "",
    income: String,
    expense: String,
    isBalanceHidden: Boolean = false,
    onToggleVisibility: () -> Unit = {}
) {
    val cardShape = RoundedCornerShape(30.dp)
    val currentDateLabel = getCurrentDateLabel()

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.isDark

    // Background matching GoalItem card from GoalsScreen (MaterialTheme.colorScheme.surface)
    val cardBg = colorScheme.surface
    val borderBrush = remember(colorScheme.primary) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.4f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    // All text colors derived from MaterialTheme.colorScheme
    val labelColor = colorScheme.onSurfaceVariant
    val dateColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val dividerColor = colorScheme.outlineVariant.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 154.dp)
            .shadow(
                elevation = 4.dp,
                shape = cardShape,
                ambientColor = colorScheme.primary.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(cardShape)
            .clickable(onClick = onToggleVisibility)
            .background(cardBg)
            .border(width = 1.dp, brush = borderBrush, shape = cardShape)
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Label & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelText(
                        text = stringResource(R.string.label_total_balance),
                        color = labelColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier
                            .size(18.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isBalanceHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (isBalanceHidden) stringResource(R.string.desc_show_balance) else stringResource(R.string.desc_hide_balance),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                LabelText(
                    text = currentDateLabel,
                    color = dateColor
                )
            }

            // Main Balance Amount
            AmountText(
                text = if (isBalanceHidden) "****" else totalBalance,
                modifier = Modifier.fillMaxWidth(),
                brush = brandGradient(alpha = 0.95f),
                responsive = !isBalanceHidden
            )

            // Previous Month Balance
            if (previousMonthBalance.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabelText(
                        text = stringResource(R.string.label_last_month),
                        color = labelColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBalanceHidden) "****" else previousMonthBalance,
                        style = MaterialTheme.typography.labelLarge.copy(
                            brush = brandGradient(alpha = 0.95f)
                        ),
                        maxLines = 1
                    )
                }
            }

            // Subtle divider
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor)
            )

            // Income vs Expense Metric Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricPill(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_income),
                    amount = if (isBalanceHidden) "****" else formatStatAmount(income, '+'),
                    badgeColor = colorScheme.income,
                    icon = Icons.Filled.ArrowUpward
                )

                MetricPill(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_expense),
                    amount = if (isBalanceHidden) "****" else formatStatAmount(expense, '-'),
                    badgeColor = colorScheme.expense,
                    icon = Icons.Filled.ArrowDownward
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    modifier: Modifier = Modifier,
    label: String,
    amount: String,
    badgeColor: Color,
    icon: ImageVector
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.isDark

    val pillBorderBrush = remember(badgeColor) {
        Brush.linearGradient(
            colors = listOf(
                badgeColor.copy(alpha = 0.4f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(badgeColor.copy(alpha = if (isDark) 0.08f else 0.10f))
            .border(
                width = 1.dp,
                brush = pillBorderBrush,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = badgeColor,
            modifier = Modifier.size(16.dp)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6
                .dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = colorScheme.onSurface
            )
        }
    }
}

private const val EmptyStatAmountFallback = "\u20B900"

private fun formatStatAmount(value: String, sign: Char): String {
    val trimmedValue = value.trim()
    if (trimmedValue.isBlank()) {
        return EmptyStatAmountFallback
    }

    return if (trimmedValue.isZeroAmount()) {
        trimmedValue.toZeroPlaceholder()
    } else if (trimmedValue.startsWith("+") || trimmedValue.startsWith("-")) {
        trimmedValue
    } else {
        "$sign$trimmedValue"
    }
}

private fun String.isZeroAmount(): Boolean {
    val digits = filter(Char::isDigit)
    return digits.isNotEmpty() && digits.all { it == '0' }
}

private fun String.toZeroPlaceholder(): String {
    val sanitizedValue = removePrefix("+").removePrefix("-").trim()
    val firstDigitIndex = sanitizedValue.indexOfFirst { it.isDigit() }
    val lastDigitIndex = sanitizedValue.indexOfLast { it.isDigit() }

    if (firstDigitIndex == -1 || lastDigitIndex == -1) {
        return EmptyStatAmountFallback
    }

    val prefix = sanitizedValue.take(firstDigitIndex).trim()
    val suffix = sanitizedValue.drop(lastDigitIndex + 1).trim()

    return when {
        prefix.isNotEmpty() -> "$prefix" + "00"
        suffix.isNotEmpty() -> "00$suffix"
        else -> EmptyStatAmountFallback
    }
}

@Preview(showBackground = false)
@Composable
fun TotalBalanceCardPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            StatsCard(
                totalBalance = "₹42,850.00",
                income = "₹12,500.00",
                expense = "₹5,320.00"
            )
        }
    }
}

@Preview(name = "Stats Card - Multi-Config", showBackground = false)
//@PreviewScreenSizes
//@PreviewFontScale
@Composable
fun StatsCardMultiConfigPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            StatsCard(
                totalBalance = "₹42,850.00",
                previousMonthBalance = "₹38,200.00",
                income = "₹12,500.00",
                expense = "₹5,320.00"
            )
        }
    }
}
