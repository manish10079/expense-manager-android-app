package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.PaymentType
import kotlinx.coroutines.flow.Flow

interface PaymentMethodRepository {
    fun observeActivePaymentMethods(): Flow<List<PaymentType>>

    fun observeAllPaymentMethods(): Flow<List<PaymentType>>

    fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>>

    /** Creates a payment method the user made, optionally with a colour. See
     * [CategoryRepository.createCustomCategory] for the colour contract, which is the same
     * here: `#RRGGBB` or null, and null is the ordinary case rather than an error.
     */
    suspend fun createCustomPaymentMethod(
        name: String,
        iconKey: String,
        colorHex: String? = null
    )

    /**
     * Sets, changes or clears the colour of any payment method, seeded or user-created. See
     * [CategoryRepository.updateCategoryColor] for the contract, which is the same here.
     */
    suspend fun updatePaymentMethodColor(id: Int, colorHex: String?)

    suspend fun deleteCustomPaymentMethod(id: Int)
}
