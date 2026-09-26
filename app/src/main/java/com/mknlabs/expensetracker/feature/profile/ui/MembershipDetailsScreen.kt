package com.mknlabs.expensetracker.feature.profile.ui

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedButton
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.ProPassRedeemDialog
import com.mknlabs.expensetracker.core.ui.navigation.LocalUpgradeToPro
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.IncomeGreen
import com.mknlabs.expensetracker.core.ui.theme.ProPassBorder
import com.mknlabs.expensetracker.core.ui.theme.ProPassGradientEnd
import com.mknlabs.expensetracker.core.ui.theme.ProPassGradientStart
import com.mknlabs.expensetracker.core.ui.theme.ProPassOnGradient
import com.mknlabs.expensetracker.core.ui.theme.PremiumBorder
import com.mknlabs.expensetracker.core.ui.theme.PremiumGradientEnd
import com.mknlabs.expensetracker.core.ui.theme.PremiumGradientStart
import com.mknlabs.expensetracker.core.ui.theme.PremiumGold
import com.mknlabs.expensetracker.core.ui.theme.PremiumOnGradient
import com.mknlabs.expensetracker.core.ui.theme.PremiumShadowNeutral
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.TextSecondaryLight
import com.mknlabs.expensetracker.core.ui.theme.currentSpacing
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

    // The same dialog the Settings row opens, hosted here too so the one code path a
    // ProPass holder looks for exists on the screen that explains their access.
    var showRedeemDialog by remember { mutableStateOf(false) }

    if (showRedeemDialog) {
        ProPassRedeemDialog(
            viewModel = monetizationViewModel,
            onDismiss = { showRedeemDialog = false }
        )
    }

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
        onRestoreClick = monetizationViewModel::restorePurchases,
        onRedeemProPassClick = { showRedeemDialog = true }
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
    onRedeemProPassClick: () -> Unit = {},
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
                    MembershipCard(
                        status = status,
                        proExpiryTimestamp = proExpiryTimestamp,
                        storeEntitlement = storeEntitlement,
                        onUpgradeClick = upgradeToPro,
                        onRedeemProPassClick = onRedeemProPassClick
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
                        // A half-strength variant wash is dark's surface here and dark keeps
                        // it; light takes the white card on the grey field. The membership
                        // hero above keeps its tier gradient - that fill is what says which
                        // access the user has - but the benefits list is chrome, and a tinted
                        // panel is the shape of thing the redesign replaces with a card.
                        colors = AppCardDefaults.colors(
                            darkContainer = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            darkBorder = BorderStroke(
                                1.dp,
                                colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
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

                        // The second way to reach Pro, and the only one open to a user who was
                        // given a code. It sits above Restore because it grants access while
                        // Restore only recovers it.
                        //
                        // Only for a pass holder here: a free user gets it inside their card,
                        // and a subscriber never sees it at all, since the server refuses a pass
                        // that would run out unused. A pass holder keeps it because their state
                        // outlives the grant — the tier is swept on a schedule, so a lapsed pass
                        // still reads as one for a while, and redeeming then is allowed.
                        if (status == MembershipStatus.PRO_PASS) {
                            AppOutlinedButton(
                                onClick = onRedeemProPassClick,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colorScheme.accentInk
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.title_redeem_pro_pass),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        AppOutlinedButton(
                            onClick = onRestoreClick,
                            enabled = !isRestoring,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.accentInk
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

/**
 * The colours one membership state draws itself in.
 *
 * Gathered in one place because the three states share a single layout and differ only here
 * and in their copy. The surface is the part that carries meaning: violet for access the user
 * pays for, blue for a pass they were given, and the neutral surface for the state with no
 * access at all.
 */
private data class MembershipCardPalette(
    val surface: Brush,
    val border: Color,
    val shadow: Color,
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
    return when (status) {
        MembershipStatus.SUBSCRIPTION -> MembershipCardPalette(
            surface = Brush.verticalGradient(listOf(PremiumGradientStart, PremiumGradientEnd)),
            border = PremiumBorder.copy(alpha = 0.7f),
            shadow = PremiumGradientEnd.copy(alpha = 0.4f),
            foreground = PremiumOnGradient,
            headerLabel = PremiumGold,
            bodyText = PremiumOnGradient.copy(alpha = 0.92f),
            accent = PremiumGold,
            panelBackground = PremiumOnGradient.copy(alpha = 0.12f),
            // A green tick on the panel is this card's "you have it" mark; the pass card marks
            // the same claim in gold, which is the accent a grant is dressed in.
            panelIcon = IncomeGreen,
            panelHeadline = PremiumOnGradient,
        )

        MembershipStatus.PRO_PASS -> MembershipCardPalette(
            surface = Brush.verticalGradient(listOf(ProPassGradientStart, ProPassGradientEnd)),
            border = ProPassBorder.copy(alpha = 0.7f),
            shadow = ProPassGradientStart.copy(alpha = 0.4f),
            foreground = ProPassOnGradient,
            headerLabel = PremiumGold,
            bodyText = ProPassOnGradient.copy(alpha = 0.92f),
            accent = PremiumGold,
            panelBackground = ProPassOnGradient.copy(alpha = 0.14f),
            panelIcon = PremiumGold,
            panelHeadline = PremiumGold,
        )

        MembershipStatus.FREE, MembershipStatus.OFFLINE -> MembershipCardPalette(
            surface = Brush.verticalGradient(
                listOf(
                    colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ),
            border = colorScheme.outlineVariant.copy(alpha = 0.4f),
            shadow = PremiumShadowNeutral.copy(alpha = 0.1f),
            foreground = colorScheme.onSurface,
            headerLabel = colorScheme.onSurfaceVariant,
            bodyText = colorScheme.onSurfaceVariant,
            accent = colorScheme.accentInk,
            panelBackground = colorScheme.onSurface.copy(alpha = 0.06f),
            panelIcon = colorScheme.accentInk,
            panelHeadline = colorScheme.onSurface,
        )
    }
}

/** The icon behind a [MembershipGlyph]. */
private fun glyphIcon(glyph: MembershipGlyph): ImageVector = when (glyph) {
    MembershipGlyph.VERIFIED -> Icons.Rounded.Verified
    MembershipGlyph.TICKET -> Icons.Rounded.ConfirmationNumber
    MembershipGlyph.LOCK -> Icons.Rounded.Lock
    MembershipGlyph.DATE -> Icons.Rounded.Event
    MembershipGlyph.CLOCK -> Icons.Rounded.Schedule
}

/**
 * The membership card: one layout for all three states.
 *
 * A subscriber, a ProPass holder and a free user are all asking the same question — what do I
 * have? — so they get the same card: what this is (header and badge), what it is called
 * (title), what the app actually knows about it (facts), and how it is paid for (panel). Only
 * the answers differ. That is what makes the three states comparable at a glance instead of
 * three unrelated designs the user has to re-read each time.
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
    onUpgradeClick: () -> Unit,
    onRedeemProPassClick: () -> Unit
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
    val cardShape = RoundedCornerShape(spacing.cardRadius + 12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = cardShape,
                ambientColor = palette.shadow,
                spotColor = palette.shadow
            )
            .clip(cardShape)
            .background(brush = palette.surface)
            .border(width = 1.dp, color = palette.border, shape = cardShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(spacing.paddingCompact)
        ) {
            MembershipCardHeader(spec = spec, palette = palette)

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
                    showRedeemAction = spec.showRedeemAction,
                    onUpgradeClick = onUpgradeClick,
                    onRedeemProPassClick = onRedeemProPassClick
                )
            }
        }
    }
}

@Composable
private fun MembershipCardHeader(spec: MembershipCardSpec, palette: MembershipCardPalette) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = glyphIcon(spec.glyph),
            contentDescription = null,
            tint = palette.headerLabel,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(Dimens.spacingSmall))

        // Weighted rather than fixed: at a large font size the label takes the width it needs
        // and pushes the badge along instead of colliding with it.
        Text(
            text = stringResource(spec.headerLabelRes),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = palette.headerLabel,
            modifier = Modifier.weight(1f)
        )

        spec.badgeRes?.let { badgeRes ->
            Spacer(modifier = Modifier.width(Dimens.spacingCompact))
            Text(
                text = stringResource(badgeRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = palette.foreground.copy(alpha = 0.9f)
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
                Icon(
                    imageVector = glyphIcon(fact.glyph),
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = membershipFactLabel(fact, datePatternRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = palette.foreground.copy(alpha = 0.9f)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = glyphIcon(panel.glyph),
                contentDescription = null,
                tint = palette.panelIcon,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(Dimens.spacingSmall))

            Text(
                text = stringResource(panel.headlineRes),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = palette.panelHeadline,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = stringResource(panel.bodyRes),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.foreground.copy(alpha = 0.85f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MembershipCardActions(
    @StringRes primaryActionRes: Int,
    showRedeemAction: Boolean,
    onUpgradeClick: () -> Unit,
    onRedeemProPassClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingCompact),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        Button(
            onClick = onUpgradeClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.cta,
                contentColor = colorScheme.onCta
            )
        ) {
            Text(
                text = stringResource(primaryActionRes),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (showRedeemAction) {
            AppTextButton(
                onClick = onRedeemProPassClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.accentInk)
            ) {
                Text(
                    text = stringResource(R.string.title_redeem_pro_pass),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
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
