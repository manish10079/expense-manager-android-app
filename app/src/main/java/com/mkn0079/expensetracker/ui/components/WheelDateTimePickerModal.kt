package com.mkn0079.expensetracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.utils.validateAndCalculateTimestamp
import com.mkn0079.expensetracker.utils.PickerResult
import java.util.*

enum class WheelPickerMode {
    SINGLE_DATE,
    SINGLE_TIME,
    DATE_TIME,
    MONTH_YEAR,
    YEAR_ONLY,
    DATE_RANGE
}

private enum class RangeTab { FROM, TO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelDateTimePickerModal(
    mode: WheelPickerMode,
    initialStartMillis: Long,
    initialEndMillis: Long? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (Long, Long?) -> Unit
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    val fromCal = remember(initialStartMillis) {
        Calendar.getInstance().apply { timeInMillis = initialStartMillis }
    }
    val toCal = remember(initialEndMillis, initialStartMillis) {
        Calendar.getInstance().apply { timeInMillis = initialEndMillis ?: initialStartMillis }
    }

    // From state - resets when initialStartMillis changes
    var fromDay by remember(initialStartMillis) { mutableIntStateOf(fromCal.get(Calendar.DAY_OF_MONTH)) }
    var fromMonth by remember(initialStartMillis) { mutableIntStateOf(fromCal.get(Calendar.MONTH)) }
    var fromYear by remember(initialStartMillis) { mutableIntStateOf(fromCal.get(Calendar.YEAR)) }
    var fromHour by remember(initialStartMillis) {
        mutableIntStateOf(fromCal.get(Calendar.HOUR_OF_DAY).let { if (it % 12 == 0) 12 else it % 12 })
    }
    var fromMin by remember(initialStartMillis) { mutableIntStateOf(fromCal.get(Calendar.MINUTE)) }
    var fromAmPm by remember(initialStartMillis) {
        mutableStateOf(if (fromCal.get(Calendar.HOUR_OF_DAY) < 12) "AM" else "PM")
    }

    // To state (range only)
    var toDay by remember(initialEndMillis, initialStartMillis) { mutableIntStateOf(toCal.get(Calendar.DAY_OF_MONTH)) }
    var toMonth by remember(initialEndMillis, initialStartMillis) { mutableIntStateOf(toCal.get(Calendar.MONTH)) }
    var toYear by remember(initialEndMillis, initialStartMillis) { mutableIntStateOf(toCal.get(Calendar.YEAR)) }

    var activeTab by remember { mutableStateOf(RangeTab.FROM) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val showDate = mode != WheelPickerMode.SINGLE_TIME
    val showTime = mode == WheelPickerMode.SINGLE_TIME || mode == WheelPickerMode.DATE_TIME
    val showDay = mode != WheelPickerMode.MONTH_YEAR && mode != WheelPickerMode.YEAR_ONLY
    val showMonth = mode != WheelPickerMode.YEAR_ONLY

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (mode) {
                    WheelPickerMode.SINGLE_DATE -> stringResource(id = R.string.title_select_date)
                    WheelPickerMode.SINGLE_TIME -> stringResource(id = R.string.title_select_time)
                    WheelPickerMode.DATE_TIME   -> stringResource(id = R.string.title_select_date_time)
                    WheelPickerMode.MONTH_YEAR  -> stringResource(id = R.string.title_select_month)
                    WheelPickerMode.YEAR_ONLY   -> stringResource(id = R.string.title_select_year)
                    WheelPickerMode.DATE_RANGE  -> stringResource(id = R.string.title_select_date_range)
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (mode == WheelPickerMode.DATE_RANGE) {
                RangeTabs(selectedTab = activeTab, onTabSelected = { activeTab = it })
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Picker area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp)
            ) {
                if (mode == WheelPickerMode.DATE_RANGE) {
                    if (activeTab == RangeTab.FROM) {
                        WheelDateTimePicker(
                            initialDateMillis = initialStartMillis,
                            showDate = true,
                            showTime = false,
                            onDateChanged = { d, m, y, _, _, _ ->
                                fromDay = d; fromMonth = m; fromYear = y
                                errorMessage = null
                            }
                        )
                    } else {
                        WheelDateTimePicker(
                            initialDateMillis = initialEndMillis ?: now,
                            showDate = true,
                            showTime = false,
                            onDateChanged = { d, m, y, _, _, _ ->
                                toDay = d; toMonth = m; toYear = y
                                errorMessage = null
                            }
                        )
                    }
                } else {
                    WheelDateTimePicker(
                        initialDateMillis = initialStartMillis,
                        showDay = showDay,
                        showMonth = showMonth,
                        showDate = showDate,
                        showTime = showTime,
                        onDateChanged = { d, m, y, h, min, ap ->
                            fromDay = d; fromMonth = m; fromYear = y
                            fromHour = h; fromMin = min; fromAmPm = ap
                            errorMessage = null
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Error message slot
            Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismissRequest, modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val normalizedFromDay = when (mode) {
                            WheelPickerMode.MONTH_YEAR,
                            WheelPickerMode.YEAR_ONLY -> 1
                            else -> fromDay
                        }
                        val normalizedFromMonth = when (mode) {
                            WheelPickerMode.YEAR_ONLY -> Calendar.JANUARY
                            else -> fromMonth
                        }
                        val fromRes = validateAndCalculateTimestamp(
                            day = normalizedFromDay,
                            month = normalizedFromMonth,
                            year = fromYear,
                            hour = fromHour, minute = fromMin, amPm = fromAmPm,
                            showDate = showDate, showTime = showTime
                        )

                        if (fromRes.error != null) {
                            errorMessage = context.getString(R.string.error_invalid_date_month)
                            if (mode == WheelPickerMode.DATE_RANGE) activeTab = RangeTab.FROM
                        } else {
                            if (mode == WheelPickerMode.DATE_RANGE) {
                                val toRes = validateAndCalculateTimestamp(
                                    day = toDay, month = toMonth, year = toYear,
                                    hour = 0, minute = 0, amPm = "AM",
                                    showDate = true, showTime = false
                                )
                                if (toRes.error != null) {
                                    errorMessage = context.getString(R.string.error_invalid_date_month)
                                    activeTab = RangeTab.TO
                                } else {
                                    val start = minOf(fromRes.timestamp!!, toRes.timestamp!!)
                                    val end   = maxOf(fromRes.timestamp!!, toRes.timestamp!!)
                                    onConfirm(start, end)
                                }
                            } else {
                                onConfirm(fromRes.timestamp!!, null)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (mode == WheelPickerMode.DATE_RANGE) stringResource(id = R.string.btn_apply_range) else stringResource(id = R.string.btn_confirm),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RangeTabs(selectedTab: RangeTab, onTabSelected: (RangeTab) -> Unit) {
    val tabs = RangeTab.entries
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val selectedIndex = tabs.indexOf(selectedTab)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { containerWidthPx = it.width }
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        val tabWidth = with(density) { (containerWidthPx.toDp() - 8.dp) / tabs.size }
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "tab_offset"
        )

        if (containerWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "text_color"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tab == RangeTab.FROM) stringResource(id = R.string.label_from) else stringResource(id = R.string.label_to),
                        color = contentColor,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}
