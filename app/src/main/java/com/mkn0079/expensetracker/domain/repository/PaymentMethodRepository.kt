package com.mkn0079.expensetracker.domain.repository

import com.mkn0079.expensetracker.models.PaymentType
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {
    fun observeActivePaymentMethods(): Flow<List<PaymentType>>

    fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>>

    suspend fun createCustomPaymentMethod(
        name: String,
        iconKey: String
    )

    suspend fun deleteCustomPaymentMethod(id: Int)
}
