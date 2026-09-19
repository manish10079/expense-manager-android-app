package com.mknlabs.expensetracker.feature.smsinbox.domain.repository

import com.mknlabs.expensetracker.feature.smsinbox.domain.model.DetectedSmsNotification
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsDetectionSource
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.sms.SmsConfidence
import kotlinx.coroutines.flow.Flow

/**
 * A detection as it arrives from the parser, before the inbox has given it an
 * identity. Carrying the parsed facts (not the raw SMS alone) is what lets the
 * inbox be rendered and filed without re-parsing, which in turn means a detection
 * cannot be lost to a parser change between receipt and review.
 */
data class NewSmsDetection(
    val sender: String,
    val body: String,
    val amountMinor: Long,
    val transactionTypeId: Int,
    val categoryId: Int? = null,
    val merchant: String? = null,
    val confidence: SmsConfidence,
    /** When the bank sent it (the SMS timestamp, falling back to receipt time). */
    val detectedAt: Long,
    val notificationCreatedAt: Long = detectedAt,
    val source: SmsDetectionSource = SmsDetectionSource.SMS
)

/** Whether a detection was filed into the inbox, or recognised as a redelivery. */
sealed interface RecordSmsOutcome {

    data class Recorded(val detection: DetectedSmsNotification) : RecordSmsOutcome

    /**
     * The same message was already detected. [existing] is returned so the caller can
     * update that row (e.g. attach a fresh notification to it) instead of dropping
     * the event on the floor.
     */
    data class Duplicate(val existing: DetectedSmsNotification) : RecordSmsOutcome
}

/** Outcome of one retention pass. */
data class SmsInboxCleanupResult(
    val expired: Int,
    val purged: Int
)

/**
 * The inbox's port. Deliberately narrow: it can read and mutate **inbox rows only**.
 * Filing a detection as a transaction is a separate use case, so the transaction
 * database is never reachable through this repository — the decoupling the spec asks
 * for is enforced by the type, not by convention.
 */
interface SmsInboxRepository {

    /**
     * Records a detection, deduplicating on the content fingerprint. Safe to call
     * from a BroadcastReceiver: it is idempotent, and a redelivery returns
     * [RecordSmsOutcome.Duplicate] rather than writing a second row.
     */
    suspend fun recordDetection(detection: NewSmsDetection): RecordSmsOutcome

    /** Reactive inbox list for one filter, optionally narrowed by a search term. */
    fun observeInbox(
        filter: SmsInboxFilter,
        searchQuery: String? = null,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<List<DetectedSmsNotification>>

    /** One page, read once — the append path for infinite scrolling. */
    suspend fun getPage(
        filter: SmsInboxFilter,
        searchQuery: String? = null,
        limit: Int = DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): List<DetectedSmsNotification>

    /** Unread (NEW) count driving the bell badge. */
    fun observeUnreadCount(): Flow<Int>

    suspend fun getById(id: String): DetectedSmsNotification?

    suspend fun getByIds(ids: List<String>): List<DetectedSmsNotification>

    /** Records that the user has seen these rows. Only NEW rows move. */
    suspend fun markViewed(ids: List<String>)

    /** Links a detection to the transaction filed from it and marks it ADDED. */
    suspend fun markAdded(id: String, transactionId: String)

    suspend fun markIgnored(ids: List<String>)

    suspend fun delete(ids: List<String>)

    /** Remembers which shade notification carries this detection. */
    suspend fun attachNotification(id: String, notificationId: Int)

    /**
     * Applies the 30-day retention policy. Returns how many rows were expired and
     * how many were purged; linked transactions are never touched.
     */
    suspend fun runCleanup(now: Long = System.currentTimeMillis()): SmsInboxCleanupResult

    companion object {
        const val DEFAULT_PAGE_SIZE = 30
    }
}
