package com.mknlabs.expensetracker.feature.analytics.ui

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.textTertiary

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.rememberBindAddFabToScroll
import com.mknlabs.expensetracker.core.ui.components.CurrentPeriodIndicator
import com.mknlabs.expensetracker.core.ui.components.hasCurrentPeriodIndicator
import com.mknlabs.expensetracker.core.ui.components.PeriodChip
import com.mknlabs.expensetracker.core.ui.components.PeriodChipDefaults

import com.adamglin.phosphoricons.regular.Wallet
import com.adamglin.phosphoricons.regular.TrendUp
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import java.util.Locale
import com.mknlabs.expensetracker.core.ui.components.DialogModeOption
import com.mknlabs.expensetracker.core.ui.components.DialogModeSelector
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.AccessStatus
import android.os.Build
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.chipInkOff

import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.flatCard
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.paymentColor
import com.mknlabs.expensetracker.core.ui.theme.glassHeroGradient
import com.mknlabs.expensetracker.core.ui.theme.heroBloom
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.textTertiary
import com.mknlabs.expensetracker.core.ui.theme.track
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.profileSheet

import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.core.ui.components.TransactionCard
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.monetization.AdPlacement
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun StatsRow(snapshot: AnalyticsSnapshotUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InsightStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.title_avg_daily),
            value = snapshot.avgDailyDisplay,
            delta = snapshot.dailyDeltaDisplay.asString(),
            deltaColor = if (snapshot.dailyDeltaPercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
            deltaBackground = (if (snapshot.dailyDeltaPercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense).copy(alpha = 0.15f),
            icon = PhosphorIcons.Regular.Wallet,
            iconFill = if (MaterialTheme.colorScheme.isDark) DailyAvgTileDark else DailyAvgTileLight,
            iconBorder = if (MaterialTheme.colorScheme.isDark) DailyAvgTileBorderDark else DailyAvgTileBorderLight,
            iconTint = if (MaterialTheme.colorScheme.isDark) DailyAvgIconDark else DailyAvgIconLight
        )
        InsightStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.title_savings),
            value = snapshot.savingsDisplay,
            delta = snapshot.savingsDeltaDisplay.asString(),
            deltaColor = if (snapshot.savingsDeltaPercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
            deltaBackground = (if (snapshot.savingsDeltaPercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense).copy(alpha = 0.15f),
            icon = PhosphorIcons.Regular.TrendUp,
            iconFill = if (MaterialTheme.colorScheme.isDark) SavingsTileDark else SavingsTileLight,
            iconBorder = if (MaterialTheme.colorScheme.isDark) SavingsTileBorderDark else SavingsTileBorderLight,
            iconTint = if (MaterialTheme.colorScheme.isDark) SavingsIconDark else SavingsIconLight
        )
    }
}

@Composable
internal fun InsightStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    delta: String,
    deltaColor: Color,
    deltaBackground: Color,
    iconFill: Color,
    iconBorder: Color,
    iconTint: Color,
    icon: ImageVector
) {
    val colorScheme = MaterialTheme.colorScheme
    // The tile is the mock's own accent: indigo for Daily Avg, violet/purple for Savings.
    val labelColor = colorScheme.onSurfaceVariant

    AppCard(
        modifier = modifier,
        shape = AppCardDefaults.shape(18.dp),
        colors = analyticsCardColors(),
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // The icon and the card's name share the top line, the name pushed to the card's
            // right edge; the amount and its percentage share the line below in the same
            // arrangement, so the name, the amount's percentage and the amount itself all line up
            // down the right of the card.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconFill)
                        .border(1.dp, iconBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = title,
                    color = labelColor,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    // Lifted clear of the icon's optical centre: centred on the 36dp badge the
                    // name read as if it belonged to the number below it, so it rides a few dp
                    // above that line and pairs with the icon instead.
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .offset(y = (-3).dp)
                )
            }

            // A quarter below the step the amount picks: headlineSmall / titleLarge /
            // titleMedium are 24 / 20 / 18sp, so the amount lands at 18 / 15 / 13.5.sp. The
            // line height shrinks with it, or the row keeps the taller line box and the
            // percentage floats away from the number it belongs to.
            val valueStyle = when {
                value.length > 10 -> MaterialTheme.typography.titleMedium
                value.length > 7 -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.headlineSmall
            }.copy(fontWeight = FontWeight.Bold).let {
                it.copy(fontSize = it.fontSize * 0.75f, lineHeight = it.lineHeight * 0.75f)
            }

            // The amount and the change in it share a line, the percentage hard against the
            // card's right edge. The amount is what gives up width if the pair is long, so the
            // pill stays whole: it ellipsizes rather than pushing the percentage out.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = valueStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(deltaBackground)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = delta,
                        color = deltaColor,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}


