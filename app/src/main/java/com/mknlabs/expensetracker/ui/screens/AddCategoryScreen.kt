package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.constants.categoryIconOptions
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.ui.models.CategoryManagementTab
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.surfaceGradient
import com.mknlabs.expensetracker.ui.viewmodels.AddCategoryViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.mknlabs.expensetracker.R

@Composable
fun AddCategoryScreen(
    viewModel: AddCategoryViewModel,
    existingCategories: List<CategoryType>,
    existingPaymentMethods: List<PaymentType>,
    onBackClick: () -> Unit,
    onCategoryCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddCategoryScreenContent(
        uiState = uiState,
        existingCategories = existingCategories,
        existingPaymentMethods = existingPaymentMethods,
        onBackClick = onBackClick,
        onCategoryCreated = onCategoryCreated,
        onNameChange = viewModel::onNameChange,
        onIconSearchQueryChange = viewModel::onIconSearchQueryChange,
        onIconSelected = viewModel::onIconSelected,
        onSaveCategory = { viewModel.saveCategory(onCategoryCreated) }
    )
}

@Composable
private fun AddCategoryScreenContent(
    uiState: com.mknlabs.expensetracker.ui.viewmodels.AddCategoryUiState,
    existingCategories: List<CategoryType>,
    existingPaymentMethods: List<PaymentType>,
    onBackClick: () -> Unit,
    onCategoryCreated: () -> Unit,
    onNameChange: (String) -> Unit,
    onIconSearchQueryChange: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onSaveCategory: () -> Unit
) {
    val targetTab = uiState.targetTab

    val existingNamesForTarget = remember(targetTab, existingCategories, existingPaymentMethods) {
        when (targetTab) {
            CategoryManagementTab.Income -> {
                existingCategories.filter { it.transactionTypeId == 1 }.map { it.name }
            }
            CategoryManagementTab.Expense -> {
                existingCategories.filter { it.transactionTypeId == 2 }.map { it.name }
            }
            CategoryManagementTab.Payment -> {
                existingPaymentMethods.map { it.name }
            }
        }
    }

    val trimmedName = uiState.name.trim()
    val isDuplicateName = existingNamesForTarget.any { it.equals(trimmedName, ignoreCase = true) }
    val canCreate = trimmedName.isNotBlank() && !isDuplicateName && !uiState.isSaving

    val context = LocalContext.current
    val filteredIcons = remember(uiState.iconSearchQuery) {
        if (uiState.iconSearchQuery.isBlank()) {
            categoryIconOptions
        } else {
            categoryIconOptions.filter {
                context.getString(it.labelRes).contains(uiState.iconSearchQuery, ignoreCase = true)
            }
        }
    }

    val selectedIcon = remember(uiState.selectedIconId) {
        categoryIconOptions.firstOrNull { it.id == uiState.selectedIconId } ?: categoryIconOptions.first()
    }

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
                title = stringResource(R.string.title_add_category),
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                CategorySectionLabel(text = stringResource(R.string.label_category_type_section))
                Spacer(modifier = Modifier.height(12.dp))
                TypePreviewChip(targetTab = targetTab)

                Spacer(modifier = Modifier.height(28.dp))

                CategorySectionLabel(text = stringResource(R.string.label_category_name_section))
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = when (targetTab) {
                                CategoryManagementTab.Income -> stringResource(R.string.placeholder_income_example)
                                CategoryManagementTab.Expense -> stringResource(R.string.placeholder_expense_example)
                                CategoryManagementTab.Payment -> stringResource(R.string.placeholder_payment_example)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(brush = brandGradient()),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedIcon.icon,
                                contentDescription = stringResource(selectedIcon.labelRes),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    )
                )

                if (isDuplicateName) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.msg_name_already_exists, stringResource(targetTab.titleRes).lowercase()),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                CategorySectionLabel(text = stringResource(R.string.label_select_visual_identity_section))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.msg_choose_icons_info),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.iconSearchQuery,
                    onValueChange = onIconSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.placeholder_search_icons),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.iconSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { onIconSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.label_delete),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredIcons) { option ->
                            IconSelectionItem(
                                option = option,
                                selected = option.id == uiState.selectedIconId,
                                onClick = { onIconSelected(option.id) }
                            )
                        }
                    }

                    if (filteredIcons.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.msg_no_icons_found, uiState.iconSearchQuery),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Action Buttons at the bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(brush = surfaceGradient())
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.label_cancel).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Button(
                onClick = onSaveCategory,
                enabled = canCreate,
                modifier = Modifier
                    .weight(1.5f)
                    .height(58.dp)
                    .shadow(
                        elevation = if (canCreate) 8.dp else 0.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = if (canCreate) {
                                brandGradient()
                            } else {
                                brandGradient(alpha = 0.35f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (targetTab == CategoryManagementTab.Payment) stringResource(R.string.label_create_type) else stringResource(R.string.label_create_category),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
    )
}

@Composable
private fun TypePreviewChip(targetTab: CategoryManagementTab) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(brush = brandGradient()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (targetTab) {
                    CategoryManagementTab.Income -> Icons.AutoMirrored.Filled.TrendingUp
                    CategoryManagementTab.Expense -> Icons.Filled.Category
                    CategoryManagementTab.Payment -> Icons.Filled.Payments
                },
                contentDescription = stringResource(targetTab.titleRes),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Column {
            Text(
                text = when (targetTab) {
                    CategoryManagementTab.Payment -> stringResource(R.string.label_payment_type_item)
                    CategoryManagementTab.Income -> stringResource(R.string.label_income_category_item)
                    CategoryManagementTab.Expense -> stringResource(R.string.label_expense_category_item)
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.msg_item_added_under, stringResource(targetTab.titleRes)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun IconSelectionItem(
    option: CategoryIconOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = if (selected) 18.dp else 0.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(
                brush = if (selected) brandGradient() else surfaceGradient()
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = stringResource(option.labelRes),
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AddCategoryScreenContentPreview() {
    com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme {
        AddCategoryScreenContent(
            uiState = com.mknlabs.expensetracker.ui.viewmodels.AddCategoryUiState(),
            existingCategories = emptyList(),
            existingPaymentMethods = emptyList(),
            onBackClick = {},
            onCategoryCreated = {},
            onNameChange = {},
            onIconSearchQueryChange = {},
            onIconSelected = {},
            onSaveCategory = {}
        )
    }
}


