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
import com.mknlabs.expensetracker.data.local.room.dao.GoalFundEntryDao
import com.mknlabs.expensetracker.data.local.room.dao.InstallmentOccurrenceDao
import com.mknlabs.expensetracker.data.local.room.dao.PaymentMethodDao
import com.mknlabs.expensetracker.data.local.room.dao.RecurringRuleDao
import com.mknlabs.expensetracker.data.local.room.dao.TransactionDao
import com.mknlabs.expensetracker.data.local.room.dao.CountryCodeDao
import com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity
import com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity
import com.mknlabs.expensetracker.data.local.room.entities.GoalEntity
import com.mknlabs.expensetracker.data.local.room.entities.GoalFundEntryEntity
import com.mknlabs.expensetracker.data.local.room.entities.InstallmentOccurrenceEntity
import com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity
import com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity
import com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity
import com.mknlabs.expensetracker.data.local.room.dao.FavoriteTransactionDao
import com.mknlabs.expensetracker.data.local.room.entities.CountryCodeEntity
import com.mknlabs.expensetracker.data.local.room.entities.FavoriteTransactionEntity
import com.mknlabs.expensetracker.data.local.room.dao.DetectedSmsNotificationDao
import com.mknlabs.expensetracker.data.local.room.entities.DetectedSmsNotificationEntity
import java.io.File

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        PaymentMethodEntity::class,
        BudgetEntity::class,
        RecurringRuleEntity::class,
        GoalEntity::class,
        GoalFundEntryEntity::class,
        CountryCodeEntity::class,
        FavoriteTransactionEntity::class,
        InstallmentOccurrenceEntity::class,
        DetectedSmsNotificationEntity::class
    ],
    version = 18,
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
    abstract fun goalFundEntryDao(): GoalFundEntryDao
    abstract fun countryCodeDao(): CountryCodeDao
    abstract fun favoriteTransactionDao(): FavoriteTransactionDao
    abstract fun installmentOccurrenceDao(): InstallmentOccurrenceDao
    abstract fun detectedSmsNotificationDao(): DetectedSmsNotificationDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18)
                    .build().also { INSTANCE = it }
            }
        }

        /**
         * Favorites join the synced set. Additive: nothing is dropped, and no favorite is
         * rewritten beyond its new bookkeeping columns.
         *
         * The three columns are the same ones every other synced table carries, so the
         * push/pull code needs no special case - `sync_state` is what `getUnsynced()`
         * selects on, `is_deleted` is the tombstone an unfavorite leaves for the other
         * devices to read, and `updated_at` is the watermark their pull filters on.
         *
         * `DEFAULT 0` is only there to satisfy the NOT NULL ALTER on a table that already
         * has rows. `is_deleted` and `sync_state` keep their defaults: nothing is deleted
         * here, and every existing favorite is owed exactly one push.
         */
        // internal (not private) so the androidTest migration suite can run it
        // through MigrationTestHelper without duplicating its SQL.
        internal val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE favorite_transactions ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE favorite_transactions ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE favorite_transactions ADD COLUMN sync_state TEXT NOT NULL DEFAULT 'PENDING_UPLOAD'")

                // The one part that is not just a column add. A pull only fetches docs
                // newer than the device's last watermark, so a favorite that predates sync
                // and kept its `created_at` would sit below every watermark in existence
                // and never reach another device. Stamping the migration's own clock on the
                // existing rows lifts them above all of them, which is what carries them
                // across on the next sync. A row written after this keeps the timestamp its
                // own write gives it and is unaffected.
                val migratedAt = System.currentTimeMillis()
                db.execSQL("UPDATE favorite_transactions SET updated_at = $migratedAt")
            }
        }

        /**
         * Recurring + EMI decoupling (additive, fully backward compatible).
         *
         * - Every existing rule becomes [RecurringType.REGULAR] via the column
         *   default, so behavior is byte-for-byte what it was before: the worker
         *   keeps generating occurrences from the template transaction.
         * - The installment columns are nullable and stay NULL for legacy rules,
         *   so nothing has to be backfilled and no rule needs rewriting.
         * - No data is moved, copied or deleted, so the migration is O(1) in the
         *   number of rules and cannot fail partway on a large table.
         *
         * The `DEFAULT 'REGULAR'` on a NOT NULL column is what makes the ALTER
         * legal on a table that already has rows — the same approach
         * MIGRATION_8_9 used for `notifications_enabled`.
         */
        // internal (not private) so the androidTest migration suite can run it
        // through MigrationTestHelper without duplicating its SQL.
        internal val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN recurring_type TEXT NOT NULL DEFAULT 'REGULAR'")
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN installment_total_minor INTEGER")
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN installment_amount_minor INTEGER")
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN installment_total_count INTEGER")
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN installment_status TEXT")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `installment_occurrences` (
                        `id` TEXT NOT NULL,
                        `rule_id` TEXT NOT NULL,
                        `installment_index` INTEGER NOT NULL,
                        `due_at` INTEGER NOT NULL,
                        `amount_minor` INTEGER NOT NULL,
                        `paid_at` INTEGER,
                        `status` TEXT NOT NULL,
                        `transaction_id` TEXT,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `sync_state` TEXT NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`rule_id`) REFERENCES `recurring_rules`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_installment_occurrences_rule_id_due_at` ON `installment_occurrences` (`rule_id`, `due_at`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_installment_occurrences_rule_id_status` ON `installment_occurrences` (`rule_id`, `status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_installment_occurrences_transaction_id` ON `installment_occurrences` (`transaction_id`)")
            }
        }

        /**
         * SMS detection inbox (detected transaction inbox).
         *
         * Purely additive: one new table that nothing else references, so no existing
         * row is read, moved or rewritten and the migration is O(1) regardless of how
         * many transactions the user has. The FK to `transactions` is `ON DELETE SET
         * NULL` on purpose — purging an inbox row can never delete a real expense,
         * and hard-deleting a transaction only unlinks its detection.
         */
        // internal (not private) so the androidTest migration suite can run it
        // through MigrationTestHelper without duplicating its SQL.
        internal val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `detected_sms_notifications` (
                        `id` TEXT NOT NULL,
                        `sms_hash` TEXT NOT NULL,
                        `sender` TEXT NOT NULL,
                        `message_body` TEXT NOT NULL,
                        `amount_minor` INTEGER NOT NULL,
                        `transaction_type_id` INTEGER NOT NULL,
                        `merchant_name` TEXT,
                        `detected_at` INTEGER NOT NULL,
                        `notification_created_at` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `linked_transaction_id` TEXT,
                        `source` TEXT NOT NULL,
                        `confidence_score` REAL NOT NULL,
                        `suggested_category_id` INTEGER,
                        `notification_id` INTEGER,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`linked_transaction_id`) REFERENCES `transactions`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_detected_sms_notifications_sms_hash` ON `detected_sms_notifications` (`sms_hash`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_detected_sms_notifications_status_detected_at` ON `detected_sms_notifications` (`status`, `detected_at`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_detected_sms_notifications_detected_at` ON `detected_sms_notifications` (`detected_at`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_detected_sms_notifications_linked_transaction_id` ON `detected_sms_notifications` (`linked_transaction_id`)")
            }
        }

        /**
         * An optional user-chosen colour on categories and payment methods.
         *
         * Two additive columns and nothing else. Both are nullable with no DEFAULT, which is
         * what makes this O(1) and impossible to fail partway on a large table: no row is
         * read, rewritten, copied or moved, and an existing row simply gains a NULL it was
         * always going to have.
         *
         * NULL is the load-bearing value here rather than an absence of one. It means "this
         * row has no colour of its own, derive one from its id", which is the state every
         * existing row — and every seeded row, forever — is in. That is what keeps the
         * palette in `CategoryPalette.kt` the single source of truth for the categories the
         * app ships, and what lets the per-launch reseed of those rows run without ever
         * overwriting a choice the user made.
         *
         * The alternative, a NOT NULL column defaulted to an empty string, was rejected: an
         * empty string cannot be told apart from a truncated write, so the resolver would
         * have had to treat corruption and "no override" as the same case.
         */
        // internal (not private) so the androidTest migration suite can run it
        // through MigrationTestHelper without duplicating its SQL.
        internal val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN color_hex TEXT")
                db.execSQL("ALTER TABLE payment_methods ADD COLUMN color_hex TEXT")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Dedup key for favorites: one favorite per source transaction.
                // Nullable on purpose — SQLite unique indexes treat NULLs as
                // distinct, so legacy favorites (created before this column
                // existed) keep their own rows untouched.
                db.execSQL("ALTER TABLE favorite_transactions ADD COLUMN transaction_id TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_favorite_transactions_transaction_id` ON `favorite_transactions` (`transaction_id`)")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `favorite_transactions` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `amount_minor` INTEGER NOT NULL,
                        `transaction_type_id` INTEGER NOT NULL DEFAULT 2,
                        `category_id` INTEGER NOT NULL DEFAULT 0,
                        `payment_type_id` INTEGER NOT NULL DEFAULT 0,
                        `note` TEXT NOT NULL DEFAULT '',
                        `is_pinned` INTEGER NOT NULL DEFAULT 1,
                        `created_at` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_favorite_transactions_is_pinned_title` ON `favorite_transactions` (`is_pinned`, `title`)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE budgets ADD COLUMN category_ids TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE budgets ADD COLUMN name TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE budgets ADD COLUMN period TEXT NOT NULL DEFAULT 'MONTHLY'")
                db.execSQL("UPDATE budgets SET category_ids = CAST(category_id AS TEXT) WHERE category_id IS NOT NULL AND category_id != 0")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `goal_fund_entries` (
                        `id` TEXT NOT NULL,
                        `goal_id` TEXT NOT NULL,
                        `amount_minor` INTEGER NOT NULL,
                        `note` TEXT NOT NULL DEFAULT '',
                        `funded_at` INTEGER NOT NULL,
                        `sync_state` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`goal_id`) REFERENCES `goals`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_goal_fund_entries_goal_id_funded_at` ON `goal_fund_entries` (`goal_id`, `funded_at`)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Per-rule notification mute (notification spec): each recurring
                // rule decides independently whether it posts reminders.
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN notifications_enabled INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Which multi-window advance alert (7/3/1/due) has fired for the
                // current occurrence of each recurring rule.
                db.execSQL("ALTER TABLE recurring_rules ADD COLUMN last_notified_window_days INTEGER")
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
