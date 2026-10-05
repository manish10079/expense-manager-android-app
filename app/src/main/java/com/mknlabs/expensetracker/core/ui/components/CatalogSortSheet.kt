package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.sheet

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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 28.dp)
        ) {
            Text(
                text = stringResource(R.string.title_sort),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            CatalogSort.entries.forEach { option ->
                val chosen = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(option)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(option.labelRes),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (chosen) {
                            MaterialTheme.colorScheme.accentInk
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f)
                    )
                    if (chosen) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.accentInk
                        )
                    }
                }
            }
        }
    }
}

private val CatalogSort.labelRes: Int
    get() = when (this) {
        CatalogSort.Newest -> R.string.label_sort_newest
        CatalogSort.Oldest -> R.string.label_sort_oldest
        CatalogSort.Az -> R.string.label_sort_az
        CatalogSort.Za -> R.string.label_sort_za
    }
