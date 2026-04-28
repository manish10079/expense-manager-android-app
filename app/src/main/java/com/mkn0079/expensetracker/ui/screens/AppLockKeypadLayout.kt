package com.mkn0079.expensetracker.ui.screens

import kotlin.random.Random

internal const val APP_LOCK_FORGOT_KEY = "forgot"
internal const val APP_LOCK_DELETE_KEY = "delete"

internal fun buildAppLockKeypadLayout(
    scrambled: Boolean,
    random: Random = Random.Default
): List<List<String>> {
    if (!scrambled) {
        return listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(APP_LOCK_FORGOT_KEY, "0", APP_LOCK_DELETE_KEY)
        )
    }

    val shuffledDigits = (0..9).map(Int::toString).shuffled(random)
    return listOf(
        listOf(shuffledDigits[0], shuffledDigits[1], shuffledDigits[2]),
        listOf(shuffledDigits[3], shuffledDigits[4], shuffledDigits[5]),
        listOf(shuffledDigits[6], shuffledDigits[7], shuffledDigits[8]),
        listOf(APP_LOCK_FORGOT_KEY, shuffledDigits[9], APP_LOCK_DELETE_KEY)
    )
}
