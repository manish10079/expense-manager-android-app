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


/** A renewing store subscription: the card dates this from the store, not the profile. */
internal fun renewingSubscription() = StoreEntitlement(
    expirationDateMillis = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30, // 30 days
    willRenew = true,
    hasBillingIssue = false
)

@Preview(name = "Subscription State", showBackground = true)
@Preview(name = "Subscription State (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun SubscriptionMembershipPreview() {
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

@Preview(name = "Subscription Cancelled State", showBackground = true)
@Preview(name = "Subscription Cancelled State (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CancelledSubscriptionMembershipPreview() {
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

@Preview(name = "Subscription Billing Issue", showBackground = true)
@Preview(name = "Subscription Billing Issue (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BillingIssueSubscriptionMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = 0L,
            isAnonymous = false,
            storeEntitlement = renewingSubscription().copy(hasBillingIssue = true),
            isRestoring = false,
            onRestoreClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "ProPass State", showBackground = true)
@Preview(name = "ProPass State (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ProPassMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.PREMIUM,
            accountTier = EntitlementResolver.TIER_PRO_PASS,
            proExpiryTimestamp = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 30L, // 30 days
            isAnonymous = false,
            storeEntitlement = null,
            isRestoring = false,
            onRestoreClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "ProPass Legacy Permanent", showBackground = true)
@Preview(name = "ProPass Legacy Permanent (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ProPassLegacyMembershipPreview() {
    ExpenseTrackerTheme {
        MembershipDetailsContent(
            userTier = UserTier.PREMIUM,
            accountTier = EntitlementResolver.TIER_PRO_PASS,
            proExpiryTimestamp = 0L,
            isAnonymous = false,
            storeEntitlement = null,
            isRestoring = false,
            onRestoreClick = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Free User State", showBackground = true)
@Preview(name = "Free User State (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun FreeMembershipPreview() {
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

@Preview(name = "Anonymous User State", showBackground = true)
@Preview(name = "Anonymous User State (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AnonymousMembershipPreview() {
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

@Preview(name = "MembershipCard Subscription", showBackground = true)
@Preview(name = "MembershipCard Subscription Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MembershipCardSubscriptionPreview() {
    MembershipCardPreviewHost(status = MembershipStatus.SUBSCRIPTION, storeEntitlement = renewingSubscription())
}

@Preview(name = "MembershipCard Subscription Cancelled", showBackground = true)
@Preview(name = "MembershipCard Subscription Cancelled Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MembershipCardSubscriptionCancelledPreview() {
    MembershipCardPreviewHost(
        status = MembershipStatus.SUBSCRIPTION,
        storeEntitlement = renewingSubscription().copy(willRenew = false)
    )
}

@Preview(name = "MembershipCard Subscription Billing Issue", showBackground = true)
@Preview(name = "MembershipCard Subscription Billing Issue Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MembershipCardSubscriptionBillingIssuePreview() {
    MembershipCardPreviewHost(
        status = MembershipStatus.SUBSCRIPTION,
        storeEntitlement = renewingSubscription().copy(hasBillingIssue = true)
    )
}

@Preview(name = "MembershipCard Pro Pass", showBackground = true)
@Preview(name = "MembershipCard Pro Pass Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MembershipCardProPassPreview() {
    MembershipCardPreviewHost(
        status = MembershipStatus.PRO_PASS,
        proExpiryTimestamp = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30
    )
}

@Preview(name = "MembershipCard Pro Pass Legacy", showBackground = true)
@Preview(name = "MembershipCard Pro Pass Legacy Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MembershipCardProPassLegacyPreview() {
    MembershipCardPreviewHost(status = MembershipStatus.PRO_PASS, proExpiryTimestamp = 0L)
}

@Preview(name = "MembershipCard Free", showBackground = true)
@Preview(name = "MembershipCard Free Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MembershipCardFreePreview() {
    MembershipCardPreviewHost(status = MembershipStatus.FREE)
}

@Preview(name = "MembershipCard Offline", showBackground = true)
@Preview(name = "MembershipCard Offline Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun MembershipCardOfflinePreview() {
    MembershipCardPreviewHost(status = MembershipStatus.OFFLINE)
}

@Composable
internal fun MembershipCardPreviewHost(
    status: MembershipStatus,
    proExpiryTimestamp: Long = 0L,
    storeEntitlement: StoreEntitlement? = null
) {
    ExpenseTrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                MembershipCard(
                    status = status,
                    proExpiryTimestamp = proExpiryTimestamp,
                    storeEntitlement = storeEntitlement,
                    onUpgradeClick = {}
                )
            }
        }
    }
}
