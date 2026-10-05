package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Info
import com.mknlabs.expensetracker.core.ui.components.sortedByCatalog
import com.mknlabs.expensetracker.core.ui.components.CatalogSortSheet
import com.mknlabs.expensetracker.core.ui.components.CatalogSort
import com.adamglin.phosphoricons.regular.SortAscending
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.BrandAddFab
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.core.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementItemUi
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
import com.mknlabs.expensetracker.core.ui.models.TabItem
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.paymentColor
import com.mknlabs.expensetracker.core.ui.theme.surfaceGradient
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import androidx.compose.foundation.BorderStroke
import com.mknlabs.expensetracker.core.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.core.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.AppIconBox
import com.mknlabs.expensetracker.core.ui.components.CategoryColorRow



import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
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
        onAddCategoryClick = onAddCategoryClick,
        // Recolouring is the ViewModel's job rather than a parameter threaded from the navigation
        // host, the way deletion is: it writes through the same repositories this screen already
        // observes, so the grid re-renders from the row rather than from a local guess.
        onRecolour = categoryManagementViewModel::updateColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryManagementContent(
    uiState: CategoryManagementUiState,
    pagerState: androidx.compose.foundation.pager.PagerState,
    isAdsEnabled: Boolean,
    onBackClick: () -> Unit,
    onDeleteCustomCategory: (Int) -> Unit,
    onDeleteCustomPaymentType: (Int) -> Unit,
    onAddCategoryClick: (CategoryManagementTab) -> Unit,
    onRecolour: (CategoryManagementItemUi, String?) -> Unit = { _, _ -> }
) {
    val activeTab = uiState.selectedTab
    val coroutineScope = rememberCoroutineScope()

    // Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteItem by remember { mutableStateOf<Pair<CategoryManagementItemUi, CategoryManagementTab>?>(null) }

    // The card whose colour is being changed, or null when the sheet is closed. Held here rather
    // than in the ViewModel because it is view state — which card the user tapped — and holding the
    // whole item means the sheet shows the row's current colour without a second lookup that could
    // disagree with the grid behind it.
    var colorEditingItem by remember { mutableStateOf<CategoryManagementItemUi?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var catalogSort by remember { mutableStateOf(CatalogSort.Newest) }
    var showSortSheet by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            delay(80)
            searchFocusRequester.requestFocus()
        }
    }
    var showCategoriesInfo by remember { mutableStateOf(false) }

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

            val enter = rememberSectionEnterAlphas(2)
            AppHeader(
                title = stringResource(R.string.title_manage_category),
                onBackClick = onBackClick,
                modifier = Modifier.alpha(enter[0])
            )

            Column(modifier = Modifier.alpha(enter[1]).weight(1f)) {
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

            val tabItems = when (activeTab) {
                CategoryManagementTab.Income -> uiState.incomeItems
                CategoryManagementTab.Expense -> uiState.expenseItems
                CategoryManagementTab.Payment -> uiState.paymentItems
            }
            val visibleItems = (if (searchQuery.isBlank()) {
                tabItems
            } else {
                tabItems.filter { it.title.contains(searchQuery, ignoreCase = true) }
            }).sortedByCatalog(catalogSort, createdAt = { it.createdAt }, name = { it.title })
            val countColor = MaterialTheme.colorScheme.onSurfaceVariant
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (activeTab) {
                        CategoryManagementTab.Income -> stringResource(R.string.label_income_categories_count, visibleItems.size)
                        CategoryManagementTab.Expense -> stringResource(R.string.label_expense_categories_count, visibleItems.size)
                        CategoryManagementTab.Payment -> stringResource(R.string.label_payment_methods_count, visibleItems.size)
                    },
                    color = countColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                    contentDescription = stringResource(R.string.desc_search_categories),
                    tint = countColor,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { isSearchExpanded = true }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = PhosphorIcons.Regular.SortAscending,
                    contentDescription = stringResource(R.string.desc_sort),
                    tint = countColor,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { showSortSheet = true }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = PhosphorIcons.Regular.Info,
                    contentDescription = stringResource(R.string.desc_categories_info),
                    tint = countColor,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { showCategoriesInfo = true }
                )
            }

            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.placeholder_search_manage_categories),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .focusRequester(searchFocusRequester),
                    leadingIcon = {
                        Icon(
                            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                            contentDescription = stringResource(R.string.desc_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            searchQuery = ""
                            isSearchExpanded = false
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.desc_close_search),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.accentInk,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 1
            ) { pageIndex ->
                val currentTab = CategoryManagementTab.entries[pageIndex]
                val pageItems = when (currentTab) {
                    CategoryManagementTab.Income -> uiState.incomeItems
                    CategoryManagementTab.Expense -> uiState.expenseItems
                    CategoryManagementTab.Payment -> uiState.paymentItems
                }
                val animatingItems = (if (searchQuery.isBlank()) {
                    pageItems
                } else {
                    pageItems.filter { it.title.contains(searchQuery, ignoreCase = true) }
                }).sortedByCatalog(catalogSort, createdAt = { it.createdAt }, name = { it.title })

                val gridState = rememberLazyGridState()
                LaunchedEffect(catalogSort, animatingItems.firstOrNull()?.id) {
                    gridState.scrollToItem(0)
                }

                AdaptiveContent(
                    maxWidth = 640.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // A grid, not a list. Each card is a glyph and a name, so it reads
                    // fine two-up and the list was wasting the horizontal half of every
                    // row. Adaptive columns fill whatever width the window offers — two
                    // on a phone, more up to the 640dp cap above — rather than fixing a
                    // count that would strand empty space on a tablet.
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        state = gridState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = animatingItems,
                            key = { item -> item.id },
                            contentType = { "category_management_item" }
                        ) { item ->
                            CategoryManagementCard(
                                item = item,
                                onClick = { colorEditingItem = item },
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

        // The shared brand "+" FAB at its standard diameter, so this button is the same
        // size as the one docked in the navigation bar and the one on the goals screen.
        // Only the shadow is heavier — it floats over the list with no bar beside it to
        // carry its edge — and that is a comment, not a size, so the three still read as
        // one button.
        BrandAddFab(
            onClick = { onAddCategoryClick(activeTab) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 22.dp, bottom = 28.dp),
            contentDescription = stringResource(R.string.desc_add_category),
            shadowElevation = 22.dp
        )
    }

    if (showCategoriesInfo) {
        CategoriesInfoDialog(onDismiss = { showCategoriesInfo = false })
    }
    if (showSortSheet) {
        CatalogSortSheet(
            selected = catalogSort,
            onSelect = { catalogSort = it },
            onDismiss = { showSortSheet = false }
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
            containerColor = MaterialTheme.colorScheme.sheet,
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
                AppTextButton(onClick = {
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
                    Text(
                        text = stringResource(R.string.label_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                AppTextButton(onClick = {
                    showDeleteDialog = false
                    pendingDeleteItem = null
                }) {
                    Text(stringResource(R.string.label_cancel_confirm), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Recolour sheet. Dismissed before the write is dispatched, so the sheet is not left on screen
    // waiting for a database round trip — the card behind it updates from the observed row.
    colorEditingItem?.let { item ->
        CategoryColorSheet(
            item = item,
            onDismiss = { colorEditingItem = null },
            onColorSelected = { colorHex ->
                onRecolour(item, colorHex)
                colorEditingItem = null
            }
        )
    }
}

/**
 * The recolour sheet: the same swatch row the create screen uses, over one existing row.
 *
 * It applies on tap and closes, rather than offering a Save. There is one setting here and the
 * choice of a swatch *is* the decision — a confirm button would only add a way to leave with the
 * tap undone, which is not a state a user can be in on purpose. The grid behind re-renders from the
 * observed row, so what is on screen after the tap is what was stored.
 *
 * The default swatch is the way back: it clears the override and returns the row to the palette
 * colour for its id, which is also why the sheet works the same for a seeded row and a
 * user-created one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryColorSheet(
    item: CategoryManagementItemUi,
    onDismiss: () -> Unit,
    onColorSelected: (String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // The row's own name, not a generic title: with the sheet open over a grid of fifteen
            // cards, "Change color" alone would not say which one is being changed.
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stringResource(R.string.msg_change_color_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            CategoryColorRow(
                selectedColorHex = item.colorHex,
                onColorSelected = onColorSelected
            )
        }
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
                        MaterialTheme.colorScheme.accentInk.copy(alpha = 0.14f),
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
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // The card is where a user checks that the colour they picked for their new category is
    // the colour it actually wears, so the glyph takes it — resolved here rather than in the
    // ViewModel, because it has to follow the theme toggle and not the theme at build time.
    val colorScheme = MaterialTheme.colorScheme
    val identityColor = if (item.isPaymentMethod) {
        colorScheme.paymentColor(paymentId = item.id, colorHex = item.colorHex)
    } else {
        colorScheme.categoryColor(categoryId = item.id, colorHex = item.colorHex)
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            // A floor rather than a fixed height, so cards sharing a grid row agree on a
            // size instead of each hugging its own title length.
            .heightIn(min = 142.dp)
            // Tapping the card changes its colour, which is the only edit a built-in row has:
            // the name and the icon of a seeded category are the app's, and the colour is the
            // part the user owns. The 'x' stays a separate target for the rows that may be
            // deleted, so "open the colour sheet" and "delete this" cannot be confused.
            .clickable(onClick = onClick),
        // The gradient is the dark surface and this card's only fill, so the container
        // beneath it stays transparent; light takes the standard white card.
        brush = darkOnlyGradient(standardCardGradient()),
        shape = AppCardDefaults.shape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIconBox(
                icon = item.icon,
                contentDescription = item.title,
                size = 48.dp,
                iconSize = 24.dp,
                tint = identityColor,
                // The palette's own wash rather than AppIconBox's flat 10%, so the tile splits
                // light-10 / dark-14 like every other glyph tile in the system.
                backgroundColor = colorScheme.categorySoft(identityColor),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // The name, and nothing else. The line that used to sit under it described
            // the category, but no category or payment method has ever carried a
            // description — neither model has the field — so it was a hardcoded fallback
            // string labelling the user's own entries with a canned sentence.
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        if (item.isUserCreated) {
            // Only the user's own entries are deletable, so the 'x' is also the signal
            // that a card is theirs — a built-in one has no control at all.
            //
            // The glyph carries the muted secondary ink the row's subtitle used, not a
            // red one: a red control on every card turned the grid into a wall of
            // warnings, and the dialog is where the caveat belongs. The box is the 48dp
            // a11y minimum touch target, which the 28dp control it replaces missed.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.content_desc_delete_item, item.title),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
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
                ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.34f),
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
            tint = if (selected) MaterialTheme.colorScheme.onCta else MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun CategoriesInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.sheet,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.title_what_are_categories),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.msg_categories_what),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.msg_categories_where),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.label_categories_example),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.label_category_example_name),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(brandGradient())
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.label_close),
                        color = MaterialTheme.colorScheme.onBrandGradient,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryManagementScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        CategoryManagementContent(
            uiState = CategoryManagementUiState(),
            pagerState = rememberPagerState(initialPage = 0) { CategoryManagementTab.entries.size },
            isAdsEnabled = true,
            onBackClick = {},
            onDeleteCustomCategory = {},
            onDeleteCustomPaymentType = {},
            onAddCategoryClick = {}
        )
    }
}
