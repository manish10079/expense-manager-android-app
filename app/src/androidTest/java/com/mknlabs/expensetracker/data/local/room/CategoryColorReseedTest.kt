package com.mknlabs.expensetracker.data.local.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.data.repository.CategoryRepository
import com.mknlabs.expensetracker.data.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.models.SyncState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The claim the whole of phase 7 rests on: a colour the user puts on a **seeded** category or payment
 * method is still there after the app starts again.
 *
 * This is the one behaviour a unit test cannot reach and a UI test cannot either, because it is a
 * property of two launches rather than one screen. The seeder runs on every launch and rebuilds each
 * seeded row from the constant with `upsertAll`, which is replace-on-primary-key — so the write that
 * destroys a stored colour is not the recolour, it is the next startup, and it happens days later.
 * That is why it presented as "my colours reset sometimes" rather than as a broken picker.
 *
 * The test performs the sequence the app performs: seed (launch one), recolour, seed again against
 * the same store (launch two). An in-memory database is the store, which is what lets both launches
 * be asserted in one process without killing it.
 */
@RunWith(AndroidJUnit4::class)
class CategoryColorReseedTest {

    private lateinit var database: ExpenseTrackerDatabase
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var paymentMethodRepository: PaymentMethodRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseTrackerDatabase::class.java
        ).build()
        categoryRepository = CategoryRepository(database.categoryDao())
        paymentMethodRepository = PaymentMethodRepository(database.paymentMethodDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun aRecolouredSeededCategorySurvivesTheNextLaunch() = runTest {
        ExpenseTrackerDatabaseInitializer.initialize(database)

        val seeded = activeCategory(id = FOOD_ID)
        assertNull("a seeded category should start with no colour of its own", seeded.colorHex)

        categoryRepository.updateCategoryColor(id = FOOD_ID, colorHex = "#9333EA")

        ExpenseTrackerDatabaseInitializer.initialize(database)

        val afterRestart = activeCategory(id = FOOD_ID)
        assertEquals("#9333EA", afterRestart.colorHex)
        // Everything else is still the app's: the reseed is meant to rewrite the row, and it does.
        assertEquals(categoryMap.getValue(FOOD_ID).name, afterRestart.name)
        assertEquals(categoryMap.getValue(FOOD_ID).iconKey, afterRestart.iconKey)
        assertTrue(afterRestart.isSystem)
    }

    @Test
    fun aRecolouredSeededPaymentMethodSurvivesTheNextLaunch() = runTest {
        ExpenseTrackerDatabaseInitializer.initialize(database)

        paymentMethodRepository.updatePaymentMethodColor(id = CASH_ID, colorHex = "#0288D1")

        ExpenseTrackerDatabaseInitializer.initialize(database)

        val afterRestart = activePaymentMethod(id = CASH_ID)
        assertEquals("#0288D1", afterRestart.colorHex)
        assertEquals(paymentTypeMap.getValue(CASH_ID).name, afterRestart.name)
    }

    @Test
    fun theColourIsMarkedForUploadWhenItIsSet() = runTest {
        // Syncing is how the colour reaches the user's other devices, and the push reads exactly the
        // rows marked out of SYNCED. A recolour that stored the value without moving the row into the
        // upload set would look perfect locally and never leave the phone — and since the reseed
        // rewrites seeded rows on every launch, it would also be the *second* device's launch that
        // pushed `null` over the first device's choice.
        ExpenseTrackerDatabaseInitializer.initialize(database)

        categoryRepository.updateCategoryColor(id = FOOD_ID, colorHex = "#9333EA")

        assertEquals(SyncState.PENDING_UPLOAD, database.categoryDao().getById(FOOD_ID)?.syncState)
        assertNotNull(
            "the recoloured row is missing from the upload set",
            database.categoryDao().getUnsynced().firstOrNull { it.id == FOOD_ID }
        )
    }

    @Test
    fun clearingASeededColoursOverrideStaysCleared() = runTest {
        // The default swatch, across a restart. Without the carry-over being null-preserving, a
        // cleared row could come back coloured from a stale value — and "reset" that does not reset
        // is indistinguishable from a broken button.
        ExpenseTrackerDatabaseInitializer.initialize(database)
        categoryRepository.updateCategoryColor(id = FOOD_ID, colorHex = "#9333EA")
        ExpenseTrackerDatabaseInitializer.initialize(database)

        categoryRepository.updateCategoryColor(id = FOOD_ID, colorHex = null)

        ExpenseTrackerDatabaseInitializer.initialize(database)

        assertNull(activeCategory(id = FOOD_ID).colorHex)
    }

    @Test
    fun aColourOnAUserCreatedCategoryIsUntouchedByTheReseed() = runTest {
        // A custom row is not in the constant at all, so the seeder never writes it — but this is the
        // assertion that says so, because a seeder that deleted or blanked what it did not recognise
        // would be a far worse bug than the one phase 7 fixed.
        ExpenseTrackerDatabaseInitializer.initialize(database)

        categoryRepository.createCustomCategory(
            name = "Coffee runs",
            iconKey = "coffee",
            transactionTypeId = 2,
            colorHex = "#D97706"
        )
        val customId = categoryRepository.observeActiveCategories().first()
            .single { it.name == "Coffee runs" }
            .id

        ExpenseTrackerDatabaseInitializer.initialize(database)

        val afterRestart = activeCategory(id = customId)
        assertEquals("#D97706", afterRestart.colorHex)
        assertEquals("Coffee runs", afterRestart.name)
    }

    @Test
    fun anUnrecolouredSeededRowStaysColourlessAcrossLaunches() = runTest {
        // The ordinary case, and the one the palette depends on: nothing may invent a colour for a
        // seeded row, or the palette would stop being the single source of truth for its id.
        ExpenseTrackerDatabaseInitializer.initialize(database)
        ExpenseTrackerDatabaseInitializer.initialize(database)

        assertNull(activeCategory(id = TRAVEL_ID).colorHex)
        assertNull(activePaymentMethod(id = CASH_ID).colorHex)
    }

    private suspend fun activeCategory(id: Int) =
        categoryRepository.observeActiveCategories().first().single { it.id == id }

    private suspend fun activePaymentMethod(id: Int) =
        paymentMethodRepository.observeActivePaymentMethods().first().single { it.id == id }

    private companion object {
        const val FOOD_ID = 1
        const val TRAVEL_ID = 2
        const val CASH_ID = 2
    }
}
