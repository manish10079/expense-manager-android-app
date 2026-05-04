package com.mkn0079.expensetracker.ui.components

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.ui.theme.income
import com.mkn0079.expensetracker.ui.theme.expense
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.brandGradient
import com.mkn0079.expensetracker.ui.theme.standardCardGradient
import com.mkn0079.expensetracker.utils.getCurrentDateLabel

@Composable
fun TotalBalanceCard(
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
            .height(240.dp)
            .clickable(onClick = onToggleVisibility)
            .shadow(
                elevation = 26.dp,
                shape = cardShape,
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.48f),
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.36f)
            )
            .clip(cardShape)
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                shape = cardShape
            )
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TOTAL BALANCE",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.1.sp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (isBalanceHidden) "Show Balance" else "Hide Balance",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = currentDateLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = if (isBalanceHidden) "••••" else totalBalance,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (isBalanceHidden) 32.sp else 40.sp,
                lineHeight = 60.sp,
                brush = brandGradient(alpha = 0.95f)
            ),
            maxLines = 1
        )

        if (previousMonthBalance.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LAST MONTH",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.3.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBalanceHidden) "••••" else previousMonthBalance,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.3.sp,
                        lineHeight = 22.sp,
                        brush = brandGradient(alpha = 0.95f)
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                label = "INCOME",
                value = if (isBalanceHidden) "••••" else formatStatAmount(income, '+'),
                icon = Icons.Filled.ArrowUpward,
                iconColor = MaterialTheme.colorScheme.income,
                iconAtStart = true
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .width(1.dp)
                    .height(98.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f))
            )

            StatItem(
                modifier = Modifier.weight(1f),
                label = "EXPENSE",
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

        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
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
    ExpenseTrackerTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            TotalBalanceCard(
                totalBalance = "₹42,850.00",
                income = "₹12,500.00",
                expense = "₹5,320.00"
            )
        }
    }
}
