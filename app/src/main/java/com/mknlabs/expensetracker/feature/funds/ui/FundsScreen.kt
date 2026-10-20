package com.mknlabs.expensetracker.feature.funds.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Info
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.CategoryColorRow
import com.mknlabs.expensetracker.core.ui.components.IconPickerGrid
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedButton
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.HeroOutlineDark
import com.mknlabs.expensetracker.core.ui.theme.HeroOutlineLight
import com.mknlabs.expensetracker.core.ui.theme.HeroSurfaceLight
import com.mknlabs.expensetracker.core.ui.theme.glassHeroGradient
import com.mknlabs.expensetracker.core.ui.theme.heroBloom
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.identityColor
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.expenseGradient
import com.mknlabs.expensetracker.core.ui.theme.ExpenseInkLight
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.chip
import com.mknlabs.expensetracker.core.ui.theme.chipOutline
import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.chipSelectedInk
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.components.TransactionCard
import com.mknlabs.expensetracker.core.ui.models.TransactionCardItemUi
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.categoryIconOptions
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mknlabs.expensetracker.domain.repository.FundDefaults
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.FundStatus
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.FundWithProgress
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val fundDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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
    categories: List<CategoryType> = emptyList(),
    paymentMethods: List<PaymentType> = emptyList(),
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    isProUser: Boolean = false,
    viewModel: FundsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var pendingScrollToTop by remember { mutableStateOf(false) }
    var previousFundsSize by remember { mutableStateOf(uiState.funds.size) }

    LaunchedEffect(uiState.funds.size) {
        if (pendingScrollToTop && uiState.funds.size > previousFundsSize) {
            listState.animateScrollToItem(0)
            pendingScrollToTop = false
        }
        previousFundsSize = uiState.funds.size
    }

    // The detail sheet's rows are the app's own transaction cards, so their date, time, payment
    // method and category label all come from the same mapper the ledger uses: a card that
    // disagreed with the home list about how a transaction reads would be a second, thinner
    // presentation of the same data. The colour and the glyph come from that mapper too, so a
    // fund's expense wears the colour its category wears everywhere else.
    val paymentTypeNames = remember(paymentMethods) {
        paymentTypeMap.mapValues { it.value.name } + paymentMethods.associate { it.id to it.name }
    }
    val fallbackCategoryName = stringResource(R.string.label_other)
    val linkedTransactionCards = remember(
        uiState.selectedTransactions,
        currencyId,
        amountFormatPreferences,
        dateFormatPattern,
        timeFormat,
        paymentTypeNames,
        categories,
        fallbackCategoryName
    ) {
        uiState.selectedTransactions.map { transaction ->
            transaction.toTransactionCardItemUi(
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences,
                dateFormatPattern = dateFormatPattern,
                timeFormat = timeFormat,
                paymentTypeName = paymentTypeNames[transaction.paymentTypeId].orEmpty(),
                categories = categories,
                fallbackCategoryName = fallbackCategoryName,
                fundName = transaction.fundId?.let { id ->
                    uiState.selectedFund?.fund?.takeIf { it.id == id }?.name
                },
                tags = emptyList()
            )
        }
    }

    var isCreateSheetVisible by rememberSaveable { mutableStateOf(false) }
    var editingFund by remember { mutableStateOf<Fund?>(null) }
    var pendingDelete by remember { mutableStateOf<FundWithProgress?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        FundListContent(
            uiState = uiState,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            listState = listState,
            onCreateClick = { isCreateSheetVisible = true },
            onFundClick = { viewModel.openFund(it.fund.id) }
        )
    }

    val selected = uiState.selectedFund
    if (selected != null) {
        FundDetailSheet(
            fund = selected,
            transactions = linkedTransactionCards,
            breakdown = uiState.selectedBreakdown,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
            isProUser = isProUser,
            onDismiss = { viewModel.closeFund() },
            onEditAmount = {
                editingFund = selected.fund
                viewModel.closeFund()
                isCreateSheetVisible = true
            },
            onArchiveToggle = { viewModel.setArchived(selected.fund.id, !selected.fund.isArchived) },
            onDelete = { pendingDelete = selected }
        )
    }

    if (isCreateSheetVisible) {
        val editing = editingFund
        CreateFundSheet(
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            categories = categories,
            existingFund = editing,
            onDismiss = {
                val reopenId = editingFund?.id
                isCreateSheetVisible = false
                editingFund = null
                if (reopenId != null) viewModel.openFund(reopenId)
            },
            onCreate = { name, amountMinor, startDate, iconKey, colorHex, note, incomeCategoryId ->
                if (editing != null) {
                    viewModel.updateFundDetails(
                        fundId = editing.id,
                        name = name,
                        amountMinor = amountMinor,
                        iconKey = iconKey,
                        colorHex = colorHex,
                        note = note
                    )
                    isCreateSheetVisible = false
                    editingFund = null
                    viewModel.openFund(editing.id)
                } else {
                    viewModel.createFund(name, amountMinor, startDate, iconKey, colorHex, note, incomeCategoryId)
                    isCreateSheetVisible = false
                    // The new bucket is ordered first. Request a scroll to top, which
                    // will fire via LaunchedEffect as soon as the new fund is emitted
                    // to uiState.funds.
                    pendingScrollToTop = true
                }
            }
        )
    }

    pendingDelete?.let { fund ->
        ModernConfirmationDialog(
            onDismissRequest = { pendingDelete = null },
            title = stringResource(id = R.string.title_delete_fund),
            text = stringResource(id = R.string.msg_delete_fund_body, fund.fund.name),
            confirmButtonText = stringResource(id = R.string.label_delete),
            onConfirm = {
                viewModel.deleteFund(fund.fund.id)
                pendingDelete = null
            },
            headerIcon = Icons.Filled.Delete,
            dismissButtonText = stringResource(id = R.string.label_cancel),
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun FundListContent(
    uiState: FundsUiState,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    listState: LazyListState,
    onCreateClick: () -> Unit,
    onFundClick: (FundWithProgress) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // The overview is the tab's header rather than its first row: its totals are what every
        // bucket below is read against, so they stay on screen while those buckets scroll. It
        // carries the same side inset the list does, because it is still the same page.
        FundSummaryCard(
            uiState = uiState,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            onCreateClick = onCreateClick,
            modifier = Modifier.padding(
                start = Dimens.ScreenPadding,
                top = 10.dp,
                end = Dimens.ScreenPadding
            )
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Dimens.ScreenPadding,
                top = 18.dp,
                end = Dimens.ScreenPadding,
                bottom = 126.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
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
                        onClick = { onFundClick(fund) },
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(durationMillis = 350),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FundSummaryCard(
    uiState: FundsUiState,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.isDark
    AppCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppCardDefaults.CornerRadius),
        colors = AppCardColors(
            containerColor = if (isDark) Color.Transparent else HeroSurfaceLight,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(
                1.dp,
                if (isDark) HeroOutlineDark else HeroOutlineLight
            )
        ),
        brush = glassHeroGradient(),
        elevation = 0.dp
    ) {
        Box(modifier = Modifier.matchParentSize().heroBloom())
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStat(
                    label = stringResource(id = R.string.label_fund_summary_allocated),
                    value = formatCurrencyValue(uiState.totalAllocatedMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                    modifier = Modifier.weight(1f)
                )
                SummaryStat(
                    label = stringResource(id = R.string.label_fund_summary_spent),
                    value = formatCurrencyValue(uiState.totalSpentMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.expense
                )
                SummaryStat(
                    label = stringResource(id = R.string.label_fund_summary_remaining),
                    value = formatCurrencyValue(uiState.totalRemainingMinor.toMajorUnits(), currencyId, amountFormatPreferences),
                    modifier = Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.income
                )
            }
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = brandGradient())
                    .clickable(onClick = onCreateClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.title_create_fund),
                    color = MaterialTheme.colorScheme.onBrandGradient,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (showFundsInfo) {
            FundsInfoDialog(onDismiss = { showFundsInfo = false })
        }
    }
}


@Composable
private fun FundsInfoDialog(onDismiss: () -> Unit) {
    ModernConfirmationDialog(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.title_what_are_funds),
        text = stringResource(id = R.string.msg_funds_what) + "\n\n" + stringResource(id = R.string.msg_funds_how),
        confirmButtonText = stringResource(id = R.string.label_close),
        onConfirm = onDismiss,
        headerIcon = PhosphorIcons.Regular.Info,
        showDismissButton = false,
        extraContent = {
            Column {
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FundCard(
    fund: FundWithProgress,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        // Flat analytics card fill: #13151F in dark, white in light.
        colors = CardDefaults.cardColors(
            containerColor = if (MaterialTheme.colorScheme.isDark) Color(0xFF13151F) else Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val identityColor = parseHexColor(fund.fund.colorHex)
                    ?: MaterialTheme.colorScheme.accentInk
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.categorySoft(identityColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = fund.fund.icon,
                        contentDescription = null,
                        tint = identityColor,
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
        FundStatus.Active -> R.string.label_fund_status_active to MaterialTheme.colorScheme.income
        FundStatus.Completed -> R.string.label_fund_status_completed to MaterialTheme.colorScheme.expense
        FundStatus.Archived -> R.string.label_fund_status_archived to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val exhausted = status == FundStatus.Completed
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (exhausted) Modifier.background(expenseGradient(alpha = 0.22f))
                else Modifier.background(tint.copy(alpha = 0.12f))
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(id = labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = if (exhausted) ExpenseInkLight else tint
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FundDetailSheet(
    fund: FundWithProgress,
    transactions: List<TransactionCardItemUi>,
    breakdown: FundBreakdown,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    isProUser: Boolean,
    onDismiss: () -> Unit,
    onEditAmount: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetContainerColor = if (MaterialTheme.colorScheme.isDark) {
        MaterialTheme.colorScheme.profileSheet
    } else {
        BottomSheetDefaults.ContainerColor
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        FundDetailContent(
            fund = fund,
            transactions = transactions,
            breakdown = breakdown,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            transactionCardCustomizationSettings = transactionCardCustomizationSettings,
            isProUser = isProUser,
            onEditAmount = onEditAmount,
            onArchiveToggle = onArchiveToggle,
            onDelete = onDelete
        )
    }
}

@Composable
private fun FundDetailContent(
    fund: FundWithProgress,
    transactions: List<TransactionCardItemUi>,
    breakdown: FundBreakdown,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    isProUser: Boolean,
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.genderRow),
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
                    // Three equal-width outlined actions, so the row reads as one control strip
                    // rather than three text links of different lengths. Labels only -- no icons --
                    // so nothing competes with the words. Edit and Archive wear the ink of the
                    // "Allocated" caption in the summary above, while Delete wears the expense ink
                    // that the overview card's Spent amount uses, so the destructive action is the
                    // only red one. Each button's hairline is the same colour as its own label,
                    // undiluted, which is what makes the pair read as one outlined button instead
                    // of a label sitting inside a plain box.
                    val actionInk = MaterialTheme.colorScheme.onSurfaceVariant
                    val deleteInk = MaterialTheme.colorScheme.expense
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppOutlinedButton(
                            onClick = onEditAmount,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = actionInk),
                            border = BorderStroke(1.dp, actionInk),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.label_fund_edit),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        AppOutlinedButton(
                            onClick = onArchiveToggle,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = actionInk),
                            border = BorderStroke(1.dp, actionInk),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    id = if (fund.fund.isArchived) R.string.label_fund_unarchive else R.string.label_fund_archive
                                ),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        AppOutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = deleteInk),
                            border = BorderStroke(1.dp, deleteInk),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.label_fund_delete),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
            items(transactions, key = { it.id }) { card ->
                // The ledger's own card, not a lookalike: the same composable, the same flags
                // the user chose for their list, so a fund's spending reads exactly like the
                // same spending does on the home list.
                TransactionCard(
                    note = card.note,
                    transactionDate = card.transactionDate,
                    transactionTime = card.transactionTime,
                    amount = card.amount,
                    transactionTypeId = card.transactionTypeId,
                    icon = card.icon,
                    categoryId = card.transaction.categoryId,
                    categoryColorHex = card.categoryColorHex,
                    paymentType = card.paymentType,
                    categoryLabel = card.categoryLabel,
                    showTypeLabel = transactionCardCustomizationSettings.showIncomeExpenseLabels,
                    showTransactionDate = transactionCardCustomizationSettings.showTransactionDate,
                    showPaymentMethod = transactionCardCustomizationSettings.showPaymentMethod,
                    showTransactionTime = transactionCardCustomizationSettings.showTransactionTime,
                    showCategoryIcon = transactionCardCustomizationSettings.showCategoryIcon,
                    showCategoryLabel = transactionCardCustomizationSettings.showCategoryLabel,
                    tags = card.tags,
                    fundName = card.fundName,
                    showNoteTooltip = isProUser,
                    isProUser = isProUser,
                    isRecurring = card.isRecurring
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
    // Resolve through the palette by id, the same chain the category rows use: a slice that
    // carries no stored override still gets its seeded category's own colour rather than a
    // blanket purple. The pick wins when there is one.
    val tint = MaterialTheme.colorScheme.categoryColor(
        categoryId = slice.categoryId,
        colorHex = slice.colorHex
    )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateFundSheet(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    categories: List<CategoryType>,
    onDismiss: () -> Unit,
    onCreate: (name: String, amountMinor: Long, startDate: Long, iconKey: String, colorHex: String, note: String, incomeCategoryId: Int) -> Unit,
    existingFund: Fund? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editKey = existingFund?.id
    var name by rememberSaveable(editKey) { mutableStateOf(existingFund?.name.orEmpty()) }
    var amountText by rememberSaveable(editKey) {
        mutableStateOf(
            existingFund?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty()
        )
    }
    var note by rememberSaveable(editKey) { mutableStateOf(existingFund?.note.orEmpty()) }
    var iconKey by rememberSaveable(editKey) { mutableStateOf(existingFund?.iconKey ?: "gift") }
    var colorHex by rememberSaveable(editKey) { mutableStateOf(existingFund?.colorHex.orEmpty()) }
    val startDate = remember(editKey) { existingFund?.startDate ?: System.currentTimeMillis() }
    val isEditing = existingFund != null

    val incomeCategories = remember(categories) {
        val filtered = categories.filter { it.transactionTypeId == 1 }.sortedBy { it.sortOrder }
        filtered.ifEmpty { categoryMap.values.filter { it.transactionTypeId == 1 }.sortedBy { it.sortOrder } }
    }
    var selectedIncomeCategoryId by rememberSaveable {
        mutableIntStateOf(incomeCategories.firstOrNull()?.id ?: FundDefaults.INCOME_CATEGORY_ID)
    }

    val amountMinor = amountText.trim().toDoubleOrNull()?.takeIf { it > 0.0 }?.toMinorUnits()
    val canSave = name.isNotBlank() && amountMinor != null

    val sheetContainerColor = if (MaterialTheme.colorScheme.isDark) {
        MaterialTheme.colorScheme.profileSheet
    } else {
        BottomSheetDefaults.ContainerColor
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor
    ) {
        // The form scrolls; the action under it does not. The body is weighted against the
        // pinned footer so it yields its height to the action instead of pushing it off the
        // sheet: with the note field focused the sheet still ends in a reachable button.
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(
                        id = if (isEditing) R.string.title_edit_fund else R.string.title_create_fund
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // The create sheet's fields take the app's own focus treatment: the border, the
                // floating label and the caret all switch to the brand's ink while the field has
                // the user's attention, instead of Material's own primary, so a focused field reads
                // the same here as it does in every other editor in the app.
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.label_fund_name)) },
                    singleLine = true,
                    shape = AppOutlinedFieldDefaults.shape,
                    colors = AppOutlinedFieldDefaults.colors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(id = R.string.label_fund_amount_field)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = AppOutlinedFieldDefaults.shape,
                    colors = AppOutlinedFieldDefaults.colors(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isEditing && incomeCategories.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.label_income_categories),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(incomeCategories, key = { it.id }) { cat ->
                            val isSelected = cat.id == selectedIncomeCategoryId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedIncomeCategoryId = cat.id },
                                label = { Text(cat.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.name,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.chipSelected,
                                    selectedLabelColor = MaterialTheme.colorScheme.chipSelectedInk,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.chipSelectedInk,
                                    containerColor = MaterialTheme.colorScheme.chip,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.chipOutline
                                )
                            )
                        }
                    }
                }

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
                IconPickerGrid(
                    selectedId = iconKey,
                    onSelect = { iconKey = it },
                    identityColor = MaterialTheme.colorScheme.identityColor(colorHex.ifBlank { null }),
                    columns = 6,
                    modifier = Modifier.height(160.dp)
                )

                Text(
                    text = stringResource(id = R.string.label_fund_color),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CategoryColorRow(
                    selectedColorHex = colorHex.ifBlank { null },
                    onColorSelected = { colorHex = it.orEmpty() }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(id = R.string.label_fund_note)) },
                    shape = AppOutlinedFieldDefaults.shape,
                    colors = AppOutlinedFieldDefaults.colors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // The action is pinned under the fields and above the keyboard: while the note is
            // being typed it is the one thing that has to stay reachable, so it takes the IME
            // inset itself rather than letting the keyboard be drawn over it.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = Dimens.ScreenPadding)
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(brush = brandGradient(alpha = if (canSave) 1f else 0.45f))
                        .clickable(enabled = canSave) {
                            val resolvedAmount = amountMinor ?: return@clickable
                            onCreate(name.trim(), resolvedAmount, startDate, iconKey, colorHex, note.trim(), selectedIncomeCategoryId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            id = if (isEditing) R.string.title_edit_fund else R.string.title_create_fund
                        ),
                        color = MaterialTheme.colorScheme.onBrandGradient,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
