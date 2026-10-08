package com.mknlabs.expensetracker.feature.settings.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.domain.repository.ReferralInvite
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Copy
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Gift
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.PaperPlaneTilt
import com.adamglin.phosphoricons.regular.Question
import com.adamglin.phosphoricons.regular.Ticket
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Star
import com.adamglin.phosphoricons.regular.UserPlus
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.domain.repository.ReferralError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MONTHLY_CAP = 4
private const val DAYS_PER_REWARD = 7

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferAFriendScreen(
    onBackClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    viewModel: ReferAFriendViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val playUrl = stringResource(R.string.url_rate_app)
    var enterCode by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showHelp by remember { mutableStateOf(false) }
    val monthLabel = remember {
        SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.title_refer_earn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.label_refer_earn_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(
                            PhosphorIcons.Regular.Question,
                            contentDescription = stringResource(R.string.desc_referral_rules)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.spacingMedium)
        ) {
            ReferTabRow(selectedTab = selectedTab, onSelect = { selectedTab = it })
            Spacer(Modifier.height(Dimens.spacingDefault))
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingDefault)
                ) {
                    HeroCard()
                    if (state.isAnonymous) {
                        GuestRegisterCard(onLinkAccountClick = onLinkAccountClick)
                    } else {
                        CodeCard(
                            code = state.code,
                            onCopy = {
                                clipboard.setText(AnnotatedString(state.code))
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
                            onApply = { viewModel.applyCode(enterCode) }
                        )
                    }
                    Spacer(Modifier.height(Dimens.spacingLarge))
                }
            } else {
                RewardsHistoryTab(successCount = state.successCount, invites = state.invites)
            }
        }
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(stringResource(R.string.title_referral_rules)) },
            text = { Text(stringResource(R.string.msg_referral_rules)) },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text(stringResource(R.string.btn_got_it))
                }
            }
        )
    }
}

@Composable
private fun ReferTabRow(selectedTab: Int, onSelect: (Int) -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        TabChip(
            selected = selectedTab == 0,
            label = stringResource(R.string.tab_refer_friends),
            icon = PhosphorIcons.Regular.Gift,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(0) }
        )
        TabChip(
            selected = selectedTab == 1,
            label = stringResource(R.string.tab_my_rewards),
            icon = PhosphorIcons.Regular.ClockCounterClockwise,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(1) }
        )
    }
}

@Composable
private fun TabChip(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) brandGradient() else Brush.linearGradient(
        listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Transparent)
    )
    val fg = if (selected) MaterialTheme.colorScheme.onBrandGradient else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = fg)
        Spacer(Modifier.size(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun HeroCard() {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Dimens.spacingMedium)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        PhosphorIcons.Regular.Crown,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        stringResource(R.string.label_pro_privilege),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.title_give_7_get_7),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brandGradient()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    PhosphorIcons.Regular.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBrandGradient,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StepCell("1", stringResource(R.string.step_share_code), stringResource(R.string.step_share_code_sub), done = false)
            StepCell("2", stringResource(R.string.step_friend_joins), stringResource(R.string.step_friend_joins_sub), done = false)
            StepCell("✓", stringResource(R.string.step_both_get_pro), stringResource(R.string.step_both_get_pro_sub), done = true)
        }
    }
}

