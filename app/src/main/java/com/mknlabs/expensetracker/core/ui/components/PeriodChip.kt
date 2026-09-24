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
import androidx.compose.ui.draw.clip
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
 * A chip is emitted per option so the parent can space the options evenly and let them wrap
 * onto a second row when they stop fitting, which is what [EvenlySpacedChips] arranges.
 *
 * The fill animates rather than sliding, because separate chips have no shared path for an
 * indicator to travel along. A colour transition is what keeps a state change reading as
 * deliberate rather than as a jump.
 *
 * @param textStyle lets a caller keep its own label size. The two call sites differ on
 *   purpose: Analytics has four short labels and uses labelLarge, while Budget has three
 *   all-caps labels that would not fit on one line at that size.
 */
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.ChipBgSelectedDark
import com.mknlabs.expensetracker.core.ui.theme.ChipBgSelectedLight
import com.mknlabs.expensetracker.core.ui.theme.ChipBorderSelectedDark
import com.mknlabs.expensetracker.core.ui.theme.ChipBorderSelectedLight
import com.mknlabs.expensetracker.core.ui.theme.ChipTextSelectedDark
import com.mknlabs.expensetracker.core.ui.theme.ChipTextSelectedLight
import com.mknlabs.expensetracker.core.ui.theme.ChipBgUnselectedDark
import com.mknlabs.expensetracker.core.ui.theme.ChipBgUnselectedLight
import com.mknlabs.expensetracker.core.ui.theme.ChipBorderUnselectedDark
import com.mknlabs.expensetracker.core.ui.theme.ChipBorderUnselectedLight
import com.mknlabs.expensetracker.core.ui.theme.ChipTextUnselectedDark
import com.mknlabs.expensetracker.core.ui.theme.ChipTextUnselectedLight


@Composable
fun PeriodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
) {
    val isDark = MaterialTheme.colorScheme.isDark

    val targetContainerColor = if (isSelected) {
        if (isDark) ChipBgSelectedDark else ChipBgSelectedLight
    } else {
        if (isDark) ChipBgUnselectedDark else ChipBgUnselectedLight
    }

    val targetBorderColor = if (isSelected) {
        if (isDark) ChipBorderSelectedDark else ChipBorderSelectedLight
    } else {
        if (isDark) ChipBorderUnselectedDark else ChipBorderUnselectedLight
    }

    val targetContentColor = if (isSelected) {
        if (isDark) ChipTextSelectedDark else ChipTextSelectedLight
    } else {
        if (isDark) ChipTextUnselectedDark else ChipTextUnselectedLight
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
