package com.mknlabs.expensetracker.utils

/**
 * At most one of permission / refer / rate per process launch.
 * Call [resolve] once when eligibility is known.
 */
object LaunchSheetPick {

    enum class Kind { Permission, Refer, Rate }

    fun resolve(
        permissionDue: Boolean,
        referDue: Boolean,
        rateDue: Boolean,
    ): Kind? {
        val due = buildList {
            if (permissionDue) add(Kind.Permission)
            if (referDue) add(Kind.Refer)
            if (rateDue) add(Kind.Rate)
        }
        if (due.isEmpty()) return null
        return due.random()
    }
}
