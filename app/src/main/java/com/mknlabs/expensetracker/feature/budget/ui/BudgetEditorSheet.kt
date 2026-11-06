package com.mknlabs.expensetracker.feature.budget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.chipSelectedInk
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.BudgetPeriod
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.utils.formatCurrencyValue

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
internal fun BudgetEditorSheet(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    monthLabel: String,
    expenseCategories: List<CategoryType>,
    existingBudget: BudgetCategoryBudgetUi?,
    categoryTrackedMap: Map<Int, List<String>>,
    sessionKey: Any?,
    onDismiss: () -> Unit,
    onSave: (List<Int>, Double, String, BudgetPeriod) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isCategoryPickerVisible by rememberSaveable(existingBudget?.id, monthLabel, sessionKey) { mutableStateOf(false) }
    val categoryPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategoryIds by rememberSaveable(existingBudget?.id, monthLabel, sessionKey) {
        mutableStateOf(
            existingBudget?.categoryIds?.ifEmpty { listOf(existingBudget.categoryId) }?.toSet()
                ?: expenseCategories.firstOrNull()?.id?.let { setOf(it) }
                ?: emptySet()
        )
    }

    var nameInput by rememberSaveable(existingBudget?.id, monthLabel, sessionKey) {
        mutableStateOf(existingBudget?.name.orEmpty())
    }

    var selectedPeriod by rememberSaveable(existingBudget?.id, monthLabel, sessionKey) {
        mutableStateOf(existingBudget?.period ?: BudgetPeriod.MONTHLY)
    }

    var amountInput by rememberSaveable(existingBudget?.id, monthLabel, sessionKey) {
        mutableStateOf(
            existingBudget?.limitAmount
                ?.takeIf { it > 0.0 }
                ?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                .orEmpty()
        )
    }

    val selectedCategories = expenseCategories.filter { it.id in selectedCategoryIds }
    val limitAmount = amountInput.toDoubleOrNull()
    val isSaveEnabled = selectedCategoryIds.isNotEmpty() && limitAmount != null && limitAmount > 0.0

    val categoryFieldValue = when {
        selectedCategories.isEmpty() -> stringResource(id = R.string.msg_no_expense_categories_available)
        selectedCategories.size == 1 -> selectedCategories.first().name
        else -> "${selectedCategories.first().name} (+${selectedCategories.size - 1})"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (existingBudget == null) stringResource(id = R.string.title_add_budget) else stringResource(id = R.string.title_edit_budget),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = monthLabel,
                    color = MaterialTheme.colorScheme.accentInk,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppOutlinedFieldDefaults.shape,
                label = { Text(stringResource(id = R.string.label_budget_name_optional)) },
                placeholder = {
                    val placeholder = when {
                        selectedCategories.size == 1 -> selectedCategories.first().name
                        selectedCategories.size > 1 -> "${selectedCategories.first().name} (+${selectedCategories.size - 1})"
                        else -> stringResource(id = R.string.label_budget_group)
                    }
                    Text(placeholder)
                },
                colors = AppOutlinedFieldDefaults.colors()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.label_select_categories),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                SelectionDialogField(
                    label = "",
                    value = categoryFieldValue,
                    actionLabel = stringResource(id = R.string.label_change_caps),
                    enabled = expenseCategories.isNotEmpty(),
                    onClick = { isCategoryPickerVisible = true },
                    dropdownContent = {}
                )
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { updatedValue ->
                    amountInput = updatedValue.filter { character ->
                        character.isDigit() || character == '.'
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppOutlinedFieldDefaults.shape,
                label = { Text(stringResource(id = R.string.label_monthly_limit)) },
                placeholder = {
                    Text(formatCurrencyValue(5000.0, currencyId, amountFormatPreferences))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = AppOutlinedFieldDefaults.colors()
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(id = R.string.label_period),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BudgetPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(stringResource(period.labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.chipSelected,
                                selectedLabelColor = MaterialTheme.colorScheme.chipSelectedInk
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.label_cancel_1))
                }
                Spacer(modifier = Modifier.width(8.dp))
                AppTextButton(
                    onClick = {
                        if (selectedCategoryIds.isNotEmpty() && limitAmount != null) {
                            onSave(selectedCategoryIds.toList(), limitAmount, nameInput.trim(), selectedPeriod)
                        }
                    },
                    enabled = isSaveEnabled
                ) {
                    Text(
                        if (existingBudget == null) stringResource(id = R.string.label_save_1) else stringResource(id = R.string.label_update_1),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (isCategoryPickerVisible) {
        BudgetCategoryPickerSheet(
            categories = expenseCategories,
            selectedCategoryIds = selectedCategoryIds,
            categoryTrackedMap = categoryTrackedMap,
            sheetState = categoryPickerSheetState,
            onDismiss = { isCategoryPickerVisible = false },
            onSelectionChanged = { newCategoryIds ->
                selectedCategoryIds = newCategoryIds
                isCategoryPickerVisible = false
            }
        )
    }
}
