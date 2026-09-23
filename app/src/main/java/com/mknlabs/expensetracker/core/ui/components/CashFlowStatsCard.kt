package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.CashFlowDateTextDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowDateTextLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowExpenseAmountDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowExpenseAmountLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowIncomeAmountDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowIncomeAmountLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowLabelDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowLabelLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowNetBalanceAmountDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowNetBalanceAmountLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowNetBalanceBgDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowNetBalanceBgLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowNetBalanceBorderDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowNetBalanceBorderLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowNetBalanceLabelDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowNetBalanceLabelLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowPillBgDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowPillBgLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowPillBorderDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowPillBorderLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowPillTextDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowPillTextLight
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.feature.home.ui.CashFlowPeriod
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Returns the current date formatted as "15 SEP 2026" (uppercase).
 */
private fun currentDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date()).uppercase(Locale.getDefault())
}

/**
 * Redesigned CashFlow card with adaptive image background:
 * - Stretches and shrinks vertically/horizontally across devices and orientations
 * - Perfect 24.dp corner clipping
 * - Supports both Dark mode (bg_cashflow_dark) and Light mode (bg_cashflow_light)
 * - All internal content styles, formatting, dropdown selection, and privacy toggle preserved
 */
@Composable
fun CashFlowStatsCard(
    expense: String,
    income: String,
    netBalance: String,
    isBalanceHidden: Boolean = false,
    onToggleVisibility: () -> Unit = {},
    selectedPeriod: CashFlowPeriod = CashFlowPeriod.THIS_MONTH,
    onPeriodChanged: (CashFlowPeriod) -> Unit = {},
    yearExpense: String = "",
    yearIncome: String = "",
    yearNetBalance: String = "",
    dropdownExpanded: Boolean = false
) {
    val isDark = MaterialTheme.colorScheme.isDark

    // Dynamic current date that refreshes every minute
    var currentDate by remember { mutableStateOf(currentDateString()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentDate = currentDateString()
            delay(60_000L)
        }
    }

    // Decide which values to show based on selected period and privacy
    val displayExpense = if (selectedPeriod == CashFlowPeriod.THIS_YEAR) {
        if (isBalanceHidden) "****" else yearExpense
    } else {
        if (isBalanceHidden) "****" else expense
    }
    val displayIncome = if (selectedPeriod == CashFlowPeriod.THIS_YEAR) {
        if (isBalanceHidden) "****" else yearIncome
    } else {
        if (isBalanceHidden) "****" else income
    }
    val displayBalance = if (selectedPeriod == CashFlowPeriod.THIS_YEAR) {
        if (isBalanceHidden) "****" else yearNetBalance
    } else {
        if (isBalanceHidden) "****" else netBalance
    }

    var dropdownMenuExpanded by remember { mutableStateOf(dropdownExpanded) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onToggleVisibility)
    ) {
        // Adaptive background image that stretches and shrinks horizontally & vertically
        Image(
            painter = painterResource(
                id = if (isDark) R.drawable.bg_cashflow_dark else R.drawable.bg_cashflow_light
            ),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Row: Current Date + Period Dropdown Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Formatted Current Date (e.g. 15 SEP 2026)
                Text(
                    text = currentDate,
                    color = if (isDark) CashFlowDateTextDark else CashFlowDateTextLight,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.2.sp
                    )
                )

                // Period Selector Dropdown Pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isDark) CashFlowPillBgDark else CashFlowPillBgLight,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isDark) CashFlowPillBorderDark else CashFlowPillBorderLight
                        ),
                        modifier = Modifier.clickable { dropdownMenuExpanded = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (selectedPeriod == CashFlowPeriod.THIS_YEAR)
                                    stringResource(R.string.label_this_year_cash_flow)
                                else
                                    stringResource(R.string.label_this_month_cash_flow),
                                color = if (isDark) CashFlowPillTextDark else CashFlowPillTextLight,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = if (isDark) CashFlowPillTextDark else CashFlowPillTextLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownMenuExpanded,
                        onDismissRequest = { dropdownMenuExpanded = false },
                        offset = DpOffset(x = 0.dp, y = 8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        CashFlowPeriodOption(
                            label = stringResource(R.string.label_this_month_cash_flow),
                            selected = selectedPeriod == CashFlowPeriod.THIS_MONTH,
                            onClick = {
                                onPeriodChanged(CashFlowPeriod.THIS_MONTH)
                                dropdownMenuExpanded = false
                            },
                            isFirst = true
                        )
                        CashFlowPeriodOption(
                            label = stringResource(R.string.label_this_year_cash_flow),
                            selected = selectedPeriod == CashFlowPeriod.THIS_YEAR,
                            onClick = {
                                onPeriodChanged(CashFlowPeriod.THIS_YEAR)
                                dropdownMenuExpanded = false
                            },
                            isLast = true
                        )
                    }
                }
            }

            // Metrics Row: EXPENSE (Left) & INCOME (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // EXPENSE
                CashFlowMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_expense_cash_flow).uppercase(Locale.getDefault()),
                    amount = displayExpense,
                    labelColor = if (isDark) CashFlowLabelDark else CashFlowLabelLight,
                    amountColor = if (isDark) CashFlowExpenseAmountDark else CashFlowExpenseAmountLight,
                    textAlign = TextAlign.Start
                )

                // INCOME
                CashFlowMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_income_cash_flow).uppercase(Locale.getDefault()),
                    amount = displayIncome,
                    labelColor = if (isDark) CashFlowLabelDark else CashFlowLabelLight,
                    amountColor = if (isDark) CashFlowIncomeAmountDark else CashFlowIncomeAmountLight,
                    textAlign = TextAlign.End
                )
            }

            // Bottom Inset Container: Net Balance + Amount
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) CashFlowNetBalanceBgDark else CashFlowNetBalanceBgLight,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isDark) CashFlowNetBalanceBorderDark else CashFlowNetBalanceBorderLight
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_net_balance_cash_flow),
                            color = if (isDark) CashFlowNetBalanceLabelDark else CashFlowNetBalanceLabelLight,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )

                        // Eye button for privacy toggle
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onToggleVisibility),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBalanceHidden) {
                                    Icons.Rounded.Visibility
                                } else {
                                    Icons.Rounded.VisibilityOff
                                },
                                contentDescription = stringResource(
                                    if (isBalanceHidden) R.string.desc_show_balance
                                    else R.string.desc_hide_balance
                                ),
                                tint = if (isDark) CashFlowNetBalanceLabelDark.copy(alpha = 0.7f) else CashFlowNetBalanceLabelLight.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = displayBalance,
                        color = if (isDark) CashFlowNetBalanceAmountDark else CashFlowNetBalanceAmountLight,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Period dropdown item option.
 */
