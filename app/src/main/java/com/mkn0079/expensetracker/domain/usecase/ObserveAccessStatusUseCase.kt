package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.MonetizationRepository
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAccessStatusUseCase @Inject constructor(
    private val repository: MonetizationRepository
) {
    operator fun invoke(feature: Feature, optionId: String? = null): Flow<AccessStatus> {
        return repository.observeAccessStatus(feature, optionId)
    }
}
