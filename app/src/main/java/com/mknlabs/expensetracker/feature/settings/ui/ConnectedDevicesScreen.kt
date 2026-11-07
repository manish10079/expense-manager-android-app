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
    val lastSyncTimeMillis by viewModel.lastSyncTimeMillis.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ConnectedDevicesContent(
        userTier = userTier,
        uiState = uiState,
        isSyncing = isSyncing,
        lastSyncTimeMillis = lastSyncTimeMillis,
        isSyncEnabled = isSyncEnabled,
        onSyncEnabledChange = onSyncEnabledChange,
        onBackClick = onBackClick,
        onUpgradeClick = onUpgradeClick,
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
        onUnlink = { viewModel.unregisterDevice(it) }
    )
}

@Composable
internal fun ConnectedDevicesContent(
    userTier: UserTier,
    uiState: ConnectedDevicesUiState,
    isSyncing: Boolean,
    lastSyncTimeMillis: Long,
    isSyncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onForceSyncClick: () -> Unit,
    onUnlink: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val enter = rememberSectionEnterAlphas(2)
        AppHeader(
            title = stringResource(R.string.title_cloud_sync_devices),
            onBackClick = onBackClick,
            modifier = Modifier.padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding).alpha(enter[0])
        )

        if (userTier != UserTier.PREMIUM) {
            SyncTeaseContent(onUpgradeClick)
        } else {
            when (uiState) {
                is ConnectedDevicesUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.accentInk)
                    }
                }
                is ConnectedDevicesUiState.Success -> {
                    DeviceListContent(
                        devices = uiState.devices,
                        maxDevices = uiState.maxDevices,
                        isSyncEnabled = isSyncEnabled,
                        onSyncEnabledChange = onSyncEnabledChange,
                        isSyncing = isSyncing,
                        lastSyncTimeMillis = lastSyncTimeMillis,
                        onForceSyncClick = onForceSyncClick,
                        onUnlink = onUnlink
                    )
                }
                is ConnectedDevicesUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}


