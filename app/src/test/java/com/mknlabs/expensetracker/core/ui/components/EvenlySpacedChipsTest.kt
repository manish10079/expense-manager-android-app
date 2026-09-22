package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the arrangement that spreads a row of period chips across a line:
 *  - leftover width is divided equally between the gaps, so the row is spread rather than
 *    packed to one side,
 *  - a gap never falls below the minimum, which is what stops four chips that just fit
 *    from being laid out touching,
 *  - the reported spacing is that minimum, because FlowRow counts it when deciding whether
 *    the line is full and therefore when to wrap onto a second row,
 *  - right-to-left mirrors the positions.
 */
class EvenlySpacedChipsTest {

    private val minimumGap = 8.dp

    /** [minimumGap] in pixels. Density 1, so dp and px are the same number here. */
    private val minGapPx = 8

    private val arrangement: Arrangement.Horizontal = EvenlySpacedChips(minGap = minimumGap)

    // Four chips at natural widths, in px at density 1.
    private val sizes = intArrayOf(60, 70, 60, 130)

    private fun positions(totalSize: Int, isRtl: Boolean = false): IntArray {
        val out = IntArray(sizes.size)
        evenlySpacedPositions(
            totalSize = totalSize,
            sizes = sizes,
            minGapPx = minGapPx,
            isRtl = isRtl,
            outPositions = out
        )
        return out
    }

    private fun gaps(outPositions: IntArray): List<Int> =
        outPositions.indices.drop(1).map { index ->
            outPositions[index] - (outPositions[index - 1] + sizes[index - 1])
        }

    @Test
    fun reportsTheMinimumGapAsSpacing_soFlowRowCountsGapsWhenDecidingToWrap() {
        assertEquals(minimumGap.value, arrangement.spacing.value, 0f)
    }

    @Test
    fun leftoverWidth_isDividedEquallyBetweenTheGaps() {
        // 320px of chips plus 3 gaps of 8px needs 344px, so 56px is left over and 18px
        // lands in each gap, leaving all three at 26px.
        val out = positions(totalSize = 400)

        assertEquals(listOf(26, 26, 26), gaps(out))
        assertArrayEquals(intArrayOf(0, 86, 182, 268), out)
    }

    @Test
    fun spreadRow_startsAtTheLeadingEdge() {
        assertEquals(0, positions(totalSize = 400)[0])
    }

    @Test
    fun exactFit_usesTheMinimumGap() {
        val out = positions(totalSize = 344)

        assertEquals(listOf(8, 8, 8), gaps(out))
        assertArrayEquals(intArrayOf(0, 68, 146, 214), out)
    }

    @Test
    fun narrowerThanRequired_neverClosesTheGapsBelowTheMinimum() {
        // The wrapping parent is expected to start a new line before this point, but if it
        // cannot, the chips overflow rather than collapsing into a solid strip.
        val out = positions(totalSize = 300)

        assertEquals(listOf(8, 8, 8), gaps(out))
    }

    @Test
    fun rightToLeft_mirrorsEveryItemToTheOppositeEdge() {
        val ltr = positions(totalSize = 400)
        val rtl = positions(totalSize = 400, isRtl = true)

        assertArrayEquals(intArrayOf(340, 244, 158, 2), rtl)
        ltr.forEachIndexed { index, ltrStart ->
            assertEquals(400 - (ltrStart + sizes[index]), rtl[index])
        }
    }

    @Test
    fun singleItem_sitsAtTheLeadingEdge() {
        val out = IntArray(1)
        evenlySpacedPositions(
            totalSize = 400,
            sizes = intArrayOf(100),
            minGapPx = minGapPx,
            isRtl = false,
            outPositions = out
        )

        assertEquals(0, out[0])
    }

    @Test
    fun noItems_writesNothing() {
        val out = IntArray(0)
        evenlySpacedPositions(
            totalSize = 400,
            sizes = IntArray(0),
            minGapPx = minGapPx,
            isRtl = false,
            outPositions = out
        )

        assertTrue(out.isEmpty())
    }
}
