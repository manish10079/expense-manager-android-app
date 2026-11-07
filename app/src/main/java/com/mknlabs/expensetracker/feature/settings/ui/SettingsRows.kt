package com.mknlabs.expensetracker.feature.settings.ui

import android.content.Intent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Cloud
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Database
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Tags
import com.composables.icons.lucide.Ticket
import com.composables.icons.lucide.UserPlus
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.User














import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.core.ui.components.adPassDurationLabel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.core.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.theme.settingsSubtitleInk
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.components.ProfileCard
import com.mknlabs.expensetracker.core.ui.components.ProPassRedeemDialog
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.disabled

import com.mknlabs.expensetracker.monetization.MonetizationViewModel


@Composable
internal fun SettingsSectionContainer(
    headerRes: Int,
    items: List<SettingsRowData>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    // The settings screen's own card fill, shared with the sibling screens that join it.
    val sectionCardColors = settingsGroupCardColors()
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(headerRes),
            // One step up from the label scale's 12sp, so the section name reads as a
            // heading above its card; the rest of labelMedium (family, weight, tracking)
            // stays as it was.
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
            // Same third-ink weight as light in both themes so the label stays an aside
            // above the card rather than competing with the brand-gradient icon pucks.
            color = colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 6.dp)
        )

        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            // Both modes take the mockup's own card fill, so the group cards match the
            // hero above them rather than sitting on a tonal container of their own.
            colors = sectionCardColors,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    SettingsRowItemView(data = item)

                    if (index < items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(colorScheme.background)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single Row Item inside a Section Card.
 */
@Composable
internal fun SettingsRowItemView(
    data: SettingsRowData,
    modifier: Modifier = Modifier
) {
    val isEnabled = data.isEnabled
    val colorScheme = MaterialTheme.colorScheme

    // The tile is the mockup's own pair per row; a disabled row keeps the neutral wash it
    // has always had, since the mockup has no disabled state to copy.
    val tileFill = if (isEnabled) data.tile.fill else colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
    val tileInk = if (isEnabled) data.tile.ink else colorScheme.disabled
    val titleColor = if (isEnabled) colorScheme.onSurface else colorScheme.disabled
    val subtitleColor = if (isEnabled) {
        colorScheme.settingsSubtitleInk
    } else {
        colorScheme.disabled
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .then(
                if (isEnabled) Modifier.clickable(onClick = data.onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon Tile (40dp, 12dp corners, centered 24dp icon)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = tileFill,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = stringResource(data.titleRes),
                tint = tileInk,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title and Subtitle Text Block
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(data.titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = data.subtitleText ?: stringResource(data.subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing countdown (e.g. ad-free pass timer)
        if (data.trailing != null) {
            Text(
                text = data.trailing,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.accentInk,
                maxLines = 1
            )
        }

        if (isEnabled) {
            Spacer(modifier = Modifier.width(4.dp))

            // Trailing Chevron Icon, on the mockup's chevron ink
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = if (colorScheme.isDark) SettingsChevronInkDark else SettingsChevronInkLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Isolated Logout action row without a card container.
 */
@Composable
internal fun SettingsIsolatedLogoutRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .defaultMinSize(minHeight = 72.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon Tile, the mockup's sign-out rose
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = TileRose.fill,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = stringResource(R.string.label_logout),
                tint = TileRose.ink,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title & Subtitle
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.label_logout),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stringResource(R.string.desc_logout_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Trailing Chevron Icon with Error tint
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Data model representing a single row within a Settings Section.
 */
internal data class SettingsRowData(
    val titleRes: Int,
    val subtitleRes: Int,
    /** Pre-formatted subtitle (e.g. a duration interpolated from Remote Config); wins over
     *  [subtitleRes] when present. */
    val subtitleText: String? = null,
    val icon: ImageVector,
    /** Leading tile fill and ink, the mockup's hue pair for this row. */
    val tile: SettingsTilePalette,
    val onClick: () -> Unit,
    val isEnabled: Boolean = true,
    val trailing: String? = null
)
