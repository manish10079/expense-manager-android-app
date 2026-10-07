package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.core.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.core.ui.theme.GlyphTileAlpha
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.data.constants.categoryIconOptions

/**
 * Shared icon grid for add-category, add-goal, and add-fund.
 *
 * Tiles follow the category picker: identity wash when idle, ring + glyph when chosen.
 */
@Composable
fun IconPickerGrid(
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    options: List<CategoryIconOption> = categoryIconOptions,
    identityColor: Color = MaterialTheme.colorScheme.accentInk,
    columns: Int = 6
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(options, key = { it.id }) { option ->
            IconPickerTile(
                option = option,
                selected = option.id == selectedId,
                identityColor = identityColor,
                onClick = { onSelect(option.id) }
            )
        }
    }
}

@Composable
fun IconPickerTile(
    option: CategoryIconOption,
    selected: Boolean,
    identityColor: Color,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = if (selected) 18.dp else 0.dp,
                shape = CircleShape,
                ambientColor = colorScheme.accentInk.copy(alpha = 0.34f),
                spotColor = colorScheme.secondary.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(
                if (selected) colorScheme.background else identityColor.copy(alpha = GlyphTileAlpha)
            )
            .then(
                if (selected) Modifier.border(1.dp, colorScheme.accentInk, CircleShape) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = stringResource(option.labelRes),
            tint = if (selected) colorScheme.accentInk else identityColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
