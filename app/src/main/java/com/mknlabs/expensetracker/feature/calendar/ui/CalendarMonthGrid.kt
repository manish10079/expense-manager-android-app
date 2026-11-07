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


@Composable
internal fun MonthHeading(
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
internal fun MonthCalendarCard(
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
        colors = AppCardDefaults.colors(
            if (MaterialTheme.colorScheme.isDark) Color(0xFF101019) else Color.Transparent
        ),
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
internal fun DayCell(
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
internal fun DotIndicator(color: Color) {
    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
}

