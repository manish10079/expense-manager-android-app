package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
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
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.Eye
import com.adamglin.phosphoricons.regular.EyeSlash
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroBorderDark
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroBorderLight
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroDateText
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroExpense
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroIncome
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroInsetBg
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroInsetBorder
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroLabel
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroNetText
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroPillBg
import com.mknlabs.expensetracker.core.ui.theme.CashFlowHeroPillBorder
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.cashFlowHeroBaseBrush
import com.mknlabs.expensetracker.core.ui.theme.cashFlowHeroGlows
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
 * Cash Flow hero card.
 *
 * The dark card is the surface it has always been: a baked PNG stretched across the
 * whole card over a borderless 24dp shape. Light mode is the app's standard card — white
 * on the grey field, outlined and lifted — because the light palette is where "avoid
 * colourful backgrounds" applies; the hero's own gradient stays a dark-mode device.
 *
 * Everything inside is identical in both themes apart from the ink it reads.
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

    // One gradient recipe in both themes — a base violet ramp with two radial blooms —
    // so the hero reads as the same card recolored, not a gradient in dark and a flat
    // white card in light. The blooms need the surface size, so they ride on the card's
    // own background fill rather than being folded into its brush.
    val heroBase = cashFlowHeroBaseBrush()

    AppCard(
        onClick = onToggleVisibility,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        brush = heroBase,
        // The hero paints its own edge in both themes, so it supplies its own outline and
        // carries no lift; only its colour differs between light and dark.
        colors = AppCardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                1.dp,
                if (isDark) CashFlowHeroBorderDark else CashFlowHeroBorderLight
            )
        ),
        elevation = 0.dp
    ) {
        // The two blooms over the base ramp, clipped to the card's rounded shape by the
        // card itself, since this fill spans the card.
        Box(
            modifier = Modifier
                .matchParentSize()
                .cashFlowHeroGlows()
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
                    color = CashFlowHeroDateText,
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
                        color = CashFlowHeroPillBg,
                        border = BorderStroke(
                            width = 1.dp,
                            color = CashFlowHeroPillBorder
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
                                color = CashFlowHeroDateText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretDown,
                                contentDescription = null,
                                tint = CashFlowHeroDateText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownMenuExpanded,
                        onDismissRequest = { dropdownMenuExpanded = false },
                        offset = DpOffset(x = 0.dp, y = 8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp)),
                        // The popup is a surface in its own right, and Material builds it
                        // from surface-container roles this palette never defines - which
                        // on the light field renders with a lavender cast. Light paints it
                        // as the card it is: the specified white, flat, with the hairline
                        // edge and the card's own corner. Dark keeps the Material default
                        // it has always had.
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (isDark) MenuDefaults.containerColor else MaterialTheme.colorScheme.surface,
                        tonalElevation = if (isDark) MenuDefaults.TonalElevation else 0.dp,
                        border = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                    labelColor = CashFlowHeroLabel,
                    amountColor = CashFlowHeroExpense,
                    textAlign = TextAlign.Start
                )

                // INCOME
                CashFlowMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_income_cash_flow).uppercase(Locale.getDefault()),
                    amount = displayIncome,
                    labelColor = CashFlowHeroLabel,
                    amountColor = CashFlowHeroIncome,
                    textAlign = TextAlign.End
                )
            }

            // Bottom Inset Container: Net Balance + Amount
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CashFlowHeroInsetBg,
                border = BorderStroke(
                    width = 1.dp,
                    color = CashFlowHeroInsetBorder
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
                            color = CashFlowHeroLabel,
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
                                    PhosphorIcons.Regular.Eye
                                } else {
                                    PhosphorIcons.Regular.EyeSlash
                                },
                                contentDescription = stringResource(
                                    if (isBalanceHidden) R.string.desc_show_balance
                                    else R.string.desc_hide_balance
                                ),
                                tint = CashFlowHeroLabel.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = displayBalance,
                        color = CashFlowHeroNetText,
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
