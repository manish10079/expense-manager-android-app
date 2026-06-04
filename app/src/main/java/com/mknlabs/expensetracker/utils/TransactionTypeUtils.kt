package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.data.constants.transactionTypeMap

fun getTransactionTypeName(id: Int): String {
    return transactionTypeMap[id]?.name ?: "Unknown"
}