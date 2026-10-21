package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Globe
import com.adamglin.phosphoricons.regular.MagnifyingGlass

import com.adamglin.phosphoricons.regular.X
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.data.constants.AppLanguageOption
import com.mknlabs.expensetracker.data.constants.SYSTEM_LANGUAGE_CODE
import com.mknlabs.expensetracker.data.constants.supportedAppLanguages

/**
 * Onboarding language picker. Light/dark chrome follows the language-selection
 * mockups; options come from [supportedAppLanguages].
 */
@Composable
fun OnboardingLanguagePage(
    selectedCode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dark = MaterialTheme.colorScheme.isDark
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) {
            supportedAppLanguages
        } else {
            supportedAppLanguages.filter { option ->
                option.nativeName.contains(q, ignoreCase = true) ||
                    option.englishName.contains(q, ignoreCase = true) ||
                    option.code.contains(q, ignoreCase = true)
            }
        }
    }
    val selectedLabel = supportedAppLanguages
        .firstOrNull { it.code == selectedCode }
        ?.nativeName
        ?: stringResource(R.string.label_language_system)

    val banner = if (dark) Color(0xFF1A1B21) else Color.White
    val searchFill = if (dark) Color(0xFF1E1F25) else Color.White
    val rowIdle = if (dark) Color(0xFF1A1B21) else Color.White
    val rowSelected = if (dark) Color(0xFF1E1F25) else Color.White
    val borderIdle = if (dark) Color.Transparent else Color(0xFFE2E8F0)
    val glyphCircle = if (dark) Color(0xFF282A2F) else Color(0xFFF1F5F9)

    val ink = MaterialTheme.colorScheme.accentInk
    Column(modifier = modifier.fillMaxSize()) {
        LanguageOverviewBanner(
            localeCount = supportedAppLanguages.size,
            container = banner,
            ink = ink
        )
        Spacer(Modifier.height(12.dp))
        LanguageSearchField(
            query = query,
            onQueryChange = { query = it },
            fill = searchFill,
            outlined = !dark
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (dark) {
                    stringResource(R.string.label_available_dialects)
                } else {
                    stringResource(R.string.label_suggested_installed)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            )
            if (dark) {
                Text(
                    text = selectedLabel,
                    color = ink,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.label_languages_available,
                        supportedAppLanguages.size
                    ),
                    color = ink,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ink.copy(alpha = 0.12f))
                        .border(1.dp, ink.copy(alpha = 0.28f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (filtered.isEmpty()) {
                item {
                    LanguageEmptySearch(query = query, onReset = { query = "" })
                }
            } else {
                items(filtered, key = { it.code }) { option ->
                    val selected = option.code == selectedCode
                    LanguageOptionRow(
                        option = option,
                        selected = selected,
                        dark = dark,
                        ink = ink,
                        container = if (selected) rowSelected else rowIdle,
                        border = borderIdle,
                        glyphCircle = glyphCircle,
                        onClick = { onSelect(option.code) }
                    )
                    Spacer(Modifier.height(if (dark) 4.dp else 10.dp))
                }
            }
        }
    }
}

@Composable
private fun LanguageOverviewBanner(
    localeCount: Int,
    container: Color,
    ink: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF282A2F)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Globe,
                contentDescription = null,
                tint = ink,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.label_app_language),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = stringResource(R.string.desc_language_changes_ledger),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = stringResource(R.string.label_locales_count, localeCount),
            color = ink,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF33353A))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun LanguageSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    fill: Color,
    outlined: Boolean
) {
    val shape = RoundedCornerShape(if (outlined) 16.dp else 12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(fill)
            .then(
                if (outlined) {
                    Modifier.border(1.dp, Color(0xFFE2E8F0), shape)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.accentInk),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.label_search_language),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                inner()
            }
        )
        if (query.isNotEmpty()) {
            Icon(
                imageVector = PhosphorIcons.Regular.X,
                contentDescription = stringResource(R.string.label_clear),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onQueryChange("") }
            )
        }
    }
}

@Composable
private fun LanguageOptionRow(
    option: AppLanguageOption,
    selected: Boolean,
    dark: Boolean,
    ink: Color,
    container: Color,
    border: Color,
    glyphCircle: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(if (dark) 12.dp else 16.dp)
    val onPrimary = Color.White
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .then(
                if (!dark) Modifier.border(1.dp, border, shape) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (dark) 40.dp else 44.dp)
                .clip(CircleShape)
                .background(
                    if (option.code == SYSTEM_LANGUAGE_CODE && !dark) {
                        Color(0xFFF5F3FF)
                    } else {
                        glyphCircle
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (option.code == SYSTEM_LANGUAGE_CODE) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Globe,
                    contentDescription = null,
                    tint = ink,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = option.code.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = option.nativeName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (option.code == SYSTEM_LANGUAGE_CODE) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_auto),
                        color = ink,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ink.copy(alpha = 0.16f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = option.englishName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) ink else if (dark) Color(0xFF33353A) else Color.Transparent)
                .then(
                    if (!selected && !dark) {
                        Modifier.border(2.dp, Color(0xFFCBD5E1), CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Check,
                    contentDescription = null,
                    tint = onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageEmptySearch(
    query: String,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.label_no_languages_found),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.msg_no_languages_match, query),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        TextButton(onClick = onReset) {
            Text(stringResource(R.string.label_show_all_languages))
        }
    }
}
