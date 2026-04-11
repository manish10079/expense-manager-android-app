package com.mkn0079.expensetracker.utils

import com.mkn0079.expensetracker.data.constants.paymentTypeMap

fun getPaymentTypeName(paymentId: Int): String {
    return paymentTypeMap[paymentId]?.name ?: "Unknown"
}