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
import com.mknlabs.expensetracker.ui.components.SettingsGroupHeader
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import androidx.hilt.navigation.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    onBackClick: () -> Unit = {}
) {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()

    // Local state — initialized once from settings, then owned locally.
    val isInPreview = LocalInspectionMode.current
    var localSettings by remember { mutableStateOf(settings) }

    // Debounced persistence: write to DataStore 300ms after the last toggle.
    LaunchedEffect(localSettings) {
        delay(300)
        onSettingsChange(localSettings)
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

    val toggleItems = remember(localSettings, incomeExpenseTitle, incomeExpenseSubtitle, showDateTitle, showDateSubtitle, showCategoryIconTitle, showCategoryIconSubtitle, showTimeTitle, showTimeSubtitle, showCategoryTitle, showCategorySubtitle, showPaymentMethodTitle, showPaymentMethodSubtitle, showDateSeparatorsTitle, showDateSeparatorsSubtitle) {
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
            )
        )
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

            if (localSettings.showDateSeparators) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    previewGroups.forEach { group ->
                        Text(
                            text = group.key,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.6.sp
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        group.value.forEach { transaction ->
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
                                paymentType = getPaymentTypeName(transaction.paymentTypeId),
                                categoryLabel = stringResource(id = R.string.label_category_1),
                                showTypeLabel = localSettings.showIncomeExpenseLabels,
                                showTransactionDate = localSettings.showTransactionDate,
                                showPaymentMethod = localSettings.showPaymentMethod,
                                showTransactionTime = localSettings.showTransactionTime,
                                showCategoryIcon = localSettings.showCategoryIcon,
                                showCategoryLabel = localSettings.showCategoryLabel
                            )
                        }
                    }
                }
            }
            else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    previewTransactions.forEach { transaction ->
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
                            paymentType = getPaymentTypeName(transaction.paymentTypeId),
                            categoryLabel = stringResource(id = R.string.label_category_1),
                            showTypeLabel = localSettings.showIncomeExpenseLabels,
                            showTransactionDate = localSettings.showTransactionDate,
                            showPaymentMethod = localSettings.showPaymentMethod,
                            showTransactionTime = localSettings.showTransactionTime,
                            showCategoryIcon = localSettings.showCategoryIcon,
                            showCategoryLabel = localSettings.showCategoryLabel
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
                        ToggleSettingsItem(
                            item = item,
                            isInPreview = isInPreview,
                            standalone = false
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
                        ToggleSettingsItem(
                            item = item,
                            isInPreview = isInPreview,
                            standalone = false
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
                        ToggleSettingsItem(
                            item = item,
                            isInPreview = isInPreview,
                            standalone = false
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
private fun ToggleSettingsItem(
    item: TransactionCardToggleItem,
    isInPreview: Boolean,
    standalone: Boolean = true
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
            standalone = standalone
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
                standalone = standalone
            )
        }
    }
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
        TransactionCardCustomizeScreen(
            settings = TransactionCardCustomizationSettings()
        )
    }
}

