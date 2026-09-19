package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.DetectedSmsNotification
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsDetectionSource
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxStatus

/**
 * One detected bank-SMS transaction, held until the user files or dismisses it.
 *
 * This table exists so a detection can outlive the notification that announced it.
 * Previously the parsed payload travelled only inside PendingIntent extras, so a
 * dismissed notification destroyed the only copy — the gap this feature closes.
 *
 * Two deliberate departures from the other tables in this database:
 *
 *  - **No `sync_state` / `is_deleted` columns.** Nothing here is uploaded: the inbox
 *    is device-private (it holds raw bank SMS text) and is never part of a Firestore
 *    document. Without a sync path there is no pending-upload state to track, and
 *    deletion is a real delete so the 30-day retention promise actually removes the
 *    message text rather than keeping it behind a flag.
 *  - **The link to a transaction never cascades.** [linkedTransactionId] is
 *    `ON DELETE SET NULL`, so purging an inbox row can never take a real transaction
 *    with it, and hard-deleting a transaction merely unlinks its detection.
 *
 * `sms_hash` carries a UNIQUE index: the duplicate rules are enforced by the
 * database, not by a read-then-write check that two fast deliveries could race past.
 *
 * It lives beside the other entities in `data.local.room.entities` rather than inside
 * the feature package, matching every other persistence primitive in this project —
 * the feature package owns the model, the use cases and the UI, while Room's
 * generated code stays with the rest of the database.
 */
@Entity(
    tableName = "detected_sms_notifications",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["linked_transaction_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["sms_hash"], unique = true),
        Index(value = ["status", "detected_at"]),
        Index(value = ["detected_at"]),
        Index(value = ["linked_transaction_id"])
    ]
)
data class DetectedSmsNotificationEntity(
    @PrimaryKey
    val id: String = "",
    @ColumnInfo(name = "sms_hash")
    val smsHash: String = "",
    @ColumnInfo(name = "sender")
    val sender: String = "",
    @ColumnInfo(name = "message_body")
    val messageBody: String = "",
    @ColumnInfo(name = "amount_minor")
    val amountMinor: Long = 0L,
    @ColumnInfo(name = "transaction_type_id")
    val transactionTypeId: Int = 0,
    @ColumnInfo(name = "merchant_name")
    val merchantName: String? = null,
    @ColumnInfo(name = "detected_at")
    val detectedAt: Long = 0L,
    @ColumnInfo(name = "notification_created_at")
    val notificationCreatedAt: Long = 0L,
    @ColumnInfo(name = "status")
    val status: SmsInboxStatus = SmsInboxStatus.NEW,
    @ColumnInfo(name = "linked_transaction_id")
    val linkedTransactionId: String? = null,
    @ColumnInfo(name = "source")
    val source: SmsDetectionSource = SmsDetectionSource.SMS,
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float = 0f,
    @ColumnInfo(name = "suggested_category_id")
    val suggestedCategoryId: Int? = null,
    @ColumnInfo(name = "notification_id")
    val notificationId: Int? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L
)

fun DetectedSmsNotificationEntity.toDomain(): DetectedSmsNotification = DetectedSmsNotification(
    id = id,
    smsHash = smsHash,
    sender = sender,
    messageBody = messageBody,
    amountMinor = amountMinor,
    transactionTypeId = transactionTypeId,
    merchantName = merchantName,
    detectedAt = detectedAt,
    notificationCreatedAt = notificationCreatedAt,
    status = status,
    linkedTransactionId = linkedTransactionId,
    source = source,
    confidenceScore = confidenceScore,
    suggestedCategoryId = suggestedCategoryId,
    notificationId = notificationId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun DetectedSmsNotification.toEntity(): DetectedSmsNotificationEntity = DetectedSmsNotificationEntity(
    id = id,
    smsHash = smsHash,
    sender = sender,
    messageBody = messageBody,
    amountMinor = amountMinor,
    transactionTypeId = transactionTypeId,
    merchantName = merchantName,
    detectedAt = detectedAt,
    notificationCreatedAt = notificationCreatedAt,
    status = status,
    linkedTransactionId = linkedTransactionId,
    source = source,
    confidenceScore = confidenceScore,
    suggestedCategoryId = suggestedCategoryId,
    notificationId = notificationId,
    createdAt = createdAt,
    updatedAt = updatedAt
)
