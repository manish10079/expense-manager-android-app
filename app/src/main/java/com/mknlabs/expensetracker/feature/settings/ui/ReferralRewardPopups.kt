package com.mknlabs.expensetracker.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Gift
import com.adamglin.phosphoricons.regular.Lightning
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.X
import androidx.compose.foundation.layout.PaddingValues
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk

private val PopupScrimCard = Color(0xEB0F172A)
private val PopupAmber = Color(0xFFF59E0B)
private val PopupEmerald = Color(0xFF34D399)
private val PopupTeal = Color(0xFF2DD4BF)
private val PopupBrand = Color(0xFF6366F1)
private val PopupSlate950 = Color(0xFF020617)
private val PopupSlate800 = Color(0xFF1E293B)
private val PopupSlate300 = Color(0xFFCBD5E1)
private val PopupSlate400 = Color(0xFF94A3B8)
private val PopupWhite = Color(0xFFF8FAFC)
private val cardShape = RoundedCornerShape(36.dp)

@Composable
internal fun ReferralTestPopupBar(
    onRefereeClick: () -> Unit,
    onReferrerClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0x33F59E0B),
                        Color(0x336366F1),
                        Color(0x3310B981)
                    )
                )
            )
            .border(1.dp, Color(0x4DF59E0B), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.label_test_popups),
            color = Color(0xFFFDE68A),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(PopupAmber)
                .clickable(onClick = onRefereeClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                stringResource(R.string.btn_test_referee_code),
                color = Color(0xFF020617),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF34D399))
                .clickable(onClick = onReferrerClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                stringResource(R.string.btn_test_first_txn),
                color = Color(0xFF020617),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class BurstParticle(
    val startXRatio: Float,
    val startYRatio: Float,
    val burstVxDp: Float,
    val burstVyDp: Float,
    val sizeDp: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

private data class FallParticle(
    val startXRatio: Float,
    val startYRatio: Float,
    val fallSpeedDp: Float,
    val swaySpeed: Float,
    val sizeDp: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
private fun LoopingConfettiCanvas(
    modifier: Modifier = Modifier
) {
    val palette = remember {
        listOf(
            Color(0xFFF59E0B), // Amber
            Color(0xFF34D399), // Emerald
            Color(0xFF9333EA), // Purple
            Color(0xFF2DD4BF), // Teal
            Color(0xFFEC4899), // Pink
            Color(0xFFFBBF24), // Gold
            Color(0xFF6366F1)  // Indigo
        )
    }

    val burstParticles = remember {
        List(55) { index ->
            val angleRad = (Math.PI * (-145.0 + (index % 18) * 6.0)) / 180.0
            val burstSpeed = 240f + (index % 8) * 45f
            BurstParticle(
                startXRatio = 0.5f + (if (index % 2 == 0) 1f else -1f) * ((index % 6) * 0.03f),
                startYRatio = 0.55f,
                burstVxDp = (Math.cos(angleRad) * burstSpeed).toFloat(),
                burstVyDp = (Math.sin(angleRad) * burstSpeed).toFloat(),
                sizeDp = 5f + (index % 6) * 2f,
                color = palette[index % palette.size],
                rotationSpeed = (if (index % 2 == 0) 2.5f else -2.5f) * (1f + (index % 4) * 0.5f),
                isCircle = index % 3 == 0
            )
        }
    }

    val fallParticles = remember {
        List(35) { index ->
            FallParticle(
                startXRatio = ((index * 0.08f + (index % 5) * 0.12f)) % 1.0f,
                startYRatio = -((index % 12) * 0.09f + 0.05f),
                fallSpeedDp = 75f + (index % 5) * 25f,
                swaySpeed = 1.8f + (index % 4) * 0.4f,
                sizeDp = 4f + (index % 5) * 2f,
                color = palette[(index + 3) % palette.size],
                rotationSpeed = (if (index % 2 == 0) 1.5f else -1.5f) * (1f + (index % 3) * 0.4f),
                isCircle = index % 3 == 0
            )
        }
    }

    var timeSeconds by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var startFrameNanos = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (startFrameNanos == 0L) startFrameNanos = frameTimeNanos
                timeSeconds = (frameTimeNanos - startFrameNanos) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        if (w <= 0f || h <= 0f) return@Canvas

        val burstDuration = 1.8f
        val gravityPx = 400.dp.toPx()

        if (timeSeconds < burstDuration) {
            val t = timeSeconds
            val fadeAlpha = ((1.0f - (t / burstDuration)) * 0.9f).coerceIn(0f, 0.9f)

            burstParticles.forEachIndexed { _, p ->
                val vx = p.burstVxDp.dp.toPx()
                val vy = p.burstVyDp.dp.toPx()
                val startX = p.startXRatio * w
                val startY = p.startYRatio * h

                val currentX = startX + vx * t
                val currentY = startY + vy * t + 0.5f * gravityPx * t * t
                val currentRotation = t * p.rotationSpeed * 120f

                rotate(currentRotation, pivot = Offset(currentX, currentY)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color,
                            radius = p.sizeDp.dp.toPx() / 2f,
                            center = Offset(currentX, currentY),
                            alpha = fadeAlpha
                        )
                    } else {
                        val pSize = p.sizeDp.dp.toPx()
                        drawRect(
                            color = p.color,
                            topLeft = Offset(currentX - pSize / 2f, currentY - pSize / 2f),
                            size = Size(pSize, pSize * 1.6f),
                            alpha = fadeAlpha
                        )
                    }
                }
            }
        } else {
            val loopTime = timeSeconds - burstDuration

            fallParticles.forEachIndexed { i, p ->
                val fallSpeedPx = p.fallSpeedDp.dp.toPx()
                val screenHeightWithPadding = h + 50.dp.toPx()
                val rawY = (p.startYRatio * h + loopTime * fallSpeedPx) % screenHeightWithPadding
                val currentY = rawY - 25.dp.toPx()

                val sway = Math.sin((loopTime * p.swaySpeed + i).toDouble()).toFloat() * 14.dp.toPx()
                val currentX = (p.startXRatio * w + sway) % w
                val currentRotation = loopTime * p.rotationSpeed * 90f

                rotate(currentRotation, pivot = Offset(currentX, currentY)) {
                    if (p.isCircle) {
                        drawCircle(
                            color = p.color,
                            radius = p.sizeDp.dp.toPx() / 2f,
                            center = Offset(currentX, currentY),
                            alpha = 0.8f
                        )
                    } else {
                        val pSize = p.sizeDp.dp.toPx()
                        drawRect(
                            color = p.color,
                            topLeft = Offset(currentX - pSize / 2f, currentY - pSize / 2f),
                            size = Size(pSize, pSize * 1.6f),
                            alpha = 0.8f
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun RefereeCodeAcceptedCard(
    validUntilLabel: String,
    onDismiss: () -> Unit,
    onExplore: () -> Unit,
    onReferMore: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "crownFloatAnim")
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffsetY"
    )
    val tiltRotationZ by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiltRotationZ"
    )

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = cardShape,
        colors = AppCardDefaults.colors(),
        contentPadding = PaddingValues(24.dp)
    ) {
        LoopingConfettiCanvas(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    translationY = floatOffsetY.dp.toPx()
                    rotationZ = tiltRotationZ
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFF9333EA)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(PhosphorIcons.Regular.Crown, null, tint = Color(0xFFFEF3C7), modifier = Modifier.size(36.dp))
                }
                Text(
                    stringResource(R.string.label_free_badge),
                    color = PopupSlate950,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(PopupEmerald)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.label_code_accepted),
                color = PopupAmber,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PopupAmber.copy(alpha = 0.15f))
                    .border(1.dp, PopupAmber.copy(alpha = 0.3f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.title_you_unlocked_7_days),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.msg_referral_code_applied_pro),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                ProPerkRow(stringResource(R.string.label_pro_ai_tools))
                Spacer(Modifier.height(8.dp))
                ProPerkRow(stringResource(R.string.label_pro_ad_free))
                Spacer(Modifier.height(8.dp))
                ProPerkRow(stringResource(R.string.label_pro_priority_themes))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.accentInk.copy(alpha = 0.15f))
                    .border(1.dp, MaterialTheme.colorScheme.accentInk.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(PhosphorIcons.Regular.Clock, null, tint = MaterialTheme.colorScheme.accentInk, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.label_valid_until), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text(validUntilLabel, color = MaterialTheme.colorScheme.accentInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFD97706))))
                    .clickable(onClick = onExplore)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.btn_start_exploring_pro),
                    color = PopupSlate950,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
            Text(
                stringResource(R.string.btn_want_more_refer),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onReferMore)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
