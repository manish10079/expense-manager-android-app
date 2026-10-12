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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
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
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.chip
import com.mknlabs.expensetracker.core.ui.theme.chipOutline
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.utils.formatCurrencyValue

/**
 * The tag hub: every tag, how much it is used, and the operations that change it.
 *
 * Route-owned state (the ViewModel) lives in the outer composable, the rendering in
 * [TagManagementContent], matching the Route/Content split the category screen uses.
 */
@Composable
fun TagManagementScreen(
    currencyId: Int = 0,
    onBackClick: () -> Unit = {}
) {
    val viewModel: TagManagementViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TagManagementContent(
        uiState = uiState,
        currencyId = currencyId,
        onBackClick = onBackClick,
        onCreateTag = viewModel::createTag,
        onRenameTag = viewModel::renameTag,
        onDeleteTags = viewModel::deleteTags,
        onMergeTags = viewModel::mergeTags,
        onUpdateColor = viewModel::updateColor
    )
}

/**
 * The bulk action the list is currently collecting tags for, or null when no selection is
 * in progress. Merge needs at least two tags (one to keep, the rest to fold in); delete
 * needs at least one.
 */
private enum class TagSelectionAction { MERGE, DELETE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagManagementContent(
    uiState: TagManagementUiState,
    currencyId: Int,
    onBackClick: () -> Unit,
    onCreateTag: (String, String?) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTags: (List<String>) -> Unit,
    onMergeTags: (targetId: String, sourceIds: List<String>) -> Unit,
    onUpdateColor: (String, String?) -> Unit
) {
    var colorEditingItem by remember { mutableStateOf<TagManagementItemUi?>(null) }
    var renameEditingItem by remember { mutableStateOf<TagManagementItemUi?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Selection mode is screen state, not ViewModel state: it is a transient way of
    // picking the rows an operation applies to, and nothing outside this screen reads it.
    var selectionAction by remember { mutableStateOf<TagSelectionAction?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMergeTargetDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showTagsInfo by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var catalogSort by remember { mutableStateOf(CatalogSort.Newest) }
    var showSortSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(catalogSort) {
        listState.scrollToItem(0)
    }
    val searchFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            delay(80)
            searchFocusRequester.requestFocus()
        }
    }

    fun exitSelection() {
        selectionAction = null
        selectedIds = emptySet()
    }

    fun enterSelection(action: TagSelectionAction, preselect: String? = null) {
        selectionAction = action
        selectedIds = if (preselect != null) setOf(preselect) else emptySet()
    }

    fun toggleSelected(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TagManagementGlow()

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
                title = stringResource(R.string.title_manage_tags),
                onBackClick = onBackClick,
                modifier = Modifier.alpha(enter[0])
            )

            Column(modifier = Modifier.alpha(enter[1]).weight(1f)) {
                Spacer(modifier = Modifier.height(16.dp))

                val visibleTags = (if (searchQuery.isBlank()) {
                    uiState.items
                } else {
                    uiState.items.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }).sortedByCatalog(catalogSort, createdAt = { it.tag.createdAt }, name = { it.name })
                val countColor = MaterialTheme.colorScheme.onSurfaceVariant
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_tags_count, visibleTags.size),
                        color = countColor,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                        contentDescription = stringResource(R.string.desc_search_tags),
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
                        contentDescription = stringResource(R.string.desc_tags_info),
                        tint = countColor,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showTagsInfo = true }
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
                                stringResource(R.string.placeholder_search_manage_tags),
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
                                    imageVector = Icons.Filled.Close,
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

