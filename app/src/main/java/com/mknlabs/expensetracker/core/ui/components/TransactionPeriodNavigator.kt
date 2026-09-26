package com.mknlabs.expensetracker.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.isDark

enum class TransactionPeriodFilter(@StringRes val labelRes: Int) {
    ALL(R.string.label_all),
    DAILY(R.string.label_daily),
    MONTHLY(R.string.label_monthly),
    YEARLY(R.string.label_yearly)
}

@Composable
fun TransactionPeriodNavigator(
    modifier: Modifier = Modifier,
    selectedFilter: TransactionPeriodFilter,
    periodLabel: String,
    canNavigateBackward: Boolean,
    canNavigateForward: Boolean,
    onFilterSelected: (TransactionPeriodFilter) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onLabelClick: (() -> Unit)? = null  // null = not clickable (e.g. ALL mode)
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.isDark

    // The strip is where the "view by" changer sits, and it stands on the field rather
    // than inside a card. In light it becomes one - white on the grey field, held by the
    // hairline edge - so the pill's secondary surface has something to read against and
    // the strip stops blending into the field. Dark keeps the outline-only strip it has
    // always had.
    val borderColor = if (isDark) {
        colorScheme.outlineVariant.copy(alpha = 0.65f)
    } else {
        colorScheme.outline
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(if (isDark) Color.Transparent else colorScheme.surface)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (selectedFilter) {
            TransactionPeriodFilter.ALL -> {
                Text(
                    text = periodLabel,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            TransactionPeriodFilter.DAILY,
            TransactionPeriodFilter.MONTHLY,
            TransactionPeriodFilter.YEARLY -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PeriodArrow(
                        enabled = true,
                        onClick = onPreviousClick,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = stringResource(R.string.desc_previous_period),
                                tint = colorScheme.accentInk
                            )
                        }
                    )

                    val isClickable = onLabelClick != null
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = if (isClickable) {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onLabelClick?.invoke() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        } else Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = periodLabel,
                            color = colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.desc_select_period),
                            tint = colorScheme.onSurface.copy(alpha = 0.86f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    PeriodArrow(
                        enabled = true,
                        onClick = onNextClick,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.desc_next_period),
                                tint = colorScheme.accentInk
                            )
                        }
                    )
                }
            }
        }

        DialogModeSelector(
            options = TransactionPeriodFilter.entries.map { filter ->
                DialogModeOption(
                    id = filter,
                    label = stringResource(filter.labelRes),
                    icon = when (filter) {
                        TransactionPeriodFilter.ALL -> Icons.AutoMirrored.Filled.List
                        TransactionPeriodFilter.DAILY -> Icons.Filled.Today
                        TransactionPeriodFilter.MONTHLY -> Icons.Filled.DateRange
                        TransactionPeriodFilter.YEARLY -> Icons.Filled.CalendarMonth
                    },
                    iconTint = colorScheme.accentInk
                )
            },
            selectedId = selectedFilter,
            onOptionSelected = onFilterSelected
        )
    }
}

@Composable
private fun PeriodArrow(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}
