package com.mknlabs.expensetracker.feature.settings.ui

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark

/**
 * Premium Upgrade Card replicated from the standalone mockup (android_premium_upgrade_card.html).
 * Features a frosted-glass surface, ambient purple backlight glow, specular sheen,
 * animated diagonal shimmer sweep, and active press micro-interaction.
 */
@Composable
fun PremiumUpgradeCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "premiumCardScale"
    )

    // 4.2-second infinite shine cycle with a 45% active sweep and 55% rest delay
    val infiniteTransition = rememberInfiniteTransition(label = "premiumCardShine")
    val shineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                0f at 0
                1f at 1890 using FastOutSlowInEasing
                1f at 4200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "premiumCardShineProgress"
    )

    val cardShape = RoundedCornerShape(28.dp)

    // Palette mapped 1:1 from android_premium_upgrade_card.html
    val cardBackgroundBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xCC1B0C2E),
                Color(0xBF11071F),
                Color(0xD90A0412)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xD9F3EAFF),
                Color(0xBFEAD9FF),
                Color(0xD9E1CAFF)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val borderColor = if (isDark) Color(0xB34A2678) else Color(0xCCC7A6F5)
    val badgeColor = if (isDark) Color(0xFF9B6CD4) else Color(0xFF7C3AED)
    val iconColor = if (isDark) Color(0xFFB582F2) else Color(0xFF6D28D9)
    val titleColor = if (isDark) Color(0xFFC99BFF) else Color(0xFF4C1D95)
    val subtitleColor = if (isDark) Color(0xFF9466CC) else Color(0xFF6B3FA0)
    val shineColor = if (isDark) Color(0x2BD8B4FE) else Color(0xA6FFFFFF)

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient soft purple backlight bloom behind the card
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0x409333EA),
                                Color(0x26A855F7),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                Color(0x59C084FC),
                                Color(0x33E879F9),
                                Color.Transparent
                            )
                        }
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
        )

        // Glass card container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 16.dp else 12.dp,
                    shape = cardShape,
                    spotColor = if (isDark) Color(0x99000000) else Color(0x336B21A8),
                    ambientColor = if (isDark) Color(0x66000000) else Color(0x1A6B21A8)
                )
                .clip(cardShape)
                .background(cardBackgroundBrush)
                .border(BorderStroke(1.dp, borderColor), cardShape)
                .drawWithContent {
                    drawContent()

                    // Top-left specular sheen overlay
                    val topSheenBrush = Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color(0x14D8B4FE) else Color(0x73FFFFFF),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.5f
                    )
                    drawRect(brush = topSheenBrush, size = size)

                    // Top-right subtle radial glass glint
                    val cornerGlintBrush = Brush.radialGradient(
                        colors = listOf(
                            if (isDark) Color(0x17C084FC) else Color(0x66FFFFFF),
                            Color.Transparent
                        ),
                        center = Offset(size.width, 0f),
                        radius = size.width * 0.45f
                    )
                    drawRect(brush = cornerGlintBrush, size = size)

                    // Animated diagonal shine sweep
                    if (shineProgress < 1f) {
                        val cardWidth = size.width
                        val cardHeight = size.height
                        val totalSweep = cardWidth * 3.5f
                        val currentX = -cardWidth * 1.5f + totalSweep * shineProgress
                        val beamWidth = cardWidth * 0.6f

                        val shineBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                shineColor,
                                Color.Transparent
                            ),
                            start = Offset(currentX, 0f),
                            end = Offset(currentX + beamWidth, cardHeight)
                        )
                        drawRect(brush = shineBrush, size = size)
                    }

                    // Top and bottom specular borders (reproducing mockup inset box-shadows)
                    val topBorderColor = if (isDark) Color(0x47D8B4FE) else Color(0xE6FFFFFF)
                    val bottomBorderColor = if (isDark) Color(0x14C084FC) else Color(0x1F9333EA)
                    drawLine(
                        color = topBorderColor,
                        start = Offset(0f, 0.5f),
                        end = Offset(size.width, 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = bottomBorderColor,
                        start = Offset(0f, size.height - 0.5f),
                        end = Offset(size.width, size.height - 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top row: "EXCLUSIVE" tag + Magic Wand Sparkle Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.badge_exclusive),
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.4.sp
                    )

                    MagicWandSparklesIcon(
                        color = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Title: "Upgrade to Pro"
                Text(
                    text = stringResource(R.string.label_upgrade_to_pro),
                    color = titleColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle Description
                Text(
                    text = stringResource(R.string.desc_upgrade_to_premium),
                    color = subtitleColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Minimalist outline magic wand with sparkles icon replicated 1:1 from the SVG in the mockup.
 */
@Composable
private fun MagicWandSparklesIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.6f.dp.toPx()
        val cap = StrokeCap.Round
        val scale = size.width / 24f

        // Wand stick: M3 21 L12 12
        drawLine(
            color = color,
            start = Offset(3f * scale, 21f * scale),
            end = Offset(12f * scale, 12f * scale),
            strokeWidth = strokeWidth,
            cap = cap
        )

        // Top vertical sparkle: M15 4 V2
        drawLine(
            color = color,
            start = Offset(15f * scale, 4f * scale),
            end = Offset(15f * scale, 2f * scale),
            strokeWidth = strokeWidth,
            cap = cap
        )

        // Bottom vertical sparkle: M15 16 V14
        drawLine(
            color = color,
            start = Offset(15f * scale, 16f * scale),
            end = Offset(15f * scale, 14f * scale),
            strokeWidth = strokeWidth,
            cap = cap
        )

        // Left horizontal sparkle: M8 9 H10
        drawLine(
            color = color,
            start = Offset(8f * scale, 9f * scale),
            end = Offset(10f * scale, 9f * scale),
            strokeWidth = strokeWidth,
            cap = cap
        )

        // Right horizontal sparkle: M20 9 H22
        drawLine(
            color = color,
            start = Offset(20f * scale, 9f * scale),
            end = Offset(22f * scale, 9f * scale),
            strokeWidth = strokeWidth,
            cap = cap
        )

        // Bottom-right diagonal sparkle: M17.8 11.8 L19 13
        drawLine(
            color = color,
            start = Offset(17.8f * scale, 11.8f * scale),
            end = Offset(19f * scale, 13f * scale),
            strokeWidth = strokeWidth,
            cap = cap
        )

        // Top-right diagonal sparkle: M17.8 6.2 L19 5
        drawLine(
            color = color,
            start = Offset(17.8f * scale, 6.2f * scale),
            end = Offset(19f * scale, 5f * scale),
            strokeWidth = strokeWidth,
            cap = cap
        )

        // Top-left diagonal sparkle: M12.2 6.2 L11 5
        drawLine(
            color = color,
            start = Offset(12.2f * scale, 6.2f * scale),
            end = Offset(11f * scale, 5f * scale),
            strokeWidth = strokeWidth,
            cap = cap
        )

        // Center sparkle node: M15 9
        drawCircle(
            color = color,
            radius = strokeWidth * 0.45f,
            center = Offset(15f * scale, 9f * scale)
        )
    }
}

@Preview(name = "Light Mode")
@Composable
private fun PremiumUpgradeCardLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .background(Color(0xFFF6F4FA))
                .padding(24.dp)
        ) {
            PremiumUpgradeCard(onClick = {})
        }
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PremiumUpgradeCardDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .background(Color(0xFF060606))
                .padding(24.dp)
        ) {
            PremiumUpgradeCard(onClick = {})
        }
    }
}
