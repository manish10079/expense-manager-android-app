package com.mknlabs.expensetracker.widget.ui

import androidx.compose.ui.graphics.Color

/** Light / dark tokens for the 2×2 month-summary widget (homescreen mockup). */
internal data class Widget2x2Colors(
    val card: Color,
    val border: Color,
    val muted: Color,
    val title: Color,
    val chip: Color,
    val chipBorder: Color,
    val income: Color,
    val expense: Color,
    val divider: Color,
    val brand: Color,
    val onBrand: Color,
    val badgeBg: Color,
    val badgeFg: Color
) {
    companion object {
        val Dark = Widget2x2Colors(
            card = Color(0xFF0F1420),
            border = Color(0xFF1E293B),
            muted = Color(0xFF94A3B8),
            title = Color(0xFFFFFFFF),
            chip = Color(0xFF181F30),
            chipBorder = Color(0xCC1E293B),
            income = Color(0xFF34D399),
            expense = Color(0xFFFB7185),
            divider = Color(0x991E293B),
            brand = Color(0xFF7C3AED),
            onBrand = Color(0xFFFFFFFF),
            badgeBg = Color(0x1A10B981),
            badgeFg = Color(0xFF34D399)
        )
        val Light = Widget2x2Colors(
            card = Color(0xF2FFFFFF),
            border = Color(0xFFE2E8F0),
            muted = Color(0xFF64748B),
            title = Color(0xFF0F172A),
            chip = Color(0xFFF1F5F9),
            chipBorder = Color(0xCCE2E8F0),
            income = Color(0xFF059669),
            expense = Color(0xFFF43F5E),
            divider = Color(0xFFE2E8F0),
            brand = Color(0xFF7C3AED),
            onBrand = Color(0xFFFFFFFF),
            badgeBg = Color(0x1A10B981),
            badgeFg = Color(0xFF059669)
        )
    }
}
