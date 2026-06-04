package com.mknlabs.expensetracker.utils

import java.math.BigDecimal
import java.math.RoundingMode

private const val MINOR_UNITS_SCALE = 2
private val MINOR_UNIT_DIVISOR = BigDecimal("100")

fun Double.toMinorUnits(): Long {
    return BigDecimal.valueOf(this)
        .setScale(MINOR_UNITS_SCALE, RoundingMode.HALF_UP)
        .multiply(MINOR_UNIT_DIVISOR)
        .longValueExact()
}

fun Long.toMajorUnits(): Double {
    return BigDecimal.valueOf(this)
        .divide(MINOR_UNIT_DIVISOR, MINOR_UNITS_SCALE, RoundingMode.HALF_UP)
        .toDouble()
}
