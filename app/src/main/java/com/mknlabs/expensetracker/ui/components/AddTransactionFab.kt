package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme

/**
 * Shared visibility controller for the standalone add-transaction FAB rendered
 * by [MainScaffold] just above the floating bottom navigation bar.
 *
 * Each tab screen binds its primary list's scroll direction to this state via
 * [rememberBindAddFabToScroll], so the FAB auto-hides while the user scrolls
 * down (revealing more list items) and reappears on scroll up. The default
 * value keeps previews compiling without a provider.
 */
val LocalAddFabVisibility = staticCompositionLocalOf<MutableState<Boolean>> {
    mutableStateOf(true)
}

/**
 * Standard 56.dp circular "Add transaction" FAB. Designed to float above
 * the top-right corner of the floating bottom navigation bar (see [MainScaffold]).
 *
 * @param onClick Navigates to the Add Transaction screen.
 * @param modifier Applied to the [AnimatedVisibility] wrapper — callers align it
 *   with `Alignment.BottomEnd` and pad it `bottom = 108.dp, end = 16.dp`.
 * @param visible Drives the show/hide animation; screens flip it from the
 *   current list's scroll state via [LocalAddFabVisibility].
 */
@Composable
fun AddTransactionFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(220)) +
            slideInVertically(animationSpec = tween(220)) { it / 2 } +
            scaleIn(initialScale = 0.9f, animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(160)) +
            slideOutVertically(animationSpec = tween(160)) { it / 2 } +
            scaleOut(targetScale = 0.9f, animationSpec = tween(160)),
        label = "add_transaction_fab_visibility"
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            ),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.desc_add_transaction),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Renders [AddTransactionFab] reading its visibility straight from
 * [LocalAddFabVisibility]. Kept as its own composable so the visibility read is
 * scoped here instead of recomposing the entire [MainScaffold] on every
 * scroll-direction flip.
 */
@Composable
fun AddTransactionFabSlot(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibility = LocalAddFabVisibility.current
    AddTransactionFab(
        onClick = onClick,
        modifier = modifier,
        visible = visibility.value
    )
}

/**
 * Returns true while [listState] is actively scrolling (any direction), and
 * false when the scroll settles — the scroll-activity signal that drives the
 * FAB's auto-hide behavior.
 */
@Composable
fun rememberFabHiddenOnScroll(listState: LazyListState): Boolean {
    var hidden by remember { mutableStateOf(false) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { isScrolling ->
                if (isScrolling) {
                    hidden = true
                } else {
                    // Small delay so the FAB doesn't flicker on micro-janks
                    delay(200L)
                    hidden = false
                }
            }
    }
    return hidden
}

/**
 * Binds [listState]'s scroll activity to the shared [LocalAddFabVisibility]
 * state. Add one call in each tab screen whose content scrolls vertically so
 * the standalone FAB hides while scrolling and reappears when scroll stops.
 */
@Composable
fun rememberBindAddFabToScroll(listState: LazyListState) {
    val visibility = LocalAddFabVisibility.current
    val hidden = rememberFabHiddenOnScroll(listState)
    LaunchedEffect(hidden) {
        visibility.value = !hidden
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTransactionFabPreview() {
    ExpenseTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            AddTransactionFab(
                onClick = {},
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 108.dp, end = 16.dp)
            )
        }
    }
}
