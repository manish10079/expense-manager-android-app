package com.mknlabs.expensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_TRANSACTION_TYPE_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.CurrencyPosition
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionDraft
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.ui.models.TabItem
import com.mknlabs.expensetracker.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.getRankedCategories
import com.mknlabs.expensetracker.utils.getCurrency
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.math.BigDecimal

private const val incomeTypeId = 1
private const val expenseTypeId = 2
private const val KEYPAD_DELETE_KEY = "delete"

private data class TransactionMode(
    val id: Int,
    @androidx.annotation.StringRes val label: Int
)

private val transactionModes = listOf(
    TransactionMode(id = incomeTypeId, label = R.string.title_income),
    TransactionMode(id = expenseTypeId, label = R.string.title_expense)
)

private data class RecurringModeOption(
    val frequency: RecurringFrequency,
    @androidx.annotation.StringRes val label: Int
)

private val recurringModeOptions = listOf(
    RecurringModeOption(RecurringFrequency.Daily, R.string.label_daily),
    RecurringModeOption(RecurringFrequency.Weekly, R.string.label_weekly),
    RecurringModeOption(RecurringFrequency.Monthly, R.string.label_monthly),
    RecurringModeOption(RecurringFrequency.Yearly, R.string.label_yearly)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    transactions: List<Transaction> = emptyList(),
    availableCategories: List<CategoryType> = categoryMap.values.toList(),
    availablePaymentMethods: List<PaymentType> = paymentTypeMap.values.sortedBy { it.id },
    existingTransaction: Transaction? = null,
    existingRecurringRule: RecurringTransactionRule? = null,
    initialAmountInput: String? = null,
    initialNote: String? = null,
    onBackClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onCalculatorClick: () -> Unit = {},
    onAmountInputChange: (String) -> Unit = {},
    onNoteChange: (String) -> Unit = {},
    onSaveClick: (Transaction, RecurringTransactionDraft?) -> Unit = { _, _ -> }
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val compact = maxHeight < 780.dp
        val dense = maxHeight < 700.dp
        val paymentMethods = remember(availablePaymentMethods) { availablePaymentMethods.sortedBy { it.id } }
        val isEditMode = existingTransaction != null

        var selectedTransactionTypeId by rememberSaveable(existingTransaction?.id) {
            mutableIntStateOf(existingTransaction?.transactionTypeId ?: DEFAULT_TRANSACTION_TYPE_ID)
        }
        var selectedCategoryId by rememberSaveable(existingTransaction?.id) {
            mutableIntStateOf(existingTransaction?.categoryId ?: 0)
        }
        var selectedPaymentId by rememberSaveable(existingTransaction?.id) {
            mutableIntStateOf(
                existingTransaction?.paymentTypeId
                    ?: paymentMethods.firstOrNull { it.id == DEFAULT_PAYMENT_TYPE_ID }?.id
                    ?: (paymentMethods.firstOrNull()?.id ?: 0)
            )
        }
        var amountInput by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingTransaction?.amount?.let(::formatEditableAmount).orEmpty().ifBlank { "0" })
        }
        var selectedDateMillis by rememberSaveable(existingTransaction?.id) {
            mutableLongStateOf(existingTransaction?.createdAt ?: System.currentTimeMillis())
        }
        var note by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingTransaction?.note.orEmpty())
        }
        var noteDraft by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingTransaction?.note.orEmpty())
        }
        var isRecurringEnabled by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingRecurringRule != null)
        }
        var selectedRecurringFrequency by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingRecurringRule?.frequency ?: RecurringFrequency.Monthly)
        }
        var recurringCountInput by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingRecurringRule?.repeatCount?.toString() ?: "12")
        }
        var isDatePickerVisible by rememberSaveable { mutableStateOf(false) }
        var isNoteSheetVisible by rememberSaveable { mutableStateOf(false) }
        var isRecurringModalVisible by rememberSaveable { mutableStateOf(false) }
        val amountFocusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        LaunchedEffect(initialAmountInput) {
            if (initialAmountInput != null && initialAmountInput != amountInput) {
                amountInput = initialAmountInput
            }
        }

        LaunchedEffect(initialNote) {
            if (initialNote != null && initialNote != note) {
                note = initialNote
                noteDraft = initialNote
            }
        }

        val colorScheme = MaterialTheme.colorScheme

        val categoriesForType = remember(transactions, availableCategories, selectedTransactionTypeId) {
            getRankedCategories(
                categories = availableCategories,
                transactions = transactions,
                transactionTypeId = selectedTransactionTypeId
            )
        }

        LaunchedEffect(categoriesForType) {
            if (categoriesForType.none { it.id == selectedCategoryId }) {
                selectedCategoryId = categoriesForType.firstOrNull()?.id ?: 0
            }
        }

        val selectedCategory = remember(categoriesForType, selectedCategoryId) {
            categoriesForType.firstOrNull { it.id == selectedCategoryId }
        }
        val selectedPayment = remember(paymentMethods, selectedPaymentId) {
            paymentMethods.firstOrNull { it.id == selectedPaymentId }
        }
        val recurringCount = recurringCountInput.toIntOrNull()
        val canSubmit = (amountInput.toDoubleOrNull() ?: 0.0) > 0 &&
            selectedCategory != null &&
            selectedPayment != null &&
            (!isRecurringEnabled || (recurringCount != null && recurringCount > 0))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    top = Dimens.HeaderSpacing,
                    bottom = if (dense) 12.dp else 14.dp
                )
        ) {
            AppHeader(
                title = stringResource(if (isEditMode) R.string.title_edit_transaction else R.string.title_add_transaction),
                onBackClick = {
                    keyboardController?.hide()
                    onBackClick()
                }
            )

            Spacer(modifier = Modifier.height(if (dense) 12.dp else 14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(if (dense) 12.dp else 16.dp)
                ) {
                    AnimatedTabSwitcher(
                        items = transactionModes.map { TabItem(it.id, stringResource(it.label)) },
                        selectedItemId = selectedTransactionTypeId,
                        onItemSelected = { selectedTransactionTypeId = it }
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CurrencyAmountCard(
                            amountText = amountInput,
                            currencyId = currencyId,
                            selectedTransactionTypeId = selectedTransactionTypeId,
                            compact = compact,
                            focusRequester = amountFocusRequester,
                            onAmountChange = {
                                val validated = validateAmountChange(it, amountInput)
                                if (validated != amountInput) {
                                    amountInput = validated
                                    onAmountInputChange(validated)
                                }
                            },
                            onClick = {
                                amountFocusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        )
                    }

                    SelectionInfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Filled.EditNote,
                        label = stringResource(R.string.label_note),
                        value = note.ifBlank { stringResource(R.string.label_add_note) },
                        isPlaceholder = note.isBlank(),
                        compact = compact,
                        onClick = {
                            noteDraft = note
                            isNoteSheetVisible = true
                        }
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(if (dense) 10.dp else 12.dp)
                    ) {
                        SectionHeader(title = stringResource(R.string.title_category_1))
                        ChoiceChipRow(
                            items = categoriesForType,
                            selectedId = selectedCategoryId,
                            compact = compact,
                            getId = { it.id },
                            getLabel = { it.name },
                            getIcon = { it.icon },
                            onItemSelected = { selectedCategoryId = it }
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(if (dense) 10.dp else 12.dp)
                    ) {
                        SectionHeader(title = stringResource(R.string.title_payment_method))
                        ChoiceChipRow(
                            items = paymentMethods,
                            selectedId = selectedPaymentId,
                            compact = compact,
                            getId = { it.id },
                            getLabel = { it.name },
                            getIcon = { it.icon },
                            onItemSelected = { selectedPaymentId = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (dense) 10.dp else 12.dp)
                    ) {
                        SelectionInfoCard(
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Filled.CalendarMonth,
                            label = stringResource(R.string.label_date),
                            value = formatTransactionDate(selectedDateMillis, dateFormatPattern),
                            compact = compact,
                            onClick = { isDatePickerVisible = true }
                        )

                        RecurringCompactCard(
                            modifier = Modifier.weight(1f),
                            isEnabled = isRecurringEnabled,
                            frequency = selectedRecurringFrequency,
                            compact = compact,
                            onClick = { isRecurringModalVisible = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (dense) 12.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SideActionButton(
                        icon = Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(R.string.desc_delete_transaction),
                        onClick = {
                            keyboardController?.hide()
                            onDeleteClick()
                        }
                    )

                    SideActionButton(
                        icon = Icons.Filled.Dialpad,
                        contentDescription = stringResource(R.string.desc_enter_amount),
                        onClick = {
                            amountFocusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    )

                    AddTransactionButton(
                        modifier = Modifier.weight(1f),
                        enabled = canSubmit,
                        label = if (isEditMode) stringResource(R.string.label_update_action) else stringResource(R.string.label_add_action),
                        selectedCategory = selectedCategory?.name.orEmpty(),
                        onClick = {
                            val category = selectedCategory ?: return@AddTransactionButton
                            val payment = selectedPayment ?: return@AddTransactionButton
                            val amount = amountInput.toDoubleOrNull() ?: return@AddTransactionButton
                            val transaction = Transaction(
                                id = existingTransaction?.id.orEmpty(),
                                note = note.trim(),
                                createdAt = selectedDateMillis,
                                amountMinor = amount.toMinorUnits(),
                                transactionTypeId = selectedTransactionTypeId,
                                paymentTypeId = payment.id,
                                categoryId = category.id,
                                contentHash = existingTransaction?.contentHash,
                                syncState = existingTransaction?.syncState ?: SyncState.PENDING_UPLOAD,
                                isDeleted = false,
                                updatedAt = existingTransaction?.updatedAt ?: selectedDateMillis,
                                sourceRecurringRuleId = existingTransaction?.sourceRecurringRuleId
                            )
                            val recurringDraft = if (
                                isRecurringEnabled &&
                                recurringCount != null &&
                                recurringCount > 0
                            ) {
                                RecurringTransactionDraft(
                                    frequency = selectedRecurringFrequency,
                                    repeatCount = recurringCount
                                )
                            } else {
                                null
                            }
                            keyboardController?.hide()
                            onSaveClick(transaction, recurringDraft)
                        }
                    )

                    SideActionButton(
                        icon = Icons.Filled.Calculate,
                        contentDescription = stringResource(R.string.desc_open_calculator),
                        onClick = {
                            keyboardController?.hide()
                            onCalculatorClick()
                        }
                    )

                    if (!isEditMode) {
                        SideActionButton(
                            icon = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.desc_clear_fields),
                            onClick = {
                                selectedTransactionTypeId = DEFAULT_TRANSACTION_TYPE_ID
                                selectedCategoryId = 0
                                selectedPaymentId = paymentMethods.firstOrNull { it.id == DEFAULT_PAYMENT_TYPE_ID }?.id ?: (paymentMethods.firstOrNull()?.id ?: 0)
                                amountInput = "0"
                                selectedDateMillis = System.currentTimeMillis()
                                note = ""
                                noteDraft = ""
                                isRecurringEnabled = false
                                selectedRecurringFrequency = RecurringFrequency.Monthly
                                recurringCountInput = "12"
                            }
                        )
                    }
                }
            }
        }

        if (isDatePickerVisible) {
            WheelDateTimePickerModal(
                mode = WheelPickerMode.SINGLE_DATE,
                initialStartMillis = selectedDateMillis,
                onDismissRequest = { isDatePickerVisible = false },
                onConfirm = { start, _ ->
                    selectedDateMillis = start
                    isDatePickerVisible = false
                }
            )
        }

        if (isNoteSheetVisible) {
            TransactionNoteBottomSheet(
                note = noteDraft,
                onNoteChange = { noteDraft = it },
                onDismissRequest = {
                    // Prevent focus from returning to the amount field (which would
                    // slide the numeric keypad back up) when the note sheet closes.
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isNoteSheetVisible = false
                },
                onSave = {
                    note = noteDraft
                    onNoteChange(note)
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isNoteSheetVisible = false
                }
            )
        }

        if (isRecurringModalVisible) {
            ModalBottomSheet(
                onDismissRequest = { isRecurringModalVisible = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    RecurringTransactionSection(
                        isEnabled = isRecurringEnabled,
                        selectedFrequency = selectedRecurringFrequency,
                        repeatCountInput = recurringCountInput,
                        compact = compact,
                        onEnabledChange = { isRecurringEnabled = it },
                        onFrequencySelected = { selectedRecurringFrequency = it },
                        onRepeatCountChange = { recurringCountInput = it.filter(Char::isDigit) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecurringTransactionSection(
    isEnabled: Boolean,
    selectedFrequency: RecurringFrequency,
    repeatCountInput: String,
    compact: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onFrequencySelected: (RecurringFrequency) -> Unit,
    onRepeatCountChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isEnabled) Color.Transparent 
                     else colorScheme.outlineVariant.copy(alpha = 0.3f),
        label = "recurring_section_border"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SolidColor(Color.Transparent))
            .border(
                width = if (isEnabled) 0.dp else 1.dp,
                color = if (isEnabled) Color.Transparent else animatedBorderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) colorScheme.primary.copy(alpha = 0.12f)
                        else colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (isEnabled) colorScheme.primary else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_recurring_transaction),
                    color = if (isEnabled) colorScheme.primary else colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )
            }

            androidx.compose.material3.Switch(
                checked = isEnabled,
                onCheckedChange = onEnabledChange,
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.onPrimary,
                    checkedTrackColor = colorScheme.primary,
                    uncheckedThumbColor = colorScheme.outline,
                    uncheckedTrackColor = colorScheme.surfaceVariant
                )
            )
        }

        if (isEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Frequency Selector (Sliding Pill)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(title = stringResource(R.string.title_frequency))
                    
                    val density = LocalDensity.current
                    var containerWidthPx by remember { mutableIntStateOf(0) }
                    val selectedIndex = recurringModeOptions.indexOfFirst { it.frequency == selectedFrequency }.coerceAtLeast(0)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .onSizeChanged { containerWidthPx = it.width }
                            .clip(RoundedCornerShape(20.dp))
                            .background(SolidColor(Color.Transparent))
                            .border(
                                width = 1.dp,
                                color = colorScheme.outlineVariant.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(4.dp)
                    ) {
                        val tabWidth = with(density) { (containerWidthPx.toDp() - 8.dp) / recurringModeOptions.size }
                        
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * selectedIndex,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "recurring_freq_indicator_offset"
                        )

                        if (containerWidthPx > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = indicatorOffset)
                                    .width(tabWidth)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(brandGradient())
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            recurringModeOptions.forEach { option ->
                                val selected = option.frequency == selectedFrequency
                                val animatedColor by animateColorAsState(
                                    targetValue = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                                    label = "recurring_freq_text_color"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onFrequencySelected(option.frequency) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(option.label),
                                        color = animatedColor,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Installments Picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(title = stringResource(R.string.label_total_installments))
                    
                    val presetInstallments = listOf("3", "6", "12", "24")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetInstallments.forEach { count ->
                            val isSelected = repeatCountInput == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) SolidColor(colorScheme.primary.copy(alpha = 0.15f))
                                        else standardCardGradient()
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onRepeatCountChange(count) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count,
                                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // Custom Input Field
                        OutlinedTextField(
                            value = if (repeatCountInput in presetInstallments) "" else repeatCountInput,
                            onValueChange = { if (it.length <= 3) onRepeatCountChange(it) },
                            modifier = Modifier.weight(1.2f).height(44.dp),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.label_other_installment), fontSize = 14.sp) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = colorScheme.primary
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.secondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.msg_installment_info),
                            color = colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall.copy(
                                lineHeight = 14.sp,
                                letterSpacing = 0.2.sp
                            )
                        )
                    }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 52.dp, top = 4.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.label_recurring_track),
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic
                    )
                )
            }
        }
    }
}



