package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.data.constants.currencyMap
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.CurrencyPosition
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.ui.models.CalendarDayUi
import com.mkn0079.expensetracker.ui.models.CalendarMonthFinancialSummaryUi
import com.mkn0079.expensetracker.ui.models.TransactionCardItemUi
import com.mkn0079.expensetracker.ui.components.TransactionCard
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.utils.getAmountColor
import com.mkn0079.expensetracker.ui.viewmodels.CalendarViewModel
import com.mkn0079.expensetracker.ui.viewmodels.calendarAmountColor
import com.mkn0079.expensetracker.ui.viewmodels.calendarMonthTitle
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

private val CalendarBackground = Color(0xFF09090C)
private val CalendarSurface = Color(0xFF1A1A1F)
private val CalendarPurple = Color(0xFF8D6BFF)
private val CalendarPurpleSoft = Color(0xFFBCA8FF)
private val CalendarPurpleDark = Color(0xFF2B2048)
private val CalendarTextPrimary = Color(0xFFF2F2F5)
private val CalendarTextSecondary = Color(0xFF8B8796)
private val CalendarExpense = Color(0xFFFF9D92)
private val CalendarIncome = Color(0xFFB59BFF)
private val CalendarAmber = Color(0xFFFFC177)
private val CalendarMuted = Color(0xFF5D5B66)

private val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
private val monthNames = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

@Composable
fun CalendarScreen(
    transactions: List<Transaction> = transactionList,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    onBackClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {}
) {
    val calendarViewModel: CalendarViewModel = viewModel()
    var isMonthYearPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isYearPickerVisible by rememberSaveable { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(
        transactions,
        currencyId,
        dateFormatPattern,
        timeFormat,
        transactionCardCustomizationSettings
    ) {
        calendarViewModel.updateInputs(
            transactions = transactions,
            currencyId = currencyId,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            customizationSettings = transactionCardCustomizationSettings
        )
    }
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()

    Surface(color = CalendarBackground, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CalendarBackground)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp)
            ) {
                CalendarTopBar(
                    onBackClick = onBackClick
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding()
                    .background(CalendarBackground),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    ViewModeToggle(
                        isYearView = uiState.isYearView,
                        onSelectMonth = { calendarViewModel.setYearView(false) },
                        onSelectYear = { calendarViewModel.setYearView(true) }
                    )
                }

                if (uiState.isYearView) {
                    item {
                        YearHeading(
                            year = uiState.displayedYear,
                            onPreviousYear = calendarViewModel::goToPreviousYear,
                            onNextYear = calendarViewModel::goToNextYear,
                            onTodayClick = calendarViewModel::jumpToToday,
                            onOpenYearPicker = { isYearPickerVisible = true }
                        )
                    }

                    item {
                        AnnualSummaryCard(
                            totalIncome = uiState.yearlyIncomeLabel,
                            totalExpense = uiState.yearlyExpenseLabel
                        )
                    }

                    item {
                        YearSummaryGrid(
                            summaries = uiState.yearSummaries,
                            onMonthClick = { summary ->
                                calendarViewModel.selectMonth(createDate(uiState.displayedYear, summary.monthIndex, 1))
                                calendarViewModel.setYearView(false)
                            }
                        )
                    }
                } else {
                    item {
                        MonthHeading(
                            monthStart = uiState.displayedMonthStart,
                            onPreviousMonth = calendarViewModel::goToPreviousMonth,
                            onNextMonth = calendarViewModel::goToNextMonth,
                            onTodayClick = calendarViewModel::jumpToToday,
                            onOpenPicker = { isMonthYearPickerVisible = true }
                        )
                    }

                    item {
                        MonthCalendarCard(
                            days = uiState.monthDays,
                            selectedDate = uiState.selectedDate,
                            onDaySelected = { day ->
                                calendarViewModel.selectDay(day)
                            },
                            onSwipePrevious = calendarViewModel::goToPreviousMonth,
                            onSwipeNext = calendarViewModel::goToNextMonth
                        )
                    }

                    item {
                        DailyTotalsRow(
                            expenseLabel = uiState.selectedDayExpenseLabel,
                            incomeLabel = uiState.selectedDayIncomeLabel
                        )
                    }

                    item {
                        TransactionSectionHeader(
                            selectedDayTitle = uiState.selectedDayTitle
                        )
                    }

                    if (uiState.selectedDayTransactions.isEmpty()) {
                        item {
                            EmptyTransactionsCard(
                                message = uiState.emptyTransactionsMessage
                            )
                        }
                    } else {
                        items(uiState.selectedDayTransactions, key = { it.id }) { transaction ->
                            CalendarTransactionCard(
                                transaction = transaction,
                                transactionCardCustomizationSettings = uiState.customizationSettings,
                                onClick = { onTransactionClick(transaction.transaction) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (isMonthYearPickerVisible) {
        MonthYearPickerDialog(
            initialMonthStart = uiState.displayedMonthStart,
            yearRange = uiState.calendarYearRange,
            onDismiss = { isMonthYearPickerVisible = false },
            onConfirm = { newMonthStart ->
                calendarViewModel.selectMonth(newMonthStart)
                isMonthYearPickerVisible = false
            }
        )
    }

    if (isYearPickerVisible) {
        YearPickerDialog(
            initialYear = uiState.displayedYear,
            yearRange = uiState.calendarYearRange,
            onDismiss = { isYearPickerVisible = false },
            onConfirm = { newYear ->
                calendarViewModel.selectYear(newYear)
                isYearPickerVisible = false
            }
        )
    }
}

@Composable
private fun CalendarTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = CalendarPurpleSoft,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Calendar",
            color = PurplePrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ViewModeToggle(
    isYearView: Boolean,
    onSelectMonth: () -> Unit,
    onSelectYear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF17171A))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToggleSegment(
            modifier = Modifier.weight(1f),
            label = "Month",
            selected = !isYearView,
            onClick = onSelectMonth
        )
        ToggleSegment(
            modifier = Modifier.weight(1f),
            label = "Year",
            selected = isYearView,
            onClick = onSelectYear
        )
    }
}

@Composable
private fun ToggleSegment(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
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
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF24114C) else Color(0xFFD9D0E8),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MonthHeading(
    monthStart: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onOpenPicker: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularNavButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft, onClick = onPreviousMonth)

            Text(
                text = calendarMonthTitle(monthStart),
                color = CalendarTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable(onClick = onOpenPicker)
            )

            CircularNavButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowRight, onClick = onNextMonth)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TodayShortcutButton(onClick = onTodayClick)
        }
    }
}

