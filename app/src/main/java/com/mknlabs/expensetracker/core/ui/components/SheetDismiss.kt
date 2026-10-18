package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Slides [sheetState] down and only then runs [onDismissed], so a screen can take the sheet
 * out of composition without cutting its exit animation. Shared by every sheet that closes
 * on a choice, so they all leave the same way.
 */
@OptIn(ExperimentalMaterial3Api::class)
fun dismissSheetThen(
    scope: CoroutineScope,
    sheetState: SheetState,
    onDismissed: () -> Unit
) {
    scope.launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) onDismissed()
    }
}