@Composable
private fun CurrencyAmountCard(
    amountText: String,
    currencyId: Int,
    selectedTransactionTypeId: Int,
    compact: Boolean,
    focusRequester: FocusRequester,
    onAmountChange: (String) -> Unit,
    onClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(if (compact) 28.dp else 32.dp)
    val currency = getCurrency(currencyId)
    val amountColor = if (selectedTransactionTypeId == incomeTypeId) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val density = LocalDensity.current
    val labelTranslationY = with(density) { (-4).dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 120.dp else 140.dp) // Tightened height
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = shape
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 16.dp else 20.dp, // Tightened horizontal padding
                    vertical = if (compact) 12.dp else 16.dp    // Tightened vertical padding
                )
                .matchParentSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Column {
                Text(
                    text = stringResource(R.string.label_enter_amount),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 12.sp // Slightly reduced label size for tightness
                    ),
                    modifier = Modifier
                        .graphicsLayer { translationY = labelTranslationY }
                )

                Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )
            }

            // Middle/Bottom Section: Amount Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currency.position == CurrencyPosition.PREFIX) {
                    Text(
                        text = currency.currencySymbol,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 22.sp else 24.sp
                        ),
                        modifier = Modifier.padding(bottom = if (compact) 4.dp else 5.dp, end = 8.dp)
                    )
                }

                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = amountText,
                        color = Color.Transparent,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 42.sp else 50.sp,
                            lineHeight = if (compact) 46.sp else 54.sp
                        ),
                        maxLines = 1
                    )
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val textFieldValue = remember(amountText) {
                        TextFieldValue(
                            text = amountText,
                            selection = TextRange(amountText.length)
                        )
                    }
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { onAmountChange(it.text) },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 42.sp else 50.sp,
                            lineHeight = if (compact) 46.sp else 54.sp,
                            color = amountColor,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                }

                if (currency.position == CurrencyPosition.POSTFIX) {
                    Text(
                        text = currency.currencySymbol,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 22.sp else 24.sp
                        ),
                        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelLarge.copy(
            letterSpacing = 2.1.sp,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    )
}

