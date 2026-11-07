package com.mknlabs.expensetracker.feature.home.ui


import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.track

import android.content.res.Configuration
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.ChatCircle
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.PiggyBank
import com.adamglin.phosphoricons.regular.X
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.avatarInitials
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.hasPhoneNumber
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.core.ui.components.*
import com.mknlabs.expensetracker.core.ui.HomeFirstRunPromptGate
import com.mknlabs.expensetracker.core.ui.components.LocalSplashGone
import com.mknlabs.expensetracker.core.ui.components.MissingPermissionsBottomSheet
import com.mknlabs.expensetracker.core.ui.components.RateAppBottomSheet
import com.mknlabs.expensetracker.core.ui.components.ReferOfferBottomSheet
import com.mknlabs.expensetracker.data.local.ReferPromptStore
import com.mknlabs.expensetracker.data.local.RateAppPromptStore
import com.mknlabs.expensetracker.data.local.SmsInboxStaleRemindStore
import com.mknlabs.expensetracker.utils.RateAppPrompt
import com.mknlabs.expensetracker.utils.SmsInboxStaleRemind
import kotlinx.coroutines.delay
import com.mknlabs.expensetracker.utils.LaunchSheetPick
import kotlinx.coroutines.launch
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnLight
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupUiState
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupViewModel
import com.mknlabs.expensetracker.utils.DeviceVendorUtils
import com.mknlabs.expensetracker.utils.findFragmentActivity
import com.mknlabs.expensetracker.utils.PermissionNudge
import com.mknlabs.expensetracker.data.local.PermissionNudgeState
import com.mknlabs.expensetracker.data.local.PermissionNudgeStore
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.AdPlacement

