package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${stringResource(R.string.label_current_period)}  $periodText",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
