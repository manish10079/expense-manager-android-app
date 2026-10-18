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

/**
 * The colours one membership state draws itself in.
 *
 * Gathered in one place because the four states share a single layout and differ only here
 * and in their copy.
 *
 * The card wears the cash flow hero's chrome: the app's own card surface in each theme behind
 * the spec's single hairline, and no shadow. What used to tell the states apart was the fill —
 * violet for access a user pays for, blue for access they were given — and that made the
 * membership card the loudest object on a screen whose whole job is to explain access. The
 * state is told by the header label, the badge, the glyph and the panel instead, so all four
 * read as one card answering one question.
 */
private data class MembershipCardPalette(
    val surface: Color,
    val border: Color,
    val foreground: Color,
    val headerLabel: Color,
    val bodyText: Color,
    val accent: Color,
    val panelBackground: Color,
    val panelIcon: Color,
    val panelHeadline: Color,
)

@Composable
private fun membershipCardPalette(status: MembershipStatus): MembershipCardPalette {
    val colorScheme = MaterialTheme.colorScheme
    return MembershipCardPalette(
        surface = colorScheme.membershipCard,
        border = colorScheme.membershipCardBorder,
        foreground = colorScheme.onSurface,
        headerLabel = if (status == MembershipStatus.SUBSCRIPTION || status == MembershipStatus.PRO_PASS) {
            colorScheme.premiumGold
        } else {
            colorScheme.membershipMutedInk
        },
        bodyText = colorScheme.onSurfaceVariant,
        accent = if (status == MembershipStatus.SUBSCRIPTION || status == MembershipStatus.PRO_PASS) {
            colorScheme.premiumGold
        } else {
            colorScheme.accentInk
        },
        panelBackground = colorScheme.onSurface.copy(alpha = 0.05f),
        // The one thing the states still say in colour: a subscriber holds access the store
        // keeps, a pass holds access that runs out, and neither is the other's claim. The
        // panel's own glyph carries that difference; everything around it is one neutral ink.
        panelIcon = if (status == MembershipStatus.SUBSCRIPTION) {
            colorScheme.income
        } else {
            colorScheme.accentInk
        },
        panelHeadline = colorScheme.onSurface,
    )
}

/** The icon behind a [MembershipGlyph]. */
private fun glyphIcon(glyph: MembershipGlyph): ImageVector = when (glyph) {
    MembershipGlyph.VERIFIED -> Icons.Default.Check
    MembershipGlyph.TICKET -> Icons.Rounded.ConfirmationNumber
    MembershipGlyph.LOCK -> Icons.Rounded.Lock
    MembershipGlyph.DATE -> Icons.Rounded.Event
    MembershipGlyph.CLOCK -> Icons.Rounded.Schedule
}

