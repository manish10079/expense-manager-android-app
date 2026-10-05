package com.mknlabs.expensetracker.feature.calendar.ui

import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.ui.draw.alpha
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.brandGradient

import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.TextUnit
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.core.ui.models.CalendarDayUi
import com.mknlabs.expensetracker.core.ui.models.CalendarMonthFinancialSummaryUi
import com.mknlabs.expensetracker.core.ui.models.TransactionCardItemUi
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.components.PeriodChip
import com.mknlabs.expensetracker.core.ui.components.TransactionCard
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.horizontalSwipe
import com.mknlabs.expensetracker.utils.getAmountColor

import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePicker
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.components.rememberBindAddFabToScroll
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.monetization.AdPlacement
import java.util.Calendar

// Theme colors are now derived from MaterialTheme.colorScheme

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
    uiState: CalendarScreenUiState,
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

    // Primary scroll surface for the calendar screen; its scroll direction
    // drives the standalone add FAB's auto-hide on compact portrait.
    val calendarListState = rememberLazyListState()
    rememberBindAddFabToScroll(calendarListState)
    val enter = rememberSectionEnterAlphas(3)

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
                        .alpha(enter[0])
                ) {
                    AppHeader(title = stringResource(id = R.string.title_calendar), onBackClick = onBackClick)
                }

                LazyColumn(
                    state = calendarListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background),
                    // Top inset is the gap under the AppHeader, so it is deliberately smaller
                    // than the 18.dp between cards, and matched to the Analytics list so both
                    // screens put their first control the same distance below the header.
                    contentPadding = PaddingValues(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 0.dp, bottom = 130.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
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
                            // The calendar's three controls, dressed as the period pills
                            // Analytics wears for its four: Month and Year pick the view (Year
                            // keeping its lock from the gate above), Today jumps to the current
                            // date — whose ViewModel handler also leaves year view, so the tap
                            // lands on this month and Month lights up here behind it. Unlike
                            // Analytics, which sizes its chips to their labels and scrolls
                            // sideways, these three split the row evenly: three short labels
                            // always fit, and a fixed third each keeps the row's shape stable.
                            Row(
                                modifier = Modifier.fillMaxWidth().alpha(enter[1]),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PeriodChip(
                                    label = stringResource(id = R.string.label_month_1),
                                    isSelected = !uiState.isYearView,
                                    onClick = { onSetYearView(false) },
                                    modifier = Modifier.weight(1f)
                                )
                                PeriodChip(
                                    label = stringResource(id = R.string.label_year),
                                    isSelected = uiState.isYearView,
                                    isLocked = isYearLocked,
                                    onClick = { if (isYearLocked) onClick() else onSetYearView(true) },
                                    modifier = Modifier.weight(1f)
                                )
                                PeriodChip(
                                    label = stringResource(id = R.string.label_today),
                                    isSelected = false,
                                    onClick = onJumpToToday,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Box(Modifier.alpha(enter[2])) {
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
                                                    todayDate = uiState.todayDate,
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
                                        Text(
                                            text = uiState.selectedDayTitle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.titleMedium
                                        )

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

                                        Text(
                                            text = stringResource(id = R.string.label_transactions),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall
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
                                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
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
    onOpenPicker: () -> Unit
) {
    // One row: two arrows and the title. Today used to sit in it too, first on a row of its own
    // below and then beside the arrows, but it belongs with Month and Year — the screen's other
    // two controls — and all three now sit together at the top as period chips, in the style
    // Analytics wears for its weeks and months. What is left here navigates and names the month
    // on screen.
    //
    // The title sits in the flexible middle so the equal-width arrows pin it on the screen
    // centre.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularNavButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(id = R.string.content_desc_previous_month),
            onClick = onPreviousMonth
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onOpenPicker),
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
                    tint = MaterialTheme.colorScheme.accentInk,
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

        }

        CircularNavButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(id = R.string.content_desc_next_month),
            onClick = onNextMonth
        )
    }
}

