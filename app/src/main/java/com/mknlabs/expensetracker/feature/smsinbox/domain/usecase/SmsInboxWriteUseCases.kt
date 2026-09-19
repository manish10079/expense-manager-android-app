package com.mknlabs.expensetracker.feature.smsinbox.domain.usecase

import com.mknlabs.expensetracker.feature.smsinbox.domain.model.DetectedSmsNotification
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxStatus
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.NewSmsDetection
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.RecordSmsOutcome
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxCleanupResult
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxRepository
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsNotificationCleaner
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsTransactionWriter
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.sms.ParsedSms
import javax.inject.Inject

/**
 * Write side of the inbox.
 */

/**
 * Records a freshly detected SMS. Called from [com.mknlabs.expensetracker.sms.SmsReceiver],
 * which is the only place a detection can enter the app — so this is the single choke
 * point where the inbox either captures it or recognises it as a redelivery.
 */
class RecordDetectedSmsUseCase @Inject constructor(
    private val repository: SmsInboxRepository
) {
    suspend operator fun invoke(detection: NewSmsDetection): RecordSmsOutcome =
        repository.recordDetection(detection)
}

/** Marks detections as seen. Only NEW rows move, so a decision is never overwritten. */
class MarkSmsDetectionsViewedUseCase @Inject constructor(
    private val repository: SmsInboxRepository
) {
    suspend operator fun invoke(ids: List<String>) {
        repository.markViewed(ids)
    }
}

/**
 * Dismisses detections. Rows are IGNORED, not deleted, so the decision is visible.
 *
 * The notification goes with the decision — whether it was taken here, in the review
 * sheet, or from the notification's own Ignore button. A card left in the shade would
 * keep offering Add / Ignore on a detection the user has already dismissed.
 */
class IgnoreSmsDetectionsUseCase @Inject constructor(
    private val repository: SmsInboxRepository,
    private val notificationCleaner: SmsNotificationCleaner
) {
    suspend operator fun invoke(ids: List<String>) {
        // Read the notification IDs first: they are only meaningful while the rows are
        // still actionable, and ignoring does not clear them off the row.
        val notificationIds = repository.getByIds(ids).mapNotNull { it.notificationId }
        repository.markIgnored(ids)
        notificationIds.forEach(notificationCleaner::clear)
    }
}

/**
 * Deletes detections outright. This is the only inbox operation that destroys the
 * stored message text, and it still cannot touch a transaction.
 */
class DeleteSmsDetectionsUseCase @Inject constructor(
    private val repository: SmsInboxRepository,
    private val notificationCleaner: SmsNotificationCleaner
) {
    suspend operator fun invoke(ids: List<String>) {
        // Same reason as ignoring, and stricter: the stored notification ID is deleted
        // along with the row, so it has to be read off beforehand.
        val notificationIds = repository.getByIds(ids).mapNotNull { it.notificationId }
        repository.delete(ids)
        notificationIds.forEach(notificationCleaner::clear)
    }
}

/** Runs the 30-day retention pass (Phase 7). */
class CleanupSmsInboxUseCase @Inject constructor(
    private val repository: SmsInboxRepository
) {
    suspend operator fun invoke(now: Long = System.currentTimeMillis()): SmsInboxCleanupResult =
        repository.runCleanup(now)
}

/** What happened when the user filed a detection. */
sealed interface FileDetectionResult {

    /** A transaction was created (or an existing one was reused) and linked. */
    data class Filed(
        val detection: DetectedSmsNotification,
        val transaction: Transaction
    ) : FileDetectionResult

    /** A transaction for this amount and timestamp already exists — nothing was written. */
    data class AlreadyAdded(
        val detection: DetectedSmsNotification
    ) : FileDetectionResult

    /** No inbox row with that id (already purged by retention). */
    data class NotFound(val detectionId: String) : FileDetectionResult
}

