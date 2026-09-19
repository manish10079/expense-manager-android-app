package com.mknlabs.expensetracker.feature.smsinbox.data.repository

import com.mknlabs.expensetracker.data.local.room.dao.DetectedSmsNotificationDao
import com.mknlabs.expensetracker.data.local.room.entities.DetectedSmsNotificationEntity
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.EXPENSE_TRANSACTION_TYPE_ID
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsConfidenceScore
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxStatus
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.NewSmsDetection
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.RecordSmsOutcome
import com.mknlabs.expensetracker.sms.SmsConfidence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Locks down the two promises the inbox makes: a detection is never dropped, and a
 * redelivery never becomes a second one. Uses an in-memory fake so the dedup rules
 * are tested without an Android device.
 */
class SmsInboxRepositoryImplTest {

    private val dao = FakeDetectedSmsNotificationDao()
    private val repository = SmsInboxRepositoryImpl(dao)

    @Test
    fun `a new detection is recorded as unread`() = runTest {
        val outcome = repository.recordDetection(detection())

        val recorded = outcome as RecordSmsOutcome.Recorded
        assertEquals(SmsInboxStatus.NEW, recorded.detection.status)
        assertEquals(AMOUNT_MINOR, recorded.detection.amountMinor)
        assertEquals(45_000L, recorded.detection.amountMinor)
        assertEquals(dao.rows.value.size, 1)
    }

    @Test
    fun `the same message is captured once no matter how often it arrives`() = runTest {
        repository.recordDetection(detection())
        val second = repository.recordDetection(detection())
        val third = repository.recordDetection(detection())

        assertTrue(second is RecordSmsOutcome.Duplicate)
        assertTrue(third is RecordSmsOutcome.Duplicate)
        assertEquals("a redelivery must never add a second row", 1, dao.rows.value.size)
    }

    @Test
    fun `the duplicate outcome hands back the row that was already stored`() = runTest {
        val first = repository.recordDetection(detection()) as RecordSmsOutcome.Recorded
        val duplicate = repository.recordDetection(detection()) as RecordSmsOutcome.Duplicate

        // Caller can still attach a new notification id to the existing detection.
        assertEquals(first.detection.id, duplicate.existing.id)
        assertEquals(first.detection.smsHash, duplicate.existing.smsHash)
    }

    @Test
    fun `a unique-index rejection is reported as a duplicate rather than lost`() = runTest {
        // Simulates the race: the pre-read misses, the insert conflicts. The caller
        // must still learn that the message exists instead of writing a second row.
        dao.failNextInsertAsConflict = true

        val outcome = repository.recordDetection(detection())

        assertTrue(outcome is RecordSmsOutcome.Duplicate)
    }

    @Test
    fun `confidence is stored as a score and banded back for display`() = runTest {
        val recorded = repository.recordDetection(
            detection(confidence = SmsConfidence.MEDIUM)
        ) as RecordSmsOutcome.Recorded

        assertEquals(SmsConfidenceScore.MEDIUM, recorded.detection.confidenceScore, 0.0001f)
        assertEquals(SmsConfidence.MEDIUM, recorded.detection.confidence)
    }

    @Test
    fun `filing a detection links the transaction without touching its status elsewhere`() = runTest {
        val recorded = repository.recordDetection(detection()) as RecordSmsOutcome.Recorded

        repository.markAdded(recorded.detection.id, "tx-123")

        val stored = dao.rows.value.single()
        assertEquals(SmsInboxStatus.ADDED, stored.status)
        assertEquals("tx-123", stored.linkedTransactionId)
    }

    @Test
    fun `viewing only advances rows the user has not decided on`() = runTest {
        val recorded = repository.recordDetection(detection()) as RecordSmsOutcome.Recorded
        dao.updateStatus(listOf(recorded.detection.id), SmsInboxStatus.IGNORED.name, 0L)

        repository.markViewed(listOf(recorded.detection.id))

        assertEquals(
            "an ignored detection must not be reopened as viewed",
            SmsInboxStatus.IGNORED,
            dao.rows.value.single().status
        )
    }

