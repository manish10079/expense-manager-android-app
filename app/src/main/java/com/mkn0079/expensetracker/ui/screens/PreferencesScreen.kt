package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.models.AppThemeMode
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.components.AppSelectionSheet
import com.mkn0079.expensetracker.ui.models.SelectionItem
import com.mkn0079.expensetracker.ui.viewmodels.PreferencesSheetType
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            AppHeader(
                title = "App Preferences",
                onBackClick = onBackClick
            )
            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PreferenceItemRow(
                    title = "Currency",
                    icon = Icons.Filled.CurrencyRupee,
                    trailing = uiState.currentCurrencyLabel,
                    onClick = { preferencesViewModel.showSheet(PreferencesSheetType.Currency) }
                )
                PreferenceItemRow(
                    title = "Theme",
                    icon = Icons.Filled.Palette,
                    trailing = uiState.currentThemeModeLabel,
                    onClick = { preferencesViewModel.showSheet(PreferencesSheetType.ThemeMode) }
                )
                PreferenceItemRow(
                    title = "Date Format",
                    icon = Icons.Filled.CalendarMonth,
                    trailing = uiState.currentDateFormatLabel,
                    onClick = { preferencesViewModel.showSheet(PreferencesSheetType.DateFormat) }
                )
                PreferenceItemRow(
                    title = "Time Format",
                    icon = Icons.Filled.Tune,
                    trailing = uiState.currentTimeFormatLabel,
                    onClick = { preferencesViewModel.showSheet(PreferencesSheetType.TimeFormat) }
                )
                PreferenceItemRow(
                    title = "Number Format",
                    icon = Icons.Filled.Tune,
                    trailing = uiState.currentGroupingLabel,
                    onClick = { preferencesViewModel.showSheet(PreferencesSheetType.NumberFormat) }
                )
                PreferenceItemRow(
                    title = "Decimal Places",
                    icon = Icons.Filled.Tune,
                    trailing = uiState.currentDecimalPlacesLabel,
                    onClick = { preferencesViewModel.showSheet(PreferencesSheetType.DecimalPlaces) }
                )
                PreferenceItemRow(
                    title = "Manage Category",
                    icon = Icons.Filled.Apps,
                    trailing = null,
                    onClick = onManageCategoryClick
                )
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

@Composable
private fun PreferenceItemRow(
    title: String,
    icon: ImageVector,
    trailing: String?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f)
            )

            if (trailing != null) {
                Text(
                    text = trailing,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
