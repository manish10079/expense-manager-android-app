package com.mknlabs.expensetracker.utils

import java.util.Locale

/**
 * Converts a string to title case (e.g., "john doe" becomes "John Doe").
 */
fun String.toTitleCase(): String {
    if (this.isBlank()) return this
    
    return this.split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}
