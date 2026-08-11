package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction

/**
 * Returns true when [transaction] is part of a recurring series: either it was
 * auto-created by a recurring rule (its `sourceRecurringRuleId` is set) or it is
 * the main/template transaction of one of the active [recurringRules].
 */
fun isRecurringTransaction(
    transaction: Transaction,
    recurringRules: List<RecurringTransactionRule>
): Boolean =
    transaction.sourceRecurringRuleId != null ||
        recurringRules.any { it.transactionId == transaction.id }
