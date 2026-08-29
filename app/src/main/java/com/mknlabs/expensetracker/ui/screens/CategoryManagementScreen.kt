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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.mknlabs.expensetracker.data.constants.transactionList
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
import com.mknlabs.expensetracker.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.AppIconBox
import com.mknlabs.expensetracker.ui.viewmodels.CategoryManagementViewModel



import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CategoryManagementScreen(
    userProfile: UserProfile = defaultUserProfile,
    transactions: List<Transaction> = transactionList,
    onBackClick: () -> Unit = {},
    onCreateCustomCategory: (String, String, Int) -> Unit = { _, _, _ -> },
    onCreateCustomPaymentType: (String, String) -> Unit = { _, _ -> },
    onDeleteCustomCategory: (Int) -> Unit = {},
    onDeleteCustomPaymentType: (Int) -> Unit = {},
    onAddCategoryClick: (CategoryManagementTab) -> Unit = {},
    isAdsEnabled: Boolean = false
) {
    val categoryManagementViewModel: CategoryManagementViewModel = hiltViewModel()
    val uiState by categoryManagementViewModel.uiState.collectAsStateWithLifecycle()

    // The pager is the SINGLE source of truth for the visible tab.
    //
    // Previously the ViewModel ALSO drove the pager back (a second
    // `LaunchedEffect(activeTab) { animateScrollToPage(...) }`). That created a
    // feedback loop: for multi-page jumps (e.g. Income -> Payment) the
    // one-directional collector below reported the intermediate page while the
    // animation was crossing it, the ViewModel flipped to that intermediate tab,
    // the other effect restarted and CANCELLED the in-flight scroll, and the
    // screen got stranded on the wrong (Expense) tab. Tab clicks now animate the
    // pager directly and the collector below only ever follows the pager, so the
    // two can no longer fight.
    //
    // Route-owned (GEMINI.md Route/Content split): state initialization and
    // LaunchedEffect wiring live in the Route, not the previewable Content.
    val pagerState = rememberPagerState(initialPage = uiState.selectedTab.ordinal) {
        CategoryManagementTab.entries.size
    }

    // Follow the pager (swipes AND tab-click animations) into the ViewModel so
    // the tab highlight and the Add FAB stay in sync.
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        categoryManagementViewModel.selectTab(CategoryManagementTab.entries[pagerState.currentPage])
    }

    CategoryManagementContent(
        uiState = uiState,
        pagerState = pagerState,
        isAdsEnabled = isAdsEnabled,
        onBackClick = onBackClick,
        onDeleteCustomCategory = onDeleteCustomCategory,
        onDeleteCustomPaymentType = onDeleteCustomPaymentType,
        onAddCategoryClick = onAddCategoryClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryManagementContent(
    uiState: com.mknlabs.expensetracker.ui.viewmodels.CategoryManagementUiState,
    pagerState: androidx.compose.foundation.pager.PagerState,
    isAdsEnabled: Boolean,
    onBackClick: () -> Unit,
    onDeleteCustomCategory: (Int) -> Unit,
    onDeleteCustomPaymentType: (Int) -> Unit,
    onAddCategoryClick: (CategoryManagementTab) -> Unit
) {
    val activeTab = uiState.selectedTab
    val coroutineScope = rememberCoroutineScope()

    // Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteItem by remember { mutableStateOf<Pair<CategoryManagementItemUi, CategoryManagementTab>?>(null) }

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
                    // Animate the pager directly — it is the single source of
                    // truth for the visible tab (see note in CategoryManagementContent).
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(tab.ordinal)
                    }
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
                val animatingItems = when (currentTab) {
                    CategoryManagementTab.Income -> uiState.incomeItems
                    CategoryManagementTab.Expense -> uiState.expenseItems
                    CategoryManagementTab.Payment -> uiState.paymentItems
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = when (currentTab) {
                            CategoryManagementTab.Income -> stringResource(R.string.label_income_categories_count, animatingItems.size)
                            CategoryManagementTab.Expense -> stringResource(R.string.label_expense_categories_count, animatingItems.size)
                            CategoryManagementTab.Payment -> stringResource(R.string.label_payment_methods_count, animatingItems.size)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    AdaptiveContent(
                        maxWidth = 640.dp,
                        modifier = Modifier.weight(1f)
                    ) {
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
                                    pendingDeleteItem = Pair(item, currentTab)
                                    showDeleteDialog = true
                                }
                            )
                        }
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

    // Delete confirmation dialog
    if (showDeleteDialog && pendingDeleteItem != null) {
        val (item, tab) = pendingDeleteItem!!
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                pendingDeleteItem = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.label_delete_confirm),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.msg_delete_category_confirm, item.title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    when (tab) {
                        CategoryManagementTab.Income,
                        CategoryManagementTab.Expense -> {
                            onDeleteCustomCategory(item.id)
                        }
                        CategoryManagementTab.Payment -> {
                            onDeleteCustomPaymentType(item.id)
                        }
                    }
                    pendingDeleteItem = null
                }) {
                    Text(stringResource(R.string.label_delete_confirm), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    pendingDeleteItem = null
                }) {
                    Text(stringResource(R.string.label_cancel_confirm), fontWeight = FontWeight.Bold)
                }
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
                style = MaterialTheme.typography.titleSmall,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.subtitleRes?.let { stringResource(it) } ?: item.subtitle ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (item.isUserCreated) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), CircleShape)
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.content_desc_delete_item, item.title),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
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
        CategoryManagementContent(
            uiState = com.mknlabs.expensetracker.ui.viewmodels.CategoryManagementUiState(),
            pagerState = rememberPagerState(initialPage = 0) { CategoryManagementTab.entries.size },
            isAdsEnabled = true,
            onBackClick = {},
            onDeleteCustomCategory = {},
            onDeleteCustomPaymentType = {},
            onAddCategoryClick = {}
        )
    }
}
