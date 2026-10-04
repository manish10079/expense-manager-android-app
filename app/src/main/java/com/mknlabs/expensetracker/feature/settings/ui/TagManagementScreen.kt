package com.mknlabs.expensetracker.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
        onDeleteTag = viewModel::deleteTag,
        onToggleMerge = viewModel::toggleMerge,
        onCancelMerge = viewModel::cancelMerge,
        onUpdateColor = viewModel::updateColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagManagementContent(
    uiState: TagManagementUiState,
    currencyId: Int,
    onBackClick: () -> Unit,
    onCreateTag: (String, String?) -> Unit,
    onRenameTag: (String, String) -> Unit,
    onDeleteTag: (String) -> Unit,
    onToggleMerge: (String) -> Unit,
    onCancelMerge: () -> Unit,
    onUpdateColor: (String, String?) -> Unit
) {
    var colorEditingItem by remember { mutableStateOf<TagManagementItemUi?>(null) }
    var renameEditingItem by remember { mutableStateOf<TagManagementItemUi?>(null) }
    var deleteCandidate by remember { mutableStateOf<TagManagementItemUi?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

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
                if (uiState.isMerging) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MergeBanner(
                        sourceName = uiState.mergeSource?.name.orEmpty(),
                        onCancel = onCancelMerge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            TagManagementCard(
                                item = item,
                                currencyId = currencyId,
                                isMergeSource = item.id == uiState.mergeSourceId,
                                isMerging = uiState.isMerging,
                                onColorClick = { colorEditingItem = item },
                                onRenameClick = { renameEditingItem = item },
                                onDeleteClick = { deleteCandidate = item },
                                onMergeClick = { onToggleMerge(item.id) }
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

    deleteCandidate?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
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
                    text = stringResource(R.string.msg_delete_tag_confirm, item.name),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                AppTextButton(onClick = {
                    onDeleteTag(item.id)
                    deleteCandidate = null
                }) {
                    Text(
                        text = stringResource(R.string.label_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                AppTextButton(onClick = { deleteCandidate = null }) {
                    Text(stringResource(R.string.label_cancel_confirm), fontWeight = FontWeight.Bold)
                }
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
}

@Composable
private fun MergeBanner(sourceName: String, onCancel: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        brush = darkOnlyGradient(standardCardGradient()),
        shape = AppCardDefaults.shape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.msg_merge_pick_target, sourceName),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            AppTextButton(onClick = onCancel) {
                Text(stringResource(R.string.label_cancel_confirm), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TagManagementCard(
    item: TagManagementItemUi,
    currencyId: Int,
    isMergeSource: Boolean,
    isMerging: Boolean,
    onColorClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMergeClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            // Tapping the card while merging names the target; otherwise it is inert, so
            // a stray tap cannot delete or rename.
            .clickable(enabled = isMerging, onClick = onMergeClick),
        brush = darkOnlyGradient(standardCardGradient()),
        shape = AppCardDefaults.shape(20.dp),
        colors = if (isMergeSource) {
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    // Kept as the row's tallest element, so this is what sets the card's height. It sits
    // below the 48dp a11y guideline to keep the card compact; the glyph stays 20dp inside.
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick),
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
        isMergeSource = false,
        isMerging = false,
        onColorClick = {},
        onRenameClick = {},
        onDeleteClick = {},
        onMergeClick = {}
    )
}
