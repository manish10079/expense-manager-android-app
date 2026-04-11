package com.mkn0079.expensetracker.data.local.room

import androidx.room.withTransaction
import android.content.Context
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap

object ExpenseTrackerDatabaseInitializer {

    suspend fun initialize(context: Context) {
        val database = ExpenseTrackerDatabase.getInstance(context)

        database.withTransaction {
            if (database.categoryDao().countAll() == 0) {
                database.categoryDao().upsertAll(categoryMap.values.map { it.toEntity() })
            }

            if (database.paymentMethodDao().countAll() == 0) {
                database.paymentMethodDao().upsertAll(paymentTypeMap.values.map { it.toEntity() })
            }
        }
    }
}
