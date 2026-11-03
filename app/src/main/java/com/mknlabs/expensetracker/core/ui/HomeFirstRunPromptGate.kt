package com.mknlabs.expensetracker.core.ui

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Session order after Home first appears:
 * 1) runtime permissions  2) emulator/root notice  3) share sheet  4) rate sheet
 */
object HomeFirstRunPromptGate {
    val permissionsComplete = MutableStateFlow(false)
    val integrityComplete = MutableStateFlow(false)
    val shareSheetVisible = MutableStateFlow(false)

    fun markPermissionsComplete() {
        permissionsComplete.value = true
    }

    fun markIntegrityComplete() {
        integrityComplete.value = true
    }

    fun setShareSheetVisible(visible: Boolean) {
        shareSheetVisible.value = visible
    }
}
