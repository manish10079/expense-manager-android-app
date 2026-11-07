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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add

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


@Composable
internal fun AddTransactionOverlays(
    currencyId: Int,
    availableFunds: List<Fund>,
    fundRemainingById: Map<String, Long>,
    availableCategories: List<CategoryType>,
    availablePaymentMethods: List<PaymentType>,
    favorites: List<FavoriteTransaction>,
    selectedTags: List<Tag>,
    isFavorite: Boolean,
    effectiveFirstDueAt: Long,
    applyFavorite: (FavoriteTransaction) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onSelectFund: (String?) -> Unit,
    onSaveClick: (Transaction, RecurringTransactionDraft?, Boolean, List<String>) -> Unit,
    showDuplicateWarningState: androidx.compose.runtime.MutableState<Boolean>,
    duplicateWarningMessageState: androidx.compose.runtime.MutableState<String?>,
    pendingSaveTransactionState: androidx.compose.runtime.MutableState<Transaction?>,
    pendingSaveDraftState: androidx.compose.runtime.MutableState<RecurringTransactionDraft?>,
    showOverspendWarningState: androidx.compose.runtime.MutableState<Boolean>,
    pendingOverspendTransactionState: androidx.compose.runtime.MutableState<Transaction?>,
    pendingOverspendDraftState: androidx.compose.runtime.MutableState<RecurringTransactionDraft?>,
    pendingOverspendFundIdState: androidx.compose.runtime.MutableState<String?>,
    isEmiFirstDuePickerVisibleState: androidx.compose.runtime.MutableState<Boolean>,
    isRecurringModalVisibleState: androidx.compose.runtime.MutableState<Boolean>,
    emiFirstDueAtPickedState: androidx.compose.runtime.MutableState<Long?>,
    isFavoritesSheetVisibleState: androidx.compose.runtime.MutableState<Boolean>,
) {
    var showDuplicateWarning by showDuplicateWarningState
    var duplicateWarningMessage by duplicateWarningMessageState
    var pendingSaveTransaction by pendingSaveTransactionState
    var pendingSaveDraft by pendingSaveDraftState
    var showOverspendWarning by showOverspendWarningState
    var pendingOverspendTransaction by pendingOverspendTransactionState
    var pendingOverspendDraft by pendingOverspendDraftState
    var pendingOverspendFundId by pendingOverspendFundIdState
    var isEmiFirstDuePickerVisible by isEmiFirstDuePickerVisibleState
    var isRecurringModalVisible by isRecurringModalVisibleState
    var emiFirstDueAtPicked by emiFirstDueAtPickedState
    var isFavoritesSheetVisible by isFavoritesSheetVisibleState
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

        // Duplicate warning dialog
        if (showDuplicateWarning && duplicateWarningMessage != null) {
            ModernConfirmationDialog(
                onDismissRequest = {
                    showDuplicateWarning = false
                    duplicateWarningMessage = null
                    pendingSaveTransaction = null
                    pendingSaveDraft = null
                },
                title = stringResource(R.string.title_cannot_duplicate_recurring),
                text = duplicateWarningMessage ?: "",
                confirmButtonText = stringResource(R.string.label_yes),
                onConfirm = {
                    showDuplicateWarning = false
                    duplicateWarningMessage = null
                    val tx = pendingSaveTransaction
                    val draft = pendingSaveDraft
                    pendingSaveTransaction = null
                    pendingSaveDraft = null
                    keyboardController?.hide()
                    if (tx != null) onSaveClick(tx, draft, isFavorite, selectedTags.map { it.id })
                },
                headerIcon = Icons.Filled.ContentCopy,
                dismissButtonText = stringResource(R.string.label_no),
                onDismiss = {
                    showDuplicateWarning = false
                    duplicateWarningMessage = null
                    pendingSaveTransaction = null
                    pendingSaveDraft = null
                }
            )
        }

        // Fund overspend warning. Three ways out, all explicit: spend anyway, go back and
        // edit the amount, or drop the fund link and save it as ordinary spending.
        if (showOverspendWarning) {
            val overspendFundName = availableFunds
                .firstOrNull { it.id == pendingOverspendFundId }?.name
                .orEmpty()
            val overspendRemaining = pendingOverspendFundId?.let { fundRemainingById[it] } ?: 0L
            Dialog(
                onDismissRequest = {
                    showOverspendWarning = false
                    pendingOverspendTransaction = null
                    pendingOverspendDraft = null
                    pendingOverspendFundId = null
                }
            ) {
                QuickActionCard(
                    title = stringResource(R.string.title_fund_overspend),
                    caption = stringResource(
                        R.string.msg_fund_overspend,
                        overspendFundName,
                        formatCurrencyValue(overspendRemaining.coerceAtLeast(0L).toMajorUnits(), currencyId)
                    ),
                    headerIcon = Icons.Filled.Savings,
                    options = listOf(
                        QuickActionOption("spend_anyway", stringResource(R.string.label_spend_anyway), Icons.Filled.AccountBalanceWallet),
                        QuickActionOption("edit_amount", stringResource(R.string.label_edit_amount_action), Icons.Filled.EditNote),
                        QuickActionOption("remove_fund", stringResource(R.string.label_remove_fund), Icons.Filled.Close)
                    ),
                    selectedId = null,
                    onSelect = { id ->
                        when (id) {
                            "spend_anyway" -> {
                                val tx = pendingOverspendTransaction
                                val draft = pendingOverspendDraft
                                showOverspendWarning = false
                                pendingOverspendTransaction = null
                                pendingOverspendDraft = null
                                pendingOverspendFundId = null
                                keyboardController?.hide()
                                if (tx != null) onSaveClick(tx, draft, isFavorite, selectedTags.map { it.id })
                            }
                            "edit_amount" -> {
                                showOverspendWarning = false
                                pendingOverspendTransaction = null
                                pendingOverspendDraft = null
                                pendingOverspendFundId = null
                            }
                            "remove_fund" -> {
                                val tx = pendingOverspendTransaction?.copy(fundId = null)
                                val draft = pendingOverspendDraft
                                showOverspendWarning = false
                                pendingOverspendTransaction = null
                                pendingOverspendDraft = null
                                pendingOverspendFundId = null
                                onSelectFund(null)
                                keyboardController?.hide()
                                if (tx != null) onSaveClick(tx, draft, isFavorite, selectedTags.map { it.id })
                            }
                        }
                    }
                )
            }
        }

        // The recurring section steps out of its sheet so this picker (itself a
        // sheet) can open; confirming or cancelling returns to the plan form.
        if (isEmiFirstDuePickerVisible) {
            WheelDateTimePickerModal(
                mode = WheelPickerMode.SINGLE_DATE,
                initialStartMillis = effectiveFirstDueAt,
                onDismissRequest = {
                    isEmiFirstDuePickerVisible = false
                    isRecurringModalVisible = true
                },
                onConfirm = { start, _ ->
                    emiFirstDueAtPicked = start
                    isEmiFirstDuePickerVisible = false
                    isRecurringModalVisible = true
                }
            )
        }

        if (isFavoritesSheetVisible) {
            FavoritesBottomSheet(
                favorites = favorites,
                currencyId = currencyId,
                availableCategories = availableCategories,
                availablePaymentMethods = availablePaymentMethods,
                onDismiss = { isFavoritesSheetVisible = false },
                onSelect = applyFavorite,
                onDelete = { id ->
                    onRemoveFavorite(id)
                    Toast.makeText(context, context.getString(R.string.msg_favorite_removed), Toast.LENGTH_SHORT).show()
                }
            )
        }

}
