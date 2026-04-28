package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.mkn0079.expensetracker.models.Currency
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.viewmodels.DecimalPlacesOptionUi
import com.mkn0079.expensetracker.ui.viewmodels.NumberFormatOptionUi
import com.mkn0079.expensetracker.ui.viewmodels.PreferencesScreenUiState
import com.mkn0079.expensetracker.ui.viewmodels.PreferencesSheetType
import com.mkn0079.expensetracker.ui.viewmodels.PreferencesViewModel
import com.mkn0079.expensetracker.ui.viewmodels.ThemeModeOptionUi
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

    when (uiState.activeSheet) {
        PreferencesSheetType.Currency -> {
            CurrencyPickerSheet(
                searchQuery = uiState.currencySearchQuery,
                filteredCurrencies = uiState.filteredCurrencies,
                selectedCurrencyId = uiState.selectedCurrencyId,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onDismiss = {
                    preferencesViewModel.clearCurrencySearchQuery()
                    preferencesViewModel.dismissSheet()
                },
                onSearchQueryChange = preferencesViewModel::updateCurrencySearchQuery,
                onCurrencySelected = preferencesViewModel::selectCurrency
            )
        }

        PreferencesSheetType.ThemeMode -> {
            ThemeModePickerSheet(
                options = uiState.themeModeOptions,
                selectedThemeMode = uiState.selectedThemeMode,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onDismiss = preferencesViewModel::dismissSheet,
                onSelected = preferencesViewModel::selectThemeMode
            )
        }

        PreferencesSheetType.DateFormat -> {
            DateFormatPickerSheet(
                selectedPattern = uiState.selectedDateFormatPattern,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onDismiss = preferencesViewModel::dismissSheet,
                onFormatSelected = preferencesViewModel::selectDateFormat
            )
        }

        PreferencesSheetType.TimeFormat -> {
            TimeFormatPickerSheet(
                selectedTimeFormat = uiState.selectedTimeFormat,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onDismiss = preferencesViewModel::dismissSheet,
                onFormatSelected = preferencesViewModel::selectTimeFormat
            )
        }

        PreferencesSheetType.NumberFormat -> {
            NumberFormatPickerSheet(
                options = uiState.numberFormatOptions,
                selectedLabel = uiState.currentGroupingLabel,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onDismiss = preferencesViewModel::dismissSheet,
                onSelected = preferencesViewModel::selectGroupingStyle
            )
        }

        PreferencesSheetType.DecimalPlaces -> {
            DecimalPlacesPickerSheet(
                options = uiState.decimalPlaceOptions,
                selectedValue = uiState.selectedDecimalPlaces,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onDismiss = preferencesViewModel::dismissSheet,
                onSelected = preferencesViewModel::selectDecimalPlaces
            )
        }

        null -> Unit
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
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    searchQuery: String,
    filteredCurrencies: List<Currency>,
    selectedCurrencyId: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCurrencySelected: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Currency",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Search by country and pick the currency you want to use across the app.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search currency",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                placeholder = {
                    Text(
                        text = "Search country",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredCurrencies,
                    key = { currency -> currency.id }
                ) { currency ->
                    CurrencyPickerRow(
                        currency = currency,
                        isSelected = currency.id == selectedCurrencyId,
                        onClick = { onCurrencySelected(currency.id) }
                    )
                }

                if (filteredCurrencies.isEmpty()) {
                    item {
                        EmptyPickerState(text = "No countries matched your search.")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun CurrencyPickerRow(
    currency: Currency,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    PickerRow(
        title = currency.countryName,
        subtitle = currency.currencyName,
        leading = currency.currencySymbol,
        isSelected = isSelected,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeModePickerSheet(
    options: List<ThemeModeOptionUi>,
    selectedThemeMode: AppThemeMode,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelected: (AppThemeMode) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Theme",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose whether the app follows your device or stays on a fixed theme.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(options, key = { option -> option.themeMode.name }) { option ->
                    PickerRow(
                        title = option.label,
                        subtitle = option.description,
                        leadingIcon = Icons.Filled.Palette,
                        isSelected = option.themeMode == selectedThemeMode,
                        onClick = { onSelected(option.themeMode) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFormatPickerSheet(
    selectedPattern: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onFormatSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Date Format",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose the date style you want to see across the app.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(supportedDateFormats, key = { option -> option.pattern }) { option ->
                    PickerRow(
                        title = option.previewLabel,
                        subtitle = option.pattern,
                        leadingIcon = Icons.Filled.CalendarMonth,
                        isSelected = option.pattern == selectedPattern,
                        onClick = { onFormatSelected(option.pattern) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeFormatPickerSheet(
    selectedTimeFormat: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onFormatSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Time Format",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose whether time is shown in 12-hour or 24-hour style.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(supportedTimeFormats, key = { option -> option.id }) { option ->
                    PickerRow(
                        title = option.label,
                        subtitle = option.previewLabel,
                        leadingIcon = Icons.Filled.Tune,
                        isSelected = option.id == selectedTimeFormat,
                        onClick = { onFormatSelected(option.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberFormatPickerSheet(
    options: List<NumberFormatOptionUi>,
    selectedLabel: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelected: (com.mkn0079.expensetracker.models.CurrencyGroupingStyle) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Number Format",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose how large amounts are grouped across the app.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(options, key = { option -> option.groupingStyle.name }) { option ->
                    PickerRow(
                        title = option.label,
                        subtitle = option.preview,
                        leadingIcon = Icons.Filled.Tune,
                        isSelected = option.label == selectedLabel,
                        onClick = { onSelected(option.groupingStyle) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecimalPlacesPickerSheet(
    options: List<DecimalPlacesOptionUi>,
    selectedValue: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSelected: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Decimal Places",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose how many decimal places are shown in currency values.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(options, key = { option -> option.value }) { option ->
                    PickerRow(
                        title = option.value.toString(),
                        subtitle = option.preview,
                        leadingIcon = Icons.Filled.Tune,
                        isSelected = option.value == selectedValue,
                        onClick = { onSelected(option.value) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    leading: String? = null,
    leadingIcon: ImageVector? = null
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                leading != null -> {
                    Text(
                        text = leading,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                leadingIcon != null -> {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = "Selected",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun EmptyPickerState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
