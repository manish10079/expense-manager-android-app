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
internal fun CustomRangeSelector(
    selectedPeriod: AnalyticsPeriod,
    customRange: LongRange?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selectedPeriod == AnalyticsPeriod.CUSTOM

    // The same five spec chip tokens PeriodChip reads, so the custom-range control and the
    // period chips beside it cannot disagree about what a selected chip looks like.
    val containerColor = if (isSelected) {
        brandGradient()
    } else {
        SolidColor(PeriodChipDefaults.customRangeContainer)
    }

    val borderColor = if (isSelected) {
        Color.Transparent
    } else {
        PeriodChipDefaults.customRangeBorder
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onBrandGradient
    } else {
        MaterialTheme.colorScheme.chipInkOff
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(18.dp))
                .background(containerColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            GatedAction(
                feature = Feature.ANALYTICS_CUSTOM_RANGE,
                displayName = stringResource(id = R.string.desc_custom_range),
                onAction = onClick
            ) { status, gatedOnClick ->
                val isLocked = status !is AccessStatus.Granted

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            // Names the control for screen readers. The calendar icon used to
                            // carry this label, and with the icon gone the visible text is
                            // either the selected range or the placeholder, so the action is
                            // stated here instead of being lost.
                            onClickLabel = stringResource(id = R.string.desc_custom_range)
                        ) { if (isLocked) gatedOnClick() else onClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    // No calendar icon: it was removed to buy the width that lets this
                    // pill share a line with the three period chips.
                    Text(
                        text = customRange?.let { formatCustomRangeLabel(it) } ?: stringResource(id = R.string.desc_custom_range),
                        color = textColor,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = stringResource(id = R.string.desc_locked),
                            tint = MaterialTheme.colorScheme.featureGateLock,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        if (selectedPeriod == AnalyticsPeriod.CUSTOM && customRange != null) {
            Text(
                text = stringResource(id = R.string.label_clear),
                color = MaterialTheme.colorScheme.accentInk,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onClear)
            )
        }
    }
}

@Composable
internal fun HeroAnalyticsSection(
    snapshot: AnalyticsSnapshotUi,
    displayMode: HeroDisplayMode,
    onDisplayModeChange: (HeroDisplayMode) -> Unit
) {
    val title = when (displayMode) {
        HeroDisplayMode.EXPENSE -> stringResource(id = R.string.label_total_spending)
        HeroDisplayMode.INCOME -> stringResource(id = R.string.label_total_income)
        HeroDisplayMode.BOTH -> stringResource(id = R.string.label_net_savings)
    }
    
    val amount = when (displayMode) {
        HeroDisplayMode.EXPENSE -> snapshot.expenseDisplay
        HeroDisplayMode.INCOME -> snapshot.incomeDisplay
        HeroDisplayMode.BOTH -> snapshot.savingsDisplay
    }

    // The hero is a neutral card in both themes, the same recipe the home hero uses: the
    // brand is the bloom, never the surface. The dark-only violet gradient this painted
    // (CashFlowCardDark*) is retired with the mock that specified it.

    val deltaColor = if (snapshot.changePercent >= 0) {
        if (displayMode == HeroDisplayMode.EXPENSE) MaterialTheme.colorScheme.expense else MaterialTheme.colorScheme.income
    } else {
        if (displayMode == HeroDisplayMode.EXPENSE) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense
    }
    val deltaArrow = if (snapshot.changePercent >= 0) "▲ " else "▼ "

    Column(modifier = Modifier.fillMaxWidth()) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppCardDefaults.shape(20.dp),
            colors = AppCardDefaults.colors(),
            brush = glassHeroGradient(),
        ) {
            // The bloom must be attached to a fill that spans the card. Its centre sits at
            // 92% of the width and 18% of the height, well inside its own radius, so a rect
            // inset by the card's padding cuts it off at near-full strength and the glow
            // reads as a square patch rather than a corner wash. The padding therefore sits
            // on the column below, as it does on the home hero — which also keeps this Box
            // and the card the same rect, since the bloom's fractions measure against this Box.
            Box(modifier = Modifier.matchParentSize().heroBloom())

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(Locale.getDefault()),
                        color = MaterialTheme.colorScheme.textTertiary,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp
                        )
                    )
                    
                    DialogModeSelector(
                        options = HeroDisplayMode.entries.map { mode ->
                            DialogModeOption(
                                id = mode,
                                label = stringResource(id = mode.labelRes),
                                icon = when (mode) {
                                    HeroDisplayMode.EXPENSE -> Icons.Filled.ArrowDownward
                                    HeroDisplayMode.INCOME -> Icons.Filled.ArrowUpward
                                    HeroDisplayMode.BOTH -> Icons.Filled.SwapHoriz
                                },
                                iconTint = when (mode) {
                                    HeroDisplayMode.EXPENSE -> MaterialTheme.colorScheme.expense
                                    HeroDisplayMode.INCOME -> MaterialTheme.colorScheme.income
                                    HeroDisplayMode.BOTH -> MaterialTheme.colorScheme.accentInk
                                }
                            )
                        },
                        selectedId = displayMode,
                        onOptionSelected = onDisplayModeChange,
                    )
                }

                Text(
                    text = amount,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(deltaColor.copy(alpha = 0.15f))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$deltaArrow${snapshot.changeDisplay.asString()}",
                            color = deltaColor,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Text(
                        text = resolveSummaryLabel(snapshot.summaryLabel),
                        color = MaterialTheme.colorScheme.textTertiary,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        AnalyticsLineChart(
            expensePoints = snapshot.expenseChartPoints,
            incomePoints = snapshot.incomeChartPoints,
            labels = snapshot.chartLabels,
            displayMode = displayMode
        )
    }
}

