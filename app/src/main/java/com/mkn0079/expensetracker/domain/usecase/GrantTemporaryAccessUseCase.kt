package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.MonetizationRepository
import com.mkn0079.expensetracker.monetization.Feature
import javax.inject.Inject

class GrantTemporaryAccessUseCase @Inject constructor(
    private val repository: MonetizationRepository
) {
    suspend fun execute(feature: Feature, optionId: String? = null, durationMillis: Long) {
        repository.grantTemporaryAccess(feature, optionId, durationMillis)
    }
}
