package com.mknlabs.expensetracker.data.repository

import androidx.room.withTransaction
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.data.local.room.dao.TagDao
import com.mknlabs.expensetracker.data.local.room.entities.TransactionTagEntity
import com.mknlabs.expensetracker.data.local.room.query.TagStatsRow
import com.mknlabs.expensetracker.data.local.room.query.TransactionTagRow
import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.domain.repository.TagRepository as DomainTagRepository
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.models.TagStats
import com.mknlabs.expensetracker.utils.normalizeColorHexOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/**
 * Tags, over [TagDao] and the shared database.
 *
 * The database is injected alongside the DAO because several operations must span both
 * tables atomically: a merge re-points links and then hides the source, and setting a
 * transaction's tags deletes and re-inserts its links. Each has to be all-or-nothing,
 * and `withTransaction` is what makes it so — the DAO alone cannot express that.
 */
class TagRepository @Inject constructor(
    private val database: ExpenseTrackerDatabase,
    private val dao: TagDao
) : DomainTagRepository {

    override fun observeActiveTags(): Flow<List<Tag>> =
        dao.observeActiveTags().map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override fun observeTagStats(): Flow<Map<String, TagStats>> =
        dao.observeTagStats().map { rows -> rows.associate { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override fun observeTagsForTransaction(transactionId: String): Flow<List<Tag>> =
        dao.observeTagsForTransaction(transactionId).map { rows ->
            rows.map { it.toTag() }
        }.flowOn(Dispatchers.IO)

    override fun observeAllTransactionTags(): Flow<Map<String, List<Tag>>> =
        dao.observeAllTransactionTags().map { rows ->
            rows.groupBy({ it.transactionId }, { it.toTag() })
        }.flowOn(Dispatchers.IO)

    override suspend fun getTagIdsForTransaction(transactionId: String): List<String> =
        withContext(Dispatchers.IO) { dao.getTagIdsForTransaction(transactionId) }

    override suspend fun createTag(name: String, colorHex: String?): Tag =
        withContext(Dispatchers.IO) {
            val trimmed = name.trim()
            require(trimmed.isNotEmpty()) { "A tag name cannot be blank" }
            val now = System.currentTimeMillis()
            val nameLower = trimmed.lowercase()

            // An existing live tag wins. Its colour is only overwritten when the caller
            // actually supplied one, so reusing a tag does not clear a colour the user
            // chose earlier.
            dao.findActiveByNameLower(nameLower)?.let { existing ->
                val color = normalizeColorHexOrNull(colorHex)
                if (color != null && color != existing.colorHex) {
                    dao.updateColorHex(existing.id, color, now)
                    return@withContext existing.copy(colorHex = color, updatedAt = now).toDomain()
                }
                return@withContext existing.toDomain()
            }

            // A deleted tag of the same name is revived rather than duplicated, which is
            // the same reasoning the category repository uses: the unique index on
            // name_lower would reject a second row anyway, and reviving keeps whatever
            // colour the row already had when the caller does not supply one.
            val color = normalizeColorHexOrNull(colorHex)
            dao.findDeletedByNameLower(nameLower)?.let { deleted ->
                val revived = deleted.copy(
                    name = trimmed,
                    nameLower = nameLower,
                    colorHex = color ?: deleted.colorHex,
                    isDeleted = false,
                    updatedAt = now,
                    syncState = SyncState.PENDING_UPLOAD
                )
                dao.upsert(revived)
                return@withContext revived.toDomain()
            }

            val tag = Tag(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                colorHex = color,
                isDeleted = false,
                syncState = SyncState.PENDING_UPLOAD,
                createdAt = now,
                updatedAt = now
            )
            dao.upsert(tag.toEntity())
            tag
        }

    override suspend fun renameTag(id: String, newName: String) = withContext(Dispatchers.IO) {
        val trimmed = newName.trim()
        require(trimmed.isNotEmpty()) { "A tag name cannot be blank" }
        val nameLower = trimmed.lowercase()

        val current = dao.getById(id) ?: return@withContext
        // Renaming to a name another live tag already holds would collide with the unique
        // index. Resolve it to the same tag instead — a no-op — rather than throwing at
        // the database layer.
        val clash = dao.findActiveByNameLower(nameLower)
        if (clash != null && clash.id != id) return@withContext
        if (current.nameLower == nameLower) {
            // Only the casing changed; still persist it, since display casing is the
            // user's and the lower-case key is unchanged.
            if (current.name != trimmed) dao.rename(id, trimmed, nameLower, System.currentTimeMillis())
            return@withContext
        }

        dao.rename(id, trimmed, nameLower, System.currentTimeMillis())
    }

    override suspend fun updateTagColor(id: String, colorHex: String?) = withContext(Dispatchers.IO) {
        dao.updateColorHex(
            id = id,
            colorHex = normalizeColorHexOrNull(colorHex),
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteTag(id: String) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            // Detach first, then hide the tag. The links are tombstoned rather than
            // hard-deleted so the removal syncs: the row stays because the tag is only
            // soft-deleted, and a tombstone is what makes "deleting a tag detaches it from
            // all transactions" true on the other devices too.
            dao.tombstoneLinksForTag(id, now)
            dao.softDelete(id, now)
        }
    }

    override suspend fun mergeTags(sourceId: String, targetId: String) = withContext(Dispatchers.IO) {
        if (sourceId == targetId) return@withContext
        val now = System.currentTimeMillis()
        database.withTransaction {
            // Fail fast if either side is gone; a merge against a missing tag has nothing
            // to re-point and would otherwise silently no-op halfway through.
            if (dao.getById(sourceId) == null || dao.getById(targetId) == null) {
                return@withTransaction
            }
            val target = dao.getById(targetId) ?: return@withTransaction

            // Re-point links to the target. `UPDATE OR IGNORE` collapses a transaction
            // that carried both tags down to a single link on the target, so the merge
            // never trips the composite primary key.
            dao.repointLinks(sourceId, targetId, now)
            // Any link that could not be re-pointed (it already existed on the target) is
            // now orphaned on the source; removing the source's links clears exactly those
            // and nothing else. The source tag is being merged away, so its own tombstone
            // is what tells other devices to drop the remaining links.
            dao.deleteLinksForTag(sourceId)
            dao.softDelete(sourceId, now)

            // The target's membership changed, so it must be re-uploaded even though its
            // own columns did not. The source's tombstone is its own push.
            dao.upsert(
                target.copy(
                    updatedAt = now,
                    syncState = SyncState.PENDING_UPLOAD
                )
            )
        }
    }

    override suspend fun setTransactionTags(transactionId: String, tagIds: List<String>) =
        withContext(Dispatchers.IO) {
            val distinct = tagIds.distinct()
            val now = System.currentTimeMillis()
            database.withTransaction {
                // Tombstone the existing links, then re-add the desired set. A tag that
                // was already attached is re-upserted with `is_deleted = 0`, which
                // resurrects its tombstone — so the net effect is exactly the new set,
                // and the whole reconciliation is one transaction.
                dao.tombstoneLinksForTransaction(transactionId, now)
                if (distinct.isNotEmpty()) {
                    dao.upsertLinks(
                        distinct.map { tagId ->
                            TransactionTagEntity(
                                transactionId = transactionId,
                                tagId = tagId,
                                createdAt = now,
                                updatedAt = now,
                                syncState = SyncState.PENDING_UPLOAD,
                                isDeleted = false
                            )
                        }
                    )
                }
            }
        }
}

private fun TagStatsRow.toDomain(): Pair<String, TagStats> =
    tagId to TagStats(
        tagId = tagId,
        transactionCount = transactionCount,
        expenseMinor = expenseMinor
    )

private fun TransactionTagRow.toTag(): Tag = Tag(
    id = tagId,
    name = name,
    colorHex = colorHex
)