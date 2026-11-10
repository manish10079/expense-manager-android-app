package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Camera
import com.adamglin.phosphoricons.regular.ChatCircle
import com.adamglin.phosphoricons.regular.Microphone
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.PencilSimple
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Lucide
import com.mknlabs.expensetracker.R
import kotlinx.coroutines.delay
import com.mknlabs.expensetracker.ai.TextParseOutcome
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.settingsHeroSweepCoral
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.sheet

@Composable
fun AddUnifiedSmartSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onClassicForm: () -> Unit,
    onVoice: () -> Unit,
    onScan: () -> Unit,
    onChatParsed: (String, List<AiParsedReviewItem>) -> Unit,
    extractViewModel: com.mknlabs.expensetracker.ai.AiTextExtractViewModel,
    initialChat: String = "",
    autoParse: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.isDark
    val sheetColor = if (dark) scheme.profileSheet else scheme.sheet
    val cardColor = if (dark) scheme.genderRow else Color(0xFFF1F5F9)
    val view = LocalView.current
    DisposableEffect(visible) {
        val window = (view.context as android.app.Activity).window
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val previous = window.isNavigationBarContrastEnforced
            if (visible) window.isNavigationBarContrastEnforced = false
            onDispose { window.isNavigationBarContrastEnforced = previous }
        } else {
            onDispose { }
        }
    }
    var chat by remember(visible, initialChat) { mutableStateOf(if (visible) initialChat else "") }
    var parsing by remember(visible) { mutableStateOf(false) }
    var parseErrorRes by remember(visible) { mutableStateOf<Int?>(null) }
    var autoParseStarted by remember(visible, autoParse, initialChat) { mutableStateOf(false) }
    var showAiPrivacy by remember { mutableStateOf(false) }
    val typeHints = listOf(
        stringResource(R.string.hint_ai_type_1),
        stringResource(R.string.hint_ai_type_2),
        stringResource(R.string.hint_ai_type_3),
        stringResource(R.string.hint_ai_type_4),
        stringResource(R.string.hint_ai_type_5),
        stringResource(R.string.hint_ai_type_6),
        stringResource(R.string.hint_ai_type_7),
        stringResource(R.string.hint_ai_type_8),
        stringResource(R.string.hint_ai_type_9),
    )
    val shuffledHints = remember(visible) { typeHints.shuffled() }
    var typedHint by remember { mutableStateOf("") }
    var chatFocused by remember { mutableStateOf(false) }
    LaunchedEffect(visible, chat.isEmpty(), chatFocused, shuffledHints) {
        if (!visible || chat.isNotEmpty() || chatFocused) {
            if (chat.isNotEmpty()) typedHint = ""
            return@LaunchedEffect
        }
        var i = 0
        while (true) {
            val phrase = shuffledHints[i % shuffledHints.size]
            typedHint = ""
            for (n in 1..phrase.length) {
                typedHint = phrase.take(n)
                delay(49)
            }
            delay(5000)
            i++
        }
    }
    LaunchedEffect(visible, autoParse, initialChat) {
        if (visible && autoParse && initialChat.isNotBlank() && !autoParseStarted) {
            autoParseStarted = true
            chat = initialChat
            submitChat(
                initialChat,
                extractViewModel,
                { parsing = it },
                { parseErrorRes = it },
                onChatParsed,
            )
        }
    }
    val cycle = rememberInfiniteTransition(label = "parse-cycle")
    val cycleAngle by cycle.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "parse-cycle-angle",
    )
    val sheetEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(220)),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (dark) 0.55f else 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .animateEnterExit(
                        enter = slideInVertically(animationSpec = tween(380, easing = sheetEasing)) { it },
                        exit = slideOutVertically(animationSpec = tween(300, easing = FastOutLinearInEasing)) { it },
                    )
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(sheetColor)
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {},
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (dark) Color(0xFF475569) else Color(0xFFCBD5E1))
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { chatFocused = it.isFocused },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_add_transaction),
                            color = scheme.onSurface,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        val quota by extractViewModel.parseQuota.collectAsState()
                        Text(
                            text = stringResource(
                                R.string.label_ai_parse_quota,
                                quota.used,
                                quota.limit,
                            ),
                            modifier = Modifier
                                .padding(start = 8.dp, end = 2.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(scheme.accentInk.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            color = scheme.accentInk,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showAiPrivacy = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.desc_ai_parse_privacy),
                                tint = scheme.accentInk.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    TextButton(
                        onClick = onClassicForm,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
                    ) {
                        Text(
                            stringResource(R.string.label_use_classic_form),
                            color = scheme.accentInk,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = chat,
                        onValueChange = { chat = it },
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        maxLines = 3,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = scheme.onSurface),
                        cursorBrush = SolidColor(scheme.cta),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                submitChat(
                                    chat,
                                    extractViewModel,
                                    { parsing = it },
                                    { parseErrorRes = it },
                                    onChatParsed,
                                )
                            },
                        ),
                        decorationBox = { inner ->
                            if (chat.isEmpty()) {
                                Text(
                                    typedHint,
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 15.sp,
                                    maxLines = 2,
                                )
                            }
                            inner()
                        },
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brandGradient(
                                    alpha = if (chat.isNotBlank() && !parsing) 1f else 0.45f,
                                ),
                            )
                            .clickable(enabled = chat.isNotBlank() && !parsing) {
                                submitChat(
                                    chat,
                                    extractViewModel,
                                    { parsing = it },
                                    { parseErrorRes = it },
                                    onChatParsed,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Lucide.ArrowRight,
                            contentDescription = null,
                            tint = scheme.onCta,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                if (parsing || (parseErrorRes != null && parseErrorRes != R.string.msg_ai_chat_daily_limit)) {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (dark) cardColor else Color(0xFFEEF2FF))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (parsing) {
                            Icon(
                                Icons.Outlined.Autorenew,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(20.dp)
                                    .graphicsLayer { rotationZ = cycleAngle },
                                tint = if (dark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                            )
                            Column(Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.label_parsing_ai_input),
                                        color = scheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                    )
                                    AiParsingDots(dark = dark)
                                }
                                Text(
                                    stringResource(R.string.msg_extracting_merchant),
                                    color = scheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(parseErrorRes!!),
                                color = scheme.settingsHeroSweepCoral,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
                Row(
                    Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SmartActionTile(
                        modifier = Modifier.weight(1f),
                        icon = PhosphorIcons.Regular.Microphone,
                        iconBg = Color(0xFF4F46E5),
                        tileBg = if (dark) cardColor else Color(0xFFEEF2FF),
                        title = stringResource(R.string.label_voice_mic),
                        subtitle = stringResource(R.string.desc_voice_mic),
                        onClick = onVoice,
                    )
                    SmartActionTile(
                        modifier = Modifier.weight(1f),
                        icon = PhosphorIcons.Regular.Camera,
                        iconBg = Color(0xFF7C3AED),
                        tileBg = if (dark) cardColor else Color(0xFFF5F3FF),
                        title = stringResource(R.string.label_scan_receipt),
                        subtitle = stringResource(R.string.desc_scan_receipt),
                        onClick = onScan,
                    )
                }
            }
        }
    }
    if (showAiPrivacy) {
        ModernConfirmationDialog(
            onDismissRequest = { showAiPrivacy = false },
            title = stringResource(R.string.title_ai_parse_privacy),
            text = stringResource(R.string.msg_ai_parse_privacy),
            confirmButtonText = stringResource(R.string.btn_got_it),
            onConfirm = { showAiPrivacy = false },
            headerIcon = Icons.Outlined.Info,
            showDismissButton = false,
        )
    }
    if (parseErrorRes == R.string.msg_ai_chat_daily_limit) {
        ModernConfirmationDialog(
            onDismissRequest = { parseErrorRes = null },
            title = stringResource(R.string.title_ai_daily_limit),
            text = stringResource(R.string.msg_ai_daily_limit),
            confirmButtonText = stringResource(R.string.label_close),
            onConfirm = { parseErrorRes = null },
            headerIcon = PhosphorIcons.Regular.Info,
            showDismissButton = false,
        )
    }
}

