package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowHeight
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowInfo
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowSize
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.core.ui.theme.BackgroundLight
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.SurfaceLight
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.PurplePrimary
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.domain.models.UpdateConfigStatus
import com.mknlabs.expensetracker.domain.models.UpdateInfo
import com.mknlabs.expensetracker.utils.AppLocaleHelper
import com.mknlabs.expensetracker.utils.localizedRemoteConfigText
import kotlinx.coroutines.launch

// ── Responsive helpers ─────────────────────────────────────────────────

/** Derived dimensions that adapt to screen size and orientation. */
private data class DialogMetrics(
    val maxWidth: Dp,
    val subtitleMaxHeight: Dp,
    val messageMaxHeight: Dp,
    val outerPadding: Dp,
    val innerPadding: Dp,
    val cornerRadius: Dp,
    val iconSize: Dp,
    val buttonSpacing: Dp,
    val buttonCornerRadius: Dp,
)

@Composable
private fun rememberDialogMetrics(): DialogMetrics {
    val config = LocalConfiguration.current
    val windowInfo = LocalAppWindowInfo.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenH = config.screenHeightDp.dp
    val screenW = config.screenWidthDp.dp

    return remember(config.orientation, windowInfo.width, windowInfo.height) {
        // ── Max dialog width ────────────────────────────────────────
        val maxW = when (windowInfo.width) {
            AppWindowSize.Compact   -> (screenW * 0.92f).coerceAtMost(400.dp)
            AppWindowSize.Medium    -> (screenW * 0.70f).coerceAtMost(480.dp)
            AppWindowSize.Expanded  -> (screenW * 0.50f).coerceAtMost(520.dp)
            AppWindowSize.Large,
            AppWindowSize.ExtraLarge -> (screenW * 0.38f).coerceAtMost(560.dp)
        }

        // ── Max message height (percentage of screen) ───────────────
        val msgMaxH = when {
            isLandscape && windowInfo.height == AppWindowHeight.Compact ->
                (screenH * 0.45f).coerceIn(120.dp, 220.dp)
            windowInfo.height == AppWindowHeight.Compact ->
                (screenH * 0.40f).coerceIn(100.dp, 180.dp)
            else ->
                (screenH * 0.32f).coerceIn(140.dp, 300.dp)
        }

        // ── Padding scales with screen width ────────────────────────
        val outerPad = when (windowInfo.width) {
            AppWindowSize.Compact   -> 20.dp
            AppWindowSize.Medium    -> 16.dp
            AppWindowSize.Expanded  -> 28.dp
            else                    -> 32.dp
        }
        val innerPad = when {
            isLandscape -> 16.dp
            else -> outerPad
        }

        DialogMetrics(
            maxWidth = maxW,
            subtitleMaxHeight = (msgMaxH * 0.38f).coerceIn(64.dp, 112.dp),
            messageMaxHeight = msgMaxH,
            outerPadding = outerPad,
            innerPadding = innerPad,
            cornerRadius = when {
                windowInfo.width >= AppWindowSize.Expanded -> 8.dp
                else -> 8.dp
            },
            iconSize = when (windowInfo.width) {
                AppWindowSize.Compact -> 28.dp
                else -> 32.dp
            },
            buttonSpacing = 8.dp,
            buttonCornerRadius = 10.dp,
        )
    }
}

// ── Main composable ────────────────────────────────────────────────────

@Composable
fun UpdateDialog(
    info: UpdateInfo,
    force: Boolean,
    onUpdateNow: () -> Unit,
    onLater: () -> Unit
) {
    val metrics = rememberDialogMetrics()

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
        modifier = Modifier.width(metrics.maxWidth),
        containerColor = updateDialogContainerColor(),
        shape = RoundedCornerShape(metrics.cornerRadius),
        title = {
            UpdateHeader(info = info)
        },
        text = {
            val languageTag = currentAppLanguageTag()
            val subtitles = stringArrayResource(R.array.update_dialog_subtitles)
            val subtitle = remember(languageTag) { subtitles.random() }
            UpdateDialogContent(
                info = info,
                subtitle = subtitle,
                subtitleMaxHeight = metrics.subtitleMaxHeight,
                messageMaxHeight = metrics.messageMaxHeight
            )
        },
        confirmButton = {
            UpdateNowButton(
                onClick = onUpdateNow,
                shape = RoundedCornerShape(metrics.buttonCornerRadius)
            )
        },
        dismissButton = {
            if (!force) {
                AppTextButton(
                    onClick = onLater,
                    shape = RoundedCornerShape(metrics.buttonCornerRadius)
                ) {
                    Text(stringResource(R.string.label_later))
                }
            }
        }
    )
}

