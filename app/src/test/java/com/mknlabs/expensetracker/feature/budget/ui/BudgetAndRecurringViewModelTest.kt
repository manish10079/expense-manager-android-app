package com.mknlabs.expensetracker.feature.budget.ui

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.domain.repository.BudgetRepository
import com.mknlabs.expensetracker.domain.repository.RecurringRuleRepository
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.Budget
import com.mknlabs.expensetracker.models.BudgetPeriod
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.InstallmentOccurrence
import com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus
import com.mknlabs.expensetracker.models.InstallmentPlan
import com.mknlabs.expensetracker.models.InstallmentStatus
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.RecurringType
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.utils.CustomMonthUtils
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import com.mknlabs.expensetracker.utils.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.UUID

private const val RULE_ID = "rule-1"
private const val TEMPLATE_ID = "tx-template-1"
private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetAndRecurringViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: BudgetAndRecurringViewModel
    private lateinit var fakeRepository: FakeBudgetRepository
    private lateinit var fakeOccurrences: MutableStateFlow<List<InstallmentOccurrence>>

    @Before
    fun setup() {
        fakeRepository = FakeBudgetRepository()
        fakeOccurrences = MutableStateFlow(emptyList())
        viewModel = BudgetAndRecurringViewModel(
            budgetRepository = fakeRepository,
            recurringRuleRepository = FakeRecurringRuleRepository(fakeOccurrences)
        )
        viewModel.updateInputs(
            transactions = emptyList(),
            categories = emptyList(),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            recurringRules = emptyList<RecurringTransactionRule>(),
            monthStartDay = 1
        )
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
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

    @Test
    fun `installment card exposes one ledger row per slot with derived status`() = runTest {
        val now = System.currentTimeMillis()
        val paidDue = now - 40L * DAY_MILLIS
        val overdueDue = now - 5L * DAY_MILLIS
        val upcomingDue = now + 5L * DAY_MILLIS

        val rule = RecurringTransactionRule(
            id = RULE_ID,
            transactionId = TEMPLATE_ID,
            frequency = RecurringFrequency.Monthly,
            repeatCount = 3,
            isEnabled = true,
            remainingCount = 2,
            anchorAt = paidDue,
            nextRunAt = overdueDue,
            recurringType = RecurringType.INSTALLMENT,
            installmentTotalMinor = 30_000L,
            installmentAmountMinor = 10_000L,
            installmentTotalCount = 3,
            installmentStatus = InstallmentStatus.ACTIVE
        )
        fakeOccurrences.value = listOf(
            occurrence("1", paidDue, InstallmentOccurrenceStatus.PAID, paidAt = paidDue),
            occurrence("2", upcomingDue, InstallmentOccurrenceStatus.SKIPPED),
            // Pending and past its date: the ledger must show it as OVERDUE
            // without that ever being stored.
            occurrence("3", overdueDue, InstallmentOccurrenceStatus.PENDING)
        )

        viewModel.updateInputs(
            transactions = listOf(templateTransaction()),
            categories = listOf(category()),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            recurringRules = listOf(rule),
            monthStartDay = 1
        )

        val card = viewModel.uiState.value.recurringExpenses.single()
        assertTrue(card.isInstallment)
        assertEquals(
            listOf(
                InstallmentOccurrenceStatus.PAID,
                InstallmentOccurrenceStatus.SKIPPED,
                InstallmentOccurrenceStatus.OVERDUE
            ),
            card.slots.map { it.status }
        )
        assertEquals(listOf(1, 2, 3), card.slots.map { it.index })
        // The row id is the occurrence id the ledger's pay/skip/undo actions
        // hand back to the repository — the same one the worker settles by.
        assertEquals(
            listOf(1, 2, 3).map { InstallmentOccurrence.idFor(RULE_ID, it) },
            card.slots.map { it.id }
        )
        // Only the paid slot carries a settlement date, which the row shows.
        assertEquals(paidDue, card.slots[0].paidAt)
        assertNull(card.slots[1].paidAt)
        assertTrue(card.slots.all { it.amountLabel.isNotBlank() })
        // Progress follows the paid slots, not the skipped ones.
        assertEquals(1, card.installmentPaidCount)
        assertEquals(3, card.totalInstallments)
        assertEquals(1f / 3f, card.installmentProgressFraction, 0.0001f)
        // An unsettled past-due slot headlines the plan as overdue.
        assertEquals(
            R.string.label_emis_overdue,
            (card.dueLabel as UiText.StringResource).resId
        )
    }

    /**
     * A plan whose remaining slots were skipped has nothing left to act on, so it
     * is finished. The data layer only persists COMPLETED for an all-paid plan
     * and parks the schedule on the last slot's (past) date — which used to leak
     * into the card as a next due date that could never arrive.
     */
    @Test
    fun `a plan with every slot settled reads ALL SETTLED instead of a next due date`() = runTest {
        val now = System.currentTimeMillis()
        val paidDue = now - 90L * DAY_MILLIS
        val skippedDue = now - 60L * DAY_MILLIS

        val rule = RecurringTransactionRule(
            id = RULE_ID,
            transactionId = TEMPLATE_ID,
            frequency = RecurringFrequency.Monthly,
            repeatCount = 2,
            // A settled plan is disabled by the schedule and its remainingCount
            // is zero, but its persisted status stays ACTIVE.
            isEnabled = false,
            remainingCount = 0,
            anchorAt = paidDue,
            nextRunAt = skippedDue,
            recurringType = RecurringType.INSTALLMENT,
            installmentTotalMinor = 20_000L,
            installmentAmountMinor = 10_000L,
            installmentTotalCount = 2,
            installmentStatus = InstallmentStatus.ACTIVE
        )
        fakeOccurrences.value = listOf(
            occurrence("1", paidDue, InstallmentOccurrenceStatus.PAID, paidAt = paidDue),
            occurrence("2", skippedDue, InstallmentOccurrenceStatus.SKIPPED)
        )

        viewModel.updateInputs(
            transactions = listOf(templateTransaction()),
            categories = listOf(category()),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            recurringRules = listOf(rule),
            monthStartDay = 1
        )

        val card = viewModel.uiState.value.recurringExpenses.single()

        assertEquals(
            "a fully settled plan must not advertise a next due date",
            R.string.label_all_settled,
            (card.dueLabel as UiText.StringResource).resId
        )
        assertEquals(InstallmentStatus.COMPLETED, card.installmentPlanStatus)
        // Settling by skipping is not a payment: progress still counts paid slots.
        assertEquals(1, card.installmentPaidCount)
        assertEquals(2, card.totalInstallments)
    }

    @Test
    fun `converting a regular rule prefills its own creation date, not the template's`() = runTest {
        val created = System.currentTimeMillis() - 200L * DAY_MILLIS
        // An edit since then repointed the rule at a newer template, so the
        // template's date is no longer the series' start.
        val templateEditedAt = System.currentTimeMillis() - 4L * DAY_MILLIS
        val rule = RecurringTransactionRule(
            id = RULE_ID,
            transactionId = TEMPLATE_ID,
            frequency = RecurringFrequency.Monthly,
            repeatCount = 8,
            isEnabled = true,
            remainingCount = 4,
            anchorAt = created,
            nextRunAt = System.currentTimeMillis() + 10L * DAY_MILLIS
        )

        viewModel.updateInputs(
            transactions = listOf(templateTransaction().copy(createdAt = templateEditedAt)),
            categories = listOf(category()),
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            recurringRules = listOf(rule),
            monthStartDay = 1
        )

        val card = viewModel.uiState.value.recurringExpenses.single()
        assertFalse(card.isInstallment)
        assertEquals(
            "converting is not creating a new rule: the plan must start where this one did",
            created,
            card.firstDueAt
        )
    }

    private fun occurrence(
        index: String,
        dueAt: Long,
        status: InstallmentOccurrenceStatus,
        paidAt: Long? = null
    ): InstallmentOccurrence = InstallmentOccurrence(
        id = "${RULE_ID}_occ_$index",
        ruleId = RULE_ID,
        installmentIndex = index.toInt(),
        dueAt = dueAt,
        amountMinor = 10_000L,
        paidAt = paidAt,
        status = status
    )

    private fun templateTransaction(): Transaction = Transaction(
        id = TEMPLATE_ID,
        note = "laptop emi",
        createdAt = System.currentTimeMillis() - 90L * DAY_MILLIS,
        amountMinor = 60_000L,
        transactionTypeId = 2,
        paymentTypeId = 1,
        categoryId = 1
    )

    private fun category(): CategoryType = CategoryType(
        id = 1,
        name = "EMIs",
        iconKey = "card",
        transactionTypeId = 2
    )

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

    private class FakeRecurringRuleRepository(
        private val occurrences: MutableStateFlow<List<InstallmentOccurrence>> = MutableStateFlow(emptyList())
    ) : RecurringRuleRepository {
        override fun observeActiveRecurringRules(): Flow<List<RecurringTransactionRule>> = flowOf(emptyList())
        override suspend fun getActiveRules(): List<RecurringTransactionRule> = emptyList()
        override suspend fun getActiveByTransactionId(transactionId: String): RecurringTransactionRule? = null
        override suspend fun upsertRule(rule: RecurringTransactionRule): RecurringTransactionRule = rule
        override suspend fun setEnabled(id: String, enabled: Boolean) = Unit
        override suspend fun setNotificationsEnabled(id: String, enabled: Boolean) = Unit
        override suspend fun deleteRule(id: String) = Unit
        override fun observeOccurrences(ruleId: String): Flow<List<InstallmentOccurrence>> = occurrences
        override fun observeAllOccurrences(): Flow<List<InstallmentOccurrence>> = occurrences
        override suspend fun getOccurrences(ruleId: String): List<InstallmentOccurrence> = emptyList()
        override suspend fun getInstallmentPlan(ruleId: String): InstallmentPlan? = null
        override suspend fun convertToInstallment(
            ruleId: String,
            totalAmountMinor: Long,
            installmentAmountMinor: Long,
            totalInstallments: Int,
            firstDueAt: Long
        ): RecurringTransactionRule? = null
        override suspend fun convertToRegular(ruleId: String): RecurringTransactionRule? = null
        override suspend fun payInstallment(occurrenceId: String, paidAt: Long): InstallmentOccurrence? = null
        override suspend fun payInstallments(occurrenceIds: List<String>, paidAt: Long): Int = 0
        override suspend fun settleOccurrenceWithTransaction(
            occurrenceId: String,
            transactionId: String,
            paidAt: Long
        ): InstallmentOccurrence? = null
        override suspend fun skipInstallment(occurrenceId: String): InstallmentOccurrence? = null
        override suspend fun undoInstallment(occurrenceId: String): InstallmentOccurrence? = null
        override suspend fun reconcileDueInstallments(ruleId: String, now: Long): List<String> = emptyList()
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
