package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import javax.inject.Inject

class BecomePremiumUseCase @Inject constructor(
    private val repository: MonetizationRepository
) {
    suspend fun execute() {
        repository.becomePremium()
    }
}
