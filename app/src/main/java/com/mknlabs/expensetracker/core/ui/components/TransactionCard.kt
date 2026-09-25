package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.rounded.Autorenew
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.theme.NeutralGray
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.getPaymentTypeName

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.transparent
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
    isProUser: Boolean = false,
    isRecurring: Boolean = false,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    // Selection keeps the filled primary container and the stronger edge it always had.
    // An unselected row was a transparent list row rather than a filled surface, and dark
    // mode is not part of this pass, so the dark fill stays clear; in light the row is a
    // card like any other, which is the whole point of the redesign.
    val baseColors = AppCardDefaults.colors()
    val cardColors = when {
        isSelected -> baseColors.copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )
        MaterialTheme.colorScheme.isDark -> baseColors.copy(containerColor = transparent)
        else -> baseColors
    }

    // Hoisted string resources: resolved once per card slot (cached across recompositions)
    // instead of inside the per-pill branch on every composition. Keyed on the Resources
    // instance so a runtime locale/config change invalidates the cached strings.
    val resources = LocalContext.current.resources
    val incomeLabel = remember(resources) { resources.getString(R.string.label_income) }
    val expenseLabel = remember(resources) { resources.getString(R.string.label_expense) }
    val noNoteLabel = remember(resources) { resources.getString(R.string.label_no_note) }

    // Row 1 binds the note and the amount to one style, row 2 binds every pill and the
    // date·time to another, so neither pair can drift apart in size or weight.
    val titleStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
    val metaStyle = MaterialTheme.typography.labelSmall

    AppCard(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                // True whenever the note is not fully readable on the card. The
                // full-note info icon is offered exactly then, for every card
                // customization.
                //
                // Two signals, because neither alone covers every way a note can hide
                // text: the ellipsis drawn on the rendered line catches a long
                // single-line note, which is clipped without ever dropping a line; and
                // the line-count comparison below catches a note whose first line fits
                // the width but which still holds more lines, so nothing ellipsizes.
                val noteLineCount = remember(displayNote) { displayNote.count { it == '\n' } + 1 }
                var noteTruncated by remember(displayNote) { mutableStateOf(false) }
                val noteTooltipState = rememberTooltipState()
                val noteTooltipScope = rememberCoroutineScope()

                // Row 1 — the note shares this row with the amount. The amount is
                // deliberately unweighted, so it is measured at its full intrinsic
                // width before the note takes the remainder: it can never be truncated
                // and the note is always what yields.
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        // The empty-note placeholder keeps its gray italic treatment —
                        // only the colour and slant differ, never the size or weight.
                        style = if (isNoteEmpty) {
                            titleStyle.copy(
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Italic
                            )
                        } else {
                            titleStyle
                        },
                        // The note claims the whole slot left over after the amount is
                        // measured, which is what keeps the amount flush right on cards
                        // with a short note. The full-note info icon lives on the meta row
                        // below, so it never competes with the note for this width.
                        modifier = Modifier.weight(1f),
                        onTextLayout = { result ->
                            noteTruncated = result.isLineEllipsized(0) ||
                                result.didOverflowWidth ||
                                result.lineCount < noteLineCount
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = amount,
                        color = if (transactionTypeId == 1) MaterialTheme.colorScheme.income else Color.White,
                        maxLines = 1,
                        softWrap = false,
                        style = titleStyle
                    )
                }

                val hasMetaRow = showTypeLabel ||
                    (showCategoryLabel && categoryLabel.isNotBlank()) ||
                    (showPaymentMethod && paymentType.isNotBlank()) ||
                    showTransactionDate ||
                    showTransactionTime

                // The icon is deliberately not gated on hasMetaRow: the note's full-text
                // tooltip must survive a user hiding every pill and the date, so the row
                // below stays alive whenever there is an icon to place on it.
                val showNoteInfo = showNoteTooltip && noteTruncated && !isNoteEmpty

                // Row 2 — the payment, income/expense and category pills on the left, the
                // date·time pinned to the right edge. The pills live in their own
                // horizontally scrollable strip so a long category name can never push or
                // squeeze the date, and the date is unweighted for the same reason the
                // amount is in row 1.
                //
                // The strip scrolls only while the pills actually overflow. It has to stay
                // inert otherwise, or it would swallow the horizontal drag that the list
                // row's swipe-to-duplicate/delete gesture relies on. Children of a
                // scrollable are measured with an unbounded max width, so the inner Row's
                // own size *is* its full content width — which is what makes the overflow
                // comparison below possible without measuring the text twice.
                // The gap between the note/amount row and the meta row. Kept just wide
                // enough to read as two distinct lines without loosening the card.
                if (hasMetaRow || showNoteInfo) {
                    Spacer(modifier = Modifier.height(7.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pillScrollState = rememberScrollState()
                        var pillContentWidth by remember { mutableIntStateOf(0) }

                        // The strip still fills the free space, which is what keeps the
                        // date·time pinned to the right edge. The pills sit one level down,
                        // in a column that claims only its own content width, so the info
                        // icon can ride along right after the last pill instead of being
                        // pushed out to the card's edge — one pill, two pills or none, the
                        // icon simply follows whatever is there.
                        //
                        // The icon stays outside the scroll region on purpose: overflow
                        // scrolls the pills, never the icon.
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BoxWithConstraints(
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                val viewportWidth = constraints.maxWidth
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(
                                            state = pillScrollState,
                                            enabled = pillContentWidth > viewportWidth
                                        )
                                        .onSizeChanged { pillContentWidth = it.width },
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (showPaymentMethod && paymentType.isNotBlank()) {
                                        TransactionPill(
                                            text = paymentType,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                                            style = metaStyle
                                        )
                                    }

                                    if (showTypeLabel) {
                                        TransactionPill(
                                            text = if (transactionTypeId == 1) incomeLabel else expenseLabel,
                                            color = if (transactionTypeId == 1) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
                                            backgroundColor = if (transactionTypeId == 1) MaterialTheme.colorScheme.income.copy(alpha = 0.12f) else MaterialTheme.colorScheme.expense.copy(alpha = 0.12f),
                                            style = metaStyle
                                        )
                                    }

                                    if (showCategoryLabel && categoryLabel.isNotBlank()) {
                                        TransactionPill(
                                            text = categoryLabel,
                                            color = MaterialTheme.colorScheme.primary,
                                            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                            style = metaStyle
                                        )
                                    }
                                }
                            }

                            // The full-note icon sits immediately after the last pill, so
                            // it reads as the tail of the pill run. The note text itself
                            // keeps its default behavior — tap opens the transaction,
                            // long-press enters multi-select — only the icon carries the
                            // tooltip.
                            if (showNoteInfo) {
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
                                    // The 16.dp glyph is centred in a 24.dp hit target, so a
                                    // 2.dp start padding leaves 6.dp of clear space before the
                                    // glyph — the same gap the pills keep between each other
                                    // via spacedBy, which is what makes the icon read as the
                                    // tail of the pill run rather than a separate element.
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 2.dp)
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
                            Spacer(modifier = Modifier.width(8.dp))

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
                                softWrap = false,
                                style = metaStyle
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))
        }

        if (isProUser && isRecurring) {
            val recurringTooltipState = rememberTooltipState()
            val recurringTooltipScope = rememberCoroutineScope()

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = {
                    PlainTooltip {
                        Text(
                            text = stringResource(R.string.label_recurring_transaction),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                state = recurringTooltipState,
                onDismissRequest = { recurringTooltipState.dismiss() },
                enableUserInput = false,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 8.dp)
                    .offset(x = 50.dp, y  =50.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .clickable {
                            recurringTooltipScope.launch {
                                if (recurringTooltipState.isVisible) {
                                    recurringTooltipState.dismiss()
                                } else {
                                    recurringTooltipState.show()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Autorenew,
                        contentDescription = stringResource(R.string.label_recurring_transaction),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

/**
 * Small label pill for the card's secondary row (type / category / payment).
 *
 * Carries a little vertical padding around the label's own line box, so the pill reads as a
 * filled chip instead of a tight rectangle that hugs the text. The type size still arrives
 * as [style] rather than a hardcoded height, so the pills and the card's date·time line — a
 * plain single-line `labelSmall` Text — keep one source of truth and scale together with the
 * user's font size, including Android 14+ non-linear font scaling.
 *
 * The corner radius stays a small constant so the silhouette stays crisp at the compact
 * size rather than turning into a lozenge.
 */
@Composable
private fun TransactionPill(
    text: String,
    color: Color,
    backgroundColor: Color,
    style: TextStyle
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            maxLines = 1,
            softWrap = false,
            style = style
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
                categoryLabel = "Salary".uppercase(),
                isProUser = true,
                isRecurring = true
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
                categoryLabel = "Food".uppercase(),
                isProUser = true,
                isRecurring = true
            )
        }
    }
}

/**
 * Narrow viewport with a note, an amount and three pills that all want more room than
 * they get: the note ellipsises, the amount stays whole, and the pill strip is wide
 * enough to scroll. Composed with [TransactionCardWidePreview] below, the pair shows
 * that the available width — not any hardcoded character count — decides the cut.
 */
@Preview(showBackground = true, widthDp = 380)
@Composable
fun TransactionCardLongContentPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            TransactionCard(
                note = "Monthly grocery run at the neighbourhood supermarket with the family",
                transactionDate = "31 Dec",
                transactionTime = formatTime(1738368000000, "12-hour"),
                amount = "-₹1,24,567.00",
                transactionTypeId = 2,
                icon = Icons.Filled.QuestionMark,
                paymentType = getPaymentTypeName(1).uppercase(),
                categoryLabel = "Groceries And Household Supplies".uppercase(),
                isProUser = true,
                isRecurring = true
            )
        }
    }
}

/**
 * The same content at a landscape/tablet width. The note is handed a wider slot, so more
 * characters survive before the ellipsis while the amount and the date keep their edges —
 * no orientation branch anywhere.
 */
@Preview(showBackground = true, widthDp = 720)
@Composable
fun TransactionCardWidePreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            TransactionCard(
                note = "Monthly grocery run at the neighbourhood supermarket with the family",
                transactionDate = "31 Dec",
                transactionTime = formatTime(1738368000000, "12-hour"),
                amount = "-₹1,24,567.00",
                transactionTypeId = 2,
                icon = Icons.Filled.QuestionMark,
                paymentType = getPaymentTypeName(1).uppercase(),
                categoryLabel = "Groceries And Household Supplies".uppercase(),
                isProUser = true,
                isRecurring = true
            )
        }
    }
}

