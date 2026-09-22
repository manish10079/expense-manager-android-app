package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Spreads items across a line with the gaps equal, but never closer than [minGap].
 *
 * `Arrangement.SpaceBetween` has no minimum, so a set of chips that *just* fits would be laid
 * out touching — and, because a wrapping row only wraps when the content genuinely overflows,
 * it would stay on one line as a solid strip instead of moving to two rows. Reporting
 * [minGap] as [spacing] is what makes the parent count the gaps when it decides whether the
 * line is full, so the row wraps before the chips touch.
 *
 * With the minimum respected, the leftover width is divided evenly between the gaps, so a row
 * that fits is still spread across the full width rather than packed to one side.
 *
 * Internal rather than private so the behaviour can be unit tested.
 */
internal class EvenlySpacedChips(private val minGap: Dp) : Arrangement.Horizontal {

    override val spacing: Dp get() = minGap

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray
    ) {
        evenlySpacedPositions(
            totalSize = totalSize,
            sizes = sizes,
            minGapPx = minGap.roundToPx(),
            isRtl = layoutDirection == LayoutDirection.Rtl,
            outPositions = outPositions
        )
    }
}

/**
 * Writes the start offset of each entry in [sizes] into [outPositions], spreading them across
 * [totalSize] with equal gaps that never fall below [minGapPx].
 *
 * Any width left over after the minimum gaps is shared equally between them, so a set of chips
 * that fits is spread across the whole line. When there is no width to spare the gaps stay at
 * the minimum, which is the signal for the wrapping parent to move the last chips onto a
 * second line.
 *
 * Split out from [EvenlySpacedChips] so the arithmetic can be unit tested without a Compose
 * Density, which a plain JVM test cannot supply.
 */
internal fun evenlySpacedPositions(
    totalSize: Int,
    sizes: IntArray,
    minGapPx: Int,
    isRtl: Boolean,
    outPositions: IntArray
) {
    if (sizes.isEmpty()) return

    val contentSize = sizes.sum()
    val requiredSize = contentSize + minGapPx * (sizes.size - 1)
    val gapPx = if (sizes.size > 1 && totalSize > requiredSize) {
        minGapPx + (totalSize - requiredSize) / (sizes.size - 1)
    } else {
        minGapPx
    }

    var current = 0
    sizes.forEachIndexed { index, size ->
        outPositions[index] = if (isRtl) totalSize - current - size else current
        current += size + gapPx
    }
}
