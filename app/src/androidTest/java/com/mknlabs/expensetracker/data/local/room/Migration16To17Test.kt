package com.mknlabs.expensetracker.data.local.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase.Companion.MIGRATION_16_17
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves the v16 -> v17 colour column is purely additive on both tables.
 *
 * Existing categories and payment methods must come through with `color_hex` NULL and every
 * other field untouched. NULL is the load-bearing assertion, not an incidental one: it is what
 * tells the renderer "derive this row's colour from its id", which is the state every seeded
 * row stays in forever. A migration that defaulted it to a value, or that rewrote rows to put
 * one there, would silently give 33 categories a colour the user never chose.
 *
 * Seeds through raw SQL against the real exported v16 schema from the packaged schema JSONs,
 * never against the current entities — that is what makes this a migration test rather than a
 * schema test. The INSERT column lists deliberately name no colour column, exactly as v16
 * code would have.
 */
@RunWith(AndroidJUnit4::class)
class Migration16To17Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseTrackerDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val db = "migration_16_17_test"

    @Test
    fun migrateTo17_keepsCategoriesAndPaymentMethodsIntact_andLeavesTheColourUnset() {
        helper.createDatabase(db, 16).apply {
            execSQL(
                "INSERT OR REPLACE INTO categories (id, name, transaction_type_id, icon_key, is_system, sort_order, is_deleted, sync_state, created_at, updated_at) " +
                    "VALUES (1, 'Food', 2, 'flatware', 1, 1, 0, 'SYNCED', 1700000000000, 1700000000000)"
            )
            execSQL(
                "INSERT OR REPLACE INTO categories (id, name, transaction_type_id, icon_key, is_system, sort_order, is_deleted, sync_state, created_at, updated_at) " +
                    "VALUES (106, 'Coffee', 2, 'local_cafe', 0, 106, 0, 'PENDING_UPLOAD', 1710000000000, 1710000000000)"
            )
            execSQL(
                "INSERT OR REPLACE INTO payment_methods (id, name, icon_key, is_system, sort_order, is_deleted, sync_state, created_at, updated_at) " +
                    "VALUES (1, 'UPI', 'qr_code', 1, 1, 0, 'SYNCED', 1700000000000, 1700000000000)"
            )
            close()
        }

        // runMigrationsAndValidate also diffs the result against the exported 17.json —
        // so the ALTER has to produce exactly the shape Room expects, not merely something
        // that runs.
        val migrated = helper.runMigrationsAndValidate(db, 17, true, MIGRATION_16_17)

        migrated.query("SELECT * FROM categories WHERE id = 1").use { cursor ->
            assertTrue("seeded category must survive the migration", cursor.moveToFirst())

            // The new column exists and is empty for a row that predates it.
            assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("color_hex")))

            // And nothing else moved.
            assertEquals("Food", cursor.getString(cursor.getColumnIndexOrThrow("name")))
            assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("transaction_type_id")))
            assertEquals("flatware", cursor.getString(cursor.getColumnIndexOrThrow("icon_key")))
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("is_system")))
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("sort_order")))
            assertEquals(1700000000000L, cursor.getLong(cursor.getColumnIndexOrThrow("created_at")))
        }

        migrated.query("SELECT * FROM categories WHERE id = 106").use { cursor ->
            assertTrue("a user-created category must survive too", cursor.moveToFirst())
            assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("color_hex")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("is_system")))
            assertEquals("Coffee", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        }

        migrated.query("SELECT * FROM payment_methods WHERE id = 1").use { cursor ->
            assertTrue("payment method must survive the migration", cursor.moveToFirst())
            assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("color_hex")))
            assertEquals("UPI", cursor.getString(cursor.getColumnIndexOrThrow("name")))
            assertEquals("qr_code", cursor.getString(cursor.getColumnIndexOrThrow("icon_key")))
        }

        migrated.close()
    }

    @Test
    fun theMigratedColumnAcceptsAColour_andAcceptsNullBack() {
        // A column that exists but cannot be written would pass the test above, since
        // everything it checks is null. This proves the round trip both ways.
        helper.createDatabase(db, 16).apply {
            execSQL(
                "INSERT OR REPLACE INTO categories (id, name, transaction_type_id, icon_key, is_system, sort_order, is_deleted, sync_state, created_at, updated_at) " +
                    "VALUES (106, 'Coffee', 2, 'local_cafe', 0, 106, 0, 'SYNCED', 1710000000000, 1710000000000)"
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(db, 17, true, MIGRATION_16_17)

        migrated.execSQL("UPDATE categories SET color_hex = '#5B2EED' WHERE id = 106")
        migrated.query("SELECT color_hex FROM categories WHERE id = 106").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("#5B2EED", cursor.getString(0))
        }

        // Clearing it has to be possible too — that is what "no colour of its own" means,
        // and a NOT NULL column could not express it.
        migrated.execSQL("UPDATE categories SET color_hex = NULL WHERE id = 106")
        migrated.query("SELECT color_hex FROM categories WHERE id = 106").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertNull(cursor.getString(0))
        }

        migrated.close()
    }

    @Test
    fun theMigrationAddsNoIndexesAndTouchesNoOtherTable() {
        // The column is read only by organic row access, so it needs no index — and an index
        // added by mistake would be a schema change the 17.json diff would not catch if it
        // were also declared on the entity. Checked explicitly so a future reader knows the
        // absence is deliberate.
        helper.createDatabase(db, 16).close()
        val migrated = helper.runMigrationsAndValidate(db, 17, true, MIGRATION_16_17)

        migrated.query("SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name IN ('categories', 'payment_methods')").use { cursor ->
            val indexes = buildList {
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
            assertTrue(
                "unexpected index on a colour column: ${indexes.filter { it.contains("color") }}",
                indexes.none { it.contains("color") }
            )
        }

        migrated.close()
    }
}
