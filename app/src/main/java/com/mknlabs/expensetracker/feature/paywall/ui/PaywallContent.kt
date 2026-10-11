package com.mknlabs.expensetracker.feature.paywall.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Crown
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.LoopingConfettiCanvas
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.SubscriptionOffer

@Composable
internal fun PaywallContent(
    uiState: PaywallUiState,
    onSubscribeClick: (String) -> Unit,
    onRestoreClick: () -> Unit,
    onRetryClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    snackbarHostState: androidx.compose.material3.SnackbarHostState = remember { androidx.compose.material3.SnackbarHostState() },
    isOpeningStore: Boolean = false,
    showProcessingOverlay: Boolean = false,
    onPurchaseSuccessConfirmed: () -> Unit = {},
    onErrorDismissed: () -> Unit = {},
) {
    val dark = isSystemInDarkTheme()
    val offers = remember(uiState.offers) { uiState.offers.sortedWith(paywallOfferOrder) }
    var selectedOfferId by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedOfferId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(offers.map { it.id }) {
        if (offers.isEmpty()) return@LaunchedEffect
        val preferred = offers.firstOrNull { it.planLabelRes == R.string.paywall_plan_twelve_months } ?: offers.first()
        if (selectedOfferId == null || offers.none { it.id == selectedOfferId }) {
            selectedOfferId = preferred.id
            expandedOfferId = preferred.id
        }
    }

    val selectedId = selectedOfferId ?: offers.firstOrNull()?.id
    val selected = offers.firstOrNull { it.id == selectedId }
    val screenBg = if (dark) PaywallScreenDark else PaywallScreenLight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            ) {
                PaywallHero(dark = dark)
                Spacer(Modifier.height(16.dp))
                when {
                    uiState.isLoadingOffers && offers.isEmpty() -> LoadingPlansPlaceholder()
                    uiState.isPlansUnavailable -> PlansUnavailable(onRetryClick)
                    else -> {
                        offers.forEachIndexed { index, offer ->
                            if (index > 0) Spacer(Modifier.height(8.dp))
                            PaywallPlanCard(
                                offer = offer,
                                dark = dark,
                                selected = offer.id == selectedId,
                                expanded = offer.id == expandedOfferId,
                                enabled = !uiState.isBusy,
                                onClick = {
                                    if (offer.id == selectedId) {
                                        expandedOfferId = if (expandedOfferId == offer.id) null else offer.id
                                    } else {
                                        selectedOfferId = offer.id
                                        expandedOfferId = offer.id
                                    }
                                },
                            )
                        }
                    }
                }
            }

            PaywallFooter(
                dark = dark,
                selected = selected,
                isBusy = uiState.isBusy,
                isOpeningStore = isOpeningStore,
                onSubscribeClick = { selectedId?.let(onSubscribeClick) },
                onOpenUrl = onOpenUrl,
            )
        }

        if (showProcessingOverlay) {
            PaymentProcessingOverlay(dark = dark)
        }
        if (uiState.isPurchaseSuccess) {
            PurchaseSuccessOverlay(dark = dark, onConfirm = onPurchaseSuccessConfirmed)
        }
        if (uiState.purchaseState is PurchaseState.Failed &&
            uiState.purchaseState.operation == PurchaseState.Operation.Purchase
        ) {
            PaymentErrorOverlay(onClose = onErrorDismissed)
        }
    }
}

private val paywallOfferOrder = Comparator<SubscriptionOffer> { a, b ->
    fun rank(res: Int) = when (res) {
        R.string.paywall_plan_twelve_months -> 0
        R.string.paywall_plan_six_months -> 1
        R.string.paywall_plan_monthly -> 2
        else -> 3
    }
    rank(a.planLabelRes).compareTo(rank(b.planLabelRes))
}

@Composable
private fun PaywallHero(dark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x406366F1), spotColor = Color(0x406366F1))
                .clip(RoundedCornerShape(24.dp))
                .background(paywallBrandGradient()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Crown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(45.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.label_upgrade_to_pro),
            style = TextStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                brush = paywallBrandGradient(),
            ),
        )
    }
}

