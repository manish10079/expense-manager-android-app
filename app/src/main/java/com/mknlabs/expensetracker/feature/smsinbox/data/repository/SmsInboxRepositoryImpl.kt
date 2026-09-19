package com.mknlabs.expensetracker.feature.smsinbox.data.repository

import com.mknlabs.expensetracker.data.local.room.dao.DetectedSmsNotificationDao
import com.mknlabs.expensetracker.data.local.room.entities.DetectedSmsNotificationEntity
import com.mknlabs.expensetracker.data.local.room.entities.toDomain
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.DetectedSmsNotification
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsConfidenceScore
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsFingerprint
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxStatus
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.NewSmsDetection
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.RecordSmsOutcome
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxCleanupResult
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Default inbox repository.
 *
 * Deduplication is enforced by the database, not by a read-then-write check: the
 * unique `sms_hash` index plus `INSERT ... IGNORE` means a carrier redelivery arriving
 * while the first insert is still in flight cannot produce a second row. The explicit
 * pre-read only exists to return the *existing* row to the caller.
 */
@Singleton
class SmsInboxRepositoryImpl @Inject constructor(
    private val dao: DetectedSmsNotificationDao
) : SmsInboxRepository {

    override suspend fun recordDetection(detection: NewSmsDetection): RecordSmsOutcome =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val smsHash = SmsFingerprint.of(
                sender = detection.sender,
                body = detection.body,
                amountMinor = detection.amountMinor,
                smsTimestamp = detection.detectedAt
            )

            dao.getByHash(smsHash)?.let { existing ->
                return@withContext RecordSmsOutcome.Duplicate(existing.toDomain())
            }

            val entity = DetectedSmsNotificationEntity(
                id = UUID.randomUUID().toString(),
                smsHash = smsHash,
                sender = detection.sender,
                messageBody = detection.body,
                amountMinor = detection.amountMinor,
                transactionTypeId = detection.transactionTypeId,
                merchantName = detection.merchant,
                detectedAt = detection.detectedAt,
                notificationCreatedAt = detection.notificationCreatedAt,
                status = SmsInboxStatus.NEW,
                source = detection.source,
                confidenceScore = SmsConfidenceScore.of(detection.confidence),
                suggestedCategoryId = detection.categoryId,
                createdAt = now,
                updatedAt = now
            )

            // -1 means the unique index rejected it: a concurrent delivery of the same
            // message won the race, so hand back the row that actually landed.
            if (dao.insert(entity) == REJECTED) {
                val winner = dao.getByHash(smsHash)
                return@withContext RecordSmsOutcome.Duplicate(
                    winner?.toDomain() ?: entity.toDomain()
                )
            }

            RecordSmsOutcome.Recorded(entity.toDomain())
        }

    override fun observeInbox(
        filter: SmsInboxFilter,
        searchQuery: String?,
        limit: Int,
        offset: Int
    ): Flow<List<DetectedSmsNotification>> =
        dao.observeInbox(
            status = filter.status?.name,
            transactionTypeId = filter.transactionTypeId,
            searchPattern = likePattern(searchQuery),
            limit = limit,
            offset = offset,
            excludeDecided = filter.showsOnlyUndecided
        ).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getPage(
        filter: SmsInboxFilter,
        searchQuery: String?,
        limit: Int,
        offset: Int
    ): List<DetectedSmsNotification> = withContext(Dispatchers.IO) {
        dao.getPage(
            status = filter.status?.name,
            transactionTypeId = filter.transactionTypeId,
            searchPattern = likePattern(searchQuery),
            limit = limit,
            offset = offset,
            excludeDecided = filter.showsOnlyUndecided
        ).map { it.toDomain() }
    }

    override fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    override suspend fun getById(id: String): DetectedSmsNotification? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomain()
    }

    override suspend fun getByIds(ids: List<String>): List<DetectedSmsNotification> =
        withContext(Dispatchers.IO) {
            if (ids.isEmpty()) return@withContext emptyList()
            dao.getByIds(ids).map { it.toDomain() }
        }

    override suspend fun markViewed(ids: List<String>) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        // NEW -> VIEWED only: opening the inbox must not overwrite a decision.
        dao.markViewed(ids, System.currentTimeMillis())
    }

    override suspend fun markAdded(id: String, transactionId: String) = withContext(Dispatchers.IO) {
        dao.markAdded(id, transactionId, System.currentTimeMillis())
    }

    override suspend fun markIgnored(ids: List<String>) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        dao.updateStatus(ids, SmsInboxStatus.IGNORED.name, System.currentTimeMillis())
    }

    override suspend fun delete(ids: List<String>) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        dao.deleteByIds(ids)
    }

    override suspend fun attachNotification(id: String, notificationId: Int) =
        withContext(Dispatchers.IO) {
            dao.attachNotification(id, notificationId, System.currentTimeMillis())
        }

    /**
     * Retention (Phase 7). Two steps so nothing ever vanishes unseen: only rows past
     * the grace day are removed, and rows crossing the 30-day line are marked EXPIRED
     * — always from this table only, never from `transactions`.
     *
     * The purge runs FIRST on purpose. Doing it the other way round marked rows EXPIRED
     * and then deleted them in the same pass, so the reported `expired` count included
     * rows the user would never see, and an aged-out detection could be described as
     * expired moments before it disappeared.
     */
    override suspend fun runCleanup(now: Long): SmsInboxCleanupResult =
        withContext(Dispatchers.IO) {
            val retentionCutoff = now - RETENTION_MILLIS
            val purged = dao.purgeBefore(retentionCutoff - PURGE_GRACE_MILLIS)
            val expired = dao.markExpiredBefore(retentionCutoff, now)
            SmsInboxCleanupResult(expired = expired, purged = purged)
        }

    /**
     * Wraps a user search term as a LIKE pattern. `%` and `_` are stripped from the
     * term so a user typing "50%" searches for the literal text instead of matching
     * every row.
     */
    private fun likePattern(searchQuery: String?): String? =
        searchQuery
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { "%" + it.replace("%", "").replace("_", "") + "%" }

    private companion object {
        /** `INSERT ... IGNORE` reported a unique-index conflict. */
        const val REJECTED = -1L

        const val RETENTION_MILLIS = 30L * 24L * 60L * 60L * 1000L
        const val PURGE_GRACE_MILLIS = 24L * 60L * 60L * 1000L
    }
}
