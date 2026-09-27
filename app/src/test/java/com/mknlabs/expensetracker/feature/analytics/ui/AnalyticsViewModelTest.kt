package com.mknlabs.expensetracker.feature.analytics.ui

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import kotlin.math.ceil

class AnalyticsViewModelTest {

    private fun transaction(
        id: Long,
        createdAt: Long,
        amountMinor: Long,
        typeId: Int = 2,
        categoryId: Int = 1,
        paymentTypeId: Int = 1
    ): Transaction {
        return Transaction(
            id = id.toString(),
            note = "test",
            createdAt = createdAt,
            amountMinor = amountMinor,
            transactionTypeId = typeId,
            paymentTypeId = paymentTypeId,
            categoryId = categoryId
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

    private fun viewModelWith(
        transactions: List<Transaction>,
        categories: List<CategoryType> = emptyList(),
        paymentTypes: List<PaymentType> = emptyList()
    ): AnalyticsViewModel {
        return AnalyticsViewModel().apply {
            updateInputs(
                transactions = transactions,
                categories = categories,
                paymentTypes = paymentTypes,
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

    // ── Breakdown identity ────────────────────────────────────────────────────
    // The donut, its legend dots and its bars now draw each category's own colour, which needs
    // two things from this layer: the loaded row (a seed constant can never carry a stored colour)
    // and the colour itself. Both are asserted here rather than left to the renderer.

    @Test
    fun `a user-created category brings its name and colour into the breakdown`() {
        // The loaded row is what makes either possible: the breakdown is keyed by id against the
        // rows the screen passes in, and a user-created category only exists there.
        val viewModel = viewModelWith(
            transactions = listOf(transaction(1, dayTimestamp(3), 4_000L, categoryId = 106)),
            categories = listOf(customCategory(id = 106, name = "Coffee", colorHex = "#D97706"))
        )

        val row = viewModel.uiState.value.snapshot.allCategoryBreakdown.single()

        assertEquals("Coffee", row.label)
        assertEquals("#D97706", row.colorHex)
        assertEquals(false, row.isOther)
    }

    @Test
    fun `a seeded category takes its label from its row and carries no colour`() {
        // The ordinary case, and the one that must not change. A seeded row has no stored colour,
        // so the chart resolves it from the palette by id at draw time; a value here would mean the
        // palette had leaked into storage.
        val viewModel = viewModelWith(
            transactions = listOf(transaction(1, dayTimestamp(3), 4_000L, categoryId = 1)),
            categories = listOf(categoryMap.getValue(1))
        )

        val row = viewModel.uiState.value.snapshot.allCategoryBreakdown.single()

        assertEquals(categoryMap.getValue(1).name, row.label)
        assertNull(row.colorHex)
        assertEquals(false, row.isOther)
    }

    @Test
    fun `a category with no loaded row stays as Other`() {
        // A transaction whose category row is gone — the category was deleted, the transaction was
        // not. The id resolves to nothing, so it must still be groupable as Other rather than
        // rendering as a blank legend entry wearing a colour it never had.
        val viewModel = viewModelWith(
            transactions = listOf(transaction(1, dayTimestamp(3), 4_000L, categoryId = 9_999)),
            categories = listOf(categoryMap.getValue(1))
        )

        val row = viewModel.uiState.value.snapshot.allCategoryBreakdown.single()

        assertEquals("", row.label)
        assertNull(row.colorHex)
        assertTrue(row.isOther)
    }

    @Test
    fun `payment breakdown resolves its row the same way`() {
        // Payment ids restart at 1, so this also proves the payment breakdown reads the payment
        // list rather than the category one: id 1 exists in both, and only the right list holds
        // the wallet the transaction was actually paid with.
        val viewModel = viewModelWith(
            transactions = listOf(transaction(1, dayTimestamp(3), 2_500L, paymentTypeId = 7)),
            paymentTypes = listOf(
                PaymentType(id = 7, name = "Wallet", iconKey = "payments", colorHex = "#0288D1")
            )
        )

        val row = viewModel.uiState.value.snapshot.allPaymentTypeBreakdown.single()

        assertEquals("Wallet", row.label)
        assertEquals("#0288D1", row.colorHex)
        assertEquals(false, row.isOther)
    }

    @Test
    fun `a seeded payment method takes its label from its row and carries no colour`() {
        val viewModel = viewModelWith(
            transactions = listOf(transaction(1, dayTimestamp(3), 2_500L, paymentTypeId = 1)),
            paymentTypes = listOf(paymentTypeMap.getValue(1))
        )

        val row = viewModel.uiState.value.snapshot.allPaymentTypeBreakdown.single()

        assertEquals(paymentTypeMap.getValue(1).name, row.label)
        assertNull(row.colorHex)
    }

    private fun customCategory(id: Int, name: String, colorHex: String?) = CategoryType(
        id = id,
        name = name,
        iconKey = "shopping_cart",
        transactionTypeId = 2,
        colorHex = colorHex,
        isSystem = false
    )
}
