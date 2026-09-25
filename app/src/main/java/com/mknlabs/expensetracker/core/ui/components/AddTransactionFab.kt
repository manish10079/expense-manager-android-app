package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.fabGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import kotlinx.coroutines.delay

import androidx.compose.ui.graphics.Brush
import com.mknlabs.expensetracker.core.ui.theme.isDark

/**
 * Diameter of the add-transaction FAB. Exposed so the shell that docks it (see
 * [AppBottomBar]) can centre it on an edge without duplicating the number.
 */
val AddTransactionFabSize = 56.dp

/**
 * How far the ambient glow reaches beyond the button on every side.
 *
 * Exported for the same reason as the diameter: the composed FAB is
 * [AddTransactionFabSize] + 2 × this tall with the button centred inside it, so a
 * dock that aligns *this* wrapper to a surface puts the circle half an inset away
 * from the edge it meant to straddle. Whoever anchors the FAB has to add it back.
 */
val AddTransactionFabGlowInset = 14.dp

/**
 * Shared visibility controller for the standalone add-transaction FAB rendered
 * by [MainScaffold] — docked into the centre of the floating bottom navigation bar.
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
 * Standard circular "Add transaction" FAB ([AddTransactionFabSize]).
 *
 * @param onClick Navigates to the Add Transaction screen.
 * @param modifier Applied to the [AnimatedVisibility] wrapper. Callers own the
 *   placement — [AppBottomBar] centres it on the capsule's top edge.
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
        enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)) +
            scaleIn(
                initialScale = 0f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            ),
        exit = fadeOut(animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)) +
            scaleOut(
                targetScale = 0f,
                transformOrigin = TransformOrigin.Center,
                animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)
            ),
        label = "add_transaction_fab_visibility"
    ) {
        val fabBrush = fabGradient()
        val isDark = MaterialTheme.colorScheme.isDark

        Box(
            contentAlignment = Alignment.Center
        ) {
            // Ambient purple glow / shadow focused on the bottom section of the FAB,
            // illuminating the frosted glass blur below it
            Box(
                modifier = Modifier
                    .size(AddTransactionFabSize + AddTransactionFabGlowInset * 2)
                    .offset(y = 8.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (isDark) Color(0xFF8B5CF6) else Color(0xFF7C4DFF)).copy(alpha = if (isDark) 0.55f else 0.35f),
                                (if (isDark) Color(0xFF6D28D9) else Color(0xFF6C52EE)).copy(alpha = if (isDark) 0.22f else 0.12f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            FloatingActionButton(
                onClick = onClick,
                shape = CircleShape,
                // The fill is painted by the gradient Box inside, so the container
                // itself must stay unpainted — otherwise a flat purple slab would sit
                // behind the gradient and flatten it back out.
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBrandGradient,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier
                    .size(AddTransactionFabSize)
                    // Brand-tinted glow focused toward the bottom section
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        ambientColor = if (isDark) Color(0xFF8B5CF6).copy(alpha = 0.55f) else Color(0xFF7C4DFF).copy(alpha = 0.40f),
                        spotColor = if (isDark) Color(0xFF7C3AED).copy(alpha = 0.60f) else Color(0xFF6D28D9).copy(alpha = 0.45f)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = fabBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.desc_add_transaction),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
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
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    val scrollVisibility = LocalAddFabVisibility.current.value
    AddTransactionFab(
        onClick = onClick,
        modifier = modifier,
        visible = visible && scrollVisibility
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
                    .align(Alignment.BottomCenter)
                    .offset(y = -(AddTransactionFabSize / 2))
            )
        }
    }
}
