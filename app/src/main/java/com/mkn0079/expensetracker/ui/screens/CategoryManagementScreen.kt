package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.res.stringResource
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.ui.models.CategoryIconOption
import com.mkn0079.expensetracker.ui.models.CategoryManagementItemUi
import com.mkn0079.expensetracker.ui.models.CategoryManagementTab
import com.mkn0079.expensetracker.ui.models.TabItem
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.brandGradient
import com.mkn0079.expensetracker.ui.theme.surfaceGradient
import com.mkn0079.expensetracker.ui.components.AnimatedTabSwitcher
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.viewmodels.CategoryManagementViewModel

import com.mkn0079.expensetracker.data.constants.categoryIconOptions
import com.mkn0079.expensetracker.data.constants.categoryFallbackDescriptions
import com.mkn0079.expensetracker.data.constants.paymentFallbackDescriptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    userProfile: UserProfile = defaultUserProfile,
    transactions: List<Transaction> = transactionList,
    customCategories: List<CategoryType> = emptyList(),
    customPaymentTypes: List<PaymentType> = emptyList(),
    onBackClick: () -> Unit = {},
    onCreateCustomCategory: (String, String, Int) -> Unit = { _, _, _ -> },
    onCreateCustomPaymentType: (String, String) -> Unit = { _, _ -> },
    onDeleteCustomCategory: (Int) -> Unit = {},
    onDeleteCustomPaymentType: (Int) -> Unit = {},
    onAddCategoryClick: (CategoryManagementTab) -> Unit = {}
) {
    val categoryManagementViewModel: CategoryManagementViewModel = viewModel()
    androidx.compose.runtime.LaunchedEffect(customCategories, customPaymentTypes) {
        categoryManagementViewModel.updateInputs(
            customCategories = customCategories,
            customPaymentTypes = customPaymentTypes
        )
    }
    val uiState by categoryManagementViewModel.uiState.collectAsStateWithLifecycle()
    val activeTab = uiState.selectedTab

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CategoryManagementGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            AppHeader(
                title = stringResource(R.string.title_manage_category),
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedTabSwitcher(
                items = CategoryManagementTab.entries.map { TabItem(it, stringResource(it.titleRes)) },
                selectedItemId = activeTab,
                onItemSelected = categoryManagementViewModel::selectTab
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = when (activeTab) {
                    CategoryManagementTab.Income -> stringResource(R.string.label_income_categories_count, uiState.itemCount)
                    CategoryManagementTab.Expense -> stringResource(R.string.label_expense_categories_count, uiState.itemCount)
                    CategoryManagementTab.Payment -> stringResource(R.string.label_payment_methods_count, uiState.itemCount)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    val targetIndex = CategoryManagementTab.entries.indexOf(targetState)
                    val initialIndex = CategoryManagementTab.entries.indexOf(initialState)
                    val direction = if (targetIndex > initialIndex) 1 else -1

                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { fullWidth -> fullWidth * direction }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { fullWidth -> -fullWidth * direction }
                    )
                },
                label = "category_tab_content_animation",
                modifier = Modifier.fillMaxSize()
            ) { currentTab ->
                val incomeSourceDesc = stringResource(R.string.desc_income_source)
                val expenseCategoryDesc = stringResource(R.string.desc_expense_category)
                val paymentMethodFallbackDesc = stringResource(R.string.desc_payment_method_fallback)

                val animatingItems = remember(currentTab, customCategories, customPaymentTypes, incomeSourceDesc, expenseCategoryDesc, paymentMethodFallbackDesc) {
                    when (currentTab) {
                        CategoryManagementTab.Income -> buildCategoryManagementItems(customCategories, 1, incomeSourceDesc)
                        CategoryManagementTab.Expense -> buildCategoryManagementItems(customCategories, 2, expenseCategoryDesc)
                        CategoryManagementTab.Payment -> {
                            val customItems = customPaymentTypes.sortedByDescending { it.id }
                            val builtinItems = paymentTypeMap.values.sortedBy { it.id }
                            (customItems + builtinItems).map { payment ->
                                CategoryManagementItemUi(
                                    id = payment.id,
                                    title = payment.name,
                                    subtitleRes = paymentFallbackDescriptions[payment.id],
                                    subtitle = if (paymentFallbackDescriptions[payment.id] == null) paymentMethodFallbackDesc else null,
                                    icon = payment.icon,
                                    isUserCreated = payment.id !in paymentTypeMap
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(
                        items = animatingItems,
                        key = { item -> item.id },
                        contentType = { "category_management_item" }
                    ) { item ->
                        CategoryManagementCard(
                            item = item,
                            onDeleteClick = {
                                when (currentTab) {
                                    CategoryManagementTab.Income,
                                    CategoryManagementTab.Expense -> {
                                        onDeleteCustomCategory(item.id)
                                    }

                                    CategoryManagementTab.Payment -> {
                                        onDeleteCustomPaymentType(item.id)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        AddCategoryFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 22.dp, bottom = 28.dp),
            onClick = {
                onAddCategoryClick(activeTab)
            }
        )
    }
}

@Composable
private fun BoxScope.CategoryManagementGlow() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 92.dp)
            .size(width = 260.dp, height = 190.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                    )
                ),
                shape = CircleShape
            )
    )
}





@Composable
private fun CategoryManagementCard(
    item: CategoryManagementItemUi,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(34.dp))
            .background(
                brush = surfaceGradient()
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                shape = RoundedCornerShape(34.dp)
            )
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.subtitleRes?.let { stringResource(it) } ?: item.subtitle ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }

        if (item.isUserCreated) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = stringResource(R.string.content_desc_delete_item, item.title),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
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
            .size(78.dp)
            .shadow(
                elevation = if (selected) 18.dp else 0.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(
                brush = if (selected) {
                    brandGradient()
                } else {
                    surfaceGradient()
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = stringResource(option.labelRes),
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(30.dp)
        )
    }
}

private fun defaultIconIdFor(tab: CategoryManagementTab): String {
    return when (tab) {
        CategoryManagementTab.Income -> "wallet"
        CategoryManagementTab.Expense -> "shopping_cart"
        CategoryManagementTab.Payment -> "payments"
    }
}

private fun buildCategoryManagementItems(
    categories: List<CategoryType>,
    transactionTypeId: Int,
    fallbackSubtitle: String
): List<CategoryManagementItemUi> {
    val customItems = categories
        .filter { it.transactionTypeId == transactionTypeId }
        .sortedByDescending { it.id }
    val builtinItems = categoryMap.values
        .filter { it.transactionTypeId == transactionTypeId }
        .sortedBy { it.id }

    return (customItems + builtinItems).map { category ->
        CategoryManagementItemUi(
            id = category.id,
            title = category.name,
            subtitleRes = categoryFallbackDescriptions[category.id],
            subtitle = if (categoryFallbackDescriptions[category.id] == null) fallbackSubtitle else null,
            icon = category.icon,
            isUserCreated = category.id !in categoryMap
        )
    }
}

@Composable
private fun AddCategoryFab(
        modifier: Modifier = Modifier,
        onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(66.dp)
                .shadow(
                    elevation = 22.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f)
                )
                .clip(CircleShape)
                .background(
                    brush = brandGradient()
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.desc_add_category),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryManagementScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        CategoryManagementScreen()
    }
}
