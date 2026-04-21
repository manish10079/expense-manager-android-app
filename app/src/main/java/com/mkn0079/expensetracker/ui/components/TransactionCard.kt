package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.utils.formatTime
import com.mkn0079.expensetracker.utils.getAmountColor
import com.mkn0079.expensetracker.utils.getPaymentTypeName

@Composable
fun TransactionCard(
    note: String,
    transactionDate: String,
    transactionTime: String,
    amount: String,
    transactionTypeId: Int,
    icon: ImageVector,
    paymentType: String,
    showTypeLabel: Boolean = true,
    showTransactionDate: Boolean = true,
    showPaymentMethod: Boolean = true,
    showTransactionTime: Boolean = true,
    showCategoryIcon: Boolean = true,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val backgroundColor = if (isSelected) Color(0xFF2D243F) else Color(0xFF171718)
    val borderColor = if (isSelected) Color(0xFF8B63F1).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.03f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(28.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .height(70.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(14.dp))

        if (showCategoryIcon) {
            BoxIcon(icon = icon, contentDescription = note)
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = note,
                color = Color(0xFFF3EEF4),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            )

            if (showTransactionDate || showTransactionTime) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showTransactionDate) {
                        Text(
                            text = transactionDate,
                            color = Color(0xFF807987),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }

                    if (showTransactionDate && showTransactionTime) {
                        Text(
                            text = " | ",
                            color = Color(0xFF807987),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            ),
                            modifier = Modifier.wrapContentWidth()
                        )
                    }

                    if (showTransactionTime) {
                        Text(
                            text = transactionTime,
                            color = Color(0xFF807987),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }
                }
            }

            if (showPaymentMethod) {
                Spacer(
                    modifier = Modifier.height(
                        if (showTransactionDate || showTransactionTime) 1.dp else 4.dp
                    )
                )

                Text(
                    text = paymentType,
                    color = Color(0xFF807987),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = amount,
                color = getAmountColor(transactionTypeId),
                maxLines = 1,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )

            if (showTypeLabel) {
                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = if (transactionTypeId == 1) "INCOME" else "EXPENSE",
                    color = Color(0xFF807987),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))
    }
}

@Composable
private fun BoxIcon(
    icon: ImageVector,
    contentDescription: String
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF3D3159))
            .border(
                width = 1.dp,
                color = Color(0xFF5B4A84),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFFE2D8FF),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionCardLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            TransactionCard(
                note = "Salary Credit",
                transactionDate = "31 Dec",
                transactionTime = formatTime(1738368000000, "12-hour"),
                amount = "+$55,000",
                transactionTypeId = 1,
                icon = Icons.Filled.QuestionMark,
                paymentType = getPaymentTypeName(3)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionCardDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface(color = Color(0xFF0A0A0A)) {
            TransactionCard(
                note = "Salary Credit",
                transactionDate = "31 Dec",
                transactionTime = formatTime(1738368000000, "24-hour"),
                amount = "+$55,000",
                transactionTypeId = 1,
                icon = Icons.Filled.QuestionMark,
                paymentType = getPaymentTypeName(3)
            )
        }
    }
}
