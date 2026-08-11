package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecurringTransactionUtilsTest {

    @Test
    fun autoCreatedInstance_isRecurring_evenWithNoRulesLoaded() {
        val instance = testTransaction(sourceRecurringRuleId = "rule-1")

        assertTrue(isRecurringTransaction(instance, emptyList()))
    }

    @Test
    fun templateTransaction_matchingAnActiveRule_isRecurring() {
        val template = testTransaction(id = "tx-1")
        val rule = testRule(transactionId = "tx-1")

        assertTrue(isRecurringTransaction(template, listOf(rule)))
    }

    @Test
    fun regularTransaction_withNoRules_isNotRecurring() {
        val transaction = testTransaction(id = "tx-9")

        assertFalse(isRecurringTransaction(transaction, emptyList()))
    }

    @Test
    fun regularTransaction_whoseIdMatchesNoRule_isNotRecurring() {
        val transaction = testTransaction(id = "tx-1")
        val unrelatedRule = testRule(transactionId = "tx-2")

        assertFalse(isRecurringTransaction(transaction, listOf(unrelatedRule)))
    }

    @Test
    fun autoCreatedInstance_matchingTemplateRule_isRecurring() {
        val instance = testTransaction(id = "tx-1-instance", sourceRecurringRuleId = "rule-1")
        val rule = testRule(id = "rule-1", transactionId = "tx-1")

        assertTrue(isRecurringTransaction(instance, listOf(rule)))
    }

    @Test
    fun ruleBelongingToADifferentTransaction_doesNotMarkTemplateAsRecurring() {
        val template = testTransaction(id = "tx-1")
        val ruleForAnotherTransaction = testRule(transactionId = "tx-2")

        assertFalse(isRecurringTransaction(template, listOf(ruleForAnotherTransaction)))
    }

    private fun testTransaction(
        id: String = "tx-0",
        sourceRecurringRuleId: String? = null
    ): Transaction {
        return Transaction(
            id = id,
            note = "",
            createdAt = 0L,
            amountMinor = 0L,
            transactionTypeId = 2,
            paymentTypeId = 0,
            categoryId = 0,
            sourceRecurringRuleId = sourceRecurringRuleId
        )
    }

    private fun testRule(
        id: String = "rule-0",
        transactionId: String = "tx-0"
    ): RecurringTransactionRule {
        return RecurringTransactionRule(
            id = id,
            transactionId = transactionId,
            frequency = RecurringFrequency.Monthly,
            repeatCount = 12,
            isEnabled = true
        )
    }
}
