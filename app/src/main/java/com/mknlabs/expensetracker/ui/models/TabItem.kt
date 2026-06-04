package com.mknlabs.expensetracker.ui.models

data class TabItem<T>(
    val id: T,
    val label: String,
    val isLocked: Boolean = false,
    val onLockedClick: () -> Unit = {}
)
