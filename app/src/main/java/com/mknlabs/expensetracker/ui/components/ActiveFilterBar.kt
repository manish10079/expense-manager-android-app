package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme

// ── Filter Pill Color Palette ────────────────────────────────────────────
// Each filter category gets a distinct, muted background with a darker text
// color for readability.  Colors are theme-aware and use alpha for the
// chip container so they work on both light and dark backgrounds.

enum class FilterPillType {
    SORT,
    DATE_RANGE,
    INCOME,
    EXPENSE,
    CATEGORY,
    PAYMENT_MODE,
    AMOUNT_RANGE,
    SEARCH
}

/**
 * Returns a pair of (containerColor, contentColor) for each [FilterPillType].
 * The container uses a tinted background; the content color is derived from
 * the same hue but darker for contrast.
 */
private fun FilterPillType.colors(): Pair<Color, Color> {
    return when (this) {
        FilterPillType.SORT ->
            Color(0xFF7C4DFF).copy(alpha = 0.12f) to Color(0xFF7C4DFF)       // Purple
        FilterPillType.DATE_RANGE ->
            Color(0xFF00BFA5).copy(alpha = 0.12f) to Color(0xFF00897B)       // Teal
        FilterPillType.INCOME ->
            Color(0xFF81C784).copy(alpha = 0.15f) to Color(0xFF2E7D32)       // IncomeGreen
        FilterPillType.EXPENSE ->
            Color(0xFFFF7D7D).copy(alpha = 0.15f) to Color(0xFFC62828)       // ExpenseRed
        FilterPillType.CATEGORY ->
            Color(0xFF00C853).copy(alpha = 0.12f) to Color(0xFF2E7D32)       // Green
        FilterPillType.PAYMENT_MODE ->
            Color(0xFF2979FF).copy(alpha = 0.12f) to Color(0xFF1565C0)       // Blue
        FilterPillType.AMOUNT_RANGE ->
            Color(0xFFFF4081).copy(alpha = 0.12f) to Color(0xFFC51162)       // Pink
        FilterPillType.SEARCH ->
            Color(0xFF78909C).copy(alpha = 0.12f) to Color(0xFF546E7A)       // Blue Grey
    }
}

// ── Data Model ───────────────────────────────────────────────────────────

/**
 * Represents a single active filter shown as a pill.
 *
 * @param type The filter category (determines pill color).
 * @param label Human-readable label, e.g. "Food", "Last 7 days", "Amount ↓".
 * @param onDismiss Called when the user taps the ✕ on this pill.
 */
data class ActiveFilter(
    val type: FilterPillType,
    val label: String,
    val onDismiss: () -> Unit
)

// ── Filter Pill ──────────────────────────────────────────────────────────

/**
 * A single dismissable filter pill.
 *
 * Design:
 * - Rounded pill shape (20dp radius)
 * - Tinted background from [FilterPillType.colors]
 * - Trailing ✕ icon button for dismissal
 * - Max width constraint to prevent oversized pills
 */
@Composable
fun FilterPill(
    filter: ActiveFilter,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = filter.type.colors()

    FilterChip(
        selected = true,
        onClick = { /* No-op: pill is purely informational + dismissable */ },
        label = {
            Text(
                text = filter.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingIcon = {
            IconButton(
                onClick = filter.onDismiss,
                modifier = Modifier.size(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove filter",
                    tint = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(10.dp)
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            selectedContainerColor = containerColor,
            labelColor = contentColor,
            selectedLabelColor = contentColor,
            iconColor = contentColor.copy(alpha = 0.7f),
            selectedLeadingIconColor = contentColor.copy(alpha = 0.7f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = contentColor.copy(alpha = 0.2f),
            selectedBorderColor = contentColor.copy(alpha = 0.3f),
            enabled = true,
            selected = true
        ),
        modifier = modifier.height(28.dp)
    )
}

// ── Active Filter Bar ────────────────────────────────────────────────────

/**
 * A horizontally scrollable row of active filter pills with a "Clear all" button.
 *
 * Layout:
 * ```
 * [✕ Food] [✕ Expense] [✕ Sep] ····· [Clear all]
 * ```
 *
 * - Pills scroll horizontally when they overflow
 * - "Clear all" is pinned to the end (visible only when ≥1 filter active)
 * - The entire bar fades in/out based on [filters] emptiness
 * - 1dp divider at the bottom separates from the list
 */
@Composable
fun ActiveFilterBar(
    filters: List<ActiveFilter>,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = filters.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = filters,
                        key = { "${it.type.name}_${it.label}" }
                    ) { filter ->
                        FilterPill(filter = filter)
                    }
                }

                // "Clear all" pinned to the end
                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "Clear all",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Subtle divider
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            )
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun ActiveFilterBarPreview() {
    ExpenseTrackerTheme {
        ActiveFilterBar(
            filters = listOf(
                ActiveFilter(FilterPillType.SORT, "Amount ↓") {},
                ActiveFilter(FilterPillType.DATE_RANGE, "Last 7 days") {},
                ActiveFilter(FilterPillType.EXPENSE, "Expense") {},
                ActiveFilter(FilterPillType.CATEGORY, "Food") {},
                ActiveFilter(FilterPillType.PAYMENT_MODE, "Cash") {},
                ActiveFilter(FilterPillType.SEARCH, "lunch") {}
            ),
            onClearAll = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun FilterPillPreview() {
    ExpenseTrackerTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill(filter = ActiveFilter(FilterPillType.SORT, "Amount ↓") {})
            FilterPill(filter = ActiveFilter(FilterPillType.CATEGORY, "Food & Dining") {})
            FilterPill(filter = ActiveFilter(FilterPillType.SEARCH, "coffee") {})
        }
    }
}
