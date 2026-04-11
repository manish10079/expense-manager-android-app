package com.mkn0079.expensetracker.data.repository

import android.content.Context
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.toDomain
import com.mkn0079.expensetracker.data.local.room.toEntity
import com.mkn0079.expensetracker.models.PaymentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentMethodRepository(context: Context) {

    private val dao = ExpenseTrackerDatabase.getInstance(context).paymentMethodDao()

    fun observeActivePaymentMethods(): Flow<List<PaymentType>> {
        return dao.observeActivePaymentMethods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>> {
        return dao.observeActiveCustomPaymentMethods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun createCustomPaymentMethod(
        name: String,
        iconKey: String
    ) {
        val now = System.currentTimeMillis()
        val nextId = dao.getMaxId() + 1
        dao.upsert(
            PaymentType(
                id = nextId,
                name = name,
                iconKey = iconKey,
                isSystem = false,
                sortOrder = nextId,
                isDeleted = false,
                createdAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    suspend fun deleteCustomPaymentMethod(id: Int) {
        dao.softDelete(id = id, updatedAt = System.currentTimeMillis())
    }
}
