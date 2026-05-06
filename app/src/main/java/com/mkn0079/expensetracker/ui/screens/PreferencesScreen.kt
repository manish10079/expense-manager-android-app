package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.models.SettingsItemType
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.components.AppSelectionSheet
import com.mkn0079.expensetracker.ui.components.SettingsItemCard
import com.mkn0079.expensetracker.ui.models.SelectionItem
import com.mkn0079.expensetracker.ui.viewmodels.PreferencesSheetType
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.viewmodels.PreferencesViewModel
import com.mkn0079.expensetracker.utils.supportedDateFormats
import com.mkn0079.expensetracker.utils.supportedTimeFormats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onManageCategoryClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    preferencesViewModel: PreferencesViewModel = viewModel()
) {
    val uiState by preferencesViewModel.uiState.collectAsStateWithLifecycle()

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
                title = "App Preferences",
                onBackClick = onBackClick
            )
            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    SettingsItemCard(
                        title = "Currency",
                        subtitle = "Primary app currency",
                        icon = Icons.Rounded.CurrencyRupee,
                        valueText = uiState.currentCurrencyLabel,
                        type = SettingsItemType.Value,
                        onClick = { preferencesViewModel.showSheet(PreferencesSheetType.Currency) }
                    )
                }
                item {
                    SettingsItemCard(
                        title = "Theme",
                        subtitle = "App color appearance",
                        icon = Icons.Filled.Palette,
                        valueText = uiState.currentThemeModeLabel,
                        type = SettingsItemType.Value,
                        onClick = { preferencesViewModel.showSheet(PreferencesSheetType.ThemeMode) }
                    )
                }
                item {
                    SettingsItemCard(
                        title = "Date Format",
                        subtitle = "How dates are shown",
                        icon = Icons.Rounded.DateRange,
                        valueText = uiState.currentDateFormatLabel,
                        type = SettingsItemType.Value,
                        onClick = { preferencesViewModel.showSheet(PreferencesSheetType.DateFormat) }
                    )
                }
                item {
                    SettingsItemCard(
                        title = "Time Format",
                        subtitle = "12h or 24h clock",
                        icon = Icons.Rounded.MoreTime,
                        valueText = uiState.currentTimeFormatLabel,
                        type = SettingsItemType.Value,
                        onClick = { preferencesViewModel.showSheet(PreferencesSheetType.TimeFormat) }
                    )
                }
                item {
                    SettingsItemCard(
                        title = "Number Format",
                        subtitle = "Digit grouping style",
                        icon = Icons.Rounded.Pin,
                        valueText = uiState.currentGroupingLabel,
                        type = SettingsItemType.Value,
                        onClick = { preferencesViewModel.showSheet(PreferencesSheetType.NumberFormat) }
                    )
                }
                item {
                    SettingsItemCard(
                        title = "Decimal Places",
                        subtitle = "Amount precision",
                        icon = Icons.Rounded.Straighten,
                        valueText = uiState.currentDecimalPlacesLabel,
                        type = SettingsItemType.Value,
                        onClick = { preferencesViewModel.showSheet(PreferencesSheetType.DecimalPlaces) }
                    )
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
                AppSelectionSheet(
                    title = "Select Currency",
                    description = "Search by country and pick the currency you want to use across the app.",
                    items = currencyItems,
                    selectedId = uiState.selectedCurrencyId,
                    onItemSelected = { preferencesViewModel.selectCurrency(it) },
                    onDismiss = {
                        preferencesViewModel.clearCurrencySearchQuery()
                        preferencesViewModel.dismissSheet()
                    },
                    showSearch = true,
                    searchQuery = uiState.currencySearchQuery,
                    onSearchQueryChange = preferencesViewModel::updateCurrencySearchQuery,
                    searchPlaceholder = "Search country"
                )
            }

            PreferencesSheetType.ThemeMode -> {
                val themeItems = remember(uiState.themeModeOptions) {
                    uiState.themeModeOptions.map { option ->
                        SelectionItem(
                            id = option.themeMode,
                            title = option.label,
                            subtitle = option.description,
                            leadingIcon = Icons.Filled.Palette
                        )
                    }
                }
                AppSelectionSheet(
                    title = "Select Theme",
                    description = "Choose whether the app follows your device or stays on a fixed theme.",
                    items = themeItems,
                    selectedId = uiState.selectedThemeMode,
                    onItemSelected = { preferencesViewModel.selectThemeMode(it) },
                    onDismiss = preferencesViewModel::dismissSheet
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
                AppSelectionSheet(
                    title = "Select Date Format",
                    description = "Choose the date style you want to see across the app.",
                    items = dateItems,
                    selectedId = uiState.selectedDateFormatPattern,
                    onItemSelected = { preferencesViewModel.selectDateFormat(it) },
                    onDismiss = preferencesViewModel::dismissSheet
                )
            }

            PreferencesSheetType.TimeFormat -> {
                val timeItems = remember {
                    supportedTimeFormats.map { option ->
                        SelectionItem(
                            id = option.id,
                            title = option.label,
                            subtitle = option.previewLabel,
                            leadingIcon = Icons.Filled.Tune
                        )
                    }
                }
                AppSelectionSheet(
                    title = "Select Time Format",
                    description = "Choose whether time is shown in 12-hour or 24-hour style.",
                    items = timeItems,
                    selectedId = uiState.selectedTimeFormat,
                    onItemSelected = { preferencesViewModel.selectTimeFormat(it) },
                    onDismiss = preferencesViewModel::dismissSheet
                )
            }

            PreferencesSheetType.NumberFormat -> {
                val numberItems = remember(uiState.numberFormatOptions) {
                    uiState.numberFormatOptions.map { option ->
                        SelectionItem(
                            id = option.groupingStyle,
                            title = option.label,
                            subtitle = option.preview,
                            leadingIcon = Icons.Filled.Tune
                        )
                    }
                }
                AppSelectionSheet(
                    title = "Select Number Format",
                    description = "Choose how large amounts are grouped across the app.",
                    items = numberItems,
                    selectedId = uiState.selectedGroupingStyle,
                    onItemSelected = { preferencesViewModel.selectGroupingStyle(it) },
                    onDismiss = preferencesViewModel::dismissSheet
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
                AppSelectionSheet(
                    title = "Select Decimal Places",
                    description = "Choose how many decimal places are shown in currency values.",
                    items = decimalItems,
                    selectedId = uiState.selectedDecimalPlaces,
                    onItemSelected = { preferencesViewModel.selectDecimalPlaces(it) },
                    onDismiss = preferencesViewModel::dismissSheet
                )
            }
        }
    }
}

