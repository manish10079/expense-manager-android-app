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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.theme.income
import com.mknlabs.expensetracker.ui.theme.expense
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
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
    val cardShape = RoundedCornerShape(35.dp)
    val currentDateLabel = getCurrentDateLabel()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .shadow(
                elevation = 20.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)
            )
            .clip(cardShape)
            .clickable(onClick = onToggleVisibility)
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                shape = cardShape
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.label_total_balance),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.4.sp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (isBalanceHidden) stringResource(R.string.desc_show_balance) else stringResource(R.string.desc_hide_balance),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = currentDateLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        val balanceFontSize = when {
            totalBalance.length > 12 -> 24.sp
            totalBalance.length > 10 -> 28.sp
            else -> 34.sp
        }

        Text(
            text = if (isBalanceHidden) "••••" else totalBalance,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isBalanceHidden) 28.sp else balanceFontSize,
                lineHeight = if (isBalanceHidden) 44.sp else (balanceFontSize.value * 1.2).sp,
                brush = brandGradient(alpha = 0.95f)
            ),
            maxLines = 1
        )

        if (previousMonthBalance.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_last_month),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.4.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBalanceHidden) "••••" else previousMonthBalance,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        brush = brandGradient(alpha = 0.95f)
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_income),
                value = if (isBalanceHidden) "••••" else formatStatAmount(income, '+'),
                icon = Icons.Filled.ArrowUpward,
                iconColor = MaterialTheme.colorScheme.income,
                iconAtStart = true
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .width(1.dp)
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f))
            )

            StatItem(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.label_expense),
                value = if (isBalanceHidden) "••••" else formatStatAmount(expense, '-'),
                icon = Icons.Filled.ArrowDownward,
                iconColor = MaterialTheme.colorScheme.expense,
                iconAtStart = false
            )
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconAtStart: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (iconAtStart) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconAtStart) {
                AppIconBox(
                    icon = icon,
                    contentDescription = label,
                    size = 38.dp,
                    iconSize = 20.dp,
                    tint = iconColor,
                    backgroundAlpha = 0.12f
                )

                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!iconAtStart) {
                Spacer(modifier = Modifier.width(12.dp))

                AppIconBox(
                    icon = icon,
                    contentDescription = label,
                    size = 38.dp,
                    iconSize = 20.dp,
                    tint = iconColor,
                    backgroundAlpha = 0.12f
                )
            }
        }

        val valueFontSize = when {
            value.length > 12 -> 14.sp
            value.length > 10 -> 16.sp
            else -> 20.sp
        }

        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            fontSize = valueFontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (iconAtStart) TextAlign.Start else TextAlign.End
        )
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
    ExpenseTrackerTheme(darkTheme = false
    ) {
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