@Composable
private fun UpdateHeader(
    info: UpdateInfo
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedRocketBadge()

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.title_update_available),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            )

            val rawVersion = info.latestVersion.trim()
            val versionLabel = if (rawVersion.isBlank()) {
                stringResource(R.string.msg_update_version_unavailable)
            } else {
                stringResource(
                    R.string.badge_version_format,
                    rawVersion.removePrefix("v").removePrefix("V")
                )
            }

            val isDark = isSystemInDarkTheme()
            val badgeInk = MaterialTheme.colorScheme.accentInk
            val badgeBg = if (isDark) {
                badgeInk.copy(alpha = 0.15f)
            } else {
                badgeInk.copy(alpha = 0.08f)
            }
            val badgeBorder = if (isDark) {
                badgeInk.copy(alpha = 0.25f)
            } else {
                badgeInk.copy(alpha = 0.18f)
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeBg,
                border = BorderStroke(1.dp, badgeBorder)
            ) {
                Text(
                    text = versionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeInk,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Renders the brand-gradient rocket icon badge with vertical high-speed flight physics:
 * - Rocket rotated -45° to fly vertically straight up
 * - Continuous upward thruster surge and high-speed engine rumble
 * - Pulsing radial afterburner flame jet underneath the nozzle
 */
@Composable
private fun AnimatedRocketBadge(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rocketFlightAnimation")

    // Vertical surge (power cycle surging straight upwards)
    val surge by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rocketSurge"
    )

    // High-frequency engine rumble / thruster vibration
    val rumble by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 65, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rocketRumble"
    )

    // Thruster afterburner flame pulse (rapid flicker at the nozzle)
    val afterburnerScale by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 90, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "afterburnerScale"
    )
    val afterburnerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 110, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "afterburnerAlpha"
    )

    val surgeOffsetY = -surge * 2.8f
    val rumbleOffsetX = rumble * 0.75f
    val rumbleOffsetY = rumble * 0.45f
    val totalOffsetX = rumbleOffsetX
    val totalOffsetY = surgeOffsetY + rumbleOffsetY
    val rotation = -45f + (rumble * 1.6f)

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush = brandGradient()),
        contentAlignment = Alignment.Center
    ) {
        // Thruster exhaust flame jet directly underneath the rocket nozzle
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(
                    x = totalOffsetX.dp,
                    y = 9.dp + (totalOffsetY * 0.75f).dp
                )
                .size(16.dp)
                .graphicsLayer {
                    scaleX = afterburnerScale * 0.85f
                    scaleY = afterburnerScale * 1.25f
                    alpha = afterburnerAlpha
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD54F),
                            Color(0xFFFF7043).copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Rocket launch icon pointing vertically straight UP
        Icon(
            imageVector = Icons.Rounded.RocketLaunch,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBrandGradient,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    translationX = totalOffsetX.dp.toPx()
                    translationY = totalOffsetY.dp.toPx()
                    rotationZ = rotation
                }
        )
    }
}