/**
 * The membership card: one layout for all four states.
 *
 * A subscriber, a ProPass holder, a free user and an anonymous one are all asking the same
 * question — what do I have? — so they get the same card, on the same neutral surface: what
 * this is (header and badge), what it is called (title), what the app actually knows about it
 * (facts), and how it is paid for (panel). Only the answers differ, and only the answers are
 * what tells the four apart. That is what makes them comparable at a glance instead of four
 * unrelated designs the user has to re-read each time.
 *
 * Size comes from the window's size class and the layout wraps rather than clips, so the card
 * survives a tablet, a split screen and a large system font without losing a fact.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MembershipCard(
    status: MembershipStatus,
    proExpiryTimestamp: Long,
    storeEntitlement: StoreEntitlement?,
    onUpgradeClick: () -> Unit
) {
    val spacing = currentSpacing()
    val palette = membershipCardPalette(status)
    val spec = membershipCardSpec(
        status = status,
        proExpiryTimestamp = proExpiryTimestamp,
        storeEntitlement = storeEntitlement,
        // Read at composition so an end date that has already passed is described as ended
        // rather than announced as an upcoming renewal.
        now = System.currentTimeMillis()
    )
    val cardShape = RoundedCornerShape(28.dp)

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        // The mockup's Obsidian card behind a white 8% hairline; light keeps the app card.
        colors = AppCardDefaults.colors(
            darkContainer = palette.surface,
            darkBorder = BorderStroke(1.dp, palette.border)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(spacing.paddingCompact)
        ) {
            MembershipCardHeader(spec = spec, palette = palette, status = status)

            Text(
                text = stringResource(spec.titleRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = palette.foreground
            )

            spec.bodyRes?.let { bodyRes ->
                Text(
                    text = stringResource(bodyRes),
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    color = palette.bodyText
                )
            }

            if (spec.facts.isNotEmpty()) {
                MembershipCardFacts(
                    facts = spec.facts,
                    // A pass dies at a moment, so its date keeps the time; a store renewal is a
                    // day, so a time would only be noise.
                    datePatternRes = if (status == MembershipStatus.PRO_PASS) {
                        R.string.date_pattern_pro_expiry
                    } else {
                        R.string.date_pattern_full_short
                    },
                    palette = palette
                )
            }

            spec.panel?.let { panel ->
                MembershipCardPanel(panel = panel, palette = palette)
            }

            spec.primaryActionRes?.let { primaryActionRes ->
                MembershipCardActions(
                    primaryActionRes = primaryActionRes,
                    onUpgradeClick = onUpgradeClick
                )
            }
        }
    }
}

@Composable
private fun MembershipCardHeader(
    spec: MembershipCardSpec,
    palette: MembershipCardPalette,
    status: MembershipStatus,
) {
    val gradient = brandGradient()
    val chipLabel = stringResource(spec.headerLabelRes).uppercase(Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val brandChip = status == MembershipStatus.PRO_PASS ||
            status == MembershipStatus.SUBSCRIPTION
        val headerShape = RoundedCornerShape(8.dp)
        val headerInk = if (brandChip) Color.White else palette.headerLabel
        Row(
            modifier = Modifier
                .clip(headerShape)
                .then(
                    if (brandChip) {
                        Modifier.background(brush = gradient)
                    } else {
                        Modifier.border(1.5.dp, palette.border, headerShape)
                    }
                )
                .padding(horizontal = Dimens.spacingCompact, vertical = Dimens.spacingTiny),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The merged card is the icon's backing; there is no separate icon tile.
            when (status) {
                MembershipStatus.OFFLINE -> Icon(
                    painter = painterResource(R.drawable.ic_verified_off_24px),
                    contentDescription = null,
                    tint = headerInk,
                    modifier = Modifier.size(18.dp)
                )
                MembershipStatus.FREE -> Icon(
                    imageVector = Icons.Outlined.Unpublished,
                    contentDescription = null,
                    tint = headerInk,
                    modifier = Modifier.size(18.dp)
                )
                MembershipStatus.PRO_PASS -> Icon(
                    imageVector = Icons.Outlined.LocalActivity,
                    contentDescription = null,
                    tint = headerInk,
                    modifier = Modifier.size(18.dp)
                )
                MembershipStatus.SUBSCRIPTION -> Icon(
                    imageVector = PhosphorIcons.Regular.Crown,
                    contentDescription = null,
                    tint = headerInk,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.spacingSmall))

            Text(
                text = chipLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = headerInk
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        spec.badgeRes?.let { badgeRes ->
            Spacer(modifier = Modifier.width(Dimens.spacingCompact))
            val isActiveBadge = badgeRes == R.string.label_active_caps
            Text(
                text = stringResource(badgeRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = if (isActiveBadge) {
                    MaterialTheme.colorScheme.income
                } else {
                    MaterialTheme.colorScheme.membershipMutedInk
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MembershipCardFacts(
    facts: List<MembershipFact>,
    @StringRes datePatternRes: Int,
    palette: MembershipCardPalette
) {
    // Flow, not Row: two facts side by side while there is room, stacked when there is not —
    // which is what a narrow screen, a split window or a large font all produce.
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingDefault),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        facts.forEach { fact ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                val colorScheme = MaterialTheme.colorScheme
                // The count is the mockup's brighter slate; the date sits one step back.
                val factColor = if (fact is MembershipFact.Date) {
                    colorScheme.membershipFactDateInk
                } else {
                    colorScheme.membershipFactInk
                }
                if (fact.glyph != MembershipGlyph.VERIFIED) {
                    Icon(
                        imageVector = glyphIcon(fact.glyph),
                        contentDescription = null,
                        tint = when (fact.glyph) {
                            MembershipGlyph.CLOCK -> colorScheme.membershipFactClockInk
                            MembershipGlyph.DATE -> colorScheme.membershipMutedInk
                            else -> factColor
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = membershipFactLabel(fact, datePatternRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = factColor
                )
            }
        }
    }
}

/** A fact's own text: a formatted date, a pluralised count, or a plain label. */
@Composable
private fun membershipFactLabel(fact: MembershipFact, @StringRes datePatternRes: Int): String =
    when (fact) {
        is MembershipFact.Label -> stringResource(fact.labelRes)

        is MembershipFact.Date ->
            stringResource(fact.labelRes, rememberFormattedDate(fact.valueMillis, datePatternRes))

        is MembershipFact.Count ->
            pluralStringResource(fact.pluralsRes, fact.count, fact.count)
    }