    @Test
    fun `cleanup expires old detections and purges only ones past the grace day`() = runTest {
        val now = 40L * DAY_MILLIS
        seed(dao, at = now - 31L * DAY_MILLIS - 1) // past retention + grace -> purged
        seed(dao, at = now - 30L * DAY_MILLIS - 1) // past retention, inside grace -> expired
        seed(dao, at = now - 1L * DAY_MILLIS)      // fresh -> untouched

        val result = repository.runCleanup(now)

        assertEquals(1, result.expired)
        assertEquals(1, result.purged)
        assertEquals(2, dao.rows.value.size)
        assertEquals(
            1,
            dao.rows.value.count { it.status == SmsInboxStatus.EXPIRED }
        )
        assertEquals(
            1,
            dao.rows.value.count { it.status == SmsInboxStatus.NEW }
        )
    }

    @Test
    fun `search terms cannot inject SQL wildcards`() = runTest {
        repository.getPage(SmsInboxFilter.ALL, searchQuery = "50%_off")

        assertEquals("%50off%", dao.lastSearchPattern)
    }

    @Test
    fun `each filter maps to exactly one query constraint`() = runTest {
        repository.getPage(SmsInboxFilter.UNREAD)
        assertEquals(SmsInboxStatus.NEW.name, dao.lastStatus)
        assertNull(dao.lastTransactionTypeId)

        repository.getPage(SmsInboxFilter.EXPENSE)
        assertNull(dao.lastStatus)
        assertEquals(EXPENSE_TRANSACTION_TYPE_ID, dao.lastTransactionTypeId)

        repository.getPage(SmsInboxFilter.ALL)
        assertNull(dao.lastStatus)
        assertNull(dao.lastTransactionTypeId)
    }

    @Test
    fun `the undecided filters leave out what the user already settled`() = runTest {
        repository.getPage(SmsInboxFilter.ALL)
        assertTrue("All is what still needs a decision", dao.lastExcludeDecided == true)

        repository.getPage(SmsInboxFilter.INCOME)
        assertTrue(dao.lastExcludeDecided == true)

        // The status-based filters are already narrow, so nothing more is excluded —
        // otherwise the Added filter could never show an added detection.
        repository.getPage(SmsInboxFilter.ADDED)
        assertFalse(dao.lastExcludeDecided == true)

        repository.getPage(SmsInboxFilter.IGNORED)
        assertFalse(dao.lastExcludeDecided == true)
    }

    @Test
    fun `blank ids are ignored instead of clearing the inbox`() = runTest {
        seed(dao, at = 1_000L)

        repository.markViewed(emptyList())
        repository.markIgnored(emptyList())
        repository.delete(emptyList())

        assertEquals(1, dao.rows.value.size)
    }

    private fun detection(
        confidence: SmsConfidence = SmsConfidence.HIGH
    ) = NewSmsDetection(
        sender = SENDER,
        body = BODY,
        amountMinor = AMOUNT_MINOR,
        transactionTypeId = EXPENSE_TRANSACTION_TYPE_ID,
        categoryId = 1,
        merchant = "Swiggy",
        confidence = confidence,
        detectedAt = DETECTED_AT
    )

    private fun seed(dao: FakeDetectedSmsNotificationDao, at: Long) {
        dao.rows.value = dao.rows.value + DetectedSmsNotificationEntity(
            id = "seed-$at",
            smsHash = "hash-$at",
            sender = SENDER,
            messageBody = BODY,
            amountMinor = AMOUNT_MINOR,
            transactionTypeId = EXPENSE_TRANSACTION_TYPE_ID,
            detectedAt = at,
            notificationCreatedAt = at,
            status = SmsInboxStatus.NEW,
            confidenceScore = SmsConfidenceScore.HIGH,
            createdAt = at,
            updatedAt = at
        )
    }

    private companion object {
        const val SENDER = "VM-HDFCBK"
        const val BODY = "Rs.450 debited from A/c XX1234 to VPA swiggy@ybl"
        const val AMOUNT_MINOR = 45_000L
        const val DETECTED_AT = 1_700_000_000_000L
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}

/**
 * Minimal in-memory stand-in for the Room DAO, faithful only where the repository's
 * behaviour depends on it: the unique hash constraint, the NEW-only view transition
 * and the queries' filter parameters.
 */
/** The two statuses a decided detection can be in — the ones "All" must leave out. */
private const val DECIDED_ADDED = "ADDED"
private const val DECIDED_IGNORED = "IGNORED"

private class FakeDetectedSmsNotificationDao : DetectedSmsNotificationDao {

    var rows = kotlinx.coroutines.flow.MutableStateFlow<List<DetectedSmsNotificationEntity>>(emptyList())