/**
 * Files one detection as a real transaction.
 *
 * Two things are deliberate here:
 *
 *  - **It files through [SmsTransactionWriter]**, the same path the notification's
 *    one-tap save uses. Filing from the inbox and filing from the shade therefore
 *    cannot drift apart in how a transaction is built — and the inbox never touches
 *    the transaction layer itself.
 *  - **The duplicate guard runs at write time**, immediately before the insert,
 *    against amount + timestamp. That is the only moment where a double-count can
 *    actually be introduced, so that is where it is checked — and [allowOverride]
 *    lets the user deliberately file a genuine second charge that looks identical.
 */
class FileSmsDetectionAsTransactionUseCase @Inject constructor(
    private val inboxRepository: SmsInboxRepository,
    private val transactionWriter: SmsTransactionWriter,
    private val notificationCleaner: SmsNotificationCleaner
) {

    suspend operator fun invoke(
        detectionId: String,
        categoryId: Int? = null,
        note: String = "",
        allowOverride: Boolean = false,
        /** Set only when the user corrected the parsed amount before filing. */
        amountMinorOverride: Long? = null
    ): FileDetectionResult {
        val detection = inboxRepository.getById(detectionId)
            ?: return FileDetectionResult.NotFound(detectionId)

        if (!allowOverride &&
            transactionWriter.transactionExistsFor(detection.amountMinor, detection.detectedAt)
        ) {
            return FileDetectionResult.AlreadyAdded(detection)
        }

        val transaction = transactionWriter.saveDetectionAsTransaction(
            parsed = detection.toParsedSms(categoryId, amountMinorOverride),
            note = note,
            categoryId = categoryId ?: detection.suggestedCategoryId ?: DEFAULT_CATEGORY_ID
        )

        inboxRepository.markAdded(detection.id, transaction.id)
        // A filed detection has nothing left to decide, so its card leaves the shade
        // too — whether it was filed from the inbox or from the notification's Add.
        detection.notificationId?.let(notificationCleaner::clear)
        return FileDetectionResult.Filed(
            detection = detection.copy(
                status = SmsInboxStatus.ADDED,
                linkedTransactionId = transaction.id
            ),
            transaction = transaction
        )
    }

    /**
     * Rebuilds the parser's output from the stored row. The inbox keeps the parsed
     * facts precisely so this reconstruction is possible without re-reading or
     * re-parsing the message.
     */
    private fun DetectedSmsNotification.toParsedSms(
        categoryId: Int?,
        amountMinorOverride: Long?
    ): ParsedSms = ParsedSms(
        amountMinor = amountMinorOverride ?: amountMinor,
        sender = sender,
        body = messageBody,
        smsTimestamp = detectedAt,
        transactionTypeId = transactionTypeId,
        categoryId = categoryId ?: suggestedCategoryId ?: DEFAULT_CATEGORY_ID,
        merchant = merchantName,
        confidence = confidence
    )

    private companion object {
        /** "Other" — matches the parser's own fallback when nothing better is known. */
        const val DEFAULT_CATEGORY_ID = 23
    }
}

/**
 * Records that a detection was filed as a transaction the user saved themselves.
 *
 * This is the inbox's half of the "tap a card, correct it in the Add Transaction screen"
 * flow. That screen owns the write — the user may have changed the amount, the category
 * or the note — so the detection must adopt the transaction that was actually saved.
 * Filing it again through [FileSmsDetectionAsTransactionUseCase] would build a second
 * transaction from the parsed facts, which is exactly what must not happen once the user
 * has corrected them by hand.
 *
 * The notification is cleared here for the same reason the other write paths clear it: a
 * card still offering Add / Ignore for a detection that is already in the ledger would be
 * asking the user to decide something twice.
 */
class AttachSmsDetectionToTransactionUseCase @Inject constructor(
    private val repository: SmsInboxRepository,
    private val notificationCleaner: SmsNotificationCleaner
) {
    suspend operator fun invoke(detectionId: String, transactionId: String) {
        // A detection purged between the tap and the save has nothing left to link. The
        // transaction the user saved is untouched either way — the inbox never deletes
        // money records, and here it does not even need to.
        val detection = repository.getById(detectionId) ?: return
        repository.markAdded(detectionId, transactionId)
        detection.notificationId?.let(notificationCleaner::clear)
    }
}
