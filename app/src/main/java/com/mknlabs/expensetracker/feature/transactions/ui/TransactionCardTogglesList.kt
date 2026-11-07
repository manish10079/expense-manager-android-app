package com.mknlabs.expensetracker.feature.transactions.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import android.content.res.Configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Today
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowDown
import com.adamglin.phosphoricons.regular.ArrowUp
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CalendarDots
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.ListChecks
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.TextAa
import com.adamglin.phosphoricons.regular.Wallet
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.AddTransactionUiPattern
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowHeight
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowInfo
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowSize
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import androidx.window.core.layout.WindowSizeClass
import com.mknlabs.expensetracker.core.ui.components.TransactionCard
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatAmount
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.getPaymentTypeName
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.AccessStatus

import com.mknlabs.expensetracker.core.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.components.SettingsGroup
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupHeader
import com.mknlabs.expensetracker.core.ui.components.AppSelectionSheet
import com.mknlabs.expensetracker.core.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.core.ui.components.transactionPeriodFilterOrDefault
import com.mknlabs.expensetracker.data.constants.defaultAppSettings
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore

import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.core.ui.models.SelectionItem
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.milliseconds

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TransactionCardTogglesList(
    toggleItems: List<TransactionCardToggleItem>,
    isInPreview: Boolean,
    defaultPeriodFilter: TransactionPeriodFilter,
    onDefaultPeriodFilterChange: (TransactionPeriodFilter) -> Unit,
    addUiPattern: AddTransactionUiPattern,
    onAddUiPatternChange: (AddTransactionUiPattern) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp
) {
    var isDefaultViewSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isAddUiSheetVisible by rememberSaveable { mutableStateOf(false) }
    val defaultViewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addUiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val periodFilterItems = TransactionPeriodFilter.entries.map { filter ->
        SelectionItem(
            id = filter,
            title = stringResource(filter.labelRes),
            leadingIcon = when (filter) {
                TransactionPeriodFilter.ALL -> Icons.AutoMirrored.Filled.List
                TransactionPeriodFilter.DAILY -> Icons.Filled.Today
                TransactionPeriodFilter.MONTHLY -> Icons.Filled.DateRange
                TransactionPeriodFilter.YEARLY -> Icons.Filled.CalendarMonth
            }
        )
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SettingsGroupHeader(
                title = stringResource(id = R.string.label_add_button_style),
                subtitle = stringResource(id = R.string.desc_add_button_style)
            )
        }
        item {
            SettingsGroup(colors = settingsGroupCardColors()) {
                SettingsItemCard(
                    icon = Icons.Filled.Add,
                    iconTile = SettingsTiles.Indigo,
                    title = stringResource(id = R.string.label_add_ui_pattern),
                    subtitle = stringResource(id = R.string.desc_add_ui_pattern),
                    valueText = stringResource(
                        if (addUiPattern == AddTransactionUiPattern.QUICK_FLOATING_ARC)
                            R.string.label_add_ui_arc
                        else
                            R.string.label_add_ui_unified
                    ),
                    type = SettingsItemType.Value,
                    standalone = false,
                    onClick = { isAddUiSheetVisible = true }
                )
            }
        }

        // Group 1: what every transaction card displays, ordered to mirror the card's own
        // anatomy -- icon, category, payment method, then the date and time stamps.
        item {
            SettingsGroupHeader(
                title = stringResource(id = R.string.label_on_each_transaction_card),
                subtitle = stringResource(id = R.string.desc_on_each_transaction_card)
            )
        }
        item {
            SettingsGroup(colors = settingsGroupCardColors()) {
                val groupItems = transactionCardToggleIds.mapNotNull { id ->
                    toggleItems.firstOrNull { it.optionId == id }
                }
                groupItems.forEachIndexed { index, item ->
                    false.ToggleSettingsItem(
                        item = item,
                        isInPreview = isInPreview
                    )
                    if (index < groupItems.size - 1) SettingsGroupDivider()
                }
            }
        }

        // Group 2: how the list itself is arranged -- grouped under day headers, with the
        // period summaries above them.
        item {
            SettingsGroupHeader(
                title = stringResource(id = R.string.label_in_the_list),
                subtitle = stringResource(id = R.string.desc_in_the_list)
            )
        }
        item {
            SettingsGroup(colors = settingsGroupCardColors()) {
                SettingsItemCard(
                    icon = Icons.Filled.DateRange,
                    iconTile = SettingsTiles.Sky,
                    title = stringResource(id = R.string.title_default_view_by),
                    subtitle = stringResource(id = R.string.desc_default_view_by),
                    valueText = stringResource(defaultPeriodFilter.labelRes),
                    type = SettingsItemType.Value,
                    standalone = false,
                    onClick = { isDefaultViewSheetVisible = true }
                )
            }
        }
        item {
            SettingsGroup(colors = settingsGroupCardColors()) {
                val groupItems = listLevelToggleIds.mapNotNull { id ->
                    toggleItems.firstOrNull { it.optionId == id }
                }
                groupItems.forEachIndexed { index, item ->
                    false.ToggleSettingsItem(
                        item = item,
                        isInPreview = isInPreview
                    )
                    if (index < groupItems.size - 1) SettingsGroupDivider()
                }
            }
        }
    }

    if (isAddUiSheetVisible) {
        AppSelectionSheet(
            title = stringResource(id = R.string.label_add_ui_pattern),
            description = stringResource(id = R.string.desc_add_ui_pattern),
            items = listOf(
                SelectionItem(
                    id = AddTransactionUiPattern.UNIFIED_SMART_SHEET,
                    title = stringResource(R.string.label_add_ui_unified),
                    leadingIcon = Icons.Filled.Add,
                ),
                SelectionItem(
                    id = AddTransactionUiPattern.QUICK_FLOATING_ARC,
                    title = stringResource(R.string.label_add_ui_arc),
                    leadingIcon = Icons.Filled.Add,
                ),
            ),
            selectedId = addUiPattern,
            sheetState = addUiSheetState,
            containerColor = MaterialTheme.colorScheme.profileSheet,
            rowColor = MaterialTheme.colorScheme.genderRow,
            onDismiss = { isAddUiSheetVisible = false },
            onItemSelected = { pattern ->
                onAddUiPatternChange(pattern)
                isAddUiSheetVisible = false
            }
        )
    }

    if (isDefaultViewSheetVisible) {
        AppSelectionSheet(
            title = stringResource(id = R.string.title_default_view_by),
            description = stringResource(id = R.string.desc_default_view_by),
            items = periodFilterItems,
            selectedId = defaultPeriodFilter,
            sheetState = defaultViewSheetState,
            containerColor = MaterialTheme.colorScheme.profileSheet,
            rowColor = MaterialTheme.colorScheme.genderRow,
            onDismiss = { isDefaultViewSheetVisible = false },
            onItemSelected = { filter ->
                onDefaultPeriodFilterChange(filter)
                isDefaultViewSheetVisible = false
            }
        )
    }
}

