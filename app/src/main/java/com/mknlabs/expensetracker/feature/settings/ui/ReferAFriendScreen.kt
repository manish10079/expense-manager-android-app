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
fun ReferAFriendScreen(
    onBackClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    viewModel: ReferAFriendViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ReferAFriendContent(
        state = state,
        onBackClick = onBackClick,
        onLinkAccountClick = onLinkAccountClick,
        onApplyCode = { code -> viewModel.applyCode(code) },
        onDismissWelcomeCelebration = { viewModel.dismissWelcomeCelebration() },
        onDismissReferrerCelebration = { viewModel.dismissReferrerCelebration() }
    )
}

@Composable
fun ReferAFriendContent(
    state: ReferAFriendUiState,
    onBackClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    onApplyCode: (String) -> Unit = {},
    onDismissWelcomeCelebration: () -> Unit = {},
    onDismissReferrerCelebration: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val clipboardScope = rememberCoroutineScope()
    val playUrl = stringResource(R.string.url_rate_app)
    var enterCode by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showHelp by remember { mutableStateOf(false) }
    var helpTapCount by remember { mutableIntStateOf(0) }
    var showTestPopups by remember { mutableStateOf(false) }
    var showRefereePopup by remember { mutableStateOf(false) }
    var showReferrerPopup by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val monthLabel = remember {
        SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppHeader(
            title = stringResource(R.string.title_refer_earn),
            onBackClick = onBackClick,
            modifier = Modifier.padding(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = Dimens.HeaderSpacing
            ),
            actions = {
                Icon(
                    PhosphorIcons.Regular.Question,
                    contentDescription = stringResource(R.string.desc_referral_rules),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            helpTapCount += 1
                            if (helpTapCount >= 10) showTestPopups = true
                            showHelp = true
                        }
                )
            }
        )
        Text(
            text = stringResource(R.string.label_refer_earn_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
        )
        Spacer(Modifier.height(Dimens.spacingDefault))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            if (showTestPopups) {
                ReferralTestPopupBar(
                    onRefereeClick = { showRefereePopup = true },
                    onReferrerClick = { showReferrerPopup = true }
                )
                Spacer(Modifier.height(8.dp))
            }
            AnimatedTabSwitcher(
                items = listOf(
                    TabItem(0, stringResource(R.string.tab_refer_friends)),
                    TabItem(1, stringResource(R.string.tab_my_rewards))
                ),
                selectedItemId = selectedTab,
                onItemSelected = { selectedTab = it },
                containerColor = settingsGroupCardColors().containerColor
            )
            Spacer(Modifier.height(Dimens.spacingDefault))
            Box(Modifier.weight(1f).fillMaxWidth()) {
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingDefault)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroCard()
                    if (state.isAnonymous) {
                        GuestRegisterCard(onLinkAccountClick = onLinkAccountClick)
                    } else {
                        CodeCard(
                            code = state.code,
                            onCopy = {
                                clipboardScope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(ClipData.newPlainText("referral_code", state.code))
                                    )
                                }
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.msg_code_copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onShare = {
                                val bodies = listOf(
                                    context.getString(R.string.msg_share_app_chat, playUrl),
                                    context.getString(R.string.msg_share_app_features, playUrl),
                                    context.getString(R.string.msg_share_app_punchy, playUrl)
                                )
                                val text = bodies.random() + "\n\n" +
                                    context.getString(R.string.msg_use_my_code, state.code)
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(
                                    Intent.createChooser(send, context.getString(R.string.title_refer_a_friend))
                                )
                            }
                        )
                    }
                    if (state.alreadyReferred) {
                        StatusBanner(
                            text = stringResource(R.string.msg_you_unlocked_welcome_pro),
                            positive = true
                        )
                    }
                    if (state.needsFirstTx) {
                        StatusBanner(
                            text = stringResource(R.string.msg_add_expense_to_complete_invite),
                            positive = false
                        )
                    }
                    MonthlyProgressCard(
                        rewardsThisMonth = state.rewardsThisMonth,
                        welcomeClaimed = state.alreadyReferred,
                        monthLabel = monthLabel
                    )
                    if (!state.isAnonymous && !state.alreadyReferred) {
                        RedeemCodeCard(
                            enterCode = enterCode,
                            onEnterCode = { enterCode = it },
                            applyBusy = state.applyBusy,
                            applyError = state.applyError,
                            welcomeDays = state.applyWelcomeDays,
                            onApply = { onApplyCode(enterCode) }
                        )
                    }
                    }
                    Spacer(Modifier.height(Dimens.spacingLarge))
                }
            } else {
                RewardsHistoryTab(successCount = state.successCount, invites = state.invites)
            }
            }
        }
    }

    if (showHelp) {
        ModernConfirmationDialog(
            onDismissRequest = { showHelp = false },
            title = stringResource(R.string.title_referral_rules),
            text = stringResource(R.string.msg_referral_rules),
            confirmButtonText = stringResource(R.string.btn_got_it),
            onConfirm = { showHelp = false },
            headerIcon = PhosphorIcons.Regular.Question,
            showDismissButton = false
        )
    }
    val refereeExpiry = state.welcomeExpiryMillis
    if (showRefereePopup || refereeExpiry != null) {
        val until = refereeExpiry?.takeIf { it > 0L } ?: (System.currentTimeMillis() + 4L * 24 * 60 * 60 * 1000)
        RefereeCodeAcceptedDialog(
            validUntilLabel = dateFmt.format(Date(until)),
            onDismiss = {
                showRefereePopup = false
                onDismissWelcomeCelebration()
            },
            onExplore = {
                showRefereePopup = false
                onDismissWelcomeCelebration()
                onBackClick()
            },
            onReferMore = {
                showRefereePopup = false
                onDismissWelcomeCelebration()
            }
        )
    }
    val rewardInvite = state.referrerRewardInvite
    if (showReferrerPopup || rewardInvite != null) {
        ReferrerFirstTxnDialog(
            friendName = if (showReferrerPopup) {
                stringResource(R.string.label_test_friend_name)
            } else {
                rewardInvite?.displayName?.ifBlank { stringResource(R.string.label_test_friend_name) }
                    ?: stringResource(R.string.label_test_friend_name)
            },
            claimedSlots = if (showReferrerPopup) 2 else state.rewardsThisMonth.coerceAtLeast(1),
            onDismiss = {
                showReferrerPopup = false
                onDismissReferrerCelebration()
            },
            onClaimed = {
                showReferrerPopup = false
                onDismissReferrerCelebration()
            },
            onInviteMore = {
                showReferrerPopup = false
                onDismissReferrerCelebration()
            }
        )
    }
}


