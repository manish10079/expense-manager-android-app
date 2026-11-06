package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient

internal const val KEYPAD_DELETE_KEY = "delete"

@Composable
internal fun NumericKeypad(
    compact: Boolean,
    onKeyPressed: (String) -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", KEYPAD_DELETE_KEY)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
            ) {
                row.forEach { key ->
                    KeypadKey(
                        label = key,
                        compact = compact,
                        onClick = { onKeyPressed(key) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun KeypadToggle(
    expanded: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (MaterialTheme.colorScheme.isDark) standardCardGradient() else SolidColor(MaterialTheme.colorScheme.surface))
            .border(
                width = 1.dp,
                color = if (MaterialTheme.colorScheme.isDark) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 16.dp else 18.dp,
                vertical = if (compact) 12.dp else 14.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.label_amount_keypad),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (compact) 15.sp else 16.sp
                )
            )
            Text(
                text = stringResource(if (expanded) R.string.label_tap_to_hide_keypad else R.string.label_tap_to_slide_up_keypad),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Box(
            modifier = Modifier
                .size(if (compact) 34.dp else 38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (expanded) {
                    Icons.Filled.KeyboardArrowDown
                } else {
                    Icons.Filled.KeyboardArrowUp
                },
                contentDescription = stringResource(if (expanded) R.string.desc_collapse_keypad else R.string.desc_expand_keypad),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(if (compact) 20.dp else 22.dp)
            )
        }
    }
}

@Composable
internal fun KeypadKey(
    label: String,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(if (compact) 46.dp else 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (MaterialTheme.colorScheme.isDark) standardCardGradient() else SolidColor(MaterialTheme.colorScheme.surface))
            .border(
                width = 1.dp,
                color = if (MaterialTheme.colorScheme.isDark) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == KEYPAD_DELETE_KEY) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = stringResource(R.string.desc_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(if (compact) 22.dp else 24.dp)
            )
        } else {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = if (compact) 24.sp else 26.sp
                )
            )
        }
    }
}
