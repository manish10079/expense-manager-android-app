package com.mknlabs.expensetracker.feature.analytics.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The Cash Flow Ratio card reads the split three ways at once, so these pin the rules that keep
 * the bar, the two percentages and the badge from contradicting each other.
 */
class CashFlowSplitTest {

    @Test
    fun `a typical month splits into whole percentages that add up to 100`() {
        val split = cashFlowSplit(income = 13545.0, expense = 70847.0)

        assertEquals(16, split.incomePercent)
        assertEquals(84, split.expensePercent)
        assertEquals(0.1605f, split.incomeWeight, 0.0005f)
        assertEquals("1 : 5.2", split.ratioDisplay)
    }

    @Test
    fun `the ratio drops a trailing zero decimal`() {
        assertEquals("1 : 2", cashFlowSplit(income = 1000.0, expense = 2000.0).ratioDisplay)
    }

    @Test
    fun `an expense share that rounds away still reads as a ratio`() {
        assertEquals("1 : 0", cashFlowSplit(income = 100.0, expense = 1.0).ratioDisplay)
    }

    @Test
    fun `no expense in the period reads as a ratio of one to nothing`() {
        val split = cashFlowSplit(income = 5000.0, expense = 0.0)

        assertEquals("1 : 0", split.ratioDisplay)
        assertEquals(100, split.incomePercent)
        assertEquals(0, split.expensePercent)
        assertEquals(1f, split.incomeWeight, 0.0001f)
    }

    @Test
    fun `a period with no income cannot be normalised to one, so it reads the other way round`() {
        val split = cashFlowSplit(income = 0.0, expense = 500.0)

        assertEquals("0 : 1", split.ratioDisplay)
        assertEquals(0, split.incomePercent)
        assertEquals(100, split.expensePercent)
        assertEquals(0f, split.incomeWeight, 0.0001f)
    }

    @Test
    fun `nothing moved, so the bar splits evenly rather than claiming one side took it all`() {
        val split = cashFlowSplit(income = 0.0, expense = 0.0)

        assertEquals(0.5f, split.incomeWeight, 0.0001f)
        assertEquals(50, split.incomePercent)
        assertEquals(50, split.expensePercent)
        assertEquals("1 : 0", split.ratioDisplay)
    }

    @Test
    fun `the two percentages never drift apart, whatever the shares are`() {
        val pairs = listOf(
            1.0 to 2.0,
            333.0 to 667.0,
            1.0 to 999.0,
            9999.0 to 1.0,
            7.0 to 3.0
        )

        for ((income, expense) in pairs) {
            val split = cashFlowSplit(income = income, expense = expense)
            assertEquals(
                "income $income expense $expense",
                100,
                split.incomePercent + split.expensePercent
            )
            assertTrue(
                "income $income expense $expense",
                split.incomeWeight > 0f && split.incomeWeight < 1f
            )
        }
    }
}
