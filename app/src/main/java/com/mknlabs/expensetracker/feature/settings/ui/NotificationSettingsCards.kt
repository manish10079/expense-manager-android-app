package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BellRinging
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.CloudCheck
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.PaperPlaneTilt
import com.adamglin.phosphoricons.regular.PiggyBank
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Ruler
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.ReminderWindow
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.monetization.AccessLevel
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.notifications.NotificationPermissionPrefs
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.SettingsGroup
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.core.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePicker
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import com.mknlabs.expensetracker.utils.validateAndCalculateTimestamp
import java.util.Calendar

import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme

@Composable
internal fun PremiumNotificationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTile: SettingsTilePalette,
    titleRes: Int,
    descRes: Int,
    isEnabled: Boolean,
    isPremium: Boolean,
    onToggle: (Boolean) -> Unit,
    onInfo: () -> Unit,
    onPremiumClick: () -> Unit
) {
    SettingsItemCard(
        icon = icon,
        iconTile = iconTile,
        title = stringResource(id = titleRes),
        subtitle = stringResource(id = descRes),
        type = SettingsItemType.Toggle,
        standalone = false,
        isLocked = !isPremium,
        accessLevel = AccessLevel.PREMIUM,
        isChecked = isEnabled,
        onCheckedChange = onToggle,
        onInfoClick = onInfo,
        onClick = onPremiumClick
    )
}

@Composable
internal fun NotificationPermissionBanner(
    hasRequestedPermission: Boolean,
    onEnableClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.errorContainer.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Info,
                contentDescription = null,
                tint = colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.notification_permission_banner_title),
                    color = colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(id = R.string.notification_permission_banner_desc),
                    color = colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            AppTextButton(onClick = onEnableClick) {
                Text(
                    text = stringResource(
                        id = if (hasRequestedPermission) {
                            R.string.btn_open_notification_settings
                        } else {
                            R.string.btn_allow_notifications
                        }
                    ),
                    color = colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
