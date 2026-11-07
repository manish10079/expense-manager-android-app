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
