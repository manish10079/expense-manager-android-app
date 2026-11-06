package com.mknlabs.expensetracker.feature.budget.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.budgetNearLimit
import com.mknlabs.expensetracker.core.ui.theme.budgetOnTrack
import com.mknlabs.expensetracker.core.ui.theme.budgetOver
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient

/**
 * The three budget health states.
 *
 * Deliberately separate from [budgetAccentColor], which reads as the obvious place to
 * put these colours but serves two unrelated meanings of [BudgetAccent] at once: a
 * budget's health, and a recurring expense's cadence. Repointing that function would
 * paint "Weekly" amber and "Monthly" green along with the budgets. Only the five
 * budget-health call sites resolve through here.
 *
 * This also corrects an inversion. The near-limit state resolved through the scheme's
 * `tertiary`, which the light scheme sets to #0D9488 — the same teal that means "on
 * track" — so a warning was drawn in the success colour, on the bar and on its label
 * and on its icon at once.
 *
 * The colour never carries the state alone: each bar is accompanied by "SAFE" /
 * "NEAR LIMIT" / "EXCEEDED", which is what makes the meaning survive red-green
 * deficiency, where amber and red are only 4.4 ΔE76 apart.
 */
@Composable
internal fun budgetHealthColor(accent: BudgetAccent): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when (accent) {
        BudgetAccent.Primary -> colorScheme.budgetOnTrack
        BudgetAccent.Warning -> colorScheme.budgetNearLimit
        BudgetAccent.Overspent -> colorScheme.budgetOver
        // Unreachable for a budget, which only ever gets the three above. Left to the
        // cadence resolver rather than duplicated so the `when` stays exhaustive.
        else -> budgetAccentColor(accent)
    }
}

@Composable
internal fun budgetAccentColor(accent: BudgetAccent): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when (accent) {
        BudgetAccent.Primary -> colorScheme.accentInk
        BudgetAccent.Warning -> colorScheme.tertiary
        BudgetAccent.Overspent -> colorScheme.error
        BudgetAccent.Disabled -> colorScheme.outline
        BudgetAccent.Daily -> colorScheme.income
        BudgetAccent.Yearly -> colorScheme.secondary
    }
}

@Composable
internal fun BoxScope.BudgetGlow() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 72.dp)
            .size(width = 260.dp, height = 180.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.accentInk.copy(alpha = 0.26f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0f)
                    )
                )
            )
    )
}

@Composable
internal fun BudgetInlineRow(
    title: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            ),
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.ExtraBold
        )
    )
}

@Composable
internal fun EmptySectionCard(message: String) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        brush = darkOnlyGradient(standardCardGradient()),
        shape = AppCardDefaults.shape(20.dp),
        contentPadding = PaddingValues(18.dp),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
internal fun BudgetProgressBar(
    progress: Float,
    accent: Brush,
    modifier: Modifier = Modifier,
    /** Defaults are the hero summary bar's 12dp rail; a card passes the goals styling. */
    height: Dp = 12.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
) {
    val view = LocalView.current
    var visibleOnScreen by remember { mutableStateOf(false) }
    val targetProgress = if (visibleOnScreen) progress.coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = if (visibleOnScreen) {
            tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        } else {
            snap()
        },
        label = "budget_progress_animation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInWindow()
                val window = Rect(0f, 0f, view.width.toFloat(), view.height.toFloat())
                val onScreen = bounds.width > 0f &&
                    bounds.height > 0f &&
                    bounds.overlaps(window)
                if (onScreen != visibleOnScreen) {
                    visibleOnScreen = onScreen
                }
            }
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(height)
                .clip(CircleShape)
                .background(accent)
        )
    }
}

@Composable
internal fun RecurringMetaChip(
    label: String,
    accent: Color,
    fillAlpha: Float = 0.14f
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = fillAlpha))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = accent,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp
            )
        )
    }
}

/** Decimal amount field matching the budget editor's input style. */
@Composable
internal fun EditorAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    supportingText: String? = null,
    shape: androidx.compose.ui.graphics.Shape = AppOutlinedFieldDefaults.shape
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.isEmpty() || (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1)) {
                onValueChange(input)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = shape,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = supportingText?.let { { Text(it) } },
        colors = AppOutlinedFieldDefaults.colors()
    )
}

/** Strip trailing zeros so 100.0 prefills as "100" but 1234.56 stays exact. */
internal fun Double.formatForInput(): String {
    val formatted = "%.2f".format(this)
    return formatted.trimEnd('0').trimEnd('.')
}
