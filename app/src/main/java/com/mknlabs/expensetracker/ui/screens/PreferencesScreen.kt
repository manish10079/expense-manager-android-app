package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.AppSelectionSheet
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
        selectDecimalPlaces = { preferencesViewModel.selectDecimalPlaces(it) }
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
    selectDecimalPlaces: (Int) -> Unit
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
            selectDecimalPlaces = {}
        )
    }
}

