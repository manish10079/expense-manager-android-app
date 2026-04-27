package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun WheelDateTimePicker(
    modifier: Modifier = Modifier,
    initialDateMillis: Long = System.currentTimeMillis(),
    showDay: Boolean = true,
    showMonth: Boolean = true,
    showDate: Boolean = true,
    showTime: Boolean = false,
    yearRange: IntRange = 1900..2100,
    onDateChanged: (Int, Int, Int, Int, Int, String) -> Unit
) {
    val calendar = remember(initialDateMillis) {
        Calendar.getInstance().apply { timeInMillis = initialDateMillis }
    }

    // ✅ FIX: proper 24 → 12 conversion
    val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
    val initialHour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
    val initialAmPm = if (hour24 < 12) "AM" else "PM"

    var selectedYear by remember(initialDateMillis) { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember(initialDateMillis) { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember(initialDateMillis) { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    var selectedHour by remember(initialDateMillis) { mutableIntStateOf(initialHour12) }
    var selectedMinute by remember(initialDateMillis) { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var selectedAmPm by remember(initialDateMillis) { mutableStateOf(initialAmPm) }

    val years = remember { yearRange.toList() }
    val months = remember { (0..11).toList() }
    val days = remember { (1..31).toList() }
    val hours = remember { (1..12).toList() }
    val minutes = remember { (0..59).toList() }
    val amPmOptions = remember { listOf("AM", "PM") }

    // ✅ FIX: precomputed month names
    val monthNames = remember {
        (0..11).map {
            Calendar.getInstance()
                .apply { set(Calendar.MONTH, it) }
                .getDisplayName(Calendar.LONG, Calendar.SHORT, Locale.getDefault()) ?: ""
        }
    }

    LaunchedEffect(
        selectedDay,
        selectedMonth,
        selectedYear,
        selectedHour,
        selectedMinute,
        selectedAmPm
    ) {
        onDateChanged(
            selectedDay,
            selectedMonth,
            selectedYear,
            selectedHour,
            selectedMinute,
            selectedAmPm
        )
    }

    Row(modifier = modifier.fillMaxWidth()) {

        if (showDate && showDay) {
            WheelPicker(
                modifier = Modifier.weight(1f),
                items = days,
                initialIndex = (selectedDay - 1).coerceIn(0, 30),
                onItemSelected = { selectedDay = it },
                label = { it.toString().padStart(2, '0') },
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showDate && showMonth) {
            WheelPicker(
                modifier = Modifier.weight(1.8f),
                items = months,
                initialIndex = selectedMonth,
                onItemSelected = { selectedMonth = it },
                label = { "${(it + 1).toString().padStart(2, '0')} ${monthNames[it]}" },
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showDate) {
            val yearIndex = years.indexOf(selectedYear).let {
                if (it == -1) years.size / 2 else it
            }

            WheelPicker(
                modifier = Modifier.weight(1.2f),
                items = years,
                initialIndex = yearIndex,
                onItemSelected = { selectedYear = it },
                label = { it.toString() },
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showTime) {
            Spacer(modifier = Modifier.width(8.dp))

            WheelPicker(
                modifier = Modifier.weight(1f),
                items = hours,
                initialIndex = hours.indexOf(selectedHour).coerceAtLeast(0),
                onItemSelected = { selectedHour = it },
                label = { it.toString().padStart(2, '0') },
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            WheelPicker(
                modifier = Modifier.weight(1f),
                items = minutes,
                initialIndex = selectedMinute,
                onItemSelected = { selectedMinute = it },
                label = { it.toString().padStart(2, '0') },
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            WheelPicker(
                modifier = Modifier.weight(1f),
                items = amPmOptions,
                initialIndex = amPmOptions.indexOf(selectedAmPm),
                onItemSelected = { selectedAmPm = it },
                label = { it },
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