@Composable
private fun TodayShortcutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF17171A))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Today",
            color = CalendarPurpleSoft,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MonthCalendarCard(
    days: List<CalendarDayUi>,
    selectedDate: Long,
    onDaySelected: (CalendarDayUi) -> Unit,
    onSwipePrevious: () -> Unit,
    onSwipeNext: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CalendarSurface),
        shape = RoundedCornerShape(26.dp),
        modifier = Modifier.pointerInput(days, selectedDate) {
            var totalDrag = 0f
            detectHorizontalDragGestures(
                onHorizontalDrag = { _, dragAmount ->
                    totalDrag += dragAmount
                },
                onDragEnd = {
                    when {
                        totalDrag > 80f -> onSwipePrevious()
                        totalDrag < -80f -> onSwipeNext()
                    }
                    totalDrag = 0f
                },
                onDragCancel = { totalDrag = 0f }
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayNames.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        color = when (index) {
                            5 -> CalendarAmber
                            6 -> CalendarExpense
                            else -> CalendarTextSecondary
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    week.forEach { day ->
                        DayCell(
                            modifier = Modifier.weight(1f),
                            day = day,
                            selected = isSameDay(day.timestamp, selectedDate),
                            onClick = { onDaySelected(day) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    modifier: Modifier = Modifier,
    day: CalendarDayUi,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.aspectRatio(0.82f).clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(if (selected) CalendarPurple else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayNumber.toString(),
                color = when {
                    selected -> CalendarTextPrimary
                    day.isCurrentMonth -> CalendarTextPrimary
                    else -> CalendarMuted
                },
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            if (day.hasExpense) {
                DotIndicator(color = if (day.isCurrentMonth) CalendarExpense else CalendarMuted.copy(alpha = 0.45f))
            }
            if (day.hasIncome) {
                DotIndicator(color = if (day.isCurrentMonth) CalendarPurpleSoft else CalendarMuted.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
private fun DotIndicator(color: Color) {
    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
}

@Composable
private fun DailyTotalsRow(
    expenseLabel: String,
    incomeLabel: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = expenseLabel,
            color = getAmountColor(2),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = incomeLabel,
            color = getAmountColor(1),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TransactionSectionHeader(
    selectedDayTitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "TRANSACTIONS",
            color = CalendarTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp
        )

        Text(
            text = selectedDayTitle,
            color = CalendarTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CalendarTransactionCard(
    transaction: TransactionCardItemUi,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    onClick: () -> Unit
) {
    TransactionCard(
        note = transaction.note,
        transactionDate = transaction.transactionDate,
        transactionTime = transaction.transactionTime,
        amount = transaction.amount,
        transactionTypeId = transaction.transactionTypeId,
        icon = transaction.icon,
        paymentType = transaction.paymentType,
        showTypeLabel = transactionCardCustomizationSettings.showIncomeExpenseLabels,
        showTransactionDate = transactionCardCustomizationSettings.showTransactionDate,
        showPaymentMethod = transactionCardCustomizationSettings.showPaymentMethod,
        showTransactionTime = transactionCardCustomizationSettings.showTransactionTime,
        showCategoryIcon = transactionCardCustomizationSettings.showCategoryIcon,
        onClick = onClick
    )
}

@Composable
private fun EmptyTransactionsCard(
    message: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CalendarSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "No transactions found",
                color = CalendarTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                color = CalendarTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun YearHeading(
    year: Int,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onTodayClick: () -> Unit,
    onOpenYearPicker: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SimpleIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous year",
                onClick = onPreviousYear
            )
            Text(
                text = year.toString(),
                color = CalendarTextPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable(onClick = onOpenYearPicker)
            )
            SimpleIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next year",
                onClick = onNextYear
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TodayShortcutButton(onClick = onTodayClick)
        }
    }
}

@Composable
private fun AnnualSummaryCard(
    totalIncome: String,
    totalExpense: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CalendarSurface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryStat(
                    label = "TOTAL INCOME",
                    value = totalIncome,
                    valueColor = CalendarTextPrimary
                )
                SummaryStat(
                    label = "TOTAL EXPENSES",
                    value = totalExpense,
                    valueColor = CalendarTextPrimary
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(
            text = label,
            color = CalendarTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.3.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun YearSummaryGrid(
    summaries: List<CalendarMonthFinancialSummaryUi>,
    onMonthClick: (CalendarMonthFinancialSummaryUi) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        summaries.chunked(3).forEach { rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowMonths.forEach { summary ->
                    Box(modifier = Modifier.weight(1f)) {
                        MonthSummaryCard(
                            summary = summary,
                            onClick = { onMonthClick(summary) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSummaryCard(
    summary: CalendarMonthFinancialSummaryUi,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CalendarSurface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.label,
                    color = CalendarTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier.size(7.dp).clip(CircleShape)
                        .background(if (summary.isProjection) CalendarMuted else CalendarPurpleSoft)
                )
            }

            if (summary.isProjection || (summary.income == 0.0 && summary.expense == 0.0)) {
                Spacer(modifier = Modifier.height(38.dp))
            } else {
                Text(
                    text = summary.expenseLabel,
                    color = getAmountColor(2),
                    fontSize = 12.sp
                )
                Text(
                    text = summary.incomeLabel,
                    color = getAmountColor(1),
                    fontSize = 12.sp
                )
                Text(
                    text = summary.netLabel,
                    color = calendarAmountColor(summary.net),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CircularNavButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape).background(CalendarSurface).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CalendarTextPrimary)
    }
}

@Composable
private fun SimpleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = CalendarTextSecondary,
            modifier = Modifier.alpha(0.9f)
        )
    }
}

private fun startOfDay(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun startOfMonth(timestamp: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun addMonths(timestamp: Long, months: Int): Long {
    return Calendar.getInstance().apply {
        timeInMillis = startOfMonth(timestamp)
        add(Calendar.MONTH, months)
    }.timeInMillis
}

private fun createDate(
    year: Int,
    month: Int,
    dayOfMonth: Int
): Long {
    return Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun isSameDay(first: Long, second: Long): Boolean {
    return startOfDay(first) == startOfDay(second)
}

private fun getField(timestamp: Long, field: Int): Int {
    return Calendar.getInstance().apply { timeInMillis = timestamp }.get(field)
}

private fun mondayFirstOffset(dayOfWeek: Int): Int {
    return (dayOfWeek + 5) % 7
}

private fun formatConfiguredCurrency(
    amount: Double,
    signed: Boolean = false,
    currencyId: Int = DEFAULT_CURRENCY_ID
): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val prefix = when {
        signed && amount > 0 -> "+"
        amount < 0 -> "-"
        else -> ""
    }
    val absoluteValue = formatter.format(abs(amount))
    val currency = currencyMap[currencyId] ?: currencyMap[DEFAULT_CURRENCY_ID]

    return when (currency?.position) {
        CurrencyPosition.POSTFIX -> "$prefix$absoluteValue${currency.currencySymbol}"
        else -> "$prefix${currency?.currencySymbol ?: "$"}$absoluteValue"
    }
}

@Composable
private fun MonthYearPickerDialog(
    initialMonthStart: Long,
    yearRange: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val years = remember(yearRange) { yearRange.toList() }
    var selectedMonth by remember(initialMonthStart) {
        mutableStateOf(getField(initialMonthStart, Calendar.MONTH))
    }
    var selectedYear by remember(initialMonthStart) {
        mutableStateOf(getField(initialMonthStart, Calendar.YEAR))
    }
    val monthListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedMonth)
    val yearListState = rememberLazyListState(
        initialFirstVisibleItemIndex = years.indexOf(selectedYear).coerceAtLeast(0)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Month",
                color = CalendarTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PickerWheel(
                    modifier = Modifier.weight(1f),
                    title = "Month",
                    items = monthNames,
                    selectedIndex = selectedMonth,
                    state = monthListState,
                    onSelectIndex = { selectedMonth = it }
                )
                PickerWheel(
                    modifier = Modifier.weight(1f),
                    title = "Year",
                    items = years.map(Int::toString),
                    selectedIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                    state = yearListState,
                    onSelectIndex = { selectedYear = years[it] }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(createDate(selectedYear, selectedMonth, 1)) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = CalendarSurface
    )
}

@Composable
private fun YearPickerDialog(
    initialYear: Int,
    yearRange: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val years = remember(yearRange) { yearRange.toList() }
    var selectedYear by remember(initialYear) { mutableStateOf(initialYear) }
    val yearListState = rememberLazyListState(
        initialFirstVisibleItemIndex = years.indexOf(selectedYear).coerceAtLeast(0)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Year",
                color = CalendarTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            PickerWheel(
                modifier = Modifier.fillMaxWidth(),
                title = "Year",
                items = years.map(Int::toString),
                selectedIndex = years.indexOf(selectedYear).coerceAtLeast(0),
                state = yearListState,
                onSelectIndex = { selectedYear = years[it] }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = CalendarSurface
    )
}

@Composable
private fun PickerWheel(
    modifier: Modifier = Modifier,
    title: String,
    items: List<String>,
    selectedIndex: Int,
    state: androidx.compose.foundation.lazy.LazyListState,
    onSelectIndex: (Int) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            color = CalendarTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF17171A)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    PickerWheelItem(
                        label = item,
                        selected = index == selectedIndex,
                        onClick = { onSelectIndex(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerWheelItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) CalendarPurple else Color(0xFF111116))
            .border(
                width = 1.dp,
                color = if (selected) CalendarPurpleSoft else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF24114C) else CalendarTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CalendarScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        CalendarScreen()
    }
}
