package com.mknlabs.expensetracker.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.UpdateInfo
import com.mknlabs.expensetracker.ui.adaptive.AppWindowHeight
import com.mknlabs.expensetracker.ui.adaptive.AppWindowSize
import com.mknlabs.expensetracker.ui.adaptive.AppWindowInfo
import com.mknlabs.expensetracker.ui.adaptive.LocalAppWindowInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.PurplePrimary
import kotlinx.coroutines.launch

// ── Responsive helpers ─────────────────────────────────────────────────

/** Derived dimensions that adapt to screen size and orientation. */
private data class DialogMetrics(
    val maxWidth: Dp,
    val messageMaxHeight: Dp,
    val outerPadding: Dp,
    val innerPadding: Dp,
    val cornerRadius: Dp,
    val iconSize: Dp,
    val buttonSpacing: Dp,
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
            AppWindowSize.Medium    -> 24.dp
            AppWindowSize.Expanded  -> 28.dp
            else                    -> 32.dp
        }
        val innerPad = when {
            isLandscape -> 16.dp
            else -> outerPad
        }

        DialogMetrics(
            maxWidth = maxW,
            messageMaxHeight = msgMaxH,
            outerPadding = outerPad,
            innerPadding = innerPad,
            cornerRadius = when {
                windowInfo.width >= AppWindowSize.Expanded -> 24.dp
                else -> 20.dp
            },
            iconSize = when (windowInfo.width) {
                AppWindowSize.Compact -> 28.dp
                else -> 32.dp
            },
            buttonSpacing = 8.dp,
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
        onDismissRequest = if (force) ({}) else onLater,
        properties = DialogProperties(
            dismissOnBackPress = !force,
            dismissOnClickOutside = !force,
            usePlatformDefaultWidth = false,
        ),
        modifier = Modifier.width(metrics.maxWidth),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Rounded.RocketLaunch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(metrics.iconSize)
            )
        },
        title = {
            Text(
                text = titleText(info),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            MessageBody(
                info = info,
                maxHeight = metrics.messageMaxHeight
            )
        },
        confirmButton = {
            Button(onClick = onUpdateNow) {
                Text(stringResource(R.string.btn_update_now))
            }
        },
        dismissButton = {
            if (!force) {
                TextButton(onClick = onLater) {
                    Text(stringResource(R.string.label_later))
                }
            }
        },
        shape = RoundedCornerShape(metrics.cornerRadius)
    )
}

// ── Extracted content ──────────────────────────────────────────────────

