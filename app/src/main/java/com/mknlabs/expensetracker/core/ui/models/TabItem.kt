package com.mknlabs.expensetracker.core.ui.models

data class TabItem<T>(
    val id: T,
    val label: String,
    val isLocked: Boolean = false,
    val onLockedClick: () -> Unit = {}
)