@Composable
private fun <T> ChoiceChipRow(
    items: List<T>,
    selectedId: Int,
    compact: Boolean,
    getId: (T) -> Int,
    getLabel: (T) -> String,
    getIcon: (T) -> ImageVector,
    onItemSelected: (Int) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)) {
        items(items, key = getId) { item ->
            ChoiceChip(
                label = getLabel(item),
                icon = getIcon(item),
                isSelected = getId(item) == selectedId,
                compact = compact,
                onClick = { onItemSelected(getId(item)) }
            )
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.widthIn(min = if (compact) 72.dp else 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 58.dp else 64.dp)
                .shadow(
                    elevation = if (isSelected) 22.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f)
                )
                .clip(CircleShape)
                .background(
                    brush = if (isSelected) {
                        brandGradient()
                    } else {
                        standardCardGradient()
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(if (compact) 18.dp else 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                fontSize = if (compact) 10.sp else 11.sp
            )
        )
    }
}

@Composable
private fun SelectionInfoCard(
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector,
    label: String,
    value: String,
    isPlaceholder: Boolean = false,
    highlighted: Boolean = false,
    compact: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val animatedBorderColor by animateColorAsState(
        targetValue = if (highlighted) Color.Transparent 
                     else colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "selection_card_border"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(title = label)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 48.dp else 56.dp)
                .shadow(
                    elevation = if (highlighted) 12.dp else 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = colorScheme.primary.copy(alpha = if (highlighted) 0.15f else 0.06f),
                    spotColor = colorScheme.secondary.copy(alpha = if (highlighted) 0.15f else 0.06f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(SolidColor(Color.Transparent))
                .border(
                    width = if (highlighted) 0.dp else 1.dp,
                    color = if (highlighted) Color.Transparent else animatedBorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(onClick = onClick)
                .padding(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 8.dp else 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = when {
                    highlighted -> colorScheme.primary
                    isPlaceholder -> colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else -> colorScheme.primary
                },
                modifier = Modifier.size(if (compact) 18.dp else 20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = value,
                color = if (isPlaceholder) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = if (compact) 14.sp else 15.sp,
                    fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    compact: Boolean,
    onKeyPressed: (String) -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", KEYPAD_DELETE_KEY)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
            ) {
                row.forEach { key ->
                    KeypadKey(
                        label = key,
                        compact = compact,
                        onClick = { onKeyPressed(key) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadToggle(
    expanded: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(standardCardGradient())
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 16.dp else 18.dp,
                vertical = if (compact) 12.dp else 14.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.label_amount_keypad),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (compact) 15.sp else 16.sp
                )
            )
            Text(
                text = stringResource(if (expanded) R.string.label_tap_to_hide_keypad else R.string.label_tap_to_slide_up_keypad),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Box(
            modifier = Modifier
                .size(if (compact) 34.dp else 38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded) {
                    Icons.Filled.KeyboardArrowDown
                } else {
                    Icons.Filled.KeyboardArrowUp
                },
                contentDescription = stringResource(if (expanded) R.string.desc_collapse_keypad else R.string.desc_expand_keypad),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(if (compact) 20.dp else 22.dp)
            )
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(if (compact) 46.dp else 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(standardCardGradient())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == KEYPAD_DELETE_KEY) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.desc_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(if (compact) 22.dp else 24.dp)
            )
        } else {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = if (compact) 24.sp else 26.sp
                )
            )
        }
    }
}

@Composable
private fun AddTransactionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    label: String,
    selectedCategory: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.55f)
            .shadow(
                elevation = 26.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.38f)
            )
            .clip(shape)
            .background(brush = brandGradient())
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )


        }
    }
}

