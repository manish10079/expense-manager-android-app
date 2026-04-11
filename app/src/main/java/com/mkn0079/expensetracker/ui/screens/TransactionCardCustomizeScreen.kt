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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.ui.components.TransactionCard
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.utils.formatAmount
import com.mkn0079.expensetracker.utils.formatDate
import com.mkn0079.expensetracker.utils.formatTime
import com.mkn0079.expensetracker.utils.getPaymentTypeName

private data class TransactionCardToggleItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

@Composable
fun TransactionCardCustomizeScreen(
    settings: TransactionCardCustomizationSettings,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    previewTransactions: List<Transaction> = transactionList.take(3),
    onSettingsChange: (TransactionCardCustomizationSettings) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val toggleItems = remember(settings, onSettingsChange) {
        listOf(
            TransactionCardToggleItem(
                title = "Income/Expense labels",
                subtitle = "Toggle visibility of transaction tags",
                icon = Icons.Outlined.Style,
                checked = settings.showIncomeExpenseLabels,
                onCheckedChange = { onSettingsChange(settings.copy(showIncomeExpenseLabels = it)) }
            ),
            TransactionCardToggleItem(
                title = "Show Transaction Date",
                subtitle = "Display the transaction date",
                icon = Icons.Outlined.DateRange,
                checked = settings.showTransactionDate,
                onCheckedChange = { onSettingsChange(settings.copy(showTransactionDate = it)) }
            ),
            TransactionCardToggleItem(
                title = "Show Payment Method",
                subtitle = "Display wallet or card used",
                icon = Icons.Outlined.Wallet,
                checked = settings.showPaymentMethod,
                onCheckedChange = { onSettingsChange(settings.copy(showPaymentMethod = it)) }
            ),
            TransactionCardToggleItem(
                title = "Show Transaction Time",
                subtitle = "Exact timestamp visibility",
                icon = Icons.Outlined.Schedule,
                checked = settings.showTransactionTime,
                onCheckedChange = { onSettingsChange(settings.copy(showTransactionTime = it)) }
            ),
            TransactionCardToggleItem(
                title = "Show Category Icon",
                subtitle = "Visual category indicators",
                icon = Icons.Outlined.Paid,
                checked = settings.showCategoryIcon,
                onCheckedChange = { onSettingsChange(settings.copy(showCategoryIcon = it)) }
            ),
            TransactionCardToggleItem(
                title = "Show Date Separators",
                subtitle = "Group transactions by day",
                icon = Icons.Outlined.DateRange,
                checked = settings.showDateSeparators,
                onCheckedChange = { onSettingsChange(settings.copy(showDateSeparators = it)) }
            )
        )
    }

    val previewGroups = remember(previewTransactions) {
        previewTransactions.groupBy { formatDate(it.createdAt, dateFormatPattern) }.entries.toList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundDark, Color(0xFF0C0B10), BackgroundDark)
                )
            )
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
                        .background(Color(0xFF18181A))
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = "Back",
                        tint = PurpleAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Card Settings",
                    color = PurpleAccent,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                )
            }
        }

        item {
            Text(
                text = "LIVE PREVIEW",
                color = Color(0xFF7C7488),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.8.sp,
                    fontSize = 11.sp
                )
            )
        }

        if (settings.showDateSeparators) {
            previewGroups.forEach { group ->
                item(key = "preview_header_${group.key}") {
                    Text(
                        text = group.key,
                        color = Color(0xFF8F889D),
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
                            currencyId = currencyId
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
                                currencyId = currencyId
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Interface",
                    color = Color(0xFFF1EBF6),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                )

                Text(
                    text = "Customize how your data is visualized",
                    color = Color(0xFFA59EB1),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                toggleItems.forEach { item ->
                    TransactionCardToggleRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun TransactionCardToggleRow(
    item: TransactionCardToggleItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF232223))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B1A1C)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color(0xFFD9CEF7),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = Color(0xFFF2EDF8),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subtitle,
                color = Color(0xFFA099AC),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Switch(
            checked = item.checked,
            onCheckedChange = item.onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF2E0E68),
                checkedTrackColor = Color(0xFF9D72FF),
                uncheckedThumbColor = Color(0xFFD8D0E7),
                uncheckedTrackColor = Color(0xFF434044),
                uncheckedBorderColor = Color(0xFF434044)
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun TransactionCardCustomizeScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        TransactionCardCustomizeScreen(
            settings = TransactionCardCustomizationSettings()
        )
    }
}
