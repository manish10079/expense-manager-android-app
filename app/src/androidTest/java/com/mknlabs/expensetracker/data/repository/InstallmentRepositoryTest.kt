package com.mknlabs.expensetracker.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

/**
 * Exercises the installment mutations against a real (in-memory) Room database
 * — DAOs, converters and mappers included — which is where the backward-compat
 * guarantees actually live: deterministic slot ids, reversible conversions,
 * pay/undo mirroring the ledger, and worker reconciliation.
 */
@RunWith(AndroidJUnit4::class)
class InstallmentRepositoryTest {

    private lateinit var db: ExpenseTrackerDatabase
    private lateinit var repository: RecurringRuleRepository

    private val dayMillis = 24 * 60 * 60 * 1000L
    private val anchor = Calendar.getInstance().apply {
        clear(); set(2024, Calendar.JANUARY, 10, 10, 0, 0)
    }.timeInMillis

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExpenseTrackerDatabase::class.java).build()
        repository = RecurringRuleRepository(db.recurringRuleDao(), db.installmentOccurrenceDao(), db.transactionDao())

        db.categoryDao().upsert(
            CategoryType(id = 1, name = "EMIs", iconKey = "card", transactionTypeId = 2).toEntity()
        )
        db.paymentMethodDao().upsert(
            PaymentType(id = 1, name = "UPI", iconKey = "upi").toEntity()
        )
        db.transactionDao().upsert(
            Transaction(
                id = TEMPLATE_ID,
                note = "laptop emi",
                createdAt = anchor,
                amountMinor = 60_000L,
                transactionTypeId = 2,
                paymentTypeId = 1,
                categoryId = 1,
                syncState = SyncState.SYNCED
            ).toEntity()
        )
        repository.upsertRule(
            RecurringTransactionRule(
                id = RULE_ID,
                transactionId = TEMPLATE_ID,
                frequency = RecurringFrequency.Monthly,
                repeatCount = 3,
                isEnabled = true,
                remainingCount = 3,
                anchorAt = anchor,
                nextRunAt = anchor
            )
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun convertToPlan(count: Int = 3) {
        repository.convertToInstallment(
            ruleId = RULE_ID,
            totalAmountMinor = 10_000L * count,
            installmentAmountMinor = 10_000L,
            totalInstallments = count,
            firstDueAt = anchor
        )
    }

    @Test
    fun payInstallment_createsLinkedTransaction_andAdvancesPlan() = runTest {
        convertToPlan()
        val slotId = "${RULE_ID}_occ_1"

        val paid = repository.payInstallment(slotId, paidAt = anchor + dayMillis)

        assertNotNull(paid)
        assertEquals("PAID", paid!!.status.name)
        assertEquals(anchor + dayMillis, paid.paidAt)
        // The slot's own transaction exists, linked back on the occurrence.
        val transaction = db.transactionDao().getById(slotId)
        assertNotNull(transaction)
        assertEquals(10_000L, transaction!!.amountMinor) // slot amount, not template
        assertEquals(anchor + dayMillis, transaction.occurredAt)
        assertEquals(RULE_ID, transaction.sourceRecurringRuleId)
        assertFalse(transaction.isDeleted)
        // The template is untouched.
        assertEquals(60_000L, db.transactionDao().getById(TEMPLATE_ID)!!.amountMinor)

        val plan = repository.getInstallmentPlan(RULE_ID)!!
        assertEquals(1, plan.paidInstallments)
        // 30k total - 10k paid = 20k remaining; next due is slot 2.
        assertEquals(20_000L, plan.remainingAmountMinor)
        assertNotNull(plan.nextDueAt)
        assertEquals("ACTIVE", plan.status.name)
    }

    @Test
    fun payInstallment_isIdempotent_doublePayCreatesNoSecondTransaction() = runTest {
        convertToPlan()
        val slotId = "${RULE_ID}_occ_1"

        repository.payInstallment(slotId, paidAt = anchor)
        val second = repository.payInstallment(slotId, paidAt = anchor)

        assertNull("already-settled slot must not pay again", second)
        // Exactly one transaction exists for the slot.
        assertEquals(1, repository.getOccurrences(RULE_ID).count { it.status.name == "PAID" })
    }

    @Test
    fun skipKeepsBalanceAndUndoRestoresSlotAndSoftDeletesTransaction() = runTest {
        convertToPlan()
        val slotId = "${RULE_ID}_occ_2"

        repository.skipInstallment(slotId)
        var plan = repository.getInstallmentPlan(RULE_ID)!!
        assertEquals(1, plan.skippedInstallments)
        // Skipped money is still owed.
        assertEquals(30_000L, plan.remainingAmountMinor)

        // A settled slot never pays directly — it must first return to PENDING
        // (this is the same guard that makes double-pay a no-op).
        assertNull(repository.payInstallment(slotId, paidAt = anchor))
        assertNotNull(repository.undoInstallment(slotId))

        repository.payInstallment(slotId, paidAt = anchor)
        plan = repository.getInstallmentPlan(RULE_ID)!!
        assertEquals(20_000L, plan.remainingAmountMinor)

        val undone = repository.undoInstallment(slotId)
        assertEquals("PENDING", undone!!.status.name)
        assertNull(undone.transactionId)
        // The generated transaction is withdrawn from the ledger, slot-scoped.
        assertTrue(db.transactionDao().getById(slotId)!!.isDeleted)
        assertFalse("template must survive undo", db.transactionDao().getById(TEMPLATE_ID)!!.isDeleted)
        assertEquals(30_000L, repository.getInstallmentPlan(RULE_ID)!!.remainingAmountMinor)
    }

    @Test
    fun payingLastInstallment_persistsCompletedStatusOnRule() = runTest {
        convertToPlan(count = 2)

        repository.payInstallments(listOf("${RULE_ID}_occ_1", "${RULE_ID}_occ_2"), paidAt = anchor)

        val rule = repository.getActiveRules().first { it.id == RULE_ID }
        assertEquals("COMPLETED", rule.installmentStatus!!.name)
        assertTrue(repository.getInstallmentPlan(RULE_ID)!!.isCompleted)
        assertEquals(0L, repository.getInstallmentPlan(RULE_ID)!!.remainingAmountMinor)

        // Undo one payment: the stored status must fall back so it cannot
        // disagree with the derived plan.
        repository.undoInstallment("${RULE_ID}_occ_2")
        val revived = repository.getActiveRules().first { it.id == RULE_ID }
        assertEquals("ACTIVE", revived.installmentStatus!!.name)
    }

    @Test
    fun regularToInstallmentRoundTrip_preservesPaidStateAmountsAndLinks() = runTest {
        convertToPlan(count = 3)
        repository.payInstallment("${RULE_ID}_occ_1", paidAt = anchor)
        repository.skipInstallment("${RULE_ID}_occ_2")

        val converted = repository.convertToRegular(RULE_ID)
        assertEquals("REGULAR", converted!!.recurringType.name)
        // REGULAR rules expose no plan, and the worker path ignores slots.
        assertNull(repository.getInstallmentPlan(RULE_ID))
        assertEquals(0, repository.getOccurrences(RULE_ID).size)

        // Convert back — plan terms retained on the rule, slots revived.
        repository.convertToInstallment(
            ruleId = RULE_ID,
            totalAmountMinor = 30_000L,
            installmentAmountMinor = 10_000L,
            totalInstallments = 3,
            firstDueAt = anchor
        )

        val occurrences = repository.getOccurrences(RULE_ID)
        assertEquals(3, occurrences.size)
        val slot1 = occurrences.first { it.installmentIndex == 1 }
        assertEquals("PAID", slot1.status.name)
        assertEquals(anchor, slot1.paidAt)
        assertEquals("${RULE_ID}_occ_1", slot1.transactionId)
        assertEquals(10_000L, slot1.amountMinor)
        assertEquals("SKIPPED", occurrences.first { it.installmentIndex == 2 }.status.name)
        val plan = repository.getInstallmentPlan(RULE_ID)!!
        assertEquals(1, plan.paidInstallments)
        assertEquals(1, plan.skippedInstallments)
    }

    @Test
    fun reconcileDueInstallments_paysOnlyPastDueSlots_atTheirDueDates() = runTest {
        convertToPlan(count = 3)
        // anchor = slot 1 due, anchor+1d would be inside the month — use now = slot 1 + half a period
        val now = anchor + dayMillis

        val notes = repository.reconcileDueInstallments(RULE_ID, now)

        assertEquals(1, notes.size)
        assertEquals("laptop emi", notes.first())
        val occurrences = repository.getOccurrences(RULE_ID)
        val slot1 = occurrences.first { it.installmentIndex == 1 }
        assertEquals("PAID", slot1.status.name)
        assertEquals("settled at its own due date, not now", anchor, slot1.paidAt)
        assertEquals(
            "later slots untouched", "PENDING",
            occurrences.first { it.installmentIndex == 2 }.status.name
        )

        // Re-run: nothing new due, nothing re-created.
        assertTrue(repository.reconcileDueInstallments(RULE_ID, now).isEmpty())
        // Running far into the future settles the rest, in order.
        val rest = repository.reconcileDueInstallments(RULE_ID, now + 365 * dayMillis)
        assertEquals(2, rest.size)
    }

    @Test
    fun everySettlementPathKeepsRuleScheduleInSyncWithTheLedger() = runTest {
        convertToPlan(count = 3)
        val dueDates = repository.getOccurrences(RULE_ID).associate { it.installmentIndex to it.dueAt }

        suspend fun rule() = repository.getActiveRules().first { it.id == RULE_ID }

        // Conversion adopts the ledger as the schedule.
        assertEquals(dueDates[1], rule().nextRunAt)
        assertEquals(3, rule().remainingCount)
        assertTrue(rule().isEnabled)

        // Paying the tracked slot advances the tracked due date to the next one.
        repository.payInstallment("${RULE_ID}_occ_1", paidAt = dueDates[1]!!)
        assertEquals(dueDates[2], rule().nextRunAt)
        assertEquals(2, rule().remainingCount)

        // A skip leaves the ledger too, so the plan waits on the following slot.
        repository.skipInstallment("${RULE_ID}_occ_2")
        assertEquals(dueDates[3], rule().nextRunAt)
        assertEquals(1, rule().remainingCount)

        // Settling the last slot closes the plan: nothing remains owed, so the
        // rule disables itself and stops alerting on a finished loan.
        repository.payInstallment("${RULE_ID}_occ_3", paidAt = dueDates[3]!!)
        assertEquals(0, rule().remainingCount)
        assertFalse("a fully settled plan must not keep alerting", rule().isEnabled)

        // Undo re-opens the slot: the schedule follows it back and the rule we
        // auto-disabled is revived (a manually muted rule would not be).
        repository.undoInstallment("${RULE_ID}_occ_3")
        assertEquals(dueDates[3], rule().nextRunAt)
        assertEquals(1, rule().remainingCount)
        assertTrue(rule().isEnabled)

        // Converting back mid-plan keeps the ledger-derived schedule, so the
        // series continues on the remaining payment rather than restarting.
        val converted = repository.convertToRegular(RULE_ID)!!
        assertEquals("REGULAR", converted.recurringType.name)
        assertEquals(dueDates[3], converted.nextRunAt)
        assertEquals(1, converted.remainingCount)
        assertTrue(converted.isEnabled)
    }

    @Test
    fun reTimingALoanReDatesPendingSlotsAndKeepsSettledOnes() = runTest {
        convertToPlan(count = 3)
        repository.payInstallment("${RULE_ID}_occ_1", paidAt = anchor)

        // The same sequence AddTransaction's save runs when the user re-times an
        // EMI rule: the rule's frequency is written first, then the plan is
        // rebuilt from the retained terms with the earliest slot as its anchor.
        repository.upsertRule(
            repository.getActiveRules().first { it.id == RULE_ID }.copy(frequency = RecurringFrequency.Weekly)
        )
        val converted = repository.convertToInstallment(
            ruleId = RULE_ID,
            // total = per × count, the invariant the EMI editor enforces.
            totalAmountMinor = 20_000L,
            installmentAmountMinor = 10_000L,
            totalInstallments = 2,
            firstDueAt = repository.getOccurrences(RULE_ID).minOf { it.dueAt }
        )

        val occurrences = repository.getOccurrences(RULE_ID)
        assertEquals("the shorter plan drops the leftover slot", 2, occurrences.size)
        val slot1 = occurrences.first { it.installmentIndex == 1 }
        assertEquals("the paid slot is revived, not reset", "PAID", slot1.status.name)
        assertEquals(anchor, slot1.paidAt)
        assertEquals("${RULE_ID}_occ_1", slot1.transactionId)
        assertEquals("settled money is still counted", 10_000L, repository.getInstallmentPlan(RULE_ID)!!.totalPaidMinor)

        // The pending slot follows the NEW frequency from the same first due date.
        val slot2 = occurrences.first { it.installmentIndex == 2 }
        assertEquals(anchor + 7 * dayMillis, slot2.dueAt)
        assertEquals("the ledger drives the schedule", slot2.dueAt, converted!!.nextRunAt)
        assertEquals(1, converted.remainingCount)
        assertEquals(2, converted.installmentTotalCount)
        assertEquals("INSTALLMENT", converted.recurringType.name)
    }

    @Test
    fun reconcileClosesThePlanOnceTheFinalOverdueSlotIsPaid() = runTest {
        convertToPlan(count = 2)

        repository.reconcileDueInstallments(RULE_ID, now = anchor + 365 * dayMillis)

        val rule = repository.getActiveRules().first { it.id == RULE_ID }
        assertEquals("COMPLETED", rule.installmentStatus!!.name)
        assertEquals(0, rule.remainingCount)
        assertFalse(rule.isEnabled)
    }

    @Test
    fun creatingAPlanAdoptsTheSavedTransactionAsInstallmentOne() = runTest {
        // The Add Transaction create-EMI path, end to end: the transaction is
        // saved first, the plan is materialized with its first due date on the
        // transaction's own date, and slot 1 is then linked to that transaction.
        convertToPlan(count = 3)
        val slot1Id = "${RULE_ID}_occ_1"

        val adopted = repository.settleOccurrenceWithTransaction(
            occurrenceId = slot1Id,
            transactionId = TEMPLATE_ID,
            paidAt = anchor
        )

        assertNotNull(adopted)
        assertEquals("PAID", adopted!!.status.name)
        assertEquals(anchor, adopted.paidAt)
        // Pointed at the transaction the user already recorded — not a copy of it.
        assertEquals(TEMPLATE_ID, adopted.transactionId)
        assertEquals(1, db.transactionDao().getAllTransactions().size)
        assertEquals(60_000L, db.transactionDao().getById(TEMPLATE_ID)!!.amountMinor)

        val plan = repository.getInstallmentPlan(RULE_ID)!!
        assertEquals(1, plan.paidInstallments)
        assertEquals(20_000L, plan.remainingAmountMinor)

        // The ledger drives the schedule, and nothing is left due on the day the
        // transaction was recorded — so the worker's run right after the save
        // cannot pay the same installment a second time.
        val dueDates = repository.getOccurrences(RULE_ID).associate { it.installmentIndex to it.dueAt }
        val rule = repository.getActiveRules().first { it.id == RULE_ID }
        assertEquals(dueDates[2], rule.nextRunAt)
        assertEquals(2, rule.remainingCount)
        assertTrue(repository.reconcileDueInstallments(RULE_ID, now = anchor).isEmpty())

        // Idempotent: the adopted slot never settles twice.
        assertNull(repository.settleOccurrenceWithTransaction(slot1Id, TEMPLATE_ID, anchor))
        assertNull(repository.settleOccurrenceWithTransaction("${RULE_ID}_occ_9", TEMPLATE_ID, anchor))
    }

    companion object {
        private const val RULE_ID = "rule-test-1"
        private const val TEMPLATE_ID = "tx-template-1"
    }
}
