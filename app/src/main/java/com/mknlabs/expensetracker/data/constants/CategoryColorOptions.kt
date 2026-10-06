package com.mknlabs.expensetracker.data.constants

import androidx.compose.ui.graphics.Color

/**
 * Swatches the colour picker offers when creating or recolouring a category.
 *
 * Replaces the old deduped seed-palette list with the Lucide category colour set
 * (icon ink hexes). Light and dark share the same tones; [adaptForContrast]
 * still lifts a pick that would fail on the other theme's card.
 */
private val lucideCategorySwatches: List<Color> = listOf(
    Color(0xFFEB7171),
    Color(0xFF818CF8),
    Color(0xFFFB923C),
    Color(0xFF38BDF8),
    Color(0xFFFBBF24),
    Color(0xFFFACC15),
    Color(0xFFFB7185),
    Color(0xFFC084FC),
    Color(0xFF60A5FA),
    Color(0xFF34D399),
    Color(0xFF2DD4BF),
    Color(0xFFA78BFA),
    Color(0xFF4ADE80),
    Color(0xFFF472B6),
    Color(0xFFE07A5F),
    Color(0xFFD97706),
    Color(0xFFA3E635),
    Color(0xFFE11D48),
    Color(0xFFD4A373),
    Color(0xFFE879F9),
    Color(0xFF2B9348),
    Color(0xFF00B4D8),
    Color(0xFF6366F1),
    Color(0xFF9D4EDD),
    Color(0xFF52B788),
    Color(0xFF3A86FF),
    Color(0xFF7209B7),
    Color(0xFF10B981),
    Color(0xFF06B6D4),
    Color(0xFF14B8A6),
    Color(0xFF8B5CF6),
    Color(0xFFCBD5E1),
)

val categoryColorOptionsLight: List<Color> = lucideCategorySwatches
val categoryColorOptionsDark: List<Color> = lucideCategorySwatches
