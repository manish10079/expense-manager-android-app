package com.mknlabs.expensetracker.feature.funds

import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.FundStatus
import com.mknlabs.expensetracker.models.FundWithProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The balance, progress and status a fund shows are all derived from the spent total, never
 * stored. These pin that derivation, which is what keeps the list, the detail screen and the
 * analytics breakdown in agreement.
 */
class FundProgressTest {

    private fun fund(amountMinor: Long, isArchived: Boolean = false) = Fund(
        id = "fund-1",
        name = "Gift",
        amountMinor = amountMinor,
        startDate = 0L,
        iconKey = "card_giftcard",
        colorHex = "#F59E0B",
        isArchived = isArchived
    )

    @Test
    fun remaining_isAmountMinusSpent() {
        val progress = FundWithProgress(fund(amountMinor = 100_000), spentMinor = 30_000)
        assertEquals(70_000L, progress.remainingMinor)
    }

    @Test
    fun remaining_neverGoesNegative_whenOverspent() {
        val progress = FundWithProgress(fund(amountMinor = 100_000), spentMinor = 150_000)
        assertEquals(0L, progress.remainingMinor)
        assertTrue(progress.isOverspent)
    }

    @Test
    fun progress_isCappedAtOne_evenWhenOverspent() {
        val progress = FundWithProgress(fund(amountMinor = 100_000), spentMinor = 250_000)
        assertEquals(1f, progress.progress, 0.0001f)
    }

    @Test
    fun progress_isHalfway_whenHalfSpent() {
        val progress = FundWithProgress(fund(amountMinor = 200_000), spentMinor = 100_000)
        assertEquals(0.5f, progress.progress, 0.0001f)
    }

    @Test
    fun status_isActive_whileMoneyRemains() {
        val progress = FundWithProgress(fund(amountMinor = 100_000), spentMinor = 99_999)
        assertEquals(FundStatus.Active, progress.status)
        assertFalse(progress.isExhausted)
    }

    @Test
    fun status_isCompleted_whenFullySpent() {
        val progress = FundWithProgress(fund(amountMinor = 100_000), spentMinor = 100_000)
        assertEquals(FundStatus.Completed, progress.status)
        assertTrue(progress.isExhausted)
    }

    @Test
    fun status_isArchived_overridesCompletion() {
        val archived = FundWithProgress(fund(amountMinor = 100_000, isArchived = true), spentMinor = 100_000)
        assertEquals(FundStatus.Archived, archived.status)
    }

    @Test
    fun zeroAmountFund_hasNoProgressAndIsNeverExhausted() {
        val progress = FundWithProgress(fund(amountMinor = 0L), spentMinor = 0L)
        assertEquals(0f, progress.progress, 0.0001f)
        assertEquals(FundStatus.Active, progress.status)
    }
}
