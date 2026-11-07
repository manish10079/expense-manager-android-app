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
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Info
import com.mknlabs.expensetracker.core.ui.components.sortedByCatalog
import com.mknlabs.expensetracker.core.ui.components.CatalogSortSheet
import com.mknlabs.expensetracker.core.ui.components.CatalogSort
import com.adamglin.phosphoricons.regular.SortAscending
import com.adamglin.phosphoricons.regular.MagnifyingGlass

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
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.core.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementItemUi
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
import com.mknlabs.expensetracker.core.ui.models.TabItem
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCountInk
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryRowIconInk
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCard
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCardBorder
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.paymentColor
import com.mknlabs.expensetracker.core.ui.theme.surfaceGradient
import com.mknlabs.expensetracker.core.ui.theme.typeSwitcherFill
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry
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
internal fun CategoryManagementCard(
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
        // The mockup's category card fill (#151926) with its white 6% hairline in dark;
        // light keeps the standard white card.
        colors = AppCardDefaults.colors(
            darkContainer = colorScheme.manageCategoryCard,
            darkBorder = BorderStroke(1.dp, colorScheme.manageCategoryCardBorder)
        ),
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
                // The mockup's badge: a 56dp rounded-2xl tile, the hue at 15% with a 20%
                // edge, and a 28dp glyph.
                size = 56.dp,
                iconSize = 28.dp,
                tint = identityColor,
                backgroundColor = identityColor.copy(alpha = 0.15f),
                border = BorderStroke(
                    width = 1.dp,
                    color = identityColor.copy(alpha = 0.20f)
                ),
                shape = RoundedCornerShape(16.dp)
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
internal fun IconSelectionItem(
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

internal fun defaultIconIdFor(tab: CategoryManagementTab): String {
    return when (tab) {
        CategoryManagementTab.Income -> "wallet"
        CategoryManagementTab.Expense -> "shopping_cart"
        CategoryManagementTab.Payment -> "payments"
    }
}


