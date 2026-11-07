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

internal const val incomeTypeId = 1
internal const val expenseTypeId = 2


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    transactions: List<Transaction> = emptyList(),
    availableCategories: List<CategoryType> = categoryMap.values.toList(),
    availablePaymentMethods: List<PaymentType> = paymentTypeMap.values.sortedBy { it.id },
    existingTransaction: Transaction? = null,
    existingRecurringRule: RecurringTransactionRule? = null,
    activeRecurringRuleCount: Int = 0,
    allRecurringRules: List<RecurringTransactionRule> = emptyList(),
    initialAmountInput: String? = null,
    initialNote: String? = null,
    initialCategoryId: Int? = null,
    initialTransactionTypeId: Int? = null,
    initialPaymentTypeId: Int? = null,
    initialCreatedAt: Long? = null,
    autoStartVoice: Boolean = false,
    onVoiceAutoStarted: () -> Unit = {},
    favorites: List<FavoriteTransaction> = emptyList(),
    onRemoveFavorite: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onCalculatorClick: () -> Unit = {},
    onAmountInputChange: (String) -> Unit = {},
    onNoteChange: (String) -> Unit = {},
    onSaveClick: (Transaction, RecurringTransactionDraft?, Boolean, List<String>) -> Unit = { _, _, _, _ -> }
) {
    // --- Route layer: Hilt collaborators and the Android voice plumbing. ---
    val paymentMethodPredictorViewModel: PaymentMethodPredictorViewModel = hiltViewModel()
    val predictedPaymentMethodId by paymentMethodPredictorViewModel.predictedPaymentMethodId
        .collectAsStateWithLifecycle()

    // Tags are their own ViewModel because they are the one part of this form backed by a
    // repository: the catalogue is observed and the selection is reconciled against it.
    val tagsViewModel: TransactionTagsViewModel = hiltViewModel()
    val allTags by tagsViewModel.allTags.collectAsStateWithLifecycle()
    val selectedTags by tagsViewModel.selectedTags.collectAsStateWithLifecycle()

    // Funds the expense can draw from. Their own ViewModel so the form's signature and
    // every existing caller stay unchanged.
    val fundsViewModel: AddTransactionFundsViewModel = hiltViewModel()
    val availableFunds by fundsViewModel.funds.collectAsStateWithLifecycle()
    val selectedFundId by fundsViewModel.selectedFundId.collectAsStateWithLifecycle()

    LaunchedEffect(existingTransaction?.id) {
        val id = existingTransaction?.id
        if (id.isNullOrBlank()) {
            tagsViewModel.prepareNewDraft()
            fundsViewModel.prepareNewDraft()
        } else {
            tagsViewModel.seedFromTransaction(id)
            fundsViewModel.seedFromTransaction(id, existingTransaction.fundId)
        }
    }

    // Voice lives in its own file because it touches android.speech, which the Compose
    // preview renderer does not ship. See [rememberTransactionVoiceInput].
    val voice = rememberTransactionVoiceInput(
        autoStartVoice = autoStartVoice,
        onVoiceAutoStarted = onVoiceAutoStarted
    )

    AddTransactionScreenContent(
        currencyId = currencyId,
        dateFormatPattern = dateFormatPattern,
        timeFormat = timeFormat,
        transactions = transactions,
        availableCategories = availableCategories,
        availablePaymentMethods = availablePaymentMethods,
        availableFunds = availableFunds,
        selectedFundId = selectedFundId,
        onSelectFund = fundsViewModel::selectFund,
        existingTransaction = existingTransaction,
        existingRecurringRule = existingRecurringRule,
        activeRecurringRuleCount = activeRecurringRuleCount,
        allRecurringRules = allRecurringRules,
        initialAmountInput = initialAmountInput,
        initialNote = initialNote,
        initialCategoryId = initialCategoryId,
        initialTransactionTypeId = initialTransactionTypeId,
        initialPaymentTypeId = initialPaymentTypeId,
        initialCreatedAt = initialCreatedAt,
        autoStartVoice = autoStartVoice,
        favorites = favorites,
        onRemoveFavorite = onRemoveFavorite,
        onBackClick = onBackClick,
        onDeleteClick = onDeleteClick,
        onCalculatorClick = onCalculatorClick,
        onAmountInputChange = onAmountInputChange,
        onNoteChange = onNoteChange,
        allTags = allTags,
        selectedTags = selectedTags,
        onToggleTag = tagsViewModel::toggleTag,
        onAddTagByName = tagsViewModel::addTagByName,
        // The Content already holds `selectedTags`, so it supplies the ids on the save
        // call and this passes the callback straight through.
        onSaveClick = onSaveClick,
        predictedPaymentMethodId = predictedPaymentMethodId,
        onPredictNote = { paymentMethodPredictorViewModel.predict(it) },
        onLearnPaymentMethod = { text, paymentId ->
            paymentMethodPredictorViewModel.learn(text, paymentId)
        },
        voiceUiState = voice.uiState,
        isVoiceSheetVisible = voice.isSheetVisible,
        onMicClick = voice.onMicClick,
        onVoiceSheetDismiss = voice.onSheetDismiss,
        onVoiceSheetRetry = voice.onSheetRetry,
        onCreateFund = fundsViewModel::createFund
    )
}

