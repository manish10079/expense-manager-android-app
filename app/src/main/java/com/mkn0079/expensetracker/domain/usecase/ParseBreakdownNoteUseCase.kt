package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.models.CalculatorLineItem
import javax.inject.Inject

/**
 * Parses a breakdown note string back into a list of [CalculatorLineItem]s.
 * The note format is assumed to be "Description - Amount" per line.
 */
class ParseBreakdownNoteUseCase @Inject constructor() {
    operator fun invoke(note: String?): List<CalculatorLineItem> {
        if (note.isNullOrBlank()) return emptyList()
        var idCounter = 1
        return note.lines()
            .mapNotNull { line ->
                val lastDash = line.lastIndexOf(" - ")
                if (lastDash < 0) return@mapNotNull null
                
                val description = line.substring(0, lastDash).trim()
                // Strip any currency symbol / formatting; keep only digits and '.'
                val rawAmount = line.substring(lastDash + 3)
                    .filter { it.isDigit() || it == '.' }
                
                val amount = rawAmount.toDoubleOrNull() ?: return@mapNotNull null
                if (description.isBlank() || amount <= 0.0) return@mapNotNull null
                
                CalculatorLineItem(id = idCounter++, description = description, amount = amount)
            }
    }
}
