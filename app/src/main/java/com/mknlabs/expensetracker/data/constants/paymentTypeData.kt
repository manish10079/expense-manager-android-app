package com.mknlabs.expensetracker.data.constants

import com.mknlabs.expensetracker.models.PaymentType

val paymentTypeMap = mapOf(
    1 to PaymentType(1, "UPI", "qr-code", sortOrder = 1),
    2 to PaymentType(2, "Cash", "banknote", sortOrder = 2),
    3 to PaymentType(3, "Bank", "building-2", sortOrder = 3),
    4 to PaymentType(4, "Card", "credit-card", sortOrder = 4),
    5 to PaymentType(5, "Other", "more-horizontal", sortOrder = 5),
    6 to PaymentType(6, "Salary", "wallet", sortOrder = 6)
)
