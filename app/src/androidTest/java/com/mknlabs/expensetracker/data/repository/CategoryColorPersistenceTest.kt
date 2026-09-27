package com.mknlabs.expensetracker.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity
import com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity
import com.mknlabs.expensetracker.data.local.room.toDomain
import com.mknlabs.expensetracker.data.local.room.toEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the colour column against a real (in-memory) Room database — DAO, converter and
 * mapper included — because that is where the two ways a user's colour can quietly disappear
 * actually live.
 *
 * The first is the recreate-after-delete path. `createCustomCategory` reactivates a
 * soft-deleted row rather than inserting, using `copy`, which carries every field it is not
 * given. A colour left out of that `copy` is inherited from the row the user deleted, so the
 * picker appears to work and then produces the wrong colour only when the user recreates
 * something they had removed earlier.
 *
 * The second is the storage contract itself: the column holds a canonical `#RRGGBB` or holds
 * nothing, so a value round-tripping through the cloud or a backup cannot arrive in a shape
 * the renderer does not recognise.
 *
 * Rows are looked up by name rather than by id. Nothing seeds the built-in map here, so
 * `getMaxId()` starts at zero and the first created category takes id 1 — an assertion against
 * 106 would be asserting the test's own setup rather than the repository's behaviour.
 */
@RunWith(AndroidJUnit4::class)
class CategoryColorPersistenceTest {

