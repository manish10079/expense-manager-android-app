package com.mknlabs.expensetracker.data.local.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase.Companion.MIGRATION_17_18
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Proves v17 -> v18 adds the three favourite sync columns without rewriting the template.
 *
 * `is_deleted` must stay 0 and `sync_state` must be PENDING_UPLOAD so every existing
 * favourite is owed exactly one push. `updated_at` must be stamped at migration time,
 * not left at the ALTER default of 0, or a peer's watermark would skip the row.
 */
@RunWith(AndroidJUnit4::class)
class Migration17To18Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseTrackerDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val db = "migration_17_18_test"

    @Test
    fun migrateTo18_addsSyncColumns_andStampsUpdatedAtSoExistingFavouritesPush() {
        val createdAt = 1_700_000_000_000L
        helper.createDatabase(db, 17).apply {
            execSQL(
                "INSERT INTO favorite_transactions (id, transaction_id, title, amount_minor, transaction_type_id, category_id, payment_type_id, note, is_pinned, created_at) " +
                    "VALUES ('fav-1', 'tx-1', 'Coffee', 4500, 2, 1, 1, 'latte', 1, $createdAt)"
            )
            close()
        }

        val before = System.currentTimeMillis()
        val migrated = helper.runMigrationsAndValidate(db, 18, true, MIGRATION_17_18)
        val after = System.currentTimeMillis()

        migrated.query("SELECT * FROM favorite_transactions WHERE id = 'fav-1'").use { cursor ->
            assertTrue("seeded favourite must survive the migration", cursor.moveToFirst())
            assertEquals("Coffee", cursor.getString(cursor.getColumnIndexOrThrow("title")))
            assertEquals("tx-1", cursor.getString(cursor.getColumnIndexOrThrow("transaction_id")))
            assertEquals(4500L, cursor.getLong(cursor.getColumnIndexOrThrow("amount_minor")))
            assertEquals(createdAt, cursor.getLong(cursor.getColumnIndexOrThrow("created_at")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("is_deleted")))
            assertEquals("PENDING_UPLOAD", cursor.getString(cursor.getColumnIndexOrThrow("sync_state")))
            val updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
            assertTrue(
                "updated_at must be the migration clock, not the ALTER default of 0",
                updatedAt in before..after
            )
        }

        migrated.close()
    }
}
