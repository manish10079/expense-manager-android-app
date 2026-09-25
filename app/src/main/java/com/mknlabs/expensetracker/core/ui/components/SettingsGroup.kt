package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        colors = AppCardDefaults.colors(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/**
 * A thin divider to visually separate items within a [SettingsGroup].
 * Uses 72.dp start inset to align directly beneath the text block without crossing the icon.
 *
 * Light draws it at the divider colour's full strength, which is the specified #E8EBEF
 * line; the half-strength wash it used to be is what dark keeps.
 */
@Composable
fun SettingsGroupDivider(
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    HorizontalDivider(
        modifier = modifier.padding(start = 72.dp, end = 16.dp),
        thickness = 1.dp,
        color = if (colorScheme.isDark) {
            colorScheme.outlineVariant.copy(alpha = 0.5f)
        } else {
            colorScheme.outline
        }
    )
}

