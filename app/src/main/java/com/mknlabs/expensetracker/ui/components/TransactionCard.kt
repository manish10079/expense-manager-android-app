package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.theme.NeutralGray
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.getAmountColor
import com.mknlabs.expensetracker.utils.getPaymentTypeName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import com.mknlabs.expensetracker.ui.theme.expense
import com.mknlabs.expensetracker.ui.theme.income
import com.mknlabs.expensetracker.ui.theme.transparent

@Composable
fun TransactionCard(
    note: String,
    transactionDate: String,
    transactionTime: String,
    amount: String,
    transactionTypeId: Int,
    icon: ImageVector,
    paymentType: String,
    categoryLabel: String = "",
    showTypeLabel: Boolean = true,
    showTransactionDate: Boolean = true,
    showPaymentMethod: Boolean = true,
    showTransactionTime: Boolean = true,
    showCategoryIcon: Boolean = true,
    showCategoryLabel: Boolean = true,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f)
    val cardBorder = remember(borderColor) {
        BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .then(
                if (isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                } else {
                    Modifier.background(transparent)
                }
            )
            .border(
                border = cardBorder,
                shape = RoundedCornerShape(28.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(14.dp))

        if (showCategoryIcon) {
            val iconBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
            val iconBorder = remember(iconBorderColor) {
                BorderStroke(
                    width = 1.dp,
                    color = iconBorderColor
                )
            }
            AppIconBox(
                icon = icon,
                contentDescription = note,
                size = 50.dp,
                iconSize = 25.dp,
                border = iconBorder
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            val isNoteEmpty = note.isBlank()
            val displayNote = if (isNoteEmpty) stringResource(R.string.label_no_note) else note

            Text(
                text = displayNote,
                color = if (isNoteEmpty) {
                    NeutralGray
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (isNoteEmpty) FontWeight.Normal else FontWeight.ExtraBold,
                    fontStyle = if (isNoteEmpty) FontStyle.Italic else FontStyle.Normal,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            text = " • ",//• ● ⬤ 

                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

            // New Pills Section
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (showTypeLabel) {
                    TransactionPill(
                        text = if (transactionTypeId == 1) stringResource(R.string.label_income) else stringResource(R.string.label_expense),
                        color = if (transactionTypeId == 1) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
                        backgroundColor = if (transactionTypeId == 1) MaterialTheme.colorScheme.income.copy(alpha = 0.12f) else MaterialTheme.colorScheme.expense.copy(alpha = 0.12f)
                    )
                }

                if (showCategoryLabel && categoryLabel.isNotBlank()) {
                    TransactionPill(
                        text = categoryLabel.uppercase(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                }

                if (showPaymentMethod && paymentType.isNotBlank()) {
                    TransactionPill(
                        text = paymentType.uppercase(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = amount,
            color = getAmountColor(transactionTypeId),
            maxLines = 1,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))
    }
}

@Composable
private fun TransactionPill(
    text: String,
    color: Color,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
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
                amount = "+₹5,000",
                transactionTypeId = 1,
                icon = Icons.Filled.QuestionMark,
                paymentType = getPaymentTypeName(3),
                categoryLabel = "Salary"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionCardDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface {
            TransactionCard(
                note = "Groceries",
                transactionDate = "31 Dec",
                transactionTime = formatTime(1738368000000, "24-hour"),
                amount = "-₹2,500",
                transactionTypeId = 2,
                icon = Icons.Filled.QuestionMark,
                paymentType = getPaymentTypeName(1),
                categoryLabel = "Food"
            )
        }
    }
}