/** Formats a timestamp once per value and pattern, falling back to N/A rather than crashing. */
@Composable
private fun rememberFormattedDate(millis: Long, @StringRes patternRes: Int): String {
    val context = LocalContext.current
    return remember(millis, patternRes) {
        try {
            java.text.SimpleDateFormat(context.getString(patternRes), java.util.Locale.getDefault())
                .format(java.util.Date(millis))
        } catch (e: Exception) {
            context.getString(R.string.label_na)
        }
    }
}

@Composable
private fun MembershipCardPanel(panel: MembershipPanel, palette: MembershipCardPalette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = palette.panelBackground, shape = RoundedCornerShape(16.dp))
            .padding(Dimens.spacingDefault),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingTiny)
    ) {
        if (panel.glyph != null || panel.headlineRes != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                panel.glyph?.let { glyph ->
                    Icon(
                        imageVector = glyphIcon(glyph),
                        contentDescription = null,
                        tint = palette.panelIcon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                }

                panel.headlineRes?.let { headlineRes ->
                    Text(
                        text = stringResource(headlineRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.panelHeadline,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Text(
            text = stringResource(panel.bodyRes),
            style = MaterialTheme.typography.bodyMedium,
            // The mockup's notice ink: the panel states a caveat, not another headline.
            color = MaterialTheme.colorScheme.membershipMutedInk
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MembershipCardActions(
    @StringRes primaryActionRes: Int,
    onUpgradeClick: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingCompact),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        val ctaShape = RoundedCornerShape(24.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(ctaShape)
                .background(brush = brandGradient())
                .clickable(onClick = onUpgradeClick),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (primaryActionRes) {
                    R.string.btn_sign_in_register -> {
                        Icon(
                            imageVector = PhosphorIcons.Regular.SignIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                    }
                    R.string.btn_membership_buy_subscription -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_award_star_24px),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacingSmall))
                    }
                }
                Text(
                    text = stringResource(primaryActionRes),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
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
                    color = if (isAvailable) colorScheme.accentInk.copy(alpha = 0.15f) else colorScheme.error.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAvailable) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isAvailable) colorScheme.accentInk else colorScheme.error,
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

@Preview(name = "Subscription State", showBackground = true)
@Preview(name = "Subscription State (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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

@Preview(name = "Subscription Cancelled State", showBackground = true)
@Preview(name = "Subscription Cancelled State (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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

@Preview(name = "Subscription Billing Issue", showBackground = true)
@Preview(name = "Subscription Billing Issue (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BillingIssueSubscriptionMembershipPreview() {
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
private fun ProPassMembershipPreview() {
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
private fun ProPassLegacyMembershipPreview() {
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

@Preview(name = "Anonymous User State", showBackground = true)
@Preview(name = "Anonymous User State (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
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

@Preview(name = "MembershipCard Subscription", showBackground = true)
@Preview(name = "MembershipCard Subscription Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MembershipCardSubscriptionPreview() {
    MembershipCardPreviewHost(status = MembershipStatus.SUBSCRIPTION, storeEntitlement = renewingSubscription())
}

@Preview(name = "MembershipCard Subscription Cancelled", showBackground = true)
@Preview(name = "MembershipCard Subscription Cancelled Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MembershipCardSubscriptionCancelledPreview() {
    MembershipCardPreviewHost(
        status = MembershipStatus.SUBSCRIPTION,
        storeEntitlement = renewingSubscription().copy(willRenew = false)
    )
}

@Preview(name = "MembershipCard Subscription Billing Issue", showBackground = true)
@Preview(name = "MembershipCard Subscription Billing Issue Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MembershipCardSubscriptionBillingIssuePreview() {
    MembershipCardPreviewHost(
        status = MembershipStatus.SUBSCRIPTION,
        storeEntitlement = renewingSubscription().copy(hasBillingIssue = true)
    )
}

@Preview(name = "MembershipCard Pro Pass", showBackground = true)
@Preview(name = "MembershipCard Pro Pass Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MembershipCardProPassPreview() {
    MembershipCardPreviewHost(
        status = MembershipStatus.PRO_PASS,
        proExpiryTimestamp = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30
    )
}

@Preview(name = "MembershipCard Pro Pass Legacy", showBackground = true)
@Preview(name = "MembershipCard Pro Pass Legacy Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MembershipCardProPassLegacyPreview() {
    MembershipCardPreviewHost(status = MembershipStatus.PRO_PASS, proExpiryTimestamp = 0L)
}

@Preview(name = "MembershipCard Free", showBackground = true)
@Preview(name = "MembershipCard Free Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MembershipCardFreePreview() {
    MembershipCardPreviewHost(status = MembershipStatus.FREE)
}

@Preview(name = "MembershipCard Offline", showBackground = true)
@Preview(name = "MembershipCard Offline Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MembershipCardOfflinePreview() {
    MembershipCardPreviewHost(status = MembershipStatus.OFFLINE)
}

@Composable
private fun MembershipCardPreviewHost(
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
