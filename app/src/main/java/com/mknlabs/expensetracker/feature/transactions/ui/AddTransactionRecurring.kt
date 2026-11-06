package com.mknlabs.expensetracker.feature.transactions.ui

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedButton
import com.mknlabs.expensetracker.feature.funds.ui.CreateFundSheet
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.chipOutline
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.MicrophoneStage
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.Star
import com.adamglin.phosphoricons.regular.Star
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AdRewardDialog
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.core.ui.models.TabItem
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.PremiumGateSheet
import com.mknlabs.expensetracker.core.ui.components.QuickActionCard
import com.mknlabs.expensetracker.core.ui.components.QuickActionOption
import com.mknlabs.expensetracker.core.ui.components.VoiceInputSheet
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.horizontalSwipe
import com.mknlabs.expensetracker.core.ui.navigation.LocalUpgradeToPro
import com.mknlabs.expensetracker.core.ui.theme.CardShadowAmbientLight
import com.mknlabs.expensetracker.core.ui.theme.CardShadowSpotLight
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.appHeaderTitle
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.identityColor
import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.chipSelectedInk
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.formCard
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.typeSwitcherFill
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.paymentColor
import com.mknlabs.expensetracker.core.ui.theme.recurringIcon
import com.mknlabs.expensetracker.core.ui.theme.recurringTile
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.theme.switchOnThumb
import com.mknlabs.expensetracker.core.ui.theme.switchOnTick
import com.mknlabs.expensetracker.core.ui.theme.switchOnTrack
import com.mknlabs.expensetracker.core.ui.theme.tagInk
import com.mknlabs.expensetracker.core.ui.theme.tagWash
import com.mknlabs.expensetracker.core.ui.theme.textTertiary
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.DEFAULT_PAYMENT_TYPE_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_TRANSACTION_TYPE_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.CurrencyPosition
import com.mknlabs.expensetracker.models.FavoriteTransaction
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringPlanEdit
import com.mknlabs.expensetracker.models.RecurringTransactionDraft
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.RecurringType
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.monetization.RecurringGateResolver
import com.mknlabs.expensetracker.monetization.RecurringRuleTier
import com.mknlabs.expensetracker.utils.USAGE_RANKING_WINDOW_MS
import com.mknlabs.expensetracker.utils.findFragmentActivity
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mknlabs.expensetracker.utils.getCurrency
import com.mknlabs.expensetracker.utils.getRankedCategories
import com.mknlabs.expensetracker.utils.getRankedPaymentMethods
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.toMinorUnits
import com.mknlabs.expensetracker.voice.VoiceInputUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

internal data class RecurringModeOption(
    val frequency: RecurringFrequency,
    @androidx.annotation.StringRes val label: Int
)