    private lateinit var db: ExpenseTrackerDatabase
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var paymentRepository: PaymentMethodRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExpenseTrackerDatabase::class.java).build()
        categoryRepository = CategoryRepository(db.categoryDao())
        paymentRepository = PaymentMethodRepository(db.paymentMethodDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun category(name: String): CategoryEntity? =
        db.categoryDao().getActiveCategories().firstOrNull { it.name == name }

    private suspend fun paymentMethod(name: String): PaymentMethodEntity? =
        db.paymentMethodDao().getActivePaymentMethods().firstOrNull { it.name == name }

    // ── The storage contract ───────────────────────────────────────────────────

    @Test
    fun aCategoryCreatedWithoutAColour_storesNull() = runTest {
        categoryRepository.createCustomCategory(name = "Coffee", iconKey = "local_cafe", transactionTypeId = 2)

        val created = category("Coffee")
        assertEquals("Coffee", created?.name)
        assertNull("a category with no colour of its own must store NULL, not ''", created?.colorHex)
    }

    @Test
    fun aCategoryCreatedWithAColour_storesItCanonically() = runTest {
        categoryRepository.createCustomCategory(
            name = "Coffee", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "#5B2EED"
        )

        assertEquals("#5B2EED", category("Coffee")?.colorHex)
    }

    @Test
    fun theStoredColourIsNormalisedBeforeItReachesTheColumn() = runTest {
        // Lower case and no sigil on one, an alpha with no meaning in storage on the other.
        // Both normalise to the single shape the cloud document and the backup file expect,
        // so a round trip through either is lossless.
        categoryRepository.createCustomCategory(
            name = "Lower", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "5b2eed"
        )
        categoryRepository.createCustomCategory(
            name = "Alpha", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "#80FF0000"
        )

        assertEquals("#5B2EED", category("Lower")?.colorHex)
        assertEquals("#FF0000", category("Alpha")?.colorHex)
    }

    @Test
    fun anUnreadableColourIsStoredAsNoColourRatherThanAsGarbage() = runTest {
        // The column should never hold something the renderer has to defend against. Anything
        // that cannot be read becomes "no override", which resolves from the palette.
        categoryRepository.createCustomCategory(
            name = "Broken", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "not a colour"
        )

        assertNull(category("Broken")?.colorHex)
    }

    @Test
    fun aPaymentMethodKeepsItsOwnColour() = runTest {
        paymentRepository.createCustomPaymentMethod(name = "Wallet", iconKey = "wallet", colorHex = "#0288D1")
        paymentRepository.createCustomPaymentMethod(name = "Coupons", iconKey = "receipt_long")

        assertEquals("#0288D1", paymentMethod("Wallet")?.colorHex)
        assertNull(paymentMethod("Coupons")?.colorHex)
    }

    // ── The recreate-after-delete path ─────────────────────────────────────────

    @Test
    fun recreatingADeletedCategory_takesTheNewColour_notTheDeletedOne() = runTest {
        categoryRepository.createCustomCategory(
            name = "Coffee", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "#5B2EED"
        )
        val id = category("Coffee")!!.id
        categoryRepository.deleteCustomCategory(id)

        // Same name and type, so this reactivates the row rather than inserting — and the
        // reactivation is what used to keep the old colour.
        categoryRepository.createCustomCategory(
            name = "Coffee", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "#DC2626"
        )

        assertEquals(
            "a recreated category must wear the colour just picked, not the deleted row's",
            "#DC2626",
            category("Coffee")?.colorHex
        )
    }

    @Test
    fun recreatingADeletedCategoryWithoutAColour_clearsTheOldOne() = runTest {
        // The other half of the same hazard: "no colour picked" has to mean no colour stored.
        // Inheriting the deleted row's colour here is just as wrong and far less obvious.
        categoryRepository.createCustomCategory(
            name = "Coffee", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "#5B2EED"
        )
        categoryRepository.deleteCustomCategory(category("Coffee")!!.id)

        categoryRepository.createCustomCategory(name = "Coffee", iconKey = "local_cafe", transactionTypeId = 2)

        assertNull(category("Coffee")?.colorHex)
    }

    @Test
    fun recreatingADeletedPaymentMethod_takesTheNewColour() = runTest {
        paymentRepository.createCustomPaymentMethod(name = "Wallet", iconKey = "wallet", colorHex = "#5B2EED")
        paymentRepository.deleteCustomPaymentMethod(paymentMethod("Wallet")!!.id)

        paymentRepository.createCustomPaymentMethod(name = "Wallet", iconKey = "wallet", colorHex = "#059669")

        assertEquals("#059669", paymentMethod("Wallet")?.colorHex)
    }

    // ── The mapper round trip ──────────────────────────────────────────────────

    @Test
    fun theColourSurvivesTheTripOutToTheDomainModelAndBack() = runTest {
        categoryRepository.createCustomCategory(
            name = "Coffee", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "#7C3AED"
        )
        paymentRepository.createCustomPaymentMethod(name = "Wallet", iconKey = "wallet", colorHex = "#0288D1")

        // Entity -> domain -> entity is the path the cloud push, the cloud pull and the backup
        // all take, so a field dropped by either mapper would surface as a colour that
        // survives until the next sync and then vanishes.
        val categoryRow = category("Coffee")!!
        assertEquals("#7C3AED", categoryRow.toDomain().toEntity().colorHex)

        val paymentRow = paymentMethod("Wallet")!!
        assertEquals("#0288D1", paymentRow.toDomain().toEntity().colorHex)
    }

    @Test
    fun theFlowTheScreensRead_carriesTheColourThrough() = runTest {
        categoryRepository.createCustomCategory(
            name = "Coffee", iconKey = "local_cafe", transactionTypeId = 2, colorHex = "#7C3AED"
        )
        paymentRepository.createCustomPaymentMethod(name = "Wallet", iconKey = "wallet", colorHex = "#0288D1")

        // The read path the resolver actually uses. What these Flows carry is what a glyph is
        // drawn with, so this is the assertion that ties storage to rendering — the step
        // between them that could still be lossy even with a correct column.
        assertEquals(
            "#7C3AED",
            categoryRepository.observeActiveCategories().first().first { it.name == "Coffee" }.colorHex
        )
        assertEquals(
            "#0288D1",
            paymentRepository.observeActivePaymentMethods().first().first { it.name == "Wallet" }.colorHex
        )
    }
}
