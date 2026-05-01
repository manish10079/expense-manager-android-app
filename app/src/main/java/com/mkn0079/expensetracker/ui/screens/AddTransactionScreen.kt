package com.mkn0079.expensetracker.ui.screens

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_TRANSACTION_TYPE_ID
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.CurrencyPosition
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.models.RecurringTransactionDraft
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.SyncState
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.components.WheelDateTimePickerModal
import com.mkn0079.expensetracker.ui.components.WheelPickerMode
import com.mkn0079.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mkn0079.expensetracker.utils.formatDate
import com.mkn0079.expensetracker.utils.getRankedCategories
import com.mkn0079.expensetracker.utils.getCurrency
import com.mkn0079.expensetracker.utils.localDateTimestampToDatePickerSelection
import com.mkn0079.expensetracker.utils.toMinorUnits
import java.math.BigDecimal

private const val incomeTypeId = 1
private const val expenseTypeId = 2

private data class TransactionMode(
    val id: Int,
    val label: String
)

private val transactionModes = listOf(
    TransactionMode(id = incomeTypeId, label = "Income"),
    TransactionMode(id = expenseTypeId, label = "Expense")
)

private data class RecurringModeOption(
    val frequency: RecurringFrequency,
    val label: String
)

private val recurringModeOptions = listOf(
    RecurringModeOption(RecurringFrequency.Daily, "Daily"),
    RecurringModeOption(RecurringFrequency.Weekly, "Weekly"),
    RecurringModeOption(RecurringFrequency.Monthly, "Monthly"),
    RecurringModeOption(RecurringFrequency.Yearly, "Yearly")
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
        var isKeypadExpanded by rememberSaveable(existingTransaction?.id) { mutableStateOf(false) }

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
            (!isRecurringEnabled || (selectedTransactionTypeId == expenseTypeId && recurringCount != null && recurringCount > 0))

        LaunchedEffect(selectedTransactionTypeId) {
            if (selectedTransactionTypeId != expenseTypeId) {
                isRecurringEnabled = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = if (dense) 16.dp else 18.dp,
                    vertical = if (dense) 12.dp else 14.dp
                )
        ) {
            HeaderRow(
                onBackClick = onBackClick,
                title = if (isEditMode) "Edit Transaction" else "Add Transaction",
                compact = compact
            )

            Spacer(modifier = Modifier.height(if (dense) 12.dp else 14.dp))

            DividerLine()

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
                    TransactionModeToggle(
                        selectedModeId = selectedTransactionTypeId,
                        compact = compact,
                        onModeSelected = { selectedTransactionTypeId = it }
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
                            onClick = { isKeypadExpanded = !isKeypadExpanded }
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(if (dense) 10.dp else 12.dp)
                    ) {
                        SectionHeader(title = "CATEGORY")
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
                        SectionHeader(title = "PAYMENT METHOD")
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
                            label = "DATE",
                            value = formatTransactionDate(selectedDateMillis, dateFormatPattern),
                            compact = compact,
                            onClick = { isDatePickerVisible = true }
                        )

                        if (selectedTransactionTypeId == expenseTypeId) {
                            RecurringCompactCard(
                                modifier = Modifier.weight(1f),
                                isEnabled = isRecurringEnabled,
                                frequency = selectedRecurringFrequency,
                                compact = compact,
                                onClick = { isRecurringModalVisible = true }
                            )
                        }
                    }

                    SelectionInfoCard(
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Filled.EditNote,
                        label = "NOTE",
                        value = note.ifBlank { "Add note" },
                        isPlaceholder = note.isBlank(),
                        compact = compact,
                        onClick = {
                            noteDraft = note
                            isNoteSheetVisible = true
                        }
                    )
                }

                AnimatedVisibility(
                    visible = isKeypadExpanded,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        NumericKeypad(
                            compact = compact,
                            onKeyPressed = { pressedKey ->
                                amountInput = updateAmountInput(current = amountInput, pressedKey = pressedKey)
                                onAmountInputChange(amountInput)
                            }
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
                        contentDescription = "Delete transaction",
                        onClick = onDeleteClick
                    )

                    SideActionButton(
                        icon = Icons.Filled.Dialpad,
                        contentDescription = "Enter amount",
                        onClick = { isKeypadExpanded = !isKeypadExpanded }
                    )

                    AddTransactionButton(
                        modifier = Modifier.weight(1f),
                        enabled = canSubmit,
                        label = if (isEditMode) "Update" else "Add",
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
                                syncState = existingTransaction?.syncState ?: SyncState.LOCAL_ONLY,
                                isDeleted = false,
                                updatedAt = existingTransaction?.updatedAt ?: selectedDateMillis,
                                sourceRecurringRuleId = existingTransaction?.sourceRecurringRuleId
                            )
                            val recurringDraft = if (
                                isRecurringEnabled &&
                                selectedTransactionTypeId == expenseTypeId &&
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
                            onSaveClick(transaction, recurringDraft)
                        }
                    )

                    SideActionButton(
                        icon = Icons.Filled.Calculate,
                        contentDescription = "Open calculator",
                        onClick = onCalculatorClick
                    )
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
                onDismissRequest = { isNoteSheetVisible = false },
                onSave = {
                    note = noteDraft
                    onNoteChange(note)
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
private fun HeaderRow(
    onBackClick: () -> Unit,
    title: String,
    compact: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 36.dp else 40.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(if (compact) 17.dp else 19.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )
    }
}