@Composable
private fun MessageBody(
    info: UpdateInfo,
    maxHeight: Dp
) {
    val content = info.updateMessage.ifBlank { stringResource(R.string.msg_update_available) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            if (scrollState.canScrollForward) {
                Spacer(modifier = Modifier.height(28.dp))
            }
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

private fun titleText(info: UpdateInfo): String {
    return info.updateTitle.ifBlank {
        info.latestVersion.ifBlank { "Update Available" }
    }
}

// ── Previews ───────────────────────────────────────────────────────────

@Preview(name = "Compact portrait", showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun PreviewCompactPortrait() {
    PreviewWithWindowInfo(AppWindowInfo(AppWindowSize.Compact, AppWindowHeight.Expanded)) {
        PreviewDialogCard(force = false) {
            UpdateDialogContent(
                info = UpdateInfo(
                    latestVersion = "2.102.0",
                    updateTitle = "",
                    updateMessage = buildRemoteConfigMessage(),
                    forceUpdate = false
                )
            )
        }
    }
}

@Preview(name = "Compact landscape", showBackground = true, widthDp = 740, heightDp = 360)
@Composable
private fun PreviewCompactLandscape() {
    PreviewWithWindowInfo(AppWindowInfo(AppWindowSize.Compact, AppWindowHeight.Compact)) {
        PreviewDialogCard(force = false) {
            UpdateDialogContent(
                info = UpdateInfo(
                    latestVersion = "2.102.0",
                    updateTitle = "",
                    updateMessage = buildRemoteConfigMessage(),
                    forceUpdate = false
                )
            )
        }
    }
}

@Preview(name = "Tablet (medium)", showBackground = true, widthDp = 800, heightDp = 1200)
@Composable
private fun PreviewTablet() {
    PreviewWithWindowInfo(AppWindowInfo(AppWindowSize.Medium, AppWindowHeight.Expanded)) {
        PreviewDialogCard(force = false) {
            UpdateDialogContent(
                info = UpdateInfo(
                    latestVersion = "2.102.0",
                    updateTitle = "",
                    updateMessage = buildRemoteConfigMessage(),
                    forceUpdate = false
                )
            )
        }
    }
}

@Preview(name = "Forced update", showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun PreviewForced() {
    PreviewWithWindowInfo(AppWindowInfo(AppWindowSize.Compact, AppWindowHeight.Expanded)) {
        PreviewDialogCard(force = true) {
            UpdateDialogContent(
                info = UpdateInfo(
                    latestVersion = "3.0.0",
                    updateTitle = "Critical Security Update",
                    updateMessage = "Critical security update \u2014 please update now:\n\n" +
                        "\u2022 Fixed authentication token leak on background sync\n" +
                        "\u2022 End-to-end encryption for cloud backups\n" +
                        "\u2022 Patched biometric bypass on rooted devices\n" +
                        "\u2022 Removed deprecated Firebase SDK calls\n" +
                        "\u2022 Hardened PIN against brute-force attempts\n" +
                        "\u2022 Resolved crash on Samsung Galaxy S24 during export\n" +
                        "\u2022 Fixed duplicate notifications on Pixel 8 Pro\n" +
                        "\u2022 Improved offline mode reliability\n" +
                        "\u2022 Corrected currency formatting for JPY and KRW\n" +
                        "\u2022 Fixed dark theme contrast on Settings screen",
                    forceUpdate = true
                )
            )
        }
    }
}

// ── Preview helpers ────────────────────────────────────────────────────

@Composable
private fun PreviewWithWindowInfo(
    info: AppWindowInfo,
    content: @Composable () -> Unit
) {
    ExpenseTrackerTheme {
        CompositionLocalProvider(LocalAppWindowInfo provides info) {
            content()
        }
    }
}

@Composable
private fun UpdateDialogContent(info: UpdateInfo) {
    val scrollState = rememberScrollState()
    Icon(
        imageVector = Icons.Rounded.RocketLaunch,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = titleText(info),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 260.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = info.updateMessage.ifBlank { stringResource(R.string.msg_update_available) },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun buildRemoteConfigMessage(): String = listOf(
    "Hey there! We\u2019ve been busy making your expense tracking experience even better.",
    "",
    "Here\u2019s what\u2019s new:",
    "",
    "\uD83D\uDE80 PERFORMANCE",
    "  \u2022 Transactions now load 3x faster with Paging 3",
    "  \u2022 Smoother scrolling on the main dashboard",
    "",
    "\uD83D\uDCCA ANALYTICS",
    "  \u2022 New yearly spending overview chart",
    "  \u2022 Category breakdown now supports custom date ranges",
    "",
    "\uD83D\uDD27 BUG FIXES",
    "  \u2022 Fixed recurring transactions not firing on Mondays",
    "  \u2022 Resolved crash when exporting CSV with 1000+ entries",
    "  \u2022 Budget alerts now respect your notification settings",
    "",
    "Thanks for being a valued user! Your feedback helps us improve."
).joinToString("\n")

@Composable
private fun PreviewDialogCard(force: Boolean, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            content()
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!force) {
                    TextButton(onClick = {}) {
                        Text(stringResource(R.string.label_later))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(onClick = {}) {
                    Text(stringResource(R.string.btn_update_now))
                }
            }
        }
    }
}
