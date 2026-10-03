package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import com.mknlabs.expensetracker.models.SyncState

/**
 * One user-defined tag, applied to any number of transactions through
 * [TransactionTagEntity].
 *
 * Ids are UUID strings rather than the small ints the seeded tables use. A tag is
 * created by the user, on any device, at any time, so there is no central place to
 * hand out the next number — and two devices that each minted `12` while offline
 * would collide on the first sync. A UUID is unique without coordination.
 *
 * [nameLower] is what makes `#NYC` and `#nyc` the same tag. SQLite's `LIKE` is only
 * case-insensitive for ASCII, and `COLLATE NOCASE` shares that limit, so neither can
 * be trusted to catch `#Café` against `#CAFÉ`. Storing an explicitly lower-cased copy
 * and putting the unique index on it makes the rule the app's own rather than a
 * collation's, and makes `findByNameIgnoreCase` an index lookup instead of a scan.
 *
 * [colorHex] follows the category/payment-method contract: `#RRGGBB`, nullable, null
 * meaning "no colour of the user's own" rather than an empty string that could not be
 * told apart from a truncated write. Tags are never seeded, so — unlike a category —
 * a null here is simply the state of a tag whose colour nobody has chosen yet.
 */
@Entity(
    tableName = "tags",
    indices = [
        Index(value = ["name_lower"], unique = true),
        Index(value = ["is_deleted", "name_lower"])
    ]
)
data class TagEntity(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    @ColumnInfo(name = "name_lower")
    val nameLower: String = "",
    @ColumnInfo(name = "color_hex")
    val colorHex: String? = null,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.PENDING_UPLOAD,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L
)