@Composable
private fun UpdateNowButton(
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    Button(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onCta
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.defaultMinSize(minHeight = 40.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(brush = brandGradient())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_update_now),
                    color = MaterialTheme.colorScheme.onCta,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onCta,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Extracted content ──────────────────────────────────────────────────

@Composable
private fun updateDialogContainerColor(): Color {
    return if (MaterialTheme.colorScheme.isDark) {
        Color(0xFF101019)
    } else {
        BackgroundLight
    }
}

@Composable
private fun updateNotesCardColors(): Pair<Color, Color> {
    val isDark = MaterialTheme.colorScheme.isDark
    val cardBg = if (isDark) {
        Color(0xFF1E293B).copy(alpha = 0.45f)
    } else {
        SurfaceLight
    }
    val cardBorder = if (isDark) {
        Color(0xFF334155).copy(alpha = 0.5f)
    } else {
        Color(0xFFE2E8F0).copy(alpha = 0.8f)
    }
    return cardBg to cardBorder
}

private fun parseBulletPoints(content: String): List<String> {
    val lines = content.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val filtered = if (lines.isNotEmpty() && !lines.first().startsWith("•") && !lines.first().startsWith("-") && !lines.first().startsWith("*")) {
        lines.drop(1)
    } else {
        lines
    }

    return filtered.map { line ->
        line.removePrefix("•").removePrefix("-").removePrefix("*").trim()
    }.filter { it.isNotBlank() }
}

@Composable
private fun UpdateDialogContent(
    info: UpdateInfo,
    subtitle: String,
    subtitleMaxHeight: Dp,
    messageMaxHeight: Dp
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (subtitle.isNotBlank()) {
            UpdateScrollCard(maxHeight = subtitleMaxHeight) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Text(
            text = stringResource(R.string.label_whats_new).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        MessageBody(
            info = info,
            maxHeight = messageMaxHeight
        )
    }
}

@Composable
private fun UpdateScrollCard(
    maxHeight: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val (cardBg, cardBorder) = updateNotesCardColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .verticalScroll(scrollState)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }

        if (scrollState.canScrollForward) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .background(PurplePrimary, CircleShape)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = stringResource(R.string.label_show_more),
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageBody(
    info: UpdateInfo,
    maxHeight: Dp
) {
    val content = localizedUpdateMessage(info)
    val bulletPoints = remember(content) { parseBulletPoints(content) }

    UpdateScrollCard(maxHeight = maxHeight) {
        if (bulletPoints.isEmpty()) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        } else {
            bulletPoints.forEach { point ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.accentInk,
                        modifier = Modifier.padding(top = 1.dp)
                    )

                    val delim = if (point.contains(":")) ":" else if (point.contains("：")) "：" else null
                    if (delim != null) {
                        val title = point.substringBefore(delim).trim()
                        val desc = point.substringAfter(delim).trim()
                        val annotated = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                append(title)
                                append(": ")
                            }
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                append(desc)
                            }
                        }
                        Text(
                            text = annotated,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    } else {
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun currentAppLanguageTag(): String {
    AppLocaleHelper.appliedLanguageTags
    return AppLocaleHelper.getCurrentLanguageTag()
}

@Composable
private fun localizedUpdateMessage(info: UpdateInfo): String {
    val lang = currentAppLanguageTag()
    return when (info.configStatus) {
        UpdateConfigStatus.Offline -> stringResource(R.string.msg_update_need_internet)
        UpdateConfigStatus.Unknown, UpdateConfigStatus.Loading ->
            stringResource(R.string.msg_update_need_internet)
        UpdateConfigStatus.Failed, UpdateConfigStatus.Fetched ->
            localizedRemoteConfigText(
                raw = info.updateMessage,
                languageTag = lang,
                nestedField = "message",
                fallback = stringResource(R.string.msg_update_notes_error),
                requireLocalizedJson = true
            )
    }
}

private fun buildRemoteConfigMessage(): String = listOf(
    "What's New in v2.4.0:",
    "• Performance Upgrade: Transactions now load 3x faster with Paging 3. Smoother scrolling on the main dashboard",
    "• Enhanced Analytics: New yearly spending overview chart. Category breakdown supports custom date ranges"
).joinToString("\n")

// ── Real Device Preview Infrastructure ─────────────────────────────────

/**
 * Renders the update dialog floating card with exact real-device dimensions,
 * elevation, colors, and button layout.
 */
@Composable
private fun UpdateDialogCardSurface(
    info: UpdateInfo,
    force: Boolean,
    onUpdateNow: () -> Unit,
    onLater: () -> Unit,
    metrics: DialogMetrics,
    subtitle: String = stringArrayResource(R.array.update_dialog_subtitles).first()
) {
    Surface(
        modifier = Modifier
            .width(metrics.maxWidth)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(metrics.cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(metrics.cornerRadius),
        color = updateDialogContainerColor(),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            UpdateHeader(info = info)
            Spacer(modifier = Modifier.height(16.dp))
            UpdateDialogContent(
                info = info,
                subtitle = subtitle,
                subtitleMaxHeight = metrics.subtitleMaxHeight,
                messageMaxHeight = metrics.messageMaxHeight
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!force) {
                    AppTextButton(
                        onClick = onLater,
                        shape = RoundedCornerShape(metrics.buttonCornerRadius)
                    ) {
                        Text(stringResource(R.string.label_later))
                    }
                    Spacer(modifier = Modifier.width(metrics.buttonSpacing))
                }
                UpdateNowButton(
                    onClick = onUpdateNow,
                    shape = RoundedCornerShape(metrics.buttonCornerRadius)
                )
            }
        }
    }
}

/**
 * Replicates the actual app screen behind the update dialog, so the preview
 * accurately mirrors what a user sees when the dialog pops up over the app.
 */
@Composable
private fun SimulatedAppBackground() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "October 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Expense Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        // Total Balance Hero Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$4,250.00",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Recent transactions list placeholders
        repeat(5) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {}
        }
    }
}

/**
 * Device-framed canvas: app dashboard in the background, darkened by a 55% scrim overlay,
 * with the update dialog card centered in foreground.
 */
