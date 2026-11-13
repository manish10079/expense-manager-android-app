package com.mknlabs.expensetracker.feature.settings.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PremiumUpgradeCardRenderTest {

    @get:Rule
    val compose = createComposeRule()

    private fun text(resId: Int): String {
        return InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)
    }

    @Test
    fun premiumUpgradeCard_rendersTitleSubtitleAndBadge_inLightMode() {
        var clicked = false
        compose.setContent {
            ExpenseTrackerTheme(darkTheme = false) {
                PremiumUpgradeCard(onClick = { clicked = true })
            }
        }

        compose.onNodeWithText(text(R.string.badge_exclusive)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.label_upgrade_to_pro)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.desc_upgrade_to_premium)).assertIsDisplayed()

        compose.onNodeWithText(text(R.string.label_upgrade_to_pro)).performClick()
        assertEquals(true, clicked)
    }

    @Test
    fun premiumUpgradeCard_rendersTitleSubtitleAndBadge_inDarkMode() {
        var clicked = false
        compose.setContent {
            ExpenseTrackerTheme(darkTheme = true) {
                PremiumUpgradeCard(onClick = { clicked = true })
            }
        }

        compose.onNodeWithText(text(R.string.badge_exclusive)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.label_upgrade_to_pro)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.desc_upgrade_to_premium)).assertIsDisplayed()

        compose.onNodeWithText(text(R.string.label_upgrade_to_pro)).performClick()
        assertEquals(true, clicked)
    }
}
