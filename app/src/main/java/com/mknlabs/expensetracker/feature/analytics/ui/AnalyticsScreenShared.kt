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

// Flat chrome for the analytics cards, the same surface the home quick cards use:
// bg-surface #13151F with a white/5 edge in dark, white with a slate-200/70 edge in light.
// Applied to the stats, breakdowns, top-spending and smart-tip cards — not the hero above
// them, which keeps its own gradient.
internal val AnalyticsCardBorderDark = Color(0x0DFFFFFF)
internal val AnalyticsCardBorderLight = Color(0xB3E2E8F0)

// The Daily Avg and Savings tiles, from the mock's quick stats: indigo-50 fill, indigo-100/60
// edge and indigo-600 glyph in light; indigo-500/15, /30 and indigo-400 in dark. Savings
// steps to violet-50 / violet-100/60 / violet-600 in light and purple-500 tints with
// purple-400 in dark.
internal val DailyAvgTileDark = Color(0x266366F1)
internal val DailyAvgTileBorderDark = Color(0x4D6366F1)
internal val DailyAvgIconDark = Color(0xFF818CF8)
internal val DailyAvgTileLight = Color(0xFFEEF2FF)
internal val DailyAvgTileBorderLight = Color(0x99E0E7FF)
internal val DailyAvgIconLight = Color(0xFF4F46E5)

internal val SavingsTileDark = Color(0x26A855F7)
internal val SavingsTileBorderDark = Color(0x4DA855F7)
internal val SavingsIconDark = Color(0xFFC084FC)
internal val SavingsTileLight = Color(0xFFF5F3FF)
internal val SavingsTileBorderLight = Color(0x99EDE9FE)
internal val SavingsIconLight = Color(0xFF7C3AED)

@Composable
internal fun analyticsCardColors(): AppCardColors = AppCardColors(
    containerColor = MaterialTheme.colorScheme.flatCard,
    contentColor = MaterialTheme.colorScheme.onSurface,
    border = BorderStroke(
        width = 1.dp,
        color = if (MaterialTheme.colorScheme.isDark) {
            AnalyticsCardBorderDark
        } else {
            AnalyticsCardBorderLight
        }
    )
)

@Composable
internal fun AnalyticsSectionRow(
    isWide: Boolean,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    if (isWide) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { first() }
            Box(modifier = Modifier.weight(1f)) { second() }
        }
    } else {
        // The compact branch owns its own Column, because it emits more than one card and the
        // caller wraps this in a Box — which stacks its children at a single origin. Emitted
        // bare, the two cards were drawn on top of each other: the first one's title was not
        // even in the accessibility tree, and only the second card was readable at all.
        Column(modifier = Modifier.fillMaxWidth()) {
            first()
            Spacer(modifier = Modifier.height(18.dp))
            second()
        }
    }
}

/**
 * The reveal every share bar on analytics plays: a 0..1 value the bar multiplies its fill by, so
 * the bar and the percentage that labels it arrive together instead of the number snapping in
 * above an empty track.
 *
 * Keyed on the values it is passed, and only those, so it replays when the data behind a bar
 * changes — and not when the card recomposes for some unrelated reason.
 */
@Composable
internal fun rememberShareReveal(vararg keys: Any?): Float {
    val reveal = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(*keys) {
        reveal.snapTo(0f)
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }
    return reveal.value
}

/**
 * The single-colour share bar the breakdown sheets print under each row. Its fill is scaled by
 * [progress] so it grows in from the left on the same reveal the cash flow card's two-segment bar
 * uses; at `progress = 1` it is the plain fraction it displays.
 */
