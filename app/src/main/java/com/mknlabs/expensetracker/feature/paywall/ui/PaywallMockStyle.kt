package com.mknlabs.expensetracker.feature.paywall.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

internal val PaywallBrandStart = Color(0xFF6A4DFF)
internal val PaywallBrandEnd = Color(0xFF6750A4)
internal val PaywallScreenDark = Color(0xFF000000)
internal val PaywallScreenLight = Color(0xFFF4F5FA)
internal val PaywallCardDark = Color(0xD9121826)
internal val PaywallCardDarkEnd = Color(0xF2121826)
internal val PaywallIndigoLight = Color(0xFF4F46E5)
internal val PaywallIndigoDark = Color(0xFFA5B4FC)
internal val PaywallEmerald = Color(0xFF10B981)
internal val PaywallSlate400 = Color(0xFF94A3B8)
internal val PaywallSlate500 = Color(0xFF64748B)
internal val PaywallSlate600 = Color(0xFF475569)
internal val PaywallSlate300 = Color(0xFFCBD5E1)
internal val PaywallBorderLight = Color(0xFFE2E8F0)
internal val PaywallOverlayCard = Color(0xFF12151C)

internal fun paywallBrandGradient(): Brush = Brush.linearGradient(
    colors = listOf(PaywallBrandStart, PaywallBrandEnd),
    start = Offset.Zero,
    end = Offset.Infinite,
)