@Composable
internal fun CashFlowCard(snapshot: AnalyticsSnapshotUi) {
    val reveal = rememberShareReveal(snapshot.incomeFraction, snapshot.incomePercent)

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        colors = analyticsCardColors(),
        shape = AppCardDefaults.shape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Horizontal padding stays at the hero's 22.dp; the vertical pair is tighter than
                // the padding this card was built with (22.dp all round), which is most of what
                // its extra height was: the header, bar and foot sit close enough now that the
                // top and bottom no longer need the same inset as the sides.
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_cash_flow_ratio),
                    color = MaterialTheme.colorScheme.onSurface,
                    // labelSmall is the size of the ratio badge beside it; the title is a fifth
                    // larger (11.sp -> 13.2.sp), which keeps it leading the pill without the
                    // uneven jump back to titleMedium.
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 13.2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                )
                RatioBadge(snapshot.ratioDisplay)
            }
            Spacer(modifier = Modifier.height(14.dp))
            CashFlowBar(
                snapshot.incomeFraction,
                MaterialTheme.colorScheme.income,
                MaterialTheme.colorScheme.expense,
                progress = reveal
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Each side is annotated under the end of the segment it belongs to: the amount
            // first, then that side's percentage in brackets, hard left for income and hard
            // right for expense so the pair sits under the bar it describes.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                CashFlowStat(
                    amount = snapshot.incomeDisplay,
                    percent = (snapshot.incomePercent * reveal).roundToInt(),
                    accent = MaterialTheme.colorScheme.income,
                    modifier = Modifier.weight(1f)
                )
                CashFlowStat(
                    amount = snapshot.expenseDisplay,
                    percent = (snapshot.expensePercent * reveal).roundToInt(),
                    accent = MaterialTheme.colorScheme.expense,
                    modifier = Modifier.weight(1f),
                    alignEnd = true
                )
            }
        }
    }
}


/**
 * One side of the card's foot, read against the end of the bar segment it describes: the period's
 * total in that direction, then its share of the period in brackets. The half it sits in is what
 * puts it on the left or the right, so a short amount still reaches its own edge of the card.
 *
 * The amount is the only child allowed to give up width, and it ellipsizes rather than pushing the
 * percentage out: the share is the part a glance needs, because the totals are also printed in the
 * hero above.
 */
@Composable
internal fun CashFlowStat(
    amount: String,
    percent: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = amount,
            color = accent,
            // labelMedium, the size of the bracketed share beside it: the pair reads as one
            // figure, so only the weight separates the total from its percentage.
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(id = R.string.format_percent, percent.toString()),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}


@Composable
internal fun CashFlowBar(
    incomeFraction: Float,
    incomeColor: Color,
    expenseColor: Color,
    progress: Float = 1f
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fraction = incomeFraction.coerceIn(0f, 1f)
            val radius = CornerRadius(size.height / 2f, size.height / 2f)
            val gap = 4.dp.toPx()
            val showGap = fraction > 0f && fraction < 1f && size.width > gap
            val usable = if (showGap) size.width - gap else size.width
            val reveal = progress.coerceIn(0f, 1f)
            // Each share grows out of its own end of the bar — income from the left, expense from
            // the right — so the two fill towards their junction as the percentages count up. At
            // reveal = 1 the geometry is exactly the static one: the segments meet `gap` apart.
            val incomeWidth = usable * fraction * reveal
            val expenseWidth = usable * (1f - fraction) * reveal
            if (incomeWidth > 0f) {
                drawRoundRect(
                    color = incomeColor,
                    size = Size(incomeWidth, size.height),
                    cornerRadius = radius
                )
            }
            if (expenseWidth > 0f) {
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(size.width - expenseWidth, 0f),
                    size = Size(expenseWidth, size.height),
                    cornerRadius = radius
                )
            }
        }
    }
}
