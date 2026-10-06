package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.SyncState

import com.google.firebase.firestore.PropertyName

/**
 * A cash bucket the user spends down over time.
 *
 * Carries the same sync bookkeeping as every other synced table, so it needs no special
 * case in the push/pull code: `sync_state` is what `getUnsynced()` selects on, `is_deleted`
 * is the tombstone a delete leaves for the other devices to read, and `updated_at` is the
 * watermark their pull filters on.
 *
 * The spent and remaining amounts are deliberately **not** columns. They are sums over the
 * transactions that point back at this row via `transactions.fund_id`, which keeps a fund's
 * balance a fact about its transactions rather than a second number the two can disagree
 * about after an edit or a sync.
 */
@Entity(
    tableName = "funds",
    indices = [
        Index(value = ["is_deleted", "start_date"]),
        Index(value = ["is_deleted", "is_archived"])
    ]
)
data class FundEntity(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long = 0L,
    @ColumnInfo(name = "start_date")
    val startDate: Long = 0L,
    @ColumnInfo(name = "icon_key")
    val iconKey: String = "",
    @ColumnInfo(name = "color_hex")
    val colorHex: String = "",
    val note: String = "",
    @get:PropertyName("isArchived")
    @field:PropertyName("isArchived")
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.SYNCED
)
