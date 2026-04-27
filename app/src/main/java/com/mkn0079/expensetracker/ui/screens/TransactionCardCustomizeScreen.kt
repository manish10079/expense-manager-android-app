package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.ui.components.TransactionCard
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.utils.formatAmount
import com.mkn0079.expensetracker.utils.formatDate
import com.mkn0079.expensetracker.utils.formatTime
import com.mkn0079.expensetracker.utils.getPaymentTypeName
import com.mkn0079.expensetracker.ui.components.GatedAction
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.AccessStatus

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
    previewTransactions: List<Transaction> = transactionList.take(3),
    onSettingsChange: (TransactionCardCustomizationSettings) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    // Local state — initialized once from settings, then owned locally.
    // No key on remember: avoids DataStore round-trip overriding user's uncommitted changes.
    var localSettings by remember { mutableStateOf(settings) }

    // Debounced persistence: write to DataStore 300ms after the last toggle.
    // No early-return guard — DataStore is idempotent, and skipping writes caused
    // "toggle not responding" when the user toggled back within the debounce window.
    LaunchedEffect(localSettings) {
        delay(300)
        onSettingsChange(localSettings)
    }

    val toggleItems = remember(localSettings) {
        listOf(
            TransactionCardToggleItem(
                title = "Income/Expense labels",
                subtitle = "Toggle visibility of transaction tags",
                icon = Icons.Outlined.Style,
                optionId = "showIncomeExpenseLabels",
                checked = localSettings.showIncomeExpenseLabels,
                onCheckedChange = { localSettings = localSettings.copy(showIncomeExpenseLabels = it) }
            ),
            TransactionCardToggleItem(
                title = "Show Transaction Date",
                subtitle = "Display the transaction date",
                icon = Icons.Outlined.DateRange,
                optionId = "showTransactionDate",
                checked = localSettings.showTransactionDate,
                onCheckedChange = { localSettings = localSettings.copy(showTransactionDate = it) }
            ),
            TransactionCardToggleItem(
                title = "Show Payment Method",
                subtitle = "Display wallet or card used",
                icon = Icons.Outlined.Wallet,
                optionId = "showPaymentMethod",
                checked = localSettings.showPaymentMethod,
                onCheckedChange = { localSettings = localSettings.copy(showPaymentMethod = it) }
            ),
            TransactionCardToggleItem(
                title = "Show Transaction Time",
                subtitle = "Exact timestamp visibility",
                icon = Icons.Outlined.Schedule,
                optionId = "showTransactionTime",
                checked = localSettings.showTransactionTime,
                onCheckedChange = { localSettings = localSettings.copy(showTransactionTime = it) }
            ),
            TransactionCardToggleItem(
                title = "Show Category Icon",
                subtitle = "Visual category indicators",
                icon = Icons.Outlined.Paid,
                optionId = "showCategoryIcon",
                checked = localSettings.showCategoryIcon,
                onCheckedChange = { localSettings = localSettings.copy(showCategoryIcon = it) }
            ),
            TransactionCardToggleItem(
                title = "Show Date Separators",
                subtitle = "Group transactions by day",
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 22.dp, top = 12.dp, end = 22.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Card Settings",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                )
            }
        }


        if (localSettings.showDateSeparators) {
            previewGroups.forEach { group ->
                item(key = "preview_header_${group.key}") {
                    Text(
                        text = group.key,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(group.value.size) { index ->
                    val transaction = group.value[index]
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
                        showTypeLabel = settings.showIncomeExpenseLabels,
                        showTransactionDate = settings.showTransactionDate,
                        showPaymentMethod = settings.showPaymentMethod,
                        showTransactionTime = settings.showTransactionTime,
                        showCategoryIcon = settings.showCategoryIcon
                    )
                }
            }
        } else {
            item {
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
                            showTypeLabel = settings.showIncomeExpenseLabels,
                            showTransactionDate = settings.showTransactionDate,
                            showPaymentMethod = settings.showPaymentMethod,
                            showTransactionTime = settings.showTransactionTime,
                            showCategoryIcon = settings.showCategoryIcon
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Customize how your data is visualized",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                toggleItems.forEach { item ->
                    GatedAction(
                        feature = Feature.CARD_CUSTOMIZATION,
                        optionId = item.optionId,
                        displayName = item.title,
                        onAction = { item.onCheckedChange(!item.checked) }
                    ) { status, onClick ->
                        TransactionCardToggleRow(
                            item = item,
                            isLocked = status !is AccessStatus.Granted,
                            onClick = onClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCardToggleRow(
    item: TransactionCardToggleItem,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLocked) Icons.Filled.Lock else item.icon,
                contentDescription = item.title,
                tint = if (isLocked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }

        Switch(
            checked = item.checked,
            onCheckedChange = { onClick() },
            enabled = !isLocked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.secondary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionCardCustomizeScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        TransactionCardCustomizeScreen(
            settings = TransactionCardCustomizationSettings()
        )
    }
}