internal val recurringModeOptions = listOf(
    RecurringModeOption(RecurringFrequency.Daily, R.string.label_daily),
    RecurringModeOption(RecurringFrequency.Weekly, R.string.label_weekly),
    RecurringModeOption(RecurringFrequency.Monthly, R.string.label_monthly),
    RecurringModeOption(RecurringFrequency.Yearly, R.string.label_yearly)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecurringTransactionSection(
    isEnabled: Boolean,
    selectedFrequency: RecurringFrequency,
    repeatCountInput: String,
    compact: Boolean,
    activeRecurringRuleCount: Int = 0,
    hasExistingRule: Boolean = false,
    canCreateInstallment: Boolean = false,
    selectedType: RecurringType = RecurringType.REGULAR,
    emiTotalInput: String = "",
    emiInstallmentInput: String = "",
    emiFirstDueText: String = "",
    isEmiPlanValid: Boolean = false,
    onEnabledChange: (Boolean) -> Unit,
    onFrequencySelected: (RecurringFrequency) -> Unit,
    onRepeatCountChange: (String) -> Unit,
    onTypeSelected: (RecurringType) -> Unit = {},
    onEmiTotalChange: (String) -> Unit = {},
    onEmiInstallmentChange: (String) -> Unit = {},
    onEmiFirstDueClick: () -> Unit = {}
) {
    // INSTALLMENT is only offered while creating; the recurring-list editor owns
    // changing an existing rule's plan.
    val isInstallment = canCreateInstallment && selectedType == RecurringType.INSTALLMENT
    val planCount = repeatCountInput.toIntOrNull() ?: 0
    val context = LocalContext.current
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    var showPremiumSheet by remember { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf(false) }
    var pendingFrequencyForAd by remember { mutableStateOf<RecurringFrequency?>(null) }

    // Access is resolved by the monetization layer, which owns the Pro bypass and the
    // pro_gating_enabled kill switch. This section only asks; it never decides.
    val enableFeature = RecurringGateResolver.enableFeature(
        activeRuleCount = activeRecurringRuleCount,
        hasExistingRule = hasExistingRule
    )
    val ruleCountGate: AccessStatus = if (enableFeature != null) {
        monetizationViewModel.getAccessStatus(enableFeature).collectAsStateWithLifecycle().value
    } else {
        AccessStatus.Granted
    }

    // Pro users are gated nowhere, so the ladder would be noise for them.
    val userTier by monetizationViewModel.userTier.collectAsStateWithLifecycle()
    val showRuleLadder = userTier != UserTier.PREMIUM
    val colorScheme = MaterialTheme.colorScheme
    val animatedBorderColor by animateColorAsState(
        targetValue = if (isEnabled) Color.Transparent 
                     else colorScheme.outlineVariant.copy(alpha = 0.3f),
        label = "recurring_section_border"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SolidColor(Color.Transparent))
            .border(
                width = if (isEnabled) 0.dp else 1.dp,
                color = if (isEnabled) Color.Transparent else animatedBorderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled) colorScheme.accentInk.copy(alpha = 0.12f)
                        else colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (isEnabled) colorScheme.accentInk else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.label_recurring_transaction),
                    color = if (isEnabled) colorScheme.accentInk else colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            androidx.compose.material3.Switch(
                modifier = Modifier.scale(0.9f),
                checked = isEnabled,
                onCheckedChange = { wantEnabled ->
                    if (wantEnabled && ruleCountGate !is AccessStatus.Granted) {
                        // Gate: rule count threshold
                        when (ruleCountGate) {
                            is AccessStatus.DeniedAd -> showAdDialog = true
                            is AccessStatus.DeniedPremium -> showPremiumSheet = true
                            else -> onEnabledChange(true)
                        }
                    } else {
                        onEnabledChange(wantEnabled)
                    }
                },
                thumbContent = if (isEnabled) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(androidx.compose.material3.SwitchDefaults.IconSize),
                            tint = colorScheme.switchOnTick
                        )
                    }
                } else {
                    null
                },
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.switchOnThumb,
                    checkedTrackColor = colorScheme.switchOnTrack,
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = colorScheme.textTertiary,
                    uncheckedTrackColor = colorScheme.outlineVariant.copy(alpha = 0.45f),
                    uncheckedBorderColor = colorScheme.textTertiary
                )
            )
        }

        // Informational only: it explains the ladder before a gate ever fires.
        if (showRuleLadder) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.accentSoft)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.desc_recurring_rule_ladder),
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = when (RecurringGateResolver.ruleTier(activeRecurringRuleCount)) {
                        RecurringRuleTier.FREE -> stringResource(
                            R.string.desc_recurring_rules_used_free,
                            activeRecurringRuleCount,
                            RecurringGateResolver.FREE_RULE_LIMIT
                        )
                        RecurringRuleTier.AD -> stringResource(
                            R.string.desc_recurring_rules_used_ad,
                            activeRecurringRuleCount,
                            RecurringGateResolver.AD_RULE_LIMIT
                        )
                        RecurringRuleTier.PREMIUM -> stringResource(
                            R.string.desc_recurring_rules_used_premium,
                            activeRecurringRuleCount
                        )
                    },
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        if (isEnabled) {
            val sectionHeaderColor = if (isEnabled) colorScheme.accentInk else colorScheme.onSurfaceVariant
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (canCreateInstallment) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = stringResource(R.string.label_recurring_type), color = sectionHeaderColor)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = !isInstallment,
                                onClick = { onTypeSelected(RecurringType.REGULAR) },
                                label = { Text(stringResource(R.string.label_type_regular)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorScheme.chipSelected,
                                    selectedLabelColor = colorScheme.chipSelectedInk
                                )
                            )
                            FilterChip(
                                selected = isInstallment,
                                onClick = { onTypeSelected(RecurringType.INSTALLMENT) },
                                label = { Text(stringResource(R.string.label_type_emi)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorScheme.chipSelected,
                                    selectedLabelColor = colorScheme.chipSelectedInk
                                )
                            )
                        }
                        if (isInstallment) {
                            Text(
                                text = stringResource(R.string.desc_emi_plan_toggle),
                                color = colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // Frequency Selector (Sliding Pill)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(title = stringResource(R.string.title_frequency), color = sectionHeaderColor)
                    
                    val density = LocalDensity.current
                    var containerWidthPx by remember { mutableIntStateOf(0) }
                    val selectedIndex = recurringModeOptions.indexOfFirst { it.frequency == selectedFrequency }.coerceAtLeast(0)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .onSizeChanged { containerWidthPx = it.width }
                            .clip(RoundedCornerShape(20.dp))
                            // Light: the specified secondary surface, so the sliding brand
                            // pill reads as the selected segment of a control.
                            .background(SolidColor(if (colorScheme.isDark) Color.Transparent else colorScheme.surfaceVariant))
                            .border(
                                width = 1.dp,
                                color = if (colorScheme.isDark) colorScheme.outlineVariant.copy(alpha = 0.2f) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(4.dp)
                    ) {
                        val tabWidth = with(density) { (containerWidthPx.toDp() - 8.dp) / recurringModeOptions.size }
                        
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * selectedIndex,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            label = "recurring_freq_indicator_offset"
                        )

                        if (containerWidthPx > 0) {
                            Box(
                                modifier = Modifier
                                    .offset(x = indicatorOffset)
                                    .width(tabWidth)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(brandGradient())
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            recurringModeOptions.forEach { option ->
                                val selected = option.frequency == selectedFrequency
                                // Frequency access comes from the monetization layer, so a Pro user is
                                // never asked to watch an ad and the kill switch still applies.
                                val frequencyFeature = RecurringGateResolver.frequencyFeature(option.frequency)
                                val freqGate: AccessStatus = if (frequencyFeature != null) {
                                    monetizationViewModel.getAccessStatus(frequencyFeature)
                                        .collectAsStateWithLifecycle().value
                                } else {
                                    AccessStatus.Granted
                                }
                                val animatedColor by animateColorAsState(
                                    targetValue = if (selected) colorScheme.onCta else colorScheme.onSurfaceVariant,
                                    label = "recurring_freq_text_color"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                        if (freqGate !is AccessStatus.Granted) {
                                            pendingFrequencyForAd = option.frequency
                                            when (freqGate) {
                                                is AccessStatus.DeniedAd -> showAdDialog = true
                                                is AccessStatus.DeniedPremium -> showPremiumSheet = true
                                                else -> onFrequencySelected(option.frequency)
                                            }
                                        } else {
                                            onFrequencySelected(option.frequency)
                                        }
                                    }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(option.label),
                                        color = animatedColor,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Plan terms — only the count below is shared with REGULAR.
                if (isInstallment) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = stringResource(R.string.label_emi_total_amount), color = sectionHeaderColor)
                        EmiAmountField(
                            value = emiTotalInput,
                            onValueChange = onEmiTotalChange,
                            supportingText = stringResource(R.string.desc_emi_total_amount)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = stringResource(R.string.label_emi_installment_amount), color = sectionHeaderColor)
                        EmiAmountField(
                            value = emiInstallmentInput,
                            onValueChange = onEmiInstallmentChange,
                            supportingText = stringResource(R.string.desc_emi_installment_amount)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(title = stringResource(R.string.label_emi_first_due), color = sectionHeaderColor)
                        OutlinedTextField(
                            value = emiFirstDueText,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onEmiFirstDueClick),
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = colorScheme.accentInk
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = MaterialTheme.colorScheme.accentInk,
                                disabledTextColor = colorScheme.onSurface,
                                disabledBorderColor = if (colorScheme.isDark) colorScheme.outlineVariant else colorScheme.outline,
                                disabledContainerColor = if (colorScheme.isDark) colorScheme.surfaceVariant else colorScheme.surface
                            )
                        )
                    }

                    // A half-filled plan is not an error yet — only an inconsistent
                    // one is, exactly as in the recurring-list editor.
                    if (!isEmiPlanValid && emiTotalInput.isNotBlank() && emiInstallmentInput.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.msg_emi_amount_mismatch),
                            color = colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Installments Picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(title = stringResource(R.string.label_total_installments), color = sectionHeaderColor)
                    
                    val presetInstallments = listOf("3", "6", "12", "24")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetInstallments.forEach { count ->
                            val isSelected = repeatCountInput == count
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            isSelected -> SolidColor(colorScheme.accentSoft)
                                            colorScheme.isDark -> standardCardGradient()
                                            else -> SolidColor(colorScheme.surfaceVariant)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) colorScheme.accentInk else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onRepeatCountChange(count) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = count,
                                    color = if (isSelected) colorScheme.accentInk else colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        // Custom Input Field
                        OutlinedTextField(
                            value = if (repeatCountInput in presetInstallments) "" else repeatCountInput,
                            onValueChange = { if (it.length <= 3) onRepeatCountChange(it) },
                            modifier = Modifier.weight(1.2f).heightIn(min = 44.dp),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.label_other_installment), style = MaterialTheme.typography.bodyMedium) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = MaterialTheme.colorScheme.accentInk,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = colorScheme.accentInk,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = colorScheme.accentInk
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colorScheme.secondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isInstallment) {
                                stringResource(
                                    R.string.msg_emi_create_info,
                                    (planCount - 1).coerceAtLeast(0)
                                )
                            } else {
                                stringResource(R.string.msg_installment_info)
                            },
                            color = colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall.copy(
                                lineHeight = 14.sp,
                                letterSpacing = 0.2.sp
                            )
                        )
                    }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 52.dp, top = 4.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.label_recurring_track),
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic
                    )
                )
            }
        }
    }

    // Ad dialog for frequency gating and rule count gating
    if (showAdDialog) {
        val isAdLoading by monetizationViewModel.isAdLoading.collectAsStateWithLifecycle()
        val adPassMinutes by monetizationViewModel.adPassDurationMinutes.collectAsStateWithLifecycle()
        AdRewardDialog(
            featureName = pendingFrequencyForAd?.label
                ?: stringResource(R.string.label_recurring_transaction),
            durationMinutes = adPassMinutes,
            isLoading = isAdLoading,
            onDismiss = { showAdDialog = false; pendingFrequencyForAd = null },
            onWatchAdClick = {
                // This section is composed inside the recurring ModalBottomSheet, which is
                // hosted in its own dialog window Ã¢â‚¬â€ LocalContext there is a
                // ContextThemeWrapper, not the Activity, so a plain `as? Activity` cast is
                // null and the rewarded ad is never even requested. Unwrap like MainScreen.
                val activity = context.findFragmentActivity()
                // Grant access once it is real Ã¢â‚¬â€ reward earned, or no ad to show. Applying
                // the pending choice up front would unlock the rule without an ad ever
                // appearing (frequency gate: select it; rule-count gate: enable it).
                val applyGrantedChoice = {
                    pendingFrequencyForAd?.let { onFrequencySelected(it) }
                    onEnabledChange(true)
                    showAdDialog = false
                    pendingFrequencyForAd = null
                }
                if (activity != null) {
                    monetizationViewModel.onAdWatched(
                        activity,
                        pendingFrequencyForAd?.let { featureForFrequency(it) }
                            ?: Feature.RECURRING_RULES_MULTI,
                        null,
                        applyGrantedChoice
                    )
                } else {
                    // No host Activity means no ad can ever be shown; don't strand the
                    // user on a button that can't do anything.
                    Log.w("AddTransactionScreen", "No host Activity for the recurring ad gate; granting directly.")
                    applyGrantedChoice()
                }
            }
        )
    }

    // Premium sheet for Yearly frequency and 7th+ rule
    val upgradeToPro = LocalUpgradeToPro.current
    if (showPremiumSheet) {
        PremiumGateSheet(
            onDismiss = { showPremiumSheet = false; pendingFrequencyForAd = null },
            onUpgradeClick = {
                showPremiumSheet = false
                upgradeToPro()
            }
        )
    }
}

