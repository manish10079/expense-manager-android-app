package com.mknlabs.expensetracker.feature.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Temporary gate for the in-app language picker. Seven rapid taps on the
 * navbar Settings icon unlocks it for the rest of the process.
 */
object LanguageOptionUnlock {
    private const val REQUIRED_TAPS = 7
    private const val WINDOW_MS = 2_000L

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    private var tapCount = 0
    private var windowStartElapsedMs = 0L

    fun onSettingsIconTapped(nowElapsedMs: Long = android.os.SystemClock.elapsedRealtime()) {
        if (_unlocked.value) {
            return
        }
        if (tapCount == 0 || nowElapsedMs - windowStartElapsedMs > WINDOW_MS) {
            tapCount = 1
            windowStartElapsedMs = nowElapsedMs
            return
        }
        tapCount++
        if (tapCount >= REQUIRED_TAPS) {
            _unlocked.value = true
        }
    }
}
