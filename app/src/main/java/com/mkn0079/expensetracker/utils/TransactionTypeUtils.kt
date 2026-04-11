package com.mkn0079.expensetracker.utils

import com.mkn0079.expensetracker.data.constants.transactionTypeMap

fun getTransactionTypeName(id: Int): String {
    return transactionTypeMap[id]?.name ?: "Unknown"
}