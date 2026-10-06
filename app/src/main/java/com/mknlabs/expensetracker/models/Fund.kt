package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits

/**
 * A pool of real money the user sets aside (a gift, a scholarship, trip money) and then
 * spends down across one or more transactions.
 *
 * Distinct from [Budget], which caps spending *by category*: a fund is a named bucket of
 * money, and it is exhausted when the sum of the expenses linked to it reaches
 * [amountMinor]. Neither the spent total nor the remaining balance is stored — both are
 * derived from the linked transactions (see [FundWithProgress]) so two devices cannot
 * disagree about a fund's state.
 *
 * Creating a fund always also creates one linked income transaction for [amountMinor]; its
 * id carries no reference to it (the transaction points *at* the fund, not the reverse), so
 * the relationship stays a single column on `transactions` rather than a second table.
 */
@Immutable
data class Fund(
    val id: String,
    val name: String,
    val amountMinor: Long,
    val startDate: Long,
    val iconKey: String,
    val colorHex: String,
    val note: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = createdAt,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.PENDING_UPLOAD
) {
    constructor(
        id: String,
        name: String,
        amount: Double,
        startDate: Long,
        iconKey: String,
        colorHex: String,
        note: String = "",
        isArchived: Boolean = false,
        createdAt: Long = System.currentTimeMillis(),
        updatedAt: Long = createdAt,
        isDeleted: Boolean = false,
        syncState: SyncState = SyncState.PENDING_UPLOAD
    ) : this(
        id = id,
        name = name,
        amountMinor = amount.toMinorUnits(),
        startDate = startDate,
        iconKey = iconKey,
        colorHex = colorHex,
        note = note,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        syncState = syncState
    )

    val amount: Double
        get() = amountMinor.toMajorUnits()

    val icon: ImageVector
        get() = ExpenseTrackerIconRegistry.iconForKey(iconKey)
}

/**
 * A fund together with the total spent against it, which is what every fund surface
 * actually renders. The balance, progress and status are all derived here so the list, the
 * detail screen and the analytics breakdown cannot drift apart.
 */
@Immutable
data class FundWithProgress(
    val fund: Fund,
    val spentMinor: Long
) {
    val remainingMinor: Long
        get() = (fund.amountMinor - spentMinor).coerceAtLeast(0L)

    val isExhausted: Boolean
        get() = fund.amountMinor > 0 && spentMinor >= fund.amountMinor

    val isOverspent: Boolean
        get() = spentMinor > fund.amountMinor

    val progress: Float
        get() = if (fund.amountMinor > 0) {
            (spentMinor.toFloat() / fund.amountMinor.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val status: FundStatus
        get() = when {
            fund.isArchived -> FundStatus.Archived
            isExhausted -> FundStatus.Completed
            else -> FundStatus.Active
        }
}

enum class FundStatus {
    /** Money left to spend. */
    Active,

    /** The full amount has been spent. */
    Completed,

    /** Manually filed away by the user; its money no longer counts toward the summary. */
    Archived
}
