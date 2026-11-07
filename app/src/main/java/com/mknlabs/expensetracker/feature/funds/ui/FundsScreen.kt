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
import androidx.compose.material.icons.rounded.ChevronRight
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


