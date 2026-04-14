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
import java.io.File

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
        const val DATABASE_NAME = "expense_tracker.db"

        @Volatile
        private var INSTANCE: ExpenseTrackerDatabase? = null

        fun getInstance(context: Context): ExpenseTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseTrackerDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
        }

        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        fun databaseFile(context: Context): File {
            return context.applicationContext.getDatabasePath(DATABASE_NAME)
        }

        fun databaseWalFile(context: Context): File {
            return File(databaseFile(context).path + "-wal")
        }

        fun databaseShmFile(context: Context): File {
            return File(databaseFile(context).path + "-shm")
        }
    }
}
