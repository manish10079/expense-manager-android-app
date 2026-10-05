package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock

/**
 * One selectable period chip: an 18.dp radius, a 1.dp border, and a primaryContainer fill
 * while it is the selected option.
 *
 * Used by the period selectors on Analytics and on Budget & Recurring. Deliberately not
 * [AnimatedTabSwitcher]: that component is a single container with a sliding indicator and
 * divides its width into equal segments, so short labels sit in a mostly empty bar, and it
 * is shared by four other screens that must not be restyled to change one.
 *
 * A chip is emitted per option so the parent can divide the row evenly and let the options wrap
 * onto a second row when they stop fitting.
 *
 * The fill animates rather than sliding, because separate chips have no shared path for an
 * indicator to travel along. A colour transition is what keeps a state change reading as
 * deliberate rather than as a jump.
 *
 * @param textStyle lets a caller keep its own label size. The two call sites differ on
 *   purpose: Analytics has four short labels and uses labelLarge, while Budget has three
 *   all-caps labels that would not fit on one line at that size.
 */
import com.mknlabs.expensetracker.core.ui.theme.chip
import com.mknlabs.expensetracker.core.ui.theme.chipInkOff
import com.mknlabs.expensetracker.core.ui.theme.chipOutline
import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.chipSelectedInk


/**
 * Per-state colours for a [PeriodChip] whose two options carry a semantic colour of their own
 * rather than the shared brand chip tokens — the Add Transaction screen's income/expense pair.
 * Aimed at the five parts a chip paints (fill/ink in each state, plus the idle outline); the
 * chosen chip's own edge is deliberately absent from the set because it is always transparent.
 *
 * Colours arrive from [MaterialTheme.colorScheme] at the call site, so a chip built with this
 * still follows light/dark like every [PeriodChip].
 */
data class PeriodChipColors(
    val selectedContainer: Color,
    val selectedContent: Color,
    val unselectedContainer: Color,
    val unselectedBorder: Color,
    val unselectedContent: Color
)

@Composable
fun PeriodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
    /**
     * Optional palette override. Null (every current caller but Add Transaction) keeps the
     * shared tokens below; providing one swaps only the colours, never the chip's shape,
     * border weight, animation or touch target.
     */
    colors: PeriodChipColors? = null
) {
    // The spec's five chip tokens, in place of the old twelve: --chip / --chipLine /
    // --chipInkOff when idle, --chipSel / --chipInk when chosen. The selected chip has no
    // edge of its own in the mock, so its border goes transparent rather than being drawn
    // in a second brand weight.
    val targetContainerColor = if (isSelected) {
        colors?.selectedContainer ?: MaterialTheme.colorScheme.chipSelected
    } else {
        colors?.unselectedContainer ?: MaterialTheme.colorScheme.chip
    }

    val targetBorderColor = if (isSelected) {
        Color.Transparent
    } else {
        colors?.unselectedBorder ?: MaterialTheme.colorScheme.chipOutline
    }

    val targetContentColor = if (isSelected) {
        colors?.selectedContent ?: MaterialTheme.colorScheme.accentInk
    } else {
        colors?.unselectedContent ?: MaterialTheme.colorScheme.chipInkOff
    }

    val containerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(durationMillis = 180),
        label = "period_chip_container"
    )
    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 180),
        label = "period_chip_border"
    )
    val contentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = tween(durationMillis = 180),
        label = "period_chip_content"
    )

    Row(
        modifier = modifier
            // The chip's own height is the label's line plus 20.dp of padding, which is under the
            // 48.dp a touch target needs. This reserves the rest around it without changing the
            // chip's appearance, the construction Material's own components use.
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        Text(
            text = label,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = textStyle
        )

        // A gated option has to say so, or the gate stays invisible until the user taps it.
        if (isLocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                // Same wording AnimatedTabSwitcher uses for its locked tab, so the gate reads
                // identically whichever control is showing it.
                contentDescription = stringResource(
                    id = R.string.content_desc_locked_formatted,
                    label
                ),
                tint = MaterialTheme.colorScheme.featureGateLock,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
