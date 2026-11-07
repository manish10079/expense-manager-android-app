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
fun CustomisationScreen(
    settings: TransactionCardCustomizationSettings,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    previewTransactions: List<Transaction> = transactionList.take(2),
    onSettingsChange: (TransactionCardCustomizationSettings) -> Unit = {},
    onBackClick: () -> Unit = {},
    isAdsEnabled: Boolean = false,
    isProUser: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appSettings by AppSettingsDataStore.getAppSettingsFlow(context)
        .collectAsStateWithLifecycle(initialValue = defaultAppSettings)
    val defaultPeriodFilter = transactionPeriodFilterOrDefault(appSettings.defaultTransactionPeriodFilter)
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val proTimeStatus by monetizationViewModel
        .getAccessStatus(Feature.CARD_CUSTOMIZATION, "showTransactionTime")
        .collectAsStateWithLifecycle()
    val proDateSeparatorsStatus by monetizationViewModel
        .getAccessStatus(Feature.CARD_CUSTOMIZATION, "showDateSeparators")
        .collectAsStateWithLifecycle()
    val proPaymentMethodStatus by monetizationViewModel
        .getAccessStatus(Feature.CARD_CUSTOMIZATION, "showPaymentMethod")
        .collectAsStateWithLifecycle()
    val proListSummariesStatus by monetizationViewModel
        .getAccessStatus(Feature.CARD_CUSTOMIZATION, "showTransactionListSummaries")
        .collectAsStateWithLifecycle()

    TransactionCardCustomizeContent(
        settings = settings,
        currencyId = currencyId,
        amountFormatPreferences = amountFormatPreferences,
        dateFormatPattern = dateFormatPattern,
        timeFormat = timeFormat,
        previewTransactions = previewTransactions,
        isAdsEnabled = isAdsEnabled,
        isProUser = isProUser,
        isTransactionTimeProGranted = proTimeStatus is AccessStatus.Granted,
        isDateSeparatorsProGranted = proDateSeparatorsStatus is AccessStatus.Granted,
        isPaymentMethodProGranted = proPaymentMethodStatus is AccessStatus.Granted,
        isListSummariesProGranted = proListSummariesStatus is AccessStatus.Granted,
        defaultPeriodFilter = defaultPeriodFilter,
        onDefaultPeriodFilterChange = { filter ->
            coroutineScope.launch {
                AppSettingsDataStore.updateAppSettings(context) { settings ->
                    settings.copy(defaultTransactionPeriodFilter = filter.name)
                }
            }
        },
        addUiPattern = appSettings.addTransactionUiPattern,
        onAddUiPatternChange = { pattern ->
            coroutineScope.launch {
                AppSettingsDataStore.updateAppSettings(context) { settings ->
                    settings.copy(addTransactionUiPattern = pattern)
                }
            }
        },
        onSettingsChange = onSettingsChange,
        onBackClick = onBackClick
    )
}

