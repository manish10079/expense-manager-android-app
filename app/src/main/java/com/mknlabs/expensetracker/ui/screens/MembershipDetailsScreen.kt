package com.mknlabs.expensetracker.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel

@Composable
fun MembershipDetailsScreen(
    userTier: UserTier = UserTier.FREE,
    proExpiryTimestamp: Long = 0L,
    isAnonymous: Boolean = false,
    isSubscription: Boolean = false,
    onBackClick: () -> Unit = {},
    monetizationViewModel: MonetizationViewModel = hiltViewModel()
) {
    MembershipDetailsContent(
        userTier = userTier,
        proExpiryTimestamp = proExpiryTimestamp,
        isAnonymous = isAnonymous,
        isSubscription = isSubscription,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MembershipDetailsContent(
    userTier: UserTier,
    proExpiryTimestamp: Long,
    isAnonymous: Boolean,
    isSubscription: Boolean,
    onBackClick: () -> Unit
) {
    val isPremium = userTier == UserTier.PREMIUM && !isAnonymous
    val colorScheme = MaterialTheme.colorScheme
    var showComingSoonDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            AppHeader(
                title = stringResource(R.string.title_membership),
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Hero Card
                item {
                    MembershipHeroCard(
                        isPremium = isPremium,
                        isAnonymous = isAnonymous,
                        proExpiryTimestamp = proExpiryTimestamp,
                        isSubscription = isSubscription,
                        onUpgradeClick = { showComingSoonDialog = true }
                    )
                }

                // Features Checklist Section
                item {
                    Text(
                        text = "MEMBERSHIP BENEFITS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BenefitRow(
                                title = stringResource(R.string.label_pro_benefit_adfree),
                                isAvailable = isPremium
                            )
                            BenefitRow(
                                title = stringResource(R.string.label_pro_benefit_devices),
                                isAvailable = isPremium
                            )
                            BenefitRow(
                                title = stringResource(R.string.label_pro_benefit_custom_card),
                                isAvailable = isPremium
                            )
                            BenefitRow(
                                title = stringResource(R.string.label_pro_benefit_advanced_features),
                                isAvailable = isPremium
                            )
                            BenefitRow(
                                title = stringResource(R.string.label_pro_benefit_many_more_features),
                                isAvailable = isPremium
                            )
                        }
                    }
                }

                // Action Buttons at the bottom
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isPremium) {
                            Button(
                                onClick = { showComingSoonDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.secondaryContainer,
                                    contentColor = colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_manage_subscription),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showComingSoonDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.primary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.btn_restore_purchase),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        // Coming Soon Dialog
        if (showComingSoonDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showComingSoonDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.WorkspacePremium,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.title_coming_soon),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.msg_billing_coming_soon),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showComingSoonDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_got_it),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                containerColor = colorScheme.surface,
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
private fun MembershipHeroCard(
    isPremium: Boolean,
    isAnonymous: Boolean,
    proExpiryTimestamp: Long,
    isSubscription: Boolean,
    onUpgradeClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val proGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF7C4DFF), // Deep Violet
            Color(0xFF651FFF)  // Vibrant Purple
        )
    )

    val formattedDate = remember(proExpiryTimestamp, isSubscription) {
        val pattern = if (isSubscription) "dd MMM yyyy" else "dd MMM yyyy h:mm a"
        try {
            java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
                .format(java.util.Date(proExpiryTimestamp))
        } catch (e: Exception) {
            "N/A"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = if (isPremium) Color(0xFF7C4DFF).copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.1f),
                spotColor = if (isPremium) Color(0xFF651FFF).copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = if (isPremium) proGradient else Brush.verticalGradient(
                    listOf(
                        colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = if (isPremium) 1.5.dp else 1.dp,
                color = if (isPremium) Color(0xFFB388FF).copy(alpha = 0.7f) else colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isPremium) {
                        if (isSubscription) "Pro Active" else "Pro Member"
                    } else if (isAnonymous) {
                        stringResource(R.string.label_unlimited_offline)
                    } else {
                        stringResource(R.string.label_free_tier)
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ),
                    color = if (isPremium) Color.White else colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (isPremium) Color.White.copy(alpha = 0.2f) else colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(com.mknlabs.expensetracker.R.drawable.ic_crown),
                        contentDescription = stringResource(R.string.label_pro),
                        tint = if (isPremium) Color(0xFFFFD700) else colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPremium) {
                    if (isSubscription) {
                        stringResource(R.string.label_pro_renews_on, formattedDate)
                    } else {
                        stringResource(R.string.label_pro_expires_on, formattedDate)
                    }
                } else if (isAnonymous) {
                    stringResource(R.string.label_offline_warning_desc)
                } else {
                    "Unlock custom transaction styles, advanced budgeting tools, and 4 device sync by going pro."
                },
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                color = if (isPremium) Color.White.copy(alpha = 0.9f) else colorScheme.onSurfaceVariant
            )

            if (!isPremium) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onUpgradeClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isAnonymous) "Sign In / Register" else stringResource(R.string.btn_upgrade_now),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitRow(
    title: String,
    isAvailable: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = if (isAvailable) colorScheme.primary.copy(alpha = 0.15f) else colorScheme.error.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAvailable) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isAvailable) colorScheme.primary else colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (isAvailable) colorScheme.onSurface else colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Preview(name = "Premium User State")
@Preview(name = "Premium User State (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PremiumMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30L, // 30 days
            isAnonymous = false,
            isSubscription = true,
            onBackClick = {}
        )
    }
}

@Preview(name = "Free User State")
@Composable
private fun FreeMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.FREE,
            proExpiryTimestamp = 0L,
            isAnonymous = false,
            isSubscription = false,
            onBackClick = {}
        )
    }
}

@Preview(name = "Anonymous User State")
@Composable
private fun AnonymousMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.FREE,
            proExpiryTimestamp = 0L,
            isAnonymous = true,
            isSubscription = false,
            onBackClick = {}
        )
    }
}