/**
 * Pure UI for the Add/Edit transaction form. Receives every Hilt-provided value and the
 * voice affordances through parameters so it stays previewable without a Hilt container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddTransactionScreenContent(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    transactions: List<Transaction> = emptyList(),
    availableCategories: List<CategoryType> = categoryMap.values.toList(),
    availablePaymentMethods: List<PaymentType> = paymentTypeMap.values.sortedBy { it.id },
    /** Cash buckets this expense can be drawn from; empty hides the selector. */
    availableFunds: List<Fund> = emptyList(),
    selectedFundId: String? = null,
    onSelectFund: (String?) -> Unit = {},
    existingTransaction: Transaction? = null,
    existingRecurringRule: RecurringTransactionRule? = null,
    activeRecurringRuleCount: Int = 0,
    allRecurringRules: List<RecurringTransactionRule> = emptyList(),
    initialAmountInput: String? = null,
    initialNote: String? = null,
    initialCategoryId: Int? = null,
    initialTransactionTypeId: Int? = null,
    initialPaymentTypeId: Int? = null,
    initialCreatedAt: Long? = null,
    autoStartVoice: Boolean = false,
    favorites: List<FavoriteTransaction> = emptyList(),
    onRemoveFavorite: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onCalculatorClick: () -> Unit = {},
    onAmountInputChange: (String) -> Unit = {},
    onNoteChange: (String) -> Unit = {},
    /** Tag catalogue for suggestions, and the tags currently applied to this draft. */
    allTags: List<Tag> = emptyList(),
    selectedTags: List<Tag> = emptyList(),
    onToggleTag: (String) -> Unit = {},
    onAddTagByName: (String) -> Unit = {},
    onSaveClick: (Transaction, RecurringTransactionDraft?, Boolean, List<String>) -> Unit = { _, _, _, _ -> },
    predictedPaymentMethodId: Int? = null,
    onPredictNote: (String) -> Unit = {},
    onLearnPaymentMethod: (String, Int) -> Unit = { _, _ -> },
    voiceUiState: VoiceInputUiState = VoiceInputUiState(),
    isVoiceSheetVisible: Boolean = false,
    onMicClick: () -> Unit = {},
    onVoiceSheetDismiss: () -> Unit = {},
    onVoiceSheetRetry: () -> Unit = {},
    onCreateFund: (name: String, amountMinor: Long, startDate: Long, iconKey: String, colorHex: String, note: String, incomeCategoryId: Int) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
    ) {
        val compact = maxHeight < 780.dp
        val dense = maxHeight < 700.dp
        // Wide windows (tablets, foldables, desktop, phone landscape) get the
        // two-pane layout; the height-based compact/dense flags still govern
        // font sizes and paddings on phones.
        val wide = maxWidth >= 720.dp
        // Usage ranking only counts transactions from the last 60 days, so the
        // "most used" pickers reflect recent behaviour rather than all-time.
        val rankingSinceMillis = remember { System.currentTimeMillis() - USAGE_RANKING_WINDOW_MS }
        val paymentMethods = remember(availablePaymentMethods, transactions, rankingSinceMillis) {
            getRankedPaymentMethods(
                paymentMethods = availablePaymentMethods,
                transactions = transactions,
                sinceMillis = rankingSinceMillis
            )
        }
        val isEditMode = existingTransaction != null
        // Spent per fund, derived from the transactions the form already holds, so the
        // overspend check reflects the same data the rest of the app shows.
        val fundSpentById = remember(transactions, existingTransaction?.id) {
            transactions
                .filter { it.transactionTypeId == 2 && !it.isDeleted && it.fundId != null }
                .filter { it.id != existingTransaction?.id }
                .groupBy { it.fundId }
                .mapValues { (_, rows) -> rows.sumOf { it.amountMinor } }
        }
        val fundRemainingById = remember(availableFunds, fundSpentById) {
            availableFunds.associate { fund ->
                fund.id to (fund.amountMinor - (fundSpentById[fund.id] ?: 0L))
            }
        }
        // The star is staged: tapping it only records the intent, and Add/Update
        // is what writes or clears the template. Until it is touched it reports
        // what is actually saved, so a template arriving from the favorites flow
        // a frame after the screen opens still shows as marked.
        val savedFavorite = existingTransaction != null &&
            favorites.any { it.transactionId == existingTransaction.id }
        var isFavoriteTouched by rememberSaveable(existingTransaction?.id) { mutableStateOf(false) }
        var isFavoriteValue by rememberSaveable(existingTransaction?.id) { mutableStateOf(false) }
        val isFavorite = if (isFavoriteTouched) isFavoriteValue else savedFavorite

        var selectedTransactionTypeId by rememberSaveable(existingTransaction?.id, initialTransactionTypeId) {
            mutableIntStateOf(existingTransaction?.transactionTypeId ?: initialTransactionTypeId ?: DEFAULT_TRANSACTION_TYPE_ID)
        }
        var selectedCategoryId by rememberSaveable(existingTransaction?.id, initialCategoryId) {
            mutableIntStateOf(existingTransaction?.categoryId ?: initialCategoryId ?: 0)
        }
        var selectedPaymentId by rememberSaveable(existingTransaction?.id, initialPaymentTypeId) {
            mutableIntStateOf(
                existingTransaction?.paymentTypeId
                    ?: initialPaymentTypeId
                    ?: paymentMethods.firstOrNull { it.id == DEFAULT_PAYMENT_TYPE_ID }?.id
                    ?: (paymentMethods.firstOrNull()?.id ?: 0)
            )
        }
        var amountInput by rememberSaveable(existingTransaction?.id, initialAmountInput) {
            mutableStateOf(existingTransaction?.amount?.let(::formatEditableAmount).orEmpty().ifBlank { initialAmountInput ?: "0" })
        }
        var selectedDateMillis by rememberSaveable(existingTransaction?.id, initialCreatedAt) {
            mutableLongStateOf(existingTransaction?.createdAt ?: initialCreatedAt ?: System.currentTimeMillis())
        }
        var note by rememberSaveable(existingTransaction?.id, initialNote) {
            mutableStateOf(existingTransaction?.note ?: initialNote.orEmpty())
        }

        var hasManuallySelectedPayment by rememberSaveable { mutableStateOf(initialPaymentTypeId != null) }

        // Auto-predict payment method when note/merchant text changes
        LaunchedEffect(note) {
            if (note.isNotBlank() && !isEditMode && !hasManuallySelectedPayment) {
                onPredictNote(note)
            }
        }

        // Apply predicted payment method when prediction arrives
        LaunchedEffect(predictedPaymentMethodId) {
            if (!isEditMode && predictedPaymentMethodId != null && !hasManuallySelectedPayment) {
                selectedPaymentId = predictedPaymentMethodId!!
            }
        }

        var noteDraft by rememberSaveable(existingTransaction?.id, initialNote) {
            mutableStateOf(existingTransaction?.note ?: initialNote.orEmpty())
        }
        var isRecurringEnabled by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingRecurringRule != null)
        }
        var selectedRecurringFrequency by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingRecurringRule?.frequency ?: RecurringFrequency.Monthly)
        }
        var recurringCountInput by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(existingRecurringRule?.repeatCount?.toString() ?: "12")
        }
        // EMI creation is offered only while creating: an existing plan is
        // changed from the recurring list editor, which can show the plan's real
        // first-due date (it lives on the slot ledger, not on the rule).
        val canCreateInstallment = !isEditMode
        var selectedRecurringType by rememberSaveable(existingTransaction?.id) {
            mutableStateOf(RecurringType.REGULAR)
        }
        var emiTotalInput by rememberSaveable(existingTransaction?.id) { mutableStateOf("") }
        var emiInstallmentInput by rememberSaveable(existingTransaction?.id) { mutableStateOf("") }
        // Null until the user picks one Ã¢â‚¬â€ an unpicked plan starts on the
        // transaction date, so changing that date still moves the plan start.
        val emiFirstDueAtPickedState = rememberSaveable(existingTransaction?.id) { mutableStateOf<Long?>(null) }
        var emiFirstDueAtPicked by emiFirstDueAtPickedState
        val isEmiFirstDuePickerVisibleState = rememberSaveable { mutableStateOf(false) }
        var isEmiFirstDuePickerVisible by isEmiFirstDuePickerVisibleState
        var isDatePickerVisible by rememberSaveable { mutableStateOf(false) }
        var isTimePickerVisible by rememberSaveable { mutableStateOf(false) }
        var isNoteSheetVisible by rememberSaveable { mutableStateOf(false) }
        val isRecurringModalVisibleState = rememberSaveable { mutableStateOf(false) }
        var isRecurringModalVisible by isRecurringModalVisibleState
        val isFavoritesSheetVisibleState = rememberSaveable { mutableStateOf(false) }
        var isFavoritesSheetVisible by isFavoritesSheetVisibleState
        var isCategorySheetVisible by rememberSaveable { mutableStateOf(false) }
        var isPaymentSheetVisible by rememberSaveable { mutableStateOf(false) }
        var isTagSheetVisible by rememberSaveable { mutableStateOf(false) }
        var isFundSheetVisible by rememberSaveable { mutableStateOf(false) }
        var isCreateFundSheetVisible by rememberSaveable { mutableStateOf(false) }
        val showDuplicateWarningState = rememberSaveable { mutableStateOf(false) }
        var showDuplicateWarning by showDuplicateWarningState
        val duplicateWarningMessageState = rememberSaveable { mutableStateOf<String?>(null) }
        var duplicateWarningMessage by duplicateWarningMessageState
        val pendingSaveTransactionState = remember { mutableStateOf<Transaction?>(null) }
        var pendingSaveTransaction by pendingSaveTransactionState
        val pendingSaveDraftState = remember { mutableStateOf<RecurringTransactionDraft?>(null) }
        var pendingSaveDraft by pendingSaveDraftState
        val showOverspendWarningState = rememberSaveable { mutableStateOf(false) }
        var showOverspendWarning by showOverspendWarningState
        val pendingOverspendTransactionState = remember { mutableStateOf<Transaction?>(null) }
        var pendingOverspendTransaction by pendingOverspendTransactionState
        val pendingOverspendDraftState = remember { mutableStateOf<RecurringTransactionDraft?>(null) }
        var pendingOverspendDraft by pendingOverspendDraftState
        val pendingOverspendFundIdState = remember { mutableStateOf<String?>(null) }
        var pendingOverspendFundId by pendingOverspendFundIdState
        val amountFocusRequester = remember { FocusRequester() }
        val noteFocusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val context = LocalContext.current

        LaunchedEffect(initialAmountInput) {
            if (initialAmountInput != null && initialAmountInput != amountInput) {
                amountInput = initialAmountInput
            }
        }

        // Auto-focus amount field and open keyboard on screen entry. Skipped when
        // launched via the Speak-to-Add shortcut so the IME can't break the
        // SpeechRecognizer right as the voice sheet opens.
        LaunchedEffect(Unit) {
            if (!autoStartVoice) {
                amountFocusRequester.requestFocus()
                keyboardController?.show()
            }
        }

        LaunchedEffect(initialNote) {
            if (initialNote != null && initialNote != note) {
                note = initialNote
                noteDraft = initialNote
            }
        }

        LaunchedEffect(initialCategoryId) {
            if (initialCategoryId != null && initialCategoryId != selectedCategoryId && initialCategoryId != 0) {
                selectedCategoryId = initialCategoryId
            }
        }

        LaunchedEffect(initialTransactionTypeId) {
            if (initialTransactionTypeId != null && initialTransactionTypeId != selectedTransactionTypeId && initialTransactionTypeId != 0) {
                selectedTransactionTypeId = initialTransactionTypeId
            }
        }

        val colorScheme = MaterialTheme.colorScheme

        val categoriesForType = remember(transactions, availableCategories, selectedTransactionTypeId, rankingSinceMillis) {
            getRankedCategories(
                categories = availableCategories,
                transactions = transactions,
                transactionTypeId = selectedTransactionTypeId,
                sinceMillis = rankingSinceMillis
            )
        }

        LaunchedEffect(categoriesForType) {
            if (categoriesForType.none { it.id == selectedCategoryId }) {
                selectedCategoryId = categoriesForType.firstOrNull()?.id ?: 0
            }
        }

        val selectedCategory = remember(categoriesForType, selectedCategoryId) {
            categoriesForType.firstOrNull { it.id == selectedCategoryId }
        }
        val selectedPayment = remember(paymentMethods, selectedPaymentId) {
            paymentMethods.firstOrNull { it.id == selectedPaymentId }
        }
        val recurringCount = recurringCountInput.toIntOrNull()

        // Ã¢â€â‚¬Ã¢â€â‚¬ Recurring draft (built once, so the Add button and the save path
        // can never disagree about what is about to be persisted) Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
        val isEmiSelected = canCreateInstallment && selectedRecurringType == RecurringType.INSTALLMENT
        val effectiveFirstDueAt = emiFirstDueAtPicked ?: selectedDateMillis
        val recurringDraft = buildRecurringDraft(
            isRecurringEnabled = isRecurringEnabled,
            frequency = selectedRecurringFrequency,
            repeatCount = recurringCount,
            isInstallmentSelected = isEmiSelected,
            totalAmount = emiTotalInput.toDoubleOrNull() ?: 0.0,
            installmentAmount = emiInstallmentInput.toDoubleOrNull() ?: 0.0,
            firstDueAt = effectiveFirstDueAt
        )

        val canSubmit = (amountInput.toDoubleOrNull() ?: 0.0) > 0 &&
            selectedCategory != null &&
            selectedPayment != null &&
            // An EMI with terms that do not add up (or that is still half-filled)
            // stays unsaveable rather than silently falling back to a plain
            // recurring rule.
            (!isRecurringEnabled ||
                (recurringDraft != null && (!isEmiSelected || recurringDraft.plan != null)))

        // Prefills the form from a tapped favorite template and notifies the
        // caller (which resets the star toggle) before confirming to the user.
        val applyFavorite: (FavoriteTransaction) -> Unit = { favorite ->
            amountInput = formatEditableAmount(favorite.amountMinor.toMajorUnits())
            selectedTransactionTypeId = favorite.transactionTypeId
            selectedCategoryId = favorite.categoryId
            selectedPaymentId = favorite.paymentTypeId
            note = favorite.note
            noteDraft = favorite.note
            Toast.makeText(context, context.getString(R.string.msg_favorite_copied), Toast.LENGTH_SHORT).show()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                // The window is drawn edge-to-edge with decorFitsSystemWindows=false, so the
                // soft keyboard overlays the form instead of resizing it. Reserving the IME's
                // height here keeps the whole screen (and thus the scrolling form) above the
                // keyboard. Unioning the IME with the navigation bar rather than stacking both
                // avoids double-counting the strip the keyboard already covers, while still
                // keeping the nav-bar padding when the keyboard is closed.
                .windowInsetsPadding(
                    WindowInsets.navigationBars.union(WindowInsets.ime).only(WindowInsetsSides.Bottom)
                )
                .padding(bottom = if (dense) 12.dp else 14.dp)
                // Swipe left → Expense (next tab), swipe right → Income (previous tab),
                // matching the Calendar screen's month/year swipe conventions. Compose's
                // gesture disambiguation (touch slop + Main pass bubbling) lets the vertical
                // scroll and the horizontally-scrollable category/payment chip rows keep
                // their gestures, so only deliberate horizontal swipes switch tabs.
                .horizontalSwipe(
                    onSwipeLeft = {
                        if (selectedTransactionTypeId == incomeTypeId) {
                            selectedTransactionTypeId = expenseTypeId
                        }
                    },
                    onSwipeRight = {
                        if (selectedTransactionTypeId == expenseTypeId) {
                            selectedTransactionTypeId = incomeTypeId
                        }
                    }
                )
        ) {
            val enter = rememberSectionEnterAlphas(3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, end = Dimens.ScreenPadding)
                    .alpha(enter[0])
            ) {
                AppHeader(
                    title = stringResource(if (isEditMode) R.string.title_edit_transaction else R.string.title_add_transaction),
                    onBackClick = {
                        keyboardController?.hide()
                        onBackClick()
                    },
                    actions = {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                            // Delete transaction icon. It wears the same rounded tile as the
                            // reset action opposite it, so edit mode keeps the header's one
                            // control shape; only the ink changes.
                            if (isEditMode) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.formCard)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            keyboardController?.hide()
                                            onDeleteClick()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = stringResource(R.string.desc_delete_transaction),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            // Reset form icon (only in add mode). It wears the same rounded
                            // tile as the screen's other glyphs, so the header action reads as
                            // one of the form's controls rather than a bare icon.
                            if (!isEditMode) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.formCard)
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedTransactionTypeId = DEFAULT_TRANSACTION_TYPE_ID
                                            selectedCategoryId = 0
                                            selectedPaymentId = paymentMethods.firstOrNull { it.id == DEFAULT_PAYMENT_TYPE_ID }?.id ?: (paymentMethods.firstOrNull()?.id ?: 0)
                                            amountInput = "0"
                                            selectedDateMillis = System.currentTimeMillis()
                                            note = ""
                                            noteDraft = ""
                                            isRecurringEnabled = false
                                            selectedRecurringFrequency = RecurringFrequency.Monthly
                                            recurringCountInput = "12"
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = stringResource(R.string.desc_clear_fields),
                                        // Same ink as the header's own title and back arrow, so the
                                        // action reads as part of the header rather than a stray accent.
                                        tint = MaterialTheme.colorScheme.appHeaderTitle.copy(alpha = 0.75f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.ScreenPadding)
                    .alpha(enter[1])
            ) {                    // Shared form blocks, reused by both the single-column (phone)
                // and two-pane (wide) layouts so behavior stays identical.
                // Quick-entry favorites carousel, shown just above the note row
                // (add mode only).
                val quickFavoritesBlock: @Composable () -> Unit = {
                    if (!isEditMode) {
                        QuickFavoritesRow(
                            favorites = favorites,
                            currencyId = currencyId,
                            onSelectFavorite = applyFavorite,
                            onOpenAllSheet = { isFavoritesSheetVisible = true }
                        )
                    }
                }
                val tabAndAmountBlock: @Composable () -> Unit = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimatedTabSwitcher(
                            items = transactionModes.map { mode ->
                                TabItem(
                                    id = mode.id,
                                    label = stringResource(mode.label),
                                    selectedColor = if (mode.id == incomeTypeId) {
                                        MaterialTheme.colorScheme.income
                                    } else {
                                        MaterialTheme.colorScheme.expense
                                    }
                                )
                            },
                            selectedItemId = selectedTransactionTypeId,
                            onItemSelected = { selectedTypeId -> selectedTransactionTypeId = selectedTypeId },
                            containerColor = MaterialTheme.colorScheme.typeSwitcherFill

                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CurrencyAmountCard(
                                amountText = amountInput,
                                currencyId = currencyId,
                                selectedTransactionTypeId = selectedTransactionTypeId,
                                footerContent = if (isEditMode) null else quickFavoritesBlock,
                                compact = compact,
                                focusRequester = amountFocusRequester,
                                canFocus = !isNoteSheetVisible,
                                onCalculatorClick = onCalculatorClick,
                                onAmountChange = {
                                    val validated = validateAmountChange(it, amountInput)
                                    if (validated != amountInput) {
                                        amountInput = validated
                                        onAmountInputChange(validated)
                                    }
                                },
                                onClick = {
                                    amountFocusRequester.requestFocus()
                                    keyboardController?.show()
                                },
                                onImeNext = {
                                    keyboardController?.hide()
                                    noteDraft = note
                                    isNoteSheetVisible = true
                                }
                            )
                        }
                    }
                }

                val noteBlock: @Composable () -> Unit = {
                    val micBorderColor = colorScheme.outlineVariant.copy(alpha = 0.5f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (dense) 8.dp else 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = if (compact) 40.dp else 44.dp)
                                .shadow(
                                    elevation = if (colorScheme.isDark) 6.dp else 12.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = if (colorScheme.isDark) colorScheme.accentInk.copy(alpha = 0.06f) else CardShadowAmbientLight,
                                    spotColor = if (colorScheme.isDark) colorScheme.secondary.copy(alpha = 0.06f) else CardShadowSpotLight
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(SolidColor(colorScheme.formCard))
                                .border(
                                    width = 1.dp,
                                    color = micBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(onClick = {
                                    noteDraft = note
                                    isNoteSheetVisible = true
                                })
                                .padding(
                                    horizontal = if (compact) 12.dp else 16.dp,
                                    vertical = if (compact) 4.dp else 6.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EditNote,
                                    contentDescription = stringResource(R.string.label_note),
                                    tint = if (note.isBlank()) colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else colorScheme.accentInk,
                                    modifier = Modifier.size(if (compact) 18.dp else 20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = note.ifBlank { stringResource(R.string.label_add_note) },
                                    color = if (note.isBlank()) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontStyle = if (note.isBlank()) FontStyle.Italic else FontStyle.Normal
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        val actionIconRamp = brandGradient()
                        val actionIconModifier = Modifier
                            .size(if (compact) 20.dp else 22.dp)
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(brush = actionIconRamp, blendMode = BlendMode.SrcIn)
                                }
                            }
                        Box(
                            modifier = Modifier
                                .size(if (compact) 40.dp else 44.dp)
                                .shadow(
                                    elevation = if (colorScheme.isDark) 6.dp else 12.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = if (colorScheme.isDark) colorScheme.accentInk.copy(alpha = 0.06f) else CardShadowAmbientLight,
                                    spotColor = if (colorScheme.isDark) colorScheme.secondary.copy(alpha = 0.06f) else CardShadowSpotLight
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(SolidColor(colorScheme.formCard))
                                .border(
                                    width = 1.dp,
                                    color = micBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(onClick = {
                                    keyboardController?.hide()
                                    onMicClick()
                                }),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Bold.MicrophoneStage,
                                contentDescription = stringResource(R.string.desc_voice_add),
                                tint = Color.White,
                                modifier = actionIconModifier
                            )
                        }

                        // Favorite template star: tapping it only records the
                        // intent, and Add/Update is what writes or clears the
                        // template, so leaving without saving changes nothing.
                        Box(
                            modifier = Modifier
                                .size(if (compact) 40.dp else 44.dp)
                                .shadow(
                                    elevation = if (colorScheme.isDark) 6.dp else 12.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = if (colorScheme.isDark) colorScheme.accentInk.copy(alpha = 0.06f) else CardShadowAmbientLight,
                                    spotColor = if (colorScheme.isDark) colorScheme.secondary.copy(alpha = 0.06f) else CardShadowSpotLight
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(SolidColor(colorScheme.formCard))
                                .border(
                                    width = 1.dp,
                                    color = micBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(onClick = {
                                    keyboardController?.hide()
                                    isFavoriteTouched = true
                                    isFavoriteValue = !isFavorite
                                }),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavorite) PhosphorIcons.Fill.Star else PhosphorIcons.Regular.Star,
                                contentDescription = stringResource(R.string.desc_toggle_favorite),
                                tint = Color.White,
                                modifier = actionIconModifier
                            )
                        }

                        // Opens the itemized breakdown screen
                        Box(
                            modifier = Modifier
                                .size(if (compact) 40.dp else 44.dp)
                                .shadow(
                                    elevation = if (colorScheme.isDark) 6.dp else 12.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = if (colorScheme.isDark) colorScheme.accentInk.copy(alpha = 0.06f) else CardShadowAmbientLight,
                                    spotColor = if (colorScheme.isDark) colorScheme.secondary.copy(alpha = 0.06f) else CardShadowSpotLight
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(SolidColor(colorScheme.formCard))
                                .border(
                                    width = 1.dp,
                                    color = micBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(onClick = {
                                    keyboardController?.hide()
                                    onCalculatorClick()
                                }),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                contentDescription = stringResource(R.string.label_itemized_calculator),
                                tint = Color.White,
                                modifier = actionIconModifier
                            )
                        }
                    }
                }

                val selectionGridBlock: @Composable () -> Unit = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(if (dense) 8.dp else 10.dp)
                    ) {
                        // Row 1: Category & Payment Method
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (dense) 8.dp else 10.dp)
                        ) {
                            val category = selectedCategory
                            val categoryColor = if (category != null) {
                                colorScheme.categoryColor(categoryId = category.id, colorHex = category.colorHex)
                            } else colorScheme.primary

                            SelectionCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.title_category_1),
                                selectedValue = category?.name ?: stringResource(R.string.label_select_category),
                                icon = category?.icon ?: Icons.Filled.Category,
                                iconTint = categoryColor,
                                onClick = { isCategorySheetVisible = true }
                            )

                            val payment = selectedPayment
                            val paymentColor = if (payment != null) {
                                colorScheme.paymentColor(paymentId = payment.id, colorHex = payment.colorHex)
                            } else colorScheme.primary

                            SelectionCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(R.string.title_payment_method),
                                selectedValue = payment?.name ?: stringResource(R.string.label_payment_type_item),
                                icon = payment?.icon ?: Icons.Filled.AccountBalanceWallet,
                                iconTint = paymentColor,
                                onClick = { isPaymentSheetVisible = true }
                            )
                        }

                        // Row 2: Fund Card (Full Width)
                        val isIncomeMode = selectedTransactionTypeId == 1
                        val fund = if (isIncomeMode) null else availableFunds.firstOrNull { it.id == selectedFundId }
                        val fundText = if (isIncomeMode) {
                            stringResource(R.string.label_fund_none)
                        } else {
                            fund?.name ?: stringResource(R.string.label_fund_none)
                        }
                        // The empty fund is drawn as the mockup's rose X, so "no fund" reads as
                        // the expense colour the rest of the screen is using rather than as a
                        // neutral placeholder; a picked fund keeps its own identity colour.
                        val fundColor = if (!isIncomeMode && fund != null) {
                            colorScheme.identityColor(fund.colorHex)
                        } else {
                            colorScheme.expense
                        }
                        val fundIcon = if (isIncomeMode || fund == null) Icons.Filled.Clear else fund.icon

                        SelectionCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(R.string.title_spend_from_fund),
                            selectedValue = fundText,
                            icon = fundIcon,
                            iconTint = fundColor,
                            enabled = !isIncomeMode,
                            onClick = { isFundSheetVisible = true }
                        )

                        // Row 3: Tag Card (Full Width)
                        TagSelectionCard(
                            modifier = Modifier.fillMaxWidth(),
                            tags = selectedTags,
                            onClick = { isTagSheetVisible = true }
                        )

                        // Row 4: Recurring Card (Full Width)
                        RecurringCompactCard(
                            modifier = Modifier.fillMaxWidth(),
                            isEnabled = isRecurringEnabled,
                            frequency = selectedRecurringFrequency,
                            isInstallment = isEmiSelected,
                            compact = compact,
                            onClick = { isRecurringModalVisible = true }
                        )

                        // Row 5: Date & Time Cards (Single Row)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(if (dense) 8.dp else 10.dp)
                        ) {
                            SelectionInfoCard(
                                modifier = Modifier.weight(1f),
                                leadingIcon = Icons.Filled.CalendarMonth,
                                label = stringResource(R.string.label_date),
                                value = formatTransactionDate(selectedDateMillis, dateFormatPattern),
                                compact = compact,
                                onClick = { isDatePickerVisible = true }
                            )

                            SelectionInfoCard(
                                modifier = Modifier.weight(1f),
                                leadingIcon = Icons.Filled.Schedule,
                                label = stringResource(R.string.label_time),
                                value = formatTime(selectedDateMillis, timeFormat),
                                compact = compact,
                                onClick = { isTimePickerVisible = true }
                            )
                        }
                    }
                }


                if (wide) {
                    // Two-pane layout
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(if (dense) 8.dp else 10.dp)
                        ) {
                            tabAndAmountBlock()
                            selectionGridBlock()
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(if (dense) 8.dp else 10.dp)
                        ) {
                            noteBlock()
                        }
                    }
                } else {
                    // Compact phone layout
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(if (dense) 8.dp else 10.dp)
                    ) {
                        tabAndAmountBlock()
                        noteBlock()
                        selectionGridBlock()
                    }
                }

                Spacer(modifier = Modifier.height(if (dense) 12.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AddTransactionButton(
                        modifier = Modifier.weight(1f),
                        enabled = canSubmit,
                        label = if (isEditMode) stringResource(R.string.label_update_action) else stringResource(R.string.label_add_action),
                        selectedCategory = selectedCategory?.name.orEmpty(),
                        onClick = {
                            val category = selectedCategory ?: return@AddTransactionButton
                            val payment = selectedPayment ?: return@AddTransactionButton
                            val amount = amountInput.toDoubleOrNull() ?: return@AddTransactionButton
                            val fundIdToSave = if (selectedTransactionTypeId == 1) {
                                null
                            } else {
                                selectedFundId
                            }
                            val transaction = Transaction(
                                id = existingTransaction?.id.orEmpty(),
                                note = note.trim(),
                                createdAt = selectedDateMillis,
                                amountMinor = amount.toMinorUnits(),
                                transactionTypeId = selectedTransactionTypeId,
                                paymentTypeId = payment.id,
                                categoryId = category.id,
                                contentHash = existingTransaction?.contentHash,
                                syncState = existingTransaction?.syncState ?: SyncState.PENDING_UPLOAD,
                                isDeleted = false,
                                updatedAt = existingTransaction?.updatedAt ?: selectedDateMillis,
                                sourceRecurringRuleId = existingTransaction?.sourceRecurringRuleId,
                                fundId = fundIdToSave
                            )
                            // Duplicate detection: check if a rule with same category + amount + frequency exists
                            if (recurringDraft != null && !isEditMode) {
                                val amount = amountInput.toDoubleOrNull() ?: 0.0
                                val duplicate = allRecurringRules.firstOrNull { rule ->
                                    !rule.isDeleted &&
                                    rule.frequency == recurringDraft.frequency &&
                                    rule.transactionId != transaction.id &&
                                    // Match by category (from the rule's linked transaction)
                                    transactions.any { t ->
                                        t.id == rule.transactionId &&
                                        t.categoryId == category.id &&
                                        t.amountMinor == transaction.amountMinor
                                    }
                                }
                                if (duplicate != null) {
                                    val categoryName = availableCategories.firstOrNull { it.id == category.id }?.name ?: "this category"
                                    val freqLabel = selectedRecurringFrequency.label
                                    val amountFormatted = formatCurrencyValue(transaction.amountMinor / 100.0, currencyId)
                                    duplicateWarningMessage = context.getString(R.string.msg_duplicate_recurring_rule, categoryName, amountFormatted, freqLabel)
                                    pendingSaveTransaction = transaction
                                    pendingSaveDraft = recurringDraft
                                    showDuplicateWarning = true
                                    return@AddTransactionButton
                                }
                            }
                            // Fund overspend: the expense is larger than what is left in the
                            // chosen bucket. Unlike the duplicate check this never proceeds
                            // silently — the shortfall is the whole point.
                            val chosenFundId = fundIdToSave
                            if (chosenFundId != null && transaction.transactionTypeId == 2) {
                                val remaining = fundRemainingById[chosenFundId]
                                if (remaining != null && transaction.amountMinor > remaining) {
                                    pendingOverspendTransaction = transaction
                                    pendingOverspendDraft = recurringDraft
                                    pendingOverspendFundId = chosenFundId
                                    showOverspendWarning = true
                                    return@AddTransactionButton
                                }
                            }
                            keyboardController?.hide()
                            // Learn merchant Ã¢â€ â€™ payment method association
                            if (note.isNotBlank()) {
                                onLearnPaymentMethod(note, payment.id)
                            }
                            onSaveClick(transaction, recurringDraft, isFavorite, selectedTags.map { it.id })
                        }
                    )


                }
            }
        }

        if (isDatePickerVisible) {
            WheelDateTimePickerModal(
                mode = WheelPickerMode.SINGLE_DATE,
                initialStartMillis = selectedDateMillis,
                onDismissRequest = { isDatePickerVisible = false },
                onConfirm = { start, _ ->
                    selectedDateMillis = datePickerSelectionToLocalDateTimestamp(
                        selectedDateMillis = start,
                        referenceTimestamp = selectedDateMillis,
                        // The wheel picker returns a LOCAL timestamp (validateAndCalculateTimestamp
                        // builds it from Calendar.getInstance()), so reading it as UTC shifted the
                        // day back by one in positive-offset zones.
                        isInputUtc = false
                    )
                    isDatePickerVisible = false
                }
            )
        }

        if (isTimePickerVisible) {
            WheelDateTimePickerModal(
                mode = WheelPickerMode.SINGLE_TIME,
                initialStartMillis = selectedDateMillis,
                onDismissRequest = { isTimePickerVisible = false },
                onConfirm = { newTime, _ ->
                    selectedDateMillis = mergeTimeWithDate(newTime, selectedDateMillis)
                    isTimePickerVisible = false
                }
            )
        }

        if (isNoteSheetVisible) {
            TransactionNoteBottomSheet(
                note = noteDraft,
                onNoteChange = { noteDraft = it },
                onDismissRequest = {
                    // force=true prevents Compose from restoring focus to any child
                    // (e.g. the amount BasicTextField) after the sheet leaves composition.
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    isNoteSheetVisible = false
                },
                onSave = {
                    note = noteDraft
                    onNoteChange(note)
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    isNoteSheetVisible = false
                }
            )
        }

        if (isVoiceSheetVisible) {
            val currencySymbol = remember(currencyId) { getCurrency(currencyId).currencySymbol }
            VoiceInputSheet(
                sheetState = voiceUiState.sheetState,
                transcript = voiceUiState.transcript,
                parsedTransaction = voiceUiState.parsedTransaction,
                errorMessage = voiceUiState.errorMessageResId?.let { stringResource(it) },
                currencySymbol = currencySymbol,
                onDismissRequest = onVoiceSheetDismiss,
                onConfirm = { transaction ->
                    // Auto-fill form fields from parsed voice result
                    amountInput = formatEditableAmount(transaction.amountMinor.toMajorUnits())
                    selectedTransactionTypeId = transaction.transactionTypeId
                    selectedCategoryId = transaction.categoryId
                    note = transaction.note
                    noteDraft = transaction.note
                    if (transaction.merchant != null) {
                        note = if (note.isNotBlank()) "$note \u2014 ${transaction.merchant}" else transaction.merchant
                        noteDraft = note
                    }
                    selectedDateMillis = transaction.createdAt
                    onVoiceSheetDismiss()
                },
                onRetry = onVoiceSheetRetry
            )
        }

        if (isCategorySheetVisible) {
            CategoryBottomSheet(
                categories = categoriesForType,
                selectedCategoryId = selectedCategoryId,
                onSelectCategory = { cat ->
                    selectedCategoryId = cat.id
                },
                onDismissRequest = { isCategorySheetVisible = false }
            )
        }

        if (isPaymentSheetVisible) {
            PaymentMethodBottomSheet(
                paymentMethods = paymentMethods,
                selectedPaymentId = selectedPaymentId,
                onSelectPayment = { payment ->
                    selectedPaymentId = payment.id
                    hasManuallySelectedPayment = true
                },
                onDismissRequest = { isPaymentSheetVisible = false }
            )
        }

        if (isTagSheetVisible) {
            TagBottomSheet(
                allTags = allTags,
                selectedTags = selectedTags,
                onToggleTag = onToggleTag,
                onAddTagByName = onAddTagByName,
                onDismissRequest = { isTagSheetVisible = false }
            )
        }

        if (isFundSheetVisible) {
            SpendFromFundBottomSheet(
                availableFunds = availableFunds,
                selectedFundId = selectedFundId,
                onSelectFund = onSelectFund,
                onCreateFundClick = {
                    isCreateFundSheetVisible = true
                },
                onDismissRequest = { isFundSheetVisible = false }
            )
        }

        if (isCreateFundSheetVisible) {
            CreateFundSheet(
                currencyId = currencyId,
                amountFormatPreferences = AmountFormatPreferences(CurrencyGroupingStyle.INTERNATIONAL, 2),
                categories = availableCategories,
                onDismiss = { isCreateFundSheetVisible = false },
                onCreate = { name, amountMinor, startDate, iconKey, colorHex, note, incomeCategoryId ->
                    onCreateFund(name, amountMinor, startDate, iconKey, colorHex, note, incomeCategoryId)
                    isCreateFundSheetVisible = false
                }
            )
        }

        if (isRecurringModalVisible) {
            ModalBottomSheet(
                onDismissRequest = { isRecurringModalVisible = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = if (MaterialTheme.colorScheme.isDark) MaterialTheme.colorScheme.profileSheet else MaterialTheme.colorScheme.sheet,
                // Flat surface (no tonal tint) so this sheet matches the sort/filter sheet.
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                ) {
                    RecurringTransactionSection(
                        isEnabled = isRecurringEnabled,
                        selectedFrequency = selectedRecurringFrequency,
                        repeatCountInput = recurringCountInput,
                        compact = compact,
                        activeRecurringRuleCount = activeRecurringRuleCount,
                        hasExistingRule = existingRecurringRule != null,
                        canCreateInstallment = canCreateInstallment,
                        selectedType = selectedRecurringType,
                        emiTotalInput = emiTotalInput,
                        emiInstallmentInput = emiInstallmentInput,
                        emiFirstDueText = formatTransactionDate(effectiveFirstDueAt, dateFormatPattern),
                        isEmiPlanValid = recurringDraft?.plan != null,
                        onEnabledChange = { isRecurringEnabled = it },
                        onFrequencySelected = { selectedRecurringFrequency = it },
                        onRepeatCountChange = { recurringCountInput = it.filter(Char::isDigit) },
                        onTypeSelected = { selectedRecurringType = it },
                        onEmiTotalChange = { emiTotalInput = it },
                        onEmiInstallmentChange = { emiInstallmentInput = it },
                        // The wheel picker is itself a modal sheet, so it cannot
                        // open on top of this one: step out to it and come back.
                        onEmiFirstDueClick = {
                            isRecurringModalVisible = false
                            isEmiFirstDuePickerVisible = true
                        }
                    )
                }
            }
        }


        AddTransactionOverlays(
            currencyId = currencyId,
            availableFunds = availableFunds,
            fundRemainingById = fundRemainingById,
            availableCategories = availableCategories,
            availablePaymentMethods = availablePaymentMethods,
            favorites = favorites,
            selectedTags = selectedTags,
            isFavorite = isFavorite,
            effectiveFirstDueAt = effectiveFirstDueAt,
            applyFavorite = applyFavorite,
            onRemoveFavorite = onRemoveFavorite,
            onSelectFund = onSelectFund,
            onSaveClick = onSaveClick,
            showDuplicateWarningState = showDuplicateWarningState,
            duplicateWarningMessageState = duplicateWarningMessageState,
            pendingSaveTransactionState = pendingSaveTransactionState,
            pendingSaveDraftState = pendingSaveDraftState,
            showOverspendWarningState = showOverspendWarningState,
            pendingOverspendTransactionState = pendingOverspendTransactionState,
            pendingOverspendDraftState = pendingOverspendDraftState,
            pendingOverspendFundIdState = pendingOverspendFundIdState,
            isEmiFirstDuePickerVisibleState = isEmiFirstDuePickerVisibleState,
            isRecurringModalVisibleState = isRecurringModalVisibleState,
            emiFirstDueAtPickedState = emiFirstDueAtPickedState,
            isFavoritesSheetVisibleState = isFavoritesSheetVisibleState,
        )
    }
}










