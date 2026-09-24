package com.mknlabs.expensetracker.core.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Opens the Pro paywall, wherever an upsell appears in the tree.
 *
 * Every "Upgrade to Pro" affordance lives deep inside feature composables (`GatedAction`,
 * a settings row, a transaction editor) that have no navigation dependency and, in the
 * case of the shared `GatedAction`, cannot be given one without a parameter change at all
 * 34 of its call sites. Providing the destination once, above the navigation host, keeps
 * those screens unaware of routing: they call [LocalUpgradeToPro] and the app decides.
 *
 * The default is intentionally a no-op. Compose Previews and any future screen rendered
 * outside the authenticated shell have no paywall to open, and a no-op is the only
 * behaviour that cannot crash them. The real provider is installed by `MainScreen`,
 * immediately above the navigation host, so every in-app call site resolves to a real
 * route.
 *
 * This is read-only navigation state with no dependencies, so `staticCompositionLocalOf`
 * is correct: it never changes at runtime, and a stale read is impossible.
 */
val LocalUpgradeToPro = staticCompositionLocalOf<() -> Unit> { {} }
