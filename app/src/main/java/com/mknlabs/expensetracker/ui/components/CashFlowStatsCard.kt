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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import com.mknlabs.expensetracker.ui.theme.ExpenseRed
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.IncomeGreen
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkCenter
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkEnd
import com.mknlabs.expensetracker.ui.theme.PremiumCardDarkStart
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightCenter
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightEnd
import com.mknlabs.expensetracker.ui.theme.PremiumCardLightStart
import com.mknlabs.expensetracker.ui.theme.isDark
import com.mknlabs.expensetracker.ui.viewmodels.CashFlowPeriod
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Returns the current date formatted as "12 Sep 2026".
 */
private fun currentDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date())
}

/**
 * Home-screen "Cash Flow" stats card: the current date as the header,
 * This Month / This Year period selector, expense/income metrics, and
 * the Net Balance inner container.
 *
 * [isBalanceHidden] / [onToggleVisibility] preserve the existing privacy
 * auto-hide behavior: amounts are masked with "****" while hidden and the
 * whole card toggles visibility when tapped.
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
    val colorScheme = MaterialTheme.colorScheme

    // Dynamic current date that updates every minute
    var currentDate by remember { mutableStateOf(currentDateString()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentDate = currentDateString()
            delay(60_000L) // refresh every minute
        }
    }

    // Theme-aware gradient background
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

    // Decide which values to show based on selected period
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

    var dropdownExpanded by remember { mutableStateOf(dropdownExpanded) }

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
            // Header Row: Current date + Period dropdown pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dynamic current date pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    contentColor = colorScheme.primary
                ) {
                    Text(
                        text = currentDate,
                        modifier = Modifier.padding(start = 10.dp, top = 6.dp, bottom = 6.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // Period selector pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        contentColor = colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { dropdownExpanded = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (selectedPeriod == CashFlowPeriod.THIS_YEAR)
                                    stringResource(R.string.label_this_year_cash_flow)
                                else
                                    stringResource(R.string.label_this_month_cash_flow),
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

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Box(modifier = Modifier.padding(start = 16.dp, top = 0.dp, bottom = 0.dp)) {
                                    Text(
                                        stringResource(R.string.label_this_month_cash_flow),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            },
                            onClick = {
                                onPeriodChanged(CashFlowPeriod.THIS_MONTH)
                                dropdownExpanded = false
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = MenuDefaults.itemColors(
                                textColor = if (selectedPeriod == CashFlowPeriod.THIS_MONTH)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        )
                        DropdownMenuItem(
                            text = {
                                Box(modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp)) {
                                    Text(
                                        stringResource(R.string.label_this_year_cash_flow),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            },
                            onClick = {
                                onPeriodChanged(CashFlowPeriod.THIS_YEAR)
                                dropdownExpanded = false
                            },
                            contentPadding = PaddingValues(0.dp),
                            colors = MenuDefaults.itemColors(
                                textColor = if (selectedPeriod == CashFlowPeriod.THIS_YEAR)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            // Metrics Row: Expense | Income (two equal halves)
            Row(modifier = Modifier.fillMaxWidth()) {
                CashFlowMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_expense_cash_flow),
                    amount = displayExpense,
                    labelColor = ExpenseRed,
                    amountColor = ExpenseRed,
                    textAlign = TextAlign.Start
                )
                CashFlowMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_income_cash_flow),
                    amount = displayIncome,
                    labelColor = IncomeGreen,
                    amountColor = IncomeGreen,
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
                    Text(
                        text = stringResource(R.string.label_net_balance_cash_flow),
                        color = colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )

                    Text(
                        text = displayBalance,
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
                fontSize = 13.sp,
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
                fontSize = 22.sp
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
                netBalance = "-₹1,200",
                yearExpense = "₹15,000",
                yearIncome = "₹50,000",
                yearNetBalance = "₹35,000"
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
                netBalance = "-₹1,200",
                yearExpense = "₹15,000",
                yearIncome = "₹50,000",
                yearNetBalance = "₹35,000"
            )
        }
    }
}

@Preview(showBackground = true, name = "Cash Flow Stats Card - Dropdown Open")
@Composable
private fun CashFlowStatsCardDropdownPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            CashFlowStatsCard(
                expense = "₹1,200",
                income = "₹500",
                netBalance = "-₹700",
                yearExpense = "₹15,000",
                yearIncome = "₹50,000",
                yearNetBalance = "₹35,000",
                dropdownExpanded = true
            )
        }
    }
}
