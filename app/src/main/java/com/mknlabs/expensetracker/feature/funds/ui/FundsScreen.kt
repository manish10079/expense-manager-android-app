package com.mknlabs.expensetracker.feature.funds.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Info
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.FundStatus
import com.mknlabs.expensetracker.models.FundWithProgress
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val fundDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

private val FUND_ICON_KEYS = listOf(
    "card_giftcard",
    "school",
    "attach_money",
    "volunteer_activism",
    "shopping_bag",
    "directions_car",
    "home",
    "favorite",
    "subscriptions",
    "security"
)

private val FUND_COLOR_HEXES = listOf(
    "#F59E0B",
    "#10B981",
    "#3B82F6",
    "#EF4444",
    "#8B5CF6",
    "#EC4899",
    "#14B8A6",
    "#F97316"
)

/** Parses an `#RRGGBB` string, or null when it is absent or malformed. */
private fun parseHexColor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
}

/**
 * The Funds tab: the list of cash buckets, and the detail view of one of them.
 *
 * Self-contained — it owns its own `ViewModel` and the whole page width — so the Budget
 * screen only has to decide that this tab is showing, not what is on it.
 */
@Composable
fun FundsScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    viewModel: FundsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isCreateSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isEditAmountVisible by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<FundWithProgress?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        FundListContent(
            uiState = uiState,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            onCreateClick = { isCreateSheetVisible = true },
            onFundClick = { viewModel.openFund(it.fund.id) }
        )
    }

    val selected = uiState.selectedFund
    if (selected != null) {
        FundDetailSheet(
            fund = selected,
            transactions = uiState.selectedTransactions,
            breakdown = uiState.selectedBreakdown,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            onDismiss = { viewModel.closeFund() },
            onEditAmount = { isEditAmountVisible = true },
            onArchiveToggle = { viewModel.setArchived(selected.fund.id, !selected.fund.isArchived) },
            onDelete = { pendingDelete = selected }
        )
    }

    if (isCreateSheetVisible) {
        CreateFundSheet(
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            onDismiss = { isCreateSheetVisible = false },
            onCreate = { name, amountMinor, startDate, iconKey, colorHex, note ->
                viewModel.createFund(name, amountMinor, startDate, iconKey, colorHex, note)
                isCreateSheetVisible = false
            }
        )
    }

    if (isEditAmountVisible && selected != null) {
        EditAmountDialog(
            current = selected,
            onDismiss = { isEditAmountVisible = false },
            onConfirm = { amountMinor ->
                viewModel.updateFundAmount(selected.fund.id, amountMinor)
                isEditAmountVisible = false
            }
        )
    }

    pendingDelete?.let { fund ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(id = R.string.title_delete_fund)) },
            text = { Text(stringResource(id = R.string.msg_delete_fund_body, fund.fund.name)) },
            confirmButton = {
                AppTextButton(onClick = {
                    viewModel.deleteFund(fund.fund.id)
                    pendingDelete = null
                }) { Text(stringResource(id = R.string.label_delete)) }
            },
            dismissButton = {
                AppTextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(id = R.string.label_cancel))
                }
            }
        )
    }
}

@Composable
private fun FundListContent(
    uiState: FundsUiState,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onCreateClick: () -> Unit,
    onFundClick: (FundWithProgress) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            top = 10.dp,
            end = Dimens.ScreenPadding,
            bottom = 126.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            FundSummaryCard(
                uiState = uiState,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences,
                onCreateClick = onCreateClick
            )
        }

        if (uiState.funds.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.label_fund_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(uiState.funds, key = { it.fund.id }) { fund ->
                FundCard(
                    fund = fund,
                    currencyId = currencyId,
                    amountFormatPreferences = amountFormatPreferences,
                    onClick = { onFundClick(fund) }
                )
            }
        }
    }
}

