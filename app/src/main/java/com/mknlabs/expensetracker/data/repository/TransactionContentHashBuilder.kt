package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.models.Transaction
import java.security.MessageDigest

object TransactionContentHashBuilder {

    fun build(transaction: Transaction): String {
        val raw = listOf(
            transaction.note.trim(),
            transaction.amountMinor.toString(),
            transaction.createdAt.toString(),
            transaction.transactionTypeId.toString(),
            transaction.paymentTypeId.toString(),
            transaction.categoryId.toString(),
            transaction.sourceRecurringRuleId.orEmpty()
        ).joinToString(separator = "|")
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