@Composable
private fun SideActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .shadow(
                elevation = 18.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            )
            .clip(CircleShape)
            .background(standardCardGradient())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun updateAmountInput(
    current: String,
    pressedKey: String
): String {
    return when (pressedKey) {
        "delete" -> {
            val updated = current.dropLast(1)
            if (updated.isBlank()) "0" else updated
        }

        "." -> {
            if (current.contains(".")) {
                current
            } else {
                "$current."
            }
        }

        else -> {
            val digitsAfterDecimal = if (current.contains(".")) {
                current.substringAfter(".").length
            } else {
                0
            }

            if (digitsAfterDecimal >= 2) {
                current
            } else if (current == "0") {
                if (pressedKey == "0") current else pressedKey
            } else {
                (current + pressedKey).take(12)
            }
        }
    }
}

private fun formatTransactionDate(
    dateInMillis: Long,
    dateFormatPattern: String
): String {
    return formatDate(dateInMillis, dateFormatPattern)
}

private fun formatEditableAmount(amount: Double): String {
    return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()
}

private fun validateAmountChange(newValue: String, current: String): String {
    if (newValue.isEmpty()) return "0"
    if (newValue.length > 12) return current

    val dotCount = newValue.count { it == '.' }
    if (dotCount > 1) return current

    if (newValue.any { !it.isDigit() && it != '.' }) return current

    if (newValue.contains(".")) {
        val decimals = newValue.substringAfter(".")
        if (decimals.length > 2) return current
    }

    if (newValue.startsWith("0") && newValue.length > 1 && newValue[1] != '.') {
        val sanitized = newValue.dropWhile { it == '0' }
        return if (sanitized.isEmpty()) "0" else sanitized
    }

    return newValue
}

