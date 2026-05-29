package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.models.SettingsItemType
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.monetization.AccessLevel
import com.mkn0079.expensetracker.ui.theme.featureGateLock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    type: SettingsItemType,
    valueText: String? = null,
    isEnabled: Boolean = true,
    isDanger: Boolean = false,
    isLocked: Boolean = false,
    isHighlight: Boolean = false,
    accessLevel: AccessLevel = AccessLevel.FREE,
    isChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val finalEnabled = isEnabled && !isLocked
    val isGated = isLocked && accessLevel != AccessLevel.FREE
    val containerShape = RoundedCornerShape(28.dp)

    val updatedOnClick by rememberUpdatedState(onClick)
    val updatedOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val interactionSource = remember { MutableInteractionSource() }

    val primary = colorScheme.primary
    val onSurface = colorScheme.onSurface
    val onSurfaceVariant = colorScheme.onSurfaceVariant
    val danger = colorScheme.error
    
    // Fix: Use opaque color to prevent gradient bleed through the card
    val containerColor = colorScheme.surface
    
    val borderColor = if (isHighlight) {
        Color.Transparent // We use a gradient border instead
    } else {
        colorScheme.outlineVariant.copy(
            alpha = if (finalEnabled) 0.4f else 0.2f
        )
    }

    val lockColor = colorScheme.featureGateLock

    val iconTint = when {
        isGated -> lockColor
        !finalEnabled -> onSurfaceVariant.copy(alpha = 0.5f)
        isDanger -> danger
        else -> primary
    }

    val iconBackground = when {
        isGated -> lockColor.copy(alpha = 0.12f)
        else -> primary.copy(alpha = 0.1f)
    }

    val titleColor = when {
        isGated -> onSurface.copy(alpha = 0.38f)
        !finalEnabled -> onSurface.copy(alpha = 0.5f)
        isDanger -> danger
        else -> onSurface
    }

    val subtitleColor = when {
        isGated -> onSurfaceVariant.copy(alpha = 0.38f)
        else -> onSurfaceVariant.copy(alpha = if (finalEnabled) 1f else 0.6f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isHighlight) {
                    Modifier.background(
                        brush = com.mkn0079.expensetracker.ui.theme.brandGradient(),
                        shape = containerShape
                    ).padding(2.dp) // Simulated border - slightly thicker for better visibility
                } else Modifier
            ),
        shape = containerShape,
        color = containerColor,
        border = if (isHighlight) null else BorderStroke(width = 1.dp, color = borderColor),
        shadowElevation = if (isHighlight) 16.dp else 8.dp // More elevation for highlighted items
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = (finalEnabled && type != SettingsItemType.Toggle) || isLocked,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                ) {
                    updatedOnClick?.invoke()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp) // Fix: Increased vertical padding to prevent internal overlap
                .heightIn(min = 72.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconBox(
                icon = if (isGated) Icons.Rounded.Lock else icon,
                contentDescription = title,
                size = 44.dp,
                iconSize = 22.dp,
                tint = iconTint,
                backgroundColor = iconBackground
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = titleColor,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isLocked && !isGated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = stringResource(R.string.desc_locked),
                            tint = onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (!subtitle.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = subtitleColor,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isGated) {
                Text(
                    text = if (accessLevel == AccessLevel.AD_SUPPORTED) stringResource(R.string.label_watch_ad) else stringResource(R.string.label_premium),
                    color = lockColor,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            } else {
                when (type) {
                    SettingsItemType.Toggle -> {
                        CompositionLocalProvider(LocalRippleConfiguration provides null) {
                            Switch(
                                checked = isChecked,
                                onCheckedChange = updatedOnCheckedChange?.takeIf { finalEnabled },
                                enabled = finalEnabled,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = primary,
                                    checkedBorderColor = Color.Transparent,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = colorScheme.outlineVariant.copy(alpha = 0.45f),
                                    uncheckedBorderColor = Color.Gray,
                                    disabledCheckedThumbColor = colorScheme.onSurface.copy(alpha = 0.38f),
                                    disabledCheckedTrackColor = primary.copy(alpha = 0.30f),
                                    disabledCheckedBorderColor = Color.Transparent,
                                    disabledUncheckedThumbColor = colorScheme.surface.copy(alpha = 0.9f),
                                    disabledUncheckedTrackColor = colorScheme.outlineVariant.copy(alpha = 0.22f),
                                    disabledUncheckedBorderColor = Color.Transparent
                                )
                            )
                        }
                    }

                    SettingsItemType.Navigation -> {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = stringResource(R.string.label_open),
                            tint = primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    SettingsItemType.Value -> {
                        if (!valueText.isNullOrEmpty()) {
                            Text(
                                text = valueText,
                                color = onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    SettingsItemType.Button -> {
                        TextButton(
                            onClick = { updatedOnClick?.invoke() },
                            enabled = finalEnabled
                        ) {
                            Text(
                                text = valueText ?: stringResource(R.string.label_action),
                                color = primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101417)
@Composable
private fun SettingsItemCardPreviewDark() {
    var notificationsEnabled by remember { mutableStateOf(true) }

    ExpenseTrackerTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SettingsItemCard(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Notifications",
                    subtitle = "Manage reminders and alerts",
                    type = SettingsItemType.Toggle,
                    isChecked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                SettingsItemCard(
                    icon = Icons.Rounded.Palette,
                    title = "Appearance",
                    subtitle = "Theme and accent preferences",
                    type = SettingsItemType.Navigation
                )

                SettingsItemCard(
                    icon = Icons.Rounded.Sync,
                    title = "Sync",
                    subtitle = "Current provider",
                    type = SettingsItemType.Value,
                    valueText = "Google Drive"
                )

                SettingsItemCard(
                    icon = Icons.Rounded.Security,
                    title = "Reset Security",
                    subtitle = "This action requires verification",
                    type = SettingsItemType.Button,
                    valueText = "Verify",
                    isLocked = true
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SettingsItemCardPreviewLight() {
    var notificationsEnabled by remember { mutableStateOf(true) }

    ExpenseTrackerTheme(darkTheme = false) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SettingsItemCard (
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Notifications",
                    subtitle = "Manage reminders and alerts",
                    type = SettingsItemType.Toggle,
                    isChecked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                SettingsItemCard(
                    icon = Icons.Rounded.Palette,
                    title = "Appearance",
                    subtitle = "Theme and accent preferences",
                    type = SettingsItemType.Navigation
                )

                SettingsItemCard(
                    icon = Icons.Rounded.Sync,
                    title = "Sync",
                    subtitle = "Current provider",
                    type = SettingsItemType.Value,
                    valueText = "Google Drive"
                )

                SettingsItemCard(
                    icon = Icons.Rounded.Security,
                    title = "Reset Security",
                    subtitle = "This action requires verification",
                    type = SettingsItemType.Button,
                    valueText = "Verify",
                    isLocked = true
                )
            }
        }
    }
}