@Composable
private fun DividerLine() {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colorScheme.onSurface.copy(alpha = 0.65f))
    )
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = if (isEnabled) colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
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
                    text = "Recurring Transaction",
                    color = colorScheme.onSurface,
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
                    SectionHeader(title = "FREQUENCY")
                    
                    val density = LocalDensity.current
                    var containerWidthPx by remember { mutableIntStateOf(0) }
                    val selectedIndex = recurringModeOptions.indexOfFirst { it.frequency == selectedFrequency }.coerceAtLeast(0)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .onSizeChanged { containerWidthPx = it.width }
                            .clip(RoundedCornerShape(20.dp))
                            .background(colorScheme.surface)
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
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(colorScheme.primary, colorScheme.secondary)
                                        )
                                    )
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
                                        text = option.label,
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
                    SectionHeader(title = "TOTAL INSTALLMENTS")
                    
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
                                        if (isSelected) colorScheme.primary.copy(alpha = 0.15f)
                                        else colorScheme.surface
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
                            placeholder = { Text("Other", fontSize = 14.sp) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
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
                            text = "Current transaction is installment #1. Future entries will be generated automatically.",
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
            Text(
                text = "Turn on to automatically track this commitment in the future.",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 52.dp)
            )
        }
    }
}

@Composable
private fun TransactionModeToggle(
    selectedModeId: Int,
    compact: Boolean,
    onModeSelected: (Int) -> Unit
) {
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val selectedIndex = transactionModes.indexOfFirst { it.id == selectedModeId }.coerceAtLeast(0)
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { containerWidthPx = it.width }
            .clip(RoundedCornerShape(24.dp))
            .background(colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val tabWidth = with(density) { (containerWidthPx.toDp() - 8.dp) / transactionModes.size }
        
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "transaction_mode_indicator_offset"
        )

        // Sliding indicator (Pill)
        if (containerWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(colorScheme.primary, colorScheme.secondary)
                        )
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            transactionModes.forEach { mode ->
                val isSelected = mode.id == selectedModeId
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "transaction_mode_text_color"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onModeSelected(mode.id) }
                        .padding(vertical = if (compact) 10.dp else 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        color = animatedColor,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 14.sp else 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountCard(
    amountText: String,
    currencyId: Int,
    selectedTransactionTypeId: Int,
    compact: Boolean
) {
    val shape = RoundedCornerShape(if (compact) 28.dp else 32.dp)
    val currency = getCurrency(currencyId)
    val amountColor = if (selectedTransactionTypeId == incomeTypeId) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
                )
            )
            .padding(
                horizontal = if (compact) 20.dp else 24.dp,
                vertical = if (compact) 18.dp else 22.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "₹",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 22.sp else 24.sp
                ),
                modifier = Modifier.padding(bottom = 6.dp, end = 8.dp)
            )

            Text(
                text = amountText,
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 42.sp else 50.sp,
                    lineHeight = if (compact) 46.sp else 54.sp
                )
            )
        }
    }
}

@Composable
private fun CurrencyAmountCard(
    amountText: String,
    currencyId: Int,
    selectedTransactionTypeId: Int,
    compact: Boolean,
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
            .shadow(
                elevation = 22.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
                )
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 20.dp else 24.dp,
                vertical = if (compact) 18.dp else 22.dp
            )
    ) {
        Text(
            text = "ENTER AMOUNT",
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontSize = if (compact) 9.sp else 10.sp
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .graphicsLayer { translationY = labelTranslationY }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = if (compact) 8.dp else 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            if (currency.position == CurrencyPosition.PREFIX) {
                Text(
                    text = currency.currencySymbol,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 22.sp else 24.sp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp, end = 8.dp)
                )
            }

            Text(
                text = amountText,
                color = amountColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 42.sp else 50.sp,
                    lineHeight = if (compact) 46.sp else 54.sp
                )
            )

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
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)
                        )
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
    compact: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(title = label)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 56.dp else 64.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .padding(
                    horizontal = if (compact) 14.dp else 16.dp,
                    vertical = if (compact) 12.dp else 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (compact) 18.dp else 20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = value,
                color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
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
        listOf(".", "0", "delete")
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                text = "Amount Keypad",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (compact) 15.sp else 16.sp
                )
            )
            Text(
                text = if (expanded) "Tap to hide keypad" else "Tap to slide up keypad",
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
                contentDescription = if (expanded) "Collapse keypad" else "Expand keypad",
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
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == "delete") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
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
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Confirm $selectedCategory transaction",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(15.dp)
                )
            }
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
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

@Preview(
    name = "Add Transaction",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Preview(
    name = "Add Transaction Compact",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=360dp,height=740dp,dpi=420"
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
        label = "RECURRING",
        value = if (isEnabled) frequency.name else "Off",
        isPlaceholder = !isEnabled,
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

    LaunchedEffect(Unit) {
        delay(300) // Small delay to ensure sheet is visible before focusing
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
                    text = "What's this for?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Area
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = note,
                    onValueChange = { if (it.length <= 200) onNoteChange(it) },
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
                            "Add a note...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        if (note.isNotEmpty()) {
                            IconButton(onClick = { onNoteChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear note",
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
                    text = "${note.length}/200",
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
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .clickable(onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Save Note",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
