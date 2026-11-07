package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.disabled

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CalendarDots
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.CurrencyInr
import com.adamglin.phosphoricons.regular.Globe
import com.adamglin.phosphoricons.regular.Phone
import com.adamglin.phosphoricons.regular.DownloadSimple
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Palette
import com.adamglin.phosphoricons.regular.PushPin
import com.adamglin.phosphoricons.regular.Ruler
import com.adamglin.phosphoricons.regular.SlidersHorizontal
import com.adamglin.phosphoricons.regular.TextAa
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.AppSelectionSheet
import com.mknlabs.expensetracker.core.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.core.ui.components.SettingsGroup
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.core.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.models.SelectionItem
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.AccessLevel
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.utils.getDateFormatPreviewLabel
import com.mknlabs.expensetracker.utils.supportedDateFormats
import com.mknlabs.expensetracker.utils.supportedTimeFormats
import com.mknlabs.expensetracker.utils.findFragmentActivity

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FontPickerSheet(
    selectedFontMode: com.mknlabs.expensetracker.models.FontMode,
    activeCustomFontFileName: String?,
    importedFontFileNames: List<String>,
    onSelectFontMode: (com.mknlabs.expensetracker.models.FontMode, String?) -> Unit,
    onImportFont: () -> Unit,
    onDeleteFont: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    showDeleteDialog?.let { fileName ->
        ModernConfirmationDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = stringResource(R.string.msg_delete_font_title),
            text = stringResource(
                R.string.msg_delete_font_desc,
                com.mknlabs.expensetracker.utils.FontFileHelper.fontDisplayName(fileName)
            ),
            confirmButtonText = stringResource(R.string.label_delete),
            onConfirm = {
                onDeleteFont(fileName)
                showDeleteDialog = null
            },
            headerIcon = Icons.Filled.Delete,
            dismissButtonText = stringResource(R.string.label_cancel),
            onDismiss = { showDeleteDialog = null }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // Same fill as the gender picker, so every preferences sheet reads alike.
        containerColor = MaterialTheme.colorScheme.profileSheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.label_select_font),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.label_select_font_desc),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Default font — always free, no gating
            FontOptionItem(
                title = stringResource(R.string.label_font_app),
                isSelected = selectedFontMode == com.mknlabs.expensetracker.models.FontMode.APP,
                onClick = {
                    onSelectFontMode(com.mknlabs.expensetracker.models.FontMode.APP, null)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // System font — ad-supported gating
            GatedAction(
                feature = Feature.SYSTEM_FONT,
                displayName = stringResource(R.string.label_font_system),
                onAction = {
                    onSelectFontMode(com.mknlabs.expensetracker.models.FontMode.SYSTEM, null)
                    onDismiss()
                }
            ) { status, onClick ->
                val accessLevel = FeatureRegistry.getAccessLevel(Feature.SYSTEM_FONT)
                FontOptionItem(
                    title = stringResource(R.string.label_font_system),
                    isSelected = selectedFontMode == com.mknlabs.expensetracker.models.FontMode.SYSTEM,
                    isLocked = status !is AccessStatus.Granted,
                    accessLevel = accessLevel,
                    onClick = onClick
                )
            }

            importedFontFileNames.forEach { fileName ->
                Spacer(modifier = Modifier.height(10.dp))
                GatedAction(
                    feature = Feature.CUSTOM_FONT,
                    optionId = fileName,
                    displayName = com.mknlabs.expensetracker.utils.FontFileHelper.fontDisplayName(fileName),
                    onAction = {
                        onSelectFontMode(com.mknlabs.expensetracker.models.FontMode.CUSTOM, fileName)
                        onDismiss()
                    }
                ) { status, onClick ->
                    val accessLevel = FeatureRegistry.getAccessLevel(Feature.CUSTOM_FONT, fileName)
                    FontOptionItem(
                        title = com.mknlabs.expensetracker.utils.FontFileHelper.fontDisplayName(fileName),
                        isSelected = selectedFontMode == com.mknlabs.expensetracker.models.FontMode.CUSTOM && activeCustomFontFileName == fileName,
                        isLocked = status !is AccessStatus.Granted,
                        accessLevel = accessLevel,
                        onClick = onClick,
                        onLongClick = { showDeleteDialog = fileName }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canImport = com.mknlabs.expensetracker.utils.FontFileHelper.canImport(importedFontFileNames.size)
            GatedAction(
                feature = Feature.CUSTOM_FONT,
                displayName = stringResource(R.string.label_import_font),
                onAction = onImportFont
            ) { status, onClick ->
                val accessLevel = FeatureRegistry.getAccessLevel(Feature.CUSTOM_FONT)
                val isGated = status !is AccessStatus.Granted
                val shape = RoundedCornerShape(16.dp)
                val ink = MaterialTheme.colorScheme.onBrandGradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(brush = brandGradient())
                        .then(
                            if (canImport) Modifier.clickable(onClick = onClick)
                            else Modifier.alpha(0.45f)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.DownloadSimple,
                            contentDescription = null,
                            tint = ink,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGated) {
                                if (accessLevel == AccessLevel.AD_SUPPORTED) stringResource(R.string.label_watch_ad) else stringResource(R.string.label_pro_required)
                            } else {
                                stringResource(R.string.label_import_font)
                            },
                            color = ink,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (!canImport) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.label_max_fonts_reached,
                        com.mknlabs.expensetracker.utils.FontFileHelper.MAX_CUSTOM_FONTS
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
internal fun FontOptionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    subtitle: String? = null,
    isLocked: Boolean = false,
    accessLevel: AccessLevel = AccessLevel.FREE,
    onLongClick: (() -> Unit)? = null
) {
    val lockColor = MaterialTheme.colorScheme.featureGateLock
    val isGated = isLocked && accessLevel != AccessLevel.FREE

    // One fill for every row: the choice is drawn as an accent edge, not as a lighter card.
    val backgroundColor = MaterialTheme.colorScheme.genderRow

    val iconBoxColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val contentColor = if (isGated) {
        MaterialTheme.colorScheme.disabled
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val rowShape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.accentInk,
                        shape = rowShape
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick?.let { { it() } }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The 42dp / 14dp tile only appears for a gated row's lock, the way every other
        // selection row shows a leading tile only when it carries one.
        if (isGated) {
            Box(
                modifier = Modifier
                    .heightIn(min = 42.dp)
                    .widthIn(min = 42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBoxColor)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Lock,
                    contentDescription = null,
                    tint = lockColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(if (isGated) 14.dp else 34.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = contentColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isGated) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = PhosphorIcons.Regular.Lock,
                        contentDescription = null,
                        tint = lockColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (isGated) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (accessLevel == AccessLevel.AD_SUPPORTED) stringResource(R.string.label_watch_ad) else stringResource(R.string.label_premium),
                color = lockColor,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

    }
}
