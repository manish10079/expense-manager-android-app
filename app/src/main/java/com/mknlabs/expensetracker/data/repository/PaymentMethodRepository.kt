package com.mknlabs.expensetracker.data.repository

import android.content.Context
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository as DomainPaymentMethodRepository
import com.mknlabs.expensetracker.models.PaymentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.mknlabs.expensetracker.models.SyncState

class PaymentMethodRepository(context: Context) : DomainPaymentMethodRepository {

    private val dao = ExpenseTrackerDatabase.getInstance(context).paymentMethodDao()

    override fun observeActivePaymentMethods(): Flow<List<PaymentType>> {
        return dao.observeActivePaymentMethods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>> {
        return dao.observeActiveCustomPaymentMethods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createCustomPaymentMethod(
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
                updatedAt = now,
                syncState = SyncState.PENDING_UPLOAD
            ).toEntity()
        )
    }

    override suspend fun deleteCustomPaymentMethod(id: Int) {
        dao.softDelete(id = id, updatedAt = System.currentTimeMillis())
        dao.updateSyncState(id = id, syncState = SyncState.PENDING_DELETE.name)
    }
}
