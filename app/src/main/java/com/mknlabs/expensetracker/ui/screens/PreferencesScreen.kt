package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.AppSelectionSheet
import com.mknlabs.expensetracker.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.ui.components.SettingsGroup
import com.mknlabs.expensetracker.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.ui.components.GatedAction
import com.mknlabs.expensetracker.ui.models.SelectionItem
import com.mknlabs.expensetracker.ui.viewmodels.PreferencesSheetType
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.AccessLevel
import com.mknlabs.expensetracker.ui.theme.featureGateLock
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
    isAdsEnabled: Boolean = false,
    userTier: com.mknlabs.expensetracker.models.UserTier = com.mknlabs.expensetracker.models.UserTier.FREE
) {
    val uiState by preferencesViewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val scope = rememberCoroutineScope()

    // Font import launcher (only for Pro users)
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
        userTier = userTier,
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
        onSelectFontMode = { fontMode, fileName ->
            preferencesViewModel.selectFontMode(fontMode, fileName)
            android.widget.Toast.makeText(
                context,
                context.getString(R.string.msg_font_changed),
                android.widget.Toast.LENGTH_SHORT
            ).show()
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
        },
        selectMonthStartDay = { day -> preferencesViewModel.selectMonthStartDay(day) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferencesScreenContent(
    uiState: com.mknlabs.expensetracker.ui.viewmodels.PreferencesScreenUiState,
    isAdsEnabled: Boolean,
    userTier: com.mknlabs.expensetracker.models.UserTier,
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
    onSelectFontMode: (com.mknlabs.expensetracker.models.FontMode, String?) -> Unit,
    onImportFont: () -> Unit,
    onDeleteFont: (String) -> Unit,
    selectMonthStartDay: (Int) -> Unit
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
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_month_start_day),
                            subtitle = stringResource(R.string.label_month_start_day_subtitle),
                            icon = Icons.Rounded.CalendarToday,
                            valueText = if (uiState.monthStartDay == 1) {
                                stringResource(R.string.label_month_start_day_standard)
                            } else {
                                stringResource(R.string.label_month_start_day_value, uiState.monthStartDay)
                            },
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.MonthStartDay) }
                        )
                    }
                }

                // Group 3: Appearance
                item {
                    var isFontCardVisible by remember { mutableStateOf(false) }
                    val themeScope = rememberCoroutineScope()
                    SettingsGroup {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        val startTime = System.currentTimeMillis()
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Main)
                                            if (event.changes.all { !it.pressed }) break
                                            if (System.currentTimeMillis() - startTime >= 5_000L) {
                                                isFontCardVisible = true
                                                break
                                            }
                                        }
                                    }
                                }
                        ) {
                            SettingsItemCard(
                                title = stringResource(R.string.title_theme),
                                subtitle = stringResource(R.string.label_theme_subtitle),
                                icon = Icons.Filled.Palette,
                                valueText = stringResource(uiState.currentThemeModeLabelRes),
                                type = SettingsItemType.Value,
                                standalone = false,
                                onClick = { showSheet(PreferencesSheetType.ThemeMode) }
                            )
                        }
                        if (isFontCardVisible) {
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

            PreferencesSheetType.MonthStartDay -> {
                val dayItems = remember {
                    (com.mknlabs.expensetracker.utils.CustomMonthUtils.MIN_MONTH_START_DAY..
                        com.mknlabs.expensetracker.utils.CustomMonthUtils.MAX_MONTH_START_DAY).map { day ->
                        SelectionItem(
                            id = day.toString(),
                            title = if (day == 1) "1st (Standard)" else "$day${getOrdinalSuffix(day)}",
                            subtitle = null,
                            leadingIcon = Icons.Filled.CalendarMonth
                        )
                    }
                }
                AppSelectionSheet<String>(
                    title = stringResource(R.string.title_month_start_day),
                    description = stringResource(R.string.label_month_start_day_subtitle),
                    items = dayItems,
                    selectedId = uiState.monthStartDay.toString(),
                    onItemSelected = { day ->
                        selectMonthStartDay(day.toInt())
                        dismissSheet()
                    },
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
                        onSelectFontMode(fontMode, fileName)
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
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canImport,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FontDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isGated) {
                            if (accessLevel == AccessLevel.AD_SUPPORTED) stringResource(R.string.label_watch_ad) else stringResource(R.string.label_pro_required)
                        } else {
                            stringResource(R.string.label_import_font)
                        }
                    )
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
private fun FontOptionItem(
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

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else if (isGated) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick?.let { { it() } }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isGated) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = lockColor,
                modifier = Modifier.size(20.dp)
            )
        } else if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(if (isGated || isSelected) 14.dp else 34.dp))
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
                        imageVector = Icons.Rounded.Lock,
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
        if (isSelected) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.label_selected),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        } else if (isGated) {
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
            userTier = com.mknlabs.expensetracker.models.UserTier.FREE,
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
            onSelectFontMode = { _, _ -> },
            onImportFont = {},
            onDeleteFont = {},
            selectMonthStartDay = {}
        )
    }
}

private fun getOrdinalSuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}

