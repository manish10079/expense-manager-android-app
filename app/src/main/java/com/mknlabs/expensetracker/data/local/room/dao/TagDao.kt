package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.TagEntity
import com.mknlabs.expensetracker.data.local.room.entities.TransactionTagEntity
import com.mknlabs.expensetracker.data.local.room.query.TagStatsRow
import com.mknlabs.expensetracker.data.local.room.query.TransactionTagRow
import kotlinx.coroutines.flow.Flow

/**
 * The tag table and its join to transactions.
 *
 * One DAO rather than two because the join table has no meaning apart from the tag:
 * every write to `transaction_tags` is either "apply this tag" or "remove it", and
 * every read is in service of showing a tag. Splitting them would put the pair of
 * tables that must agree in two files and let one drift.
 *
 * Names are matched through `name_lower`, never through `name`. See [TagEntity] for
 * why the stored lower-case copy exists: it is what makes `#NYC` and `#nyc` the same
 * tag without depending on a collation that only handles ASCII.
 */
@Dao
interface TagDao {

    // ─── Tags ────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM tags WHERE is_deleted = 0 ORDER BY created_at DESC, name_lower ASC")
    fun observeActiveTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE is_deleted = 0 ORDER BY created_at DESC, name_lower ASC")
    suspend fun getActiveTags(): List<TagEntity>

    @Query("SELECT * FROM tags ORDER BY created_at DESC, name_lower ASC")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TagEntity?

    /**
     * The live tag whose name is [nameLower], if any. The caller lower-cases the name
     * it is looking for, because only the caller knows the locale the user typed in.
     *
     * `is_deleted = 0` on purpose: a deleted tag is not a name the user is trying to
     * reach, and `createTag` handles the deleted-row case separately so it can
     * reactivate rather than resurrect silently here.
     */
    @Query("SELECT * FROM tags WHERE name_lower = :nameLower AND is_deleted = 0 LIMIT 1")
    suspend fun findActiveByNameLower(nameLower: String): TagEntity?

    /** A soft-deleted tag with this name, so re-creating it can revive the old row. */
    @Query("SELECT * FROM tags WHERE name_lower = :nameLower AND is_deleted = 1 LIMIT 1")
    suspend fun findDeletedByNameLower(nameLower: String): TagEntity?

    @Upsert
    suspend fun upsert(tag: TagEntity)

    @Upsert
    suspend fun upsertAll(tags: List<TagEntity>)

    @Query("SELECT COUNT(*) FROM tags WHERE is_deleted = 0")
    suspend fun countActive(): Int

