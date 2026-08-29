package com.mknlabs.expensetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Base text composable that resolves a [TextStyle] from the Material 3 typography system.
 *
 * All other text components in this file delegate to [AppText].
 *
 * @param text The text string to display.
 * @param style The typography token to use. Defaults to [MaterialTheme.typography.bodyMedium].
 * @param color Text color override. Defaults to [Color.Unspecified] (inherits from style).
 * @param fontWeight Weight override. Pass null to keep the weight from [style].
 * @param fontSize Size override. Pass [TextUnit.Unspecified] to keep the size from [style].
 * @param textAlign Horizontal alignment.
 * @param maxLines Maximum number of lines before truncation.
 * @param overflow How to handle overflow (e.g. [TextOverflow.Ellipsis]).
 * @param softWrap Whether the text should wrap at soft line breaks.
 * @param onTextLayout Callback with the [TextLayoutResult] after layout.
 * @param brush Optional gradient brush for the text color.
 */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    brush: Brush? = null
) {
    val resolvedStyle = style.copy(
        fontWeight = fontWeight ?: style.fontWeight,
        fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
        brush = brush
    )

    Text(
        text = text,
        modifier = modifier,
        style = resolvedStyle,
        color = if (brush != null) Color.Unspecified else color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
        onTextLayout = onTextLayout
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Display / Screen Titles
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Primary screen title — Google Sans, TitleLarge (20sp Medium).
 *
 * Use for: screen headers, primary page titles, dialog titles.
 */
@Composable
fun AppTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    AppText(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Section / Card Titles
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Section header — Google Sans, TitleMedium (18sp Medium).
 *
 * Use for: section headers within a screen, card titles, list group headers.
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    AppText(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Amount / Currency Text
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Prominent amount display — Google Sans Bold, responsive sizing.
 *
 * Use for: balance amounts, transaction amounts, totals.
 *
 * @param text The formatted amount string (e.g. "₹42,850").
 * @param color The text color. Defaults to [MaterialTheme.colorScheme.onSurface].
 * @param brush Optional gradient brush for the text color (e.g. brand gradient).
 * @param responsive If true, the font size scales down for long amounts (>10 chars → 28sp, >12 → 24sp).
 */
@Composable
fun AmountText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    brush: Brush? = null,
    responsive: Boolean = true,
    textAlign: TextAlign? = null,
    maxLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    val baseFontSize = 30.sp
    val resolvedFontSize = if (responsive) {
        when {
            text.length > 12 -> 20.sp
            text.length > 10 -> 25.sp
            else -> baseFontSize
        }
    } else {
        baseFontSize
    }

    AppText(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = resolvedFontSize,
            lineHeight = (resolvedFontSize.value * 1.2).sp
        ),
        color = if (brush != null) Color.Unspecified else color,
        brush = brush,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
        onTextLayout = onTextLayout
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Caption / Helper Text
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Small caption text — Roboto, BodySmall (12sp).
 *
 * Use for: timestamps, helper text, metadata, secondary labels.
 */
@Composable
fun CaptionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    AppText(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Label / Chip Text
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Small label text — Google Sans, LabelSmall (11sp Medium).
 *
 * Use for: chip labels, badges, navigation labels, tag text.
 */
@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    AppText(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout
    )
}
