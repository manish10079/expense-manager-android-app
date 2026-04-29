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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkn0079.expensetracker.models.SettingsItemType
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme

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
    isChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val finalEnabled = isEnabled && !isLocked
    val containerShape = RoundedCornerShape(28.dp)

    val updatedOnClick by rememberUpdatedState(onClick)
    val updatedOnCheckedChange by rememberUpdatedState(onCheckedChange)
    val interactionSource = remember { MutableInteractionSource() }

    val primary = colorScheme.primary
    val onSurface = colorScheme.onSurface
    val onSurfaceVariant = colorScheme.onSurfaceVariant
    val danger = colorScheme.error
    val containerColor = colorScheme.surface
    val borderColor = colorScheme.outlineVariant.copy(
        alpha = if (finalEnabled) 0.4f else 0.2f
    )

    val iconTint = when {
        !finalEnabled -> onSurfaceVariant.copy(alpha = 0.5f)
        isDanger -> danger
        else -> primary
    }

    val titleColor = when {
        !finalEnabled -> onSurface.copy(alpha = 0.5f)
        isDanger -> danger
        else -> onSurface
    }

    val subtitleColor = onSurfaceVariant.copy(
        alpha = if (finalEnabled) 1f else 0.6f
    )

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = containerShape,
        color = containerColor,
        border = BorderStroke(width = 1.dp, color = borderColor),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = finalEnabled && type != SettingsItemType.Toggle,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current
                ) {
                    updatedOnClick?.invoke()
                }
                .padding(horizontal = 16.dp, vertical = 1.dp)
                .heightIn(min = 76.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

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

                    if (isLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Locked",
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
                        contentDescription = "Open",
                        tint = onSurfaceVariant.copy(alpha = 0.35f),
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
                            text = valueText ?: "Action",
                            color = primary
                        )
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
                    icon = Icons.Rounded.Notifications,
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
                    icon = Icons.Rounded.Notifications,
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
