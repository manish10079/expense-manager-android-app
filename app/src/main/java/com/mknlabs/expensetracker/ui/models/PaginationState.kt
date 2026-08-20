package com.mknlabs.expensetracker.ui.models

import androidx.compose.runtime.Immutable

/**
 * Holds pagination metadata for a paginated list.
 *
 * @property currentPage Zero-based index of the most recently loaded page.
 * @property pageSize Number of items per page.
 * @property hasMore Whether more pages are available to load.
 * @property isLoading Whether a page load is currently in flight.
 * @property loadedCount Number of items currently loaded into memory.
 * @property totalCount Total number of items matching the current filter.
 */
@Immutable
data class PaginationState(
    val currentPage: Int = 0,
    val pageSize: Int = PAGE_SIZE_DEFAULT,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val loadedCount: Int = 0,
    val totalCount: Int = 0
) {
    companion object {
        /** Default page size — balances memory usage vs scroll smoothness. */
        const val PAGE_SIZE_DEFAULT = 50
    }
}
