package com.mknlabs.expensetracker.feature.settings.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import android.content.ClipData
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.domain.repository.ReferralInvite
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Copy

import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.PaperPlaneTilt
import com.adamglin.phosphoricons.regular.Question
import com.adamglin.phosphoricons.regular.Ticket

import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.UserPlus
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.models.TabItem
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.domain.repository.ReferralError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun GuestRegisterCard(onLinkAccountClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardDefaults.shape(24.dp),
        colors = settingsGroupCardColors()
    ) {
    Column(
        modifier = Modifier.padding(Dimens.spacingMedium),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.msg_refer_register_for_code), style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = onLinkAccountClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(brandGradient())
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.btn_register_for_referral), color = MaterialTheme.colorScheme.onBrandGradient, fontWeight = FontWeight.Bold)
            }
        }
    }
    }
}

@Composable
internal fun CodeCard(code: String, onCopy: () -> Unit, onShare: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardDefaults.shape(24.dp),
        colors = settingsGroupCardColors()
    ) {
    Column(modifier = Modifier.padding(Dimens.spacingMedium)) {
        Text(
            stringResource(R.string.label_your_referral_code),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = code.ifBlank { "••••••••" },
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onCopy,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.accentInk)
            ) {
                Icon(PhosphorIcons.Regular.Copy, contentDescription = stringResource(R.string.btn_copy_code), modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.btn_copy_code))
            }
        }
        Spacer(Modifier.height(6.dp))
        val shareCd = stringResource(R.string.btn_share_invite_link)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(brandGradient())
                .clickable(onClick = onShare)
                .padding(vertical = 14.dp)
                .semantics { contentDescription = shareCd },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(PhosphorIcons.Regular.PaperPlaneTilt, null, tint = MaterialTheme.colorScheme.onBrandGradient, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    stringResource(R.string.btn_share_invite_link),
                    color = MaterialTheme.colorScheme.onBrandGradient,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
    }
}

