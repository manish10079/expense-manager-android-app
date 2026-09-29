package com.mknlabs.expensetracker.feature.settings.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Payments
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementItemUi
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Covers the grid's half of the colour contract: which colour each card is told to draw, against
 * which palette, and where a new colour is written.
 *
 * Three failures this guards against are all quiet ones. A card with the user's colour dropped looks
 * like an ordinary uncoloured row. A *payment* method looked up in the category palette is not an
 * error at all — it returns a colour, just the wrong one, because both sets of ids start at 1. And a
 * recolour that writes to the wrong repository succeeds while changing nothing on screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryManagementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ── What each card is told to draw ────────────────────────────────────────

    @Test
    fun `a user-created category carries the colour that was stored on it`() = runTest {
        val viewModel = viewModel(
            categories = listOf(
                category(id = 106, name = "Coffee", colorHex = "#D97706", isSystem = false)
            )
        )

        val item = viewModel.uiState.value.expenseItems.single { it.id == 106 }

        assertEquals("#D97706", item.colorHex)
        assertTrue(item.isUserCreated)
        assertFalse(item.isPaymentMethod)
    }

    @Test
    fun `a recoloured seeded category carries its colour too`() = runTest {
        // The whole point of phase 7. A seeded row's colour lives on the row, and the grid reads the
        // rows — the seed constant it used to read for built-ins is keyed by id and could never have
        // carried a value, so a recoloured built-in would have shown as uncoloured here.
        val repository = StubCategoryRepository(
            listOf(category(id = 1, name = "Food", colorHex = "#9333EA", isSystem = true))
        )
        val viewModel = viewModel(categories = repository.categories)

        val item = viewModel.uiState.value.expenseItems.single { it.id == 1 }

        assertEquals("#9333EA", item.colorHex)
        assertFalse(item.isUserCreated)
    }

    @Test
    fun `a seeded category with no colour of its own passes none on`() = runTest {
        // Null is the ordinary answer, not an absence: this is how a seeded category resolves from
        // the palette, and inventing a colour here would stop it following the theme.
        val viewModel = viewModel(categories = listOf(category(id = 1, name = "Food", isSystem = true)))

        val item = viewModel.uiState.value.expenseItems.single()

        assertNull(item.colorHex)
        assertFalse(item.isUserCreated)
    }

    @Test
    fun `payment methods are flagged as payments`() = runTest {
        // Payment ids restart at 1, so id 1 is both Food and UPI. Without the flag the card would
        // resolve UPI against the category palette and draw Food's purple — and, worse, a recolour
        // would be written to the categories table.
        val viewModel = viewModel(
            paymentMethods = listOf(
                PaymentType(id = 7, name = "Wallet", iconKey = "payments", colorHex = "#0288D1")
            )
        )

        val item = viewModel.uiState.value.paymentItems.single { it.id == 7 }

        assertEquals("#0288D1", item.colorHex)
        assertTrue(item.isPaymentMethod)
        assertTrue(viewModel.uiState.value.paymentItems.all { it.isPaymentMethod })
        assertTrue(viewModel.uiState.value.expenseItems.all { !it.isPaymentMethod })
        assertTrue(viewModel.uiState.value.incomeItems.all { !it.isPaymentMethod })
    }

    @Test
    fun `a category keeps its colour on the tab it was created under`() = runTest {
        val viewModel = viewModel(
            categories = listOf(
                category(id = 106, name = "Coffee", transactionTypeId = EXPENSE, colorHex = "#D97706"),
                category(id = 107, name = "Bonus", transactionTypeId = INCOME, colorHex = "#059669")
            )
        )

        assertEquals("#D97706", viewModel.uiState.value.expenseItems.single { it.id == 106 }.colorHex)
        assertEquals("#059669", viewModel.uiState.value.incomeItems.single { it.id == 107 }.colorHex)
    }

    @Test
    fun `switching tabs does not lose a colour`() = runTest {
        // The tab switch rebuilds the lists from the cached rows; a rebuild that dropped the colour
        // would show up only as a card that looks uncoloured until the next visit.
        val viewModel = viewModel(
            categories = listOf(category(id = 106, name = "Coffee", colorHex = "#D97706"))
        )

        viewModel.selectTab(CategoryManagementTab.Payment)
        viewModel.selectTab(CategoryManagementTab.Expense)

        assertEquals("#D97706", viewModel.uiState.value.expenseItems.single { it.id == 106 }.colorHex)
    }

    @Test
    fun `a newly created category leads its tab`() = runTest {
        val viewModel = viewModel(
            categories = listOf(
                category(id = 106, name = "Older"),
                category(id = 107, name = "Newer", colorHex = "#2563EB")
            )
        )

        val first = viewModel.uiState.value.expenseItems.first()

        assertEquals(107, first.id)
        assertEquals("#2563EB", first.colorHex)
    }

    @Test
    fun `the seeded rows follow the user's own rather than leading`() = runTest {
        val viewModel = viewModel(
            categories = listOf(
                category(id = 1, name = "Food", isSystem = true),
                category(id = 106, name = "Coffee"),
                category(id = 2, name = "Travel", isSystem = true)
            )
        )

        assertEquals(listOf(106, 1, 2), viewModel.uiState.value.expenseItems.map { it.id })
    }

    // ── Where a new colour is written ─────────────────────────────────────────

    @Test
    fun `recolouring a category writes to the category repository`() = runTest {
        val categories = StubCategoryRepository(
            listOf(category(id = 1, name = "Food", isSystem = true))
        )
        val payments = StubPaymentMethodRepository(emptyList())
        val viewModel = CategoryManagementViewModel(categories, payments)

        viewModel.updateColor(ItemFixtures.seededCategory(id = 1), "#9333EA")

        assertEquals(listOf(1 to "#9333EA"), categories.colorUpdates)
        assertEquals(0, payments.colorUpdates.size)
    }

    @Test
    fun `recolouring a payment method writes to the payment repository`() = runTest {
        // The id-1 collision again, one layer down: this is the assertion that catches a recolour
        // landing in the categories table because the domain was inferred from the id.
        val categories = StubCategoryRepository(emptyList())
        val payments = StubPaymentMethodRepository(
            listOf(PaymentType(id = 1, name = "UPI", iconKey = "payments"))
        )
        val viewModel = CategoryManagementViewModel(categories, payments)

        viewModel.updateColor(ItemFixtures.paymentMethod(id = 1), "#0288D1")

        assertEquals(listOf(1 to "#0288D1"), payments.colorUpdates)
        assertEquals(0, categories.colorUpdates.size)
    }

    @Test
    fun `clearing a colour sends null rather than an empty string`() = runTest {
        // The default swatch. Null is what returns a row to the palette, and "" is a value no reader
        // would know what to do with — it would have to be treated as null at every call site.
        val categories = StubCategoryRepository(
            listOf(category(id = 1, name = "Food", colorHex = "#9333EA", isSystem = true))
        )
        val viewModel = CategoryManagementViewModel(categories, StubPaymentMethodRepository(emptyList()))

        viewModel.updateColor(ItemFixtures.seededCategory(id = 1), null)

        assertEquals(listOf<Pair<Int, String?>>(1 to null), categories.colorUpdates)
    }

    @Test
    fun `a stored colour reaches the grid through the observed row`() = runTest {
        // The recolour is not echoed into the UI state; the grid re-renders from the flow. That is
        // the property being checked: a second source of truth could disagree with the row and leave
        // the card showing a colour that was never stored.
        val categories = StubCategoryRepository(
            listOf(category(id = 1, name = "Food", isSystem = true))
        )
        val viewModel = CategoryManagementViewModel(categories, StubPaymentMethodRepository(emptyList()))

        assertNull(viewModel.uiState.value.expenseItems.single().colorHex)

        categories.publish(listOf(category(id = 1, name = "Food", colorHex = "#9333EA", isSystem = true)))

        assertEquals("#9333EA", viewModel.uiState.value.expenseItems.single().colorHex)
    }

    private fun viewModel(
        categories: List<CategoryType> = emptyList(),
        paymentMethods: List<PaymentType> = emptyList()
    ) = CategoryManagementViewModel(
        StubCategoryRepository(categories),
        StubPaymentMethodRepository(paymentMethods)
    )

    private companion object {
        const val EXPENSE = 2
        const val INCOME = 1
    }
}