/**
 * The two degenerate paths: no category icon (the text column starts at the card's own
 * padding), a blank note (the gray italic placeholder, still the same size as the amount)
 * and only a single pill, so row 2 sits well inside the width.
 */
@Preview(showBackground = true, widthDp = 380)
@Composable
fun TransactionCardMinimalPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            TransactionCard(
                note = "",
                transactionDate = "Today",
                transactionTime = formatTime(1738368000000, "12-hour"),
                amount = "₹1,200",
                transactionTypeId = 2,
                icon = Icons.Filled.QuestionMark,
                paymentType = getPaymentTypeName(3).uppercase(),
                categoryLabel = "",
                showCategoryIcon = false,
                showTypeLabel = false,
                isProUser = false
            )
        }
    }
}

/**
 * The truncated note of a Pro user who switched every pill and the date·time off: row 2
 * has nothing left but the info icon, which is the point of keeping that row alive rather
 * than folding the icon into the note row.
 */
@Preview(showBackground = true, widthDp = 380)
@Composable
fun TransactionCardInfoIconOnlyPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            TransactionCard(
                note = "Monthly grocery run at the neighbourhood supermarket with the family",
                transactionDate = "",
                transactionTime = "",
                amount = "-₹1,24,567.00",
                transactionTypeId = 2,
                icon = Icons.Filled.QuestionMark,
                paymentType = "",
                categoryLabel = "",
                showTypeLabel = false,
                showTransactionDate = false,
                showPaymentMethod = false,
                showTransactionTime = false,
                showCategoryLabel = false,
                isProUser = true
            )
        }
    }
}

