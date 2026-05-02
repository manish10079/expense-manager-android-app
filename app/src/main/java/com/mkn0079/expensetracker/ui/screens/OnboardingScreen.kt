package com.mkn0079.expensetracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.brandGradient
import com.mkn0079.expensetracker.ui.theme.surfaceGradient

private data class OnboardingPage(
    val title: String,
    val description: String,
    val actionLabel: String,
    val accentedText: String? = null,
    val titleFontSize: TextUnit = 42.sp,
    val titleLineHeight: TextUnit = 48.sp,
    val supportingContent: (@Composable () -> Unit)? = null,
    val illustration: @Composable BoxScope.() -> Unit
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Track Expenses\nEasily",
        description = "Log your daily spending in seconds with our intuitive, high-speed interface.",
        actionLabel = "Next",
        illustration = { ExpenseCardIllustration() }
    ),
    OnboardingPage(
        title = "Secure & Private",
        description = "Your financial data is encrypted and protected by bank-grade biometric security.",
        actionLabel = "Next",
        illustration = { SecureTrackerIllustration() }
    ),
    OnboardingPage(
        title = "Visual Analytics",
        description = "Gain deep insights into your financial habits with high-fidelity charts and automated reports.",
        actionLabel = "Next",
        illustration = { AnalyticsIllustration() }
    ),
    OnboardingPage(
        title = "Premium by Design,\nPrivate by Nature",
        description = "Every feature unlocked. No paywalls, no subscriptions. Fully offline, your data never leaves your device.",
        actionLabel = "Get Started",
        accentedText = "Private by Nature",
        titleFontSize = 34.sp,
        titleLineHeight = 40.sp,
        supportingContent = { PremiumBenefitCards() },
        illustration = { PremiumPrivacyIllustration() }
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit = {}
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = onboardingPages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AmbientBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, bottom = 16.dp)
        ) {
            BrandBar()

            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    label = "onboarding_illustration"
                ) { pageIndex ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(390.dp)
                    ) {
                        onboardingPages[pageIndex].illustration(this)
                    }
                }
            }

            AnimatedContent(
                targetState = currentPage,
                label = "onboarding_copy"
            ) { pageIndex ->
                val current = onboardingPages[pageIndex]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = buildAnnotatedString {
                            if (current.accentedText.isNullOrEmpty() || !current.title.contains(current.accentedText)) {
                                append(current.title)
                            } else {
                                val accentStart = current.title.indexOf(current.accentedText)
                                append(current.title.substring(0, accentStart))
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                    append(current.accentedText)
                                }
                                append(current.title.substring(accentStart + current.accentedText.length))
                            }
                        },
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = current.titleFontSize,
                            lineHeight = current.titleLineHeight
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = current.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            lineHeight = 27.sp
                        ),
                        modifier = Modifier.fillMaxWidth(0.88f)
                    )

                    current.supportingContent?.let { content ->
                        Spacer(modifier = Modifier.height(28.dp))
                        content()
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (page.supportingContent == null) 34.dp else 24.dp))

            PrimaryOnboardingButton(
                label = page.actionLabel,
                onClick = {
                    if (currentPage == onboardingPages.lastIndex) {
                        onFinish()
                    } else {
                        currentPage += 1
                    }
                }
            )

            Spacer(modifier = Modifier.height(26.dp))

            BottomControls(
                pageCount = onboardingPages.size,
                currentPage = currentPage,
                onPreviousClick = {
                    if (currentPage > 0) {
                        currentPage -= 1
                    }
                },
                onSkipClick = onFinish
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BoxScope.AmbientBackdrop() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        MaterialTheme.colorScheme.background
                    ),
                    center = Offset(0.5f, 0.38f),
                    radius = 1200f
                )
            )
    )
}

@Composable
private fun BrandBar(
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Expense Tracker",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "EXPENSE TRACKER",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                fontSize = 21.sp
            )
        )
    }
}

@Composable
private fun BottomControls(
    pageCount: Int,
    currentPage: Int,
    onPreviousClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "PREV",
            color = if (currentPage == 0) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                fontSize = 14.sp
            ),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .alpha(if (currentPage == 0) 0.45f else 1f)
                .clickable(enabled = currentPage > 0, onClick = onPreviousClick)
                .padding(vertical = 12.dp)
        )

        PageIndicator(
            pageCount = pageCount,
            currentPage = currentPage,
            modifier = Modifier.align(Alignment.Center)
        )

        Text(
            text = "SKIP",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                fontSize = 14.sp
            ),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable(onClick = onSkipClick)
                .padding(vertical = 12.dp)
        )
    }
}