internal fun RefereeCodeAcceptedDialog(
    validUntilLabel: String,
    onDismiss: () -> Unit,
    onExplore: () -> Unit,
    onReferMore: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        RefereeCodeAcceptedCard(
            validUntilLabel = validUntilLabel,
            onDismiss = onDismiss,
            onExplore = onExplore,
            onReferMore = onReferMore
        )
    }
}

@Composable
internal fun ReferrerFirstTxnCard(
    friendName: String,
    claimedSlots: Int,
    onDismiss: () -> Unit,
    onClaimed: () -> Unit,
    onInviteMore: () -> Unit
) {
    val earned = claimedSlots.coerceIn(1, 4)
    val daysLeft = (4 - earned) * 4
    val infiniteTransition = rememberInfiniteTransition(label = "giftFloatAnim")
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffsetY"
    )
    val tiltRotationZ by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiltRotationZ"
    )

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = cardShape,
        colors = AppCardDefaults.colors(),
        contentPadding = PaddingValues(24.dp)
    ) {
        LoopingConfettiCanvas(modifier = Modifier.matchParentSize())
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.graphicsLayer {
                    translationY = floatOffsetY.dp.toPx()
                    rotationZ = tiltRotationZ
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF10B981), PopupTeal, PopupBrand))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(PhosphorIcons.Regular.Gift, null, tint = Color(0xFFD1FAE5), modifier = Modifier.size(36.dp))
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 12.dp, y = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, PopupEmerald, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(PhosphorIcons.Regular.CheckCircle, null, tint = PopupEmerald, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.label_first_txn_done),
                        color = PopupEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.label_referral_completed),
                color = PopupEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x1A10B981))
                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.title_you_earned_7_days),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.msg_friend_registered_first_tx, friendName),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.label_monthly_rewards_progress),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(R.string.label_rewards_fraction, earned, 4),
                        color = PopupEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(4) { index ->
                        val claimed = index < earned
                        val latest = index == earned - 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        latest -> Color(0x4D10B981)
                                        claimed -> Color(0x3310B981)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    }
                                )
                                .border(
                                    if (latest) 2.dp else 1.dp,
                                    when {
                                        latest -> PopupEmerald
                                        claimed -> Color(0x6610B981)
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    },
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    if (claimed) PhosphorIcons.Regular.Check else PhosphorIcons.Regular.Lock,
                                    null,
                                    tint = if (claimed) PopupEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    if (latest) stringResource(R.string.label_claimed)
                                    else if (claimed) stringResource(R.string.label_7_days_slot)
                                    else stringResource(R.string.label_plus_days, 4),
                                    color = if (claimed) PopupEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x1A10B981))
                    .border(1.dp, Color(0x4D10B981), RoundedCornerShape(16.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(PhosphorIcons.Regular.Lightning, null, tint = PopupAmber, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.msg_7_days_pro_auto_activated),
                    color = PopupEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF34D399), PopupTeal, PopupBrand)))
                    .clickable(onClick = onClaimed)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(PhosphorIcons.Regular.CheckCircle, null, tint = PopupSlate950, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.btn_7_days_pro_claimed),
                        color = PopupSlate950,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
            Text(
                stringResource(R.string.btn_invite_more_days_left, daysLeft),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onInviteMore)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
