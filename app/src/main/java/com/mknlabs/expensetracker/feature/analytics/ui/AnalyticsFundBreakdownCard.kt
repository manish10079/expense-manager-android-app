package com.mknlabs.expensetracker.feature.analytics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.identityColor
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.track
import kotlin.math.roundToInt

/**
 * How the period's spending split across the funds it was drawn from.
 *
 * Only expenses linked to a fund appear, which is why a user with no funds (or none used
 * this period) sees the empty line rather than an empty chart.
 */
@Composable
internal fun FundBreakdownCard(
    modifier: Modifier = Modifier,
    snapshot: AnalyticsSnapshotUi,
    onViewAllClick: () -> Unit
) {
    val reveal = rememberShareReveal(snapshot.fundBreakdown)
    AppCard(
        modifier = modifier,
        colors = analyticsCardColors(),
        shape = AppCardDefaults.shape(30.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_funds_breakdown),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (snapshot.allFundBreakdown.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onViewAllClick)
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_view_all),
                            color = MaterialTheme.colorScheme.accentInk,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.accentInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            FundDonutChart(
                snapshot.fundBreakdown,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                progress = reveal
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (snapshot.fundBreakdown.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_fund_breakdown),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    snapshot.fundBreakdown.forEach { fund ->
                        FundBreakdownLegendRow(fund = fund, progress = reveal)
                    }
                }
            }
        }
    }
}

@Composable
private fun FundBreakdownLegendRow(
    fund: FundBreakdownUi,
    progress: Float = 1f
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.identityColor(fund.colorHex))
            )
            Text(
                text = if (fund.isOther) {
                    stringResource(id = R.string.label_other)
                } else {
                    fund.label
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = stringResource(
                R.string.format_percentage,
                (fund.percentLabel * progress).roundToInt()
            ),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun FundDonutChart(
    breakdown: List<FundBreakdownUi>,
    modifier: Modifier = Modifier,
    progress: Float = 1f
) {
    val trackColor = MaterialTheme.colorScheme.track
    val segmentColors = breakdown.map { MaterialTheme.colorScheme.identityColor(it.colorHex) }
    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            var startAngle = -180f
            val gap = 5f
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            val revealedSweep = progress.coerceIn(0f, 1f) * 360f
            breakdown.forEachIndexed { index, segment ->
                val sweep = (segment.fraction * 360f) - gap
                val visibleSweep = (revealedSweep - (startAngle + 180f))
                    .coerceIn(0f, sweep.coerceAtLeast(0f))
                if (visibleSweep > 0f) {
                    drawArc(
                        color = segmentColors[index],
                        startAngle = startAngle,
                        sweepAngle = visibleSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                startAngle += sweep + gap
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(
                    id = R.string.label_top_val,
                    breakdown.firstOrNull()?.let {
                        if (it.isOther) stringResource(id = R.string.label_other) else it.label
                    } ?: stringResource(id = R.string.label_not_available)
                ),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FundBreakdownBottomSheet(
    funds: List<FundBreakdownUi>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.sheet,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            val filtered = remember(funds) { funds.filter { it.fraction > 0f } }
            Text(
                text = stringResource(id = R.string.label_all_funds_breakdown),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (filtered.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_fund_breakdown),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filtered.size) { index ->
                        FundBreakdownLegendRow(fund = filtered[index])
                    }
                }
            }
        }
    }
}
