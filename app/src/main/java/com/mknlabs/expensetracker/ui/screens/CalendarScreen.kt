package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.TextUnit
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.ui.models.CalendarDayUi
import com.mknlabs.expensetracker.ui.models.CalendarMonthFinancialSummaryUi
import com.mknlabs.expensetracker.ui.models.TransactionCardItemUi
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.CurrentPeriodIndicator
import com.mknlabs.expensetracker.ui.components.GatedAction
import com.mknlabs.expensetracker.ui.components.TransactionCard
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.expense
import com.mknlabs.expensetracker.ui.theme.featureGateLock
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.income
import com.mknlabs.expensetracker.ui.horizontalSwipe
import com.mknlabs.expensetracker.utils.getAmountColor
import com.mknlabs.expensetracker.ui.viewmodels.CalendarViewModel
import com.mknlabs.expensetracker.ui.viewmodels.calendarMonthTitle
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.ui.components.WheelDateTimePicker
import com.mknlabs.expensetracker.ui.models.TabItem
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.monetization.AdPlacement
import java.util.Calendar

// Theme colors are now derived from MaterialTheme.colorScheme

private val dayNames = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

@Composable
fun CalendarScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    transactions: List<Transaction> = transactionList,
    categories: List<CategoryType> = emptyList(),
    monthStartDay: Int = 1,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    onBackClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    isAdsEnabled: Boolean = false,
    isProUser: Boolean = false
) {
    val calendarViewModel: CalendarViewModel = hiltViewModel()

    // Re-anchor the calendar to the current date whenever the screen resumes
    // (e.g. the date changed while the app was in the background).
    LifecycleResumeEffect(Unit) {
        calendarViewModel.refreshToday()
        onPauseOrDispose { }
    }

    androidx.compose.runtime.LaunchedEffect(
        transactions,
        currencyId,
        amountFormatPreferences,
        dateFormatPattern,
        timeFormat,
        categories,
        monthStartDay,
        transactionCardCustomizationSettings
    ) {
        calendarViewModel.updateInputs(
            transactions = transactions,
            categories = categories,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            customizationSettings = transactionCardCustomizationSettings,
            monthStartDay = monthStartDay
        )
    }
    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreenContent(
        uiState = uiState,
        isAdsEnabled = isAdsEnabled,
        isProUser = isProUser,
        onBackClick = onBackClick,
        onTransactionClick = onTransactionClick,
        onSetYearView = { calendarViewModel.setYearView(it) },
        onGoToNextYear = { calendarViewModel.goToNextYear() },
        onGoToPreviousYear = { calendarViewModel.goToPreviousYear() },
        onJumpToToday = { calendarViewModel.jumpToToday() },
        onSelectYear = { calendarViewModel.selectYear(it) },
        onSelectMonth = { calendarViewModel.selectMonth(it) },
        onGoToPreviousMonth = { calendarViewModel.goToPreviousMonth() },
        onGoToNextMonth = { calendarViewModel.goToNextMonth() },
        onSelectDay = { calendarViewModel.selectDay(it) }
    )
}