@Composable
internal fun TransactionCardCustomizeContent(
    settings: TransactionCardCustomizationSettings,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    timeFormat: String,
    previewTransactions: List<Transaction>,
    isAdsEnabled: Boolean,
    isProUser: Boolean = false,
    isTransactionTimeProGranted: Boolean,
    isDateSeparatorsProGranted: Boolean,
    isPaymentMethodProGranted: Boolean,
    isListSummariesProGranted: Boolean,
    defaultPeriodFilter: TransactionPeriodFilter = TransactionPeriodFilter.MONTHLY,
    onDefaultPeriodFilterChange: (TransactionPeriodFilter) -> Unit = {},
    addUiPattern: AddTransactionUiPattern = AddTransactionUiPattern.UNIFIED_SMART_SHEET,
    onAddUiPatternChange: (AddTransactionUiPattern) -> Unit = {},
    onSettingsChange: (TransactionCardCustomizationSettings) -> Unit,
    onBackClick: () -> Unit
) {
    // Local state — initialized once from settings, then owned locally.
    val isInPreview = LocalInspectionMode.current
    var localSettings by remember { mutableStateOf(settings) }

    // Debounced persistence: write to DataStore 300ms after the last toggle.
    LaunchedEffect(localSettings) {
        delay(300.milliseconds)
        onSettingsChange(localSettings)
    }

    // Pro-gated toggles must be reset to OFF for non-Pro users.
    // The Route layer observes access status and passes plain granted flags down.
    LaunchedEffect(
        isProUser,
        isTransactionTimeProGranted,
        isDateSeparatorsProGranted,
        isPaymentMethodProGranted,
        isListSummariesProGranted
    ) {
        var next = localSettings
        if (isProUser) {
            next = next.copy(
                showCategoryLabel = true,
                showPaymentMethod = true
            )
        }
        if (!isTransactionTimeProGranted && next.showTransactionTime) {
            next = next.copy(showTransactionTime = false)
        }
        if (!isDateSeparatorsProGranted && next.showDateSeparators) {
            next = next.copy(showDateSeparators = false)
        }
        if (!isPaymentMethodProGranted && next.showPaymentMethod) {
            next = next.copy(showPaymentMethod = false)
        }
        if (!isListSummariesProGranted && next.showTransactionListSummaries) {
            next = next.copy(showTransactionListSummaries = false)
        }
        if (next != localSettings) {
            localSettings = next
        }
    }

    val showDateTitle = stringResource(id = R.string.title_show_transaction_date)
    val showDateSubtitle = stringResource(id = R.string.title_display_the_transaction_date)
    val showCategoryIconTitle = stringResource(id = R.string.title_show_category_icon)
    val showCategoryIconSubtitle = stringResource(id = R.string.title_visual_category_indicators)
    val showTimeTitle = stringResource(id = R.string.title_show_transaction_time)
    val showTimeSubtitle = stringResource(id = R.string.title_exact_timestamp_visibility)
    val showCategoryTitle = stringResource(id = R.string.title_show_category)
    val showCategorySubtitle = stringResource(id = R.string.desc_display_category_label)
    val showPaymentMethodTitle = stringResource(id = R.string.title_show_payment_method)
    val showPaymentMethodSubtitle = stringResource(id = R.string.desc_display_wallet_or_card)
    val showDateSeparatorsTitle = stringResource(id = R.string.title_show_date_separators)
    val showDateSeparatorsSubtitle = stringResource(id = R.string.desc_group_transactions_by_day)
    val showListSummariesTitle = stringResource(id = R.string.title_show_list_summaries)
    val showListSummariesSubtitle = stringResource(id = R.string.desc_show_list_summaries)

    val toggleItems = remember(localSettings, showDateTitle, showDateSubtitle, showCategoryIconTitle, showCategoryIconSubtitle, showTimeTitle, showTimeSubtitle, showCategoryTitle, showCategorySubtitle, showPaymentMethodTitle, showPaymentMethodSubtitle, showDateSeparatorsTitle, showDateSeparatorsSubtitle, showListSummariesTitle, showListSummariesSubtitle) {
        listOf(
            TransactionCardToggleItem(
                title = showDateTitle,
                subtitle = showDateSubtitle,
                icon = PhosphorIcons.Regular.CalendarBlank,
                iconTile = SettingsTiles.Sky,
                optionId = "showTransactionDate",
                checked = localSettings.showTransactionDate,
                onCheckedChange = { localSettings = localSettings.copy(showTransactionDate = it) }
            ),
            TransactionCardToggleItem(
                title = showCategoryIconTitle,
                subtitle = showCategoryIconSubtitle,
                icon = PhosphorIcons.Regular.Tag,
                iconTile = SettingsTiles.Emerald,
                optionId = "showCategoryIcon",
                checked = localSettings.showCategoryIcon,
                onCheckedChange = { localSettings = localSettings.copy(showCategoryIcon = it) }
            ),
            TransactionCardToggleItem(
                title = showTimeTitle,
                subtitle = showTimeSubtitle,
                icon = PhosphorIcons.Regular.Clock,
                iconTile = SettingsTiles.Indigo,
                optionId = "showTransactionTime",
                checked = localSettings.showTransactionTime,
                onCheckedChange = { localSettings = localSettings.copy(showTransactionTime = it) }
            ),
            TransactionCardToggleItem(
                title = showCategoryTitle,
                subtitle = showCategorySubtitle,
                icon = PhosphorIcons.Regular.TextAa,
                iconTile = SettingsTiles.Fuchsia,
                optionId = "showCategoryLabel",
                checked = localSettings.showCategoryLabel,
                onCheckedChange = { localSettings = localSettings.copy(showCategoryLabel = it) }
            ),
            TransactionCardToggleItem(
                title = showPaymentMethodTitle,
                subtitle = showPaymentMethodSubtitle,
                icon = PhosphorIcons.Regular.Wallet,
                iconTile = SettingsTiles.Amber,
                optionId = "showPaymentMethod",
                checked = localSettings.showPaymentMethod,
                onCheckedChange = { localSettings = localSettings.copy(showPaymentMethod = it) }
            ),
            TransactionCardToggleItem(
                title = showDateSeparatorsTitle,
                subtitle = showDateSeparatorsSubtitle,
                icon = PhosphorIcons.Regular.CalendarDots,
                iconTile = SettingsTiles.Teal,
                optionId = "showDateSeparators",
                checked = localSettings.showDateSeparators,
                onCheckedChange = { localSettings = localSettings.copy(showDateSeparators = it) }
            ),
            TransactionCardToggleItem(
                title = showListSummariesTitle,
                subtitle = showListSummariesSubtitle,
                icon = PhosphorIcons.Regular.ListChecks,
                iconTile = SettingsTiles.Violet,
                optionId = "showTransactionListSummaries",
                checked = localSettings.showTransactionListSummaries,
                onCheckedChange = { localSettings = localSettings.copy(showTransactionListSummaries = it) }
            )
        )
    }

    val previewTotalIncome = remember(previewTransactions, currencyId, amountFormatPreferences) {
        val amount = previewTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
        com.mknlabs.expensetracker.utils.formatCurrencyValue(amount, currencyId, amountFormatPreferences)
    }
    val previewTotalExpense = remember(previewTransactions, currencyId, amountFormatPreferences) {
        val amount = previewTransactions.filter { it.transactionTypeId != 1 }.sumOf { it.amount }
        com.mknlabs.expensetracker.utils.formatCurrencyValue(amount, currencyId, amountFormatPreferences)
    }

    val previewGroups = remember(previewTransactions) {
        previewTransactions.groupBy { formatDate(it.createdAt, dateFormatPattern) }.entries.toList()
    }

    // Adaptive layout: stacked on compact portrait; two columns (preview left,
    // toggles right) on landscape, tablets, and other wide screens.
    val isTwoPane = !LocalAppWindowInfo.current.isCompactPortrait

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (isTwoPane) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                // LEFT: header + live preview
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(end = 16.dp)
                ) {
                    val enter = rememberSectionEnterAlphas(2)
                    AppHeader(
                        title = stringResource(id = R.string.title_transaction_card_settings),
                        onBackClick = onBackClick,
                        modifier = Modifier.alpha(enter[0])
                    )
                    Box(Modifier.alpha(enter[1])) {
                    TransactionCardPreviewSection(
                        localSettings = localSettings,
                        previewTransactions = previewTransactions,
                        previewTotalIncome = previewTotalIncome,
                        previewTotalExpense = previewTotalExpense,
                        previewGroups = previewGroups,
                        currencyId = currencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = dateFormatPattern,
                        timeFormat = timeFormat,
                        isProUser = isProUser
                    )
                    }
                }

                // RIGHT: customize toggles + ad
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    TransactionCardTogglesList(
                        toggleItems = toggleItems,
                        isInPreview = isInPreview,
                        defaultPeriodFilter = defaultPeriodFilter,
                        onDefaultPeriodFilterChange = onDefaultPeriodFilterChange,
                        addUiPattern = addUiPattern,
                        onAddUiPatternChange = onAddUiPatternChange,
                        modifier = Modifier.weight(1f),
                        horizontalPadding = 0.dp
                    )
                    AdContainer(
                        isAdsEnabled = isAdsEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.PaddingMedium, bottom = 8.dp)
                    ) {
                        NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                    }
                }
            }
        } else {
            // Fixed Top Section: Header and Preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                AppHeader(
                    title = stringResource(id = R.string.title_transaction_card_settings),
                    onBackClick = onBackClick,
                    modifier = Modifier
                )

                TransactionCardPreviewSection(
                    localSettings = localSettings,
                    previewTransactions = previewTransactions,
                    previewTotalIncome = previewTotalIncome,
                    previewTotalExpense = previewTotalExpense,
                    previewGroups = previewGroups,
                    currencyId = currencyId,
                    amountFormatPreferences = amountFormatPreferences,
                    dateFormatPattern = dateFormatPattern,
                    timeFormat = timeFormat,
                    isProUser = isProUser
                )
            }

            // Scrollable Bottom Section: Customization Toggles
            TransactionCardTogglesList(
                toggleItems = toggleItems,
                isInPreview = isInPreview,
                defaultPeriodFilter = defaultPeriodFilter,
                onDefaultPeriodFilterChange = onDefaultPeriodFilterChange,
                addUiPattern = addUiPattern,
                onAddUiPatternChange = onAddUiPatternChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalPadding = 20.dp
            )

            // Fixed Native Ad at the bottom
            AdContainer(
                isAdsEnabled = isAdsEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = Dimens.PaddingMedium, bottom = 8.dp)
            ) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
            }
        }
    }
}


