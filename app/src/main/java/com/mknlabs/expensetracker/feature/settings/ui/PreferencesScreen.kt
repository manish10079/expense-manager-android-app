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
fun PreferencesScreen(
    onManageCategoryClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    preferencesViewModel: PreferencesViewModel = hiltViewModel(),
    isAdsEnabled: Boolean = false,
    userTier: com.mknlabs.expensetracker.models.UserTier = com.mknlabs.expensetracker.models.UserTier.FREE
) {
    val uiState by preferencesViewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context.findFragmentActivity()
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
        selectLanguage = { preferencesViewModel.selectLanguage(it, activity) },
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
internal fun PreferencesScreenContent(
    uiState: PreferencesScreenUiState,
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
    selectLanguage: (String) -> Unit,
    selectDateFormat: (String) -> Unit,
    selectTimeFormat: (String) -> Unit,
    selectGroupingStyle: (com.mknlabs.expensetracker.models.CurrencyGroupingStyle) -> Unit,
    selectDecimalPlaces: (Int) -> Unit,
    onSelectFontMode: (com.mknlabs.expensetracker.models.FontMode, String?) -> Unit,
    onImportFont: () -> Unit,
    onDeleteFont: (String) -> Unit,
    selectMonthStartDay: (Int) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
    ) {
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
                title = stringResource(R.string.title_app_preferences),
                onBackClick = onBackClick,
                modifier = Modifier.alpha(enter[0])
            )

            AdaptiveContent(
                maxWidth = 640.dp,
                modifier = Modifier.weight(1f).alpha(enter[1])
            ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Group 1: Currency & Formats
                item {
                    SettingsGroup(colors = settingsGroupCardColors()) {
                        SettingsItemCard(
                            title = stringResource(R.string.title_currency),
                            iconTile = SettingsTiles.Sky,
                            subtitle = stringResource(R.string.label_primary_currency),
                            icon = PhosphorIcons.Regular.CurrencyInr,
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
                            iconTile = SettingsTiles.Emerald,
                            subtitle = stringResource(R.string.label_decimal_places_subtitle),
                            icon = PhosphorIcons.Regular.Ruler,
                            valueText = uiState.currentDecimalPlacesLabel,
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.DecimalPlaces) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_number_format),
                            iconTile = SettingsTiles.Amber,
                            subtitle = stringResource(R.string.label_number_format_subtitle),
                            icon = PhosphorIcons.Regular.PushPin,
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
                    SettingsGroup(colors = settingsGroupCardColors()) {
                        SettingsItemCard(
                            title = stringResource(R.string.title_date_format),
                            iconTile = SettingsTiles.Indigo,
                            subtitle = stringResource(R.string.label_date_format_subtitle),
                            icon = PhosphorIcons.Regular.CalendarBlank,
                            valueText = uiState.currentDateFormatLabel,
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.DateFormat) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_time_format),
                            iconTile = SettingsTiles.Fuchsia,
                            subtitle = stringResource(R.string.label_time_format_subtitle),
                            icon = PhosphorIcons.Regular.Clock,
                            valueText = stringResource(uiState.currentTimeFormatLabelRes),
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.TimeFormat) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_month_start_day),
                            iconTile = SettingsTiles.Teal,
                            subtitle = stringResource(R.string.label_month_start_day_subtitle),
                            icon = PhosphorIcons.Regular.CalendarDots,
                            valueText = if (uiState.monthStartDay == 1) {
                                stringResource(R.string.label_month_start_day_standard)
                            } else {
                                stringResource(R.string.label_month_start_day_value, uiState.monthStartDay)
                            },
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.MonthStartDay) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_language),
                            iconTile = SettingsTiles.Rose,
                            subtitle = stringResource(R.string.label_language_subtitle),
                            icon = PhosphorIcons.Regular.Globe,
                            valueText = uiState.currentLanguageLabel,
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.Language) }
                        )
                    }
                }

                item {
                    AdContainer(isAdsEnabled = isAdsEnabled) {
                        NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                    }
                }

                // Group 3: Appearance — the theme mode and the app font share one card, so the
                // two things that change how the app looks sit together apart from formats.
                item {
                    SettingsGroup(colors = settingsGroupCardColors()) {
                        SettingsItemCard(
                            title = stringResource(R.string.title_theme),
                            iconTile = SettingsTiles.Violet,
                            subtitle = stringResource(R.string.label_theme_subtitle),
                            icon = PhosphorIcons.Regular.Palette,
                            valueText = stringResource(uiState.currentThemeModeLabelRes),
                            type = SettingsItemType.Value,
                            standalone = false,
                            onClick = { showSheet(PreferencesSheetType.ThemeMode) }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_font),
                            iconTile = SettingsTiles.Pink,
                            subtitle = stringResource(R.string.label_font_subtitle),
                            icon = PhosphorIcons.Regular.TextAa,
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
                    containerColor = colorScheme.profileSheet,
                    rowColor = colorScheme.genderRow,
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

            PreferencesSheetType.Language -> {
                val languageItems = remember(uiState.languageOptions) {
                    uiState.languageOptions.map { option ->
                        // Each language is recognised by a letter of its own script in the
                        // leading tile, the way the currency sheet puts a symbol there; only
                        // the System row, which is a device choice, keeps an icon.
                        SelectionItem(
                            id = option.code,
                            title = option.nativeName,
                            subtitle = option.englishName,
                            leadingText = option.glyph,
                            leadingIcon = if (option.glyph == null) PhosphorIcons.Regular.Phone else null
                        )
                    }
                }
                AppSelectionSheet<String>(
                    title = stringResource(R.string.label_select_language),
                    description = stringResource(R.string.label_language_selection_desc),
                    items = languageItems,
                    selectedId = uiState.selectedLanguageCode,
                    containerColor = colorScheme.profileSheet,
                    rowColor = colorScheme.genderRow,
                    // 21 languages: open the whole sheet so the list gets the screen instead of
                    // ending 440dp down with a flick left to scroll.
                    fullHeight = true,
                    onItemSelected = { selectLanguage(it) },
                    onDismiss = dismissSheet
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
                            leadingIcon = PhosphorIcons.Regular.Palette
                        )
                    }
                }
                AppSelectionSheet<com.mknlabs.expensetracker.models.AppThemeMode>(
                    title = stringResource(R.string.label_select_theme),
                    description = stringResource(R.string.label_theme_selection_desc),
                    items = themeItems,
                    selectedId = uiState.selectedThemeMode,
                    containerColor = colorScheme.profileSheet,
                    rowColor = colorScheme.genderRow,
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
                            leadingIcon = PhosphorIcons.Regular.CalendarBlank
                        )
                    }
                }
                AppSelectionSheet<String>(
                    title = stringResource(R.string.label_select_date_format),
                    description = stringResource(R.string.label_choose_the_date_style_you_want),
                    items = dateItems,
                    selectedId = uiState.selectedDateFormatPattern,
                    containerColor = colorScheme.profileSheet,
                    rowColor = colorScheme.genderRow,
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
                            leadingIcon = PhosphorIcons.Regular.SlidersHorizontal
                        )
                    }
                }
                AppSelectionSheet<String>(
                    title = stringResource(R.string.label_select_time_format),
                    description = stringResource(R.string.label_choose_whether_time_is_shown_i),
                    items = timeItems,
                    selectedId = uiState.selectedTimeFormat,
                    containerColor = colorScheme.profileSheet,
                    rowColor = colorScheme.genderRow,
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
                            leadingIcon = PhosphorIcons.Regular.CalendarBlank
                        )
                    }
                }
                AppSelectionSheet<String>(
                    title = stringResource(R.string.title_month_start_day),
                    description = stringResource(R.string.label_month_start_day_subtitle),
                    items = dayItems,
                    selectedId = uiState.monthStartDay.toString(),
                    containerColor = colorScheme.profileSheet,
                    rowColor = colorScheme.genderRow,
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
                            leadingIcon = PhosphorIcons.Regular.SlidersHorizontal
                        )
                    }
                }
                AppSelectionSheet<com.mknlabs.expensetracker.models.CurrencyGroupingStyle>(
                    title = stringResource(R.string.label_select_number_format),
                    description = stringResource(R.string.label_number_format_desc),
                    items = numberItems,
                    selectedId = uiState.selectedGroupingStyle,
                    containerColor = colorScheme.profileSheet,
                    rowColor = colorScheme.genderRow,
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
                            leadingIcon = PhosphorIcons.Regular.SlidersHorizontal
                        )
                    }
                }
                AppSelectionSheet<Int>(
                    title = stringResource(R.string.label_select_decimal_places),
                    description = stringResource(R.string.label_choose_how_many_decimal_places),
                    items = decimalItems,
                    selectedId = uiState.selectedDecimalPlaces,
                    containerColor = colorScheme.profileSheet,
                    rowColor = colorScheme.genderRow,
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

