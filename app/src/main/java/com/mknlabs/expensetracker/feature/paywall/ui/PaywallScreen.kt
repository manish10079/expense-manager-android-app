package com.mknlabs.expensetracker.feature.paywall.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AdaptiveContent
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.monetization.SubscriptionOffer
import com.mknlabs.expensetracker.utils.findFragmentActivity
import kotlinx.coroutines.delay

private const val TAG = "PaywallScreen"

/**
 * How long the purchase confirmation stays on screen before the paywall closes itself.
 *
 * Long enough to register the message, short enough that the close still reads as the
 * result of the purchase rather than as a delay. Deliberately shorter than the snackbar's
 * own timeout, which would hold a paid-for purchase screen up for four seconds.
 */
private const val PURCHASE_CONFIRMATION_HOLD_MILLIS = 1_500L

/**
 * Paywall route: owns the ViewModel, collects state and resolves the host Activity.
 *
 * The Activity is needed because RevenueCat's purchase call hands the Play sheet's result
 * back through it; it is resolved through `findFragmentActivity()` rather than a cast,
 * and it is never retained.
 */
@Composable
fun PaywallRoute(
    onBackClick: () -> Unit,
    onPrepareForExternalActivity: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // System back leaves the paywall rather than the app — same convention as every
    // other route.
    BackHandler {
        onBackClick()
    }

    // Deliberately an external browser rather than a WebView: the legal pages are the same
    // ones AboutScreen opens, and leaving the app must suppress the auto-lock first —
    // otherwise returning from the browser demands the PIN mid-purchase.
    val openUrl: (String) -> Unit = { url ->
        try {
            onPrepareForExternalActivity()
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No app can open $url", e)
        }
    }

    LaunchedEffect(uiState.messageRes) {
        val messageRes = uiState.messageRes ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(messageRes))
        // Released only after the message has actually been shown, so a configuration
        // change mid-snackbar cannot swallow it.
        viewModel.onOutcomeShown()
    }

    // A settled attempt closes the paywall. The user came here to buy, they have bought, and
    // a purchase screen they have already paid on is the bug this exists to prevent — the
    // confirmation above is what they get for their money, not another chance to pay twice.
    LaunchedEffect(uiState.isPurchaseSettled) {
        if (!uiState.isPurchaseSettled) return@LaunchedEffect
        delay(PURCHASE_CONFIRMATION_HOLD_MILLIS)
        // Consumed here rather than by the effect above, because leaving cancels that one
        // mid-snackbar and its acknowledgement would never run — and an outcome left
        // unconsumed is announced again the next time the paywall opens. Both calls below
        // are non-suspending, so clearing the state that keys this effect cannot cancel them
        // part-way: the last suspension point is the delay.
        viewModel.onOutcomeShown()
        onBackClick()
    }

    PaywallContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSubscribeClick = { offerId ->
            // Gated content can live in its own dialog window, where LocalContext is a
            // ContextThemeWrapper rather than the Activity — a plain `as? Activity` cast
            // would be null and no Play sheet would ever open.
            context.findFragmentActivity()?.let { activity ->
                viewModel.onSubscribeClick(activity, offerId)
            }
        },
        onRestoreClick = viewModel::onRestoreClick,
        onRetryClick = viewModel::onRetryClick,
        onOpenUrl = openUrl,
    )
}

/**
 * Pure paywall UI: no ViewModel, no state collection, everything through parameters, so it
 * renders in a Compose Preview.
 */
@Composable
internal fun PaywallContent(
    uiState: PaywallUiState,
    onSubscribeClick: (String) -> Unit,
    onRestoreClick: () -> Unit,
    onRetryClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // Selection is UI-local and survives rotation; the first plan is preselected so the
    // primary action is usable the moment plans arrive, with no dead initial tap.
    var selectedOfferId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedId = selectedOfferId ?: uiState.offers.firstOrNull()?.id

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        // The project's shared adaptive wrapper: it caps the column on tablets and
        // unfoldables so plans never stretch to an unreadable width, while a phone's
        // narrower parent wins, so nothing is ever clipped.
        AdaptiveContent(
            maxWidth = 560.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(
                    horizontal = Dimens.ScreenPadding,
                    vertical = Dimens.spacingSmall,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingDefault),
            ) {
                item { PaywallHero() }

                if (uiState.isPremium) {
                    item { AlreadyProNotice() }
                }

                item { ProBenefits() }

                item {
                    Text(
                        text = stringResource(R.string.label_paywall_choose_plan),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                when {
                    uiState.isLoadingOffers -> item { LoadingPlans() }

                    uiState.offers.isEmpty() -> item { PlansUnavailable(onRetryClick) }

                    else -> items(items = uiState.offers, key = { it.id }) { offer ->
                        PlanCard(
                            offer = offer,
                            isSelected = offer.id == selectedId,
                            isEnabled = !uiState.isBusy,
                            onSelect = { selectedOfferId = offer.id },
                        )
                    }
                }

                item {
                    SubscribeAction(
                        isEnabled = selectedId != null && !uiState.isBusy,
                        onSubscribeClick = { selectedId?.let(onSubscribeClick) },
                    )
                }

                // Play requires the auto-renewal terms to be visible on the purchase
                // screen itself, not only behind a link.
                item { RenewalDisclosure() }

                item {
                    AppTextButton(
                        onClick = onRestoreClick,
                        enabled = !uiState.isBusy,
                        modifier = Modifier.height(44.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.btn_restore_purchase),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        )
                    }
                }

                // Only for a subscriber the store gave a management page for; a
                // non-subscriber has nothing to manage.
                if (uiState.canManageSubscription) {
                    item {
                        AppTextButton(
                            onClick = { uiState.managementUrl?.let(onOpenUrl) },
                            enabled = !uiState.isBusy,
                            modifier = Modifier.height(44.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.btn_manage_subscription),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                    }
                }

                item { PaywallLegalLinks(onOpenUrl = onOpenUrl) }
            }
        }
    }
}

