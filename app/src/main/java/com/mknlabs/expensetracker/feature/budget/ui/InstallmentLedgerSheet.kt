package com.mknlabs.expensetracker.feature.budget.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus
import com.mknlabs.expensetracker.utils.formatDate

/**
 * Installment ledger for one EMI rule: every scheduled slot with what it is
 * worth, what happened to it, and the three settlement actions.
 *
 * Paying is per slot and multi-selectable â€” the selected slots go up in one
 * action, and the repository's per-slot idempotency makes a double submission
 * (or a slot the worker settled meanwhile) harmless. Skipping waives the cycle
 * without withdrawing the money owed, and undo returns either kind of settled
 * slot to PENDING (withdrawing the transaction a payment had generated).
 *
 * Selection state is dropped whenever a slot stops being payable, so the
 * "Pay selected" count can never contain a slot that is no longer pending.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InstallmentLedgerSheet(
    expense: BudgetRecurringExpenseUi,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPay: (List<String>) -> Unit,
    onSkip: (String) -> Unit,
    onUndo: (String) -> Unit
) {
    val accent = budgetAccentColor(expense.accent)
    val progressLabel = stringResource(
        R.string.label_emi_paid_progress,
        expense.installmentPaidCount,
        expense.totalInstallments
    )
    val remainingLabel = stringResource(
        R.string.label_emi_remaining_format,
        expense.installmentRemainingLabel
    )
    var selectedIds by remember(expense.id) { mutableStateOf(emptySet<String>()) }
    val payableIds = remember(expense.slots) {
        expense.slots
            .filter { it.status == InstallmentOccurrenceStatus.PENDING }
            .map { it.id }
            .toSet()
    }
    // A slot may be settled by the worker (or undone) while the sheet is open:
    // keep only selections that are still payable.
    LaunchedEffect(payableIds) {
        selectedIds = selectedIds intersect payableIds
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.sheet,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.title_emis_for_format, expense.title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$progressLabel \u2022 $remainingLabel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecurringMetaChip(
                    label = expense.frequencyLabel,
                    accent = accent
                )
                RecurringMetaChip(
                    label = expense.dueLabel.asString(),
                    accent = accent
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(expense.slots, key = { it.id }) { slot ->
                    InstallmentSlotRow(
                        slot = slot,
                        accent = accent,
                        selected = slot.id in selectedIds,
                        onSelectedChange = { checked ->
                            selectedIds = if (checked) selectedIds + slot.id else selectedIds - slot.id
                        },
                        onPay = { onPay(listOf(slot.id)) },
                        onSkip = { onSkip(slot.id) },
                        onUndo = { onUndo(slot.id) }
                    )
                }
            }

            if (selectedIds.isNotEmpty()) {
                Button(
                    onClick = { onPay(selectedIds.toList()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.label_pay_selected_format, selectedIds.size),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
internal fun InstallmentSlotRow(
    slot: InstallmentSlotUi,
    accent: Color,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    onPay: () -> Unit,
    onSkip: () -> Unit,
    onUndo: () -> Unit
) {
    val isPending = slot.status == InstallmentOccurrenceStatus.PENDING
    val statusColor = when (slot.status) {
        InstallmentOccurrenceStatus.PAID -> MaterialTheme.colorScheme.accentInk
        InstallmentOccurrenceStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant
        InstallmentOccurrenceStatus.OVERDUE -> MaterialTheme.colorScheme.error
        InstallmentOccurrenceStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusLabel = stringResource(
        when (slot.status) {
            InstallmentOccurrenceStatus.PAID -> R.string.label_paid_caps
            InstallmentOccurrenceStatus.SKIPPED -> R.string.label_skipped_caps
            InstallmentOccurrenceStatus.OVERDUE -> R.string.label_overdue_caps
            InstallmentOccurrenceStatus.PENDING -> R.string.label_pending_caps
        }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Multi-select only ever applies to slots that can still be paid â€” a
        // settled slot changes through UNDO instead.
        if (isPending) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (selected) accent else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (selected) accent else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onSelectedChange(!selected) },
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.label_slot_index_format, slot.index) +
                    "  " + formatDate(slot.dueAt),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = slot.paidAt?.let { paidAt ->
                    slot.amountLabel + " \u2022 " + formatDate(paidAt)
                } ?: slot.amountLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            RecurringMetaChip(label = statusLabel, accent = statusColor)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isPending) {
                BudgetCardAction(
                    label = stringResource(R.string.action_pay_caps),
                    contentDescription = stringResource(R.string.action_pay_caps),
                    accent = accent,
                    onClick = onPay
                )
                AppTextButton(onClick = onSkip) {
                    Text(
                        text = stringResource(R.string.action_skip_caps),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            } else {
                AppTextButton(onClick = onUndo) {
                    Text(
                        text = stringResource(R.string.action_undo_caps),
                        color = MaterialTheme.colorScheme.accentInk,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}
