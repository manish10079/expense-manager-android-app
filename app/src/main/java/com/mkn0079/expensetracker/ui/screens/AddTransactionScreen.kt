package com.mkn0079.expensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurpleGlow
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
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
            .background(BackgroundDark)
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
        var isNoteDialogVisible by rememberSaveable { mutableStateOf(false) }
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
                        Text(
                            text = "ENTER AMOUNT",
                            color = Color(0xFF8E8799),
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 2.2.sp,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (dense) 11.sp else 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(if (dense) 10.dp else 14.dp))

                        CurrencyAmountCard(
                            amountText = amountInput,
                            currencyId = currencyId,
                            selectedTransactionTypeId = selectedTransactionTypeId,
                            compact = compact
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

                        SelectionInfoCard(
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Filled.EditNote,
                            label = "NOTE",
                            value = note.ifBlank { "Add note" },
                            isPlaceholder = note.isBlank(),
                            compact = compact,
                            onClick = {
                                noteDraft = note
                                isNoteDialogVisible = true
                            }
                        )
                    }

                    if (selectedTransactionTypeId == expenseTypeId) {
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

                Spacer(modifier = Modifier.height(if (dense) 12.dp else 16.dp))

                KeypadToggle(
                    expanded = isKeypadExpanded,
                    compact = compact,
                    onClick = { isKeypadExpanded = !isKeypadExpanded }
                )

                AnimatedVisibility(
                    visible = isKeypadExpanded,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(if (dense) 12.dp else 16.dp))

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
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = localDateTimestampToDatePickerSelection(selectedDateMillis)
            )
            DatePickerDialog(
                onDismissRequest = { isDatePickerVisible = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedDateMillis = datePickerState.selectedDateMillis
                                ?.let { pickedDateMillis ->
                                    datePickerSelectionToLocalDateTimestamp(
                                        selectedDateMillis = pickedDateMillis,
                                        referenceTimestamp = selectedDateMillis
                                    )
                                }
                                ?: selectedDateMillis
                            isDatePickerVisible = false
                        }
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isDatePickerVisible = false }) {
                        Text(text = "Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState, showModeToggle = false)
            }
        }

        if (isNoteDialogVisible) {
            AlertDialog(
                onDismissRequest = { isNoteDialogVisible = false },
                title = {
                    Text(
                        text = "Transaction Note",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        placeholder = { Text("Add a note") },
                        minLines = 3,
                        maxLines = 4
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            note = noteDraft
                            onNoteChange(note)
                            isNoteDialogVisible = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isNoteDialogVisible = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun HeaderRow(
    onBackClick: () -> Unit,
    title: String,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 36.dp else 40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = PurpleAccent,
                modifier = Modifier.size(if (compact) 17.dp else 19.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            color = PurplePrimary,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 22.sp else 24.sp
            )
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.08f))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF141418))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isEnabled,
                onCheckedChange = onEnabledChange
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Recurring Transaction",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Default is off. Turn it on to track this expense in Budget & Recurring.",
                    color = Color(0xFF9A93A6),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (isEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "FREQUENCY")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF17171A))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recurringModeOptions.forEach { option ->
                        val selected = option.frequency == selectedFrequency
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) {
                                        Brush.horizontalGradient(
                                            colors = listOf(PurplePrimary, Color(0xFFB89AF7))
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                )
                                .clickable { onFrequencySelected(option.frequency) }
                                .padding(vertical = if (compact) 10.dp else 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.label,
                                color = if (selected) Color(0xFF24114C) else Color(0xFFD9D0E8),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = repeatCountInput,
                onValueChange = onRepeatCountChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Repeat count") },
                placeholder = { Text("12") },
                supportingText = {
                    Text(
                        text = "How many times this recurring transaction should repeat.",
                        color = Color(0xFF9A93A6)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                    focusedContainerColor = Color(0xFF19191D),
                    unfocusedContainerColor = Color(0xFF19191D),
                    focusedLabelColor = PurplePrimary,
                    unfocusedLabelColor = Color(0xFFAAA2B8),
                    cursorColor = PurplePrimary
                )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF17171A))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        transactionModes.forEach { mode ->
            val isSelected = mode.id == selectedModeId
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(PurplePrimary, Color(0xFFB89AF7))
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    )
                    .clickable { onModeSelected(mode.id) }
                    .padding(vertical = if (compact) 10.dp else 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    color = if (isSelected) Color(0xFF24114C) else Color(0xFFD9D0E8),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 14.sp else 15.sp
                    )
                )
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
        Color(0xFFCBF8D7)
    } else {
        Color(0xFFF1ECF8)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = shape,
                ambientColor = PurplePrimary.copy(alpha = 0.18f),
                spotColor = PurpleGlow.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF27222F), Color(0xFF1A1A1D))
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
                color = PurpleAccent,
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
    compact: Boolean
) {
    val shape = RoundedCornerShape(if (compact) 28.dp else 32.dp)
    val currency = getCurrency(currencyId)
    val amountColor = if (selectedTransactionTypeId == incomeTypeId) {
        Color(0xFFCBF8D7)
    } else {
        Color(0xFFF1ECF8)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = shape,
                ambientColor = PurplePrimary.copy(alpha = 0.18f),
                spotColor = PurpleGlow.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF27222F), Color(0xFF1A1A1D))
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
            if (currency.position == CurrencyPosition.PREFIX) {
                Text(
                    text = currency.currencySymbol,
                    color = PurpleAccent,
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
                    color = PurpleAccent,
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
        color = Color(0xFFD2CBDD),
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
                    ambientColor = PurplePrimary.copy(alpha = 0.26f),
                    spotColor = PurpleGlow.copy(alpha = 0.24f)
                )
                .clip(CircleShape)
                .background(
                    brush = if (isSelected) {
                        Brush.linearGradient(
                            colors = listOf(PurplePrimary, Color(0xFFB89AF7))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF242424), Color(0xFF1F1F1F))
                        )
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF24104E) else Color(0xFF8F8A97),
                modifier = Modifier.size(if (compact) 18.dp else 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = if (isSelected) Color(0xFFE0D7F4) else Color(0xFF7A7482),
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
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1C1C1D))
                .clickable(onClick = onClick)
                .padding(
                    horizontal = if (compact) 14.dp else 16.dp,
                    vertical = if (compact) 14.dp else 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = if (isPlaceholder) Color(0xFFC3BAD5) else PurpleAccent,
                modifier = Modifier.size(if (compact) 18.dp else 20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = value,
                color = if (isPlaceholder) Color(0xFF8B8594) else Color(0xFFF0ECF6),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (compact) 14.sp else 15.sp,
                    fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal
                )
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
            .background(Color.White.copy(alpha = 0.04f))
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
                color = Color(0xFFF4F1F7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (compact) 15.sp else 16.sp
                )
            )
            Text(
                text = if (expanded) "Tap to hide keypad" else "Tap to slide up keypad",
                color = Color(0xFF9C95AB),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Box(
            modifier = Modifier
                .size(if (compact) 34.dp else 38.dp)
                .clip(CircleShape)
                .background(PurpleAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded) {
                    Icons.Filled.KeyboardArrowDown
                } else {
                    Icons.Filled.KeyboardArrowUp
                },
                contentDescription = if (expanded) "Collapse keypad" else "Expand keypad",
                tint = PurpleAccent,
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
            .background(Color.White.copy(alpha = 0.04f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == "delete") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = Color(0xFFD8CEEA),
                modifier = Modifier.size(if (compact) 22.dp else 24.dp)
            )
        } else {
            Text(
                text = label,
                color = Color(0xFFF4F1F7),
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
                ambientColor = PurplePrimary.copy(alpha = 0.34f),
                spotColor = PurpleGlow.copy(alpha = 0.38f)
            )
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF7A56F5), Color(0xFFB89AF7))
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
                color = Color(0xFF24114C),
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
                    .background(Color(0xFF2A1558)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Confirm $selectedCategory transaction",
                    tint = Color(0xFFEFE9FA),
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
                ambientColor = PurplePrimary.copy(alpha = 0.24f),
                spotColor = PurpleGlow.copy(alpha = 0.2f)
            )
            .clip(CircleShape)
            .background(Color(0xFF1C1C1F))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = PurpleAccent,
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
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Preview(
    name = "Add Transaction Compact",
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=360dp,height=740dp,dpi=420"
)
@Composable
private fun AddTransactionScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        AddTransactionScreen()
    }
}