@Composable
private fun CalendarScreenContent(
    uiState: com.mknlabs.expensetracker.ui.viewmodels.CalendarScreenUiState,
    isAdsEnabled: Boolean,
    isProUser: Boolean = false,
    onBackClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onSetYearView: (Boolean) -> Unit,
    onGoToNextYear: () -> Unit,
    onGoToPreviousYear: () -> Unit,
    onJumpToToday: () -> Unit,
    onSelectYear: (Int) -> Unit,
    onSelectMonth: (Long) -> Unit,
    onGoToPreviousMonth: () -> Unit,
    onGoToNextMonth: () -> Unit,
    onSelectDay: (CalendarDayUi) -> Unit
) {
    var isMonthYearPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isYearPickerVisible by rememberSaveable { mutableStateOf(false) }
    // Medium+ windows get the month grid and selected-day transactions side-by-side.
    val isWide = LocalAppWindowInfo.current.isWide

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    AppHeader(title = stringResource(id = R.string.title_calendar), onBackClick = onBackClick)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 20.dp, bottom = 130.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        CurrentPeriodIndicator(
                            startMillis = uiState.currentPeriodStartMillis,
                            endMillis = uiState.currentPeriodEndMillis,
                            monthStartDay = uiState.monthStartDay
                        )
                    }
                    item {
                        GatedAction(
                            feature = Feature.CALENDAR_YEAR_VIEW,
                            displayName = stringResource(id = R.string.label_calendar_year_view),
                            onAction = { onSetYearView(true) }
                        ) { status, onClick ->
                            val isYearLocked = status !is AccessStatus.Granted
                            LaunchedEffect(isYearLocked, uiState.isYearView) {
                                if (isYearLocked && uiState.isYearView) {
                                    onSetYearView(false)
                                }
                            }
                            AnimatedTabSwitcher(
                                items = listOf(
                                    TabItem(false, stringResource(id = R.string.label_month_1)),
                                    TabItem(
                                        id = true,
                                        label = stringResource(id = R.string.label_year),
                                        isLocked = isYearLocked,
                                        onLockedClick = { onClick() }
                                    )
                                ),
                                selectedItemId = uiState.isYearView,
                                onItemSelected = { isYearView -> onSetYearView(isYearView) }
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
                                                    onSwipeLeft = onGoToNextYear,
                                                    onSwipeRight = onGoToPreviousYear
                                                ),
                                            verticalArrangement = Arrangement.spacedBy(18.dp)
                                        ) {
                                            GatedAction(
                                                feature = Feature.CALENDAR_DIRECT_YEAR_PICKER,
                                                displayName = stringResource(id = R.string.label_calendar_year_picker),
                                                onAction = { isYearPickerVisible = true }
                                            ) { status, onClick ->
                                                YearHeading(
                                                    year = targetYear,
                                                    isPickerLocked = status !is AccessStatus.Granted,
                                                    onPreviousYear = onGoToPreviousYear,
                                                    onNextYear = onGoToNextYear,
                                                    onTodayClick = onJumpToToday,
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
                                                    val calendar = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, uiState.displayedYear)
                                                        set(Calendar.MONTH, summary.monthIndex)
                                                        set(Calendar.DAY_OF_MONTH, 1)
                                                        set(Calendar.HOUR_OF_DAY, 0)
                                                        set(Calendar.MINUTE, 0)
                                                        set(Calendar.SECOND, 0)
                                                        set(Calendar.MILLISECOND, 0)
                                                    }
                                                    onSelectMonth(calendar.timeInMillis)
                                                    onSetYearView(false)
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    // Month view: calendar grid + selected-day details.
                                    // Wide windows render them side-by-side (two panes);
                                    // compact stays a single scroll column (unchanged).
                                    val monthCalendarBlock: @Composable () -> Unit = {
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
                                                    displayName = stringResource(id = R.string.label_calendar_month_picker),
                                                    onAction = { isMonthYearPickerVisible = true }
                                                ) { status, onClick ->
                                                    MonthHeading(
                                                        monthStart = targetMonthStart,
                                                        isPickerLocked = status !is AccessStatus.Granted,
                                                        onPreviousMonth = onGoToPreviousMonth,
                                                        onNextMonth = onGoToNextMonth,
                                                        onTodayClick = onJumpToToday,
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
                                                        onSelectDay(day)
                                                    },
                                                    onSwipePrevious = onGoToPreviousMonth,
                                                    onSwipeNext = onGoToNextMonth
                                                )
                                            }
                                        }
                                    }

                                    val dayDetailsBlock: @Composable () -> Unit = {
                                        DailyTotalsRow(
                                            expenseLabel = uiState.selectedDayExpenseLabel.asString(),
                                            incomeLabel = uiState.selectedDayIncomeLabel.asString()
                                        )

                                        AdContainer(
                                            isAdsEnabled = isAdsEnabled,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            NativeAdCard(placement = AdPlacement.BUDGET_CALENDAR)
                                        }

                                        TransactionSectionHeader(
                                            selectedDayTitle = uiState.selectedDayTitle
                                        )
                                        if (uiState.selectedDayTransactions.isEmpty()) {
                                            EmptyTransactionsCard(
                                                message = uiState.emptyTransactionsMessage.asString()
                                            )
                                        } else {
                                            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                                                uiState.selectedDayTransactions.forEach { transaction ->
                                                    CalendarTransactionCard(
                                                        transaction = transaction,
                                                        transactionCardCustomizationSettings = uiState.customizationSettings,
                                                        isProUser = isProUser,
                                                        onClick = { onTransactionClick(transaction.transaction) }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (isWide) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(18.dp)
                                            ) {
                                                monthCalendarBlock()
                                            }
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(18.dp)
                                            ) {
                                                dayDetailsBlock()
                                            }
                                        }
                                    } else {
                                        monthCalendarBlock()
                                        dayDetailsBlock()
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
                onSelectMonth(newMonthStart)
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
                onSelectYear(newYear)
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
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = stringResource(id = R.string.content_desc_jump_to_date),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                if (isPickerLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(id = R.string.content_desc_month_picker_locked),
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
            text = stringResource(id = R.string.label_today),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium
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
                        style = MaterialTheme.typography.labelSmall,
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
        modifier = modifier.heightIn(min = 56.dp).clip(RoundedCornerShape(18.dp))
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
                style = if (selected) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium
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
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = incomeLabel,
            color = getAmountColor(1),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun TransactionSectionHeader(
    selectedDayTitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.label_transactions),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            text = selectedDayTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun CalendarTransactionCard(
    transaction: TransactionCardItemUi,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings,
    isProUser: Boolean = false,
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
        showNoteTooltip = isProUser,
        isProUser = isProUser,
        isRecurring = transaction.isRecurring,
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
                text = stringResource(id = R.string.label_no_transactions_found),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
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
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = stringResource(id = R.string.content_desc_jump_to_year),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                if (isPickerLocked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(id = R.string.content_desc_year_picker_locked),
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
                    label = stringResource(id = R.string.label_total_income),
                    value = totalIncome,
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                SummaryStat(
                    label = stringResource(id = R.string.label_total_expenses),
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
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.titleMedium
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
                    text = summary.label.asString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
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
                    isBold = true
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

private fun startOfMonth(timestamp: Long, monthStartDay: Int = 1): Long =
    com.mknlabs.expensetracker.utils.CustomMonthUtils.getStartOfCustomMonth(timestamp, monthStartDay)

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
                text = stringResource(id = R.string.title_choose_month_year),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
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
                Text(stringResource(id = R.string.label_apply), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel_1))
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
                text = stringResource(id = R.string.label_choose_year),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge
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
                Text(stringResource(id = R.string.label_apply), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel_1))
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
        CalendarScreenContent(
            uiState = com.mknlabs.expensetracker.ui.viewmodels.CalendarScreenUiState(),
            isAdsEnabled = true,
            onBackClick = {},
            onTransactionClick = {},
            onSetYearView = {},
            onGoToNextYear = {},
            onGoToPreviousYear = {},
            onJumpToToday = {},
            onSelectYear = {},
            onSelectMonth = {},
            onGoToPreviousMonth = {},
            onGoToNextMonth = {},
            onSelectDay = {}
        )
    }
}