@Composable
private fun StepCell(badge: String, title: String, subtitle: String, done: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (done) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(PhosphorIcons.Regular.Check, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.tertiary)
            } else {
                Text(badge, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun GuestRegisterCard(onLinkAccountClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Dimens.spacingMedium),
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

@Composable
private fun CodeCard(code: String, onCopy: () -> Unit, onShare: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Dimens.spacingMedium)
    ) {
        Text(
            stringResource(R.string.label_your_referral_code),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.label_tap_to_copy),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = code.ifBlank { "••••••••" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            TextButton(onClick = onCopy) {
                Icon(PhosphorIcons.Regular.Copy, contentDescription = stringResource(R.string.btn_copy_code), modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.btn_copy_code))
            }
        }
        Spacer(Modifier.height(12.dp))
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

@Composable
private fun MonthlyProgressCard(
    rewardsThisMonth: Int,
    welcomeClaimed: Boolean,
    monthLabel: String
) {
    val earned = (rewardsThisMonth + if (welcomeClaimed) 1 else 0).coerceIn(0, MONTHLY_CAP)
    val daysEarned = earned * DAYS_PER_REWARD
    val daysLeft = (MONTHLY_CAP - earned) * DAYS_PER_REWARD
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Dimens.spacingMedium)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.title_monthly_progress), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    stringResource(R.string.label_earned_of_rewards, earned, MONTHLY_CAP),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                stringResource(R.string.label_plus_days, daysEarned),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(MONTHLY_CAP) { index ->
                RewardSlot(
                    index = index,
                    earned = earned,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { earned / MONTHLY_CAP.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.label_days_left_to_max, daysLeft),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.label_max_28_days),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RewardSlot(index: Int, earned: Int, modifier: Modifier) {
    val claimed = index < earned
    val next = index == earned && earned < MONTHLY_CAP
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    claimed -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                    next -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        claimed -> MaterialTheme.colorScheme.tertiary
                        next -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                claimed -> Icon(PhosphorIcons.Regular.Check, null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onTertiary)
                next -> Text("${index + 1}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                else -> Icon(PhosphorIcons.Regular.Lock, null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("7 Days", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(
            when {
                claimed -> stringResource(R.string.label_claimed)
                next -> stringResource(R.string.label_next_slot)
                else -> stringResource(R.string.label_slot_locked)
            },
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RedeemCodeCard(
    enterCode: String,
    onEnterCode: (String) -> Unit,
    applyBusy: Boolean,
    applyError: ReferralError?,
    welcomeDays: Int?,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Dimens.spacingMedium)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(PhosphorIcons.Regular.Ticket, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(6.dp))
            Text(
                stringResource(R.string.label_have_friend_code),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = enterCode,
            onValueChange = onEnterCode,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.label_enter_friend_code)) },
            singleLine = true,
            trailingIcon = {
                TextButton(onClick = onApply, enabled = enterCode.isNotBlank() && !applyBusy) {
                    Text(stringResource(R.string.btn_apply_code), fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
        applyError?.let {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(referralErrorRes(it)), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
        welcomeDays?.let { days ->
            if (days > 0) {
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.msg_welcome_days, days), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatusBanner(text: String, positive: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (positive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (positive) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
            .padding(Dimens.spacingDefault)
    )
}

@Composable
private fun RewardsHistoryTab(successCount: Int, invites: List<ReferralInvite>) {
    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.title_your_referrals), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                stringResource(R.string.label_total_active, successCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        if (invites.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    PhosphorIcons.Regular.UserPlus,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.msg_invite_more_friends),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            invites.forEach { invite ->
                InviteRow(invite = invite, dateLabel = if (invite.attributedAt > 0L) dateFmt.format(Date(invite.attributedAt)) else "")
            }
        }
    }
}

@Composable
private fun InviteRow(invite: ReferralInvite, dateLabel: String) {
    val initials = invite.displayName.take(2).uppercase(Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(invite.displayName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            if (dateLabel.isNotBlank()) {
                Text(
                    stringResource(R.string.label_signed_up_on, dateLabel),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (invite.qualified) {
                Text(
                    stringResource(
                        if (invite.rewarded) R.string.label_plus_7_days_pro else R.string.label_referral_counted
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    stringResource(R.string.label_completed_transaction),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    stringResource(R.string.label_pending),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    stringResource(R.string.label_awaiting_first_tx),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun referralErrorRes(error: ReferralError): Int = when (error) {
    ReferralError.NotSignedIn -> R.string.error_refer_not_signed_in
    ReferralError.InvalidCode -> R.string.error_refer_invalid
    ReferralError.OwnCode -> R.string.error_refer_own_code
    ReferralError.AlreadyReferred -> R.string.error_refer_already
    ReferralError.Network -> R.string.error_refer_network
    ReferralError.Unknown -> R.string.error_refer_unknown
}
