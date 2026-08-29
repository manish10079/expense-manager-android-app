package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.AppSelectionSheet
import com.mknlabs.expensetracker.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.ui.components.SettingsGroup
import com.mknlabs.expensetracker.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.ui.models.SelectionItem
import com.mknlabs.expensetracker.ui.viewmodels.PreferencesSheetType
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.viewmodels.PreferencesViewModel
import com.mknlabs.expensetracker.utils.supportedDateFormats
import com.mknlabs.expensetracker.utils.supportedTimeFormats

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onManageCategoryClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    preferencesViewModel: PreferencesViewModel = hiltViewModel(),
    isAdsEnabled: Boolean = false
) {
    val uiState by preferencesViewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    // Font import launcher
    val fontImportLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val result = com.mknlabs.expensetracker.domain.usecase.ImportCustomFontUseCase(
                    context = context,
                    repository = preferencesViewModel.repository
                )(it)
                result.onSuccess { name ->
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.msg_import_font_success),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { e ->
                    android.widget.Toast.makeText(
                        context,
                        e.message ?: context.getString(R.string.msg_import_font_error),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    PreferencesScreenContent(
        uiState = uiState,
        isAdsEnabled = isAdsEnabled,
        onManageCategoryClick = onManageCategoryClick,
        onBackClick = onBackClick,
        showSheet = { preferencesViewModel.showSheet(it) },
        selectCurrency = { preferencesViewModel.selectCurrency(it) },
        clearCurrencySearchQuery = { preferencesViewModel.clearCurrencySearchQuery() },
        dismissSheet = { preferencesViewModel.dismissSheet() },
        updateCurrencySearchQuery = { preferencesViewModel.updateCurrencySearchQuery(it) },
        selectThemeMode = { preferencesViewModel.selectThemeMode(it) },
        selectDateFormat = { preferencesViewModel.selectDateFormat(it) },
        selectTimeFormat = { preferencesViewModel.selectTimeFormat(it) },
        selectGroupingStyle = { preferencesViewModel.selectGroupingStyle(it) },
        selectDecimalPlaces = { preferencesViewModel.selectDecimalPlaces(it) },
        selectFontMode = { fontMode, fileName ->
            preferencesViewModel.selectFontMode(fontMode, fileName)
        },
        onImportFont = {
            fontImportLauncher.launch(arrayOf("font/*"))
        },
        onDeleteFont = { fileName ->
            scope.launch {
                com.mknlabs.expensetracker.domain.usecase.DeleteCustomFontUseCase(
                    context = context,
                    repository = preferencesViewModel.repository
                )(fileName)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferencesScreenContent(
    uiState: com.mknlabs.expensetracker.ui.viewmodels.PreferencesScreenUiState,
    isAdsEnabled: Boolean,
    onManageCategoryClick: () -> Unit,
    onBackClick: () -> Unit,
    showSheet: (PreferencesSheetType) -> Unit,
    selectCurrency: (Int) -> Unit,
    clearCurrencySearchQuery: () -> Unit,
    dismissSheet: () -> Unit,
    updateCurrencySearchQuery: (String) -> Unit,
    selectThemeMode: (com.mknlabs.expensetracker.models.AppThemeMode) -> Unit,
    selectDateFormat: (String) -> Unit,
    selectTimeFormat: (String) -> Unit,
    selectGroupingStyle: (com.mknlabs.expensetracker.models.CurrencyGroupingStyle) -> Unit,
    selectDecimalPlaces: (Int) -> Unit,
    selectFontMode: (com.mknlabs.expensetracker.models.FontMode, String?) -> Unit,
    onImportFont: () -> Unit,
    onDeleteFont: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            AppHeader(
                title = stringResource(R.string.title_app_preferences),
                onBackClick = onBackClick
            )
            Spacer(modifier = Modifier.height(18.dp))

            AdaptiveContent(
                maxWidth = 640.dp,
                modifier = Modifier.weight(1f)
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Group 1: Currency & Formats
                item {
                    SettingsGroup {
                        SettingsItemCard(
                            title = stringResource(R.string.title_currency),
                            subtitle = stringResource(R.string.label_primary_currency),
                            icon = Icons.Rounded.CurrencyRupee,
                            valueText = if (uiState.currentCurrencyLabelRes != 0) {
                                stringResource(uiState.currentCurrencyLabelRes)
                            } else {
                                uiState.currentCurrencyLabel
                            },
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.Currency) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_decimal_places),
                            subtitle = stringResource(R.string.label_decimal_places_subtitle),
                            icon = Icons.Rounded.Straighten,
                            valueText = uiState.currentDecimalPlacesLabel,
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.DecimalPlaces) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_number_format),
                            subtitle = stringResource(R.string.label_number_format_subtitle),
                            icon = Icons.Rounded.Pin,
                            valueText = stringResource(uiState.currentGroupingLabelRes),
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.NumberFormat) }
                        )
                    }
                }

                item {
                    AdContainer(isAdsEnabled = isAdsEnabled) {
                        NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                    }
                }

                // Group 2: Date & Time
                item {
                    SettingsGroup {
                        SettingsItemCard(
                            title = stringResource(R.string.title_date_format),
                            subtitle = stringResource(R.string.label_date_format_subtitle),
                            icon = Icons.Rounded.DateRange,
                            valueText = uiState.currentDateFormatLabel,
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.DateFormat) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_time_format),
                            subtitle = stringResource(R.string.label_time_format_subtitle),
                            icon = Icons.Rounded.MoreTime,
                            valueText = stringResource(uiState.currentTimeFormatLabelRes),
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.TimeFormat) }
                        )
                    }
                }

                // Group 3: Appearance
                item {
                    SettingsGroup {
                        SettingsItemCard(
                            title = stringResource(R.string.title_theme),
                            subtitle = stringResource(R.string.label_theme_subtitle),
                            icon = Icons.Filled.Palette,
                            valueText = stringResource(uiState.currentThemeModeLabelRes),
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.ThemeMode) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_font),
                            subtitle = stringResource(R.string.label_font_subtitle),
                            icon = Icons.Filled.FontDownload,
                            valueText = when (uiState.selectedFontMode) {
                                com.mknlabs.expensetracker.models.FontMode.APP -> stringResource(R.string.label_font_app)
                                com.mknlabs.expensetracker.models.FontMode.SYSTEM -> stringResource(R.string.label_font_system)
                                com.mknlabs.expensetracker.models.FontMode.CUSTOM -> {
                                    val activeName = uiState.activeCustomFontFileName?.let {
                                        com.mknlabs.expensetracker.utils.FontFileHelper.fontDisplayName(it)
                                    } ?: stringResource(R.string.label_font_custom)
                                    activeName
                                }
                            },
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.Font) }
                        )
                    }
                }

                item {
                    AdContainer(isAdsEnabled = isAdsEnabled) {
                        NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                    }
                }
            }
            }
        }
    }

    // Universal Picker Sheet
    val activeSheet = uiState.activeSheet
    if (activeSheet != null) {
        when (activeSheet) {
            PreferencesSheetType.Currency -> {
                val currencyItems = remember(uiState.filteredCurrencies) {
                    uiState.filteredCurrencies.map { currency ->
                        SelectionItem(
                            id = currency.id,
                            title = currency.countryName,
                            subtitle = currency.currencyName,
                            leadingText = currency.currencySymbol
                        )
                    }
                }
                AppSelectionSheet<Int>(
                    title = stringResource(R.string.label_select_currency),
                    description = stringResource(R.string.label_search_by_country_and_pick_the),
                    items = currencyItems,
                    selectedId = uiState.selectedCurrencyId,
                    onItemSelected = { selectCurrency(it) },
                    onDismiss = {
                        clearCurrencySearchQuery()
                        dismissSheet()
                    },
                    showSearch = true,
                    searchQuery = uiState.currencySearchQuery,
                    onSearchQueryChange = updateCurrencySearchQuery,
                    searchPlaceholder = stringResource(R.string.label_search_country)
                )
            }

            PreferencesSheetType.ThemeMode -> {
                val themeItems = remember(uiState.themeModeOptions) {
                    uiState.themeModeOptions.map { option ->
                        SelectionItem(
                            id = option.themeMode,
                            title = "",
                            titleRes = option.labelRes,
                            subtitle = "",
                            subtitleRes = option.descriptionRes,
                            leadingIcon = Icons.Filled.Palette
                        )
                    }
                }
                AppSelectionSheet<com.mknlabs.expensetracker.models.AppThemeMode>(
                    title = stringResource(R.string.label_select_theme),
                    description = stringResource(R.string.label_theme_selection_desc),
                    items = themeItems,
                    selectedId = uiState.selectedThemeMode,
                    onItemSelected = { selectThemeMode(it) },
                    onDismiss = dismissSheet
                )
            }

            PreferencesSheetType.DateFormat -> {
                val dateItems = remember {
                    supportedDateFormats.map { option ->
                        SelectionItem(
                            id = option.pattern,
                            title = option.previewLabel,
                            subtitle = option.pattern,
                            leadingIcon = Icons.Filled.CalendarMonth
                        )
                    }
                }
                AppSelectionSheet<String>(
                    title = stringResource(R.string.label_select_date_format),
                    description = stringResource(R.string.label_choose_the_date_style_you_want),
                    items = dateItems,
                    selectedId = uiState.selectedDateFormatPattern,
                    onItemSelected = { selectDateFormat(it) },
                    onDismiss = dismissSheet
                )
            }

            PreferencesSheetType.TimeFormat -> {
                val timeItems = remember {
                    supportedTimeFormats.map { option ->
                        SelectionItem(
                            id = option.id,
                            title = "",
                            titleRes = option.labelRes,
                            subtitle = option.previewLabel,
                            leadingIcon = Icons.Filled.Tune
                        )
                    }
                }
                AppSelectionSheet<String>(
                    title = stringResource(R.string.label_select_time_format),
                    description = stringResource(R.string.label_choose_whether_time_is_shown_i),
                    items = timeItems,
                    selectedId = uiState.selectedTimeFormat,
                    onItemSelected = { selectTimeFormat(it) },
                    onDismiss = dismissSheet
                )
            }

            PreferencesSheetType.NumberFormat -> {
                val numberItems = remember(uiState.numberFormatOptions) {
                    uiState.numberFormatOptions.map { option ->
                        SelectionItem(
                            id = option.groupingStyle,
                            title = "",
                            titleRes = option.labelRes,
                            subtitle = option.preview,
                            leadingIcon = Icons.Filled.Tune
                        )
                    }
                }
                AppSelectionSheet<com.mknlabs.expensetracker.models.CurrencyGroupingStyle>(
                    title = stringResource(R.string.label_select_number_format),
                    description = stringResource(R.string.label_number_format_desc),
                    items = numberItems,
                    selectedId = uiState.selectedGroupingStyle,
                    onItemSelected = { selectGroupingStyle(it) },
                    onDismiss = dismissSheet
                )
            }

            PreferencesSheetType.DecimalPlaces -> {
                val decimalItems = remember(uiState.decimalPlaceOptions) {
                    uiState.decimalPlaceOptions.map { option ->
                        SelectionItem(
                            id = option.value,
                            title = option.value.toString(),
                            subtitle = option.preview,
                            leadingIcon = Icons.Filled.Tune
                        )
                    }
                }
                AppSelectionSheet<Int>(
                    title = stringResource(R.string.label_select_decimal_places),
                    description = stringResource(R.string.label_choose_how_many_decimal_places),
                    items = decimalItems,
                    selectedId = uiState.selectedDecimalPlaces,
                    onItemSelected = { selectDecimalPlaces(it) },
                    onDismiss = dismissSheet
                )
            }

            PreferencesSheetType.Font -> {
                FontPickerSheet(
                    selectedFontMode = uiState.selectedFontMode,
                    activeCustomFontFileName = uiState.activeCustomFontFileName,
                    importedFontFileNames = uiState.importedFontFileNames,
                    onSelectFontMode = { fontMode, fileName ->
                        selectFontMode(fontMode, fileName)
                    },
                    onImportFont = onImportFont,
                    onDeleteFont = onDeleteFont,
                    onDismiss = dismissSheet
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontPickerSheet(
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
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.msg_delete_font_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.msg_delete_font_desc,
                        com.mknlabs.expensetracker.utils.FontFileHelper.fontDisplayName(fileName)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteFont(fileName)
                    showDeleteDialog = null
                }) {
                    Text(stringResource(R.string.label_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.label_cancel))
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.label_select_font),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.label_select_font_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Built-in",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            FontOptionItem(
                title = stringResource(R.string.label_font_app),
                isSelected = selectedFontMode == com.mknlabs.expensetracker.models.FontMode.APP,
                onClick = { onSelectFontMode(com.mknlabs.expensetracker.models.FontMode.APP, null) }
            )

            FontOptionItem(
                title = stringResource(R.string.label_font_system),
                isSelected = selectedFontMode == com.mknlabs.expensetracker.models.FontMode.SYSTEM,
                onClick = { onSelectFontMode(com.mknlabs.expensetracker.models.FontMode.SYSTEM, null) }
            )

            if (importedFontFileNames.isNotEmpty() || selectedFontMode == com.mknlabs.expensetracker.models.FontMode.CUSTOM) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.label_font_count,
                            importedFontFileNames.size,
                            com.mknlabs.expensetracker.utils.FontFileHelper.MAX_CUSTOM_FONTS
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                importedFontFileNames.forEach { fileName ->
                    FontOptionItem(
                        title = com.mknlabs.expensetracker.utils.FontFileHelper.fontDisplayName(fileName),
                        isSelected = selectedFontMode == com.mknlabs.expensetracker.models.FontMode.CUSTOM && activeCustomFontFileName == fileName,
                        onClick = { onSelectFontMode(com.mknlabs.expensetracker.models.FontMode.CUSTOM, fileName) },
                        onDelete = { showDeleteDialog = fileName }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canImport = com.mknlabs.expensetracker.utils.FontFileHelper.canImport(importedFontFileNames.size)
            Button(
                onClick = onImportFont,
                modifier = Modifier.fillMaxWidth(),
                enabled = canImport,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FontDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.label_import_font))
            }

            if (!canImport) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.label_max_fonts_reached,
                        com.mknlabs.expensetracker.utils.FontFileHelper.MAX_CUSTOM_FONTS
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FontOptionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.label_delete_font),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreferencesScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        PreferencesScreenContent(
            uiState = com.mknlabs.expensetracker.ui.viewmodels.PreferencesScreenUiState(
                currentCurrencyLabel = "INR",
                currentDecimalPlacesLabel = "2 decimal places",
                currentGroupingLabelRes = R.string.label_grouping_indian,
                currentDateFormatLabel = "dd/MM/yyyy",
                currentTimeFormatLabelRes = R.string.label_24hour,
                currentThemeModeLabelRes = R.string.label_theme_dark,
            ),
            isAdsEnabled = true,
            onManageCategoryClick = {},
            onBackClick = {},
            showSheet = {},
            selectCurrency = {},
            clearCurrencySearchQuery = {},
            dismissSheet = {},
            updateCurrencySearchQuery = {},
            selectThemeMode = {},
            selectDateFormat = {},
            selectTimeFormat = {},
            selectGroupingStyle = {},
            selectDecimalPlaces = {},
            selectFontMode = { _, _ -> },
            onImportFont = {},
            onDeleteFont = {}
        )
    }
}

