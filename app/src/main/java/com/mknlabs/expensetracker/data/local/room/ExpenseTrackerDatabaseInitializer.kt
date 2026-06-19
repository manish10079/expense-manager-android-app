package com.mknlabs.expensetracker.data.local.room

import androidx.room.withTransaction
import android.content.Context
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.data.constants.countryCodeMap
import com.mknlabs.expensetracker.data.local.room.entities.CountryCodeEntity

object ExpenseTrackerDatabaseInitializer {

    suspend fun initialize(context: Context) {
        val database = ExpenseTrackerDatabase.getInstance(context)

        database.withTransaction {
            database.categoryDao().upsertAll(categoryMap.values.map { it.toEntity() })
            database.paymentMethodDao().upsertAll(paymentTypeMap.values.map { it.toEntity() })
            database.countryCodeDao().upsertAll(countryCodeMap.values.map { CountryCodeEntity.fromDomain(it) })
        }
    }
}
