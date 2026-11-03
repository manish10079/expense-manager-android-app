package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.ProfileAvatar
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.surfaceGradient
@Composable
internal fun BoxScope.ExpenseCardIllustration() {
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
            text = stringResource(id = R.string.label_expense_tracker_caps),
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
                text = stringResource(id = R.string.label_secure_logging),
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
internal fun BoxScope.SecureTrackerIllustration() {
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
                    tint = MaterialTheme.colorScheme.onCta,
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
                text = stringResource(id = R.string.label_encrypted_mode),
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
                    text = stringResource(id = R.string.label_system_active),
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
internal fun BoxScope.AnalyticsIllustration() {
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
            text = stringResource(id = R.string.label_growth_index),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium.copy(
                letterSpacing = 1.4.sp,
                fontSize = 13.sp
            )
        )

        Text(
            text = stringResource(id = R.string.label_248),
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
                    text = stringResource(id = R.string.label_accuracy),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.8.sp,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(id = R.string.label_high_fidelity),
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
internal fun BoxScope.PremiumPrivacyIllustration() {
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
                    ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.26f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                )
                .clip(CircleShape)
                .background(brush = brandGradient()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onCta,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
internal fun PremiumBenefitCards() {
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
            title = stringResource(id = R.string.label_architecture),
            value = stringResource(id = R.string.label_100_offline)
        )

        BenefitCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.CheckCircle,
            iconTint = MaterialTheme.colorScheme.tertiary,
            title = stringResource(id = R.string.label_access),
            value = stringResource(id = R.string.label_full_control)
        )
    }
}

@Composable
internal fun BenefitCard(
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                )
            )
        }

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 16.sp
            ),
            textAlign = TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun FloatingCircle(
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
internal fun Modifier.borderGlowCircle(
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

@Composable
internal fun BoxScope.GoalIllustration() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(240.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Savings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(100.dp)
        )
    }
}

@Composable
internal fun BoxScope.SetupIllustration() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(240.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        ProfileAvatar()
    }
}
