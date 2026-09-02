package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mknlabs.expensetracker.R


/**
 * A single selectable option for [DialogModeSelector].
 *
 * @param id       Unique identifier used to track the selected option.
 * @param label    Display text shown in the pill and the menu.
 * @param icon     Optional leading icon shown both in the pill and menu.
 * @param iconTint Optional tint for [icon].
 */
data class DialogModeOption<T>(
    val id: T,
    val label: String,
    val icon: ImageVector? = null,
    val iconTint: Color? = null
)

/**
 * A pill-shaped selector chip. Tapping it opens a [ViewPickerDialog] — a
 * centered half-screen popup with large date-picker-style option tiles.
 * The expansion state is managed internally.
 *
 * @param initialExpanded Whether to start with the dialog open (used by previews).
 */
@Composable
fun <T> DialogModeSelector(
    options: List<DialogModeOption<T>>,
    selectedId: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    menuMaxWidth: Dp = 120.dp,
    initialExpanded: Boolean = false
) {
    var isDialogVisible by rememberSaveable { mutableStateOf(initialExpanded) }
    val selectedOption = options.firstOrNull { it.id == selectedId }

    Box(modifier = modifier) {
        // Pill chip — tapping opens the centered dialog
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { isDialogVisible = true }
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
        }

        // Centered half-screen picker dialog
        if (isDialogVisible) {
            ViewPickerDialog(
                options = options,
                selectedId = selectedId,
                onOptionSelected = { id ->
                    isDialogVisible = false
                    onOptionSelected(id)
                },
                onDismiss = { isDialogVisible = false }
            )
        }
    }
}

/**
 * A centered dialog occupying the bottom half of the screen vertically and full
 * width horizontally. Each option is rendered as a large tappable tile arranged
 * in a 2-column grid — icon + label — with the selected tile highlighted in
 * the primary color, similar to a date-picker style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ViewPickerDialog(
    options: List<DialogModeOption<T>>,
    selectedId: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    // Title
                    Text(
                        text = stringResource(R.string.title_view_by),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        textAlign = TextAlign.Center
                    )

                    // Options in a 2-column grid
                    val chunkedOptions = options.chunked(2)
                    chunkedOptions.forEachIndexed { rowIndex, rowOptions ->
                        if (rowIndex > 0) Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowOptions.forEach { option ->
                                val isSelected = option.id == selectedId
                                val bgColor by animateColorAsState(
                                    targetValue = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    animationSpec = tween(200),
                                    label = "tileBg_${option.label}"
                                )
                                val contentColor by animateColorAsState(
                                    targetValue = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    animationSpec = tween(200),
                                    label = "tileContent_${option.label}"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 80.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(bgColor)
                                        .then(
                                            if (!isSelected) Modifier.border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(18.dp)
                                            ) else Modifier
                                        )
                                        .clickable { onOptionSelected(option.id) }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        option.icon?.let { icon ->
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected)
                                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                                                        else
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected)
                                                        MaterialTheme.colorScheme.onPrimary
                                                    else
                                                        MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = option.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                            // Fill empty slot if odd number of options in last row
                            if (rowOptions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
