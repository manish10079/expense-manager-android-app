package com.mknlabs.expensetracker.core.ui.components

import com.mknlabs.expensetracker.core.ui.theme.disabled
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.models.SelectionItem

import androidx.compose.material.icons.rounded.Lock
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import androidx.compose.ui.text.style.TextOverflow

/**
 * The 1.5dp accent edge a chosen row wears instead of a tinted card, the same edge every other
 * selection sheet in the app draws, so one choice never reads as two different designs.
 */
private val SelectedRowBorderWidth = 1.5.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AppSelectionSheet(
    title: String,
    onDismiss: () -> Unit,
    items: List<SelectionItem<T>>,
    selectedId: T?,
    onItemSelected: (T) -> Unit,
    description: String? = null,
    showSearch: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    searchPlaceholder: String? = null,
    maxListHeight: Dp = 440.dp,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    // A caller whose own mockup paints the sheet on a different fill can override it — the edit
    // profile gender sheet does. Null keeps [ColorScheme.sheet], the container every other
    // selection sheet in the app is painted on.
    containerColor: Color? = null,
    // The idle row fill, for a caller whose mockup draws its own card. Null keeps
    // surfaceVariant. The chosen row uses this same fill plus an accent edge, so there is no
    // second colour to hand over.
    rowColor: Color? = null,
    // A long list — the language picker's 21 rows — opens as a full-height sheet so more of it is
    // on screen at once, instead of a short sheet wrapping a list capped at [maxListHeight].
    // The list then fills the space the header leaves and scrolls inside it.
    fullHeight: Boolean = false,
    footerContent: (LazyListScope.() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    // A row slides the sheet down first, then reports the choice, so a caller that drops
    // the sheet from composition on selection does not cut its exit animation.
    val isDismissing = remember { mutableStateOf(false) }
    val sheetContainerColor = containerColor ?: MaterialTheme.colorScheme.sheet

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fullHeight) Modifier.fillMaxHeight() else Modifier)
                .then(if (fullHeight) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Section
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            if (!description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            if (showSearch) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.desc_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    placeholder = {
                        Text(
                            text = searchPlaceholder ?: stringResource(R.string.label_search_dots),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = sheetContainerColor,
                        unfocusedContainerColor = sheetContainerColor,
                        focusedBorderColor = MaterialTheme.colorScheme.accentInk,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.accentInk
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Selection List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (fullHeight) {
                            Modifier.weight(1f)
                        } else {
                            Modifier.heightIn(max = maxListHeight)
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = items,
                    key = { it.id.hashCode() }
                ) { item ->
                    SelectionRow(
                        item = item,
                        isSelected = item.id == selectedId,
                        rowColor = rowColor,
                        onClick = {
                            if (!isDismissing.value) {
                                isDismissing.value = true
                                dismissSheetThen(scope, sheetState) { onItemSelected(item.id) }
                            }
                        }
                    )
                }

                if (items.isEmpty() && showSearch) {
                    item {
                        EmptySelectionState(text = stringResource(R.string.label_no_results_found))
                    }
                }

                footerContent?.invoke(this)
            }
        }
    }
}

@Composable
private fun <T> SelectionRow(
    item: SelectionItem<T>,
    isSelected: Boolean,
    rowColor: Color?,
    onClick: () -> Unit
) {
    // Choosing a row changes its edge, never its fill: the chosen card keeps the idle fill so
    // the list does not light up, and only the accent border marks the choice. Losing the
    // "Selected" label means the choice must still reach TalkBack, which `selectable` carries.
    val backgroundColor = rowColor ?: MaterialTheme.colorScheme.surfaceVariant
    val boxBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = MaterialTheme.colorScheme.onSurface
    val rowShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = SelectedRowBorderWidth,
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
        // Leading Icon/Text Box
        if (item.leadingText != null || item.leadingIcon != null) {
            Box(
                modifier = Modifier
                    .heightIn(min = 42.dp)
                    .widthIn(min = 42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(boxBackgroundColor)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.leadingText != null) {
                    Text(
                        text = item.leadingText,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else if (item.leadingIcon != null) {
                    Icon(
                        imageVector = item.leadingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
        }

        // Title and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val titleText = if (item.titleRes != 0) stringResource(item.titleRes) else item.title
                Text(
                    text = titleText,
                    color = if (item.isLocked) MaterialTheme.colorScheme.disabled else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (item.isLocked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Rounded.Lock,
                        contentDescription = stringResource(R.string.desc_locked),
                        tint = MaterialTheme.colorScheme.featureGateLock,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            val subtitleText = when {
                item.subtitleRes != 0 -> stringResource(item.subtitleRes)
                !item.subtitle.isNullOrEmpty() -> item.subtitle
                else -> null
            }
            if (!subtitleText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleText,
                    color = if (item.isLocked) MaterialTheme.colorScheme.disabled else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Access badge. The chosen row's state is its border, so it adds nothing here.
        if (!isSelected && item.isLocked) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (item.accessLevel == com.mknlabs.expensetracker.monetization.AccessLevel.AD_SUPPORTED)
                    stringResource(R.string.label_watch_ad) else stringResource(R.string.label_premium),
                color = MaterialTheme.colorScheme.featureGateLock,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun EmptySelectionState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