@Composable
internal fun ShareBar(fraction: Float, color: Color, progress: Float = 1f) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((fraction * progress).coerceIn(0f, 1f))
                .height(8.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/** The period's income to expense ratio, normalised to income = 1; see [cashFlowSplit]. */
@Composable
internal fun RatioBadge(ratio: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = stringResource(id = R.string.format_cash_flow_ratio, ratio),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

/**
 * The colour of a category's slice, legend dot and progress bar: the user's own if they picked
 * one, otherwise the palette entry for that category's id.
 *
 * **This replaces the positional ramp, and the trade-off it accepted is worth knowing.** The ramp
 * gave every slice a colour by its rank, which guaranteed that two adjacent slices differed. The
 * palette repeats hues on purpose — Travel, Insurance and Transport are all `#0288D1`, and Bills,
 * Groceries and Donations all `#059669` — so two adjacent slices can now be the same colour.
 *
 * That is acceptable here and only here, because the legend under the donut prints a text label
 * for every dot: identity comes from the name, and the colour's job is to match the slice to a
 * category the user already recognises from the transactions list. A chart whose only key was the
 * colour could not afford this; this one is not.
 *
 * It also cannot wrap or run out the way the ramp could. A category id is either in the palette or
 * it is not, and the not-in case takes the brand fallback ink rather than a recycled hue.
 */
@Composable
internal fun categoryBreakdownColor(category: CategoryBreakdownUi): Color =
    MaterialTheme.colorScheme.categoryColor(
        categoryId = category.id,
        colorHex = category.colorHex
    )

/**
 * The same lookup as [categoryBreakdownColor], against the payment palette.
 *
 * Kept as its own function rather than passed a palette in, because payment ids restart at 1: id
 * 1 is both Food and UPI, so the domain has to be decided here and not at the call site.
 */
@Composable
internal fun paymentBreakdownColor(item: PaymentTypeBreakdownUi): Color =
    MaterialTheme.colorScheme.paymentColor(
        paymentId = item.id,
        colorHex = item.colorHex
    )

@Composable
internal fun BoxScope.PremiumLockedOverlay(
    modifier: Modifier = Modifier,
    displayText: String,
    icon: ImageVector = Icons.Filled.Lock,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .matchParentSize()
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.54f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.featureGateLock,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = displayText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
internal fun resolveSummaryLabel(label: SummaryLabelUi): String {
    return when {
        label.resId != null -> stringResource(id = label.resId)
        label.patternResId != null && label.timestamp != null -> {
            formatDate(label.timestamp, stringResource(id = label.patternResId))
        }
        label.customRange != null -> label.customRange.asString()
        else -> ""
    }
}

/**
 * Locked analytics cards keep their silhouette. On Android 12+ (API 31+), content is veiled
 * with a Haze frosted glass blur effect. On Android 11 and lower (API <= 30), an opaque surface
 * background is used as a fallback so older devices hide the card content cleanly.
 */
@Composable
internal fun GatedCardContent(
    isLocked: Boolean,
    card: @Composable (Modifier) -> Unit
) {
    if (isLocked) {
        val hazeSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val hazeState = remember { HazeState() }
        val overlayShape = RoundedCornerShape(30.dp)

        Box {
            card(
                if (hazeSupported) {
                    Modifier.hazeSource(state = hazeState)
                } else {
                    Modifier
                }
            )
            if (hazeSupported) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(overlayShape)
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                blurRadius = 24.dp,
                                noiseFactor = 0.03f,
                                tints = emptyList()
                            )
                        )
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.20f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(overlayShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    } else {
        card(Modifier)
    }
}

@Composable
internal fun resolveSmartTip(tip: SmartTipUi): String {
    return if (tip.resId == R.string.msg_spending_trend) {
        stringResource(
            id = tip.resId,
            tip.flowChange?.asString() ?: "",
            tip.directionResId?.let { stringResource(id = it) } ?: "",
            tip.topCategory ?: stringResource(id = R.string.label_spending),
            tip.savingAmount ?: ""
        )
    } else {
        stringResource(id = tip.resId)
    }
}

@Composable
internal fun resolveChartLabel(label: ChartLabelUi): String {
    return when {
        label.resId != null && label.index != null -> {
            val array = stringArrayResource(id = label.resId)
            if (label.index in array.indices) array[label.index] else ""
        }
        else -> label.label.asString()
    }
}