@Composable
private fun RealDevicePreviewScaffold(
    darkTheme: Boolean,
    windowSize: AppWindowSize = AppWindowSize.Compact,
    windowHeight: AppWindowHeight = AppWindowHeight.Expanded,
    content: @Composable (DialogMetrics) -> Unit
) {
    ExpenseTrackerTheme(darkTheme = darkTheme) {
        val windowInfo = AppWindowInfo(windowSize, windowHeight)
        CompositionLocalProvider(LocalAppWindowInfo provides windowInfo) {
            val metrics = rememberDialogMetrics()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Themed app background
                SimulatedAppBackground()

                // Dialog Scrim (55% black opacity, exactly how AlertDialog dims background on Android)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    content(metrics)
                }
            }
        }
    }
}

// ── Device Previews ────────────────────────────────────────────────────

@Preview(
    name = "Real Device — Light Mode",
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_8"
)
@Composable
private fun PreviewUpdateDialogDeviceLight() {
    RealDevicePreviewScaffold(darkTheme = false) { metrics ->
        UpdateDialogCardSurface(
            info = UpdateInfo(
                updateMessage = buildRemoteConfigMessage(),
                forceUpdate = false,
                latestVersion = "2.4.0"
            ),
            force = false,
            onUpdateNow = {},
            onLater = {},
            metrics = metrics,
            subtitle = "Faster performance. Same disappearing act for your money."
        )
    }
}

@Preview(
    name = "Real Device — Dark Mode",
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_8",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewUpdateDialogDeviceDark() {
    RealDevicePreviewScaffold(darkTheme = true) { metrics ->
        UpdateDialogCardSurface(
            info = UpdateInfo(
                updateMessage = buildRemoteConfigMessage(),
                forceUpdate = false,
                latestVersion = "2.4.0"
            ),
            force = false,
            onUpdateNow = {},
            onLater = {},
            metrics = metrics,
            subtitle = "Faster performance. Same disappearing act for your money."
        )
    }
}

@Preview(
    name = "Real Device — Forced Light",
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_8"
)
@Composable
private fun PreviewUpdateDialogForcedLight() {
    RealDevicePreviewScaffold(darkTheme = false) { metrics ->
        UpdateDialogCardSurface(
            info = UpdateInfo(
                updateMessage = buildRemoteConfigMessage(),
                forceUpdate = true,
                latestVersion = "2.4.0"
            ),
            force = true,
            onUpdateNow = {},
            onLater = {},
            metrics = metrics,
            subtitle = "Built by one dev who also struggles to stick to a budget."
        )
    }
}

@Preview(
    name = "Real Device — Forced Dark",
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_8",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewUpdateDialogForcedDark() {
    RealDevicePreviewScaffold(darkTheme = true) { metrics ->
        UpdateDialogCardSurface(
            info = UpdateInfo(
                updateMessage = buildRemoteConfigMessage(),
                forceUpdate = true,
                latestVersion = "2.4.0"
            ),
            force = true,
            onUpdateNow = {},
            onLater = {},
            metrics = metrics,
            subtitle = "Built by one dev who also struggles to stick to a budget."
        )
    }
}

@Preview(
    name = "Real Device — Landscape",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=891dp,height=411dp,orientation=landscape"
)
@Composable
private fun PreviewUpdateDialogLandscape() {
    RealDevicePreviewScaffold(
        darkTheme = false,
        windowSize = AppWindowSize.Expanded,
        windowHeight = AppWindowHeight.Compact
    ) { metrics ->
        UpdateDialogCardSurface(
            info = UpdateInfo(
                updateMessage = buildRemoteConfigMessage(),
                forceUpdate = false,
                latestVersion = "2.4.0"
            ),
            force = false,
            onUpdateNow = {},
            onLater = {},
            metrics = metrics,
            subtitle = "Faster performance. Same disappearing act for your money."
        )
    }
}

@Preview(
    name = "Real Device — Tablet",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
@Composable
private fun PreviewUpdateDialogTablet() {
    RealDevicePreviewScaffold(
        darkTheme = true,
        windowSize = AppWindowSize.Large,
        windowHeight = AppWindowHeight.Expanded
    ) { metrics ->
        UpdateDialogCardSurface(
            info = UpdateInfo(
                updateMessage = buildRemoteConfigMessage(),
                forceUpdate = false,
                latestVersion = "2.4.0"
            ),
            force = false,
            onUpdateNow = {},
            onLater = {},
            metrics = metrics,
            subtitle = "Faster performance. Same disappearing act for your money."
        )
    }
}
