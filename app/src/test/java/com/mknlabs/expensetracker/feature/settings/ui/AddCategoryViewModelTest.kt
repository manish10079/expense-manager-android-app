package com.mknlabs.expensetracker.feature.settings.ui

import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Covers the picker's half of the colour contract: what the screen chose reaches the repository,
 * and nothing leaks between tabs.
 *
 * The persistence tests already prove a colour handed to the repository is stored. What is left
 * is whether the screen hands over the right value — the failure that would not show up in any
 * database assertion, because a category with the wrong colour is a perfectly valid row.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddCategoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var categories: RecordingCategoryRepository
    private lateinit var paymentMethods: RecordingPaymentMethodRepository
    private lateinit var viewModel: AddCategoryViewModel

    @Before
    fun setup() {
        categories = RecordingCategoryRepository()
        paymentMethods = RecordingPaymentMethodRepository()
        viewModel = AddCategoryViewModel(categories, paymentMethods)
    }

    @Test
    fun `a created category carries the colour that was picked`() = runTest {
        viewModel.onNameChange("Coffee")
        viewModel.onColorSelected("#5B2EED")
        viewModel.saveCategory {}

        assertEquals(1, categories.created.size)
        assertEquals("#5B2EED", categories.created.single().colorHex)
    }

    @Test
    fun `a created category with no colour picked stores none`() = runTest {
        // The ordinary case. A default that invented a colour would stop a category following the
        // palette, and would do it silently.
        viewModel.onNameChange("Coffee")
        viewModel.saveCategory {}

        assertNull(categories.created.single().colorHex)
    }

    @Test
    fun `the default swatch clears a colour that was already picked`() = runTest {
        // The way back. Without it a user who tapped a swatch once could never undo it, because
        // every seeded category relies on "no colour of its own" being reachable.
        viewModel.onNameChange("Coffee")
        viewModel.onColorSelected("#5B2EED")
        viewModel.onColorSelected(null)
        viewModel.saveCategory {}

        assertNull(categories.created.single().colorHex)
    }

    @Test
    fun `a payment method carries its colour too`() = runTest {
        viewModel.setTargetTab(CategoryManagementTab.Payment)
        viewModel.onNameChange("Wallet")
        viewModel.onColorSelected("#0288D1")
        viewModel.saveCategory {}

        assertEquals("#0288D1", paymentMethods.created.single().colorHex)
        assertEquals(0, categories.created.size)
    }

    @Test
    fun `switching the target tab clears the picked colour`() = runTest {
        // A colour chosen for an expense must not arrive on an income category the user moved to.
        // The tab switch rebuilds the state, which is what makes that true rather than merely
        // likely.
        viewModel.onNameChange("Coffee")
        viewModel.onColorSelected("#5B2EED")

        viewModel.setTargetTab(CategoryManagementTab.Income)
        viewModel.onNameChange("Bonus")
        viewModel.saveCategory {}

        assertNull(categories.created.single().colorHex)
        assertEquals(1, categories.created.single().transactionTypeId)
    }

    @Test
    fun `resetting the form clears the picked colour`() = runTest {
        // Reset runs on every entry to the screen, so a colour left from a previous visit would
        // reappear on a category the user never gave one.
        viewModel.onColorSelected("#5B2EED")
        viewModel.resetState()

        assertNull(viewModel.uiState.value.selectedColorHex)
    }

    @Test
    fun `a blank name saves nothing`() = runTest {
        viewModel.onNameChange("   ")
        viewModel.onColorSelected("#5B2EED")
        viewModel.saveCategory {}

        assertEquals(0, categories.created.size)
    }
}

private data class CreatedCategory(
    val name: String,
    val iconKey: String,
    val transactionTypeId: Int,
    val colorHex: String?
)

private data class CreatedPaymentMethod(
    val name: String,
    val iconKey: String,
    val colorHex: String?
)

private class RecordingCategoryRepository : CategoryRepository {
    val created = mutableListOf<CreatedCategory>()

    override fun observeActiveCategories(): Flow<List<CategoryType>> = flowOf(emptyList())
    override fun observeAllCategories(): Flow<List<CategoryType>> = flowOf(emptyList())
    override fun observeActiveCustomCategories(): Flow<List<CategoryType>> = flowOf(emptyList())

    override suspend fun createCustomCategory(
        name: String,
        iconKey: String,
        transactionTypeId: Int,
        colorHex: String?
    ) {
        created += CreatedCategory(name, iconKey, transactionTypeId, colorHex)
    }

    override suspend fun updateCategoryColor(id: Int, colorHex: String?) = Unit

    override suspend fun deleteCustomCategory(id: Int) = Unit

    override suspend fun getFrequentlyUsedCategories(
        transactionTypeId: Int,
        limit: Int,
        sinceMillis: Long
    ): List<CategoryType> = emptyList()
}

private class RecordingPaymentMethodRepository : PaymentMethodRepository {
    val created = mutableListOf<CreatedPaymentMethod>()

    override fun observeActivePaymentMethods(): Flow<List<PaymentType>> = flowOf(emptyList())
    override fun observeAllPaymentMethods(): Flow<List<PaymentType>> = flowOf(emptyList())
    override fun observeActiveCustomPaymentMethods(): Flow<List<PaymentType>> = flowOf(emptyList())

    override suspend fun createCustomPaymentMethod(
        name: String,
        iconKey: String,
        colorHex: String?
    ) {
        created += CreatedPaymentMethod(name, iconKey, colorHex)
    }

    override suspend fun updatePaymentMethodColor(id: Int, colorHex: String?) = Unit

    override suspend fun deleteCustomPaymentMethod(id: Int) = Unit
}