@Composable
fun AddQuickFloatingArc(
    visible: Boolean,
    onDismiss: () -> Unit,
    onVoice: () -> Unit,
    onScan: () -> Unit,
    onChat: () -> Unit,
    onManual: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.isDark
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + slideInVertically(tween(280)) { it / 4 },
        exit = fadeOut(tween(140)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (dark) 0.7f else 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 88.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ArcActionRow(
                    highlight = true,
                    icon = PhosphorIcons.Regular.Microphone,
                    iconTint = Color.White,
                    iconBg = Color.White.copy(0.2f),
                    title = stringResource(R.string.label_speak_transaction),
                    subtitle = stringResource(R.string.desc_speak_transaction),
                    onClick = onVoice,
                )
                ArcActionRow(
                    highlight = false,
                    icon = PhosphorIcons.Regular.Camera,
                    iconTint = Color(0xFF818CF8),
                    iconBg = if (dark) Color(0xFF312E81).copy(0.5f) else Color(0xFFEDE9FE),
                    title = stringResource(R.string.label_scan_receipt_pdf),
                    subtitle = stringResource(R.string.desc_scan_receipt_pdf),
                    onClick = onScan,
                )
                ArcActionRow(
                    highlight = false,
                    icon = PhosphorIcons.Regular.ChatCircle,
                    iconTint = Color(0xFF34D399),
                    iconBg = if (dark) Color(0xFF064E3B).copy(0.5f) else Color(0xFFD1FAE5),
                    title = stringResource(R.string.label_quick_chat_note),
                    subtitle = stringResource(R.string.desc_quick_chat_note),
                    onClick = onChat,
                )
                ArcActionRow(
                    highlight = false,
                    icon = PhosphorIcons.Regular.PencilSimple,
                    iconTint = Color(0xFFFBBF24),
                    iconBg = if (dark) Color(0xFF78350F).copy(0.5f) else Color(0xFFFEF3C7),
                    title = stringResource(R.string.label_manual_entry),
                    subtitle = stringResource(R.string.desc_manual_entry),
                    onClick = onManual,
                )
            }
        }
    }
}

