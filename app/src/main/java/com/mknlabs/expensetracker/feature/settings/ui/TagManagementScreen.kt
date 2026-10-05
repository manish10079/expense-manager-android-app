package com.mknlabs.expensetracker.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
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
import com.mknlabs.expensetracker.core.ui.components.BrandAddFab
import com.mknlabs.expensetracker.core.ui.components.CategoryColorRow
import com.mknlabs.expensetracker.core.ui.components.PeriodChip
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
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
                // The two bulk actions read as the period chips on Analytics: one option
                // per chip, the active one filled. Tapping the active chip leaves the mode,
                // so the same control that enters selection is the one that cancels it.
                if (uiState.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    val action = selectionAction
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PeriodChip(
                            label = stringResource(R.string.label_merge_tags),
                            isSelected = action == TagSelectionAction.MERGE,
                            onClick = {
                                if (action == TagSelectionAction.MERGE) {
                                    exitSelection()
                                } else {
                                    enterSelection(TagSelectionAction.MERGE)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PeriodChip(
                            label = stringResource(R.string.label_delete_tags),
                            isSelected = action == TagSelectionAction.DELETE,
                            onClick = {
                                if (action == TagSelectionAction.DELETE) {
                                    exitSelection()
                                } else {
                                    enterSelection(TagSelectionAction.DELETE)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (action != null) {
                        val hasEnoughSelected = if (action == TagSelectionAction.MERGE) {
                            selectedIds.size >= 2
                        } else {
                            selectedIds.isNotEmpty()
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    if (action == TagSelectionAction.MERGE) {
                                        R.string.msg_select_tags_to_merge
                                    } else {
                                        R.string.msg_select_tags_to_delete
                                    }
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    when (action) {
                                        TagSelectionAction.MERGE -> showMergeTargetDialog = true
                                        TagSelectionAction.DELETE -> showDeleteConfirm = true
                                    }
                                },
                                enabled = hasEnoughSelected,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.accentInk,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (action == TagSelectionAction.MERGE) {
                                        stringResource(R.string.label_merge_selected, selectedIds.size)
                                    } else {
                                        stringResource(R.string.label_delete_selected, selectedIds.size)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = stringResource(R.string.label_tags_count, uiState.items.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.items.isEmpty()) {
                    Text(
                        text = stringResource(R.string.msg_no_tags_yet),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = uiState.items, key = { it.id }) { item ->
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
                                onDeleteClick = { enterSelection(TagSelectionAction.DELETE, item.id) },
                                onMergeClick = { enterSelection(TagSelectionAction.MERGE, item.id) }
                            )
                        }
                    }
                }
            }
        }

        BrandAddFab(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 22.dp, bottom = 28.dp),
            contentDescription = stringResource(R.string.desc_add_tag),
            shadowElevation = 22.dp
        )
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
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
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
                    text = stringResource(R.string.msg_delete_tags_confirm, selectedIds.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                AppTextButton(onClick = {
                    onDeleteTags(selectedIds.toList())
                    showDeleteConfirm = false
                    exitSelection()
                }) {
                    Text(
                        text = stringResource(R.string.label_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.label_cancel_confirm), fontWeight = FontWeight.Bold)
                }
            }
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
    onDeleteClick: () -> Unit,
    onMergeClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            // The card itself only reacts while a bulk action is collecting tags, where a
            // tap toggles the row in or out of the selection. Outside that mode it is inert,
            // so a stray tap cannot delete or rename.
            .clickable(enabled = selectionMode, onClick = onCardClick),
        brush = darkOnlyGradient(standardCardGradient()),
        shape = AppCardDefaults.shape(20.dp),
        colors = if (isSelected) {
            AppCardDefaults.colors(
                darkContainer = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.16f)
            )
        } else {
            AppCardDefaults.colors()
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

                if (selectionMode) {
                    // While picking tags the per-row actions give way to a single check, so
                    // the whole row is one obvious toggle and nothing competes with the tap.
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.accentInk
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    IconAction(
                        icon = Icons.Filled.Edit,
                        description = stringResource(R.string.desc_rename_tag, item.name),
                        onClick = onRenameClick
                    )
                    IconAction(
                        icon = Icons.Filled.CallMerge,
                        description = stringResource(R.string.desc_merge_tag, item.name),
                        onClick = onMergeClick
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.sheet,
        title = {
            Text(
                text = stringResource(R.string.title_merge_tags),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.msg_merge_choose_target),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
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
        },
        confirmButton = {
            AppTextButton(
                enabled = targetId != null,
                onClick = { targetId?.let(onConfirm) }
            ) {
                Text(
                    text = stringResource(R.string.label_merge_tags),
                    fontWeight = FontWeight.Bold,
                    color = if (targetId != null) {
                        MaterialTheme.colorScheme.accentInk
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel_confirm), fontWeight = FontWeight.Bold)
            }
        }
    )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.sheet,
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(stringResource(R.string.label_tag_name)) },
                shape = AppOutlinedFieldDefaults.shape,
                colors = AppOutlinedFieldDefaults.colors()
            )
        },
        confirmButton = {
            AppTextButton(
                enabled = isValid,
                onClick = { if (isValid) onConfirm(name) }
            ) {
                Text(
                    text = confirmLabel,
                    fontWeight = FontWeight.Bold,
                    color = if (isValid) MaterialTheme.colorScheme.accentInk
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel_confirm), fontWeight = FontWeight.Bold)
            }
        }
    )
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
        onMergeClick = {}
    )
}
