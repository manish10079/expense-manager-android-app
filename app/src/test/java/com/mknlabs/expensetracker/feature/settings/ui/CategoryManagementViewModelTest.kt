package com.mknlabs.expensetracker.feature.settings.ui

import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
import com.mknlabs.expensetracker.data.constants.categoryMap
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
 * Covers the screen's half of the colour contract: which colour each card is told to draw, and
 * against which palette.
 *
 * Neither failure this guards against is visible in a database assertion. A row with the user's
 * colour dropped looks like an ordinary uncoloured category, and a *payment* method looked up in
 * the category palette is not an error at all — it returns a colour, just the wrong one, because
 * both sets of ids start at 1. That is why `isPaymentMethod` is asserted rather than assumed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryManagementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `a user-created category carries the colour that was stored on it`() = runTest {
        val viewModel = viewModel(
            categories = listOf(
                category(id = 106, name = "Coffee", transactionTypeId = EXPENSE, colorHex = "#D97706")
            )
        )

        val item = viewModel.uiState.value.expenseItems.single { it.id == 106 }

        assertEquals("#D97706", item.colorHex)
        assertTrue(item.isUserCreated)
        assertFalse(item.isPaymentMethod)
    }

    @Test
    fun `a category with no colour of its own passes none on`() = runTest {
        // Null is the ordinary answer, not an absence: this is how every seeded category resolves
        // from the palette, and inventing a colour here would stop it following the theme.
        val viewModel = viewModel(
            categories = listOf(category(id = 106, name = "Coffee", transactionTypeId = EXPENSE))
        )

        assertNull(viewModel.uiState.value.expenseItems.single { it.id == 106 }.colorHex)
    }

    @Test
    fun `seeded categories never carry a colour`() = runTest {
        // The rows the initializer rewrites on every launch. If a colour ever reached one of these
        // cards it would mean the palette had leaked into storage.
        val viewModel = viewModel()

        val seeded = viewModel.uiState.value.expenseItems.filter { !it.isUserCreated }

        assertTrue(seeded.isNotEmpty())
        assertTrue(seeded.all { it.colorHex == null })
        assertTrue(viewModel.uiState.value.incomeItems.all { it.colorHex == null })
    }

    @Test
    fun `payment methods are flagged as payments`() = runTest {
        // Payment ids restart at 1, so id 1 is both Food and UPI. Without the flag the card would
        // resolve UPI against the category palette and draw Food's purple — a colour, merely the
        // wrong one, which is exactly the kind of bug a screenshot review misses.
        val viewModel = viewModel(
            paymentMethods = listOf(
                PaymentType(id = 7, name = "Wallet", iconKey = "payments", colorHex = "#0288D1")
            )
        )

        val item = viewModel.uiState.value.paymentItems.single { it.id == 7 }

        assertEquals("#0288D1", item.colorHex)
        assertTrue(item.isPaymentMethod)
        // The payment list holds payments only, so nothing on it may be resolved as a category.
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
        // The tab switch rebuilds the lists from the cached rows; a rebuild that dropped the
        // colour would show up only as a card that looks uncoloured until the next visit.
        val viewModel = viewModel(
            categories = listOf(
                category(id = 106, name = "Coffee", transactionTypeId = EXPENSE, colorHex = "#D97706")
            )
        )

        viewModel.selectTab(CategoryManagementTab.Payment)
        viewModel.selectTab(CategoryManagementTab.Expense)

        assertEquals("#D97706", viewModel.uiState.value.expenseItems.single { it.id == 106 }.colorHex)
    }

    @Test
    fun `a newly created category leads its tab`() = runTest {
        val viewModel = viewModel(
            categories = listOf(
                category(id = 106, name = "Older", transactionTypeId = EXPENSE),
                category(id = 107, name = "Newer", transactionTypeId = EXPENSE, colorHex = "#2563EB")
            )
        )

        val first = viewModel.uiState.value.expenseItems.first()

        assertEquals(107, first.id)
        assertEquals("#2563EB", first.colorHex)
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

private fun category(
    id: Int,
    name: String,
    transactionTypeId: Int,
    colorHex: String? = null
) = CategoryType(
    id = id,
    name = name,
    iconKey = "shopping_cart",
    transactionTypeId = transactionTypeId,
    colorHex = colorHex,
    isSystem = false
)

private class StubCategoryRepository(
    categories: List<CategoryType>
) : CategoryRepository {
    private val flow = MutableStateFlow(categories)

    override fun observeActiveCategories(): Flow<List<CategoryType>> = flow
    override fun observeAllCategories(): Flow<List<CategoryType>> = flow
    override fun observeActiveCustomCategories(): Flow<List<CategoryType>> = flow

    override suspend fun createCustomCategory(
        name: String,
        iconKey: String,
        transactionTypeId: Int,
        colorHex: String?
    ) = Unit

    override suspend fun deleteCustomCategory(id: Int) = Unit

    override suspend fun getFrequentlyUsedCategories(
        transactionTypeId: Int,
        limit: Int,
        sinceMillis: Long
    ): List<CategoryType> = emptyList()
}

private class StubPaymentMethodRepository(
    paymentMethods: List<PaymentType>
) : PaymentMethodRepository {
    private val flow = MutableStateFlow(paymentMethods)

    override fun observeActivePaymentMethods(): Flow<List<PaymentType>> = flow
    override fun observeAllPaymentMethods(): Flow<List<PaymentType>> = flow
    override fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>> = flow

    override suspend fun createCustomPaymentMethod(
        name: String,
        iconKey: String,
        colorHex: String?
    ) = Unit

    override suspend fun deleteCustomPaymentMethod(id: Int) = Unit
}