@Composable
internal fun Boolean.ToggleSettingsItem(
    item: TransactionCardToggleItem,
    isInPreview: Boolean
) {
    val accessLevel = FeatureRegistry.getAccessLevel(
        feature = Feature.CARD_CUSTOMIZATION,
        optionId = item.optionId
    )
    if (isInPreview) {
        SettingsItemCard(
            icon = item.icon,
            iconTile = item.iconTile,
            title = item.title,
            subtitle = item.subtitle,
            type = SettingsItemType.Toggle,
            accessLevel = accessLevel,
            isLocked = false,
            isChecked = item.checked,
            onCheckedChange = item.onCheckedChange,
            onClick = { item.onCheckedChange(!item.checked) },
            standalone = this
        )
    } else {
        GatedAction(
            feature = Feature.CARD_CUSTOMIZATION,
            optionId = item.optionId,
            displayName = item.title,
            onAction = { item.onCheckedChange(!item.checked) }
        ) { status, onClick ->
            SettingsItemCard(
                icon = item.icon,
                iconTile = item.iconTile,
                title = item.title,
                subtitle = item.subtitle,
                type = SettingsItemType.Toggle,
                accessLevel = accessLevel,
                isLocked = status !is AccessStatus.Granted,
                isChecked = item.checked,
                onCheckedChange = { onClick() },
                onClick = onClick,
                standalone = this
            )
        }
    }
}

