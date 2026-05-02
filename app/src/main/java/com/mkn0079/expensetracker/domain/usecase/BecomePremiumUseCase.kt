package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.MonetizationRepository
import javax.inject.Inject

class BecomePremiumUseCase @Inject constructor(
    private val repository: MonetizationRepository
) {
    suspend fun execute() {
        repository.becomePremium()
    }
}
