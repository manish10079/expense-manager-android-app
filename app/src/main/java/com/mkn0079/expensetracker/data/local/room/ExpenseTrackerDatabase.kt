package com.mkn0079.expensetracker.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mkn0079.expensetracker.data.local.room.dao.BudgetDao
import com.mkn0079.expensetracker.data.local.room.dao.CategoryDao
import com.mkn0079.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mkn0079.expensetracker.data.local.room.dao.RecurringRuleDao
import com.mkn0079.expensetracker.data.local.room.dao.TransactionDao
import com.mkn0079.expensetracker.data.local.room.entities.BudgetEntity
import com.mkn0079.expensetracker.data.local.room.entities.CategoryEntity
import com.mkn0079.expensetracker.data.local.room.entities.PaymentMethodEntity
import com.mkn0079.expensetracker.data.local.room.entities.RecurringRuleEntity
import com.mkn0079.expensetracker.data.local.room.entities.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class ExpenseTrackerDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseTrackerDatabase? = null

        fun getInstance(context: Context): ExpenseTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseTrackerDatabase::class.java,
                    "expense_tracker.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
