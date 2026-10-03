package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

/**
 * A user-defined label applied to any number of transactions.
 *
 * [id] is a UUID string; see
 * [TagEntity][com.mknlabs.expensetracker.data.local.room.entities.TagEntity] for why a tag
 * is not keyed by a small int the way categories are.
 *
 * [name] keeps the casing the user typed. Case is a display choice, not identity — `#NYC`
 * and `#nyc` are the same tag, and that rule is enforced in storage and in the repository
 * rather than here, so the model never has to decide which spelling wins.
 */
@Immutable
data class Tag(
    val id: String,
    val name: String,
    val colorHex: String? = null,
    val isDeleted: Boolean = false,
    val syncState: SyncState = SyncState.PENDING_UPLOAD,
    val createdAt: Long = 0L,
    val updatedAt: Long = createdAt
)

/**
 * How many transactions use a tag and how much expense they represent.
 *
 * Shown on the Tag Management screen. [expenseMinor] counts expense-type transactions
 * only, matching the app's convention that income is not spending; [transactionCount]
 * counts every live transaction carrying the tag, income included.
 */
@Immutable
data class TagStats(
    val tagId: String,
    val transactionCount: Int,
    val expenseMinor: Long
)

/**
 * How multiple selected tags combine when filtering the ledger.
 *
 * [AND] means a transaction must carry every selected tag; [OR] means it must carry at
 * least one. Both are first-class in the PRD, and the choice is the user's — the filter
 * sheet exposes it as a toggle rather than fixing one behaviour.
 */
enum class TagMatchMode {
    AND,
    OR
}