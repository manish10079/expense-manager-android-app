package com.mknlabs.expensetracker.models

import androidx.compose.ui.graphics.vector.ImageVector

enum class PinVisualMode {
    NORMAL,
    PRO_ANIMATED
}

sealed interface PinSlotState {
    object Empty : PinSlotState
    object Dot : PinSlotState
    data class AnimatedIcon(val icon: ImageVector, val triggerId: Long) : PinSlotState
}
