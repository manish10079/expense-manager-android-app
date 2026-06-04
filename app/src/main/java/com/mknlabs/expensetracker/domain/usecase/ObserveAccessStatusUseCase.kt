package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAccessStatusUseCase @Inject constructor(
    private val repository: MonetizationRepository
) {
    operator fun invoke(feature: Feature, optionId: String? = null): Flow<AccessStatus> {
        return repository.observeAccessStatus(feature, optionId)
    }
}
