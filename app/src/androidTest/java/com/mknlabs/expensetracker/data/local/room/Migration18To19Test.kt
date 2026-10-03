package com.mknlabs.expensetracker.data.local.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase.Companion.MIGRATION_18_19
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves v18 -> v19 adds the two tag tables additively, without touching existing data.
 *
 * The migration is pure DDL, so the things worth pinning are that a pre-existing
 * transaction survives untouched, that the join table really is keyed on the pair, and
 * that the two indexes the tag filter depends on were created.
 */
@RunWith(AndroidJUnit4::class)
class Migration18To19Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseTrackerDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val db = "migration_18_19_test"

    @Test
    fun migrateTo19_addsTagTables_andKeepsExistingTransactions() {
        val occurredAt = 1_700_000_000_000L
        helper.createDatabase(db, 18).apply {
            execSQL(
                "INSERT INTO transactions (id, note, amount_minor, occurred_at, created_at, updated_at, " +
                    "transaction_type_id, category_id, payment_type_id, is_deleted, sync_state) " +
                    "VALUES ('tx-1', 'Coffee', 4500, $occurredAt, $occurredAt, $occurredAt, 2, 1, 1, 0, 'SYNCED')"
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(db, 19, true, MIGRATION_18_19)

        // The pre-existing row is untouched.
        migrated.query("SELECT * FROM transactions WHERE id = 'tx-1'").use { cursor ->
            assertTrue("seeded transaction must survive the migration", cursor.moveToFirst())
            assertEquals("Coffee", cursor.getString(cursor.getColumnIndexOrThrow("note")))
            assertEquals(4500L, cursor.getLong(cursor.getColumnIndexOrThrow("amount_minor")))
        }

        // Both new tables exist and are empty.
        migrated.query("SELECT COUNT(*) FROM tags").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        migrated.query("SELECT COUNT(*) FROM transaction_tags").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }

        // A tag can be created and linked, which exercises the schema the feature needs.
        migrated.execSQL(
            "INSERT INTO tags (id, name, name_lower, color_hex, is_deleted, sync_state, created_at, updated_at) " +
                "VALUES ('tag-1', 'Trip', 'trip', NULL, 0, 'PENDING_UPLOAD', $occurredAt, $occurredAt)"
        )
        migrated.execSQL(
            "INSERT INTO transaction_tags (transaction_id, tag_id, created_at, updated_at, sync_state, is_deleted) " +
                "VALUES ('tx-1', 'tag-1', $occurredAt, $occurredAt, 'PENDING_UPLOAD', 0)"
        )
        migrated.query("SELECT COUNT(*) FROM transaction_tags").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }

        migrated.close()
    }
}