@Composable
private fun PaywallPlanCard(
    offer: SubscriptionOffer,
    dark: Boolean,
    selected: Boolean,
    expanded: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val isAnnual = offer.planLabelRes == R.string.paywall_plan_twelve_months
    val isSix = offer.planLabelRes == R.string.paywall_plan_six_months
    val isMonth = offer.planLabelRes == R.string.paywall_plan_monthly
    val showBestValue = isAnnual && offer.discountPercent != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (showBestValue) 18.dp else 0.dp)
            .graphicsLayer { clip = false },
    ) {
        AppCard(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
        ) {
            Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlanRadio(selected = selected, dark = dark)
                Spacer(Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(
                                when {
                                    isAnnual -> R.string.paywall_plan_title_annual
                                    isSix -> R.string.paywall_plan_title_six
                                    isMonth -> R.string.paywall_plan_title_month
                                    else -> offer.planLabelRes
                                }
                            ),
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                        )
                        if (isSix && offer.discountPercent != null) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.paywall_discount_badge, offer.discountPercent),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 11.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(paywallBrandGradient())
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                            )
                        }
                    }
                    if (isMonth) {
                        Text(
                            text = stringResource(R.string.paywall_standard_recurring),
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            color = if (dark) PaywallSlate400 else PaywallSlate500,
                            maxLines = 1,
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = offer.priceText.ifBlank { "—" },
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dark) PaywallSlate400 else PaywallSlate500,
                                maxLines = 1,
                            )
                            if (offer.strikethroughPriceText != null) {
                                Text(
                                    text = offer.strikethroughPriceText,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    color = if (dark) PaywallSlate600 else PaywallSlate400,
                                    style = TextStyle(textDecoration = TextDecoration.LineThrough),
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    val perMonth = offer.perMonthPriceText ?: "—"
                    val priceColor = if (dark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = perMonth,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = priceColor,
                        )
                        Text(
                            text = stringResource(R.string.paywall_per_month_suffix),
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = PaywallSlate400,
                        )
                    }
                    if (offer.savingsPriceText != null) {
                        Text(
                            text = stringResource(R.string.paywall_save_amount, offer.savingsPriceText),
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PaywallEmerald,
                        )
                    } else if (isMonth) {
                        Text(
                            text = stringResource(R.string.paywall_base_tier),
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            color = PaywallSlate400,
                        )
                    }
                }
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(200),
                    label = "chevron",
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = PaywallSlate400,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = rotation },
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(250, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = shrinkVertically(tween(250, easing = FastOutSlowInEasing)) + fadeOut(),
            ) {
                PlanBenefits(offer = offer, dark = dark)
            }
            }
        }
        if (showBestValue) {
            Text(
                text = stringResource(R.string.paywall_best_value_save, offer.discountPercent!!),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp, y = (-18).dp)
                    .shadow(4.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(paywallBrandGradient())
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun PlanRadio(selected: Boolean, dark: Boolean) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .then(
                if (selected) Modifier.background(paywallBrandGradient())
                else Modifier.border(
                    2.dp,
                    if (dark) PaywallSlate600 else PaywallSlate400,
                    CircleShape,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.content_desc_paywall_plan_selected),
                tint = Color.White,
                modifier = Modifier.size(9.dp),
            )
        }
    }
}

