package com.mknlabs.expensetracker.data.local.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase.Companion.MIGRATION_19_20
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves v19 -> v20 adds the funds table and the nullable transaction link additively,
 * without touching existing data.
 *
 * The migration is pure DDL, so the things worth pinning are that a pre-existing
 * transaction survives untouched and gains a NULL link, that the new table really is
 * keyed on its id, and that a fund and a transaction can be linked to each other.
 */
@RunWith(AndroidJUnit4::class)
class Migration19To20Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseTrackerDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val db = "migration_19_20_test"

    @Test
    fun migrateTo20_addsFundsTable_andNullFundLinkOnExistingTransactions() {
        val occurredAt = 1_700_000_000_000L
        helper.createDatabase(db, 19).apply {
            execSQL(
                "INSERT INTO transactions (id, note, amount_minor, occurred_at, created_at, updated_at, " +
                    "transaction_type_id, category_id, payment_method_id, is_deleted, sync_state) " +
                    "VALUES ('tx-1', 'Coffee', 4500, $occurredAt, $occurredAt, $occurredAt, 2, 1, 1, 0, 'SYNCED')"
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(db, 20, true, MIGRATION_19_20)

        // The pre-existing row is untouched and its new link is null.
        migrated.query("SELECT * FROM transactions WHERE id = 'tx-1'").use { cursor ->
            assertTrue("seeded transaction must survive the migration", cursor.moveToFirst())
            assertEquals("Coffee", cursor.getString(cursor.getColumnIndexOrThrow("note")))
            assertEquals(4500L, cursor.getLong(cursor.getColumnIndexOrThrow("amount_minor")))
            assertNull(cursor.getString(cursor.getColumnIndexOrThrow("fund_id")))
        }

        // The new table exists and is empty.
        migrated.query("SELECT COUNT(*) FROM funds").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }

        // A fund can be created and a transaction linked to it, which exercises the schema
        // the feature needs.
        migrated.execSQL(
            "INSERT INTO funds (id, name, amount_minor, start_date, icon_key, color_hex, note, " +
                "is_archived, created_at, updated_at, is_deleted, sync_state) " +
                "VALUES ('fund-1', 'Gift', 100000, $occurredAt, 'card_giftcard', '#F59E0B', '', " +
                "0, $occurredAt, $occurredAt, 0, 'PENDING_UPLOAD')"
        )
        migrated.execSQL("UPDATE transactions SET fund_id = 'fund-1' WHERE id = 'tx-1'")

        migrated.query("SELECT COUNT(*) FROM funds").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
        migrated.query("SELECT fund_id FROM transactions WHERE id = 'tx-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("fund-1", cursor.getString(cursor.getColumnIndexOrThrow("fund_id")))
        }

        migrated.close()
    }
}
