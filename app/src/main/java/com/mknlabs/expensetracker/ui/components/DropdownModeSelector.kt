package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.ui.theme.standardCardGradient

/**
 * A single selectable option for [DropdownModeSelector].
 *
 * @param id       Unique identifier used to track the selected option.
 * @param label    Display text shown in the pill and the menu.
 * @param icon     Optional leading icon shown both in the pill and menu.
 * @param iconTint Optional tint for [icon].
 */
data class DropdownModeOption<T>(
    val id: T,
    val label: String,
    val icon: ImageVector? = null,
    val iconTint: Color? = null
)

/**
 * A pill-shaped dropdown style selector. Shows the selected option as a
 * compact chip with a leading icon and a chevron; tapping it opens a menu
 * listing every option with an icon and label. The currently selected option
 * is highlighted in the primary color.
 *
 * The dropdown expansion state is managed internally.
 *
 * @param initialExpanded Whether to start with the dropdown menu expanded
 *                        (used by previews to show the open menu).
 */
@Composable
fun <T> DropdownModeSelector(
    options: List<DropdownModeOption<T>>,
    selectedId: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    menuMaxWidth: Dp = 120.dp,
    containerBackground: Brush = standardCardGradient(),
    initialExpanded: Boolean = false
) {
    var isDropdownExpanded by rememberSaveable { mutableStateOf(initialExpanded) }
    val selectedOption = options.firstOrNull { it.id == selectedId }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(containerBackground)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { isDropdownExpanded = true }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            selectedOption?.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = selectedOption.iconTint ?: MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = selectedOption?.label ?: "",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier.widthIn(max = menuMaxWidth),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            option.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = option.iconTint ?: MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = option.label,
                                color = if (option.id == selectedId) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (option.id == selectedId) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            )
                        }
                    },
                    onClick = {
                        isDropdownExpanded = false
                        onOptionSelected(option.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun DropdownModeSelectorPreviewContent() {
    var selectedFilter by rememberSaveable { mutableStateOf(TransactionPeriodFilter.MONTHLY) }

    DropdownModeSelector(
        options = TransactionPeriodFilter.entries.map { filter ->
            DropdownModeOption(
                id = filter,
                label = stringResource(filter.labelRes),
                icon = when (filter) {
                    TransactionPeriodFilter.ALL -> Icons.AutoMirrored.Filled.List
                    TransactionPeriodFilter.DAILY -> Icons.Filled.Today
                    TransactionPeriodFilter.MONTHLY -> Icons.Filled.DateRange
                    TransactionPeriodFilter.YEARLY -> Icons.Filled.CalendarMonth
                },
                iconTint = MaterialTheme.colorScheme.primary
            )
        },
        selectedId = selectedFilter,
        onOptionSelected = { selectedFilter = it },
        initialExpanded = true
    )
}

