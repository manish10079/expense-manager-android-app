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
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
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
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.transactionList
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
import com.mknlabs.expensetracker.core.ui.components.SettingsGroup
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupHeader

import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.milliseconds

private data class TransactionCardToggleItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val checked: Boolean,
    val optionId: String,
    val onCheckedChange: (Boolean) -> Unit
)

/**
 * The two scopes the customize toggles are grouped by, each an explicit ordered id list.
 *
 * The split is by scope rather than by theme: the first five change what a single
 * transaction card shows, the last two change how the list around those cards is arranged.
 * The order a group renders in is the order written here, not the order the toggle pool
 * happens to declare, so adding a toggle to the pool cannot silently reshuffle a group.
 */
private val transactionCardToggleIds = listOf(
    "showCategoryIcon",
    "showCategoryLabel",
    "showPaymentMethod",
    "showTransactionDate",
    "showTransactionTime"
)

private val listLevelToggleIds = listOf(
    "showDateSeparators",
    "showTransactionListSummaries"
)

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
        onSettingsChange = onSettingsChange,
        onBackClick = onBackClick
    )
}

@Composable
private fun TransactionCardCustomizeContent(
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
    LaunchedEffect(isTransactionTimeProGranted, isDateSeparatorsProGranted, isPaymentMethodProGranted, isListSummariesProGranted) {
        if (!isTransactionTimeProGranted && localSettings.showTransactionTime) {
            localSettings = localSettings.copy(showTransactionTime = false)
        }
        if (!isDateSeparatorsProGranted && localSettings.showDateSeparators) {
            localSettings = localSettings.copy(showDateSeparators = false)
        }
        if (!isPaymentMethodProGranted && localSettings.showPaymentMethod) {
            localSettings = localSettings.copy(showPaymentMethod = false)
        }
        if (!isListSummariesProGranted && localSettings.showTransactionListSummaries) {
            localSettings = localSettings.copy(showTransactionListSummaries = false)
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
                icon = Icons.Outlined.DateRange,
                optionId = "showTransactionDate",
                checked = localSettings.showTransactionDate,
                onCheckedChange = { localSettings = localSettings.copy(showTransactionDate = it) }
            ),
            TransactionCardToggleItem(
                title = showCategoryIconTitle,
                subtitle = showCategoryIconSubtitle,
                icon = Icons.Outlined.Paid,
                optionId = "showCategoryIcon",
                checked = localSettings.showCategoryIcon,
                onCheckedChange = { localSettings = localSettings.copy(showCategoryIcon = it) }
            ),
            TransactionCardToggleItem(
                title = showTimeTitle,
                subtitle = showTimeSubtitle,
                icon = Icons.Outlined.Schedule,
                optionId = "showTransactionTime",
                checked = localSettings.showTransactionTime,
                onCheckedChange = { localSettings = localSettings.copy(showTransactionTime = it) }
            ),
            TransactionCardToggleItem(
                title = showCategoryTitle,
                subtitle = showCategorySubtitle,
                icon = Icons.Outlined.Paid,
                optionId = "showCategoryLabel",
                checked = localSettings.showCategoryLabel,
                onCheckedChange = { localSettings = localSettings.copy(showCategoryLabel = it) }
            ),
            TransactionCardToggleItem(
                title = showPaymentMethodTitle,
                subtitle = showPaymentMethodSubtitle,
                icon = Icons.Outlined.Wallet,
                optionId = "showPaymentMethod",
                checked = localSettings.showPaymentMethod,
                onCheckedChange = { localSettings = localSettings.copy(showPaymentMethod = it) }
            ),
            TransactionCardToggleItem(
                title = showDateSeparatorsTitle,
                subtitle = showDateSeparatorsSubtitle,
                icon = Icons.Outlined.DateRange,
                optionId = "showDateSeparators",
                checked = localSettings.showDateSeparators,
                onCheckedChange = { localSettings = localSettings.copy(showDateSeparators = it) }
            ),
            TransactionCardToggleItem(
                title = showListSummariesTitle,
                subtitle = showListSummariesSubtitle,
                icon = Icons.Outlined.Summarize,
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
            .background(MaterialTheme.colorScheme.background)
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
                    Text(
                        text = stringResource(id = R.string.title_customize_transaction_card),
                        color = MaterialTheme.colorScheme.accentInk,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(top = Dimens.PaddingMedium, bottom = Dimens.PaddingMedium)
                    )
                    TransactionCardTogglesList(
                        toggleItems = toggleItems,
                        isInPreview = isInPreview,
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

                Text(
                    text = stringResource(id = R.string.title_customize_transaction_card),
                    color = MaterialTheme.colorScheme.accentInk,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = Dimens.PaddingMedium, bottom = Dimens.PaddingMedium)
                )
            }

            // Scrollable Bottom Section: Customization Toggles
            TransactionCardTogglesList(
                toggleItems = toggleItems,
                isInPreview = isInPreview,
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

@Composable
private fun TransactionCardPreviewSection(
    localSettings: TransactionCardCustomizationSettings,
    previewTransactions: List<Transaction>,
    previewTotalIncome: String,
    previewTotalExpense: String,
    previewGroups: List<Map.Entry<String, List<Transaction>>>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    timeFormat: String,
    isProUser: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(id = R.string.label_preview),
            color = MaterialTheme.colorScheme.accentInk,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp
            )
        )

        if (localSettings.showTransactionListSummaries) {
            PreviewTransactionSummaryCard(
                income = previewTotalIncome,
                expense = previewTotalExpense
            )
        }

        if (localSettings.showDateSeparators) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                previewGroups.forEach { group ->
                    PreviewDateHeader(
                        dayLabel = if (previewGroups.indexOf(group) == 0) "Today" else "Yesterday",
                        dateLabel = group.key
                    )
                    group.value.forEach { transaction ->
                        PreviewTransactionCard(
                            transaction = transaction,
                            settings = localSettings,
                            currencyId = currencyId,
                            amountFormatPreferences = amountFormatPreferences,
                            dateFormatPattern = dateFormatPattern,
                            timeFormat = timeFormat,
                            isProUser = isProUser
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                previewTransactions.forEach { transaction ->
                    PreviewTransactionCard(
                        transaction = transaction,
                        settings = localSettings,
                        currencyId = currencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        dateFormatPattern = dateFormatPattern,
                        timeFormat = timeFormat,
                        isProUser = isProUser
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionCardTogglesList(
    toggleItems: List<TransactionCardToggleItem>,
    isInPreview: Boolean,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Group 1: what every transaction card displays, ordered to mirror the card's own
        // anatomy -- icon, category, payment method, then the date and time stamps.
        item {
            SettingsGroupHeader(
                title = stringResource(id = R.string.label_on_each_transaction_card),
                subtitle = stringResource(id = R.string.desc_on_each_transaction_card)
            )
        }
        item {
            SettingsGroup {
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
            SettingsGroup {
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
}

@Composable
private fun Boolean.ToggleSettingsItem(
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

@Composable
private fun PreviewTransactionCard(
    transaction: Transaction,
    settings: TransactionCardCustomizationSettings,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    timeFormat: String,
    isProUser: Boolean = false
) {
    TransactionCard(
        note = transaction.note,
        transactionDate = formatDate(transaction.createdAt, dateFormatPattern),
        transactionTime = formatTime(transaction.createdAt, timeFormat),
        amount = formatAmount(
            amount = transaction.amount,
            transactionTypeId = transaction.transactionTypeId,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences
        ),
        transactionTypeId = transaction.transactionTypeId,
        icon = transaction.categoryIcon,
        categoryId = transaction.categoryId,
        paymentType = getPaymentTypeName(transaction.paymentTypeId).uppercase(),
        categoryLabel = stringResource(id = R.string.label_category_1).uppercase(),
        showTypeLabel = settings.showIncomeExpenseLabels,
        showTransactionDate = settings.showTransactionDate,
        showPaymentMethod = settings.showPaymentMethod,
        showTransactionTime = settings.showTransactionTime,
        showCategoryIcon = settings.showCategoryIcon,
        showCategoryLabel = settings.showCategoryLabel,
        showNoteTooltip = isProUser,
        isProUser = isProUser,
        isRecurring = !transaction.sourceRecurringRuleId.isNullOrBlank()
    )
}

// Smartphone pair: the same 412dp handset rendered in both themes so the screen can be
// reviewed against the light redesign without flipping the IDE's night mode. The uiMode
// annotation keeps the simulated system bars in step with the theme the content is given,
// and the handset window classes pin the run to the phone's single-column layout.

/** The 412dp portrait handset both smartphone previews are rendered in. */
private val PREVIEW_HANDSET_WINDOW_INFO =
    AppWindowInfo(AppWindowSize.Compact, AppWindowHeight.Expanded)

@Preview(
    name = "Card Settings - Smartphone Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun CustomisationScreenLightPreview() {
    TransactionCardCustomizePreviewContent(
        darkTheme = false,
        windowInfo = PREVIEW_HANDSET_WINDOW_INFO
    )
}

@Preview(
    name = "Card Settings - Smartphone Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CustomisationScreenDarkPreview() {
    TransactionCardCustomizePreviewContent(
        darkTheme = true,
        windowInfo = PREVIEW_HANDSET_WINDOW_INFO
    )
}

// Multi-config adaptive preview: verifies the stacked (compact) vs two-column
// (landscape/wide) layouts of the Card Settings screen (roadmap Milestone 5).
@Preview(name = "Card Settings - Multi-Config", showBackground = true)
@PreviewScreenSizes
@Composable
private fun CustomisationScreenMultiConfigPreview() {
    TransactionCardCustomizePreviewContent(darkTheme = true)
}

@Composable
private fun TransactionCardCustomizePreviewContent(
    darkTheme: Boolean,
    windowInfo: AppWindowInfo? = null
) {
    // The screen branches on LocalAppWindowInfo, which only the app root provides. A static
    // preview installs no provider, so it fell back to the local's default -- compact width
    // AND compact height, which reads as phone LANDSCAPE, not portrait -- and every preview,
    // the 412dp handset pair included, drew the two-column layout instead of the phone's
    // single column. A preview that knows its own frame passes the classes for it; one that
    // covers several frames (the multi-config preview) leaves this null and gets the classes
    // its current frame implies, so a wide frame still splits into two.
    val configuration = LocalConfiguration.current
    val frameWindowInfo = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        previewAppWindowInfo(
            widthDp = configuration.screenWidthDp,
            heightDp = configuration.screenHeightDp
        )
    }
    val resolvedWindowInfo = windowInfo ?: frameWindowInfo
    ExpenseTrackerTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalAppWindowInfo provides resolvedWindowInfo) {
            TransactionCardCustomizeContent(
                settings = TransactionCardCustomizationSettings(),
                currencyId = DEFAULT_CURRENCY_ID,
                amountFormatPreferences = defaultAmountFormatPreferences,
                dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
                timeFormat = DEFAULT_TIME_FORMAT,
                previewTransactions = transactionList.take(2),
                isAdsEnabled = false,
                isProUser = true,
                isTransactionTimeProGranted = true,
                isDateSeparatorsProGranted = true,
                isPaymentMethodProGranted = true,
                isListSummariesProGranted = true,
                onSettingsChange = {},
                onBackClick = {}
            )
        }
    }
}

/**
 * The size classes a preview frame implies, mapped at the same breakpoints
 * `rememberAppWindowInfo()` reads at runtime -- a preview has no real window to ask, only the
 * width and height its `@Preview` frame was given.
 */
private fun previewAppWindowInfo(widthDp: Int, heightDp: Int): AppWindowInfo = AppWindowInfo(
    width = when {
        widthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> AppWindowSize.ExtraLarge
        widthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> AppWindowSize.Large
        widthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> AppWindowSize.Expanded
        widthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> AppWindowSize.Medium
        else -> AppWindowSize.Compact
    },
    height = when {
        heightDp >= WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND -> AppWindowHeight.Expanded
        heightDp >= WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND -> AppWindowHeight.Medium
        else -> AppWindowHeight.Compact
    }
)

@Composable
private fun PreviewDateHeader(
    dayLabel: String,
    dateLabel: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dayLabel,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = dateLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PreviewTransactionSummaryCard(
    income: String,
    expense: String,
    periodLabel: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!periodLabel.isNullOrBlank()) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.accentInk,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }

                // Income
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.income,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.label_income) + ": " + income,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Expense
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.expense,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.label_expense) + ": " + expense,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )
        }
    }
}

