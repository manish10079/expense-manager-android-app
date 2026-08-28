package com.mknlabs.expensetracker.widget.model

/**
 * State machine for the Expense Home Widget.
 *
 * The widget renders entirely from this state — no mutable UI logic.
 *
 * ```text
 * Idle → Listening → Processing → Preview → Idle (after save/cancel)
 *                  ↓                ↓
 *                Error            Error
 * ```
 */
internal sealed class WidgetState {

    /** Idle state: mic icon displayed, awaiting tap. */
    data object Idle : WidgetState()

    /** Listening state: voice recording in progress. */
    data object Listening : WidgetState()

    /** Processing state: AI is analyzing the speech transcript. */
    data object Processing : WidgetState()

    /** Preview state: parsed transaction displayed, awaiting user confirmation. */
    data class Preview(val parsedTransaction: WidgetParsedTransaction) : WidgetState()

    /** Saving state: transaction is being written to Room. */
    data object Saving : WidgetState()

    /** Error state: something went wrong, with a user-friendly message. */
    data class Error(val errorMessageResId: Int) : WidgetState()
}
