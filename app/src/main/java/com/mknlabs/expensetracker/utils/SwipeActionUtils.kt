package com.mknlabs.expensetracker.utils

/**
 * The action a horizontal swipe on a transaction card triggers.
 *
 * Swiping left (finger moving left, i.e. right-to-left) replicates the
 * transaction; swiping right (finger moving right, i.e. left-to-right) deletes
 * it (soft-delete with an Undo snackbar).
 */
enum class TransactionSwipeAction {
    Duplicate,
    Delete
}

/**
 * Maps a horizontal swipe direction to its action.
 *
 * @param isLeftSwipe true when the finger moved left (negative drag — the
 *   `onSwipeLeft` callback of [com.mknlabs.expensetracker.ui.horizontalSwipe]).
 */
fun transactionSwipeAction(isLeftSwipe: Boolean): TransactionSwipeAction =
    if (isLeftSwipe) TransactionSwipeAction.Duplicate else TransactionSwipeAction.Delete