                if (visibleTags.isEmpty()) {
                    Text(
                        text = stringResource(R.string.msg_no_tags_yet),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = visibleTags, key = { it.id }) { item ->
                            val action = selectionAction
                            TagManagementCard(
                                item = item,
                                currencyId = currencyId,
                                selectionMode = action != null,
                                isSelected = item.id in selectedIds,
                                onCardClick = {
                                    if (action != null) toggleSelected(item.id)
                                },
                                onColorClick = { colorEditingItem = item },
                                onRenameClick = { renameEditingItem = item },
                                onDeleteClick = { enterSelection(TagSelectionAction.DELETE, item.id) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.items.isNotEmpty()) {
            val action = selectionAction
            if (action == null) {
                val scheme = MaterialTheme.colorScheme
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = scheme.background.copy(alpha = 0.95f),
                    tonalElevation = 0.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)))
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val chipShape = RoundedCornerShape(12.dp)
                        Text(
                            text = stringResource(R.string.label_merge_tags),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBrandGradient,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(chipShape)
                                .background(brandGradient())
                                .clickable { enterSelection(TagSelectionAction.MERGE) }
                                .padding(vertical = 10.dp)
                        )
                        Text(
                            text = stringResource(R.string.label_delete_tags),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBrandGradient,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(chipShape)
                                .background(brandGradient())
                                .clickable { enterSelection(TagSelectionAction.DELETE) }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            } else {
                val scheme = MaterialTheme.colorScheme
                val chipShape = RoundedCornerShape(12.dp)
                val allIds = uiState.items.map { it.id }.toSet()
                val allSelected = allIds.isNotEmpty() && allIds.all { it in selectedIds }
                val canAct = if (action == TagSelectionAction.MERGE) {
                    selectedIds.size >= 2
                } else {
                    selectedIds.isNotEmpty()
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = scheme.background.copy(alpha = 0.95f),
                    tonalElevation = 0.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)))
                            .navigationBarsPadding()
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.label_sms_inbox_selected_count, selectedIds.size),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                id = if (allSelected) {
                                    R.string.action_sms_inbox_deselect_all
                                } else {
                                    R.string.action_sms_inbox_select_all
                                }
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            modifier = Modifier
                                .clip(chipShape)
                                .background(scheme.chip)
                                .border(BorderStroke(1.dp, scheme.chipOutline), chipShape)
                                .clickable {
                                    selectedIds = if (allSelected) emptySet() else allIds
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val actionEnabled = canAct
                        Text(
                            text = if (action == TagSelectionAction.MERGE) {
                                stringResource(R.string.label_merge_tags)
                            } else {
                                stringResource(R.string.action_sms_inbox_delete_selected)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onBrandGradient,
                            modifier = Modifier
                                .clip(chipShape)
                                .background(brandGradient())
                                .clickable(enabled = actionEnabled) {
                                    when (action) {
                                        TagSelectionAction.MERGE -> showMergeTargetDialog = true
                                        TagSelectionAction.DELETE -> showDeleteConfirm = true
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .then(if (actionEnabled) Modifier else Modifier.alpha(0.45f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(scheme.chip)
                                .clickable { exitSelection() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.desc_exit_selection),
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        if (selectionAction == null) {
            BrandAddFab(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 22.dp, bottom = 88.dp),
                contentDescription = stringResource(R.string.desc_add_tag),
                shadowElevation = 22.dp
            )
        }
    }

    if (showCreateDialog) {
        TagNameDialog(
            title = stringResource(R.string.title_new_tag),
            initialName = "",
            confirmLabel = stringResource(R.string.label_create),
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreateTag(name, null)
                showCreateDialog = false
            }
        )
    }

    renameEditingItem?.let { item ->
        TagNameDialog(
            title = stringResource(R.string.title_rename_tag),
            initialName = item.name,
            confirmLabel = stringResource(R.string.label_rename),
            onDismiss = { renameEditingItem = null },
            onConfirm = { name ->
                onRenameTag(item.id, name)
                renameEditingItem = null
            }
        )
    }

    colorEditingItem?.let { item ->
        TagColorSheet(
            item = item,
            onDismiss = { colorEditingItem = null },
            onColorSelected = { colorHex ->
                onUpdateColor(item.id, colorHex)
                colorEditingItem = null
            }
        )
    }

    if (showTagsInfo) {
        TagsInfoDialog(onDismiss = { showTagsInfo = false })
    }
    if (showSortSheet) {
        CatalogSortSheet(
            selected = catalogSort,
            onSelect = { catalogSort = it },
            onDismiss = { showSortSheet = false }
        )
    }

    if (showMergeTargetDialog) {
        MergeTargetDialog(
            tags = uiState.items.filter { it.id in selectedIds },
            onDismiss = { showMergeTargetDialog = false },
            onConfirm = { targetId ->
                onMergeTags(targetId, selectedIds.toList())
                showMergeTargetDialog = false
                exitSelection()
            }
        )
    }

    if (showDeleteConfirm) {
        ModernConfirmationDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = stringResource(R.string.label_delete_confirm),
            text = stringResource(R.string.msg_delete_tags_confirm, selectedIds.size),
            confirmButtonText = stringResource(R.string.label_delete_confirm),
            onConfirm = {
                onDeleteTags(selectedIds.toList())
                showDeleteConfirm = false
                exitSelection()
            },
            headerIcon = Icons.Filled.Delete,
            dismissButtonText = stringResource(R.string.label_cancel_confirm),
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun TagManagementCard(
    item: TagManagementItemUi,
    currencyId: Int,
    selectionMode: Boolean,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onColorClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            // The card itself only reacts while a bulk action is collecting tags, where a
            // tap toggles the row in or out of the selection. Outside that mode it is inert,
            // so a stray tap cannot delete or rename.
            .clickable(enabled = selectionMode, onClick = onCardClick),
        brush = darkOnlyGradient(standardCardGradient()),
        shape = RoundedCornerShape(8.dp),
        colors = run {
            val base = AppCardDefaults.colors()
            if (isSelected) {
                base.copy(border = BorderStroke(1.dp, MaterialTheme.colorScheme.accentInk))
            } else {
                base
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_tag_prefixed, item.name),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (!selectionMode) {
                    IconAction(
                        icon = Icons.Filled.Edit,
                        description = stringResource(R.string.desc_rename_tag, item.name),
                        onClick = onRenameClick
                    )
                    IconAction(
                        icon = Icons.Filled.Close,
                        description = stringResource(R.string.desc_delete_tag, item.name),
                        tint = MaterialTheme.colorScheme.error,
                        onClick = onDeleteClick
                    )
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.msg_tag_transaction_count, item.transactionCount),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatCurrencyValue(item.expenseMinor / 100.0, currencyId),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun IconAction(
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
@Composable
private fun MergeTargetDialog(
    tags: List<TagManagementItemUi>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var targetId by remember { mutableStateOf(tags.firstOrNull()?.id) }

    Dialog(onDismissRequest = onDismiss) {
        NamedCreateCard(
            title = stringResource(R.string.title_merge_tags),
            caption = stringResource(R.string.msg_merge_choose_target),
            value = "",
            onValueChange = {},
            placeholder = "",
            confirmLabel = stringResource(R.string.label_merge_tags),
            onConfirm = { targetId?.let(onConfirm) },
            headerIcon = Icons.Filled.CheckCircle,
            confirmEnabled = targetId != null,
            showField = false,
            extraContent = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    tags.forEach { tag ->
                        val isChosen = tag.id == targetId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { targetId = tag.id }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isChosen) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = null,
                                tint = if (isChosen) {
                                    MaterialTheme.colorScheme.accentInk
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.label_tag_prefixed, tag.name),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun TagNameDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    val isValid = name.trim().isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        NamedCreateCard(
            title = title,
            caption = stringResource(R.string.msg_tag_name_caption),
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.label_tag_name),
            confirmLabel = confirmLabel,
            onConfirm = { if (isValid) onConfirm(name) },
            confirmEnabled = isValid
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagColorSheet(
    item: TagManagementItemUi,
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
            Text(
                text = stringResource(R.string.label_tag_prefixed, item.name),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.msg_tag_color_hint),
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
private fun BoxScope.TagManagementGlow() {
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

/**
 * The tag card in both themes. The tag reads in its "#name" form, and the summary line
 * carries the transaction count and the expense the card exists to show.
 */

@Composable
private fun TagsInfoDialog(onDismiss: () -> Unit) {
    ModernConfirmationDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.title_what_are_tags),
        text = stringResource(R.string.msg_tags_what) + "\n\n" + stringResource(R.string.msg_tags_where),
        confirmButtonText = stringResource(R.string.label_close),
        onConfirm = onDismiss,
        headerIcon = PhosphorIcons.Regular.Info,
        showDismissButton = false,
        extraContent = {
            Column {
                Text(
                    text = stringResource(R.string.label_tags_example),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.accentInk
                    )
                    Text(
                        text = stringResource(R.string.label_tag_example_name),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Tag Card (Light)")
@Composable
private fun TagManagementCardLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        TagManagementCardPreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Tag Card (Dark)")
@Composable
private fun TagManagementCardDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        TagManagementCardPreviewContent()
    }
}

@Composable
private fun TagManagementCardPreviewContent() {
    TagManagementCard(
        item = TagManagementItemUi(
            tag = Tag(id = "preview-tag", name = "Groceries", colorHex = "#5B2EED"),
            transactionCount = 12,
            expenseMinor = 48_250L
        ),
        currencyId = 0,
        selectionMode = false,
        isSelected = false,
        onCardClick = {},
        onColorClick = {},
        onRenameClick = {},
        onDeleteClick = {},
    )
}
