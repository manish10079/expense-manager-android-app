package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mknlabs.expensetracker.data.local.room.entities.DetectedSmsNotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room access for the detection inbox.
 *
 * Every write here touches `detected_sms_notifications` only — this DAO has no access
 * to `transactions`, which is what keeps the inbox from being able to mutate money
 * records even by accident. Filing a detection as a transaction happens in the
 * repository, through the existing `SmsRepository` save path.
 *
 * Sits beside the project's other DAOs (Room's generated code stays with the rest of
 * the database); the feature package owns the model, use cases and UI.
 */
@Dao
interface DetectedSmsNotificationDao {

    /**
     * Records a detection. Returns the new row id, or `-1` when the unique
     * `sms_hash` index rejected it as a duplicate — the caller never has to
     * read-then-write, so two near-simultaneous deliveries cannot both win.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: DetectedSmsNotificationEntity): Long

    @Query("SELECT * FROM detected_sms_notifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DetectedSmsNotificationEntity?

    @Query("SELECT * FROM detected_sms_notifications WHERE sms_hash = :smsHash LIMIT 1")
    suspend fun getByHash(smsHash: String): DetectedSmsNotificationEntity?

    @Query("SELECT * FROM detected_sms_notifications WHERE id IN (:ids) ORDER BY detected_at DESC")
    suspend fun getByIds(ids: List<String>): List<DetectedSmsNotificationEntity>

    /**
     * The inbox list. All constraints are optional, so one query serves every filter in
     * Phase 3 (All / Income / Expense / Added / Ignored / Unread) plus merchant-and-body
     * search. [searchPattern] is expected pre-wrapped in `%`.
     *
     * [excludeDecided] drops rows that already carry a decision (filed or ignored). The
     * filtering happens in SQL rather than in the ViewModel on purpose: a page is a
     * `LIMIT`, so removing rows after the read would silently shrink every page.
     */
    @Query(
        """
        SELECT * FROM detected_sms_notifications
        WHERE (:status IS NULL OR status = :status)
          AND (:transactionTypeId IS NULL OR transaction_type_id = :transactionTypeId)
          AND (
                :searchPattern IS NULL
                OR merchant_name LIKE :searchPattern
                OR message_body LIKE :searchPattern
                OR sender LIKE :searchPattern
              )
          AND (:excludeDecided = 0 OR status NOT IN ('ADDED', 'IGNORED'))
        ORDER BY detected_at DESC, created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeInbox(
        status: String?,
        transactionTypeId: Int?,
        searchPattern: String?,
        limit: Int,
        offset: Int,
        excludeDecided: Boolean
    ): Flow<List<DetectedSmsNotificationEntity>>

    /**
     * Same projection as [observeInbox], read once. Used by the paging path, where
     * the ViewModel appends pages instead of re-collecting the whole list.
     */
    @Query(
        """
        SELECT * FROM detected_sms_notifications
        WHERE (:status IS NULL OR status = :status)
          AND (:transactionTypeId IS NULL OR transaction_type_id = :transactionTypeId)
          AND (
                :searchPattern IS NULL
                OR merchant_name LIKE :searchPattern
                OR message_body LIKE :searchPattern
                OR sender LIKE :searchPattern
              )
          AND (:excludeDecided = 0 OR status NOT IN ('ADDED', 'IGNORED'))
        ORDER BY detected_at DESC, created_at DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getPage(
        status: String?,
        transactionTypeId: Int?,
        searchPattern: String?,
        limit: Int,
        offset: Int,
        excludeDecided: Boolean
    ): List<DetectedSmsNotificationEntity>

    /** Unread count for the bell badge. */
    @Query("SELECT COUNT(*) FROM detected_sms_notifications WHERE status = 'NEW'")
    fun observeUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM detected_sms_notifications WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM detected_sms_notifications WHERE status = 'NEW'")
    suspend fun countNew(): Int

    /** Applies a status to one or many rows in a single statement (bulk actions). */
    @Query("UPDATE detected_sms_notifications SET status = :status, updated_at = :updatedAt WHERE id IN (:ids)")
    suspend fun updateStatus(ids: List<String>, status: String, updatedAt: Long)

    /**
     * NEW -> VIEWED only. Opening the inbox must never overwrite a decision the user
     * already made (ADDED/IGNORED) or resurrect an EXPIRED row.
     */
    @Query("UPDATE detected_sms_notifications SET status = 'VIEWED', updated_at = :updatedAt WHERE id IN (:ids) AND status = 'NEW'")
    suspend fun markViewed(ids: List<String>, updatedAt: Long)

    /** Files a detection against the transaction created from it. */
    @Query(
        """
        UPDATE detected_sms_notifications
        SET status = 'ADDED', linked_transaction_id = :transactionId, updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun markAdded(id: String, transactionId: String, updatedAt: Long)

    @Query("UPDATE detected_sms_notifications SET notification_id = :notificationId, updated_at = :updatedAt WHERE id = :id")
    suspend fun attachNotification(id: String, notificationId: Int, updatedAt: Long)

    @Query("DELETE FROM detected_sms_notifications WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    /**
     * Retention step 1: untouched detections older than the window become EXPIRED so
     * they are visibly stale for the day or so before they are purged.
     */
    @Query("UPDATE detected_sms_notifications SET status = 'EXPIRED', updated_at = :updatedAt WHERE detected_at < :cutoff AND status IN ('NEW', 'VIEWED')")
    suspend fun markExpiredBefore(cutoff: Long, updatedAt: Long): Int

    /**
     * Retention step 2: remove rows older than [cutoff]. Only this table is touched —
     * the linked transactions are untouched, and the FK is `SET NULL`, so no money
     * record can be deleted by a cleanup pass.
     */
    @Query("DELETE FROM detected_sms_notifications WHERE detected_at < :cutoff")
    suspend fun purgeBefore(cutoff: Long): Int

    @Query("DELETE FROM detected_sms_notifications")
    suspend fun deleteAll()
}
