package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mknlabs.expensetracker.ui.theme.IncomeGreen
import com.mknlabs.expensetracker.ui.theme.ExpenseRed
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import com.mknlabs.expensetracker.ui.components.TransactionCard
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatAmount
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.getPaymentTypeName
import com.mknlabs.expensetracker.ui.components.GatedAction
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.AccessStatus

import com.mknlabs.expensetracker.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.ui.components.SettingsGroup
import com.mknlabs.expensetracker.ui.components.SettingsGroupDivider

import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
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

@Composable
fun TransactionCardCustomizeScreen(
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

    val incomeExpenseTitle = stringResource(id = R.string.title_incomeexpense_labels)
    val incomeExpenseSubtitle = stringResource(id = R.string.title_toggle_visibility_of_transacti)
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

    val toggleItems = remember(localSettings, incomeExpenseTitle, incomeExpenseSubtitle, showDateTitle, showDateSubtitle, showCategoryIconTitle, showCategoryIconSubtitle, showTimeTitle, showTimeSubtitle, showCategoryTitle, showCategorySubtitle, showPaymentMethodTitle, showPaymentMethodSubtitle, showDateSeparatorsTitle, showDateSeparatorsSubtitle, showListSummariesTitle, showListSummariesSubtitle) {
        listOf(
            TransactionCardToggleItem(
                title = incomeExpenseTitle,
                subtitle = incomeExpenseSubtitle,
                icon = Icons.Outlined.Style,
                optionId = "showIncomeExpenseLabels",
                checked = localSettings.showIncomeExpenseLabels,
                onCheckedChange = { localSettings = localSettings.copy(showIncomeExpenseLabels = it) }
            ),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Fixed Top Section: Header and Preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppHeader(
                title = stringResource(id = R.string.title_transaction_card_settings),
                onBackClick = onBackClick,
                modifier = Modifier.padding(top = 10.dp)
            )

            Text(
                text = stringResource(id = R.string.label_preview),
                color = MaterialTheme.colorScheme.primary,
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

            Text(
                text = stringResource(id = R.string.title_customize_transaction_card),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = Dimens.PaddingMedium, bottom = Dimens.PaddingMedium)
            )
        }

        // Scrollable Bottom Section: Customization Toggles
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Group 1: Visual Style
            item {
                SettingsGroup {
                    val groupItems = toggleItems.filter { it.optionId in listOf("showIncomeExpenseLabels", "showCategoryIcon") }
                    groupItems.forEachIndexed { index, item ->
                        false.ToggleSettingsItem(
                            item = item,
                            isInPreview = isInPreview
                        )
                        if (index < groupItems.size - 1) SettingsGroupDivider()
                    }
                }
            }

            // Group 2: Transaction Details
            item {
                SettingsGroup {
                    val groupItems = toggleItems.filter { it.optionId in listOf("showCategoryLabel", "showPaymentMethod", "showTransactionTime") }
                    groupItems.forEachIndexed { index, item ->
                        false.ToggleSettingsItem(
                            item = item,
                            isInPreview = isInPreview
                        )
                        if (index < groupItems.size - 1) SettingsGroupDivider()
                    }
                }
            }

            // Group 3: Time & Organization
            item {
                SettingsGroup {
                    val groupItems = toggleItems.filter { it.optionId in listOf("showTransactionDate", "showDateSeparators") }
                    groupItems.forEachIndexed { index, item ->
                        false.ToggleSettingsItem(
                            item = item,
                            isInPreview = isInPreview
                        )
                        if (index < groupItems.size - 1) SettingsGroupDivider()
                    }
                }
            }

            // Group 4: Transaction List Summaries
            item {
                SettingsGroup {
                    val groupItems = toggleItems.filter { it.optionId in listOf("showTransactionListSummaries") }
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
        paymentType = getPaymentTypeName(transaction.paymentTypeId).uppercase(),
        categoryLabel = stringResource(id = R.string.label_category_1).uppercase(),
        showTypeLabel = settings.showIncomeExpenseLabels,
        showTransactionDate = settings.showTransactionDate,
        showPaymentMethod = settings.showPaymentMethod,
        showTransactionTime = settings.showTransactionTime,
        showCategoryIcon = settings.showCategoryIcon,
        showCategoryLabel = settings.showCategoryLabel,
        showNoteTooltip = isProUser
    )
}

@Preview(
    name = "Card Settings Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun TransactionCardCustomizeScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
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
                        color = MaterialTheme.colorScheme.primary,
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
                        tint = IncomeGreen,
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
                        tint = ExpenseRed,
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

