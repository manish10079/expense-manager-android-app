package com.mknlabs.expensetracker.utils

/**
 * The canonical form of a stored colour: an upper-case `#RRGGBB`, exactly seven characters,
 * no alpha.
 *
 * This is the **writer's** half of the colour contract. It deliberately does not share code
 * with
 * [parseHexColorOrNull][com.mknlabs.expensetracker.core.ui.theme.parseHexColorOrNull], which
 * is the reader's half, because the two want opposite things:
 *
 * - the reader is tolerant, so that a value which predates a rule — a bare `5B2EED` with no
 *   sigil, or eight digits carrying an alpha — still renders as the colour it names rather
 *   than silently becoming the fallback;
 * - the writer is strict, so that the database, the cloud document and the backup file only
 *   ever hold one shape of value, and a round trip through any of the three is lossless.
 *
 * Returns `null` for anything that is not a colour, which the caller turns into "no
 * override" rather than storing a value nothing can read. That is the whole point of
 * normalising at the boundary: after this, storage either holds a colour or holds nothing.
 *
 * Alpha is dropped rather than kept. A user picks a colour, not an opacity — the transparency
 * a surface needs (a glyph's wash, a disabled state) is applied where it is drawn, so an alpha
 * arriving from somewhere else is reduced to its RGB rather than allowed to leak into a
 * column the renderer reads as authoritative.
 */
fun normalizeColorHexOrNull(hex: String?): String? {
    val body = hex?.trim()?.removePrefix("#") ?: return null
    if (body.length != 6 && body.length != 8) return null
    if (!body.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }) return null

    // An eight-digit value is `#AARRGGBB`, so the colour is its low six.
    return "#" + body.takeLast(6).uppercase()
}
