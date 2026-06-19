package com.mknlabs.expensetracker.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mknlabs.expensetracker.data.local.room.dao.BudgetDao
import com.mknlabs.expensetracker.data.local.room.dao.CategoryDao
import com.mknlabs.expensetracker.data.local.room.dao.GoalDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.data.local.room.dao.RecurringRuleDao
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.dao.CountryCodeDao
import com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity
import com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity
import com.mknlabs.expensetracker.data.local.room.entities.GoalEntity
import com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity
import com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity
import com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity
import com.mknlabs.expensetracker.data.local.room.entities.CountryCodeEntity
import java.io.File

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class,
        GoalEntity::class,
        CountryCodeEntity::class
    ],
    version = 8,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class ExpenseTrackerDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringRuleDao(): RecurringRuleDao
    abstract fun goalDao(): GoalDao
    abstract fun countryCodeDao(): CountryCodeDao

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
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build().also { INSTANCE = it }
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `country_codes` (
                        `id` INTEGER NOT NULL, 
                        `country` TEXT NOT NULL, 
                        `dial_code` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add is_deleted to goals
                db.execSQL("ALTER TABLE goals ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add last_notified_occurrence_at to recurring_rules
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN last_notified_occurrence_at INTEGER")

                // Create goals table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `goals` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `target_amount_minor` INTEGER NOT NULL, 
                        `current_amount_minor` INTEGER NOT NULL, 
                        `deadline_at` INTEGER, 
                        `icon_key` TEXT NOT NULL, 
                        `color_hex` TEXT NOT NULL, 
                        `is_completed` INTEGER NOT NULL, 
                        `created_at` INTEGER NOT NULL, 
                        `updated_at` INTEGER NOT NULL, 
                        `sync_state` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add sync_state to categories
                db.execSQL("ALTER TABLE categories ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'LOCAL_ONLY'")
                // Add sync_state to payment_methods
                db.execSQL("ALTER TABLE payment_methods ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'LOCAL_ONLY'")
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recurring Rules
                db.execSQL("DROP INDEX IF EXISTS `index_recurring_rules_transaction_id_is_deleted` ")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_rules_transaction_id_is_deleted` ON `recurring_rules` (`transaction_id`, `is_deleted`)")

                // Budgets
                db.execSQL("DROP INDEX IF EXISTS `index_budgets_category_id_month_start_is_deleted` ")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_category_id_month_start_is_deleted` ON `budgets` (`category_id`, `month_start`, `is_deleted`)")

                // Categories
                db.execSQL("DROP INDEX IF EXISTS `index_categories_name_transaction_type_id_is_deleted` ")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_name_transaction_type_id_is_deleted` ON `categories` (`name`, `transaction_type_id`, `is_deleted`)")

                // Payment Methods
                db.execSQL("DROP INDEX IF EXISTS `index_payment_methods_name_is_deleted` ")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_methods_name_is_deleted` ON `payment_methods` (`name`, `is_deleted`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE budgets ADD COLUMN edit_count INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Migrate transactions and budgets using duplicate 'Bill' (21) to 'Bills' (4)
                db.execSQL("UPDATE transactions SET category_id = 4 WHERE category_id = 21")
                db.execSQL("UPDATE budgets SET category_id = 4 WHERE category_id = 21")
                // Remove the duplicate category
                db.execSQL("DELETE FROM categories WHERE id = 21")
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
