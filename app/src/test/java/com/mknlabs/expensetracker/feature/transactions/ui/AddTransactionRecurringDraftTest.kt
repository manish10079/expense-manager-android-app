package com.mknlabs.expensetracker.feature.transactions.ui

import com.mknlabs.expensetracker.models.RecurringFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Locks the contract Add Transaction's save path relies on when a loan is created
 * straight from the form: a REGULAR rule keeps behaving exactly as before, and an
 * EMI only ever carries a plan whose terms add up — an inconsistent or half-filled
 * plan surfaces as `plan == null` (which the screen treats as unsaveable) instead
 * of silently creating an ordinary recurring rule.
 */
class AddTransactionRecurringDraftTest {

    private fun draft(
        isRecurringEnabled: Boolean = true,
        frequency: RecurringFrequency = RecurringFrequency.Monthly,
        repeatCount: Int? = 12,
        isInstallmentSelected: Boolean = false,
        totalAmount: Double = 0.0,
        installmentAmount: Double = 0.0,
        firstDueAt: Long = FIRST_DUE
    ) = buildRecurringDraft(
        isRecurringEnabled = isRecurringEnabled,
        frequency = frequency,
        repeatCount = repeatCount,
        isInstallmentSelected = isInstallmentSelected,
        totalAmount = totalAmount,
        installmentAmount = installmentAmount,
        firstDueAt = firstDueAt
    )

    @Test
    fun recurringOffOrUnusableCount_producesNoDraft() {
        assertNull("switch off", draft(isRecurringEnabled = false))
        assertNull("blank count", draft(repeatCount = null))
        assertNull("zero count", draft(repeatCount = 0))
    }

    @Test
    fun regularRule_carriesNoPlan() {
        val regular = draft(isInstallmentSelected = false, repeatCount = 6)!!

        assertEquals(RecurringFrequency.Monthly, regular.frequency)
        assertEquals(6, regular.repeatCount)
        assertNull(regular.plan)
    }

    @Test
    fun consistentPlan_becomesAnInstallmentPlanInMinorUnits() {
        val emi = draft(
            isInstallmentSelected = true,
            repeatCount = 12,
            totalAmount = 60000.0,
            installmentAmount = 5000.0
        )!!

        assertNotNull(emi.plan)
        val plan = emi.plan!!
        assertEquals(6_000_000L, plan.totalAmountMinor)
        assertEquals(500_000L, plan.installmentAmountMinor)
        assertEquals(12, plan.totalInstallments)
        assertEquals(FIRST_DUE, plan.firstDueAt)
        // The count the plan materializes is the one on the rule's repeat count —
        // the ledger and the series can never disagree about how many there are.
        assertEquals(emi.repeatCount, plan.totalInstallments)
    }

    @Test
    fun planIsComparedInMinorUnits_soDecimalNoiseCannotRejectIt() {
        // 1234.56 × 3 = 3703.68: exactly what a user types, but not exactly what
        // binary floating point multiplies to.
        val emi = draft(
            isInstallmentSelected = true,
            repeatCount = 3,
            totalAmount = 3703.68,
            installmentAmount = 1234.56
        )!!

        assertNotNull(emi.plan)
        assertEquals(370_368L, emi.plan!!.totalAmountMinor)
        assertEquals(123_456L, emi.plan.installmentAmountMinor)
    }

    private fun planOf(
        repeatCount: Int? = 12,
        totalAmount: Double = 0.0,
        installmentAmount: Double = 0.0
    ) = draft(
        isInstallmentSelected = true,
        repeatCount = repeatCount,
        totalAmount = totalAmount,
        installmentAmount = installmentAmount
    )?.plan

    @Test
    fun inconsistentOrHalfFilledPlan_yieldsNoPlan() {
        assertNull("nothing typed", planOf())
        assertNull(
            "total does not match installment × count",
            planOf(totalAmount = 60000.0, installmentAmount = 4000.0)
        )
        assertNull("installment missing", planOf(totalAmount = 60000.0))
        assertNull(
            "count is unusable, so the plan cannot be sized",
            planOf(repeatCount = 0, totalAmount = 60000.0, installmentAmount = 5000.0)
        )
    }

    @Test
    fun switchingBackToRegular_dropsTheRetainedPlanFields() {
        // The user types the plan, then changes the type back to Regular: the
        // saved rule must be a plain recurring rule, not a loan.
        val regular = draft(
            isInstallmentSelected = false,
            repeatCount = 12,
            totalAmount = 60000.0,
            installmentAmount = 5000.0
        )!!

        assertNull(regular.plan)
        assertEquals(12, regular.repeatCount)
    }

    companion object {
        private const val FIRST_DUE = 1_700_000_000_000L
    }
}
