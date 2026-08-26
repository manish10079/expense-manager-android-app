package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository as DomainPaymentMethodRepository
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class PaymentMethodRepository @Inject constructor(
    private val dao: PaymentMethodDao
) : DomainPaymentMethodRepository {

    override fun observeActivePaymentMethods(): Flow<List<PaymentType>> {
        return dao.observeActivePaymentMethods().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeAllPaymentMethods(): Flow<List<PaymentType>> {
        return dao.observeAllPaymentMethods().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>> {
        return dao.observeActiveCustomPaymentMethods().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun createCustomPaymentMethod(
        name: String,
        iconKey: String
    ) = withContext(Dispatchers.IO) {
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

    override suspend fun deleteCustomPaymentMethod(id: Int) = withContext(Dispatchers.IO) {
        dao.softDelete(id = id, updatedAt = System.currentTimeMillis())
    }
}
