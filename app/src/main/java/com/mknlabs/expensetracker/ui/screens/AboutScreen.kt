package com.mknlabs.expensetracker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.components.AppIconBox
import androidx.compose.foundation.border
// Legacy theme imports removed

import androidx.hilt.navigation.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onPrepareForExternalActivity: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppHeader(
            title = stringResource(R.string.title_about),
            onBackClick = onBackClick,
            modifier = Modifier.padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.label_app_version),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // About App Card
            AboutInfoCard(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.title_about_app),
                description = stringResource(R.string.label_about_app_desc)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Developed By Card
            DeveloperCard(
                name = "Manish Kumar Nayak",
                email = "mknlabs.dev@gmail.com",
                onEmailClick = { sendEmail("mknlabs.dev@gmail.com") }
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
                    icon = Icons.Filled.Star,
                    title = stringResource(R.string.title_rate_app),
                    onClick = { /* Handle rate app */ }
                )
                AboutActionItem(
                    icon = Icons.Filled.Email,
                    title = stringResource(R.string.title_send_feedback),
                    onClick = { sendEmail("mknlabs.dev@gmail.com") }
                )
                AboutActionItem(
                    icon = Icons.Filled.BugReport,
                    title = stringResource(R.string.title_report_bug),
                    onClick = { /* Handle report bug */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legal Section
            AboutSectionHeader(title = stringResource(R.string.title_legal))
            val privacyPolicyUrl = stringResource(R.string.url_privacy_policy)
            SupportLegalSection {
                AboutActionItem(
                    icon = Icons.Filled.Lock,
                    title = stringResource(R.string.title_privacy_policy),
                    onClick = { openUrl(privacyPolicyUrl) }
                )
                AboutActionItem(
                    icon = Icons.Filled.Description,
                    title = stringResource(R.string.title_terms_conditions),
                    onClick = { openUrl("https://expense-tracker-2ea00.web.app/") }
                )
                AboutActionItem(
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    title = stringResource(R.string.title_open_source_licenses),
                    onClick = { /* Show licenses */ }
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
                    icon = Icons.Filled.Public,
                    label = stringResource(R.string.label_website),
                    onClick = { openUrl("https://expense-tracker-2ea00.web.app/") }
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialButton(
                    icon = Icons.Filled.Code,
                    label = stringResource(R.string.label_github),
                    onClick = { openUrl("https://github.com/manish10079") }
                )
                Spacer(modifier = Modifier.width(24.dp))
                SocialButton(
                    icon = Icons.Filled.Work,
                    label = stringResource(R.string.label_linkedin),
                    onClick = { openUrl("https://linkedin.com/in/manishkumar10079") }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.label_handcrafted_with_precision_and),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AboutInfoCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun DeveloperCard(
    name: String,
    email: String,
    onEmailClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onEmailClick)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.label_developed_by),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AboutSectionHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
private fun SupportLegalSection(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}

@Composable
private fun AboutActionItem(
    icon: ImageVector,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
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
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.clickable(onClick = onClick)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "About Screen - Light Mode")
@Composable
fun AboutScreenPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        AboutScreen(onBackClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "About Screen - Dark Mode")
@Composable
fun AboutScreenPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        AboutScreen(onBackClick = {})
    }
}
