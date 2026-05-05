package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.ui.models.CalendarDayUi
import com.mkn0079.expensetracker.ui.models.CalendarMonthFinancialSummaryUi
import com.mkn0079.expensetracker.ui.models.TransactionCardItemUi
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.components.GatedAction
import com.mkn0079.expensetracker.ui.components.TransactionCard
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.expense
import com.mkn0079.expensetracker.ui.theme.featureGateLock
import com.mkn0079.expensetracker.ui.theme.standardCardGradient
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.income
import com.mkn0079.expensetracker.ui.horizontalSwipe
import com.mkn0079.expensetracker.utils.getAmountColor
import com.mkn0079.expensetracker.ui.viewmodels.CalendarViewModel
import com.mkn0079.expensetracker.ui.viewmodels.calendarMonthTitle
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.ui.components.AnimatedTabSwitcher
import com.mkn0079.expensetracker.ui.components.WheelDateTimePicker
import com.mkn0079.expensetracker.ui.models.TabItem
import java.util.Calendar

// Theme colors are now derived from MaterialTheme.colorScheme

private val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
private val monthNames = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

@Composable
fun CalendarScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    transactions: List<Transaction> = transactionList,
    categories: List<CategoryType> = emptyList(),
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
        amountFormatPreferences,
        dateFormatPattern,
        timeFormat,
        categories,
        transactionCardCustomizationSettings
    ) {
        calendarViewModel.updateInputs(
            transactions = transactions,
            categories = categories,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            customizationSettings = transactionCardCustomizationSettings
        )
    }
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, end = Dimens.ScreenPadding)
            ) {
                AppHeader(title = "Calendar", onBackClick = onBackClick)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 20.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    GatedAction(
                        feature = Feature.CALENDAR_YEAR_VIEW,
                        displayName = "Calendar Year View",
                        onAction = { calendarViewModel.setYearView(true) }
                    ) { status, onClick ->
                        val isYearLocked = status !is AccessStatus.Granted
                        LaunchedEffect(isYearLocked, uiState.isYearView) {
                            if (isYearLocked && uiState.isYearView) {
                                calendarViewModel.setYearView(false)
                            }
                        }
                        AnimatedTabSwitcher(
                            items = listOf(
                                TabItem(false, "Month"),
                                TabItem(
                                    id = true,
                                    label = "Year",
                                    isLocked = isYearLocked,
                                    onLockedClick = { onClick() }
                                )
                            ),
                            selectedItemId = uiState.isYearView,
                            onItemSelected = { isYearView -> calendarViewModel.setYearView(isYearView) }
                        )
                    }
                }

                item {
                    AnimatedContent(
                        targetState = uiState.isYearView,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                        },
                        label = "calendar_view_mode_transition"
                    ) { isYearView ->
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            if (isYearView) {
                                AnimatedContent(
                                    targetState = uiState.displayedYear,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(300)) togetherWith
                                            fadeOut(animationSpec = tween(300))
                                    },
                                    label = "year_navigation_transition"
                                ) { targetYear ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalSwipe(
                                                key = targetYear,
                                                onSwipeLeft = calendarViewModel::goToNextYear,
                                                onSwipeRight = calendarViewModel::goToPreviousYear
                                            ),
                                        verticalArrangement = Arrangement.spacedBy(18.dp)
                                    ) {
                                        GatedAction(
                                            feature = Feature.CALENDAR_DIRECT_YEAR_PICKER,
                                            displayName = "Calendar Year Picker",
                                            onAction = { isYearPickerVisible = true }
                                        ) { status, onClick ->
                                            YearHeading(
                                                year = targetYear,
                                                isPickerLocked = status !is AccessStatus.Granted,
                                                onPreviousYear = calendarViewModel::goToPreviousYear,
                                                onNextYear = calendarViewModel::goToNextYear,
                                                onTodayClick = calendarViewModel::jumpToToday,
                                                onOpenYearPicker = {
                                                    if (status is AccessStatus.Granted) {
                                                        isYearPickerVisible = true
                                                    } else {
                                                        onClick()
                                                    }
                                                }
                                            )
                                        }

                                        AnnualSummaryCard(
                                            totalIncome = uiState.yearlyIncomeLabel,
                                            totalExpense = uiState.yearlyExpenseLabel
                                        )

                                        YearSummaryGrid(
                                            summaries = uiState.yearSummaries,
                                            onMonthClick = { summary ->
                                                calendarViewModel.selectMonth(createDate(uiState.displayedYear, summary.monthIndex, 1))
                                                calendarViewModel.setYearView(false)
                                            }
                                        )
                                    }
                                }
                            } else {
                                AnimatedContent(
                                    targetState = uiState.displayedMonthStart,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(300)) togetherWith
                                            fadeOut(animationSpec = tween(300))
                                    },
                                    label = "month_navigation_transition"
                                ) { targetMonthStart ->
                                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                        GatedAction(
                                            feature = Feature.CALENDAR_DIRECT_MONTH_PICKER,
                                            displayName = "Calendar Month Picker",
                                            onAction = { isMonthYearPickerVisible = true }
                                        ) { status, onClick ->
                                            MonthHeading(
                                                monthStart = targetMonthStart,
                                                isPickerLocked = status !is AccessStatus.Granted,
                                                onPreviousMonth = calendarViewModel::goToPreviousMonth,
                                                onNextMonth = calendarViewModel::goToNextMonth,
                                                onTodayClick = calendarViewModel::jumpToToday,
                                                onOpenPicker = {
                                                    if (status is AccessStatus.Granted) {
                                                        isMonthYearPickerVisible = true
                                                    } else {
                                                        onClick()
                                                    }
                                                }
                                            )
                                        }

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
                                }

                                DailyTotalsRow(
                                    expenseLabel = uiState.selectedDayExpenseLabel,
                                    incomeLabel = uiState.selectedDayIncomeLabel
                                )

                                TransactionSectionHeader(
                                    selectedDayTitle = uiState.selectedDayTitle
                                )

                                if (uiState.selectedDayTransactions.isEmpty()) {
                                    EmptyTransactionsCard(
                                        message = uiState.emptyTransactionsMessage
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                        uiState.selectedDayTransactions.forEach { transaction ->
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
private fun MonthHeading(
    monthStart: Long,
    isPickerLocked: Boolean,
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

            Row(
                modifier = Modifier.clickable(onClick = onOpenPicker),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = calendarMonthTitle(monthStart),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Jump to date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                if (isPickerLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Month picker locked",
                        tint = MaterialTheme.colorScheme.featureGateLock,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Today",
            color = MaterialTheme.colorScheme.primary,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(standardCardGradient())
            .horizontalSwipe(
                key = days to selectedDate,
                onSwipeLeft = onSwipeNext,
                onSwipeRight = onSwipePrevious
            )
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
                            5 -> MaterialTheme.colorScheme.tertiary
                            6 -> MaterialTheme.colorScheme.expense
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayNumber.toString(),
                color = when {
                    selected -> MaterialTheme.colorScheme.onPrimary
                    day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.outline
                },
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            if (day.hasExpense) {
                DotIndicator(color = if (day.isCurrentMonth) MaterialTheme.colorScheme.expense else MaterialTheme.colorScheme.outline.copy(alpha =  0.65f))
            }
            if (day.hasIncome) {
                DotIndicator(color = if (day.isCurrentMonth) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.outline.copy(alpha =  0.65f))
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp
        )

        Text(
            text = selectedDayTitle,
            color = MaterialTheme.colorScheme.onSurface,
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
        categoryLabel = transaction.categoryLabel,
        showTypeLabel = transactionCardCustomizationSettings.showIncomeExpenseLabels,
        showTransactionDate = transactionCardCustomizationSettings.showTransactionDate,
        showPaymentMethod = transactionCardCustomizationSettings.showPaymentMethod,
        showTransactionTime = transactionCardCustomizationSettings.showTransactionTime,
        showCategoryIcon = transactionCardCustomizationSettings.showCategoryIcon,
        showCategoryLabel = transactionCardCustomizationSettings.showCategoryLabel,
        onClick = onClick
    )
}

@Composable
private fun EmptyTransactionsCard(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(standardCardGradient())
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "No transactions found",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun YearHeading(
    year: Int,
    isPickerLocked: Boolean,
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularNavButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft, onClick = onPreviousYear)
            Row(
                modifier = Modifier.clickable(onClick = onOpenYearPicker),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = year.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Jump to year",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                if (isPickerLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Year picker locked",
                        tint = MaterialTheme.colorScheme.featureGateLock,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            CircularNavButton(icon = Icons.AutoMirrored.Filled.KeyboardArrowRight, onClick = onNextYear)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                SummaryStat(
                    label = "TOTAL EXPENSES",
                    value = totalExpense,
                    valueColor = MaterialTheme.colorScheme.onSurface
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier.size(7.dp).clip(CircleShape)
                        .background(if (summary.isProjection) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.secondary)
                )
            }

            if (summary.isProjection || (summary.income == 0.0 && summary.expense == 0.0)) {
                Spacer(modifier = Modifier.height(38.dp))
            } else {
                SummaryRow(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    label = summary.expenseLabel,
                    color = getAmountColor(2)
                )
                SummaryRow(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = summary.incomeLabel,
                    color = getAmountColor(1)
                )
                SummaryRow(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = summary.netLabel,
                    color = if (summary.net < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    isBold = true,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    icon: ImageVector,
    label: String,
    color: Color,
    isBold: Boolean = false,
    fontSize: TextUnit = 12.sp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.8f),
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun CircularNavButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun MonthYearPickerDialog(
    initialMonthStart: Long,
    yearRange: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var tempDate by remember { mutableStateOf(initialMonthStart) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Month & Year",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                WheelDateTimePicker(
                    initialDateMillis = initialMonthStart,
                    showDay = false,
                    showMonth = true,
                    showDate = true,
                    showTime = false,
                    yearRange = yearRange,
                    onDateChanged = { _, month, year, _, _, _ ->
                        tempDate = createDate(year, month, 1)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempDate) }) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun YearPickerDialog(
    initialYear: Int,
    yearRange: IntRange,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var tempYear by remember { mutableIntStateOf(initialYear) }
    val initialDateMillis = remember(initialYear) { createDate(initialYear, 0, 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Year",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                WheelDateTimePicker(
                    initialDateMillis = initialDateMillis,
                    showDay = false,
                    showMonth = false,
                    showDate = true,
                    showTime = false,
                    yearRange = yearRange,
                    onDateChanged = { _, _, year, _, _, _ ->
                        tempYear = year
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempYear) }) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CalendarScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        CalendarScreen()
    }
}
