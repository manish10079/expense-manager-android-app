package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Pure colour maths for the category/payment palette. No Compose runtime, no
 * `MaterialTheme`, no `@Composable` — so every function here is a plain unit test away
 * from being proven, which is what the storage-backed picker needs: a colour the user
 * typed has to be reconciled with two themes that were designed without it.
 *
 * This file deliberately does not know what a "category" is. It parses a string and it
 * moves a colour until it is legible; [CategoryPalette] is what decides when to call it.
 */

/**
 * The canonical length of a stored colour, i.e. `#RRGGBB`. The writer always produces
 * this form; [parseHexColorOrNull] tolerates more so that a value which predates the
 * picker, or a hand-edited backup, still renders.
 */
internal const val CANONICAL_HEX_LENGTH = 7

/**
 * Parses `#RRGGBB` or `#AARRGGBB` into a [Color], or returns `null` for anything that is
 * not a colour at all.
 *
 * Two deliberate leniencies:
 *
 * - **The `#` is optional.** A bare `5B2EED` parses. The cost of accepting it is nothing;
 *   the cost of rejecting it is a category that renders in the fallback colour because a
 *   value arrived without its sigil.
 * - **Alpha is honoured, not stripped.** This is a parser, so it reports what the string
 *   says rather than quietly rewriting it. Normalising to an opaque colour is the writer's
 *   job — the picker stores six digits — which keeps "what is stored" and "what is
 *   parsed" from disagreeing in the middle.
 *
 * Everything else is rejected rather than guessed at: wrong length, non-hex characters,
 * `null`, blank, and whitespace-only all return `null`, so the caller falls through to
 * the palette instead of drawing an arbitrary colour.
 */
fun parseHexColorOrNull(hex: String?): Color? {
    val trimmed = hex?.trim().orEmpty()
    if (trimmed.isEmpty()) return null

    val body = trimmed.removePrefix("#")
    if (body.length != 6 && body.length != 8) return null
    if (!body.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) return null

    val value = body.toLongOrNull(radix = 16) ?: return null
    return if (body.length == 6) Color(0xFF000000L or value) else Color(value)
}

/**
 * The WCAG contrast ratio between two colours, from 1.0 (identical) to 21.0 (black on
 * white).
 *
 * Uses the same relative-luminance definition the rest of the app's contrast commentary
 * is written against — the sRGB electro-optical transfer function, per channel, weighted
 * 0.2126 / 0.7152 / 0.0722 — so a figure quoted in a comment and a figure this function
 * returns are the same number.
 */
/**
 * The canonical `#RRGGBB` for [this], the inverse of [parseHexColorOrNull].
 *
 * Rounds each channel to a byte, because that is the only precision a hex string carries and
the only precision storage carries — so the value this produces is exactly the value a round
trip through the database would give back. Opaque by construction: alpha is dropped, matching
what the picker stores. The colour picker is the reader.
 */
fun Color.toCanonicalHex(): String = "#" + listOf(red, green, blue).joinToString("") {
    ((it * 255f).toInt()).coerceIn(0, 255).toString(16).padStart(2, '0').uppercase()
}

fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = first.luminance()
    val secondLuminance = second.luminance()
    val lighter = maxOf(firstLuminance, secondLuminance)
    val darker = minOf(firstLuminance, secondLuminance)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/** The ratio a glyph needs against what it is drawn on, per WCAG 1.4.11. */
internal const val GLYPH_MIN_CONTRAST = 3.0f

/**
 * How many steps the adaptation may take toward the ink. Fixed rather than adaptive so
 * the result is reproducible: the same pick and the same theme always yield the same hex,
 * which is what lets a test pin it.
 */
private const val ADAPT_STEPS = 32

/**
 * Returns [color] unchanged if it already clears [minRatio] against [background], and
 * otherwise walks it toward the theme's ink until it does.
 *
 * The ink is derived from the background rather than passed in — light ink over a dark
 * surface, dark ink over a light one — so the function stays a pure two-colour
 * relationship with no knowledge of which `ColorScheme` it is serving. On the app's two
 * cards that resolves to white over `#141418` and black over `#FFFFFF`, which is what
 * `onSurface` already is in each theme.
 *
 * Contrast rises monotonically along that walk, so the **first** step that clears the
 * ratio is the smallest change that does the job: the pick keeps as much of its own hue
 * as legibility allows rather than being thrown to pure white or black. The test asserts
 * that monotonicity directly.
 *
 * The terminal case is the ink itself. Against any background that is not also the ink,
 * pure white or pure black is the maximum contrast available, so it cannot be reached and
 * still fail — but the ratio is checked one final time anyway rather than assumed, and the
 * ink is returned regardless rather than the original colour, because a colour that has
 * been walked toward legibility should not be silently abandoned back to an illegible one.
 *
 * **Called only for picker-authored colours.** The supplied palette draws verbatim in both
 * themes by design; see the note at the top of [CategoryPalette].
 */
fun adaptForContrast(color: Color, background: Color, minRatio: Float): Color {
    if (contrastRatio(color, background) >= minRatio) return color

    val ink = if (background.luminance() < 0.5f) Color.White else Color.Black
    for (step in 1..ADAPT_STEPS) {
        val candidate = lerp(color, ink, step / ADAPT_STEPS.toFloat())
        if (contrastRatio(candidate, background) >= minRatio) return candidate
    }
    return ink
}
