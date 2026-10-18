package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.theme.isDark

/**
 * A container that groups multiple settings items into a single card.
 *
 * This is the surface five of the settings screens are built out of, so it is also the
 * one place their group cards are styled: the shared card in light - white on the grey
 * field, outlined and lifted - and the tonal container every group has always been
 * drawn on in dark, whose 1dp tonal elevation was inert because the container was always
 * named explicitly.
 */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    colors: AppCardColors? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        // Callers that sit inside the settings redesign pass its card fill; every other
        // group keeps the tonal container it has always been drawn on.
        colors = colors ?: AppCardDefaults.colors(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/**
 * A thin divider to visually separate items within a [SettingsGroup].
 * Full-bleed across the group card, including under the leading icon.
 *
 * Light draws it at the divider colour's full strength, which is the specified #E8EBEF
 * line; the half-strength wash it used to be is what dark keeps.
 */
@Composable
fun SettingsGroupDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(MaterialTheme.colorScheme.background)
    )
}

/**
 * The group card as the settings screen draws it: the mockup's #151824 body with no
 * outline in dark, and the outlined white card in light. Screens that sit beside
 * Settings take this so their groups cannot drift off its card language.
 */
@Composable
fun settingsGroupCardColors(): AppCardColors {
    val scheme = MaterialTheme.colorScheme
    return if (scheme.isDark) {
        AppCardColors(
            containerColor = SettingsCardFillDark,
            contentColor = scheme.onSurface,
            border = null
        )
    } else {
        AppCardColors(
            containerColor = SettingsCardFillLight,
            contentColor = scheme.onSurface,
            border = BorderStroke(1.dp, scheme.outline)
        )
    }
}