    var failNextInsertAsConflict = false
    var lastStatus: String? = null
    var lastTransactionTypeId: Int? = null
    var lastSearchPattern: String? = null
    var lastExcludeDecided: Boolean? = null

    override suspend fun insert(entity: DetectedSmsNotificationEntity): Long {
        if (failNextInsertAsConflict) {
            failNextInsertAsConflict = false
            return -1L
        }
        if (rows.value.any { it.smsHash == entity.smsHash }) return -1L
        rows.value = rows.value + entity
        return rows.value.size.toLong()
    }

    override suspend fun getById(id: String) = rows.value.firstOrNull { it.id == id }

    override suspend fun getByHash(smsHash: String) = rows.value.firstOrNull { it.smsHash == smsHash }

    override suspend fun getByIds(ids: List<String>) = rows.value.filter { it.id in ids }

    override fun observeInbox(
        status: String?,
        transactionTypeId: Int?,
        searchPattern: String?,
        limit: Int,
        offset: Int,
        excludeDecided: Boolean
    ): Flow<List<DetectedSmsNotificationEntity>> =
        flowOf(page(status, transactionTypeId, searchPattern, limit, offset, excludeDecided))

    override suspend fun getPage(
        status: String?,
        transactionTypeId: Int?,
        searchPattern: String?,
        limit: Int,
        offset: Int,
        excludeDecided: Boolean
    ) = page(status, transactionTypeId, searchPattern, limit, offset, excludeDecided)

    private fun page(
        status: String?,
        transactionTypeId: Int?,
        searchPattern: String?,
        limit: Int,
        offset: Int,
        excludeDecided: Boolean
    ): List<DetectedSmsNotificationEntity> {
        lastStatus = status
        lastTransactionTypeId = transactionTypeId
        lastSearchPattern = searchPattern
        lastExcludeDecided = excludeDecided
        return rows.value
            .filter { status == null || it.status.name == status }
            .filter { transactionTypeId == null || it.transactionTypeId == transactionTypeId }
            .filter { !excludeDecided || (it.status.name != DECIDED_ADDED && it.status.name != DECIDED_IGNORED) }
    }

    override fun observeUnreadCount(): Flow<Int> =
        flowOf(rows.value.count { it.status == SmsInboxStatus.NEW })

    override suspend fun countByStatus(status: String) = rows.value.count { it.status.name == status }

    override suspend fun countNew() = rows.value.count { it.status == SmsInboxStatus.NEW }

    override suspend fun updateStatus(ids: List<String>, status: String, updatedAt: Long) {
        val target = SmsInboxStatus.entries.first { it.name == status }
        rows.value = rows.value.map { if (it.id in ids) it.copy(status = target, updatedAt = updatedAt) else it }
    }

    override suspend fun markViewed(ids: List<String>, updatedAt: Long) {
        rows.value = rows.value.map {
            if (it.id in ids && it.status == SmsInboxStatus.NEW) it.copy(status = SmsInboxStatus.VIEWED, updatedAt = updatedAt) else it
        }
    }

    override suspend fun markAdded(id: String, transactionId: String, updatedAt: Long) {
        rows.value = rows.value.map {
            if (it.id == id) it.copy(status = SmsInboxStatus.ADDED, linkedTransactionId = transactionId, updatedAt = updatedAt) else it
        }
    }

    override suspend fun attachNotification(id: String, notificationId: Int, updatedAt: Long) {
        rows.value = rows.value.map {
            if (it.id == id) it.copy(notificationId = notificationId, updatedAt = updatedAt) else it
        }
    }

    override suspend fun deleteByIds(ids: List<String>) {
        rows.value = rows.value.filterNot { it.id in ids }
    }

    override suspend fun markExpiredBefore(cutoff: Long, updatedAt: Long): Int {
        val affected = rows.value.count {
            it.detectedAt < cutoff && (it.status == SmsInboxStatus.NEW || it.status == SmsInboxStatus.VIEWED)
        }
        rows.value = rows.value.map {
            if (it.detectedAt < cutoff && (it.status == SmsInboxStatus.NEW || it.status == SmsInboxStatus.VIEWED)) {
                it.copy(status = SmsInboxStatus.EXPIRED, updatedAt = updatedAt)
            } else it
        }
        return affected
    }

    override suspend fun purgeBefore(cutoff: Long): Int {
        val affected = rows.value.count { it.detectedAt < cutoff }
        rows.value = rows.value.filterNot { it.detectedAt < cutoff }
        return affected
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }
}