internal fun ReferrerFirstTxnDialog(
    friendName: String,
    claimedSlots: Int,
    onDismiss: () -> Unit,
    onClaimed: () -> Unit,
    onInviteMore: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ReferrerFirstTxnCard(
            friendName = friendName,
            claimedSlots = claimedSlots,
            onDismiss = onDismiss,
            onClaimed = onClaimed,
            onInviteMore = onInviteMore
        )
    }
}

@Composable
private fun ProPerkRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(PopupEmerald.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(PhosphorIcons.Regular.Check, null, tint = PopupEmerald, modifier = Modifier.size(10.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(name = "Referee Code Accepted Card - Light", showBackground = true)
@Composable
internal fun RefereeCodeAcceptedCardPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        RefereeCodeAcceptedCard(
            validUntilLabel = "Oct 16, 2026",
            onDismiss = {},
            onExplore = {},
            onReferMore = {}
        )
    }
}

@Preview(name = "Referee Code Accepted Card - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun RefereeCodeAcceptedCardDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        RefereeCodeAcceptedCard(
            validUntilLabel = "Oct 16, 2026",
            onDismiss = {},
            onExplore = {},
            onReferMore = {}
        )
    }
}

@Preview(name = "Referrer First Txn Card - Light", showBackground = true)
@Composable
internal fun ReferrerFirstTxnCardPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        ReferrerFirstTxnCard(
            friendName = "Alex",
            claimedSlots = 1,
            onDismiss = {},
            onClaimed = {},
            onInviteMore = {}
        )
    }
}

@Preview(name = "Referrer First Txn Card - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ReferrerFirstTxnCardDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        ReferrerFirstTxnCard(
            friendName = "Alex",
            claimedSlots = 1,
            onDismiss = {},
            onClaimed = {},
            onInviteMore = {}
        )
    }
}
