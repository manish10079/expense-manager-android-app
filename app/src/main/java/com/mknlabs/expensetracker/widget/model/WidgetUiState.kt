package com.mknlabs.expensetracker.widget.model

/**
 * Immutable UI state for the Expense Home Widget.
 *
 * Rendered by Glance composables. Contains all data needed for every widget state.
 * No mutableStateOf — all updates via copy().
 *
 * Future-ready placeholders for Phase 4.2–4.5:
 * - todaySpending
 * - recentTransactions
 * - budgetSummary
 */
internal data class WidgetUiState(
    /** Current widget state machine position. */
    val state: WidgetState = WidgetState.Idle,

    /** Today's total expense in minor units. */
    val todaySpendingMinor: Long = 0L,

    /** Currency symbol (e.g., "₹"). */
    val currencySymbol: String = "₹",

    /** The transcript from speech recognition. */
    val transcript: String = "",

    /** Error message resource ID if in error state. */
    val errorMessageResId: Int? = null,

    /** Whether the microphone permission has been granted. */
    val hasMicPermission: Boolean = false
)