/** Maps a frequency to its gating Feature. */
internal fun featureForFrequency(freq: RecurringFrequency): Feature = when (freq) {
    RecurringFrequency.Daily -> Feature.RECURRING_FREQUENCY_DAILY
    RecurringFrequency.Weekly -> Feature.RECURRING_FREQUENCY_WEEKLY
    RecurringFrequency.Yearly -> Feature.RECURRING_FREQUENCY_YEARLY
    RecurringFrequency.Monthly -> Feature.RECURRING_FREQUENCY_MONTHLY
}

@Composable
internal fun RecurringCompactCard(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    frequency: RecurringFrequency,
    isInstallment: Boolean = false,
    compact: Boolean,
    onClick: () -> Unit
) {
    val frequencyLabel = stringResource(recurringModeOptions.first { it.frequency == frequency }.label)
    SelectionInfoCard(
        modifier = modifier,
        leadingIcon = Icons.Default.CalendarMonth,
        label = stringResource(R.string.label_recurring),
        value = when {
            !isEnabled -> stringResource(R.string.label_off)
            isInstallment -> stringResource(R.string.label_emi_frequency_format, frequencyLabel)
            else -> frequencyLabel
        },
        isPlaceholder = !isEnabled,
        highlighted = isEnabled,
        compact = compact,
        // The mockup's recurring row is a neutral slate tile with a muted glyph in both
        // themes, so the icon no longer switches to the accent when the switch is on.
        iconTint = MaterialTheme.colorScheme.recurringIcon,
        iconContainerColor = MaterialTheme.colorScheme.recurringTile,
        onClick = onClick
    )
}
