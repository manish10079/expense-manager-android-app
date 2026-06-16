package com.mknlabs.expensetracker.domain.repository

interface ProPassRepository {
    /**
     * Redeems a ProPass code and grants temporary access if valid.
     * @return Result containing the duration in days on success.
     */
    suspend fun redeemCode(code: String): Result<Int>
}
