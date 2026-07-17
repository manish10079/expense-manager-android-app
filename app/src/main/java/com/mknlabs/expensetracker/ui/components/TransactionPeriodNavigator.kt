package com.mknlabs.expensetracker.ui.components

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R

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
    isMenuExpanded: Boolean,
    canNavigateBackward: Boolean,
    canNavigateForward: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onFilterSelected: (TransactionPeriodFilter) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onLabelClick: (() -> Unit)? = null  // null = not clickable (e.g. ALL mode)
) {
    val colorScheme = MaterialTheme.colorScheme
    val surfaceColor = colorScheme.surface.copy(alpha = 0.96f)

    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Transparent)
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
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
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
                                tint = colorScheme.primary
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
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
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
                                tint = colorScheme.primary
                            )
                        }
                    )
                }
            }
        }

        Box {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(colorScheme.background.copy(alpha = 0.50f))
                    .border(
                        width = 1.dp,
                        color = if (isMenuExpanded) {
                            colorScheme.primary.copy(alpha = 0.55f)
                        } else {
                            colorScheme.surface.copy(alpha = 0f)
                        },
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onMenuExpandedChange(true) }
                    .padding(horizontal = 20.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(selectedFilter.labelRes),
                    color = colorScheme.onSurface.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = (MaterialTheme.typography.titleMedium.fontSize.value * 0.72f).sp
                    )
                )
            }

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { onMenuExpandedChange(false) },
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(surfaceColor),
                containerColor = surfaceColor,
                shape = RoundedCornerShape(18.dp)
            ) {
                TransactionPeriodFilter.values().forEach { filter ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(filter.labelRes),
                                color = if (filter == selectedFilter) {
                                    colorScheme.onSurface
                                } else {
                                    colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (filter == selectedFilter) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    fontSize = (MaterialTheme.typography.titleMedium.fontSize.value * 0.96f).sp
                                )
                            )
                        },
                        onClick = { onFilterSelected(filter) }
                    )
                }
            }
        }
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
