package com.mknlabs.expensetracker.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Info
import com.mknlabs.expensetracker.core.ui.components.sortedByCatalog
import com.mknlabs.expensetracker.core.ui.components.CatalogSortSheet
import com.mknlabs.expensetracker.core.ui.components.CatalogSort
import com.adamglin.phosphoricons.regular.SortAscending
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.NamedCreateCard
import com.mknlabs.expensetracker.core.ui.components.BrandAddFab
import com.mknlabs.expensetracker.core.ui.components.CategoryColorRow
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.TagRowActionFillDark
import com.mknlabs.expensetracker.core.ui.theme.TagRowBorderDark
import com.mknlabs.expensetracker.core.ui.theme.TagRowDeleteBorderDark
import com.mknlabs.expensetracker.core.ui.theme.TagRowDeleteFillDark
import com.mknlabs.expensetracker.core.ui.theme.TagRowFillDark
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.chip
import com.mknlabs.expensetracker.core.ui.theme.chipOutline
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.parseHexColorOrNull
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.utils.formatCurrencyValue


@Composable
internal fun TagManagementCard(
    item: TagManagementItemUi,
    currencyId: Int,
    selectionMode: Boolean,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onColorClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.isDark
    // The mockup's row: a single line with a # tile, the name over its usage count, the
    // spend, then the edit and delete buttons. Only dark is restyled; light stays the
    // standard white card.
    val identity = parseHexColorOrNull(item.colorHex) ?: colorScheme.accentInk
    val rowFill = if (isDark) TagRowFillDark else colorScheme.surface
    val rowEdge = if (isDark) TagRowBorderDark else colorScheme.outlineVariant.copy(alpha = 0.65f)
    val actionFill = if (isDark) {
        TagRowActionFillDark
    } else {
        colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    }
    val deleteFill = if (isDark) {
        TagRowDeleteFillDark
    } else {
        colorScheme.error.copy(alpha = 0.08f)
    }
    val deleteEdge = if (isDark) {
        TagRowDeleteBorderDark
    } else {
        colorScheme.error.copy(alpha = 0.18f)
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            // The card itself only reacts while a bulk action is collecting tags, where a
            // tap toggles the row in or out of the selection. Outside that mode it is inert,
            // so a stray tap cannot delete or rename.
            .clickable(enabled = selectionMode, onClick = onCardClick),
        shape = RoundedCornerShape(16.dp),
        colors = AppCardColors(
            containerColor = rowFill,
            contentColor = colorScheme.onSurface,
            border = BorderStroke(
                1.dp,
                if (isSelected) colorScheme.accentInk else rowEdge
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // The # tile borrows the tag's own colour: 15% wash, 20% edge, full-strength
            // glyph, matching the mockup's badge treatment for its violet.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(identity.copy(alpha = 0.15f))
                    .border(1.dp, identity.copy(alpha = 0.20f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#",
                    color = identity,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.msg_tag_transaction_count, item.transactionCount),
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = formatCurrencyValue(item.expenseMinor / 100.0, currencyId),
                // A tag with no spend keeps the muted ink, so the row reads as idle.
                color = if (item.expenseMinor == 0L) {
                    colorScheme.onSurfaceVariant
                } else {
                    colorScheme.onSurface
                },
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1
            )

            if (!selectionMode) {
                TagRowAction(
                    icon = Icons.Filled.Edit,
                    description = stringResource(R.string.desc_rename_tag, item.name),
                    tint = colorScheme.onSurfaceVariant,
                    fill = actionFill,
                    onClick = onRenameClick
                )
                TagRowAction(
                    icon = Icons.Filled.Close,
                    description = stringResource(R.string.desc_delete_tag, item.name),
                    tint = colorScheme.error,
                    fill = deleteFill,
                    edge = deleteEdge,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
internal fun TagRowAction(
    icon: ImageVector,
    description: String,
    tint: Color,
    fill: Color,
    onClick: () -> Unit,
    edge: Color? = null
) {
    // A compact 32dp button with a 16dp glyph, as in the mockup. The ripple is suppressed
    // for the same reason the row's other icon actions suppress it: two small buttons sit
    // tight together, and the spray of ripples read as noise.
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(shape)
            .background(fill)
            .then(edge?.let { Modifier.border(1.dp, it, shape) } ?: Modifier)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}
@Composable
internal fun IconAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    // Kept as the row's tallest element, so this is what sets the card's height. It sits
    // below the 48dp a11y guideline to keep the card compact; the glyph stays 20dp inside.
    //
    // The ripple is suppressed on purpose: these glyphs are small and sit tight against
    // each other, and the spray of ripples among three adjacent icons read as noise. The
    // tap still lands — only the ink response is gone.
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Lets the user pick which of the tags they selected should survive the merge. The chosen
 * one keeps its name and colour; every other selected tag is folded into it.
 */
