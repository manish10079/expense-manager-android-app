package com.mknlabs.expensetracker.feature.budget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppIconBox
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.utils.formatCurrencyValue

internal enum class BudgetCopyMode {
    All,
    Selected
}

internal data class PendingBudgetCopy(
    val mode: BudgetCopyMode,
    val budgetIds: List<String>
)

@Composable
internal fun CopyPreviousMonthBudgetsAction(
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isLocked) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.accentInk
    }
    val borderColor = if (isLocked) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.accentInk.copy(alpha = 0.45f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLocked) {
            val lockColor = MaterialTheme.colorScheme.featureGateLock
            AppIconBox(
                icon = Icons.Rounded.Lock,
                contentDescription = stringResource(id = R.string.title_copy_previous_month_budgets),
                size = 34.dp,
                iconSize = 18.dp,
                tint = lockColor,
                backgroundColor = lockColor.copy(alpha = 0.14f)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = stringResource(id = R.string.title_copy_previous_month_budgets),
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(id = R.string.title_copy_previous_month_budgets),
            color = contentColor,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 1.1.sp
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CopyPreviousMonthBudgetsSheet(
    candidates: List<BudgetCopyCandidateUi>,
    availableCategories: List<CategoryType>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    previousMonthLabel: String,
    currentMonthLabel: String,
    onDismiss: () -> Unit,
    onCreateBudget: () -> Unit,
    onCopyAll: () -> Unit,
    onCopySelected: (List<String>) -> Unit
) {
    var checkedIds by remember(candidates.map { it.id }) { mutableStateOf(emptySet<String>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.sheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 4.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(id = R.string.title_copy_previous_month_budgets),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )
                Text(
                    text = stringResource(
                        id = R.string.msg_copy_previous_budgets_context,
                        previousMonthLabel,
                        currentMonthLabel
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (candidates.isEmpty()) {
                CopyPreviousMonthEmptyState(onCreateBudget = onCreateBudget)
            } else {
                Button(
                    onClick = onCopyAll,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onCta
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(brush = brandGradient(), shape = RoundedCornerShape(16.dp))
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onCta,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.action_copy_all),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = candidates, key = { it.id }) { candidate ->
                        CopyBudgetRow(
                            candidate = candidate,
                            isChecked = candidate.id in checkedIds,
                            availableCategories = availableCategories,
                            currencyId = currencyId,
                            amountFormatPreferences = amountFormatPreferences,
                            onCheckedChange = { isChecked ->
                                checkedIds = if (isChecked) {
                                    checkedIds + candidate.id
                                } else {
                                    checkedIds - candidate.id
                                }
                            }
                        )
                    }
                }

                Button(
                    onClick = { onCopySelected(checkedIds.toList()) },
                    enabled = checkedIds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onCta,
                        disabledContentColor = MaterialTheme.colorScheme.onCta.copy(alpha = 0.6f)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = brandGradient(alpha = if (checkedIds.isNotEmpty()) 1f else 0.45f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.action_copy_selected),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CopyBudgetRow(
    candidate: BudgetCopyCandidateUi,
    isChecked: Boolean,
    availableCategories: List<CategoryType>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onCheckedChange: (Boolean) -> Unit
) {
    val firstCategory = candidate.categoryIds.firstOrNull()?.let { categoryId ->
        availableCategories.firstOrNull { it.id == categoryId }
    }
    val title = when {
        candidate.name.isNotBlank() -> candidate.name
        candidate.categoryIds.size == 1 -> firstCategory?.name
            ?: stringResource(id = R.string.label_budget_group)
        candidate.categoryIds.size > 1 && firstCategory != null ->
            "${firstCategory.name} (+${candidate.categoryIds.size - 1})"
        else -> stringResource(id = R.string.label_budget_group)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = if (isChecked) {
                    MaterialTheme.colorScheme.accentInk.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = firstCategory?.icon ?: Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.accentInk,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatCurrencyValue(
                        amount = candidate.limitAmount,
                        currencyId = currencyId,
                        amountFormatPreferences = amountFormatPreferences
                    ),
                    color = MaterialTheme.colorScheme.accentInk,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Circular selection indicator on the right, matching the budget/category picker style.
        if (isChecked) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(brush = brandGradient()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onCta,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
            )
        }
    }
}


@Composable
internal fun CopyPreviousMonthEmptyState(
    onCreateBudget: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(42.dp)
        )

        Text(
            text = stringResource(id = R.string.empty_copy_previous_month_title),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
            )
        )

        Text(
            text = stringResource(id = R.string.msg_copy_previous_month_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onCreateBudget,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.action_create_budget),
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
            )
        }
    }
}

@Composable
internal fun ConfirmCopyBudgetsDialog(
    monthLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModernConfirmationDialog(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.title_copy_budgets_confirmation),
        text = stringResource(id = R.string.msg_copy_budgets_confirmation, monthLabel),
        confirmButtonText = stringResource(id = R.string.action_copy),
        onConfirm = onConfirm,
        headerIcon = Icons.Filled.ContentCopy,
        dismissButtonText = stringResource(id = R.string.label_cancel_1),
        onDismiss = onDismiss
    )
}