@Composable
private fun RenewalDisclosure() {
    Text(
        text = stringResource(R.string.msg_paywall_renewal_disclosure),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Terms and privacy links.
 *
 * Opens the app's existing legal URLs — the same `strings.xml` entries AboutScreen uses,
 * so there is a single source of truth for both places.
 */
@Composable
private fun PaywallLegalLinks(onOpenUrl: (String) -> Unit) {
    val termsUrl = stringResource(R.string.url_terms_conditions)
    val privacyUrl = stringResource(R.string.url_privacy_policy)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTextButton(
            onClick = { onOpenUrl(termsUrl) },
            modifier = Modifier.height(44.dp),
        ) {
            Text(
                text = stringResource(R.string.title_terms_conditions),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(Dimens.spacingSmall))
        AppTextButton(
            onClick = { onOpenUrl(privacyUrl) },
            modifier = Modifier.height(44.dp),
        ) {
            Text(
                text = stringResource(R.string.title_privacy_policy),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PaywallHero() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                        )
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spacingCompact))

        Text(
            text = stringResource(R.string.msg_paywall_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AlreadyProNotice() {
    Text(
        text = stringResource(R.string.msg_paywall_already_pro),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ProBenefits() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
    ) {
        Text(
            text = stringResource(R.string.label_paywall_benefits),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        )

        BenefitRow(stringResource(R.string.label_pro_benefit_adfree))
        BenefitRow(stringResource(R.string.label_pro_benefit_devices))
        BenefitRow(stringResource(R.string.label_pro_benefit_custom_card))
        BenefitRow(stringResource(R.string.label_pro_benefit_advanced_features))
        BenefitRow(stringResource(R.string.label_pro_benefit_many_more_features))
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            // Decorative: the benefit text beside it carries the meaning.
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
        }
        Spacer(modifier = Modifier.width(Dimens.spacingCompact))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun PlanCard(
    offer: SubscriptionOffer,
    isSelected: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // `selectable` rather than a plain click: it publishes the selected state to
            // accessibility services with a radio-button role, so the choice is announced
            // instead of just looking different.
            .selectable(
                selected = isSelected,
                enabled = isEnabled,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        shape = RoundedCornerShape(Dimens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingDefault),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(offer.planLabelRes),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val discountPercent = offer.discountPercent
                    if (discountPercent != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.paywall_discount_badge, discountPercent),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(offer.periodLabelRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (offer.strikethroughPriceText != null) {
                    Text(
                        text = offer.strikethroughPriceText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                Text(
                    text = offer.priceText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun SubscribeAction(
    isEnabled: Boolean,
    onSubscribeClick: () -> Unit,
) {
    Button(
        onClick = onSubscribeClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Text(
            text = stringResource(R.string.btn_paywall_subscribe),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun LoadingPlans() {
    val loadingDescription = stringResource(R.string.content_desc_loading_plans)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = loadingDescription },
        )
    }
}

@Composable
private fun PlansUnavailable(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
    ) {
        Text(
            text = stringResource(R.string.label_paywall_plans_unavailable),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.msg_paywall_plans_unavailable_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        AppTextButton(onClick = onRetryClick) {
            Text(text = stringResource(R.string.btn_paywall_retry))
        }
    }
}

@Preview(name = "Paywall - Light", showBackground = true)
@Composable
private fun PaywallPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        PaywallContent(
            uiState = PaywallPreviewState,
            onSubscribeClick = {},
            onRestoreClick = {},
            onRetryClick = {},
            onOpenUrl = {},
        )
    }
}

@Preview(name = "Paywall - Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PaywallPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        PaywallContent(
            uiState = PaywallPreviewState,
            onSubscribeClick = {},
            onRestoreClick = {},
            onRetryClick = {},
            onOpenUrl = {},
        )
    }
}

@Preview(name = "Paywall - Large font", showBackground = true, fontScale = 2f)
@Composable
private fun PaywallPreviewLargeFont() {
    ExpenseTrackerTheme(darkTheme = false) {
        PaywallContent(
            uiState = PaywallPreviewState,
            onSubscribeClick = {},
            onRestoreClick = {},
            onRetryClick = {},
            onOpenUrl = {},
        )
    }
}

/**
 * Literal demo state for the previews above.
 *
 * Kept inline and local to this file, matching how every other screen in the project feeds
 * its previews — no shared fake-data infrastructure is introduced for a preview to work.
 */
private val PaywallPreviewState = PaywallUiState(
    offers = listOf(
        SubscriptionOffer(
            id = "monthly",
            planLabelRes = R.string.paywall_plan_monthly,
            periodLabelRes = R.string.paywall_period_month,
            priceText = "₹149.00",
        ),
        SubscriptionOffer(
            id = "six_months",
            planLabelRes = R.string.paywall_plan_six_months,
            periodLabelRes = R.string.paywall_period_months,
            priceText = "₹749.00",
        ),
        SubscriptionOffer(
            id = "annual",
            planLabelRes = R.string.paywall_plan_twelve_months,
            periodLabelRes = R.string.paywall_period_year,
            priceText = "₹1,299.00",
        ),
    ),
    isLoadingOffers = false,
)
