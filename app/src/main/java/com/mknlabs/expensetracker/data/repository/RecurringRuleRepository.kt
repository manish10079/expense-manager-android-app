package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.data.local.room.dao.InstallmentOccurrenceDao
import com.mknlabs.expensetracker.data.local.room.dao.RecurringRuleDao
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.entities.InstallmentOccurrenceEntity
import com.mknlabs.expensetracker.domain.repository.RecurringRuleRepository as DomainRecurringRuleRepository
import com.mknlabs.expensetracker.models.InstallmentOccurrence
import com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus
import com.mknlabs.expensetracker.models.InstallmentPlan
import com.mknlabs.expensetracker.models.InstallmentStatus
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.RecurringType
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.utils.RecurringScheduleCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.UUID
import javax.inject.Inject

class RecurringRuleRepository @Inject constructor(
    private val dao: RecurringRuleDao,
    private val occurrenceDao: InstallmentOccurrenceDao,
    private val transactionDao: TransactionDao
) : DomainRecurringRuleRepository {

    override fun observeActiveRecurringRules(): Flow<List<RecurringTransactionRule>> {
        return dao.observeActiveRecurringRules().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getActiveRules(): List<RecurringTransactionRule> = withContext(Dispatchers.IO) {
        dao.getActiveRules().map { it.toDomain() }
    }

    override suspend fun getActiveByTransactionId(transactionId: String): RecurringTransactionRule? = withContext(Dispatchers.IO) {
        dao.getActiveByTransactionId(transactionId)?.toDomain()
    }

    override suspend fun upsertRule(rule: RecurringTransactionRule): RecurringTransactionRule = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val resolved = rule.copy(
            id = rule.id.ifBlank { UUID.randomUUID().toString() },
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )
        dao.upsert(resolved.toEntity())
        resolved
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        dao.updateEnabled(
            id = id,
            enabled = enabled,
            syncState = SyncState.PENDING_UPLOAD.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun setNotificationsEnabled(id: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        dao.updateNotificationsEnabled(
            id = id,
            enabled = enabled,
            syncState = SyncState.PENDING_UPLOAD.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteRule(id: String) = withContext(Dispatchers.IO) {
        dao.softDelete(
            id = id,
            syncState = SyncState.PENDING_DELETE.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    // ── Installment (EMI) plan ──────────────────────────────────────────────

    override fun observeOccurrences(ruleId: String): Flow<List<InstallmentOccurrence>> {
        return occurrenceDao.observeByRule(ruleId).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getOccurrences(ruleId: String): List<InstallmentOccurrence> = withContext(Dispatchers.IO) {
        occurrenceDao.getByRule(ruleId).map { it.toDomain() }
    }

    /**
     * Progress is counted from the PAID occurrences rather than read from a
     * stored counter, so the plan terms and its progress can never disagree —
     * and a loan completes itself the moment its last installment is paid, with
     * no separate bookkeeping step that could be missed.
     */
    override suspend fun getInstallmentPlan(ruleId: String): InstallmentPlan? = withContext(Dispatchers.IO) {
        val rule = dao.getById(ruleId)?.toDomain() ?: return@withContext null
        // REGULAR rules never expose a plan, even though converted-to-REGULAR
        // rules retain their (inert) plan terms so a later conversion loses nothing.
        if (!rule.isInstallment) return@withContext null

        val total = rule.installmentTotalMinor ?: return@withContext null
        val perInstallment = rule.installmentAmountMinor ?: return@withContext null
        val totalCount = rule.installmentTotalCount ?: return@withContext null

        val paid = occurrenceDao.countPaid(ruleId)
        val skipped = occurrenceDao.countSkipped(ruleId)
        val paidAmount = occurrenceDao.sumPaidMinor(ruleId)

        InstallmentPlan(
            totalAmountMinor = total,
            installmentAmountMinor = perInstallment,
            totalInstallments = totalCount,
            paidInstallments = paid,
            skippedInstallments = skipped,
            // Clamped: an overpayment (or a plan whose terms were edited down)
            // must never report a negative balance.
            remainingAmountMinor = (total - paidAmount).coerceAtLeast(0L),
            status = when {
                rule.installmentStatus == InstallmentStatus.CANCELLED -> InstallmentStatus.CANCELLED
                totalCount > 0 && paid >= totalCount -> InstallmentStatus.COMPLETED
                else -> InstallmentStatus.ACTIVE
            },
            nextDueAt = occurrenceDao.getPendingByRule(ruleId).firstOrNull()?.dueAt
        )
    }

    override suspend fun convertToInstallment(
        ruleId: String,
        totalAmountMinor: Long,
        installmentAmountMinor: Long,
        totalInstallments: Int,
        firstDueAt: Long
    ): RecurringTransactionRule? = withContext(Dispatchers.IO) {
        val existing = dao.getById(ruleId) ?: return@withContext null
        val rule = existing.toDomain()
        val now = System.currentTimeMillis()

        // Any slot this rule had before — including ones soft-deleted by an
        // earlier convertToRegular — so a REGULAR -> INSTALLMENT -> REGULAR ->
        // INSTALLMENT round trip restores paid state instead of resetting it.
        val priorSlots = occurrenceDao.getAllByRuleIncludingDeleted(ruleId)
            .associateBy { it.id }

        val dueDates = RecurringScheduleCalculator.occurrencesFrom(
            firstDueAt = firstDueAt,
            frequency = rule.frequency,
            count = totalInstallments,
            baseAnchor = firstDueAt
        )

        // Clear the whole schedule first, then write back exactly the current
        // plan. This drops slots left over from a longer previous plan while the
        // assoc-by-id above preserves the ones that still apply.
        occurrenceDao.softDeleteByRule(
            ruleId = ruleId,
            syncState = SyncState.PENDING_UPLOAD.name,
            updatedAt = now
        )

        val rows = dueDates.mapIndexed { index, dueAt ->
            val id = InstallmentOccurrence.idFor(ruleId, index + 1)
            val prior = priorSlots[id]
            InstallmentOccurrenceEntity(
                id = id,
                ruleId = ruleId,
                installmentIndex = index + 1,
                dueAt = dueAt,
                amountMinor = installmentAmountMinor,
                paidAt = prior?.paidAt,
                status = prior?.status ?: InstallmentOccurrenceStatus.PENDING,
                transactionId = prior?.transactionId,
                createdAt = prior?.createdAt ?: now,
                updatedAt = now,
                syncState = SyncState.PENDING_UPLOAD,
                isDeleted = false
            )
        }
        occurrenceDao.upsertAll(rows)

        val paidCount = rows.count { it.status == InstallmentOccurrenceStatus.PAID }
        val updated = rule.copy(
            recurringType = RecurringType.INSTALLMENT,
            installmentTotalMinor = totalAmountMinor,
            installmentAmountMinor = installmentAmountMinor,
            installmentTotalCount = totalInstallments,
            installmentStatus = if (totalInstallments > 0 && paidCount >= totalInstallments) {
                InstallmentStatus.COMPLETED
            } else {
                InstallmentStatus.ACTIVE
            },
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )
        dao.upsert(updated.toEntity())
        updated
    }

    override suspend fun convertToRegular(ruleId: String): RecurringTransactionRule? = withContext(Dispatchers.IO) {
        val existing = dao.getById(ruleId) ?: return@withContext null
        val rule = existing.toDomain()
        val now = System.currentTimeMillis()

        // Soft-delete the schedule rather than deleting it: the plan terms stay on
        // the rule and the occurrences stay in the table, so converting back is a
        // revive, not a rebuild — no paid state, amount or linked transaction is
        // lost. The template transaction and every generated transaction are
        // untouched either way.
        occurrenceDao.softDeleteByRule(
            ruleId = ruleId,
            syncState = SyncState.PENDING_UPLOAD.name,
            updatedAt = now
        )

        val updated = rule.copy(
            recurringType = RecurringType.REGULAR,
            installmentStatus = null,
            updatedAt = now,
            syncState = SyncState.PENDING_UPLOAD
        )
        dao.upsert(updated.toEntity())
        updated
    }

    // ── Installment (EMI) mutations ─────────────────────────────────────────

    override suspend fun payInstallment(
        occurrenceId: String,
        paidAt: Long
    ): InstallmentOccurrence? = withContext(Dispatchers.IO) {
        settleOccurrence(occurrenceId, paidAt)
    }

    override suspend fun payInstallments(occurrenceIds: List<String>, paidAt: Long): Int =
        withContext(Dispatchers.IO) {
            var paid = 0
            occurrenceIds.forEach { id ->
                if (settleOccurrence(id, paidAt) != null) paid++
            }
            paid
        }

    override suspend fun skipInstallment(occurrenceId: String): InstallmentOccurrence? =
        withContext(Dispatchers.IO) {
            val occurrence = occurrenceDao.getById(occurrenceId)
                ?.takeIf { !it.isDeleted && it.status == InstallmentOccurrenceStatus.PENDING }
                ?: return@withContext null
            val now = System.currentTimeMillis()
            occurrenceDao.updateStatus(
                id = occurrenceId,
                status = InstallmentOccurrenceStatus.SKIPPED.name,
                paidAt = null,
                transactionId = null,
                syncState = SyncState.PENDING_UPLOAD.name,
                updatedAt = now
            )
            occurrenceDao.getById(occurrenceId)?.toDomain()
        }
    /**
     * Returns the slot to PENDING, and when it had been paid, soft-deletes the
     * transaction that slot generated (matched by its slot-derived id, so the
     * template and every other slot's transaction are structurally out of
     * reach). Also un-persists an auto-COMPLETED plan — the same
     * paid >= total comparison [getInstallmentPlan] derives with, so the stored
     * status and the derived one can never disagree about an undone plan.
     */
    override suspend fun undoInstallment(occurrenceId: String): InstallmentOccurrence? =
        withContext(Dispatchers.IO) {
            val occurrence = occurrenceDao.getById(occurrenceId)
                ?.takeIf { !it.isDeleted && it.status != InstallmentOccurrenceStatus.PENDING }
                ?: return@withContext null
            val now = System.currentTimeMillis()
            occurrenceDao.updateStatus(
                id = occurrenceId,
                status = InstallmentOccurrenceStatus.PENDING.name,
                paidAt = null,
                transactionId = null,
                syncState = SyncState.PENDING_UPLOAD.name,
                updatedAt = now
            )
            if (occurrence.status == InstallmentOccurrenceStatus.PAID) {
                occurrence.transactionId?.let { transactionId ->
                    transactionDao.softDelete(
                        id = transactionId,
                        syncState = SyncState.PENDING_UPLOAD.name,
                        updatedAt = now
                    )
                }
                val rule = dao.getById(occurrence.ruleId)?.toDomain()
                if (rule != null && rule.installmentStatus == InstallmentStatus.COMPLETED) {
                    dao.upsert(
                        rule.copy(
                            installmentStatus = InstallmentStatus.ACTIVE,
                            updatedAt = now,
                            syncState = SyncState.PENDING_UPLOAD
                        ).toEntity()
                    )
                }
            }
            occurrenceDao.getById(occurrenceId)?.toDomain()
        }

    override suspend fun reconcileDueInstallments(ruleId: String, now: Long): List<String> =
        withContext(Dispatchers.IO) {
            val dueSlots = occurrenceDao.getPendingByRule(ruleId).filter { it.dueAt <= now }
            if (dueSlots.isEmpty()) return@withContext emptyList()

            // Each slot is dated at its own due date, so the ledger reads as if
            // the payment happened when it was actually due, whatever time the
            // worker ran.
            val settledCount = dueSlots.count { settleOccurrence(it.id, it.dueAt) != null }
            if (settledCount == 0) return@withContext emptyList()

            // All slots of one plan share the template transaction's note —
            // load it once for the notification.
            val rule = dao.getById(ruleId)?.toDomain()
            val note = rule?.let { transactionDao.getById(it.transactionId)?.note } ?: ""
            List(settledCount) { note }
        }

    /**
     * Shared pay path: validate -> create the linked transaction -> mark PAID
     * -> persist auto-completion. Null when the slot is missing, soft-deleted,
     * or already settled, which makes the whole operation idempotent under
     * retry (crash after the transaction insert, worker re-run, double tap).
     */
    private suspend fun settleOccurrence(
        occurrenceId: String,
        paidAt: Long
    ): InstallmentOccurrence? {
        val occurrence = occurrenceDao.getById(occurrenceId)
            ?.takeIf { !it.isDeleted && it.status == InstallmentOccurrenceStatus.PENDING }
            ?: return null
        val rule = dao.getById(occurrence.ruleId)?.toDomain() ?: return null
        val template = transactionDao.getById(rule.transactionId) ?: return null
        val now = System.currentTimeMillis()

        // The slot id doubles as the transaction id — deterministic, so a retry
        // upserts the same row instead of duplicating a payment, and it can
        // never collide with the worker's legacy "{ruleId}_{timestamp}" ids.
        transactionDao.upsert(
            template.copy(
                id = occurrence.id,
                amountMinor = occurrence.amountMinor,
                occurredAt = paidAt,
                createdAt = paidAt,
                updatedAt = now,
                isDeleted = false,
                syncState = SyncState.PENDING_UPLOAD,
                sourceRecurringRuleId = rule.id
            )
        )
        occurrenceDao.updateStatus(
            id = occurrenceId,
            status = InstallmentOccurrenceStatus.PAID.name,
            paidAt = paidAt,
            transactionId = occurrence.id,
            syncState = SyncState.PENDING_UPLOAD.name,
            updatedAt = now
        )

        // Persist completion so the plan's stored status matches the derived one.
        val totalCount = rule.installmentTotalCount
        if (rule.installmentStatus == InstallmentStatus.ACTIVE &&
            totalCount != null && totalCount > 0 &&
            occurrenceDao.countPaid(rule.id) >= totalCount
        ) {
            dao.upsert(
                rule.copy(
                    installmentStatus = InstallmentStatus.COMPLETED,
                    updatedAt = now,
                    syncState = SyncState.PENDING_UPLOAD
                ).toEntity()
            )
        }
        return occurrenceDao.getById(occurrenceId)?.toDomain()
    }
}
