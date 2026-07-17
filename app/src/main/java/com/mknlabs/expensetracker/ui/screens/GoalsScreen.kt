package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.GoalProgressHigh
import com.mknlabs.expensetracker.ui.theme.GoalProgressLow
import com.mknlabs.expensetracker.ui.theme.GoalProgressMedium
import com.mknlabs.expensetracker.ui.viewmodels.GoalsViewModel

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onBackClick: () -> Unit,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    var isAddGoalDialogVisible by rememberSaveable { mutableStateOf(false) }
    var fundingGoalId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteGoal by remember { mutableStateOf<Goal?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddGoalDialogVisible = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_goal))
            }
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
                    title = stringResource(R.string.title_savings_goals),
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(goals, key = { it.id }) { goal ->
                            GoalItem(
                                goal = goal,
                                currencyId = currencyId,
                                amountFormatPreferences = amountFormatPreferences,
                                onFund = { fundingGoalId = goal.id },
                                onDelete = { pendingDeleteGoal = goal }
                            )
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
            onDismiss = { isAddGoalDialogVisible = false },
            onSave = { name, amount ->
                viewModel.addGoal(name, amount)
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
                viewModel.fundGoal(id, amount)
                fundingGoalId = null
            }
        )
    }

    pendingDeleteGoal?.let { goal ->
        DeleteGoalDialog(
            goalName = goal.name,
            onDismiss = { pendingDeleteGoal = null },
            onConfirm = {
                viewModel.deleteGoal(goal.id)
                pendingDeleteGoal = null
            }
        )
    }
}

@Composable
fun DeleteGoalDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.title_delete_goal),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.msg_delete_goal_confirm, goalName),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.label_delete_1),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel_1))
            }
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
        title = {
            Text(
                text = stringResource(R.string.title_fund_goal),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(fundAmount) },
                enabled = isSaveEnabled
            ) {
                Text(stringResource(R.string.label_add_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel_1))
            }
        }
    )
}

@Composable
fun AddGoalDialog(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var amountInput by rememberSaveable { mutableStateOf("") }
    
    val targetAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isSaveEnabled = name.isNotBlank() && targetAmount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.title_add_goal),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, targetAmount) },
                enabled = isSaveEnabled
            ) {
                Text(stringResource(R.string.label_save_1))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel_1))
            }
        }
    )
}

@Composable
fun GoalItem(
    goal: Goal,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onFund: () -> Unit,
    onDelete: () -> Unit
) {
    val progressColor = when {
        goal.progress < 0.34f -> GoalProgressLow
        goal.progress < 0.67f -> GoalProgressMedium
        else -> GoalProgressHigh
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoalCardAction(
                        icon = Icons.Default.Add,
                        contentDescription = "Add Funds",
                        accent = MaterialTheme.colorScheme.primary,
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
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.format_goal_progress, (goal.progress * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        }
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
