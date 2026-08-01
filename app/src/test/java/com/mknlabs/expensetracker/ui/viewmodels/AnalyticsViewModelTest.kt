package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import kotlin.math.ceil

class AnalyticsViewModelTest {

    private fun transaction(
        id: Long,
        createdAt: Long,
        amountMinor: Long,
        typeId: Int = 2
    ): Transaction {
        return Transaction(
            id = id.toString(),
            note = "test",
            createdAt = createdAt,
            amountMinor = amountMinor,
            transactionTypeId = typeId,
            paymentTypeId = 1,
            categoryId = 1
        )
    }

    private fun dayTimestamp(day: Int): Long {
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR)
        val month = now.get(Calendar.MONTH)
        return Calendar.getInstance().apply {
            set(year, month, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun viewModelWith(transactions: List<Transaction>): AnalyticsViewModel {
        return AnalyticsViewModel().apply {
            updateInputs(
                transactions = transactions,
                categories = emptyList(),
                paymentTypes = emptyList(),
                currencyId = DEFAULT_CURRENCY_ID,
                amountFormatPreferences = defaultAmountFormatPreferences
            )
        }
    }

    @Test
    fun `month view aggregates expenses into weekly buckets`() {
        val now = Calendar.getInstance()
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val expectedWeeks = ceil(daysInMonth / 7.0).toInt()

        // 100.00 on day 1 (week 1), 50.00 on day 8 (week 2), 25.00 on day 15 (week 3)
        val transactions = listOf(
            transaction(1, dayTimestamp(1), 10_000L),
            transaction(2, dayTimestamp(8), 5_000L),
            transaction(3, dayTimestamp(15), 2_500L)
        )

        val viewModel = viewModelWith(transactions)
        val snapshot = viewModel.uiState.value.snapshot

        // One bucket per week, not per day
        assertEquals(expectedWeeks, snapshot.expenseChartPoints.size)
        assertEquals(100.0f, snapshot.expenseChartPoints[0])
        assertEquals(50.0f, snapshot.expenseChartPoints[1])
        assertEquals(25.0f, snapshot.expenseChartPoints[2])
    }

    @Test
    fun `month view produces zero buckets for weeks without transactions`() {
        val now = Calendar.getInstance()
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val expectedWeeks = ceil(daysInMonth / 7.0).toInt()
        // Only day 1 (week 1) and day 22 (week 4) have expenses
        val transactions = listOf(
            transaction(1, dayTimestamp(1), 10_000L),
            transaction(2, dayTimestamp(22), 7_000L)
        )

        val viewModel = viewModelWith(transactions)
        val snapshot = viewModel.uiState.value.snapshot

        assertEquals(expectedWeeks, snapshot.expenseChartPoints.size)
        assertEquals(100.0f, snapshot.expenseChartPoints[0])
        assertEquals(0.0f, snapshot.expenseChartPoints[1])
        assertEquals(0.0f, snapshot.expenseChartPoints[2])
        if (expectedWeeks > 3) {
            assertEquals(70.0f, snapshot.expenseChartPoints[3])
        }
    }

    @Test
    fun `switching from custom range to a tab clears the stuck custom range`() {
        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * 24 * 60 * 60 * 1000L

        val viewModel = viewModelWith(emptyList())
        viewModel.applyCustomRange(weekAgo, now)

        // Sanity: custom range is active before switching
        assertEquals(AnalyticsPeriod.CUSTOM, viewModel.uiState.value.selectedPeriod)
        assertEquals(weekAgo, viewModel.uiState.value.customRangeStart)

        viewModel.selectPeriod(AnalyticsPeriod.WEEK)

        // Custom range must be cleared so data is not stuck on it
        assertEquals(AnalyticsPeriod.WEEK, viewModel.uiState.value.selectedPeriod)
        assertEquals(null, viewModel.uiState.value.customRangeStart)
        assertEquals(null, viewModel.uiState.value.customRangeEnd)
        assertEquals(null, viewModel.uiState.value.customRange)

        // Snapshot must revert to the tab's summary label, not the custom one
        assertEquals(R.string.label_this_week, viewModel.uiState.value.snapshot.summaryLabel.resId)
    }

    @Test
    fun `month view keeps income and expense buckets aligned per week`() {
        val now = Calendar.getInstance()
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val expectedWeeks = ceil(daysInMonth / 7.0).toInt()

        // Expense on day 2, income on day 9 (different weeks)
        val transactions = listOf(
            transaction(1, dayTimestamp(2), 8_000L, typeId = 2),
            transaction(2, dayTimestamp(9), 6_000L, typeId = 1)
        )

        val viewModel = viewModelWith(transactions)
        val snapshot = viewModel.uiState.value.snapshot

        assertEquals(expectedWeeks, snapshot.expenseChartPoints.size)
        assertEquals(expectedWeeks, snapshot.incomeChartPoints.size)
        assertEquals(80.0f, snapshot.expenseChartPoints[0])
        assertEquals(0.0f, snapshot.expenseChartPoints[1])
        assertEquals(0.0f, snapshot.incomeChartPoints[0])
        assertEquals(60.0f, snapshot.incomeChartPoints[1])
    }
}