    /**
     * Renames one tag and marks it for upload. The name and its lower-case key always
     * move together, so no caller can update one and leave the unique index pointing
     * at the old name.
     */
    @Query("UPDATE tags SET name = :name, name_lower = :nameLower, updated_at = :updatedAt, sync_state = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun rename(id: String, name: String, nameLower: String, updatedAt: Long)

    @Query("UPDATE tags SET color_hex = :colorHex, updated_at = :updatedAt, sync_state = 'PENDING_UPLOAD' WHERE id = :id")
    suspend fun updateColorHex(id: String, colorHex: String?, updatedAt: Long)

    @Query("UPDATE tags SET is_deleted = 1, sync_state = 'PENDING_DELETE', updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("SELECT * FROM tags WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<TagEntity>

    @Query("UPDATE tags SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    /**
     * Every tag with its live-transaction count and expense total.
     *
     * `LEFT JOIN` so a tag nobody has used yet still appears, with zeros — the
     * management screen must be able to show a tag the user just created and has not
     * applied to anything.
     *
     * The join conditions live in the `ON` clause rather than a `WHERE`, which is what
     * keeps the left join left: `WHERE t.is_deleted = 0` would drop the unused tags
     * entirely. `GROUP BY tags.id` groups by the primary key, so SQLite's bare-column
     * extension makes the other selected tag columns well-defined.
     */
    @Query(
        """
        SELECT tags.id AS tag_id,
               COUNT(t.id) AS transaction_count,
               COALESCE(SUM(CASE WHEN t.transaction_type_id != 1 THEN t.amount_minor ELSE 0 END), 0) AS expense_minor
        FROM tags
        LEFT JOIN transaction_tags tt ON tt.tag_id = tags.id
        LEFT JOIN transactions t ON t.id = tt.transaction_id AND t.is_deleted = 0
        WHERE tags.is_deleted = 0
        GROUP BY tags.id
        """
    )
    fun observeTagStats(): Flow<List<TagStatsRow>>

    // ─── Transaction links ───────────────────────────────────────────────────

    /**
     * The tags of one transaction, as a flow — the editor observes this so a rename or
     * delete made on the management screen shows up without reopening the screen.
     */
    @Query(
        """
        SELECT tt.transaction_id AS transaction_id, tt.tag_id AS tag_id,
               tags.name AS name, tags.color_hex AS color_hex
        FROM transaction_tags tt
        JOIN tags ON tags.id = tt.tag_id
        WHERE tt.transaction_id = :transactionId AND tags.is_deleted = 0
        ORDER BY tags.name_lower ASC
        """
    )
    fun observeTagsForTransaction(transactionId: String): Flow<List<TransactionTagRow>>

    /**
     * Every live transaction's tags in one result set, for the ledger.
     *
     * A single query rather than one per visible row: the list can be paging through
     * thousands of transactions, and a per-row lookup would be a per-row query. The
     * caller groups the rows by `transaction_id` in memory.
     *
     * `t.is_deleted = 0` and `tags.is_deleted = 0` are both required: the join table
     * survives a soft delete by design (see [TransactionTagEntity]), so filtering
     * here is what stops a deleted transaction's or tag's links from rendering.
     */
    @Query(
        """
        SELECT tt.transaction_id AS transaction_id, tt.tag_id AS tag_id,
               tags.name AS name, tags.color_hex AS color_hex
        FROM transaction_tags tt
        JOIN tags ON tags.id = tt.tag_id
        JOIN transactions t ON t.id = tt.transaction_id
        WHERE t.is_deleted = 0 AND tags.is_deleted = 0
        ORDER BY tags.name_lower ASC
        """
    )
    fun observeAllTransactionTags(): Flow<List<TransactionTagRow>>

    /** The tag ids currently applied to one transaction, for the editor's initial state. */
    @Query(
        """
        SELECT tt.tag_id FROM transaction_tags tt
        JOIN tags ON tags.id = tt.tag_id
        WHERE tt.transaction_id = :transactionId AND tags.is_deleted = 0
        """
    )
    suspend fun getTagIdsForTransaction(transactionId: String): List<String>

    /**
     * Tombstones every live link of one transaction, for the sync set to push.
     *
     * A soft delete rather than a hard one: the other devices learn the tag was removed
     * from the tombstone, exactly as they do for every other entity. The rows are purged
     * once they have been uploaded.
     */
    @Query("UPDATE transaction_tags SET is_deleted = 1, sync_state = 'PENDING_DELETE', updated_at = :updatedAt WHERE transaction_id = :transactionId AND is_deleted = 0")
    suspend fun tombstoneLinksForTransaction(transactionId: String, updatedAt: Long)

    @Query("DELETE FROM transaction_tags WHERE is_deleted = 1 AND sync_state = 'SYNCED'")
    suspend fun purgeDeletedLinks()

    @Query("SELECT * FROM transaction_tags WHERE sync_state != 'SYNCED'")
    suspend fun getUnsyncedLinks(): List<TransactionTagEntity>

    /**
     * Every live link, for the JSON export.
     *
     * Tombstones (`is_deleted = 1`) are excluded on purpose: a backup describes the data
     * the user has, not the sync bookkeeping of removals. Joining to live tags and
     * transactions also drops links whose tag or transaction was soft-deleted, which keeps
     * the export self-consistent — every link it contains references rows it also contains.
     */
    @Query(
        """
        SELECT tt.* FROM transaction_tags tt
        JOIN tags ON tags.id = tt.tag_id AND tags.is_deleted = 0
        JOIN transactions t ON t.id = tt.transaction_id AND t.is_deleted = 0
        WHERE tt.is_deleted = 0
        """
    )
    suspend fun exportableLinks(): List<TransactionTagEntity>

    /**
     * Marks the pushed links as synced.
     *
     * `transaction_id = :transactionId AND tag_id = :tagId` per pair, driven by the two
     * parallel lists the caller builds from the same tasks. A link has no single-column
     * key, so there is no `IN (:ids)` form to reach for; the pairs are what identify it.
     *
     * `is_deleted = 0` keeps a tombstone's state intact — a tombstoned row stays
     * `PENDING_DELETE` for [purgeDeletedLinks] rather than being flipped to `SYNCED` and
     * lingering forever.
     */
    @Query(
        "UPDATE transaction_tags SET sync_state = 'SYNCED' " +
            "WHERE transaction_id = :transactionId AND tag_id = :tagId AND is_deleted = 0"
    )
    suspend fun markLinkSynced(transactionId: String, tagId: String)

    @Query(
        "UPDATE transaction_tags SET sync_state = 'SYNCED' " +
            "WHERE transaction_id = :transactionId AND tag_id = :tagId AND is_deleted = 1"
    )
    suspend fun markDeletedLinkSynced(transactionId: String, tagId: String)

    /** Batch form used by the sync push, pair-by-pair. */
    suspend fun markLinksSynced(transactionIds: List<String>, tagIds: List<String>) {
        transactionIds.zip(tagIds).forEach { (transactionId, tagId) ->
            markLinkSynced(transactionId, tagId)
            markDeletedLinkSynced(transactionId, tagId)
        }
    }

    @Upsert
    suspend fun upsertLinks(links: List<TransactionTagEntity>)

    /**
     * Re-points every link from one tag to another — the merge operation.
     *
     * `OR IGNORE` is load-bearing: a transaction tagged with both the source and the
     * target would otherwise hit the composite primary key and abort the whole
     * statement. Ignoring the collision keeps the transaction tagged once, which is
     * exactly the intended outcome, and leaves the duplicate row to be removed with
     * the source tag.
     */
    @Query(
        "UPDATE OR IGNORE transaction_tags SET tag_id = :targetId, updated_at = :updatedAt, " +
            "sync_state = 'PENDING_UPLOAD' WHERE tag_id = :sourceId AND is_deleted = 0"
    )
    suspend fun repointLinks(sourceId: String, targetId: String, updatedAt: Long)

    /**
     * Tombstones every live link of one tag — the detach half of deleting a tag.
     *
     * Soft, for the same reason as [tombstoneLinksForTransaction]: the removal has to
     * reach the other devices.
     */
    @Query("UPDATE transaction_tags SET is_deleted = 1, sync_state = 'PENDING_DELETE', updated_at = :updatedAt WHERE tag_id = :tagId AND is_deleted = 0")
    suspend fun tombstoneLinksForTag(tagId: String, updatedAt: Long)

    /**
     * Hard-deletes a tag's link rows. Only safe once the tombstones have been uploaded,
     * which is what the merge path relies on when it clears the source tag's orphans.
     */
    @Query("DELETE FROM transaction_tags WHERE tag_id = :tagId")
    suspend fun deleteLinksForTag(tagId: String)

    @Query("SELECT COUNT(*) FROM transaction_tags WHERE tag_id = :tagId")
    suspend fun countLinksForTag(tagId: String): Int
}