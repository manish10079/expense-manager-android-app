package com.mknlabs.expensetracker.feature.goals.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AvatarRingBrush
import com.mknlabs.expensetracker.core.ui.components.BrandAddFab
import com.mknlabs.expensetracker.core.ui.components.IconPickerGrid
import com.mknlabs.expensetracker.core.ui.components.BrandAddFabDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.NamedCreateCard
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.categoryIconOptions
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.GoalFundEntry
import com.mknlabs.expensetracker.core.ui.components.AppDialogConfirmButton
import com.mknlabs.expensetracker.core.ui.components.AppDialogDefaults
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppDialogDismissButton
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.CategoryColorRow
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.core.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.identityColor
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.track
import com.mknlabs.expensetracker.core.ui.theme.GoalProgressHigh
import com.mknlabs.expensetracker.core.ui.theme.flatCard
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.profileSheet

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mknlabs.expensetracker.utils.daysUntilTimestamp
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatDate
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Long?, String, Double, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf("") }
    var amountInput by rememberSaveable { mutableStateOf("") }
    var initialAmountInput by rememberSaveable { mutableStateOf("") }
    var deadlineAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var iconKey by rememberSaveable { mutableStateOf("savings") }
    var colorHex by rememberSaveable { mutableStateOf("") }
    var isDeadlinePickerVisible by rememberSaveable { mutableStateOf(false) }

    val targetAmount = amountInput.toDoubleOrNull() ?: 0.0
    val initialAmount = initialAmountInput.toDoubleOrNull() ?: 0.0
    val isSaveEnabled = name.isNotBlank() && targetAmount > 0.0
    val identity = MaterialTheme.colorScheme.identityColor(colorHex.ifBlank { null })

    val sheetContainerColor = if (MaterialTheme.colorScheme.isDark) {
        MaterialTheme.colorScheme.profileSheet
    } else {
        BottomSheetDefaults.ContainerColor
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.title_add_goal),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.label_goal_name)) },
                placeholder = { Text(stringResource(R.string.label_goal_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppOutlinedFieldDefaults.shape,
                colors = AppOutlinedFieldDefaults.colors()
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = { updatedValue ->
                    amountInput = updatedValue.filter { it.isDigit() || it == '.' }
                },
                label = { Text(stringResource(R.string.label_target_amount)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppOutlinedFieldDefaults.shape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = AppOutlinedFieldDefaults.colors()
            )

            OutlinedTextField(
                value = initialAmountInput,
                onValueChange = { updatedValue ->
                    initialAmountInput = updatedValue.filter { it.isDigit() || it == '.' }
                },
                label = { Text(stringResource(R.string.label_initial_saved_amount)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppOutlinedFieldDefaults.shape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = AppOutlinedFieldDefaults.colors()
            )

            DeadlinePickerRow(
                deadlineAt = deadlineAt,
                dateFormatPattern = dateFormatPattern,
                onPick = { isDeadlinePickerVisible = true },
                onClear = { deadlineAt = null }
            )

            Text(
                text = stringResource(R.string.label_goal_icon),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconPickerGrid(
                selectedId = iconKey,
                onSelect = { iconKey = it },
                identityColor = identity,
                columns = 6,
                modifier = Modifier.height(112.dp)
            )

            Text(
                text = stringResource(R.string.label_color_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CategoryColorRow(
                selectedColorHex = colorHex.ifBlank { null },
                onColorSelected = { colorHex = it.orEmpty() }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = brandGradient(alpha = if (isSaveEnabled) 1f else 0.45f))
                    .clickable(enabled = isSaveEnabled) {
                        onSave(name, targetAmount, deadlineAt, iconKey, initialAmount, colorHex)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.label_save_1),
                    color = MaterialTheme.colorScheme.onBrandGradient,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (isDeadlinePickerVisible) {
        DeadlinePickerModal(
            initialDeadlineMillis = deadlineAt,
            onDismiss = { isDeadlinePickerVisible = false },
            onConfirm = {
                deadlineAt = it
                isDeadlinePickerVisible = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalDialog(
    goal: Goal,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Long?, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable(goal.id) { mutableStateOf(goal.name) }
    var amountInput by rememberSaveable(goal.id) { mutableStateOf(formatAmountForInput(goal.targetAmountMinor)) }
    var deadlineAt by rememberSaveable(goal.id) { mutableStateOf(goal.deadlineAt) }
    var iconKey by rememberSaveable(goal.id) { mutableStateOf(goal.iconKey) }
    var colorHex by rememberSaveable(goal.id) { mutableStateOf(goal.colorHex) }
    var isDeadlinePickerVisible by rememberSaveable { mutableStateOf(false) }

    val targetAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isSaveEnabled = name.isNotBlank() && targetAmount > 0.0
    val identity = MaterialTheme.colorScheme.identityColor(colorHex.ifBlank { null })

    val sheetContainerColor = if (MaterialTheme.colorScheme.isDark) {
        MaterialTheme.colorScheme.profileSheet
    } else {
        BottomSheetDefaults.ContainerColor
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.title_edit_goal),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.label_goal_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppOutlinedFieldDefaults.shape,
                colors = AppOutlinedFieldDefaults.colors()
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = { updatedValue ->
                    amountInput = updatedValue.filter { it.isDigit() || it == '.' }
                },
                label = { Text(stringResource(R.string.label_target_amount)) },
                supportingText = {
                    Text(
                        text = stringResource(
                            R.string.label_saved_amount,
                            formatCurrencyValue(goal.currentAmountMinor / 100.0, currencyId, amountFormatPreferences)
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppOutlinedFieldDefaults.shape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = AppOutlinedFieldDefaults.colors()
            )

            DeadlinePickerRow(
                deadlineAt = deadlineAt,
                dateFormatPattern = dateFormatPattern,
                onPick = { isDeadlinePickerVisible = true },
                onClear = { deadlineAt = null }
            )

            Text(
                text = stringResource(R.string.label_goal_icon),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconPickerGrid(
                selectedId = iconKey,
                onSelect = { iconKey = it },
                identityColor = identity,
                columns = 6,
                modifier = Modifier.height(112.dp)
            )

            Text(
                text = stringResource(R.string.label_color_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CategoryColorRow(
                selectedColorHex = colorHex.ifBlank { null },
                onColorSelected = { colorHex = it.orEmpty() }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = brandGradient(alpha = if (isSaveEnabled) 1f else 0.45f))
                    .clickable(enabled = isSaveEnabled) {
                        onSave(name, targetAmount, deadlineAt, iconKey, colorHex)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.label_save_1),
                    color = MaterialTheme.colorScheme.onBrandGradient,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (isDeadlinePickerVisible) {
        DeadlinePickerModal(
            initialDeadlineMillis = deadlineAt,
            onDismiss = { isDeadlinePickerVisible = false },
            onConfirm = {
                deadlineAt = it
                isDeadlinePickerVisible = false
            }
        )
    }
}

@Composable
internal fun DeadlinePickerRow(
    deadlineAt: Long?,
    dateFormatPattern: String,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            // A picker row is a field inside the dialog, so in light it takes the spec's
            // secondary surface. The half-strength wash it used to be sits almost on the
            // dialog's own white and left the row with no edge at all.
            .background(
                if (colorScheme.isDark) colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else colorScheme.surfaceVariant
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.label_deadline),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        if (deadlineAt != null) {
            Text(
                text = formatDate(deadlineAt, dateFormatPattern),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colorScheme.accentInk,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onPick)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            )
            AppTextButton(onClick = onClear) {
                Text(
                    text = stringResource(R.string.label_clear),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.error
                )
            }
        } else {
            AppTextButton(onClick = onPick) {
                Text(
                    text = stringResource(R.string.label_set_deadline),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.accentInk
                )
            }
        }
    }
}

@Composable
internal fun DeadlinePickerModal(
    initialDeadlineMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    WheelDateTimePickerModal(
        mode = WheelPickerMode.SINGLE_DATE,
        initialStartMillis = initialDeadlineMillis ?: System.currentTimeMillis(),
        onDismissRequest = onDismiss,
        onConfirm = { pickedDateMillis, _ ->
            onConfirm(
                datePickerSelectionToLocalDateTimestamp(
                    selectedDateMillis = pickedDateMillis,
                    isInputUtc = false
                )
            )
        }
    )
}

