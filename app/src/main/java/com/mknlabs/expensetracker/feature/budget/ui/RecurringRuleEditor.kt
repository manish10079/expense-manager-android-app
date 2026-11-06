package com.mknlabs.expensetracker.feature.budget.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.chipSelectedInk
import com.mknlabs.expensetracker.core.ui.theme.menu
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringPlanEdit
import com.mknlabs.expensetracker.models.RecurringType
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecurringRuleEditorModal(
    rule: BudgetRecurringExpenseUi,
    onDismiss: () -> Unit,
    onSave: (RecurringFrequency, Int) -> Unit,
    onSavePlan: (RecurringFrequency, RecurringPlanEdit) -> Unit,
    onConvertToRegular: () -> Unit
) {
    var selectedType by remember {
        mutableStateOf(if (rule.isInstallment) RecurringType.INSTALLMENT else RecurringType.REGULAR)
    }
    var selectedFrequency by remember { mutableStateOf(rule.frequency) }
    var installmentsInput by remember { mutableStateOf(rule.totalInstallments.toString()) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }

    // EMI fields â€” prefilled from the rule when it already carries a plan, so
    // editing an existing loan shows its real terms.
    var totalInput by remember { mutableStateOf(rule.installmentTotalAmount.formatForInput()) }
    var installmentInput by remember { mutableStateOf(rule.installmentPerAmount.formatForInput()) }
    var firstDueAt by remember {
        mutableStateOf(rule.firstDueAt.takeIf { it > 0L } ?: System.currentTimeMillis())
    }
    var isFirstDuePickerVisible by remember { mutableStateOf(false) }
    var showRegularConfirm by remember { mutableStateOf(false) }

    val count = installmentsInput.toIntOrNull() ?: rule.totalInstallments
    val totalAmount = totalInput.toDoubleOrNull() ?: 0.0
    val installmentAmount = installmentInput.toDoubleOrNull() ?: 0.0
    val planValid = count > 0 && totalAmount > 0.0 && installmentAmount > 0.0
    // The recurring date is 12:00 local â€” slot dates inherit it, so "same
    // total as count Ã— installment" stays exact regardless of month lengths.
    val planConsistent = totalAmount == installmentAmount * count
    // Nothing editable changed â€” saving would rewrite sync timestamps for no
    // visible reason, so the button stays disabled. (count <= 0 mirrors the old
    // guard that never let a REGULAR rule save a zero repeat count.)
    val regularInvalid = selectedType == RecurringType.REGULAR && count <= 0
    val regularUnchanged = regularInvalid ||
        (selectedType == RecurringType.REGULAR &&
            selectedFrequency == rule.frequency && count == rule.totalInstallments)
    val planUnchanged =
        selectedType == RecurringType.INSTALLMENT &&
            selectedFrequency == rule.frequency &&
            rule.isInstallment &&
            totalAmount == rule.installmentTotalAmount &&
            installmentAmount == rule.installmentPerAmount &&
            count == rule.totalInstallments &&
            firstDueAt == rule.firstDueAt

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    fun autoCalculateTotal(perInstStr: String, countInt: Int) {
        val perInst = perInstStr.toDoubleOrNull()
        if (perInst != null && perInst > 0.0 && countInt > 0) {
            totalInput = (perInst * countInt).formatForInput()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
        dragHandle = {
            // The drag-handle slot is laid out as a Box by the sheet host, so padding
            // must live on the handle itself. A sibling Spacer overlapped the pill and
            // pinned it flush to the sheet's top edge.
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.title_edit_recurring),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )

                // Type selector â€” REGULAR stays exactly the legacy editor;
                // INSTALLMENT swaps the repeat-count row for the plan terms.
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.label_recurring_type),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = selectedType == RecurringType.REGULAR,
                            onClick = {
                                if (rule.isInstallment && selectedType != RecurringType.REGULAR) {
                                    showRegularConfirm = true
                                } else {
                                    selectedType = RecurringType.REGULAR
                                }
                            },
                            label = { Text(stringResource(id = R.string.label_type_regular)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.chipSelected,
                                selectedLabelColor = MaterialTheme.colorScheme.chipSelectedInk
                            )
                        )
                        FilterChip(
                            selected = selectedType == RecurringType.INSTALLMENT,
                            onClick = {
                                selectedType = RecurringType.INSTALLMENT
                                if (totalInput.isBlank() || totalAmount == 0.0) {
                                    autoCalculateTotal(installmentInput, count)
                                }
                            },
                            label = { Text(stringResource(id = R.string.label_type_emi)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.chipSelected,
                                selectedLabelColor = MaterialTheme.colorScheme.chipSelectedInk
                            )
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.label_frequency_capitalized),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedFrequency.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.accentInk
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = MaterialTheme.colorScheme.accentInk,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { isFrequencyDropdownExpanded = true }
                        )

                        DropdownMenu(
                            expanded = isFrequencyDropdownExpanded,
                            onDismissRequest = { isFrequencyDropdownExpanded = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.menu)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        ) {
                            RecurringFrequency.entries.forEach { frequency ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = frequency.label,
                                            color = if (frequency == selectedFrequency) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        selectedFrequency = frequency
                                        isFrequencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                AnimatedContent(
                    targetState = selectedType,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) +
                            slideInVertically(animationSpec = tween(220, easing = LinearOutSlowInEasing)) { height -> height / 6 })
                            .togetherWith(
                                fadeOut(animationSpec = tween(180)) +
                                    slideOutVertically(animationSpec = tween(180)) { height -> -height / 6 }
                            )
                    },
                    label = "recurring_type_transition"
                ) { targetType ->
                    if (targetType == RecurringType.REGULAR) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(id = R.string.label_total_installments),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelLarge
                            )

                            OutlinedTextField(
                                value = installmentsInput,
                                onValueChange = { input ->
                                    if (input.length <= 3 && input.all { char -> char.isDigit() }) {
                                        installmentsInput = input
                                        autoCalculateTotal(installmentInput, input.toIntOrNull() ?: 0)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = AppOutlinedFieldDefaults.shape,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = AppOutlinedFieldDefaults.colors()
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(id = R.string.label_emi_total_amount),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                EditorAmountField(
                                    value = totalInput,
                                    onValueChange = { totalInput = it }
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(id = R.string.label_emi_installment_amount),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                EditorAmountField(
                                    value = installmentInput,
                                    onValueChange = { newInst ->
                                        installmentInput = newInst
                                        autoCalculateTotal(newInst, count)
                                    }
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(id = R.string.label_total_installments),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                OutlinedTextField(
                                    value = installmentsInput,
                                    onValueChange = { input ->
                                        if (input.length <= 3 && input.all { char -> char.isDigit() }) {
                                            installmentsInput = input
                                            autoCalculateTotal(installmentInput, input.toIntOrNull() ?: 0)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = AppOutlinedFieldDefaults.shape,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = AppOutlinedFieldDefaults.colors()
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = stringResource(id = R.string.label_emi_first_due),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                                OutlinedTextField(
                                    value = editorDateFormatter.format(Date(firstDueAt)),
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isFirstDuePickerVisible = true },
                                    enabled = false,
                                    shape = RoundedCornerShape(14.dp),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.accentInk
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        cursorColor = MaterialTheme.colorScheme.accentInk,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                            if (!planConsistent && totalAmount > 0.0 && installmentAmount > 0.0) {
                                Text(
                                    text = stringResource(id = R.string.msg_emi_amount_mismatch),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.label_cancel_caps), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                AppTextButton(
                    onClick = {
                        when {
                            selectedType == RecurringType.REGULAR -> onSave(selectedFrequency, count)
                            planValid -> onSavePlan(
                                selectedFrequency,
                                RecurringPlanEdit(
                                    totalAmountMinor = totalAmount.toMinorUnits(),
                                    installmentAmountMinor = installmentAmount.toMinorUnits(),
                                    totalInstallments = count,
                                    firstDueAt = firstDueAt
                                )
                            )
                        }
                    },
                    enabled = !if (selectedType == RecurringType.REGULAR) regularUnchanged else (planUnchanged || !planValid || !planConsistent),
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            MaterialTheme.colorScheme.accentInk.copy(
                                alpha = if (
                                    if (selectedType == RecurringType.REGULAR) regularUnchanged
                                    else (planUnchanged || !planValid || !planConsistent)
                                ) 0.4f else 1f
                            ),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        stringResource(id = R.string.label_save_changes_caps),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    if (isFirstDuePickerVisible) {
        WheelDateTimePickerModal(
            mode = WheelPickerMode.SINGLE_DATE,
            initialStartMillis = firstDueAt,
            onDismissRequest = { isFirstDuePickerVisible = false },
            onConfirm = { start, _ ->
                firstDueAt = start
                isFirstDuePickerVisible = false
            }
        )
    }

    // Switching an EMI rule back to REGULAR is confirmed first: the plan is
    // only hidden (soft-deleted), and the info line says so, so the user
    // knows paid history is not at risk.
    if (showRegularConfirm) {
        ModernConfirmationDialog(
            onDismissRequest = { showRegularConfirm = false },
            title = stringResource(id = R.string.label_type_regular),
            text = stringResource(id = R.string.msg_convert_to_regular_info),
            confirmButtonText = stringResource(id = R.string.label_save_changes_caps),
            onConfirm = {
                showRegularConfirm = false
                onConvertToRegular()
            },
            headerIcon = Icons.Filled.SwapHoriz,
            dismissButtonText = stringResource(id = R.string.label_cancel_caps),
            onDismiss = { showRegularConfirm = false }
        )
    }
}


internal val editorDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