@Composable
private fun SmartActionTile(
    modifier: Modifier,
    icon: ImageVector,
    iconBg: Color,
    tileBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tileBg)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ArcActionRow(
    highlight: Boolean,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.isDark
    val bg = if (highlight) {
        Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))
    } else {
        Brush.horizontalGradient(
            listOf(
                if (dark) Color(0xFF1E293B) else Color.White,
                if (dark) Color(0xFF1E293B) else Color.White,
            )
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .then(
                if (highlight) Modifier else Modifier.border(
                    1.dp,
                    if (dark) Color(0xFF334155) else Color(0xFFE2E8F0),
                    RoundedCornerShape(16.dp),
                )
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = if (highlight) Color.White else scheme.onSurface,
            )
            Text(
                subtitle,
                fontSize = 10.sp,
                color = if (highlight) Color.White.copy(0.85f) else scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AiParsingDots(dark: Boolean) {
    val pulse = rememberInfiniteTransition(label = "ai-dots")
    val progress by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ai-dots-progress",
    )
    val color = if (dark) Color(0xFF818CF8) else Color(0xFF4F46E5)
    val delays = floatArrayOf(0.1f, 0.3f, 0.5f)
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val phase = (progress - delays[index] + 1f) % 1f
            val wave = 0.5f - 0.5f * kotlin.math.cos((phase * 2f * Math.PI).toFloat())
            val scale = 0.7f + 0.5f * wave
            val alpha = 0.4f + 0.6f * wave
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

private fun submitChat(
    chat: String,
    extractViewModel: com.mknlabs.expensetracker.ai.AiTextExtractViewModel,
    setParsing: (Boolean) -> Unit,
    setParseError: (Int?) -> Unit,
    onChatParsed: (String, List<AiParsedReviewItem>) -> Unit,
) {
    val text = chat.trim()
    if (text.isEmpty()) return
    setParseError(null)
    setParsing(true)
    extractViewModel.parse(text) { outcome ->
        setParsing(false)
        when (outcome) {
            is TextParseOutcome.Review -> {
                setParseError(null)
                onChatParsed(text, outcome.items)
            }
            is TextParseOutcome.DailyLimit -> {
                setParseError(R.string.msg_ai_chat_daily_limit)
            }
            is TextParseOutcome.Failed -> {
                setParseError(R.string.msg_ai_chat_generic_error)
            }
            is TextParseOutcome.RawAdd -> {
                setParseError(R.string.msg_ai_chat_no_transactions)
            }
        }
    }
}
