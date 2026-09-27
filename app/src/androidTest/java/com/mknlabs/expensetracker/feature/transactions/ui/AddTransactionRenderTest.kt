package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Renders the Add/Edit transaction form with no Hilt container — the same entry point the
 * Compose preview uses. A composition that needs `hiltViewModel()` (or any other
 * platform-only collaborator) throws here with a stack trace, which is what makes this the
 * regression guard for the preview breaking.
 *
 * Method names are camelCase rather than backticked sentences on purpose: instrumented tests
 * are dexed and this app's DEX version rejects spaces in method names.
 */
@RunWith(AndroidJUnit4::class)
class AddTransactionRenderTest {

    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun contentRendersWithoutHiltContainer() {
        compose.setContent {
            ExpenseTrackerTheme {
                AddTransactionScreenContent()
            }
        }

        compose.waitForIdle()

        // The amount card is the top of the form; if composition survives to here, Hilt was
        // never reached.
        compose.onNodeWithText(context.getString(R.string.label_enter_amount))
            .assertIsDisplayed()
    }
}
