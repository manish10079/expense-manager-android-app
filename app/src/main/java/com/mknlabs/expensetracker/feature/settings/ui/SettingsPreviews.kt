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


@Preview(
    name = "Settings Screen Light Preview",
    showBackground = true,
    widthDp = 412,
    heightDp = 1000
)
@Composable
internal fun SettingsScreenLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        SettingsScreenContent()
    }
}

@Preview(
    name = "Settings Screen Dark Preview",
    showBackground = true,
    widthDp = 412,
    heightDp = 1000
)
@Composable
internal fun SettingsScreenDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SettingsScreenContent()
    }
}

internal val previewSignedInProfile = defaultUserProfile.copy(
    fullName = "Johnathan Doe",
    emailAddress = "john.doe@example.com",
    gender = "Male",
    authProvider = "google"
)

@Preview(
    name = "Settings Screen Signed In Light",
    showBackground = true,
    widthDp = 412,
    heightDp = 1000
)
@Composable
internal fun SettingsScreenSignedInLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        SettingsScreenContent(
            userProfile = previewSignedInProfile,
            userTier = UserTier.PREMIUM
        )
    }
}

@Preview(
    name = "Settings Screen Signed In Dark",
    showBackground = true,
    widthDp = 412,
    heightDp = 1000
)
@Composable
internal fun SettingsScreenSignedInDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SettingsScreenContent(
            userProfile = previewSignedInProfile,
            userTier = UserTier.PREMIUM
        )
    }
}
