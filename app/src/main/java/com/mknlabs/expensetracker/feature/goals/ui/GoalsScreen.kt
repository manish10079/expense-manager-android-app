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
        onAddGoal = { name, amount, deadline, iconKey, initial, colorHex ->
            viewModel.addGoal(name, amount, deadline, iconKey, initial, colorHex)
        },
        onFundGoal = { id, amount -> viewModel.fundGoal(id, amount) },
        onEditGoal = { id, name, amount, deadline, iconKey, colorHex ->
            viewModel.updateGoal(id, name, amount, deadline, iconKey, colorHex)
        },
        onDeleteGoal = { viewModel.deleteGoal(it.id) },
        onToggleFundHistory = { goalId -> viewModel.toggleGoalHistory(goalId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalsScreenContent(
    goals: List<Goal>,
    expandedGoalHistoryId: String?,
    fundEntries: Map<String, List<GoalFundEntry>>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    onBackClick: () -> Unit,
    onAddGoal: (String, Double, Long?, String, Double, String) -> Unit,
    onFundGoal: (String, Double) -> Unit,
    onEditGoal: (String, String, Double, Long?, String, String) -> Unit,
    onDeleteGoal: (Goal) -> Unit,
    onToggleFundHistory: (String) -> Unit
) {
    var isAddGoalDialogVisible by rememberSaveable { mutableStateOf(false) }
    var fundingGoalId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingGoalId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteGoal by remember { mutableStateOf<Goal?>(null) }

    val activeGoals = goals.filter { !it.isCompleted }
    val completedGoals = goals.filter { it.isCompleted }
    val enter = rememberSectionEnterAlphas(2)

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
                .appScreenBackground()
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
                    onBackClick = onBackClick,
                    modifier = Modifier.alpha(enter[0])
                )

                if (goals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .alpha(enter[1]),
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
                            .fillMaxWidth()
                            .alpha(enter[1]),
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
            onSave = { name, amount, deadline, iconKey, initial, colorHex ->
                onAddGoal(name, amount, deadline, iconKey, initial, colorHex)
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
                onSave = { name, amount, deadline, iconKey, colorHex ->
                    onEditGoal(id, name, amount, deadline, iconKey, colorHex)
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


