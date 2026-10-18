package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.SortAscending
import com.adamglin.phosphoricons.regular.SortDescending
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.genderRowSelected
import com.mknlabs.expensetracker.core.ui.theme.profileSheet

enum class CatalogSort {
    Newest,
    Oldest,
    Az,
    Za
}

fun <T> List<T>.sortedByCatalog(
    sort: CatalogSort,
    createdAt: (T) -> Long,
    name: (T) -> String
): List<T> = when (sort) {
    CatalogSort.Newest -> sortedWith(
        compareByDescending<T> { createdAt(it) }.thenBy { name(it).lowercase() }
    )
    CatalogSort.Oldest -> sortedWith(
        compareBy<T> { createdAt(it) }.thenBy { name(it).lowercase() }
    )
    CatalogSort.Az -> sortedBy { name(it).lowercase() }
    CatalogSort.Za -> sortedByDescending { name(it).lowercase() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSortSheet(
    selected: CatalogSort,
    onSelect: (CatalogSort) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colorScheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // The gender picker's sheet: the edit profile card colour, with each option on the
        // same 24dp card and 14dp icon tile the gender rows use.
        containerColor = colorScheme.profileSheet,
        scrimColor = colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.title_sort),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(18.dp))

            CatalogSort.entries.forEachIndexed { index, option ->
                val chosen = option == selected
                val cardColor = if (chosen) colorScheme.genderRowSelected else colorScheme.genderRow
                val tileColor = if (chosen) {
                    colorScheme.secondary.copy(alpha = 0.18f)
                } else {
                    colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
                val ink = if (chosen) colorScheme.accentInk else colorScheme.onSurface

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .clickable {
                            onSelect(option)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tileColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (chosen) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = colorScheme.accentInk
                        )
                    }
                }

                if (index != CatalogSort.entries.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

private val CatalogSort.icon: ImageVector
    get() = when (this) {
        CatalogSort.Newest -> PhosphorIcons.Regular.Clock
        CatalogSort.Oldest -> PhosphorIcons.Regular.ClockCounterClockwise
        CatalogSort.Az -> PhosphorIcons.Regular.SortAscending
        CatalogSort.Za -> PhosphorIcons.Regular.SortDescending
    }

private val CatalogSort.labelRes: Int
    get() = when (this) {
        CatalogSort.Newest -> R.string.label_sort_newest
        CatalogSort.Oldest -> R.string.label_sort_oldest
        CatalogSort.Az -> R.string.label_sort_az
        CatalogSort.Za -> R.string.label_sort_za
    }
