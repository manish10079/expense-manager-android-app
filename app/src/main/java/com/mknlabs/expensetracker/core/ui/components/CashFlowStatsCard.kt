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
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Lucide
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
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.menu
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme




import com.mknlabs.expensetracker.core.ui.theme.TextPrimaryDark
import com.mknlabs.expensetracker.core.ui.theme.TextPrimaryLight
import com.mknlabs.expensetracker.core.ui.theme.TextSecondaryDark
import com.mknlabs.expensetracker.core.ui.theme.TextSecondaryLight
import com.mknlabs.expensetracker.core.ui.theme.TextTertiaryDark
import com.mknlabs.expensetracker.core.ui.theme.TextTertiaryLight

import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.feature.home.ui.CashFlowPeriod
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Returns the current date formatted as "27 Sep 2026" (title-case month).
 */
private fun currentDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    return sdf.format(Date())
}

private fun currentYearString(): String {
    return SimpleDateFormat("yyyy", Locale.ENGLISH).format(Date())
}
// Cash Flow hero background, quoted from the homescreen redesign mock
// (homescree-redesign-2.html). The ramp, edge, shadow and both corner flares are one
// recipe and live here on purpose: no other surface in the app paints this card.
private val HeroCardStartDark = Color(0xFF1A1D2B)        // from-surface-secondary
private val HeroCardMidDark = Color(0xFF191929)          // via-[#191929]
private val HeroCardEndDark = Color(0xFF121320)          // to-[#121320]
private val HeroCardStartLight = Color(0xFFFFFFFF)       // from-white
private val HeroCardMidLight = Color(0xFFF8FAFF)         // via-[#F8FAFF]
private val HeroCardEndLight = Color(0xFFEEF2FF)         // to-[#EEF2FF]

private val HeroCardBorderDark = Color(0x1AFFFFFF)       // border-white/10
private val HeroCardBorderLight = Color(0xCCE0E7FF)      // border-indigo-100/80
private val HeroCardShadowDark = Color(0x99000000)       // 0 12px 32px rgba(0,0,0,0.6)
private val HeroCardShadowLight = Color(0x80E2E8F0)      // shadow-slate-200/50

private val HeroCardFlarePurpleDark = Color(0x337C3AED)  // accent-purple/20
private val HeroCardFlareIndigoDark = Color(0x266366F1)  // accent-indigo/15
private val HeroCardFlareVioletLight = Color(0x1A8B5CF6) // violet-500/10
private val HeroCardFlareIndigoLight = Color(0x1A6366F1) // indigo-500/10

// Calendar glyph beside the date: text-slate-400 in dark, text-slate-500 in light.
private val HeroDateIconDark = Color(0xFF94A3B8)
private val HeroDateIconLight = Color(0xFF64748B)
// Period chip, quoted from the mock: white/5 fill, white/10 edge and slate-200 label
// in dark; white fill, slate-200/80 edge and slate-700 label in light. The caret is one
// step quieter in each theme (slate-400 / slate-500).
private val HeroPeriodChipDark = Color(0x0DFFFFFF)        // bg-white/5
private val HeroPeriodChipLight = Color(0xFFFFFFFF)       // bg-white
private val HeroPeriodChipBorderDark = Color(0x1AFFFFFF)  // border-white/10
private val HeroPeriodChipBorderLight = Color(0xCCE2E8F0) // border-slate-200/80
private val HeroPeriodChipTextDark = Color(0xFFE2E8F0)    // text-slate-200
private val HeroPeriodChipTextLight = Color(0xFF334155)   // text-slate-700
private val HeroPeriodChipCaretDark = Color(0xFF94A3B8)   // text-slate-400
private val HeroPeriodChipCaretLight = Color(0xFF64748B)  // text-slate-500
// Net Balance inset module, quoted from the mock: black/35 over the dark card with a
// white/5 edge, and white/90 over the light card with a slate-100 edge and shadow-sm.
private val HeroNetInsetDark = Color(0x59000000)         // bg-black/35
private val HeroNetInsetLight = Color(0xE6FFFFFF)        // bg-white/90
private val HeroNetInsetBorderDark = Color(0x0DFFFFFF)   // border-white/5
private val HeroNetInsetBorderLight = Color(0xFFF1F5F9)  // border-slate-100

private fun heroCardGradient(isDark: Boolean): Brush =
    Brush.linearGradient(
        // to-br in the mock: the default top-left to bottom-right axis.
        colors = if (isDark) {
            listOf(HeroCardStartDark, HeroCardMidDark, HeroCardEndDark)
        } else {
            listOf(HeroCardStartLight, HeroCardMidLight, HeroCardEndLight)
        }
    )

