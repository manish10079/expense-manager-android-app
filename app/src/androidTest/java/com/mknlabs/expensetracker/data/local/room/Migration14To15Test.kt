package com.mknlabs.expensetracker.data.local.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase.Companion.MIGRATION_14_15
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves the v14 -> v15 installment migration is fully backward compatible:
 * legacy rules gain the new columns with the REGULAR default and NULL plan
 * terms, their rows are untouched, and the new schedule table + indexes exist.
 *
 * Seeds through raw SQL against the REAL exported v14 schema (from the schema
 * JSONs packaged into the androidTest assets), never against the current
 * entities — that is what makes it a migration test rather than a schema test.
 */
@RunWith(AndroidJUnit4::class)
class Migration14To15Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseTrackerDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val db = "migration_14_15_test"

    @Test
    fun migrateTo15_keepsLegacyRulesIntact_andDefaultsThemToRegular() {
        helper.createDatabase(db, 14).apply {
            execSQL(
                "INSERT OR REPLACE INTO categories (id, name, transaction_type_id, icon_key, is_system, sort_order, is_deleted, sync_state, created_at, updated_at) " +
                    "VALUES (1, 'Cat', 2, 'x', 1, 0, 0, 'SYNCED', 0, 0)"
            )
            execSQL(
                "INSERT OR REPLACE INTO payment_methods (id, name, icon_key, is_system, sort_order, is_deleted, sync_state, created_at, updated_at) " +
                    "VALUES (1, 'Cash', 'x', 1, 0, 0, 'SYNCED', 0, 0)"
            )
            execSQL(
                """
                INSERT INTO transactions
                    (id, note, amount_minor, occurred_at, created_at, updated_at,
                     transaction_type_id, category_id, payment_method_id, is_deleted, sync_state)
                VALUES ('tx-legacy', 'Old EMI shaped rule', 60000, 1700000000000, 1700000000000, 1700000000000,
                        2, 1, 1, 0, 'SYNCED')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO recurring_rules
                    (id, transaction_id, frequency, interval_count, repeat_count, remaining_count,
                     anchor_at, next_run_at, last_run_at, last_notified_occurrence_at, is_enabled,
                     notifications_enabled, last_notified_window_days, created_at, updated_at, sync_state, is_deleted)
                VALUES ('rule-legacy', 'tx-legacy', 'Monthly', 1, 60, 58,
                        1700000000000, 1720000000000, 1710000000000, NULL, 1,
                        1, NULL, 1700000000000, 1710000000000, 'SYNCED', 0)
                """.trimIndent()
            )
            close()
        }

        // runMigrationsAndValidate also diffs the result against the exported
        // 15.json — table shape, indexes and FK definitions must all match.
        val migrated = helper.runMigrationsAndValidate(db, 15, true, MIGRATION_14_15)

        migrated.query("SELECT * FROM recurring_rules WHERE id = 'rule-legacy'").use { cursor ->
            assertTrue("legacy rule row must survive the migration", cursor.moveToFirst())
            val typeIndex = cursor.getColumnIndexOrThrow("recurring_type")
            val totalIndex = cursor.getColumnIndexOrThrow("installment_total_minor")
            val countIndex = cursor.getColumnIndexOrThrow("installment_total_count")
            val statusIndex = cursor.getColumnIndexOrThrow("installment_status")
            val remainingIndex = cursor.getColumnIndexOrThrow("remaining_count")
            val nextRunIndex = cursor.getColumnIndexOrThrow("next_run_at")

            // Additive column default: legacy rows are REGULAR without backfill.
            assertEquals("REGULAR", cursor.getString(typeIndex))
            assertTrue(cursor.isNull(totalIndex))
            assertTrue(cursor.isNull(countIndex))
            assertTrue(cursor.isNull(statusIndex))
            // Pre-existing data must be byte-identical.
            assertEquals(58, cursor.getInt(remainingIndex))
            assertEquals(1720000000000L, cursor.getLong(nextRunIndex))
        }

        // The new schedule table is empty but ready, and accepts rows linked
        // to existing rules.
        migrated.query("SELECT COUNT(*) FROM installment_occurrences").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        migrated.execSQL(
            """
            INSERT INTO installment_occurrences
                (id, rule_id, installment_index, due_at, amount_minor, status, created_at, updated_at, sync_state, is_deleted)
            VALUES ('rule-legacy_occ_1', 'rule-legacy', 1, 1719000000000, 10000, 'PENDING', 1719000000000, 1719000000000, 'SYNCED', 0)
            """.trimIndent()
        )
        migrated.query("SELECT COUNT(*) FROM installment_occurrences").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun migrationRejectsOrphanOccurrence_viaForeignKey() {
        helper.createDatabase(db, 14).close()
        val migrated = helper.runMigrationsAndValidate(db, 15, true, MIGRATION_14_15)
        // Framework connections default to FKs OFF; Room itself turns them on.
        migrated.setForeignKeyConstraintsEnabled(true)
        var rejected = false
        try {
            migrated.execSQL(
                """
                INSERT INTO installment_occurrences
                    (id, rule_id, installment_index, due_at, amount_minor, status, created_at, updated_at, sync_state, is_deleted)
                VALUES ('orphan', 'no-such-rule', 1, 1, 1, 'PENDING', 1, 1, 'SYNCED', 0)
                """.trimIndent()
            )
        } catch (e: Exception) {
            rejected = true
        }
        assertTrue("rule_id FK must reject occurrences of unknown rules", rejected)
        migrated.close()
    }
}