@Composable
private fun PlanBenefits(offer: SubscriptionOffer, dark: Boolean) {
    val isAnnual = offer.planLabelRes == R.string.paywall_plan_twelve_months
    val isSix = offer.planLabelRes == R.string.paywall_plan_six_months
    val header = when {
        isAnnual -> R.string.paywall_benefits_header_annual to Icons.Filled.AutoAwesome
        isSix -> R.string.paywall_benefits_header_six to Icons.Filled.Check
        else -> R.string.paywall_benefits_header_month to Icons.Outlined.Star
    }
    val rows = when {
        isAnnual -> listOf(
            R.string.paywall_benefit_ad_free,
            R.string.paywall_benefit_txn_details,
            R.string.paywall_benefit_device_sync,
            R.string.paywall_benefit_favorites,
            R.string.paywall_benefit_recurring_yearly,
            R.string.paywall_benefit_copy_budgets,
            R.string.paywall_benefit_ai_backups,
            R.string.paywall_benefit_lock_fonts_tags,
            R.string.paywall_benefit_insights_alerts,
            R.string.paywall_benefit_many_more,
        )
        isSix -> listOf(
            R.string.paywall_benefit_ad_free,
            R.string.paywall_benefit_txn_details,
            R.string.paywall_benefit_device_sync,
            R.string.paywall_benefit_favorites,
            R.string.paywall_benefit_recurring_copy,
            R.string.paywall_benefit_ai_backup,
            R.string.paywall_benefit_lock_fonts,
            R.string.paywall_benefit_separators,
            R.string.paywall_benefit_many_more,
        )
        else -> listOf(
            R.string.paywall_benefit_ad_free,
            R.string.paywall_benefit_txn_details,
            R.string.paywall_benefit_device_sync,
            R.string.paywall_benefit_favorites,
            R.string.paywall_benefit_recurring_only,
            R.string.paywall_benefit_ai_daily,
            R.string.paywall_benefit_lock_fonts_short,
            R.string.paywall_benefit_many_more,
        )
    }
    val ink = if (dark) PaywallSlate300 else PaywallSlate600
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .border(
                width = 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(0),
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(if (dark) Color(0xCC1E293B) else Color(0xFFE2E8F0)),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = header.second,
                contentDescription = null,
                tint = if (dark) PaywallIndigoDark else PaywallIndigoLight,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(header.first),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) PaywallIndigoDark else PaywallIndigoLight,
            )
        }
        rows.forEach { res ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = PaywallEmerald,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(10.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(res),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp,
                    color = ink,
                )
            }
        }
    }
}

@Composable
private fun PaywallFooter(
    dark: Boolean,
    selected: SubscriptionOffer?,
    isBusy: Boolean,
    isOpeningStore: Boolean,
    onSubscribeClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "ctaScale")
    val period = when (selected?.planLabelRes) {
        R.string.paywall_plan_twelve_months -> stringResource(R.string.paywall_cta_period_annual)
        R.string.paywall_plan_six_months -> stringResource(R.string.paywall_cta_period_six)
        R.string.paywall_plan_monthly -> stringResource(R.string.paywall_cta_period_month)
        else -> selected?.let { stringResource(it.planLabelRes) }.orEmpty()
    }
    val perMonth = selected?.perMonthPriceText ?: "—"
    val cta = if (selected == null) {
        stringResource(R.string.btn_paywall_subscribe)
    } else {
        stringResource(R.string.paywall_cta_continue, period, perMonth)
    }
    val termsUrl = stringResource(R.string.url_terms_conditions)
    val privacyUrl = stringResource(R.string.url_privacy_policy)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .shadow(10.dp, RoundedCornerShape(12.dp), ambientColor = PaywallBrandStart.copy(alpha = 0.4f), spotColor = PaywallBrandStart.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(12.dp))
                .background(paywallBrandGradient())
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = selected != null && !isBusy && selected.perMonthPriceText != null,
                    onClick = onSubscribeClick,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isOpeningStore) {
                val opening = stringResource(R.string.content_desc_opening_play_store)
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .semantics { contentDescription = opening },
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cta,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.paywall_terms_of_use),
                fontSize = 11.sp,
                color = if (dark) PaywallSlate500 else PaywallSlate400,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onOpenUrl(termsUrl) },
            )
            Text(
                text = "  •  ",
                fontSize = 11.sp,
                color = if (dark) PaywallSlate500 else PaywallSlate400,
            )
            Text(
                text = stringResource(R.string.title_privacy_policy),
                fontSize = 11.sp,
                color = if (dark) PaywallSlate500 else PaywallSlate400,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onOpenUrl(privacyUrl) },
            )
        }
    }
}

