package com.mkn0079.expensetracker.data.constants

import com.mkn0079.expensetracker.models.PaymentType

val paymentTypeMap = mapOf(
    1 to PaymentType(1, "UPI", "qr_code", sortOrder = 1),
    2 to PaymentType(2, "Cash", "payments", sortOrder = 2),
    3 to PaymentType(3, "Bank", "assured_workload", sortOrder = 3),
    4 to PaymentType(4, "Card", "credit_card", sortOrder = 4),
    5 to PaymentType(5, "Other", "more_horiz", sortOrder = 5),
    6 to PaymentType(6, "Salary", "account_balance_wallet", sortOrder = 6)
)
