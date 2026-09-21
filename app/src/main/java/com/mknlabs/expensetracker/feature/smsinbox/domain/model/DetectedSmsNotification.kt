package com.mknlabs.expensetracker.feature.smsinbox.domain.model

import com.mknlabs.expensetracker.sms.SmsConfidence

/** Transaction type ids shared with the SMS parser (`1` = income, `2` = expense). */
const val INCOME_TRANSACTION_TYPE_ID = 1
const val EXPENSE_TRANSACTION_TYPE_ID = 2

/**
 * Lifecycle of one detected-but-not-yet-filed transaction.
 *
 * The inbox is the *only* place a detection lives until the user acts on it, so a
 * status change is the user's decision being recorded — never an inference from
 * what happens to be on screen.
 */
enum class SmsInboxStatus {
    /** Detected, never opened. Drives the unread badge. */
    NEW,

    /** The user has seen it in the inbox (or in the review sheet) but not decided. */
    VIEWED,

    /** Filed as a transaction; [DetectedSmsNotification.linkedTransactionId] is set. */
    ADDED,

    /** Deliberately dismissed. Kept so "Ignore All" stays reversible and search stays honest. */
    IGNORED,

    /** Aged past the retention window without a decision; purged on the next cleanup pass. */
    EXPIRED;

    /** Whether the user can still act on it (Add / Ignore). */
    val isActionable: Boolean
        get() = this == NEW || this == VIEWED || this == EXPIRED
}

/**
 * Where the detection came from. SMS is the normal path; the other two exist so a
 * detection recovered after process death, or reconstructed from a notification,
 * is still attributable rather than silently indistinguishable from a live SMS.
 */
enum class SmsDetectionSource {
    SMS,
    NOTIFICATION,
    MANUAL_RECOVERY
}

/** Inbox list filters (Phase 3). Each maps to at most one query constraint. */
enum class SmsInboxFilter {
    ALL,
    INCOME,
    EXPENSE;

    /** Status constraint for this filter, or null when the filter is not status-based. */
    val status: SmsInboxStatus?
        get() = null

    /** Transaction type constraint for this filter, or null when it is not type-based. */
    val transactionTypeId: Int?
        get() = when (this) {
            INCOME -> INCOME_TRANSACTION_TYPE_ID
            EXPENSE -> EXPENSE_TRANSACTION_TYPE_ID
            else -> null
        }

    /**
     * True for the filters that show the inbox's own job: detections still waiting for a
     * decision.
     */
    val showsOnlyUndecided: Boolean
        get() = true
}

/**
 * One bank-SMS detection held in the inbox.
 *
 * Deliberately self-contained: it stores the parsed facts (amount, type, merchant,
 * suggestion) alongside the raw message, so the row can be rendered, re-parsed, or
 * filed as a transaction long after the notification that announced it is gone.
 *
 * This row is NOT a transaction. [linkedTransactionId] points at the transaction the
 * user created from it, and is cleared (never cascaded) if that transaction is ever
 * hard-deleted — the inbox must not be able to take a real expense with it.
 */
data class DetectedSmsNotification(
    val id: String,
    /** Content fingerprint — the duplicate-prevention key (see `SmsFingerprint`). */
    val smsHash: String,
    val sender: String,
    val messageBody: String,
    val amountMinor: Long,
    val transactionTypeId: Int,
    val merchantName: String?,
    /** When the bank sent the SMS. */
    val detectedAt: Long,
    /** When the notification for it was posted. */
    val notificationCreatedAt: Long,
    val status: SmsInboxStatus,
    val linkedTransactionId: String? = null,
    val source: SmsDetectionSource = SmsDetectionSource.SMS,
    /** Parser confidence as a 0f..1f score; banded back into [SmsConfidence] for display. */
    val confidenceScore: Float,
    val suggestedCategoryId: Int? = null,
    /** The shade notification carrying this detection, so its actions can resolve the row. */
    val notificationId: Int? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val isIncome: Boolean
        get() = transactionTypeId == INCOME_TRANSACTION_TYPE_ID

    val confidence: SmsConfidence
        get() = SmsConfidenceScore.band(confidenceScore)

    /** True once the user has filed this detection as a transaction. */
    val isFiled: Boolean
        get() = status == SmsInboxStatus.ADDED || !linkedTransactionId.isNullOrBlank()
}

/**
 * The parser reports confidence as a band, the inbox stores it as a score. Keeping
 * both is deliberate: the band is what the parser reasoned with, the score is what
 * the UI renders as an indicator and what a future threshold can tune.
 */
object SmsConfidenceScore {

    const val HIGH = 0.9f
    const val MEDIUM = 0.6f
    const val LOW = 0.3f

    fun of(confidence: SmsConfidence): Float = when (confidence) {
        SmsConfidence.HIGH -> HIGH
        SmsConfidence.MEDIUM -> MEDIUM
        SmsConfidence.LOW -> LOW
    }

    fun band(score: Float): SmsConfidence = when {
        score >= HIGH -> SmsConfidence.HIGH
        score >= MEDIUM -> SmsConfidence.MEDIUM
        else -> SmsConfidence.LOW
    }
}