@Composable
private fun MonthCalendarCard(
    days: List<CalendarDayUi>,
    selectedDate: Long,
    todayDate: Long,
    onDaySelected: (CalendarDayUi) -> Unit,
    onSwipePrevious: () -> Unit,
    onSwipeNext: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalSwipe(
                key = days to selectedDate,
                onSwipeLeft = onSwipeNext,
                onSwipeRight = onSwipePrevious
            ),
        // The gradient is the dark surface and the only fill this card has, so the
        // container underneath it stays transparent and no outline is added; light falls
        // back to the standard card, which is what the redesign asks of every hero.
        brush = darkOnlyGradient(standardCardGradient()),
        colors = AppCardDefaults.colors(Color.Transparent),
        shape = AppCardDefaults.shape(26.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title-case weekday initials shared with the analytics charts.
                val dayNames = stringArrayResource(R.array.days_of_week_short)
                dayNames.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        color = when (index) {
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
                            isToday = isSameDay(day.timestamp, todayDate),
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
    isToday: Boolean,
    onClick: () -> Unit
) {
    // Today keeps its mark after the selection moves off it, otherwise picking any other day
    // makes the grid forget where today is. The two states are mutually exclusive because a
    // ring drawn over the selection's own purple fill would not be visible at all.
    val showTodayRing = showsTodayRing(isToday = isToday, isSelected = selected)

    Column(
        modifier = modifier.heightIn(min = 56.dp).clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .then(
                    when {
                        selected -> Modifier.background(brandGradient(), CircleShape)
                        else -> Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0f))
                    }
                )
                .then(
                    if (showTodayRing) {
                        Modifier.border(1.5.dp, brandGradient(), CircleShape)
                    } else {
                        Modifier
                    }
                ),
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
        categoryId = transaction.transaction.categoryId,
        categoryColorHex = transaction.categoryColorHex,
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
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        brush = darkOnlyGradient(standardCardGradient()),
        colors = AppCardDefaults.colors(Color.Transparent),
        shape = AppCardDefaults.shape(24.dp),
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
    onOpenYearPicker: () -> Unit
) {
    // Mirrors [MonthHeading] exactly, including the centred title and the corner-pinned arrows.
    // They are kept identical on purpose: switching between month and year must not look like
    // the header changed shape.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularNavButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(id = R.string.content_desc_previous_year),
            onClick = onPreviousYear
        )

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onOpenYearPicker),
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
                    tint = MaterialTheme.colorScheme.accentInk,
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

        }

        CircularNavButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(id = R.string.content_desc_next_year),
            onClick = onNextYear
        )
    }
}

@Composable
private fun AnnualSummaryCard(
    totalIncome: String,
    totalExpense: String
) {
    AppCard(
        colors = AppCardDefaults.colors(MaterialTheme.colorScheme.surface),
        shape = AppCardDefaults.shape(28.dp),
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
    AppCard(
        onClick = onClick,
        colors = AppCardDefaults.colors(MaterialTheme.colorScheme.surface),
        shape = AppCardDefaults.shape(22.dp),
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
                // The three amounts are one block of readings, so they sit closer to each other
                // (4.dp) than to the month label above them, which keeps the 7.dp above. Grouping
                // them is also what allows that: as siblings of the label they could only be
                // spaced by the column's one arrangement, which would have tightened the label's
                // own separation by the same amount.
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                        color = if (summary.net < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.accentInk,
                        isBold = true
                    )
                }
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
    contentDescription: String,
    onClick: () -> Unit
) {
    // 32.dp is the circle the user sees; the layout around it reserves at least 48.dp, which is
    // the size a touch target has to be to be reliably hittable and the size Material's own icon
    // buttons enforce for themselves. A bare size(32.dp).clickable() opts out of that, and this
    // whole screen's controls were doing exactly that.
    //
    // [contentDescription] is required rather than optional: the icon is the only thing that says
    // what the button does, so without it the button announces nothing useful when focused.
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.accentInk)
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

/**
 * Whether a day cell draws the ring that keeps today identifiable.
 *
 * Only when today is not the selection: the selected cell already fills with the same colour,
 * so drawing the ring too would add nothing a user could see. Pure so the rule is testable
 * without Compose, and shared by every cell rather than repeated at each call site.
 */
internal fun showsTodayRing(isToday: Boolean, isSelected: Boolean): Boolean = isToday && !isSelected

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
            AppTextButton(onClick = { onConfirm(tempDate) }) {
                Text(stringResource(id = R.string.label_apply), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel_1))
            }
        },
        containerColor = MaterialTheme.colorScheme.sheet,
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
            AppTextButton(onClick = { onConfirm(tempYear) }) {
                Text(stringResource(id = R.string.label_apply), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel_1))
            }
        },
        containerColor = MaterialTheme.colorScheme.sheet,
        shape = RoundedCornerShape(28.dp)
    )
}


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CalendarScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        CalendarScreenContent(
            uiState = CalendarScreenUiState(),
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
