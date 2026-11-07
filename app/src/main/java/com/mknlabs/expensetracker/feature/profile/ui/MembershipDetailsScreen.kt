package com.mknlabs.expensetracker.feature.profile.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.SignIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.outlined.LocalActivity
import androidx.compose.material.icons.outlined.Unpublished
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedButton
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.navigation.LocalUpgradeToPro
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.membershipCard
import com.mknlabs.expensetracker.core.ui.theme.membershipCardBorder
import com.mknlabs.expensetracker.core.ui.theme.membershipFactClockInk
import com.mknlabs.expensetracker.core.ui.theme.membershipFactDateInk
import com.mknlabs.expensetracker.core.ui.theme.membershipFactInk
import com.mknlabs.expensetracker.core.ui.theme.membershipMutedInk
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.premiumGold
import com.mknlabs.expensetracker.core.ui.theme.TextSecondaryLight
import com.mknlabs.expensetracker.core.ui.theme.currentSpacing
import com.mknlabs.expensetracker.feature.paywall.ui.purchaseMessageRes
import com.mknlabs.expensetracker.feature.settings.ui.ReferAFriendViewModel
import com.mknlabs.expensetracker.monetization.EntitlementResolver
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import java.util.Locale

@Composable
fun MembershipDetailsScreen(
    userTier: UserTier = UserTier.FREE,
    accountTier: String = "",
    proExpiryTimestamp: Long = 0L,
    isAnonymous: Boolean = false,
    onBackClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    monetizationViewModel: MonetizationViewModel = hiltViewModel(),
    referralViewModel: ReferAFriendViewModel = hiltViewModel()
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
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) referralViewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
        accountTier = accountTier,
        proExpiryTimestamp = proExpiryTimestamp,
        isAnonymous = isAnonymous,
        storeEntitlement = storeEntitlement,
        isRestoring = restoreState is PurchaseState.InProgress,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onRestoreClick = monetizationViewModel::restorePurchases,
        onSignInClick = onSignInClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MembershipDetailsContent(
    userTier: UserTier,
    accountTier: String = "",
    proExpiryTimestamp: Long,
    isAnonymous: Boolean,
    storeEntitlement: StoreEntitlement?,
    isRestoring: Boolean,
    onBackClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onSignInClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val status = resolveMembershipStatus(
        userTier = userTier,
        isAnonymous = isAnonymous,
        // The entitlement itself, not a cached boolean: the card needs its date too.
        hasActiveStoreSubscription = storeEntitlement != null,
        hasProPassGrant = com.mknlabs.expensetracker.monetization.EntitlementResolver.hasActiveProPassGrant(
            accountTier = accountTier,
            proExpiryTimestamp = proExpiryTimestamp,
            now = System.currentTimeMillis(),
        ),
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
            .appScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            val enter = rememberSectionEnterAlphas(2)
            AppHeader(
                title = stringResource(R.string.title_membership),
                onBackClick = onBackClick,
                modifier = Modifier.alpha(enter[0])
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .alpha(enter[1]),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Hero Card
                item {
                    MembershipCard(
                        status = status,
                        proExpiryTimestamp = proExpiryTimestamp,
                        storeEntitlement = storeEntitlement,
                        onUpgradeClick = if (status == MembershipStatus.OFFLINE) onSignInClick else upgradeToPro
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
                        // A section header is a label rather than something to tap, so in
                        // light it takes the secondary ink instead of the brand purple the
                        // card above it already uses for its actions.
                        color = if (colorScheme.isDark) colorScheme.accentInk.copy(alpha = 0.8f)
                        else TextSecondaryLight,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }

                item {
                    AppCard(
                        shape = AppCardDefaults.shape(24.dp),
                        // The mockup puts the benefits list on the same Obsidian card as the
                        // hero above; light keeps the app's white card on the grey field.
                        colors = AppCardDefaults.colors(
                            darkContainer = colorScheme.membershipCard,
                            darkBorder = BorderStroke(1.dp, colorScheme.membershipCardBorder)
                        ),
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
                            // The mockup's subscribe action: a 12dp-rounded 52dp bar filled
                            // with the brand gradient, not a flat container colour.
                            Button(
                                onClick = upgradeToPro,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .background(
                                        brush = brandGradient(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Text(
                                    text = stringResource(actionRes),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        AppOutlinedButton(
                            onClick = onRestoreClick,
                            enabled = !isRestoring,
                            // The mockup's restore bar: the same 12dp corner at 48dp.
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.accentInk
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
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

