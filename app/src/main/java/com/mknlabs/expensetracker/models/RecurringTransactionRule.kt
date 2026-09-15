package com.mknlabs.expensetracker.models

import androidx.compose.runtime.Immutable

enum class RecurringFrequency(val label: String, val periodUnit: String) {
    Daily("Daily", "day"),
    Weekly("Weekly", "week"),
    Monthly("Monthly", "month"),
    Yearly("Yearly", "year")
}

/**
 * What a recurring rule represents.
 *
 * [REGULAR] is every rule that existed before installments — a plain repeating
 * transaction. [INSTALLMENT] additionally carries a loan/EMI plan (total amount,
 * per-installment amount, installment count) and materialises
 * [InstallmentOccurrence] rows so each installment can be paid, skipped, or paid
 * late.
 *
 * The two are convertible in BOTH directions at any time, without touching the
 * template transaction or any already-generated transaction — see
 * `RecurringRuleRepository.convertToInstallment` / `convertToRegular`.
 */
enum class RecurringType {
    REGULAR,
    INSTALLMENT
}

/** Lifecycle of an installment plan, independent of any single occurrence. */
enum class InstallmentStatus {
    /** Installments remain to be paid. */
    ACTIVE,

    /** Every installment has been paid — the loan is finished. */
    COMPLETED,

    /** User abandoned the plan; remaining installments are no longer due. */
    CANCELLED
}

/**
 * State of a single scheduled installment.
 *
 * [OVERDUE] is a *derived* presentation of a [PENDING] occurrence whose due date
 * has passed — it is never written to the database, so a device with a wrong
 * clock can never corrupt the stored state.
 */
enum class InstallmentOccurrenceStatus {
    PENDING,
    PAID,
    SKIPPED,
    OVERDUE
}

@Immutable
data class RecurringTransactionRule(
    val id: String,
    val transactionId: String,
    val frequency: RecurringFrequency,
    val repeatCount: Int,
    val isEnabled: Boolean,
    val intervalCount: Int = 1,
    val remainingCount: Int? = repeatCount,
    val anchorAt: Long = System.currentTimeMillis(),
    val nextRunAt: Long = anchorAt,
    val lastRunAt: Long? = null,
    val lastNotifiedOccurrenceAt: Long? = null,
    /** Per-rule notification mute (spec): reminder only fires when this is true AND the category toggle is on. */
    val notificationsEnabled: Boolean = true,
    /** Advance-alert window (7 / 3 / 1 / 0 days-before) already fired for the current occurrence. */
    val lastNotifiedWindowDays: Int? = null,
    // ── Installment (EMI) plan terms ────────────────────────────────────────
    // All optional and defaulted so every pre-existing rule — in code and in the
    // database — remains a valid REGULAR rule with no behavior change.
    /** REGULAR for every legacy rule; INSTALLMENT once a plan is attached. */
    val recurringType: RecurringType = RecurringType.REGULAR,
    /** Principal + interest the user is paying off. Null for REGULAR. */
    val installmentTotalMinor: Long? = null,
    /** Amount due per installment. Null for REGULAR. */
    val installmentAmountMinor: Long? = null,
    /** How many installments the plan is split into. Null for REGULAR. */
    val installmentTotalCount: Int? = null,
    /** Plan lifecycle. Null for REGULAR. */
    val installmentStatus: InstallmentStatus? = null,
    val createdAt: Long = anchorAt,
    val updatedAt: Long = createdAt,
    val syncState: SyncState = SyncState.PENDING_UPLOAD,
    val isDeleted: Boolean = false
) {
    /** True when this rule carries an EMI/loan plan. */
    val isInstallment: Boolean get() = recurringType == RecurringType.INSTALLMENT
}

@Immutable
data class RecurringTransactionDraft(
    val frequency: RecurringFrequency,
    val repeatCount: Int
)

/**
 * Progress view over an installment plan. Derived, never stored: [paidInstallments]
 * and [remainingAmountMinor] are counted from the rule's PAID occurrences, so the
 * plan terms and its progress cannot drift apart.
 *
 * @param totalAmountMinor Principal + interest.
 * @param installmentAmountMinor Amount due per installment.
 * @param totalInstallments Number of installments in the plan.
 * @param paidInstallments Installments already paid (counted, not stored).
 * @param skippedInstallments Installments the user deliberately skipped.
 * @param remainingAmountMinor [totalAmountMinor] minus everything paid so far.
 * @param status Plan lifecycle, including auto-COMPLETED once fully paid.
 * @param nextDueAt Due date of the next unpaid installment, or null when done.
 */
@Immutable
data class InstallmentPlan(
    val totalAmountMinor: Long,
    val installmentAmountMinor: Long,
    val totalInstallments: Int,
    val paidInstallments: Int,
    val skippedInstallments: Int,
    val remainingAmountMinor: Long,
    val status: InstallmentStatus,
    val nextDueAt: Long?
) {
    val progressFraction: Float
        get() = if (totalInstallments <= 0) 0f
        else (paidInstallments.toFloat() / totalInstallments.toFloat()).coerceIn(0f, 1f)

    val isCompleted: Boolean get() = status == InstallmentStatus.COMPLETED
}

/**
 * One scheduled installment of an [InstallmentPlan] — the *intent* to pay, as
 * opposed to a [Transaction], which is the *fact* of a payment.
 *
 * [id] is deterministic (`"{ruleId}_occ_{installmentIndex}"`) so the worker can
 * re-derive a slot without ever duplicating it, matching how recurring
 * transactions are already made idempotent.
 */
@Immutable
data class InstallmentOccurrence(
    val id: String,
    val ruleId: String,
    /** 1-based position within the plan. */
    val installmentIndex: Int,
    val dueAt: Long,
    val amountMinor: Long,
    /** Set when paid; null while pending or skipped. */
    val paidAt: Long? = null,
    val status: InstallmentOccurrenceStatus = InstallmentOccurrenceStatus.PENDING,
    /** The transaction that settled this installment, when one exists. */
    val transactionId: String? = null,
    val createdAt: Long = dueAt,
    val updatedAt: Long = createdAt,
    val syncState: SyncState = SyncState.PENDING_UPLOAD,
    val isDeleted: Boolean = false
) {
    /**
     * Status as of [now] — a PENDING occurrence past its due date reads as
     * OVERDUE without that ever being persisted.
     */
    fun statusAt(now: Long): InstallmentOccurrenceStatus =
        if (status == InstallmentOccurrenceStatus.PENDING && dueAt < now) {
            InstallmentOccurrenceStatus.OVERDUE
        } else {
            status
        }

    companion object {
        /**
         * Deterministic slot id. Deriving it instead of generating a UUID is what
         * makes schedule materialization idempotent: re-running the generator for
         * the same rule/slot resolves to the same row rather than a duplicate,
         * mirroring the deterministic ids already used for auto-generated
         * recurring transactions.
         */
        fun idFor(ruleId: String, installmentIndex: Int): String =
            "${ruleId}_occ_$installmentIndex"
    }
}
