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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.identityColor
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.theme.track
import kotlin.math.roundToInt

@Composable
internal fun TagBreakdownCard(
    modifier: Modifier = Modifier,
    snapshot: AnalyticsSnapshotUi,
    onViewAllClick: () -> Unit,
    onShowTransactions: (String, String) -> Unit
) {
    val reveal = rememberShareReveal(snapshot.tagBreakdown)
    AppCard(
        modifier = modifier,
        brush = darkOnlyGradient(standardCardGradient()),
        colors = AppCardDefaults.colors(androidx.compose.ui.graphics.Color.Transparent),
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
                    text = stringResource(id = R.string.label_tags_breakdown),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (snapshot.allTagBreakdown.isNotEmpty()) {
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
            TagDonutChart(
                snapshot.tagBreakdown,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                progress = reveal
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (snapshot.tagBreakdown.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_tag_breakdown),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    snapshot.tagBreakdown.forEach { tag ->
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
                                        .background(MaterialTheme.colorScheme.identityColor(tag.colorHex))
                                )
                                Text(
                                    text = if (tag.isOther) {
                                        stringResource(id = R.string.label_other)
                                    } else {
                                        tag.label
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.format_percentage,
                                        (tag.percentLabel * reveal).roundToInt()
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = stringResource(id = R.string.desc_show_transactions),
                                    tint = MaterialTheme.colorScheme.accentInk,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onShowTransactions(tag.id, tag.label) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagDonutChart(
    breakdown: List<TagBreakdownUi>,
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
internal fun TagBreakdownBottomSheet(
    tags: List<TagBreakdownUi>,
    onDismiss: () -> Unit,
    onShowTransactions: (String, String) -> Unit
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
            val filtered = remember(tags) { tags.filter { it.fraction > 0f } }
            Text(
                text = stringResource(id = R.string.label_all_tags_breakdown),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (filtered.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_tag_breakdown),
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
                        val tag = filtered[index]
                        val reveal = rememberShareReveal(tag.id, tag.fraction)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onShowTransactions(tag.id, tag.label) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.identityColor(tag.colorHex))
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text(
                                text = tag.label,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(
                                    R.string.format_percentage,
                                    (tag.percentLabel * reveal).roundToInt()
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
