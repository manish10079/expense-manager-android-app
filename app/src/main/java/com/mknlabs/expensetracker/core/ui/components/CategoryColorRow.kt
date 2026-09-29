package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.parseHexColorOrNull
import com.mknlabs.expensetracker.core.ui.theme.toCanonicalHex
import com.mknlabs.expensetracker.data.constants.categoryColorOptionsDark
import com.mknlabs.expensetracker.data.constants.categoryColorOptionsLight

/**
 * The colour swatches, leading with the way back to "no colour of its own".
 *
 * Shared by the two places a colour is chosen — creating a category, and recolouring one that
 * already exists — so the two cannot offer different palettes or store different values for the
 * same tap. It reads as one component rather than a copy of one for that reason.
 *
 * The row shows the **active theme's** palette, and tapping a swatch reports the exact hex that was
 * on screen. Three consequences, all deliberate:
 *
 * - every swatch is legible against the surface it is drawn on, where one shared set would put deep
 *   tones on the near-black card and read as a row of mud;
 * - a colour chosen in dark mode and a colour chosen in light mode are different stored values,
 *   because they are two different colours that a user saw. The other theme is the resolver's
 *   problem, and its contrast adapter already handles a colour picked looking at the other one;
 * - a colour chosen while looking at the *other* theme is not one of these swatches, so it is drawn
 *   as a swatch of its own. Without that it would render as nothing selected, which reads as "no
 *   colour" while the row is in fact coloured — and on the recolour sheet a second tap would then
 *   silently replace a colour the user never meant to change.
 *
 * The default swatch is not decoration either. "No colour of its own" is the state every seeded row
 * is in and the state the create screen starts from, and it is also the only way back out of a
 * choice: without it, a user who tapped a swatch once could not undo it short of deleting the row.
 */
@Composable
fun CategoryColorRow(
    selectedColorHex: String?,
    onColorSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val swatches = if (MaterialTheme.colorScheme.isDark) {
        categoryColorOptionsDark
    } else {
        categoryColorOptionsLight
    }

    val selectedColor = parseHexColorOrNull(selectedColorHex)
    val selectedIsAbsent = selectedColor != null && swatches.none { it.toCanonicalHex() == selectedColorHex }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        item(key = "color_default") {
            ColorSwatch(
                color = null,
                contentDescription = stringResource(R.string.desc_color_default),
                selected = selectedColorHex == null,
                onClick = { onColorSelected(null) }
            )
        }

        if (selectedColor != null && selectedIsAbsent) {
            item(key = "color_current") {
                ColorSwatch(
                    color = selectedColor,
                    contentDescription = stringResource(
                        R.string.desc_color_swatch_selected,
                        selectedColor.toCanonicalHex()
                    ),
                    selected = true,
                    onClick = { onColorSelected(selectedColorHex) }
                )
            }
        }

        items(swatches, key = { it.toCanonicalHex() }) { swatch ->
            val hex = swatch.toCanonicalHex()
            val selected = hex == selectedColorHex
            ColorSwatch(
                color = swatch,
                contentDescription = stringResource(
                    if (selected) R.string.desc_color_swatch_selected else R.string.desc_color_swatch,
                    hex
                ),
                selected = selected,
                onClick = { onColorSelected(hex) }
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color?,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val fill = color ?: colorScheme.surfaceVariant

    // The tick has to sit on whatever the user chose, so its polarity is derived from the swatch
    // rather than fixed: white on a light amber fails, black on a deep purple fails, and one of the
    // two is right for every value the palette holds.
    val tickColor = if (color == null || color.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(44.dp)
            .shadow(
                elevation = if (selected) 12.dp else 0.dp,
                shape = CircleShape,
                ambientColor = colorScheme.accentInk.copy(alpha = 0.34f),
                spotColor = colorScheme.secondary.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(fill)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colorScheme.onSurface else colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = tickColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
