package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.domain.repository.BudgetRepository
import com.mknlabs.expensetracker.models.Budget
import com.mknlabs.expensetracker.models.BudgetPeriod
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.utils.CustomMonthUtils
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetAndRecurringViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: BudgetAndRecurringViewModel
    private lateinit var fakeRepository: FakeBudgetRepository

    @Before
    fun setup() {
        fakeRepository = FakeBudgetRepository()
        viewModel = BudgetAndRecurringViewModel(budgetRepository = fakeRepository)
        viewModel.updateInputs(
            transactions = emptyList(),
            categories = emptyList(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            recurringRules = emptyList<RecurringTransactionRule>(),
            monthStartDay = 1
        )
    }

    @Test
    fun `ui state exposes only previous month budgets as copy candidates`() = runTest {
        val currentMonthStart = startOfCurrentMonth()
        val prevMonthStart = shiftedMonthStart(currentMonthStart, -1)
        val twoMonthsAgoStart = shiftedMonthStart(currentMonthStart, -2)

        fakeRepository.upsertBudget(budget(id = "prev_1", monthStart = prevMonthStart, categoryId = 1, limitAmount = 500.0, name = "Food"))
        fakeRepository.upsertBudget(budget(id = "prev_2", monthStart = prevMonthStart, categoryId = 2, limitAmount = 200.0, name = "Transport"))
        fakeRepository.upsertBudget(budget(id = "current_1", monthStart = currentMonthStart, categoryId = 3, limitAmount = 900.0, name = "Shopping"))
        fakeRepository.upsertBudget(budget(id = "old_1", monthStart = twoMonthsAgoStart, categoryId = 4, limitAmount = 50.0, name = "Old"))

        val candidateIds = viewModel.uiState.value.previousMonthBudgets.map { it.id }.toSet()
        assertEquals(setOf("prev_1", "prev_2"), candidateIds)
        assertTrue(viewModel.uiState.value.previousMonthLabel.isNotBlank())
    }

    @Test
    fun `copy all duplicates every previous month budget into current month without deleting anything`() = runTest {
        val currentMonthStart = startOfCurrentMonth()
        val prevMonthStart = shiftedMonthStart(currentMonthStart, -1)

        fakeRepository.upsertBudget(budget(id = "prev_1", monthStart = prevMonthStart, categoryId = 1, limitAmount = 500.0, name = "Food"))
        fakeRepository.upsertBudget(
            budget(
                id = "prev_2",
                monthStart = prevMonthStart,
                categoryId = 2,
                categoryIds = listOf(2, 5),
                limitAmount = 300.0,
                name = "Transport",
                period = BudgetPeriod.YEARLY
            )
        )
        // Existing current-month budget must be kept untouched (never-delete semantics).
        fakeRepository.upsertBudget(budget(id = "current_1", monthStart = currentMonthStart, categoryId = 3, limitAmount = 900.0, name = "Shopping"))

        viewModel.copyAllPreviousMonthBudgets()

        val allBudgets = fakeRepository.getAll()
        // Originals are preserved (never-delete semantics).
        assertTrue(allBudgets.map { it.id }.containsAll(setOf("prev_1", "prev_2", "current_1")))
        val copied = allBudgets.filter { it.id !in setOf("prev_1", "prev_2", "current_1") }
        assertEquals(2, copied.size)

        val copiedFood = copied.first { it.name == "Food" }
        assertEquals(currentMonthStart, copiedFood.monthStart)
        assertEquals(listOf(1), copiedFood.effectiveCategoryIds)
        assertEquals(500_00L, copiedFood.limitMinor)
        assertEquals(0, copiedFood.editCount)

        val copiedTransport = copied.first { it.name == "Transport" }
        assertEquals(currentMonthStart, copiedTransport.monthStart)
        assertEquals(listOf(2, 5), copiedTransport.effectiveCategoryIds)
        assertEquals(BudgetPeriod.YEARLY, copiedTransport.period)
        assertEquals(300_00L, copiedTransport.limitMinor)
        assertEquals(0, copiedTransport.editCount)
    }

    @Test
    fun `copy selected only duplicates the checked previous month budgets`() = runTest {
        val currentMonthStart = startOfCurrentMonth()
        val prevMonthStart = shiftedMonthStart(currentMonthStart, -1)

        fakeRepository.upsertBudget(budget(id = "prev_1", monthStart = prevMonthStart, categoryId = 1, limitAmount = 500.0, name = "Food"))
        fakeRepository.upsertBudget(budget(id = "prev_2", monthStart = prevMonthStart, categoryId = 2, limitAmount = 200.0, name = "Transport"))

        viewModel.copySelectedPreviousMonthBudgets(listOf("prev_2"))

        val copies = fakeRepository.getAll().filter { it.id !in setOf("prev_1", "prev_2") }
        assertEquals(1, copies.size)
        val copy = copies.single()
        assertEquals("Transport", copy.name)
        assertEquals(currentMonthStart, copy.monthStart)
        assertEquals(listOf(2), copy.effectiveCategoryIds)
        assertEquals(200_00L, copy.limitMinor)
        assertNotEquals("prev_2", copy.id)
        // Food (prev_1) must NOT be duplicated into the current month.
        val foodCopies = fakeRepository.getAll().count {
            it.id != "prev_1" && it.name == "Food" && it.monthStart == currentMonthStart
        }
        assertEquals(0, foodCopies)
    }

    @Test
    fun `copy selected with an empty list does nothing`() = runTest {
        val currentMonthStart = startOfCurrentMonth()
        val prevMonthStart = shiftedMonthStart(currentMonthStart, -1)
        fakeRepository.upsertBudget(budget(id = "prev_1", monthStart = prevMonthStart, categoryId = 1, limitAmount = 500.0, name = "Food"))

        val before = fakeRepository.getAll().size
        viewModel.copySelectedPreviousMonthBudgets(emptyList())
        assertEquals(before, fakeRepository.getAll().size)
    }

    @Test
    fun `copy all when previous month has no budgets creates nothing`() = runTest {
        fakeRepository.upsertBudget(budget(id = "current_1", monthStart = startOfCurrentMonth(), categoryId = 3, limitAmount = 900.0, name = "Shopping"))

        viewModel.copyAllPreviousMonthBudgets()
        assertEquals(1, fakeRepository.getAll().size)
    }

    private fun startOfCurrentMonth(): Long =
        CustomMonthUtils.getStartOfCustomMonth(System.currentTimeMillis(), 1)

    private fun shiftedMonthStart(base: Long, offset: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = base
            add(Calendar.MONTH, offset)
        }
        return CustomMonthUtils.getStartOfCustomMonth(cal.timeInMillis, 1)
    }

    private fun budget(
        id: String,
        monthStart: Long,
        categoryId: Int,
        limitAmount: Double,
        name: String = "",
        categoryIds: List<Int> = if (categoryId != 0) listOf(categoryId) else emptyList(),
        period: BudgetPeriod = BudgetPeriod.MONTHLY
    ): Budget {
        return Budget(
            id = id,
            categoryId = categoryId,
            categoryIds = categoryIds,
            name = name,
            period = period,
            monthStart = monthStart,
            limitAmount = limitAmount,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncState = SyncState.SYNCED,
            editCount = 0,
            isDeleted = false
        )
    }

    private class FakeBudgetRepository : BudgetRepository {
        private val store = mutableMapOf<String, Budget>()
        private val flow = MutableStateFlow<List<Budget>>(emptyList())

        override fun observeActiveBudgets(): Flow<List<Budget>> = flow

        override suspend fun upsertBudget(budget: Budget): Budget {
            val resolved = budget.copy(
                id = budget.id.ifBlank { UUID.randomUUID().toString() },
                syncState = SyncState.PENDING_UPLOAD
            )
            store[resolved.id] = resolved
            flow.value = store.values.toList()
            return resolved
        }

        override suspend fun deleteBudget(id: String) {
            store.remove(id)
            flow.value = store.values.toList()
        }

        fun getAll(): List<Budget> = store.values.toList()
    }
}
