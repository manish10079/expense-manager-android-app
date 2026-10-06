package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.FundEntity
import com.mknlabs.expensetracker.data.local.room.query.FundSpendRow
import kotlinx.coroutines.flow.Flow

@Dao
interface FundDao {

    @Query("SELECT * FROM funds WHERE is_deleted = 0 ORDER BY is_archived ASC, created_at DESC")
    fun observeActiveFunds(): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds WHERE is_deleted = 0 ORDER BY is_archived ASC, created_at DESC")
    suspend fun getActiveFunds(): List<FundEntity>

    @Query("SELECT * FROM funds WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FundEntity?

    @Upsert
    suspend fun upsert(fund: FundEntity)

    @Query("UPDATE funds SET is_deleted = 1, sync_state = :syncState, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, syncState: String, updatedAt: Long)

    @Query("SELECT * FROM funds WHERE sync_state != 'SYNCED'")
    suspend fun getUnsynced(): List<FundEntity>

    @Query("UPDATE funds SET sync_state = :syncState WHERE id IN (:ids)")
    suspend fun updateSyncStates(ids: List<String>, syncState: String)

    @Query("DELETE FROM funds WHERE is_deleted = 1 AND sync_state = 'SYNCED' AND updated_at < :threshold")
    suspend fun purgeOldDeleted(threshold: Long)

    /**
     * Spent total per fund, summed in SQL over the live expenses that link to it.
     *
     * Expensed only (`transaction_type_id = 2`), so the income row a fund is created with
     * never counts against its own balance. Funds with no spending simply do not appear in
     * the result, which is what the caller treats as "nothing spent yet".
     */
    @Query(
        """
        SELECT fund_id AS fund_id, COALESCE(SUM(amount_minor), 0) AS spent_minor
        FROM transactions
        WHERE is_deleted = 0
          AND fund_id IS NOT NULL
          AND transaction_type_id = 2
        GROUP BY fund_id
        """
    )
    fun observeSpentByFund(): Flow<List<FundSpendRow>>

    /** The same sum for a single fund, for the detail screen. */
    @Query(
        """
        SELECT COALESCE(SUM(amount_minor), 0)
        FROM transactions
        WHERE is_deleted = 0
          AND fund_id = :fundId
          AND transaction_type_id = 2
        """
    )
    suspend fun getSpentMinor(fundId: String): Long

    /**
     * Drops the link from every transaction that pointed at [fundId].
     *
     * Part of deleting a fund (see `FundRepository.deleteFund`): the transactions survive
     * as ordinary spending, they just stop counting toward a bucket that no longer exists.
     * Each touched row is marked for upload so the unlink propagates.
     */
    @Query(
        """
        UPDATE transactions
        SET fund_id = NULL, updated_at = :updatedAt, sync_state = 'PENDING_UPLOAD'
        WHERE fund_id = :fundId
        """
    )
    suspend fun clearTransactionLinks(fundId: String, updatedAt: Long)

    /** Live transactions linked to a fund, newest first. */
    @Query("SELECT id FROM transactions WHERE fund_id = :fundId AND is_deleted = 0")
    suspend fun getLinkedTransactionIds(fundId: String): List<String>
}