@Composable
private fun LoadingPlansPlaceholder() {
    val loading = stringResource(R.string.content_desc_loading_plans)
    Box(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .semantics { contentDescription = loading },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = PaywallBrandStart, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun PlansUnavailable(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.label_paywall_plans_unavailable), color = Color.White, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.btn_paywall_retry),
            color = PaywallBrandStart,
            modifier = Modifier.clickable(onClick = onRetryClick),
        )
    }
}

@Composable
private fun PaymentProcessingOverlay(dark: Boolean) {
    val cardColors = if (dark) {
        AppCardColors(
            containerColor = PaywallOverlayCard,
            contentColor = Color.White,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        )
    } else {
        AppCardDefaults.colors()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (dark) Color.Black.copy(alpha = 0.62f) else PaywallScreenLight.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center,
    ) {
        AppCard(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = cardColors,
            elevation = if (dark) 24.dp else AppCardDefaults.Elevation,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PayProcessingSpinner(dark = dark)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.paywall_processing_payment),
                    color = if (dark) Color.White else Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.paywall_please_wait),
                    color = if (dark) PaywallSlate400 else PaywallSlate500,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun PayProcessingSpinner(dark: Boolean) {
    val spin = rememberInfiniteTransition(label = "paySpin")
    val angle by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "paySpinAngle",
    )
    val track = if (dark) Color.White.copy(alpha = 0.18f) else PaywallBrandStart.copy(alpha = 0.20f)
    Canvas(
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer { rotationZ = angle },
    ) {
        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Butt)
        drawCircle(color = track, style = stroke)
        drawArc(
            color = PaywallBrandStart,
            startAngle = -90f,
            sweepAngle = 90f,
            useCenter = false,
            style = stroke,
        )
    }
}

@Composable
private fun PurchaseSuccessOverlay(dark: Boolean, onConfirm: () -> Unit) {
    val overlay = if (dark) {
        Brush.radialGradient(
            colors = listOf(PaywallBrandStart.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.88f)),
            center = Offset.Unspecified,
        )
    } else {
        Brush.radialGradient(
            colors = listOf(PaywallBrandStart.copy(alpha = 0.28f), PaywallScreenLight.copy(alpha = 0.94f)),
        )
    }
    val infinite = rememberInfiniteTransition(label = "proShine")
    val shine by infinite.animateFloat(
        initialValue = 1.2f,
        targetValue = -1.2f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "shine",
    )
    val tilt by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "tilt",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlay),
        contentAlignment = Alignment.Center,
    ) {
        LoopingConfettiCanvas(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .width(280.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer { alpha = 0.55f }
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0x73FFD778), PaywallBrandStart.copy(alpha = 0.15f), Color.Transparent),
                        ),
                        CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .offset(y = (-120).dp)
                    .size(88.dp)
                    .graphicsLayer {
                        rotationZ = tilt
                        scaleX = 1.08f
                        scaleY = 1.08f
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(paywallBrandGradient()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Crown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp),
                )
            }
            Text(
                text = stringResource(R.string.paywall_youre_pro_now),
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5B3FD6),
                            Color(0xFFA78BFA),
                            Color.White,
                            Color(0xFFFFE08A),
                            Color.White,
                            Color(0xFFC4B5FD),
                            Color(0xFF6A4DFF),
                        ),
                        start = Offset(shine * 400f, 0f),
                        end = Offset(shine * 400f + 280f, 80f),
                    ),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-100).dp),
            )
            Text(
                text = stringResource(R.string.paywall_congrats_purchase),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (dark) PaywallSlate300 else Color(0xFF1E293B),
                modifier = Modifier.offset(y = (-90).dp),
            )
            Text(
                text = stringResource(R.string.paywall_invested_in_yourself),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (dark) Color.White else Color(0xFF0F172A),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-80).dp),
            )
            Box(
                modifier = Modifier
                    .offset(y = (-64).dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(paywallBrandGradient())
                    .clickable(onClick = onConfirm)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.paywall_start_using_pro),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun PaymentErrorOverlay(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PaywallOverlayCard)
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Red.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.paywall_payment_failed),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.paywall_error_unknown),
                color = PaywallSlate400,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF334155))
                    .clickable(onClick = onClose)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.btn_close),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
