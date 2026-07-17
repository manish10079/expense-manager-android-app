package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.ui.models.CategoryManagementItemUi
import com.mknlabs.expensetracker.ui.models.CategoryManagementTab
import com.mknlabs.expensetracker.ui.models.TabItem
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.surfaceGradient
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import androidx.compose.foundation.BorderStroke
import com.mknlabs.expensetracker.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.AppIconBox
import com.mknlabs.expensetracker.ui.viewmodels.CategoryManagementViewModel

import com.mknlabs.expensetracker.data.constants.categoryFallbackDescriptions
import com.mknlabs.expensetracker.data.constants.paymentFallbackDescriptions

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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
    val categoryManagementViewModel: CategoryManagementViewModel = hiltViewModel()
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(customCategories, customPaymentTypes) {
        categoryManagementViewModel.updateInputs(
            customCategories = customCategories,
            customPaymentTypes = customPaymentTypes
        )
    }
    val uiState by categoryManagementViewModel.uiState.collectAsStateWithLifecycle()
    val activeTab = uiState.selectedTab

    val pagerState = rememberPagerState(initialPage = activeTab.ordinal) { CategoryManagementTab.entries.size }
    val coroutineScope = rememberCoroutineScope()

    // Sync pager with ViewModel state (when tab is clicked)
    androidx.compose.runtime.LaunchedEffect(activeTab) {
        if (pagerState.currentPage != activeTab.ordinal) {
            pagerState.animateScrollToPage(activeTab.ordinal)
        }
    }

    // Sync ViewModel with pager state (when user swipes)
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        categoryManagementViewModel.selectTab(CategoryManagementTab.entries[pagerState.currentPage])
    }

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
                onItemSelected = { tab ->
                    categoryManagementViewModel.selectTab(tab)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Inline Native Ad before category count text
            AdContainer(
                isAdsEnabled = isAdsEnabled,
                modifier = Modifier.padding(bottom = 18.dp)
            ) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val currentTab = CategoryManagementTab.entries[pageIndex]
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

                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = when (currentTab) {
                            CategoryManagementTab.Income -> stringResource(R.string.label_income_categories_count, animatingItems.size)
                            CategoryManagementTab.Expense -> stringResource(R.string.label_expense_categories_count, animatingItems.size)
                            CategoryManagementTab.Payment -> stringResource(R.string.label_payment_methods_count, animatingItems.size)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(22.dp))

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
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = standardCardGradient()
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(14.dp))

        AppIconBox(
            icon = item.icon,
            contentDescription = item.title,
            size = 50.dp,
            iconSize = 25.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
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
            Spacer(modifier = Modifier.width(8.dp))
            AppIconBox(
                icon = Icons.Filled.DeleteOutline,
                contentDescription = stringResource(R.string.content_desc_delete_item, item.title),
                size = 42.dp,
                iconSize = 20.dp,
                tint = MaterialTheme.colorScheme.error,
                backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                modifier = Modifier.clickable(onClick = onDeleteClick)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))
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
