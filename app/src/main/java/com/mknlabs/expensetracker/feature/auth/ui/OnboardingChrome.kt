package com.mknlabs.expensetracker.feature.auth.ui

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.disabled
import com.mknlabs.expensetracker.core.ui.theme.onCta
@Composable
internal fun BoxScope.AmbientBackdrop() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.accentInk.copy(alpha = 0.18f),
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
internal fun BottomControls(
    pageCount: Int,
    currentPage: Int,
    showSkip: Boolean = true,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val isAuthPage = currentPage == 5
    val isSetupPage = currentPage == 6
    val isWelcomeBackPage = currentPage == 7
    val isPromoPage = currentPage == 8

    if (isAuthPage || isSetupPage || isWelcomeBackPage || isPromoPage) {
        Spacer(modifier = Modifier.height(56.dp)) 
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (!isAuthPage) {
                Text(
                    text = stringResource(id = R.string.label_prev),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onPreviousClick)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }
        }

        PageIndicator(
            pageCount = pageCount,
            currentPage = currentPage,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (showSkip) {
                Text(
                    text = stringResource(id = R.string.label_skip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onSkipClick)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
            } else {
                Text(
                    text = stringResource(id = R.string.label_next_caps),
                    color = MaterialTheme.colorScheme.accentInk,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onNextClick)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
internal fun PrimaryOnboardingButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "onboarding_button_scale"
    )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val buttonHeight = when {
        compact -> 52.dp
        isLandscape -> 56.dp
        else -> 74.dp
    }
    val buttonShape = RoundedCornerShape(16.dp)

    val buttonModifier = if (enabled) {
        Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = buttonShape,
                ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
            )
    } else {
        Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .scale(scale)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = buttonModifier,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onCta,
            disabledContentColor = MaterialTheme.colorScheme.disabled
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) brandGradient() else Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    ),
                    shape = buttonShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (enabled) MaterialTheme.colorScheme.onCta else MaterialTheme.colorScheme.disabled,
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
internal fun PageIndicator(
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
            key(index) {
                val selected = index == currentPage
                val dotScale by animateFloatAsState(
                    targetValue = if (selected) 1.2f else 0.85f,
                    animationSpec = tween(220),
                    label = "onboarding_dot_scale_$index"
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .scale(dotScale)
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.accentInk
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            }
                        )
                )
            }
        }
    }
}