@Composable
private fun PrimaryOnboardingButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .shadow(
                elevation = 34.dp,
                shape = RoundedCornerShape(999.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
            ),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = brandGradient(),
                    shape = RoundedCornerShape(999.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val dotScale by animateFloatAsState(
                targetValue = if (selected) 1.2f else 0.85f,
                animationSpec = tween(220),
                label = "onboarding_dot_scale"
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    .alpha(if (selected) 1f else 0.7f)
            )
        }
    }
}

@Composable
private fun BoxScope.ExpenseCardIllustration() {
    FloatingCircle(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 10.dp, end = 10.dp),
        size = 110.dp,
        icon = Icons.Filled.CurrencyBitcoin,
        iconTint = MaterialTheme.colorScheme.secondary
    )

    FloatingCircle(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(top = 124.dp),
        size = 110.dp,
        icon = Icons.Filled.Money,
        iconTint = MaterialTheme.colorScheme.tertiary
    )

    FloatingCircle(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(top = 140.dp, end = 8.dp),
        size = 100.dp,
        icon = Icons.Filled.Savings,
        iconTint = MaterialTheme.colorScheme.onSurface
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(top = 8.dp)
            .fillMaxWidth(0.72f)
            .height(230.dp)
            .graphicsLayer {
                rotationZ = -4f
            }
            .clip(RoundedCornerShape(38.dp))
            .background(
                brush = surfaceGradient()
            )
            .padding(24.dp)
    ) {
        Text(
            text = "EXPENSE TRACKER",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Box(
            modifier = Modifier
                .padding(top = 18.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Box(
                modifier = Modifier
                    .width(82.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PRIVATE MEMBER",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 2.2.sp,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun BoxScope.SecureTrackerIllustration() {
    FloatingCircle(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(top = 110.dp, start = 10.dp),
        size = 74.dp,
        icon = Icons.Filled.Key,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    )

    FloatingCircle(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 52.dp, end = 54.dp),
        size = 84.dp,
        icon = Icons.Filled.Security,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.82f)
            .height(270.dp)
            .clip(RoundedCornerShape(42.dp))
            .background(
                brush = surfaceGradient()
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(152.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0f))
            )

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(
                        brush = brandGradient()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ENCRYPTED MODE",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.2.sp,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "System Active",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BoxScope.AnalyticsIllustration() {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 12.dp, top = 36.dp)
            .size(90.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .borderGlowCircle(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f), 0.08f)
        )
    }

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.82f)
            .height(300.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(
                brush = surfaceGradient()
            )
            .padding(24.dp)
    ) {
        Text(
            text = "GROWTH INDEX",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium.copy(
                letterSpacing = 1.4.sp,
                fontSize = 13.sp
            )
        )

        Text(
            text = "+24.8%",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            ),
            modifier = Modifier.padding(top = 34.dp)
        )

        Icon(
            imageVector = Icons.Filled.SsidChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(88.dp, 144.dp, 64.dp, 122.dp).forEachIndexed { index, height ->
                val isActive = index == 1
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(height)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                        .background(
                            if (isActive) {
                                brandGradient()
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 18.dp, bottom = 22.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "ACCURACY",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.8.sp,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "High Fidelity",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BoxScope.PremiumPrivacyIllustration() {
    FloatingCircle(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 44.dp, end = 34.dp),
        size = 76.dp,
        icon = Icons.Filled.Lock,
        iconTint = MaterialTheme.colorScheme.secondary
    )

    FloatingCircle(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 24.dp, top = 86.dp),
        size = 84.dp,
        icon = Icons.Filled.Savings,
        iconTint = MaterialTheme.colorScheme.tertiary
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.64f)
            .height(250.dp)
            .clip(RoundedCornerShape(42.dp))
            .background(
                brush = surfaceGradient()
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(118.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
private fun PremiumBenefitCards() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BenefitCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.CloudOff,
            iconTint = MaterialTheme.colorScheme.secondary,
            title = "ARCHITECTURE",
            value = "100% Offline"
        )

        BenefitCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.CheckCircle,
            iconTint = MaterialTheme.colorScheme.tertiary,
            title = "ACCESS",
            value = "Lifetime Pro"
        )
    }
}

@Composable
private fun BenefitCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium.copy(
                letterSpacing = 2.sp,
                fontSize = 11.sp
            )
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            modifier = Modifier.widthIn(max = 132.dp)
        )
    }
}

@Composable
private fun FloatingCircle(
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    icon: ImageVector,
    iconTint: Color
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.34f)
        )
    }
}

@Composable
private fun Modifier.borderGlowCircle(
    color: Color,
    alpha: Float
): Modifier = this.then(
    Modifier
        .rotate(0f)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha),
                    MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                )
            ),
            shape = CircleShape
        )
)

@Preview(
    name = "Onboarding Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun OnboardingScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreen()
    }
}