/** The mock's two blurred corner washes, drawn as radial gradients scaled to the card. */
private fun Modifier.heroCardFlares(isDark: Boolean): Modifier = drawWithCache {
    val topRight = Brush.radialGradient(
        colors = listOf(
            if (isDark) HeroCardFlarePurpleDark else HeroCardFlareVioletLight,
            Color.Transparent
        ),
        center = Offset(size.width * 0.92f, -size.height * 0.12f),
        radius = size.width * 0.55f
    )
    val bottomLeft = Brush.radialGradient(
        colors = listOf(
            if (isDark) HeroCardFlareIndigoDark else HeroCardFlareIndigoLight,
            Color.Transparent
        ),
        center = Offset(-size.width * 0.08f, size.height * 1.05f),
        radius = size.width * 0.45f
    )
    onDrawBehind {
        drawRect(brush = topRight)
        drawRect(brush = bottomLeft)
    }
}

/**
 * Cash Flow hero card.
 *
 * The surface is the mock's own hero: a to-br ramp (dark #1A1D2B → #191929 → #121320,
 * light white → #F8FAFF → #EEF2FF), the matching hairline, a soft outer lift and two
 * blurred corner washes. Those tokens are private to this file so the recipe cannot leak
 * into other cards.
 *
 * Everything inside is identical in both themes apart from the ink it reads, and that ink
 * is the app's shared text and semantic tokens rather than a set of the hero's own.
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

    val cardShape = RoundedCornerShape(AppCardDefaults.CornerRadius)
    AppCard(
        onClick = onToggleVisibility,
        modifier = Modifier
            .fillMaxWidth()
            // The mock's hero lift: a deep black shadow in dark, a soft slate one in light.
            .shadow(
                elevation = if (isDark) 12.dp else 6.dp,
                shape = cardShape,
                clip = false,
                ambientColor = if (isDark) HeroCardShadowDark else HeroCardShadowLight,
                spotColor = if (isDark) HeroCardShadowDark else HeroCardShadowLight
            ),
        shape = cardShape,
        // The mock's to-br ramp replaces the old neutral flat fill.
        colors = AppCardColors(
            containerColor = if (isDark) HeroCardStartDark else HeroCardStartLight,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                1.dp,
                if (isDark) HeroCardBorderDark else HeroCardBorderLight
            )
        ),
        brush = heroCardGradient(isDark),
        elevation = 0.dp
    ) {
        // The mock's two corner washes sit behind the content, clipped to the card shape.
        Box(
            modifier = Modifier
                .matchParentSize()
                .heroCardFlares(isDark)
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
                // Formatted Current Date (e.g. 27 Sep 2026), preceded by the mock's
                // calendar glyph.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Calendar,
                        contentDescription = null,
                        tint = if (isDark) HeroDateIconDark else HeroDateIconLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (selectedPeriod == CashFlowPeriod.THIS_YEAR) currentYearString() else currentDate,
                        color = if (isDark) TextSecondaryDark else TextSecondaryLight,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    )
                }

                // Period Selector Dropdown Pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isDark) HeroPeriodChipDark else HeroPeriodChipLight,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isDark) HeroPeriodChipBorderDark else HeroPeriodChipBorderLight
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
                                color = if (isDark) HeroPeriodChipTextDark else HeroPeriodChipTextLight,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretDown,
                                contentDescription = null,
                                tint = if (isDark) HeroPeriodChipCaretDark else HeroPeriodChipCaretLight,
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
                        containerColor = MaterialTheme.colorScheme.menu,
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
                    label = stringResource(R.string.label_expense_cash_flow),
                    amount = displayExpense,
                    labelColor = if (isDark) TextTertiaryDark else TextTertiaryLight,
                    amountColor = MaterialTheme.colorScheme.expense,
                    textAlign = TextAlign.Start
                )

                // INCOME
                CashFlowMetric(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.label_income_cash_flow),
                    amount = displayIncome,
                    labelColor = if (isDark) TextTertiaryDark else TextTertiaryLight,
                    amountColor = MaterialTheme.colorScheme.income,
                    textAlign = TextAlign.End
                )
            }

            // Bottom Inset Container: Net Balance + Amount
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) HeroNetInsetDark else HeroNetInsetLight,
                shadowElevation = if (isDark) 0.dp else 2.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isDark) HeroNetInsetBorderDark else HeroNetInsetBorderLight
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
                            color = if (isDark) TextTertiaryDark else TextTertiaryLight,
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
                                tint = if (isDark) TextSecondaryDark else TextSecondaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = displayBalance,
                        color = if (isDark) TextPrimaryDark else TextPrimaryLight,
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
            MaterialTheme.colorScheme.accentInk
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
                fontSize = 12.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = amount,
            color = amountColor,
            textAlign = textAlign,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (amount == "****") 26.sp else 23.4.sp
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