@Preview(
    name = "Add Transaction",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)

@Composable
private fun AddTransactionScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        AddTransactionScreen()
    }
}

@Composable
private fun RecurringCompactCard(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    frequency: RecurringFrequency,
    compact: Boolean,
    onClick: () -> Unit
) {
    SelectionInfoCard(
        modifier = modifier,
        leadingIcon = Icons.Default.CalendarMonth,
        label = stringResource(R.string.label_recurring),
        value = if (isEnabled) stringResource(recurringModeOptions.first { it.frequency == frequency }.label) else stringResource(R.string.label_off),
        isPlaceholder = !isEnabled,
        highlighted = isEnabled,
        compact = compact,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionNoteBottomSheet(
    note: String,
    onNoteChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    // Keep the cursor at the right-most end of existing text when the sheet opens.
    var noteFieldValue by remember {
        mutableStateOf(TextFieldValue(text = note, selection = TextRange(note.length)))
    }

    LaunchedEffect(Unit) {
        delay(300) // Small delay to ensure sheet is visible before focusing
        noteFieldValue = noteFieldValue.copy(selection = TextRange(noteFieldValue.text.length))
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_what_is_this_for),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.desc_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Area
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = noteFieldValue,
                    onValueChange = { newValue ->
                        if (newValue.text.length <= 200) {
                            noteFieldValue = newValue
                            onNoteChange(newValue.text)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .focusRequester(focusRequester)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    placeholder = {
                        Text(
                            stringResource(R.string.placeholder_add_note),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        if (noteFieldValue.text.isNotEmpty()) {
                            IconButton(onClick = {
                                noteFieldValue = TextFieldValue("")
                                onNoteChange("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.desc_clear_note),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                // Character Counter
                Text(
                    text = "${noteFieldValue.text.length}/200",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }

            // Save Button (Mini Gradient Button)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(brandGradient())
                    .clickable(onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.label_save_note),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
