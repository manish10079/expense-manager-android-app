package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
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
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.utils.getCurrentDateLabel

@Composable
fun TotalBalanceCard(
    totalBalance: String,
    income: String,
    expense: String
) {
    val cardShape = RoundedCornerShape(35.dp)
    val currentDateLabel = getCurrentDateLabel()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(
                elevation = 26.dp,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.48f),
                spotColor = Color.Black.copy(alpha = 0.36f)
            )
            .clip(cardShape)
            .background(
                color = Color(0xFF1D1D1F),
                shape = cardShape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.03f),
                shape = cardShape
            )
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL BALANCE",
                color = Color(0xFFB7B0C3),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.1.sp
            )

            Text(
                text = currentDateLabel,
                color = Color(0xFF9E98AA),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = totalBalance,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF8E6FFF),
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 60.sp,
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            StatItem(
                modifier = Modifier.weight(1f),
                label = "INCOME",
                value = formatStatAmount(income, '+'),
                icon = Icons.Filled.ArrowUpward,
                iconColor = Color(0xFFF6C7A6),
                iconAtStart = true
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .width(1.dp)
                    .height(98.dp)
                    .background(Color.White.copy(alpha = 0.04f))
            )

            StatItem(
                modifier = Modifier.weight(1f),
                label = "EXPENSE",
                value = formatStatAmount(expense, '-'),
                icon = Icons.Filled.ArrowDownward,
                iconColor = Color(0xFFF2B9AF),
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
                StatIconBubble(
                    icon = icon,
                    iconColor = iconColor,
                    contentDescription = label
                )

                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                color = Color(0xFFBEB8C7)
            )

            if (!iconAtStart) {
                Spacer(modifier = Modifier.width(12.dp))

                StatIconBubble(
                    icon = icon,
                    iconColor = iconColor,
                    contentDescription = label
                )
            }
        }

        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF3F1F6),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (iconAtStart) TextAlign.Start else TextAlign.End
        )
    }
}

@Composable
private fun StatIconBubble(
    icon: ImageVector,
    iconColor: Color,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(Color(0xFF232326), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
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

@Preview(showBackground = true, backgroundColor = 0xFF09090B)
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
