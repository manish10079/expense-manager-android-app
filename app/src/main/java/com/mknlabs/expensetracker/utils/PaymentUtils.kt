package com.mknlabs.expensetracker.utils

import com.mknlabs.expensetracker.data.constants.paymentTypeMap

fun getPaymentTypeName(paymentId: Int): String {
    return paymentTypeMap[paymentId]?.name ?: "Unknown"
}