@Composable
internal fun HomeHeaderRow(
    userProfile: UserProfile,
    uiState: HomeScreenUiState,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSmsInboxClick: () -> Unit = {},
    onCalculatorClick: () -> Unit = {},
    onCalendarViewClick: () -> Unit = {},
    isLockOverlayActive: Boolean
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val greetingInk = if (isDark) GreetingNameDark else GreetingNameLight
    val phraseInk = if (isDark) PsychologyPhraseDark else PsychologyPhraseLight
    val moreButtonBackground = if (isDark) Color.Transparent else MoreButtonBackgroundLight
    val moreButtonBorder = if (isDark) MoreButtonBorderDark else MoreButtonBorderLight
    val moreButtonIcon = if (isDark) MoreButtonIconDark else MoreButtonIconLight

    // Pick a phrase whenever the header enters composition (tab switches dispose it) and
    // whenever the app comes back to the foreground with Home already composed.
    val psychologyPhrases = stringArrayResource(R.array.psychology_phrases)
    var psychologyPhrase by remember { mutableStateOf(psychologyPhrases.random()) }
    val phraseLifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(phraseLifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                psychologyPhrase = psychologyPhrases.random()
            }
        }
        phraseLifecycleOwner.lifecycle.addObserver(observer)
        onDispose { phraseLifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val waveRotation = remember { androidx.compose.animation.core.Animatable(0f) }
    var menuExpanded by remember { mutableStateOf(false) }
    val unreadSms = uiState.smsInboxUnreadCount
    val detectionDotScale = remember { androidx.compose.animation.core.Animatable(1f) }
    var lastUnreadSms by remember { mutableStateOf(unreadSms) }
    LaunchedEffect(unreadSms) {
        val arrived = unreadSms > lastUnreadSms && unreadSms > 0
        lastUnreadSms = unreadSms
        if (arrived) {
            repeat(4) {
                detectionDotScale.animateTo(1.4f, tween(180, easing = FastOutSlowInEasing))
                detectionDotScale.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
            }
        }
    }

    val isAnonymous = userProfile.authProvider.isBlank() || userProfile.authProvider == "anonymous"
    val isPremium = uiState.userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM &&
        !isAnonymous

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.clickable(onClick = onProfileClick)
        ) {
            ProfileAvatar(
                gender = userProfile.gender,
                size = 50.dp,
                photoUri = userProfile.photoUri,
                userTier = uiState.userTier,
                isSyncing = uiState.isSyncing,
                isAnonymous = isAnonymous,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                showBadge = isPremium,
                badgeIcon = PhosphorIcons.Regular.Crown,
                badgeContentDescription = stringResource(R.string.label_pro)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            val greetingName = uiState.greetingName

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            var lifecycleResumed by remember {
                mutableStateOf(
                    lifecycleOwner.lifecycle.currentState.isAtLeast(
                        androidx.lifecycle.Lifecycle.State.RESUMED
                    )
                )
            }
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    lifecycleResumed = event == androidx.lifecycle.Lifecycle.Event.ON_RESUME
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            var entrancePlayed by remember { mutableStateOf(false) }
            LaunchedEffect(lifecycleResumed, isLockOverlayActive) {
                if (lifecycleResumed && !isLockOverlayActive && !entrancePlayed) {
                    entrancePlayed = true
                    waveRotation.snapTo(0f)
                    waveRotation.animateTo(25f, tween(500, easing = LinearOutSlowInEasing))
                    waveRotation.animateTo(-20f, tween(450, easing = FastOutSlowInEasing))
                    waveRotation.animateTo(18f, tween(400, easing = FastOutSlowInEasing))
                    waveRotation.animateTo(-14f, tween(350, easing = FastOutSlowInEasing))
                    waveRotation.animateTo(10f, tween(300, easing = FastOutSlowInEasing))
                    waveRotation.animateTo(-6f, tween(250, easing = FastOutSlowInEasing))
                    waveRotation.animateTo(0f, tween(250, easing = FastOutLinearInEasing))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.label_hi_val, greetingName),
                    color = greetingInk,
                    maxLines = maxLinesForTier(compact = 1, large = 2, huge = 2),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "👋",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = waveRotation.value
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.7f, 0.9f)
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = psychologyPhrase,
                color = phraseInk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
        }

        Box(modifier = Modifier.offset(x = 14.dp)) {
            IconButton(onClick = { menuExpanded = true }) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(moreButtonBackground)
                        .border(1.dp, moreButtonBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(id = R.string.desc_more_options),
                        tint = moreButtonIcon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (unreadSms > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-16).dp, y = 12.dp)
                        .size(8.dp)
                        .graphicsLayer {
                            scaleX = detectionDotScale.value
                            scaleY = detectionDotScale.value
                        }
                        .clip(CircleShape)
                        .background(MoreButtonDotColor)
                )
            }
            WhatsAppStyleDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            menuExpanded = false
                            onSmsInboxClick()
                        }
                        .padding(
                            horizontal = Dimens.spacingDefault,
                            vertical = Dimens.spacingCompact
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
                ) {
                    Text(
                        text = stringResource(id = R.string.title_detected_txns),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (unreadSms > 0) {
                        TabCountBadge(count = unreadSms, containerColor = MoreButtonDotColor)
                    }
                }
                Text(
                    text = stringResource(id = R.string.title_home_calculator),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            menuExpanded = false
                            onCalculatorClick()
                        }
                        .padding(
                            horizontal = Dimens.spacingDefault,
                            vertical = Dimens.spacingCompact
                        )
                )
                Text(
                    text = stringResource(id = R.string.title_calendar_view),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            menuExpanded = false
                            onCalendarViewClick()
                        }
                        .padding(
                            horizontal = Dimens.spacingDefault,
                            vertical = Dimens.spacingCompact
                        )
                )
            }
        }
    }
}

/**
 * Setup cards + stats + mini cards + native ad — the vertical stack below the
 * greeting header. Shared by the Compact single column and the left pane of the
 * two-pane layout (where it scrolls independently).
 */

@Composable
fun SettingsButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            // Visual 40dp; keeps the 48dp touch-target minimum.
            .size(40.dp)
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.Gear,
            contentDescription = stringResource(id = R.string.desc_settings),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(name = "Home header Pro Light", showBackground = true)
@Preview(name = "Home header Pro Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun HomeHeaderRowProPreview() {
    ExpenseTrackerTheme {
        HomeHeaderRow(
            userProfile = defaultUserProfile.copy(
                fullName = "Guest User",
                gender = "Male",
                authProvider = "google",
            ),
            uiState = HomeScreenUiState(
                greetingName = "Guest",
                userTier = com.mknlabs.expensetracker.models.UserTier.PREMIUM,
            ),
            onProfileClick = {},
            onSettingsClick = {},
            onSmsInboxClick = {},
            isLockOverlayActive = false,
        )
    }
}
