package com.mknlabs.expensetracker.data.constants

import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.core.ui.theme.CategoryAccentDark
import com.mknlabs.expensetracker.core.ui.theme.CategoryAccentLight
import com.mknlabs.expensetracker.core.ui.theme.PaymentAccentDark
import com.mknlabs.expensetracker.core.ui.theme.PaymentAccentLight

/**
 * The swatches the colour picker offers, one list per theme.
 *
 * **Deduped.** The palette deliberately reuses tones — ten distinct light hexes cover all 27
 * categories and 6 payment methods — so offering the raw entries would show the same six greens
 * as six separate choices, which is a worse picker, not a more generous one. Twenty-seven
 * swatches collapse to ten, and eleven in dark where one pair splits.
 *
 * **Per theme, and the picker stores what it shows.** A swatch is drawn in the colour the active
 * theme would actually paint, and tapping it stores that hex. The consequence worth stating: the
 * same swatch yields a different stored value in light and in dark, because they are two
 * different colours that a user saw. That is the point — every swatch in the row is legible
 * against the surface it is shown on, where a single shared set would put deep tones on the
 * near-black card and read as a row of mud. The other theme is the resolver's problem, and the
 * contrast adapter already handles a colour that was chosen looking at the other one.
 *
 * The sets are deliberately *not* extended with a free colour wheel. Every value in the palette
 * has been contrast-checked against both cards, which is what makes offering it safe; an
 * arbitrary colour would have no such guarantee and would rely on the adapter to rescue it.
 */

/** Distinct palette tones for the light theme: ten of them. */
val categoryColorOptionsLight: List<Color> =
    (CategoryAccentLight.values + PaymentAccentLight.values).distinct()

/** Distinct palette tones for the dark theme: eleven of them. */
val categoryColorOptionsDark: List<Color> =
    (CategoryAccentDark.values + PaymentAccentDark.values).distinct()
