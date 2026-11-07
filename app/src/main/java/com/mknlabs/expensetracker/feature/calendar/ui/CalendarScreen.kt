package com.mknlabs.expensetracker.feature.calendar.ui

import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
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

import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock

import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
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
internal fun CalendarScreenContent(
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
                    .appScreenBackground()
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
                        .weight(1f),
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
                                    label = stringResource(id = R.string.label_today),
                                    isSelected = false,
                                    onClick = onJumpToToday,
                                    modifier = Modifier.weight(1f)
                                )
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