@Composable
private fun FundSummaryCard(
    uiState: FundsUiState,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onCreateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp)
    ) {
        var showFundsInfo by rememberSaveable { mutableStateOf(false) }
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.title_fund_overview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.Info,
                    contentDescription = stringResource(id = R.string.desc_funds_info),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { showFundsInfo = true }
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryStat(
                    label = stringResource(id = R.string.label_fund_summary_allocated),
                    value = formatCurrencyValue(uiState.totalAllocatedMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                    modifier = Modifier.weight(1f)
                )
                SummaryStat(
                    label = stringResource(id = R.string.label_fund_summary_spent),
                    value = formatCurrencyValue(uiState.totalSpentMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                    modifier = Modifier.weight(1f)
                )
                SummaryStat(
                    label = stringResource(id = R.string.label_fund_summary_remaining),
                    value = formatCurrencyValue(uiState.totalRemainingMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(14.dp))
            Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.title_create_fund))
            }
        }
        if (showFundsInfo) {
            FundsInfoDialog(onDismiss = { showFundsInfo = false })
        }
    }
}


@Composable
private fun FundsInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.sheet,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(id = R.string.title_what_are_funds),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.msg_funds_what),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.msg_funds_how),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.label_funds_example),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = R.string.label_fund_example_name),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.accentInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.msg_funds_example),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(brandGradient())
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.label_close),
                        color = MaterialTheme.colorScheme.onBrandGradient,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FundCard(
    fund: FundWithProgress,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(fund.fund.colorHex) ?: MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fund.fund.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fund.fund.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(
                            id = R.string.label_fund_spent_of,
                            formatCurrencyValue(fund.spentMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                            formatCurrencyValue(fund.fund.amountMinor.toMajorUnits(), currencyId, amountFormatPreferences)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FundStatusBadge(status = fund.status)
            }

            Spacer(Modifier.height(12.dp))
            FundProgressBar(progress = fund.progress, overspent = fund.isOverspent)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    id = R.string.label_fund_remaining_format,
                    formatCurrencyValue(fund.remainingMinor.toMajorUnits(), currencyId, amountFormatPreferences)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FundStatusBadge(status: FundStatus) {
    val (labelRes, tint) = when (status) {
        FundStatus.Active -> R.string.label_fund_status_active to MaterialTheme.colorScheme.primary
        FundStatus.Completed -> R.string.label_fund_status_completed to MaterialTheme.colorScheme.tertiary
        FundStatus.Archived -> R.string.label_fund_status_archived to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(tint.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}

/** A hand-rolled bar so the fill, the track and the overspent colour are all ours. */
@Composable
private fun FundProgressBar(progress: Float, overspent: Boolean) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = if (overspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(fillColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FundDetailSheet(
    fund: FundWithProgress,
    transactions: List<Transaction>,
    breakdown: FundBreakdown,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDismiss: () -> Unit,
    onEditAmount: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        FundDetailContent(
            fund = fund,
            transactions = transactions,
            breakdown = breakdown,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            onEditAmount = onEditAmount,
            onArchiveToggle = onArchiveToggle,
            onDelete = onDelete
        )
    }
}

@Composable
private fun FundDetailContent(
    fund: FundWithProgress,
    transactions: List<Transaction>,
    breakdown: FundBreakdown,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onEditAmount: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            top = 4.dp,
            end = Dimens.ScreenPadding,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = fund.fund.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                FundStatusBadge(status = fund.status)
            }
        }

        if (fund.fund.note.isNotBlank()) {
            item {
                Text(
                    text = fund.fund.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryStat(
                            label = stringResource(id = R.string.label_fund_summary_allocated),
                            value = formatCurrencyValue(fund.fund.amountMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStat(
                            label = stringResource(id = R.string.label_fund_summary_spent),
                            value = formatCurrencyValue(fund.spentMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStat(
                            label = stringResource(id = R.string.label_fund_summary_remaining),
                            value = formatCurrencyValue(fund.remainingMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    FundProgressBar(progress = fund.progress, overspent = fund.isOverspent)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppTextButton(onClick = onEditAmount) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(id = R.string.label_fund_edit_amount))
                        }
                        AppTextButton(onClick = onArchiveToggle) {
                            Icon(Icons.Filled.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stringResource(
                                    id = if (fund.fund.isArchived) R.string.label_fund_unarchive else R.string.label_fund_archive
                                )
                            )
                        }
                        AppTextButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(id = R.string.label_fund_delete))
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(id = R.string.title_fund_breakdown),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (breakdown.isEmpty) {
            item {
                Text(
                    text = stringResource(id = R.string.label_fund_no_spending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    breakdown.slices.forEach { slice ->
                        CategorySliceRow(
                            slice = slice,
                            currencyId = currencyId,
                            amountFormatPreferences = amountFormatPreferences
                        )
                    }
                }
            }
            if (breakdown.timeline.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.label_fund_breakdown_timeline),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                item {
                    TimelineChart(
                        points = breakdown.timeline,
                        currencyId = currencyId,
                        amountFormatPreferences = amountFormatPreferences
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(id = R.string.label_fund_linked_transactions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.label_fund_no_spending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(transactions, key = { it.id }) { transaction ->
                LinkedTransactionRow(
                    transaction = transaction,
                    currencyId = currencyId,
                    amountFormatPreferences = amountFormatPreferences
                )
            }
        }
    }
}

@Composable
private fun CategorySliceRow(
    slice: FundCategorySlice,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
) {
    val tint = parseHexColor(slice.colorHex) ?: MaterialTheme.colorScheme.primary
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(tint)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = slice.label.ifBlank { stringResource(id = R.string.label_fund_uncategorized) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatCurrencyValue(slice.amountMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(slice.fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(tint)
            )
        }
    }
}

@Composable
private fun TimelineChart(
    points: List<FundTimelinePoint>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatCurrencyValue(point.amountMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height((72 * point.fraction.coerceIn(0.05f, 1f)).dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LinkedTransactionRow(
    transaction: Transaction,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
) {
    val isIncome = transaction.transactionTypeId == 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.categoryIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note.ifBlank { stringResource(id = R.string.label_fund_uncategorized) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = fundDateFormatter.format(Date(transaction.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatCurrencyValue(
                transaction.amount,
                currencyId,
                amountFormatPreferences,
                prefix = if (isIncome) "+" else "-"
            ),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateFundSheet(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDismiss: () -> Unit,
    onCreate: (name: String, amountMinor: Long, startDate: Long, iconKey: String, colorHex: String, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var iconKey by rememberSaveable { mutableStateOf(FUND_ICON_KEYS.first()) }
    var colorHex by rememberSaveable { mutableStateOf(FUND_COLOR_HEXES.first()) }
    val startDate = remember { System.currentTimeMillis() }

    val amountMinor = amountText.trim().toDoubleOrNull()?.takeIf { it > 0.0 }?.toMinorUnits()
    val canSave = name.isNotBlank() && amountMinor != null

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(id = R.string.title_create_fund),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.label_fund_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text(stringResource(id = R.string.label_fund_amount_field)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(
                    id = R.string.label_fund_start_date_value,
                    fundDateFormatter.format(Date(startDate))
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(id = R.string.label_fund_icon),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(FUND_ICON_KEYS) { key ->
                    val selected = key == iconKey
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { iconKey = key },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ExpenseTrackerIconRegistry.iconForKey(key),
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = stringResource(id = R.string.label_fund_color),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(FUND_COLOR_HEXES) { hex ->
                    val selected = hex == colorHex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(hex) ?: MaterialTheme.colorScheme.primary)
                            .border(
                                width = if (selected) 3.dp else 0.dp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { colorHex = hex }
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(id = R.string.label_fund_note)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val resolvedAmount = amountMinor ?: return@Button
                    onCreate(name.trim(), resolvedAmount, startDate, iconKey, colorHex, note.trim())
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.title_create_fund))
            }
        }
    }
}

@Composable
private fun EditAmountDialog(
    current: FundWithProgress,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amountText by rememberSaveable {
        mutableStateOf(
            current.fund.amount.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.label_fund_edit_amount)) },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.msg_fund_edit_amount_updates_income),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(id = R.string.label_fund_amount_field)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            AppTextButton(
                enabled = amountText.trim().toDoubleOrNull()?.let { it > 0.0 } == true,
                onClick = {
                    amountText.trim().toDoubleOrNull()?.takeIf { it > 0.0 }?.let { onConfirm(it.toMinorUnits()) }
                }
            ) { Text(stringResource(id = R.string.label_save)) }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) { Text(stringResource(id = R.string.label_cancel)) }
        }
    )
}