@Composable
internal fun AnalyticsLineChart(
    expensePoints: List<Float>,
    incomePoints: List<Float>,
    labels: List<ChartLabelUi>,
    displayMode: HeroDisplayMode
) {
    val expenseColor = MaterialTheme.colorScheme.expense
    val incomeColor = MaterialTheme.colorScheme.income
    val backgroundColor = MaterialTheme.colorScheme.background
    val showExpense = displayMode == HeroDisplayMode.EXPENSE || displayMode == HeroDisplayMode.BOTH
    val showIncome = displayMode == HeroDisplayMode.INCOME || displayMode == HeroDisplayMode.BOTH

    val animationProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(expensePoints, incomePoints, labels, displayMode) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
    }
    val progress = animationProgress.value
    
    Column(modifier = Modifier.fillMaxWidth()) {
        val maxExpense = if (showExpense && expensePoints.isNotEmpty()) expensePoints.maxOrNull() ?: 0f else 0f
        val maxIncome = if (showIncome && incomePoints.isNotEmpty()) incomePoints.maxOrNull() ?: 0f else 0f
        val maxValue = maxOf(maxExpense, maxIncome).coerceAtLeast(1f)

        Row(modifier = Modifier.fillMaxWidth().height(170.dp)) {
            Column(
                modifier = Modifier
                    .width(34.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatYAxisAmount(maxValue),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = formatYAxisAmount(maxValue / 2),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = stringResource(id = R.string.label_zero),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    if (!showExpense && !showIncome) return@Canvas
                    
                    val chartHeight = size.height * 0.82f
                    
                    val gridLines = listOf(16.dp.toPx(), (chartHeight + 16.dp.toPx()) / 2)
                    gridLines.forEach { y ->
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    val lineStrokeWidth = 2.5.dp.toPx()
                    val dotRadius = 5.dp.toPx()
                    
                    if (showExpense && expensePoints.isNotEmpty()) {
                        val stepX = if (expensePoints.size > 1) size.width / (expensePoints.size - 1) else size.width
                        val normalized = expensePoints.mapIndexed { index, value ->
                            val animValue = value * progress
                            Offset(
                                x = stepX * index,
                                y = chartHeight - ((animValue / maxValue) * (chartHeight - 16.dp.toPx()))
                            )
                        }
                        
                        drawSkippedLine(
                            values = expensePoints,
                            normalized = normalized,
                            chartHeight = chartHeight,
                            lineColor = expenseColor.copy(alpha = progress.coerceIn(0.3f, 1f)),
                            fillColors = if (displayMode == HeroDisplayMode.EXPENSE) {
                                listOf(
                                    expenseColor.copy(alpha = 0.6f * progress),
                                    expenseColor.copy(alpha = 0.2f * progress),
                                    backgroundColor.copy(alpha = 0.1f * progress)
                                )
                            } else null,
                            lineStrokeWidth = lineStrokeWidth,
                            dotRadius = dotRadius * progress
                        )
                    }
                    
                    if (showIncome && incomePoints.isNotEmpty()) {
                        val stepX = if (incomePoints.size > 1) size.width / (incomePoints.size - 1) else size.width
                        val normalized = incomePoints.mapIndexed { index, value ->
                            val animValue = value * progress
                            Offset(
                                x = stepX * index,
                                y = chartHeight - ((animValue / maxValue) * (chartHeight - 16.dp.toPx()))
                            )
                        }
                        
                        drawSkippedLine(
                            values = incomePoints,
                            normalized = normalized,
                            chartHeight = chartHeight,
                            lineColor = incomeColor.copy(alpha = progress.coerceIn(0.3f, 1f)),
                            fillColors = if (displayMode == HeroDisplayMode.INCOME) {
                                listOf(
                                    incomeColor.copy(alpha = 0.6f * progress),
                                    incomeColor.copy(alpha = 0.2f * progress),
                                    backgroundColor.copy(alpha = 0.1f * progress)
                                )
                            } else null,
                            lineStrokeWidth = lineStrokeWidth,
                            dotRadius = dotRadius * progress
                        )
                    }

                    drawLine(
                        color = gridColor,
                        start = Offset(0f, chartHeight),
                        end = Offset(size.width, chartHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            Spacer(modifier = Modifier.width(42.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 42.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { labelUi ->
                Text(
                    text = resolveChartLabel(labelUi),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        
        if (displayMode == HeroDisplayMode.BOTH) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(expenseColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(id = R.string.label_expense), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(20.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(incomeColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(id = R.string.label_income), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
internal fun formatYAxisAmount(amount: Float): String {
    return when {
        amount >= 1_000_000f -> stringResource(id = R.string.format_millions, amount / 1_000_000f)
        amount >= 1_000f -> stringResource(id = R.string.format_thousands, amount / 1_000f)
        else -> amount.toInt().toString()
    }
}

/**
 * Draws a line chart series while skipping zero-valued data points.
 * Each zero point is omitted (no dot, no connecting segment), so the line
 * breaks at weeks/days with no activity instead of dipping to zero.
 */
internal fun DrawScope.drawSkippedLine(
    values: List<Float>,
    normalized: List<Offset>,
    chartHeight: Float,
    lineColor: Color,
    fillColors: List<Color>?,
    lineStrokeWidth: Float,
    dotRadius: Float
) {
    if (values.isEmpty()) return

    // Runs of consecutive non-zero data points (zero points are skipped)
    val runs = buildList {
        var start = -1
        values.forEachIndexed { index, value ->
            if (value > 0f) {
                if (start < 0) start = index
            } else {
                if (start >= 0) {
                    add(start..(index - 1))
                    start = -1
                }
            }
        }
        if (start >= 0) add(start..values.lastIndex)
    }

    // Draw one smoothed segment per run, reusing the path for both fill and line
    runs.forEach { run ->
        if (run.last > run.first) {
            val path = Path().apply {
                moveTo(normalized[run.first].x, normalized[run.first].y)
                for (index in run.first + 1..run.last) {
                    val previous = normalized[index - 1]
                    val current = normalized[index]
                    val controlX = (previous.x + current.x) / 2f
                    cubicTo(controlX, previous.y, controlX, current.y, current.x, current.y)
                }
            }

            // Gradient fill under this run (per line separately, zero points excluded)
            if (fillColors != null) {
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(normalized[run.last].x, chartHeight)
                    lineTo(normalized[run.first].x, chartHeight)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(colors = fillColors, endY = chartHeight)
                )
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = lineStrokeWidth, cap = StrokeCap.Round)
            )
        }
    }

    // Dots only at non-zero data points
    runs.forEach { run ->
        for (index in run.first..run.last) {
            drawCircle(
                color = lineColor,
                radius = dotRadius,
                center = normalized[index]
            )
        }
    }
}
