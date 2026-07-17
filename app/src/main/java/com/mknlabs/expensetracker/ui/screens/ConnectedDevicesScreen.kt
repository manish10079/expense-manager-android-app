package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.PhonelinkErase
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mknlabs.expensetracker.domain.repository.RegisteredDevice
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.viewmodels.ConnectedDevicesUiState
import com.mknlabs.expensetracker.ui.viewmodels.ConnectedDevicesViewModel
import com.mknlabs.expensetracker.utils.formatDate

@Composable
fun ConnectedDevicesScreen(
    userTier: UserTier,
    isSyncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    viewModel: ConnectedDevicesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppHeader(
            title = stringResource(R.string.title_connected_devices),
            onBackClick = onBackClick,
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp)
        )

        if (userTier != UserTier.PREMIUM) {
            SyncTeaseContent(onUpgradeClick)
        } else {
            when (val state = uiState) {
                is ConnectedDevicesUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ConnectedDevicesUiState.Success -> {
                    DeviceListContent(
                        devices = state.devices,
                        maxDevices = state.maxDevices,
                        isSyncEnabled = isSyncEnabled,
                        onSyncEnabledChange = onSyncEnabledChange,
                        isSyncing = isSyncing,
                        onForceSyncClick = {
                            viewModel.forceSync { result ->
                                if (result.isSuccess) {
                                    android.widget.Toast.makeText(
                                        context,
                                        context.getString(R.string.toast_force_sync_success),
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                                    android.widget.Toast.makeText(
                                        context,
                                        context.getString(R.string.toast_force_sync_failed, errorMsg),
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        onUnlink = viewModel::unregisterDevice
                    )
                }
                is ConnectedDevicesUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncTeaseContent(onUpgradeClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudSync,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.title_upgrade_for_sync),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.desc_upgrade_for_sync),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onUpgradeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.label_upgrade_to_pro),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun DeviceListContent(
    devices: List<RegisteredDevice>,
    maxDevices: Int,
    isSyncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    isSyncing: Boolean,
    onForceSyncClick: () -> Unit,
    onUnlink: (String) -> Unit
) {
    var deviceToUnlink by remember { mutableStateOf<RegisteredDevice?>(null) }
    var showForceSyncInfo by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sync Toggle
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.desc_cloud_sync),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isSyncEnabled,
                    onCheckedChange = onSyncEnabledChange
                )
            }
        }

        // Force Sync option if sync is enabled
        if (isSyncEnabled) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.title_force_sync),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showForceSyncInfo = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = stringResource(R.string.desc_force_sync),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Button(
                            onClick = onForceSyncClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudSync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.btn_sync_now),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Usage Summary
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_devices_used, devices.size, maxDevices),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(devices.size.toFloat() / maxDevices * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { devices.size.toFloat() / maxDevices },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (devices.size >= maxDevices) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }

        Text(
            text = stringResource(R.string.desc_connected_devices),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding + 8.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(devices, key = { it.id }) { device ->
                DeviceItem(
                    device = device,
                    onUnlinkClick = { deviceToUnlink = device }
                )
            }
        }
    }

    if (deviceToUnlink != null) {
        AlertDialog(
            onDismissRequest = { deviceToUnlink = null },
            title = { Text(deviceToUnlink!!.modelName) },
            text = { Text(stringResource(R.string.msg_unlink_device_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUnlink(deviceToUnlink!!.id)
                        deviceToUnlink = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.btn_unlink_device), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToUnlink = null }) {
                    Text(stringResource(R.string.label_cancel_confirm))
                }
            }
        )
    }

    if (showForceSyncInfo) {
        AlertDialog(
            onDismissRequest = { showForceSyncInfo = false },
            title = {
                Text(
                    text = stringResource(R.string.title_force_sync),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.desc_force_sync),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showForceSyncInfo = false }) {
                    Text(text = stringResource(R.string.label_ok))
                }
            }
        )
    }
}

@Composable
private fun DeviceItem(
    device: RegisteredDevice,
    onUnlinkClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (device.isCurrentDevice) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (device.isCurrentDevice) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Devices,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (device.isCurrentDevice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = device.modelName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    )
                )
                
                if (device.isCurrentDevice) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.label_this_device),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = if (device.isCurrentDevice) stringResource(R.string.label_active_now) 
                           else stringResource(R.string.label_last_active, formatDate(device.lastActiveMillis, "dd MMM, HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (device.isCurrentDevice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!device.isCurrentDevice) {
                IconButton(
                    onClick = onUnlinkClick, 
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhonelinkErase,
                        contentDescription = stringResource(R.string.btn_unlink_device),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}
