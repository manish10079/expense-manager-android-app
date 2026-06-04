package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.monetization.Feature
import javax.inject.Inject

class GrantTemporaryAccessUseCase @Inject constructor(
    private val repository: MonetizationRepository
) {
    suspend fun execute(feature: Feature, optionId: String? = null, durationMillis: Long) {
        repository.grantTemporaryAccess(feature, optionId, durationMillis)
    }
}
