package com.mknlabs.expensetracker.core.ui.models

data class TabItem<T>(
    val id: T,
    val label: String,
    val isLocked: Boolean = false,
    val onLockedClick: () -> Unit = {},
    /** Count overlay. Null or 0 draws nothing, so the label stays centred. */
    val badgeCount: Int? = null
)
