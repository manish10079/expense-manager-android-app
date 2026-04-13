package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.*
import com.mkn0079.expensetracker.models.Currency
import com.mkn0079.expensetracker.ui.theme.*
import com.mkn0079.expensetracker.ui.viewmodels.SettingsViewModel
import com.mkn0079.expensetracker.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    currentCurrencyId: Int = DEFAULT_CURRENCY_ID,
    currentDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    currentTimeFormat: String = DEFAULT_TIME_FORMAT,
    onCurrencyChange: (Int) -> Unit = {},
    onDateFormatChange: (String) -> Unit = {},
    onTimeFormatChange: (String) -> Unit = {},
    onManageCategoryClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var isCurrencyPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isDateFormatPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isTimeFormatPickerVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(currentCurrencyId, currentDateFormatPattern, currentTimeFormat) {
        settingsViewModel.updateInputs(
            currentCurrencyId = currentCurrencyId,
            currentDateFormatPattern = currentDateFormatPattern,
            currentTimeFormat = currentTimeFormat,
            autoLockDurationMinutes = 0,
            transactionCount = 0
        )
    }

    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val currencyPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timeFormatPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentCurrencyLabel = uiState.filteredCurrencies.find { it.id == currentCurrencyId }
        ?.let { "${it.currencySymbol} ${it.countryName}" }
        ?: "Select"

    val currentDateFormatLabel = getDateFormatPreviewLabel(currentDateFormatPattern)
    val currentTimeFormatLabel = getTimeFormatPreviewLabel(currentTimeFormat)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundDark, Color(0xFF0B0B0C), BackgroundDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.03f))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE4DBF6),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "App Preferences",
                    color = PurplePrimary,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF18181A))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PreferenceItemRow(
                    title = "Currency",
                    icon = Icons.Filled.CurrencyRupee,
                    trailing = currentCurrencyLabel,
                    onClick = { isCurrencyPickerVisible = true }
                )
                PreferenceItemRow(
                    title = "Date Format",
                    icon = Icons.Filled.CalendarMonth,
                    trailing = currentDateFormatLabel,
                    onClick = { isDateFormatPickerVisible = true }
                )
                PreferenceItemRow(
                    title = "Time Format",
                    icon = Icons.Filled.Tune,
                    trailing = currentTimeFormatLabel,
                    onClick = { isTimeFormatPickerVisible = true }
                )
                PreferenceItemRow(
                    title = "Manage Category",
                    icon = Icons.Filled.Apps,
                    trailing = null,
                    onClick = { onManageCategoryClick() }
                )
            }
        }
    }

    if (isCurrencyPickerVisible) {
        CurrencyPickerSheet(
            searchQuery = uiState.currencySearchQuery,
            filteredCurrencies = uiState.filteredCurrencies,
            selectedCurrencyId = currentCurrencyId,
            sheetState = currencyPickerSheetState,
            onDismiss = {
                settingsViewModel.clearCurrencySearchQuery()
                isCurrencyPickerVisible = false
            },
            onSearchQueryChange = settingsViewModel::updateCurrencySearchQuery,
            onCurrencySelected = { currencyId ->
                onCurrencyChange(currencyId)
                settingsViewModel.clearCurrencySearchQuery()
                isCurrencyPickerVisible = false
            }
        )
    }

    if (isDateFormatPickerVisible) {
        DateFormatPickerSheet(
            selectedPattern = currentDateFormatPattern,
            sheetState = dateFormatPickerSheetState,
            onDismiss = { isDateFormatPickerVisible = false },
            onFormatSelected = { pattern ->
                onDateFormatChange(pattern)
                isDateFormatPickerVisible = false
            }
        )
    }

    if (isTimeFormatPickerVisible) {
        TimeFormatPickerSheet(
            selectedTimeFormat = currentTimeFormat,
            sheetState = timeFormatPickerSheetState,
            onDismiss = { isTimeFormatPickerVisible = false },
            onFormatSelected = { timeFormat ->
                onTimeFormatChange(timeFormat)
                isTimeFormatPickerVisible = false
            }
        )
    }
}

@Composable
private fun PreferenceItemRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    .background(Color(0xFF232326)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PurpleAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                modifier = Modifier.weight(1f)
            )

            if (trailing != null) {
                Text(
                    text = trailing,
                    color = Color(0xFF898297),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open title",
                tint = Color(0xFF6F687C),
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
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Currency",
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Search by country and pick the currency you want to use across the app.",
                color = Color(0xFF968EA8),
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
                        tint = Color(0xFF9B93AE)
                    )
                },
                placeholder = {
                    Text(
                        text = "Search country",
                        color = Color(0xFF7E778D)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1D1D21),
                    unfocusedContainerColor = Color(0xFF1D1D21),
                    focusedBorderColor = PurpleAccent.copy(alpha = 0.7f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedTextColor = Color(0xFFF0EBF7),
                    unfocusedTextColor = Color(0xFFF0EBF7),
                    cursorColor = PurpleAccent
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1A1A1E))
                                .padding(horizontal = 18.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = "No countries matched your search.",
                                color = Color(0xFF9B93AE),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) {
                    PurplePrimary.copy(alpha = 0.18f)
                } else {
                    Color(0xFF1A1A1E)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) {
                        PurpleAccent.copy(alpha = 0.18f)
                    } else {
                        Color(0xFF232326)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currency.currencySymbol,
                color = if (isSelected) PurpleAccent else Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.countryName,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = currency.currencyName,
                color = Color(0xFF9B93AE),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = "Selected",
                color = PurpleAccent,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
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
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Date Format",
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose the date style you want to see across the app.",
                color = Color(0xFF968EA8),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = supportedDateFormats,
                    key = { option -> option.pattern }
                ) { option ->
                    DateFormatPickerRow(
                        option = option,
                        isSelected = option.pattern == selectedPattern,
                        onClick = { onFormatSelected(option.pattern) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun DateFormatPickerRow(
    option: DateFormatOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) {
                    PurplePrimary.copy(alpha = 0.18f)
                } else {
                    Color(0xFF1A1A1E)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) {
                        PurpleAccent.copy(alpha = 0.18f)
                    } else {
                        Color(0xFF232326)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = option.pattern,
                tint = if (isSelected) PurpleAccent else Color(0xFFF0EBF7),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.previewLabel,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = option.pattern,
                color = Color(0xFF9B93AE),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = "Selected",
                color = PurpleAccent,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
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
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Time Format",
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose whether time is shown in 12-hour or 24-hour style.",
                color = Color(0xFF968EA8),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = supportedTimeFormats,
                    key = { option -> option.id }
                ) { option ->
                    TimeFormatPickerRow(
                        option = option,
                        isSelected = option.id == selectedTimeFormat,
                        onClick = { onFormatSelected(option.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun TimeFormatPickerRow(
    option: TimeFormatOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) {
                    PurplePrimary.copy(alpha = 0.18f)
                } else {
                    Color(0xFF1A1A1E)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) {
                        PurpleAccent.copy(alpha = 0.18f)
                    } else {
                        Color(0xFF232326)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = option.label,
                tint = if (isSelected) PurpleAccent else Color(0xFFF0EBF7),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = option.previewLabel,
                color = Color(0xFF9B93AE),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = "Selected",
                color = PurpleAccent,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

