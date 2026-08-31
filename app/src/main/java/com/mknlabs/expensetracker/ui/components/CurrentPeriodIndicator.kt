package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.utils.CustomMonthUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays the current billing period label (e.g. "Aug 15 – Sep 14, 2026").
 *
 * Only visible when [monthStartDay] != 1 (standard calendar month).
 *
 * @param startMillis epoch millis for period start (inclusive).
 * @param endMillis epoch millis for period end (inclusive).
 * @param monthStartDay custom month start day (1–28).
 */
@Composable
fun CurrentPeriodIndicator(
    startMillis: Long,
    endMillis: Long,
    monthStartDay: Int,
    modifier: Modifier = Modifier
) {
    if (monthStartDay == 1) return

    val periodText = remember(startMillis, endMillis, monthStartDay) {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val startStr = dateFormat.format(Date(startMillis))
        val endStr = dateFormat.format(Date(endMillis))
        val endYear = yearFormat.format(Date(endMillis))
        "$startStr – $endStr, $endYear"
    }

    Row(
        modifier = modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.DateRange,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = stringResource(R.string.label_current_period),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = periodText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