@Composable
private fun CashFlowPeriodOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Text(
        text = label,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = if (isFirst) 0.dp else 4.dp,
                bottom = if (isLast) 0.dp else 4.dp
            )
    )
}

/**
 * Individual metric display for Expense and Income.
 */
@Composable
private fun CashFlowMetric(
    modifier: Modifier = Modifier,
    label: String,
    amount: String,
    labelColor: Color,
    amountColor: Color,
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
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = amount,
            color = amountColor,
            textAlign = textAlign,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
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
                expense = "₹24,580",
                income = "₹62,400",
                netBalance = "₹37,820",
                yearExpense = "₹1,50,000",
                yearIncome = "₹5,00,000",
                yearNetBalance = "₹3,50,000"
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
                expense = "₹24,580",
                income = "₹62,400",
                netBalance = "₹37,820",
                yearExpense = "₹1,50,000",
                yearIncome = "₹5,00,000",
                yearNetBalance = "₹3,50,000"
            )
        }
    }
}

@Preview(showBackground = true, name = "Cash Flow Stats Card - Dropdown Open")
@Composable
private fun CashFlowStatsCardDropdownPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            CashFlowStatsCard(
                expense = "₹24,580",
                income = "₹62,400",
                netBalance = "₹37,820",
                yearExpense = "₹1,50,000",
                yearIncome = "₹5,00,000",
                yearNetBalance = "₹3,50,000",
                dropdownExpanded = true
            )
        }
    }
}
