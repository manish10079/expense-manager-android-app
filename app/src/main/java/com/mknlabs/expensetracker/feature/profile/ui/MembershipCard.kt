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
internal fun MembershipCard(
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
internal fun MembershipCardHeader(
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
internal fun MembershipCardFacts(
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
internal fun membershipFactLabel(fact: MembershipFact, @StringRes datePatternRes: Int): String =
    when (fact) {
        is MembershipFact.Label -> stringResource(fact.labelRes)

        is MembershipFact.Date ->
            stringResource(fact.labelRes, rememberFormattedDate(fact.valueMillis, datePatternRes))

        is MembershipFact.Count ->
            pluralStringResource(fact.pluralsRes, fact.count, fact.count)
    }

/** Formats a timestamp once per value and pattern, falling back to N/A rather than crashing. */
@Composable
internal fun rememberFormattedDate(millis: Long, @StringRes patternRes: Int): String {
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
internal fun MembershipCardPanel(panel: MembershipPanel, palette: MembershipCardPalette) {
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
internal fun MembershipCardActions(
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
internal fun BenefitRow(
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

