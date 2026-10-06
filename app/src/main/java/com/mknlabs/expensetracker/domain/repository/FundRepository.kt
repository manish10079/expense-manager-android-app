package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.FundWithProgress
import com.mknlabs.expensetracker.models.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Cash buckets. Every balance the UI shows is derived here from the transactions linked to
 * a fund — nothing about a fund's spending is stored on the fund itself.
 */
interface FundRepository {

    /** Active (non-deleted) funds, archived ones last. */
    fun observeActiveFunds(): Flow<List<Fund>>

    /** Active funds each paired with the total spent against them, summed in SQL. */
    fun observeFundProgress(): Flow<List<FundWithProgress>>

    /** Live transactions drawn from one fund, newest first. */
    fun observeTransactionsByFund(fundId: String): Flow<List<Transaction>>

    suspend fun getFundById(id: String): Fund?

    /**
     * Creates a fund together with its linked income transaction (the money arriving),
     * both in one transaction so a fund can never exist without the income that funds it.
     */
    suspend fun createFund(
        fund: Fund,
        incomeCategoryId: Int = FundDefaults.INCOME_CATEGORY_ID,
        incomePaymentTypeId: Int = FundDefaults.INCOME_PAYMENT_TYPE_ID
    ): Fund

    suspend fun updateFund(fund: Fund): Fund

    /**
     * Changes a fund's amount and, in the same transaction, restates the linked income row
     * so the money that arrived still matches the bucket it filled.
     */
    suspend fun updateFundAmount(id: String, amountMinor: Long): Fund?

    suspend fun setArchived(id: String, archived: Boolean)

    /**
     * Removes a fund but keeps everything spent from it: the transactions survive and are
     * unlinked, so deleting a bucket can never destroy real spending history.
     */
    suspend fun deleteFund(id: String)
}

/** Seeded ids the linked income row is created against. */
object FundDefaults {
    /** The income category a fund's money is booked under ("Other"). */
    const val INCOME_CATEGORY_ID = 105

    /** Payment method for the arrival; generic, and editable like any other transaction. */
    const val INCOME_PAYMENT_TYPE_ID = 1
}
