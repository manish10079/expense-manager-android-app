package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.PaymentType
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {
    fun observeActivePaymentMethods(): Flow<List<PaymentType>>

    fun observeAllPaymentMethods(): Flow<List<PaymentType>>

    fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>>

    suspend fun createCustomPaymentMethod(
        name: String,
        iconKey: String
    )

    suspend fun deleteCustomPaymentMethod(id: Int)
}
