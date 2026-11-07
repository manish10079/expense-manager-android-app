package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.track

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.outlined.Info
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Desktop
import com.adamglin.phosphoricons.regular.LinkBreak
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.switchOnThumb
import com.mknlabs.expensetracker.core.ui.theme.switchOnTick
import com.mknlabs.expensetracker.core.ui.theme.textTertiary
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.syncCard
import com.mknlabs.expensetracker.core.ui.theme.syncCardBorder
import com.mknlabs.expensetracker.core.ui.theme.syncDeviceCard
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.domain.repository.RegisteredDevice
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.utils.formatDate
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme


@Composable
internal fun DeviceListContent(
    devices: List<RegisteredDevice>,
    maxDevices: Int,
    isSyncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    isSyncing: Boolean,
    lastSyncTimeMillis: Long,
    onForceSyncClick: () -> Unit,
    onUnlink: (String) -> Unit
) {
    var deviceToUnlink by remember { mutableStateOf<RegisteredDevice?>(null) }
    var showForceSyncInfo by remember { mutableStateOf(false) }

    // The whole screen is one scroll surface: the sync cards and the device rows
    // scroll together, so a short window (phone landscape) can still reach the
    // cards that otherwise clipped off the top of an unscrollable Column.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Sync Toggle
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            // The mockup's Obsidian card fill, #121622 with the 8% white edge in dark;
            // light stays the standard white card.
            colors = AppCardDefaults.colors(
                darkContainer = MaterialTheme.colorScheme.syncCard,
                darkBorder = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.syncCardBorder
                )
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.title_cloud_sync),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.desc_cloud_sync),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Material's Switch paints a flat track, so the mockup's brand-gradient
                // track sits behind a switch whose own checked track is transparent.
                // Both share the same scale so the pill stays under the thumb.
                val trackAlpha by animateFloatAsState(
                    targetValue = if (isSyncEnabled) 1f else 0f,
                    label = "cloudSyncTrack"
                )
                Box(
                    modifier = Modifier.scale(0.9f),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(32.dp)
                            .alpha(trackAlpha)
                            .clip(CircleShape)
                            .background(brush = brandGradient())
                    )
                    Switch(
                        checked = isSyncEnabled,
                        onCheckedChange = onSyncEnabledChange,
                        thumbContent = if (isSyncEnabled) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                    tint = MaterialTheme.colorScheme.switchOnTick
                                )
                            }
                        } else {
                            null
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.switchOnThumb,
                            checkedTrackColor = androidx.compose.ui.graphics.Color.Transparent,
                            checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            // Off matches the app-lock toggle on Security & Privacy.
                            uncheckedThumbColor = MaterialTheme.colorScheme.textTertiary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            uncheckedBorderColor = MaterialTheme.colorScheme.textTertiary
                        )
                    )
                }
            }
        }

        // Force Sync option if sync is enabled
        if (isSyncEnabled) {
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                // The sync action shares the master switch's card fill in the mockup.
                colors = AppCardDefaults.colors(
                    darkContainer = MaterialTheme.colorScheme.syncCard,
                    darkBorder = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.syncCardBorder
                    )
                ),
            ) {
                // The mockup stacks the title, its info glyph and "Last synced" in one
                // block with the Sync Now button underneath at phone width, and lays the
                // two out side by side once the card is wide enough (sm:flex-row).
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    val sideBySide = maxWidth >= 560.dp
                    val lastSyncText = if (lastSyncTimeMillis > 0L) {
                        stringResource(R.string.label_last_synced, formatDate(lastSyncTimeMillis, "MMM dd, yyyy · hh:mm a"))
                    } else {
                        stringResource(R.string.label_never_synced)
                    }
                    val syncAction: @Composable (Modifier) -> Unit = { modifier ->
                        if (isSyncing) {
                            Box(modifier, contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.accentInk,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        } else {
                            Button(
                                onClick = onForceSyncClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onBrandGradient
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                modifier = modifier.background(
                                    brush = brandGradient(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.btn_sync_now),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                    val syncInfo: @Composable () -> Unit = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.title_force_sync),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                IconButton(
                                    onClick = { showForceSyncInfo = true },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = stringResource(R.string.desc_force_sync),
                                        tint = MaterialTheme.colorScheme.accentInk,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = lastSyncText,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (sideBySide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Box(Modifier.weight(1f)) { syncInfo() }
                            syncAction(Modifier)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            syncInfo()
                            syncAction(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }

        // Usage Summary
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            // Same Obsidian fill as the sync cards above, as the mockup draws it.
            colors = AppCardDefaults.colors(
                darkContainer = MaterialTheme.colorScheme.syncCard,
                darkBorder = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.syncCardBorder
                )
            ),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.label_devices_used, devices.size, maxDevices),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                val filled = devices.size.coerceIn(0, maxDevices)
                val atCap = devices.size >= maxDevices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(maxDevices.coerceAtLeast(1)) { index ->
                        val on = index < filled
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        on && atCap -> MaterialTheme.colorScheme.error
                                        on -> MaterialTheme.colorScheme.accentInk
                                        else -> MaterialTheme.colorScheme.track
                                    }
                                )
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.desc_connected_devices),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding + 8.dp, vertical = 8.dp)
        )

        // Rendered directly rather than through a nested lazy list: the cap keeps
        // this to a handful of rows, and a second scroll container inside the one
        // above would fight it for the gesture.
        devices.forEach { device ->
            DeviceItem(
                device = device,
                onUnlinkClick = { deviceToUnlink = device }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (deviceToUnlink != null) {
        ModernConfirmationDialog(
            onDismissRequest = { deviceToUnlink = null },
            title = deviceToUnlink!!.modelName,
            text = stringResource(R.string.msg_unlink_device_confirm),
            confirmButtonText = stringResource(R.string.btn_unlink_device),
            onConfirm = {
                onUnlink(deviceToUnlink!!.id)
                deviceToUnlink = null
            },
            headerIcon = PhosphorIcons.Regular.LinkBreak,
            dismissButtonText = stringResource(R.string.label_cancel_confirm),
            onDismiss = { deviceToUnlink = null }
        )
    }

    if (showForceSyncInfo) {
        ModernConfirmationDialog(
            onDismissRequest = { showForceSyncInfo = false },
            title = stringResource(R.string.title_force_sync),
            text = stringResource(R.string.desc_force_sync) + "\n\n" + stringResource(R.string.desc_last_synced_info),
            confirmButtonText = stringResource(R.string.label_ok),
            onConfirm = { showForceSyncInfo = false },
            headerIcon = Icons.Rounded.CloudSync,
            showDismissButton = false
        )
    }
}

