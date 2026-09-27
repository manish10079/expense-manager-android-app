package com.mknlabs.expensetracker.core.ui.models

import androidx.compose.ui.graphics.Color

data class TabItem<T>(
    val id: T,
    val label: String,
    val isLocked: Boolean = false,
    val onLockedClick: () -> Unit = {},
    /** Count overlay. Null or 0 draws nothing, so the label stays centred. */
    val badgeCount: Int? = null,
    /**
     * Optional semantic colour for this tab while it is selected: the pill takes a wash of it
     * and the label takes the colour itself.
     *
     * Null keeps `AnimatedTabSwitcher`'s own brand tokens, which is what every screen wants
     * except the Add/Edit screen's income/expense pair — those two read as their amount inks
     * (mint and coral) so the selected half matches the figure beneath it.
     */
    val selectedColor: Color? = null
)
