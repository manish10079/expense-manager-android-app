package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.theme.NeutralGray
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.getAmountColor
import com.mknlabs.expensetracker.utils.getPaymentTypeName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.mknlabs.expensetracker.ui.theme.expense
import com.mknlabs.expensetracker.ui.theme.income
import com.mknlabs.expensetracker.ui.theme.transparent
import kotlinx.coroutines.launch

private val SeparatorSpanStyle = SpanStyle(letterSpacing = 0.8.sp)

@OptIn(ExperimentalMaterial3Api::class)
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
    // Pro-gated: the full-note tooltip (info icon) only renders for Pro users.
    showNoteTooltip: Boolean = true,
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

    // Hoisted string resources: resolved once per card slot (cached across recompositions)
    // instead of inside the per-pill branch on every composition. Keyed on the Resources
    // instance so a runtime locale/config change invalidates the cached strings.
    val resources = LocalContext.current.resources
    val incomeLabel = remember(resources) { resources.getString(R.string.label_income) }
    val expenseLabel = remember(resources) { resources.getString(R.string.label_expense) }
    val noNoteLabel = remember(resources) { resources.getString(R.string.label_no_note) }

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
            val displayNote = if (isNoteEmpty) noNoteLabel else note
            // True only while the note is actually truncated (single line +
            // ellipsis) — the full-note info icon is offered only in that case.
            // Line count matters: a multi-line note whose first line fits the
            // width is still truncated, so we also compare the note's real line
            // count against what the single-line Text actually rendered.
            val noteLineCount = remember(displayNote) { displayNote.count { it == '\n' } + 1 }
            var noteTruncated by remember(displayNote) { mutableStateOf(false) }
            val noteTooltipState = rememberTooltipState()
            val noteTooltipScope = rememberCoroutineScope()

            // Truncated notes get a small info icon that reveals the complete
            // text in a themed tooltip popup when tapped (tapping again dismisses
            // it; tapping anywhere else dismisses too). The note text itself
            // keeps its default behavior — tap opens the transaction, long-press
            // enters multi-select — only the icon carries the tooltip.
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayNote,
                    color = if (isNoteEmpty) {
                        NeutralGray
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isNoteEmpty) FontWeight.Normal else FontWeight.Bold,
                        fontStyle = if (isNoteEmpty) FontStyle.Italic else FontStyle.Normal,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.weight(1f, fill = false),
                    onTextLayout = { result ->
                        noteTruncated = result.didOverflowWidth || result.lineCount < noteLineCount
                    }
                )

                if (showNoteTooltip && noteTruncated && !isNoteEmpty) {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                            TooltipAnchorPosition.Above
                        ),
                        tooltip = {
                            PlainTooltip {
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                )
                            }
                        },
                        state = noteTooltipState,
                        onDismissRequest = { noteTooltipState.dismiss() },
                        enableUserInput = false
                    ) {
                        // 24.dp Box keeps the interactive area comfortable to hit while the
                        // 16.dp glyph stays visually light inside the compact card row.
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    noteTooltipScope.launch {
                                        if (noteTooltipState.isVisible) {
                                            noteTooltipState.dismiss()
                                        } else {
                                            noteTooltipState.show()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(
                                    if (noteTooltipState.isVisible) {
                                        R.string.desc_hide_full_note

                                    } else {
                                        R.string.desc_view_full_note
                                    }
                                ),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (showTransactionDate || showTransactionTime) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = remember(transactionDate, transactionTime, showTransactionDate, showTransactionTime) {
                        buildAnnotatedString {
                            if (showTransactionDate) {
                                append(transactionDate)
                            }
                            if (showTransactionDate && showTransactionTime) {
                                withStyle(SeparatorSpanStyle) {
                                    append(" • ") // • ● ⬤
                                }
                            }
                            if (showTransactionTime) {
                                append(transactionTime)
                            }
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp
                    )
                )
            }

            // Pills Section (single-line Row — cheaper than FlowRow for short labels)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (showTypeLabel) {
                    TransactionPill(
                        text = if (transactionTypeId == 1) incomeLabel else expenseLabel,
                        color = if (transactionTypeId == 1) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
                        backgroundColor = if (transactionTypeId == 1) MaterialTheme.colorScheme.income.copy(alpha = 0.12f) else MaterialTheme.colorScheme.expense.copy(alpha = 0.12f)
                    )
                }

                if (showCategoryLabel && categoryLabel.isNotBlank()) {
                    TransactionPill(
                        text = categoryLabel,
                        color = MaterialTheme.colorScheme.primary,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                }

                if (showPaymentMethod && paymentType.isNotBlank()) {
                    TransactionPill(
                        text = paymentType,
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
            softWrap = false,
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
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
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
                paymentType = getPaymentTypeName(3).uppercase(),
                categoryLabel = "Salary".uppercase()
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
                note = "Groceries\nchips",
                transactionDate = "31 Dec",
                transactionTime = formatTime(1738368000000, "24-hour"),
                amount = "-₹2,500",
                transactionTypeId = 2,
                icon = Icons.Filled.QuestionMark,
                paymentType = getPaymentTypeName(1).uppercase(),
                categoryLabel = "Food".uppercase()
            )
        }
    }
}

