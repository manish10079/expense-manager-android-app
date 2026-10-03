package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.google.firebase.firestore.PropertyName
import com.mknlabs.expensetracker.models.SyncState

/**
 * The join row that applies one [TagEntity] to one [TransactionEntity] — the
 * many-to-many the tagging feature is built on.
 *
 * The primary key is the pair, not a surrogate id: a tag can be applied to a
 * transaction at most once, and stating that as the key makes a double-tag attempt an
 * upsert rather than a duplicate row that every reader would have to de-duplicate.
 *
 * Both columns are indexed on top of the composite primary key. The composite key
 * already covers `transaction_id`-first lookups (loading one transaction's tags), but
 * the reverse — "every transaction carrying this tag", which is what a tag filter and
 * the per-tag statistics both ask — leads with `tag_id` and needs its own index or it
 * scans the table. That index is the one the PRD's performance requirement is about.
 *
 * Both foreign keys cascade. A tag's links are meaningless without the tag, and a
 * hard-deleted transaction's links are meaningless without the transaction. Note this
 * is about *hard* deletes: the app soft-deletes both, and a tag filter joins through
 * live rows only, so a soft-deleted tag's links linger harmlessly until the tag is
 * purged. That is deliberate — un-deleting a tag must bring its tagging back with it.
 */
@Entity(
    tableName = "transaction_tags",
    primaryKeys = ["transaction_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tag_id"]),
        Index(value = ["transaction_id"])
    ]
)
data class TransactionTagEntity(
    @ColumnInfo(name = "transaction_id")
    val transactionId: String = "",
    @ColumnInfo(name = "tag_id")
    val tagId: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,
    @ColumnInfo(name = "sync_state")
    val syncState: SyncState = SyncState.PENDING_UPLOAD,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)