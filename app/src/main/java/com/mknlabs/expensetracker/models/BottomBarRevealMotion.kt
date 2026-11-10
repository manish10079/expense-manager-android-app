package com.mknlabs.expensetracker.models

enum class BottomBarRevealMotion {
    SlideUp,
    ExpandFromCenter,
}

fun bottomBarRevealMotionOrDefault(value: String?): BottomBarRevealMotion {
    return BottomBarRevealMotion.entries.firstOrNull { it.name == value }
        ?: BottomBarRevealMotion.ExpandFromCenter
}
