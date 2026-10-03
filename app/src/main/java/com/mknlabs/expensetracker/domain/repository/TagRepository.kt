package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.models.TagStats
import kotlinx.coroutines.flow.Flow

/**
 * Tags and the transaction↔tag association.
 *
 * Every write that touches a tag's identity — create, rename, delete, merge — keeps the
 * invariant that a tag's name is unique case-insensitively and that transactions are
 * never deleted as a side effect. Those are the two properties the rest of the app
 * relies on, so they are enforced here rather than at each call site.
 */
interface TagRepository {

    /** All live tags, name-ordered. */
    fun observeActiveTags(): Flow<List<Tag>>

    /** Every live tag with its usage count and expense total, keyed by tag id. */
    fun observeTagStats(): Flow<Map<String, TagStats>>

    /** The live tags applied to one transaction, as a flow. */
    fun observeTagsForTransaction(transactionId: String): Flow<List<Tag>>

    /**
     * Every live transaction's tags, grouped by transaction id, for the ledger.
     *
     * One flow over the whole join table rather than a per-row lookup: the list pages
     * through transactions, and a query per visible row would not scale to the
     * thousands of transactions the tag filter is specified against.
     */
    fun observeAllTransactionTags(): Flow<Map<String, List<Tag>>>

    /** The live tag ids currently applied to one transaction. */
    suspend fun getTagIdsForTransaction(transactionId: String): List<String>

    /**
     * Creates a tag, or returns the existing one that already matches [name]
     * case-insensitively.
     *
     * Returning the existing tag rather than failing is the behaviour the inline
     * creator needs: typing a name that already exists should attach the tag that is
     * already there, not report an error the user cannot act on.
     *
     * [colorHex] is `#RRGGBB` or null; it is normalised before storage. When the tag
     * already exists, a non-null colour updates it and null leaves the stored colour
     * alone — reusing a tag must not silently wipe the colour someone picked for it.
     */
    suspend fun createTag(name: String, colorHex: String? = null): Tag

    /** Renames a tag in place; every transaction using it sees the new name. */
    suspend fun renameTag(id: String, newName: String)

    /** Sets, changes or clears a tag's colour. */
    suspend fun updateTagColor(id: String, colorHex: String?)

    /** Soft-deletes a tag and detaches it from every transaction. */
    suspend fun deleteTag(id: String)

    /**
     * Merges [sourceId] into [targetId]: every transaction carrying the source ends up
     * carrying the target, duplicates collapse to one, and the source is soft-deleted.
     */
    suspend fun mergeTags(sourceId: String, targetId: String)

    /**
     * Replaces a transaction's tags with exactly [tagIds].
     *
     * The full set is passed rather than add/remove deltas because that is what the
     * editor holds: it renders the chips, so it knows the desired end state, and
     * reconciling to it in one transaction cannot drift the way a sequence of toggles
     * can when a save is retried.
     */
    suspend fun setTransactionTags(transactionId: String, tagIds: List<String>)
}