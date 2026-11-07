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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FundDetailSheet(
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
internal fun FundDetailContent(
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
internal fun CategorySliceRow(
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
