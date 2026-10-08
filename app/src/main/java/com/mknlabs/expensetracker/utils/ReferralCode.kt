package com.mknlabs.expensetracker.utils

fun referralCodeFromUid(uid: String): String {
    if (uid.length < 8) return uid.uppercase()
    return (uid.take(4) + uid.takeLast(4)).uppercase()
}
