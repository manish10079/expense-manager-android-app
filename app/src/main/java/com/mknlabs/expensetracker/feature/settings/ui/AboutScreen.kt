package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Bug
import com.adamglin.phosphoricons.regular.Code
import com.adamglin.phosphoricons.regular.Envelope
import com.adamglin.phosphoricons.regular.FileText
import com.adamglin.phosphoricons.regular.Globe
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithCache
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.components.RateAppBottomSheet
import com.mknlabs.expensetracker.data.local.RateAppPromptStore
import com.mknlabs.expensetracker.core.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.components.AppIconBox
import androidx.compose.foundation.border
// Legacy theme imports removed

import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

import androidx.activity.compose.BackHandler

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onPrepareForExternalActivity: () -> Unit = {},
    isAdsEnabled: Boolean = false
) {
    val context = LocalContext.current

    BackHandler {
        onBackClick()
    }

    val openUrl: (String) -> Unit = { url ->
        try {
            onPrepareForExternalActivity()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }

    val sendEmail: (String) -> Unit = { email ->
        try {
            onPrepareForExternalActivity()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }

    AboutScreenContent(
        isAdsEnabled = isAdsEnabled,
        onBackClick = onBackClick,
        onOpenUrl = openUrl,
        onSendEmail = sendEmail,
        onFeedbackClick = onFeedbackClick
    )
}

@Composable
private fun AboutScreenContent(
    isAdsEnabled: Boolean,
    onBackClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onSendEmail: (String) -> Unit,
    onFeedbackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val privacyPolicyUrl = stringResource(R.string.url_privacy_policy)
    val termsConditionsUrl = stringResource(R.string.url_terms_conditions)
    var isRateSheetVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val enter = rememberSectionEnterAlphas(2)
        AppHeader(
            title = stringResource(R.string.title_about),
            onBackClick = onBackClick,
            modifier = Modifier.padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = Dimens.HeaderSpacing).alpha(enter[0])
        )

        AdaptiveContent(
            maxWidth = 640.dp,
            modifier = Modifier.fillMaxSize().alpha(enter[1])
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = stringResource(R.string.desc_app_icon),
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.label_app_name_display),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = stringResource(R.string.label_app_version),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // About App Card
            AboutInfoCard(
                icon = PhosphorIcons.Regular.Info,
                title = stringResource(R.string.title_about_app),
                description = stringResource(R.string.label_about_app_desc)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Developed By Card
            DeveloperCard(
                lab = "Mkn Labs",
                email = "mknlabs.dev@gmail.com",
                onEmailClick = { onSendEmail("mknlabs.dev@gmail.com") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Inline Native Ad before Support Section
            AdContainer(isAdsEnabled = isAdsEnabled) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Support Section
            AboutSectionHeader(title = stringResource(R.string.title_support))
            SupportLegalSection {
                AboutActionItem(
                    icon = PhosphorIcons.Regular.Star,
                    tile = SettingsTiles.Amber,
                    title = stringResource(R.string.title_rate_app),
                    onClick = { isRateSheetVisible = true }
                )
                AboutActionItem(
                    icon = PhosphorIcons.Regular.Envelope,
                    tile = SettingsTiles.Sky,
                    title = stringResource(R.string.title_send_feedback),
                    onClick = onFeedbackClick
                )
                AboutActionItem(
                    icon = PhosphorIcons.Regular.Bug,
                    tile = SettingsTiles.Rose,
                    title = stringResource(R.string.title_report_bug),
                    onClick = { /* Handle report bug */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legal Section
            AboutSectionHeader(title = stringResource(R.string.title_legal))
            SupportLegalSection {
                AboutActionItem(
                    icon = PhosphorIcons.Regular.Lock,
                    tile = SettingsTiles.Emerald,
                    title = stringResource(R.string.title_privacy_policy),
                    onClick = { onOpenUrl(privacyPolicyUrl) }
                )
                AboutActionItem(
                    icon = PhosphorIcons.Regular.FileText,
                    tile = SettingsTiles.Indigo,
                    title = stringResource(R.string.title_terms_conditions),
                    onClick = { onOpenUrl(termsConditionsUrl) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Follow Us Section
            AboutSectionHeader(title = stringResource(R.string.label_follow_us))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialButton(
                    icon = PhosphorIcons.Regular.Globe,
                    label = stringResource(R.string.label_website),
                    onClick = { onOpenUrl("https://expense-tracker-2ea00.web.app/") }
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialButton(
                    icon = PhosphorIcons.Regular.Code,
                    label = stringResource(R.string.label_github),
                    onClick = { onOpenUrl("https://github.com/manish10079") }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.label_handcrafted_with_precision_and),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
        }
    }

    if (isRateSheetVisible) {
        val rateContext = LocalContext.current
        val rateScope = rememberCoroutineScope()
        RateAppBottomSheet(
            onDismiss = { isRateSheetVisible = false },
            onRated = {
                rateScope.launch { RateAppPromptStore.markCompleted(rateContext) }
            }
        )
    }
}

@Composable
private fun AboutInfoCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardDefaults.shape(24.dp),
        colors = settingsGroupCardColors(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconBox(
                icon = icon,
                contentDescription = null,
                size = 40.dp,
                iconSize = 24.dp,
                tint = SettingsTiles.Violet.ink,
                backgroundColor = SettingsTiles.Violet.fill,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
        }
    }
}

@Composable
private fun DeveloperCard(
    lab: String,
    email: String,
    onEmailClick: () -> Unit
) {
    AppCard(
        onClick = onEmailClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardDefaults.shape(24.dp),
        colors = settingsGroupCardColors(),
        contentPadding = PaddingValues(24.dp),
    ) {
        Column {
            Text(
                text = stringResource(R.string.label_developed_by),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lab,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = email,
                color = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AboutSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.8f),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
private fun SupportLegalSection(content: @Composable () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardDefaults.shape(24.dp),
        colors = settingsGroupCardColors(),
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun AboutActionItem(
    icon: ImageVector,
    tile: SettingsTilePalette,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconBox(
            icon = icon,
            contentDescription = null,
            size = 40.dp,
            iconSize = 24.dp,
            tint = tile.ink,
            backgroundColor = tile.fill,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SocialButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppIconBox(
            icon = icon,
            contentDescription = label,
            size = 60.dp,
            iconSize = 28.dp,
            backgroundBrush = brandGradient(),
            tint = MaterialTheme.colorScheme.onCta,
            modifier = Modifier.clickable(onClick = onClick)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "About Screen - Light Mode")
@Composable
fun AboutScreenPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        AboutScreenContent(
            isAdsEnabled = true,
            onBackClick = {},
            onOpenUrl = {},
            onSendEmail = {},
            onFeedbackClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "About Screen - Dark Mode")
@Composable
fun AboutScreenPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        AboutScreenContent(
            isAdsEnabled = false,
            onBackClick = {},
            onOpenUrl = {},
            onSendEmail = {},
            onFeedbackClick = {}
        )
    }
}
