package com.mknlabs.expensetracker.feature.profile.ui

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.navigation.LocalUpgradeToPro
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.PremiumBorder
import com.mknlabs.expensetracker.core.ui.theme.PremiumGradientEnd
import com.mknlabs.expensetracker.core.ui.theme.PremiumGradientStart
import com.mknlabs.expensetracker.core.ui.theme.PremiumGold
import com.mknlabs.expensetracker.core.ui.theme.PremiumOnGradient
import com.mknlabs.expensetracker.core.ui.theme.PremiumShadowNeutral
import com.mknlabs.expensetracker.feature.paywall.ui.purchaseMessageRes
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.StoreEntitlement

@Composable
fun MembershipDetailsScreen(
    userTier: UserTier = UserTier.FREE,
    proExpiryTimestamp: Long = 0L,
    isAnonymous: Boolean = false,
    onBackClick: () -> Unit = {},
    monetizationViewModel: MonetizationViewModel = hiltViewModel()
) {
    // Whether Pro came from the store decides which Pro state this screen is in, so it is
    // read from the entitlement rather than from the profile's `isSubscription` mirror —
    // only a Firestore sync writes that, so a real subscriber used to read as a ProPass.
    val storeEntitlement by monetizationViewModel.storeEntitlement.collectAsStateWithLifecycle()
    val restoreState by monetizationViewModel.restoreState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Ask the store what it knows before this card describes the user's access. RevenueCat
    // delivers customer info only through its listener, a login or a purchase, so a
    // subscriber who was already signed in could arrive here with no store snapshot at all
    // — and the card then named a ProPass grant as the source of their Pro.
    LaunchedEffect(Unit) {
        monetizationViewModel.refreshStoreEntitlement()
    }

    // The restore is announced here because it now runs here. Same pure mapper as the
    // paywall so one outcome can never be worded two ways; `isPremium` is the store's own
    // verdict, which is what separates "restored" from "nothing to restore".
    val restoreMessageRes = purchaseMessageRes(restoreState, isPremium = storeEntitlement != null)
    LaunchedEffect(restoreMessageRes) {
        val messageRes = restoreMessageRes ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(messageRes))
        // Released only after the message has been shown, so a configuration change
        // mid-snackbar cannot swallow it.
        monetizationViewModel.onRestoreOutcomeShown()
    }

    MembershipDetailsContent(
        userTier = userTier,
        proExpiryTimestamp = proExpiryTimestamp,
        isAnonymous = isAnonymous,
        storeEntitlement = storeEntitlement,
        isRestoring = restoreState is PurchaseState.InProgress,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onRestoreClick = monetizationViewModel::restorePurchases
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MembershipDetailsContent(
    userTier: UserTier,
    proExpiryTimestamp: Long,
    isAnonymous: Boolean,
    storeEntitlement: StoreEntitlement?,
    isRestoring: Boolean,
    onBackClick: () -> Unit,
    onRestoreClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val status = resolveMembershipStatus(
        userTier = userTier,
        isAnonymous = isAnonymous,
        // The entitlement itself, not a cached boolean: the card needs its date too.
        hasActiveStoreSubscription = storeEntitlement != null
    )
    val isPremium = status.isPro
    val colorScheme = MaterialTheme.colorScheme
    // Opens the Pro paywall (provided by the app shell). The actions that need plans or the
    // store's management page land on it: it lists the plans and carries the store's
    // management link. Restore deliberately does not — it is an action of its own, and is
    // run here rather than by sending the user to the purchase screen.
    val upgradeToPro = LocalUpgradeToPro.current

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
                        status = status,
                        proExpiryTimestamp = proExpiryTimestamp,
                        storeEntitlement = storeEntitlement,
                        onUpgradeClick = upgradeToPro
                    )
                }

                // Features Checklist Section
                item {
                    Text(
                        text = stringResource(R.string.label_membership_benefits),
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
                        // The two Pro states need opposite actions, which is the whole
                        // reason they are tracked separately:
                        //  - a subscriber has a subscription to manage in the store;
                        //  - a ProPass holder has none, and is the user most likely to
                        //    want one, so they get the store's plans instead.
                        val primaryActionRes = when (status) {
                            MembershipStatus.SUBSCRIPTION -> R.string.btn_manage_subscription
                            MembershipStatus.PRO_PASS -> R.string.btn_paywall_subscribe
                            // Free and offline states carry their call to action on the
                            // hero card, where the benefits are spelled out.
                            MembershipStatus.FREE, MembershipStatus.OFFLINE -> null
                        }

                        primaryActionRes?.let { actionRes ->
                            Button(
                                onClick = upgradeToPro,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.secondaryContainer,
                                    contentColor = colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(actionRes),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onRestoreClick,
                            enabled = !isRestoring,
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

                        // Where cancelling actually happens, under the action a subscriber is
                        // most likely to try first. Play owns the renewal, so the app cannot
                        // offer a cancel control of its own — the honest answer is the store
                        // path. Only a subscriber has something to cancel: a ProPass grant
                        // runs out on its own, and a free user has nothing to end.
                        if (status == MembershipStatus.SUBSCRIPTION) {
                            SubscriptionCancelHint()
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

// The two lines that answer "how do I stop being charged?". Split rather than written as
// one sentence because the store path is the part that has to be read, and a subscriber
// scanning for it should not have to parse a paragraph to find it.
@Composable
private fun SubscriptionCancelHint() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.label_cancel_subscription_anytime),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.label_cancel_subscription_where),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MembershipHeroCard(
    status: MembershipStatus,
    proExpiryTimestamp: Long,
    storeEntitlement: StoreEntitlement?,
    onUpgradeClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val isPremium = status.isPro
    val isAnonymous = status == MembershipStatus.OFFLINE
    val proGradient = Brush.verticalGradient(
        colors = listOf(
            PremiumGradientStart,
            PremiumGradientEnd
        )
    )

    // Which sentence to show, and which date belongs in it, is decided by
    // `membershipHeroCopy`; formatting happens only when it named a date. That keeps the two
    // sources of truth apart: a subscriber is dated by the store's own `expirationDate`, a
    // ProPass by the timestamp the Cloud Function wrote.
    val heroCopy = membershipHeroCopy(
        status = status,
        proExpiryTimestamp = proExpiryTimestamp,
        storeEntitlement = storeEntitlement,
        // Read at composition so an end date that has already passed is described as
        // expired rather than announced as an upcoming renewal.
        now = System.currentTimeMillis()
    )
    val dateMillis = heroCopy.dateMillis
    val formattedDate = remember(dateMillis) {
        if (dateMillis == null) {
            ""
        } else {
            // A renewal is a day, not an instant: the store reports midnight-ish boundaries,
            // so a time would only add noise. A ProPass expiry keeps its time, because it is
            // accurate to the moment the pass dies.
            val pattern = if (status == MembershipStatus.SUBSCRIPTION) {
                R.string.date_pattern_full_short
            } else {
                R.string.date_pattern_pro_expiry
            }
            try {
                java.text.SimpleDateFormat(context.getString(pattern), java.util.Locale.getDefault())
                    .format(java.util.Date(dateMillis))
            } catch (e: Exception) {
                context.getString(R.string.label_na)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = if (isPremium) PremiumGradientStart.copy(alpha = 0.4f) else PremiumShadowNeutral.copy(alpha = 0.1f),
                spotColor = if (isPremium) PremiumGradientEnd.copy(alpha = 0.4f) else PremiumShadowNeutral.copy(alpha = 0.1f)
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
                color = if (isPremium) PremiumBorder.copy(alpha = 0.7f) else colorScheme.outlineVariant.copy(alpha = 0.4f),
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
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = when (status) {
                            MembershipStatus.SUBSCRIPTION -> stringResource(R.string.label_pro_subscription_active)
                            MembershipStatus.PRO_PASS -> stringResource(R.string.label_pro_pass_active)
                            MembershipStatus.OFFLINE -> stringResource(R.string.label_unlimited_offline)
                            MembershipStatus.FREE -> stringResource(R.string.label_free_tier)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isPremium) PremiumOnGradient else colorScheme.onSurface
                    )

                    // Where Pro comes from, said outright. The two Pro states look alike
                    // from the outside but mean opposite things: one is a recurring charge
                    // the user can cancel, the other a grant that simply runs out. The card
                    // names which, instead of leaving it to be inferred from the wording
                    // above — the whole reason a subscriber used to read as a ProPass.
                    val sourceRes = when (status) {
                        MembershipStatus.SUBSCRIPTION -> R.string.label_pro_source_subscription
                        MembershipStatus.PRO_PASS -> R.string.label_pro_source_pro_pass
                        MembershipStatus.OFFLINE, MembershipStatus.FREE -> null
                    }
                    sourceRes?.let { res ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(res),
                            style = MaterialTheme.typography.labelLarge,
                            color = PremiumOnGradient.copy(alpha = 0.85f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (isPremium) PremiumOnGradient.copy(alpha = 0.2f) else colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(com.mknlabs.expensetracker.R.drawable.ic_crown),
                        contentDescription = stringResource(R.string.label_pro),
                        tint = if (isPremium) PremiumGold else colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (dateMillis != null) {
                    stringResource(heroCopy.descriptionRes, formattedDate)
                } else {
                    stringResource(heroCopy.descriptionRes)
                },
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                color = if (isPremium) PremiumOnGradient.copy(alpha = 0.9f) else colorScheme.onSurfaceVariant
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
                        text = if (isAnonymous) stringResource(R.string.btn_sign_in_register) else stringResource(R.string.btn_upgrade_now),
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

/** A renewing store subscription: the card dates this from the store, not the profile. */
private fun renewingSubscription() = StoreEntitlement(
    expirationDateMillis = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30, // 30 days
    willRenew = true,
    hasBillingIssue = false
)

@Preview(name = "Subscription State")
@Preview(name = "Subscription State (Dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SubscriptionMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.PREMIUM,
            // Zero on purpose: a real subscriber has no local expiry, so this must never be
            // what the card dates itself from.
            proExpiryTimestamp = 0L,
            isAnonymous = false,
            storeEntitlement = renewingSubscription(),
            isRestoring = false,
            onRestoreClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Subscription Cancelled State")
@Composable
private fun CancelledSubscriptionMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = 0L,
            isAnonymous = false,
            // Still active until it expires, but it will not renew — the card must not call
            // this date a renewal.
            storeEntitlement = renewingSubscription().copy(willRenew = false),
            isRestoring = false,
            onRestoreClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "ProPass State")
@Composable
private fun ProPassMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30L, // 30 days
            isAnonymous = false,
            storeEntitlement = null,
            isRestoring = false,
            onRestoreClick = {},
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
            storeEntitlement = null,
            isRestoring = false,
            onRestoreClick = {},
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
            storeEntitlement = null,
            isRestoring = false,
            onRestoreClick = {},
            onBackClick = {}
        )
    }
}
