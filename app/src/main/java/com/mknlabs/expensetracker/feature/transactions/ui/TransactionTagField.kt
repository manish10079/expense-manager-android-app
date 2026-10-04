package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.parseHexColorOrNull
import com.mknlabs.expensetracker.models.Tag

/**
 * The Tags row on the Add/Edit Transaction form: the chosen tags as removable chips,
 * and an inline field whose suggestions come from the existing tags.
 *
 * ## Why the suggestions live in a wrapping column and not a floating dropdown
 *
 * The form scrolls, and it can be shown in a two-pane layout where the tag row is not
 * near the top. A `DropdownMenu` anchors to its parent and would be clipped by the
 * scroll container the moment the row is scrolled — a real defect on a phone with the
 * keyboard up. Rendering the matches in flow directly beneath the field keeps them in
 * the same scroll context, so they are always reachable, and it reads the same on a
 * phone and a tablet. The interaction the PRD asks for — type, see matches, pick or
 * create — is unchanged.
 *
 * ## Creating versus reusing
 *
 * The caller passes [onAddByName] for the create/reuse path and [onToggle] for a known
 * tag. A typed name that matches an existing tag case-insensitively is offered as that
 * tag (reuse); a name that matches nothing is offered as "Create #…". Both end up in
 * the same repository call, so the two affordances cannot diverge in what they store.
 */
@Composable
fun TransactionTagField(
    allTags: List<Tag>,
    selectedTags: List<Tag>,
    onToggle: (String) -> Unit,
    onAddByName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val selectedIds = remember(selectedTags) { selectedTags.map { it.id }.toSet() }
    val trimmed = query.trim()

    // Suggestions: existing tags that match the typed text and are not already chosen.
    // Matched case-insensitively via `contains` on the lower-cased name, which is the
    // same rule storage enforces — so a suggestion can never offer a tag that the
    // create path would refuse as a duplicate.
    val suggestions = remember(allTags, selectedIds, trimmed) {
        if (trimmed.isEmpty()) {
            emptyList()
        } else {
            allTags
                .filter { it.id !in selectedIds }
                .filter { it.name.contains(trimmed, ignoreCase = true) }
                .take(6)
        }
    }

    // Only offered when nothing already matches: typing an existing tag should attach
    // that tag, never mint a second one.
    val canCreate = trimmed.isNotEmpty() && suggestions.none { it.name.equals(trimmed, ignoreCase = true) }

    // The field can sit low in a scrolling form, and the keyboard overlays the window
    // rather than resizing it. Whenever the suggestion panel opens it grows downward
    // from the field, so scroll the whole block (field, chips and matches) into view:
    // because the screen reserves the IME's height, "into view" lands it just above the
    // keyboard and the user can see what they are typing.
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(trimmed, suggestions) {
        if (suggestions.isNotEmpty() || canCreate) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = if (selectedTags.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.accentInk
                },
                modifier = Modifier.size(18.dp)
            )

            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.accentInk),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.placeholder_search_tags),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    inner()
                }
            )
        }

        if (selectedTags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onRemove = { onToggle(tag.id) }
                    )
                }
            }
        }

        if (suggestions.isNotEmpty() || canCreate) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                suggestions.forEach { tag ->
                    SuggestionRow(
                        label = tag.name,
                        leadingColor = parseHexColorOrNull(tag.colorHex),
                        onClick = {
                            onToggle(tag.id)
                            query = ""
                        }
                    )
                }
                if (canCreate) {
                    SuggestionRow(
                        label = stringResource(R.string.label_create_tag_named, trimmed),
                        leadingColor = null,
                        isCreate = true,
                        onClick = {
                            onAddByName(trimmed)
                            query = ""
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    label: String,
    leadingColor: Color?,
    isCreate: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isCreate) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.accentInk,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(leadingColor ?: MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCreate) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * One applied tag, as a chip with an "X".
 *
 * The chip carries the tag's colour as a soft wash with the text in the theme's ink,
 * rather than painting the text itself in the tag colour: an arbitrary user-chosen
 * colour is not guaranteed to have contrast against the surface, and the ledger is
 * dense enough that a row of low-contrast labels would be unreadable.
 */
@Composable
fun TagChip(
    tag: Tag,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val identity = parseHexColorOrNull(tag.colorHex) ?: MaterialTheme.colorScheme.accentInk

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = identity.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(
                start = 10.dp,
                end = if (onRemove != null) 4.dp else 10.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(identity)
            )
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 140.dp)
            )
            if (onRemove != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.desc_remove_tag, tag.name),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}