/** The UI items a recolour is asked for, built the way the grid builds them. */
private object ItemFixtures {
    fun seededCategory(id: Int) = CategoryManagementItemUi(
        id = id,
        title = "Food",
        icon = Icons.Filled.Category,
        isUserCreated = false
    )

    fun paymentMethod(id: Int) = CategoryManagementItemUi(
        id = id,
        title = "UPI",
        icon = Icons.Filled.Payments,
        isUserCreated = false,
        isPaymentMethod = true
    )
}

private fun category(
    id: Int,
    name: String,
    transactionTypeId: Int = 2,
    colorHex: String? = null,
    isSystem: Boolean = false
) = CategoryType(
    id = id,
    name = name,
    iconKey = "shopping_cart",
    transactionTypeId = transactionTypeId,
    colorHex = colorHex,
    isSystem = isSystem
)

private class StubCategoryRepository(
    initial: List<CategoryType>
) : CategoryRepository {
    private val flow = MutableStateFlow(initial)

    val categories: List<CategoryType> get() = flow.value
    val colorUpdates = mutableListOf<Pair<Int, String?>>()

    fun publish(next: List<CategoryType>) {
        flow.value = next
    }

    override fun observeActiveCategories(): Flow<List<CategoryType>> = flow
    override fun observeAllCategories(): Flow<List<CategoryType>> = flow
    override fun observeActiveCustomCategories(): Flow<List<CategoryType>> = flow

    override suspend fun createCustomCategory(
        name: String,
        iconKey: String,
        transactionTypeId: Int,
        colorHex: String?
    ) = Unit

    override suspend fun updateCategoryColor(id: Int, colorHex: String?) {
        colorUpdates += id to colorHex
    }

    override suspend fun deleteCustomCategory(id: Int) = Unit

    override suspend fun getFrequentlyUsedCategories(
        transactionTypeId: Int,
        limit: Int,
        sinceMillis: Long
    ): List<CategoryType> = emptyList()
}

private class StubPaymentMethodRepository(
    initial: List<PaymentType>
) : PaymentMethodRepository {
    private val flow = MutableStateFlow(initial)

    val colorUpdates = mutableListOf<Pair<Int, String?>>()

    override fun observeActivePaymentMethods(): Flow<List<PaymentType>> = flow
    override fun observeAllPaymentMethods(): Flow<List<PaymentType>> = flow
    override fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>> = flow

    override suspend fun createCustomPaymentMethod(
        name: String,
        iconKey: String,
        colorHex: String?
    ) = Unit

    override suspend fun updatePaymentMethodColor(id: Int, colorHex: String?) {
        colorUpdates += id to colorHex
    }

    override suspend fun deleteCustomPaymentMethod(id: Int) = Unit
}
