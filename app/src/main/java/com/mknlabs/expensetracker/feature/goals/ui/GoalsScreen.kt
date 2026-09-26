package com.mknlabs.expensetracker.feature.goals.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.BrandAddFab
import com.mknlabs.expensetracker.core.ui.components.BrandAddFabDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.categoryIconOptions
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.GoalFundEntry
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppDialogConfirmButton
import com.mknlabs.expensetracker.core.ui.components.AppDialogDefaults
import com.mknlabs.expensetracker.core.ui.components.AppDialogDismissButton
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.core.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.track
import com.mknlabs.expensetracker.core.ui.theme.CardShadowAmbientLight
import com.mknlabs.expensetracker.core.ui.theme.CardShadowSpotLight
import com.mknlabs.expensetracker.core.ui.theme.GoalProgressHigh
import com.mknlabs.expensetracker.core.ui.theme.GoalProgressLow
import com.mknlabs.expensetracker.core.ui.theme.GoalProgressMedium
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.PremiumCardDarkStart
import com.mknlabs.expensetracker.core.ui.theme.PremiumCardDarkCenter
import com.mknlabs.expensetracker.core.ui.theme.PremiumCardDarkEnd

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

private const val DAY_MILLIS = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onBackClick: () -> Unit,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val expandedGoalHistoryId by viewModel.expandedGoalHistoryId.collectAsStateWithLifecycle()
    val fundEntries by viewModel.fundEntries.collectAsStateWithLifecycle()

    GoalsScreenContent(
        goals = goals,
        expandedGoalHistoryId = expandedGoalHistoryId,
        fundEntries = fundEntries,
        currencyId = currencyId,
        amountFormatPreferences = amountFormatPreferences,
        dateFormatPattern = dateFormatPattern,
        onBackClick = onBackClick,
        onAddGoal = { name, amount, deadline, iconKey -> viewModel.addGoal(name, amount, deadline, iconKey) },
        onFundGoal = { id, amount -> viewModel.fundGoal(id, amount) },
        onEditGoal = { id, name, amount, deadline, iconKey ->
            viewModel.updateGoal(id, name, amount, deadline, iconKey)
        },
        onDeleteGoal = { viewModel.deleteGoal(it.id) },
        onToggleFundHistory = { goalId -> viewModel.toggleGoalHistory(goalId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalsScreenContent(
    goals: List<Goal>,
    expandedGoalHistoryId: String?,
    fundEntries: Map<String, List<GoalFundEntry>>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    onBackClick: () -> Unit,
    onAddGoal: (String, Double, Long?, String) -> Unit,
    onFundGoal: (String, Double) -> Unit,
    onEditGoal: (String, String, Double, Long?, String) -> Unit,
    onDeleteGoal: (Goal) -> Unit,
    onToggleFundHistory: (String) -> Unit
) {
    var isAddGoalDialogVisible by rememberSaveable { mutableStateOf(false) }
    var fundingGoalId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingGoalId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteGoal by remember { mutableStateOf<Goal?>(null) }

    val activeGoals = goals.filter { !it.isCompleted }
    val completedGoals = goals.filter { it.isCompleted }

    Scaffold(
        floatingActionButton = {
            // The very same brand "+" FAB the shell docks on the home screen — one
            // component, so the ramp, glow, shape and elevation cannot drift from it.
            // It carries its own label because this button adds a goal, not a
            // transaction. The offset cancels the room the component reserves for its
            // glow, which Scaffold would otherwise count as button width and leave the
            // circle inset from the screen edge rather than on the standard margin.
            BrandAddFab(
                onClick = { isAddGoalDialogVisible = true },
                modifier = Modifier.offset(
                    x = BrandAddFabDefaults.GlowInset,
                    y = BrandAddFabDefaults.GlowInset
                ),
                contentDescription = stringResource(R.string.cd_add_goal),
                // Pools the halo downward, matching the docked FAB it is the same
                // button as.
                glowOffset = 8.dp
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = Dimens.ScreenPadding)
            ) {
                Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

                AppHeader(
                    title = stringResource(R.string.title_my_goals),
                    onBackClick = onBackClick
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (goals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.msg_no_goals),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (activeGoals.isNotEmpty()) {
                            item(key = "active_header") {
                                GoalsSectionHeader(stringResource(R.string.title_active_goals))
                            }
                            items(activeGoals, key = { it.id }) { goal ->
                                GoalItem(
                                    goal = goal,
                                    currencyId = currencyId,
                                    amountFormatPreferences = amountFormatPreferences,
                                    dateFormatPattern = dateFormatPattern,
                                    isHistoryExpanded = expandedGoalHistoryId == goal.id,
                                    fundEntries = fundEntries[goal.id] ?: emptyList(),
                                    onFund = { fundingGoalId = goal.id },
                                    onEdit = { editingGoalId = goal.id },
                                    onDelete = { pendingDeleteGoal = goal },
                                    onToggleFundHistory = { onToggleFundHistory(goal.id) }
                                )
                            }
                        }

                        if (completedGoals.isNotEmpty()) {
                            item(key = "completed_header") {
                                GoalsSectionHeader(stringResource(R.string.title_completed_goals))
                            }
                            items(completedGoals, key = { "completed_${it.id}" }) { goal ->
                                GoalItem(
                                    goal = goal,
                                    currencyId = currencyId,
                                    amountFormatPreferences = amountFormatPreferences,
                                    dateFormatPattern = dateFormatPattern,
                                    isHistoryExpanded = expandedGoalHistoryId == goal.id,
                                    fundEntries = fundEntries[goal.id] ?: emptyList(),
                                    onFund = { fundingGoalId = goal.id },
                                    onEdit = { editingGoalId = goal.id },
                                    onDelete = { pendingDeleteGoal = goal },
                                    onToggleFundHistory = { onToggleFundHistory(goal.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddGoalDialogVisible) {
        AddGoalDialog(
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            onDismiss = { isAddGoalDialogVisible = false },
            onSave = { name, amount, deadline, iconKey ->
                onAddGoal(name, amount, deadline, iconKey)
                isAddGoalDialogVisible = false
            }
        )
    }

    fundingGoalId?.let { id ->
        FundGoalDialog(
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            onDismiss = { fundingGoalId = null },
            onSave = { amount ->
                onFundGoal(id, amount)
                fundingGoalId = null
            }
        )
    }

    editingGoalId?.let { id ->
        val editingGoal = goals.firstOrNull { it.id == id }
        if (editingGoal != null) {
            EditGoalDialog(
                goal = editingGoal,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences,
                dateFormatPattern = dateFormatPattern,
                onDismiss = { editingGoalId = null },
                onSave = { name, amount, deadline, iconKey ->
                    onEditGoal(id, name, amount, deadline, iconKey)
                    editingGoalId = null
                }
            )
        }
    }

    pendingDeleteGoal?.let { goal ->
        DeleteGoalDialog(
            goalName = goal.name,
            onDismiss = { pendingDeleteGoal = null },
            onConfirm = {
                onDeleteGoal(goal)
                pendingDeleteGoal = null
            }
        )
    }
}

@Composable
private fun GoalsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
fun DeleteGoalDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppDialogDefaults.shape(),
        containerColor = AppDialogDefaults.containerColor(),
        title = {
            Text(
                text = stringResource(R.string.title_delete_goal),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = stringResource(R.string.msg_delete_goal_confirm, goalName),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            AppTextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.label_delete_1),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            AppDialogDismissButton(
                text = stringResource(R.string.label_cancel_1),
                onClick = onDismiss
            )
        }
    )
}

@Composable
fun FundGoalDialog(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amountInput by rememberSaveable { mutableStateOf("") }
    val fundAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isSaveEnabled = fundAmount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppDialogDefaults.shape(),
        containerColor = AppDialogDefaults.containerColor(),
        title = {
            Text(
                text = stringResource(R.string.title_fund_goal),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { updatedValue ->
                        amountInput = updatedValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text(stringResource(R.string.label_fund_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = AppDialogDefaults.fieldShape(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        },
        confirmButton = {
            AppTextButton(
                onClick = { onSave(fundAmount) },
                enabled = isSaveEnabled
            ) {
                Text(stringResource(R.string.label_add_action))
            }
        },
        dismissButton = {
            AppDialogDismissButton(
                text = stringResource(R.string.label_cancel_1),
                onClick = onDismiss
            )
        }
    )
}

@Composable
fun AddGoalDialog(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Long?, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var amountInput by rememberSaveable { mutableStateOf("") }
    var deadlineAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var iconKey by rememberSaveable { mutableStateOf("savings") }
    var isDeadlinePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isIconPickerVisible by rememberSaveable { mutableStateOf(false) }

    val targetAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isSaveEnabled = name.isNotBlank() && targetAmount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppDialogDefaults.shape(),
        containerColor = AppDialogDefaults.containerColor(),
        title = {
            Text(
                text = stringResource(R.string.title_add_goal),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_goal_name)) },
                    placeholder = { Text(stringResource(R.string.label_goal_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = AppDialogDefaults.fieldShape(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { updatedValue ->
                        amountInput = updatedValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text(stringResource(R.string.label_target_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = AppDialogDefaults.fieldShape(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                DeadlinePickerRow(
                    deadlineAt = deadlineAt,
                    dateFormatPattern = dateFormatPattern,
                    onPick = { isDeadlinePickerVisible = true },
                    onClear = { deadlineAt = null }
                )

                GoalIconPickerRow(
                    iconKey = iconKey,
                    onPick = { isIconPickerVisible = true }
                )
            }
        },
        confirmButton = {
            AppDialogConfirmButton(
                text = stringResource(R.string.label_save_1),
                onClick = { onSave(name, targetAmount, deadlineAt, iconKey) },
                enabled = isSaveEnabled
            )
        },
        dismissButton = {
            AppDialogDismissButton(
                text = stringResource(R.string.label_cancel_1),
                onClick = onDismiss
            )
        }
    )

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

    if (isIconPickerVisible) {
        GoalIconPickerModal(
            selectedIconKey = iconKey,
            onDismiss = { isIconPickerVisible = false },
            onConfirm = {
                iconKey = it
                isIconPickerVisible = false
            }
        )
    }
}

@Composable
fun EditGoalDialog(
    goal: Goal,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, Long?, String) -> Unit
) {
    var name by rememberSaveable(goal.id) { mutableStateOf(goal.name) }
    var amountInput by rememberSaveable(goal.id) { mutableStateOf(formatAmountForInput(goal.targetAmountMinor)) }
    var deadlineAt by rememberSaveable(goal.id) { mutableStateOf(goal.deadlineAt) }
    var iconKey by rememberSaveable(goal.id) { mutableStateOf(goal.iconKey) }
    var isDeadlinePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isIconPickerVisible by rememberSaveable { mutableStateOf(false) }

    val targetAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isSaveEnabled = name.isNotBlank() && targetAmount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppDialogDefaults.shape(),
        containerColor = AppDialogDefaults.containerColor(),
        title = {
            Text(
                text = stringResource(R.string.title_edit_goal),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.label_goal_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = AppDialogDefaults.fieldShape(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
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
                    shape = AppDialogDefaults.fieldShape(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                DeadlinePickerRow(
                    deadlineAt = deadlineAt,
                    dateFormatPattern = dateFormatPattern,
                    onPick = { isDeadlinePickerVisible = true },
                    onClear = { deadlineAt = null }
                )

                GoalIconPickerRow(
                    iconKey = iconKey,
                    onPick = { isIconPickerVisible = true }
                )
            }
        },
        confirmButton = {
            AppDialogConfirmButton(
                text = stringResource(R.string.label_save_1),
                onClick = { onSave(name, targetAmount, deadlineAt, iconKey) },
                enabled = isSaveEnabled
            )
        },
        dismissButton = {
            AppDialogDismissButton(
                text = stringResource(R.string.label_cancel_1),
                onClick = onDismiss
            )
        }
    )

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

    if (isIconPickerVisible) {
        GoalIconPickerModal(
            selectedIconKey = iconKey,
            onDismiss = { isIconPickerVisible = false },
            onConfirm = {
                iconKey = it
                isIconPickerVisible = false
            }
        )
    }
}

@Composable
private fun DeadlinePickerRow(
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
private fun DeadlinePickerModal(
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

@Composable
private fun GoalIconPickerRow(
    iconKey: String,
    onPick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val selectedOption = remember(iconKey) {
        categoryIconOptions.firstOrNull { it.id == iconKey }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            // The icon picker's row is a field too, so it takes the secondary surface in
            // light for the same reason the deadline row above does.
            .background(
                if (colorScheme.isDark) colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else colorScheme.surfaceVariant
            )
            .clickable(onClick = onPick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(brush = brandGradient()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = selectedOption?.icon ?: Icons.Filled.Savings,
                contentDescription = stringResource(R.string.cd_goal_icon),
                tint = colorScheme.onCta,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.label_goal_icon),
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.title_choose_icon),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = colorScheme.accentInk
        )
    }
}

@Composable
private fun GoalIconPickerModal(
    selectedIconKey: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppDialogDefaults.shape(),
        containerColor = AppDialogDefaults.containerColor(),
        title = {
            Text(
                text = stringResource(R.string.title_choose_icon),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
            ) {
                items(categoryIconOptions, key = { it.id }) { option ->
                    GoalIconSelectionItem(
                        option = option,
                        selected = option.id == selectedIconKey,
                        onClick = { onConfirm(option.id) }
                    )
                }
            }
        },
        confirmButton = {
            AppDialogDismissButton(
                text = stringResource(R.string.label_cancel_1),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun GoalIconSelectionItem(
    option: CategoryIconOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                color = if (selected) colorScheme.accentInk else colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colorScheme.accentInk else colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = stringResource(option.labelRes),
            tint = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun GoalItem(
    goal: Goal,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    isHistoryExpanded: Boolean,
    fundEntries: List<GoalFundEntry>,
    onFund: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFundHistory: () -> Unit
) {
    // Resolve the picked icon from the same catalog the picker uses; fall back to
    // the registry-resolved icon for legacy keys not in the catalog.
    val goalIcon = remember(goal.iconKey) {
        categoryIconOptions.firstOrNull { it.id == goal.iconKey }?.icon ?: goal.icon
    }

    val progressColor = when {
        goal.progress < 0.34f -> GoalProgressLow
        goal.progress < 0.67f -> GoalProgressMedium
        else -> GoalProgressHigh
    }

    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.isDark
    val cardShape = RoundedCornerShape(24.dp)

    // The premium gradient is dark's card surface and dark's alone; light takes the shared
    // white card below, so the lavender tokens this used to blend are gone with it.
    val gradientBrush = Brush.linearGradient(
        colors = listOf(PremiumCardDarkStart, PremiumCardDarkCenter, PremiumCardDarkEnd)
    )
    val borderBrush = remember(colorScheme.accentInk) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.accentInk.copy(alpha = 0.4f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    // Light flattens this hero to the shared card: the lavender gradient it painted is
    // exactly the sort of surface the redesign replaces with white. Dark keeps that
    // gradient byte for byte, along with the two things the shared card has no way to
    // carry - a violet-tinted lift and a gradient edge, where AppCard takes a solid
    // outline and one shadow colour - so the chrome branches and the content under it is
    // untouched. The light branch still reads its container, outline and elevation from
    // AppCardDefaults, so this card cannot drift away from the rest.
    val cardChrome = if (isDark) {
        Modifier
            .shadow(
                elevation = 12.dp,
                shape = cardShape,
                ambientColor = colorScheme.accentInk.copy(alpha = 0.2f),
                spotColor = Color.Black
            )
            .clip(cardShape)
            .background(brush = gradientBrush)
            .border(width = 1.dp, brush = borderBrush, shape = cardShape)
    } else {
        val lightCard = AppCardDefaults.colors()
        Modifier
            .shadow(
                elevation = AppCardDefaults.Elevation,
                shape = cardShape,
                ambientColor = CardShadowAmbientLight,
                spotColor = CardShadowSpotLight
            )
            .clip(cardShape)
            .background(lightCard.containerColor)
            .then(
                lightCard.border?.let { Modifier.border(border = it, shape = cardShape) }
                    ?: Modifier
            )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardChrome)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = goalIcon,
                            contentDescription = stringResource(R.string.cd_goal_icon),
                            tint = MaterialTheme.colorScheme.accentInk,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoalCardAction(
                        icon = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.cd_edit_goal),
                        accent = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onEdit
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    GoalCardAction(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_funds),
                        accent = MaterialTheme.colorScheme.accentInk,
                        onClick = onFund
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    GoalCardAction(
                        icon = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_delete_goal),
                        accent = MaterialTheme.colorScheme.error,
                        onClick = onDelete
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.track,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (goal.isCompleted) {
                        stringResource(R.string.label_goal_completed)
                    } else {
                        stringResource(R.string.format_goal_progress, (goal.progress * 100).toInt())
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (goal.isCompleted) {
                        GoalProgressHigh
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (goal.isCompleted) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = stringResource(
                        R.string.format_goal_amount_status,
                        formatCurrencyValue(goal.currentAmountMinor / 100.0, currencyId, amountFormatPreferences),
                        formatCurrencyValue(goal.targetAmountMinor / 100.0, currencyId, amountFormatPreferences)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            // Deadline + Fund History in the same row
            if (!goal.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: deadline text (if set)
                    goal.deadlineAt?.let { deadline ->
                        val daysLeft = daysUntilTimestamp(deadline)
                        val (deadlineText, deadlineColor) = when {
                            daysLeft < 0 -> stringResource(
                                R.string.format_days_overdue, -daysLeft
                            ) to MaterialTheme.colorScheme.error

                            daysLeft == 0L -> stringResource(R.string.label_goal_due_today) to MaterialTheme.colorScheme.error
                            daysLeft == 1L -> stringResource(R.string.label_one_day_left) to MaterialTheme.colorScheme.error
                            else -> stringResource(R.string.format_days_left, daysLeft) to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = deadlineText,
                            style = MaterialTheme.typography.bodySmall,
                            color = deadlineColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If no deadline, push fund history to the start
                    if (goal.deadlineAt == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    // Right: Fund History toggle
                    GoalFundHistoryInline(
                        entryCount = fundEntries.size,
                        isExpanded = isHistoryExpanded,
                        onClick = onToggleFundHistory
                    )
                }
            }
            // Completed goals: still show fund history
            if (goal.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                GoalFundHistoryInline(
                    entryCount = fundEntries.size,
                    isExpanded = isHistoryExpanded,
                    onClick = onToggleFundHistory
                )
            }

            // Collapsible Fund History
            AnimatedVisibility(
                visible = isHistoryExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                GoalFundHistorySection(
                    entries = fundEntries,
                    currencyId = currencyId,
                    amountFormatPreferences = amountFormatPreferences,
                    dateFormatPattern = dateFormatPattern
                )
            }
        }
    }
}

/** Formats a minor-unit amount as plain input text (e.g. 250000 -> "2500", 250050 -> "2500.50"). */
private fun formatAmountForInput(amountMinor: Long): String {
    val value = amountMinor / 100.0
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", value)
    }
}

@Composable
private fun GoalCardAction(
    icon: ImageVector,
    contentDescription: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.22f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun GoalFundHistoryInline(
    entryCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = stringResource(R.string.desc_toggle_fund_history),
            tint = colorScheme.accentInk,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.label_fund_history_inline),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        if (entryCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(
                    if (entryCount == 1) R.string.format_fund_entries_one
                    else R.string.format_fund_entries_count,
                    entryCount
                ),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun GoalFundHistorySection(
    entries: List<GoalFundEntry>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.label_no_fund_history),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 26.dp, top = 4.dp, bottom = 4.dp)
            )
        } else {
            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 26.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Colored dot indicator
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GoalProgressHigh)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Formatted amount
                    Text(
                        text = formatCurrencyValue(
                            entry.amountMinor / 100.0,
                            currencyId,
                            amountFormatPreferences
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    // Date
                    Text(
                        text = stringResource(
                            R.string.label_funded_on,
                            formatDate(entry.fundedAt, dateFormatPattern)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalsScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        GoalsScreenContent(
            goals = listOf(
                Goal(
                    id = "1",
                    name = "New Car",
                    targetAmountMinor = 2500000,
                    currentAmountMinor = 500000,
                    deadlineAt = System.currentTimeMillis() + 30L * DAY_MILLIS,
                    iconKey = "savings",
                    colorHex = "#7B61FF",
                    isCompleted = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                Goal(
                    id = "2",
                    name = "Emergency Fund",
                    targetAmountMinor = 1000000,
                    currentAmountMinor = 800000,
                    deadlineAt = System.currentTimeMillis() + 2L * DAY_MILLIS,
                    iconKey = "savings",
                    colorHex = "#7B61FF",
                    isCompleted = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ),
                Goal(
                    id = "3",
                    name = "Trip",
                    targetAmountMinor = 1000000,
                    currentAmountMinor = 1200000,
                    deadlineAt = null,
                    iconKey = "savings",
                    colorHex = "#7B61FF",
                    isCompleted = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            ),
            expandedGoalHistoryId = null,
            fundEntries = emptyMap(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
            onBackClick = {},
            onAddGoal = { _, _, _, _ -> },
            onFundGoal = { _, _ -> },
            onEditGoal = { _, _, _, _, _ -> },
            onDeleteGoal = {},
            onToggleFundHistory = {}
        )
    }
}
