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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedButton
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.models.CategoryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BudgetCategoryPickerSheet(
    categories: List<CategoryType>,
    selectedCategoryIds: Set<Int>,
    categoryTrackedMap: Map<Int, List<String>>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelectionChanged: (Set<Int>) -> Unit
) {
    var tempSelectedIds by remember(selectedCategoryIds) { mutableStateOf(selectedCategoryIds) }

    val conflictingTrackedCategories = tempSelectedIds.filter { catId ->
        categoryTrackedMap[catId]?.isNotEmpty() == true
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.label_select_categories),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(id = R.string.label_choose_from_all_expense_catego),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            if (conflictingTrackedCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(id = R.string.tip_duplicate_budget_tracking),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = categories,
                    key = { category -> category.id }
                ) { category ->
                    val isSelected = category.id in tempSelectedIds
                    val trackedInBudgets = categoryTrackedMap[category.id]

                    BudgetCategoryMultiPickerRow(
                        category = category,
                        isSelected = isSelected,
                        trackedInBudgets = trackedInBudgets,
                        onClick = {
                            tempSelectedIds = if (isSelected) {
                                tempSelectedIds - category.id
                            } else {
                                tempSelectedIds + category.id
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppOutlinedButton(
                    onClick = {
                        tempSelectedIds = categories.map { it.id }.toSet()
                    },
                    modifier = Modifier.weight(2f), // ~40% width
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.label_select_all),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AppOutlinedButton(
                    onClick = {
                        tempSelectedIds = categories
                            .filter { categoryTrackedMap[it.id].isNullOrEmpty() }
                            .map { it.id }
                            .toSet()
                    },
                    modifier = Modifier.weight(3f), // ~60% width
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.label_select_unbudgeted),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The Add Transaction screen's CTA, in both its fill and its ink: confirming this
            // picker is the same brand surface the user just pressed to get here, so it wears
            // `brandGradient()` -- the same ramp the selected rows' check marks already use --
            // rather than the flat `primary` a stock Button paints, which in dark mode is a
            // different, harder violet than the Add Transaction button beside it.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = brandGradient())
                    .clickable { onSelectionChanged(tempSelectedIds) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.label_add),
                    color = MaterialTheme.colorScheme.onCta,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun BudgetCategoryMultiPickerRow(
    category: CategoryType,
    isSelected: Boolean,
    trackedInBudgets: List<String>?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.accentInk.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Same rule as the single-select picker below: an unselected row wears the
            // category's own colour, and the selected row keeps the brand wash the checked
            // state is built from, so "selected" stays legible as one thing across the app.
            val categoryColor = MaterialTheme.colorScheme.categoryColor(
                categoryId = category.id,
                colorHex = category.colorHex
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.accentSoft
                        else MaterialTheme.colorScheme.categorySoft(categoryColor)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.accentInk else categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = category.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                if (!trackedInBudgets.isNullOrEmpty()) {
                    Text(
                        text = stringResource(id = R.string.label_already_tracked_in, trackedInBudgets.joinToString(", ")),
                        color = MaterialTheme.colorScheme.accentInk,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        if (isSelected) {
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
internal fun SelectionDialogField(
    label: String,
    value: String,
    actionLabel: String,
    enabled: Boolean,
    onClick: () -> Unit,
    dropdownContent: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(standardCardGradient())
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = actionLabel,
                    color = if (enabled) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                )
            }

            dropdownContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BudgetCategoryPickerSheet(
    categories: List<CategoryType>,
    selectedCategoryId: Int?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCategorySelected: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.label_select_category),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.label_choose_from_all_expense_catego),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(
                items = categories,
                key = { category -> category.id }
            ) { category ->
                BudgetCategoryPickerRow(
                    category = category,
                    isSelected = category.id == selectedCategoryId,
                    onClick = { onCategorySelected(category.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
internal fun BudgetCategoryPickerRow(
    category: CategoryType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rowShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.accentInk,
                        shape = rowShape
                    )
                } else {
                    Modifier
                }
            )
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                // Every row carries the category's own colour, so this picker reads as the
                // same set of categories the rest of the app draws. The chosen row is marked
                // by its accent edge alone, not by repainting the glyph.
                tint = MaterialTheme.colorScheme.categoryColor(
                    categoryId = category.id,
                    colorHex = category.colorHex
                ),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.label_expense_category),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

    }
}
