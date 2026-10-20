package com.mknlabs.expensetracker.feature.transactions.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.QuickActionCard
import com.mknlabs.expensetracker.core.ui.components.QuickActionOption
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowHeight
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowInfo
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowSize
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CalculatorHistoryEntry
import com.mknlabs.expensetracker.models.CalculatorLineItem
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Divide
import com.adamglin.phosphoricons.bold.Equals
import com.adamglin.phosphoricons.bold.Minus
import com.adamglin.phosphoricons.bold.NumberEight
import com.adamglin.phosphoricons.bold.NumberFive
import com.adamglin.phosphoricons.bold.NumberFour
import com.adamglin.phosphoricons.bold.NumberNine
import com.adamglin.phosphoricons.bold.NumberOne
import com.adamglin.phosphoricons.bold.NumberSeven
import com.adamglin.phosphoricons.bold.NumberSix
import com.adamglin.phosphoricons.bold.NumberThree
import com.adamglin.phosphoricons.bold.NumberTwo
import com.adamglin.phosphoricons.bold.NumberZero
import com.adamglin.phosphoricons.bold.Plus
import com.adamglin.phosphoricons.bold.X
import com.mknlabs.expensetracker.core.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.models.TabItem
import com.mknlabs.expensetracker.core.ui.theme.CardShadowAmbientLight
import com.mknlabs.expensetracker.core.ui.theme.CardShadowSpotLight
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBrandShadow
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmTitleLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateCaptionDark
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateCaptionLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateFieldBorderDark
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateFieldBorderLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreatePlaceholder
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateTitleDark
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCard
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCardBorder
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.breakdownHeroFill
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient

import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.getCurrency
import com.mknlabs.expensetracker.utils.getDateTime

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog

// The itemised mockup's total hero: a violet radial glow over the obsidian card, a white
// amount line and a violet currency glyph. Applied in light mode; dark keeps the gradient
// it has always had.
private val TotalHeroGlow = Color(0x1F8B5CF6)
private val TotalHeroMid = Color(0xF2141826)
private val TotalHeroBase = Color(0xFF0F121C)
private val TotalHeroBorder = Color(0x14FFFFFF)
private val TotalHeroLabel = Color(0xCCC4B5FD)
private val TotalHeroCurrency = Color(0xFFC4B5FD)

// The mockup's dashed "Add New Item" card: violet-500/30 dash over a 40% obsidian strip,
// a violet circle around the plus and violet-200 label. Light keeps the same construction
// with a deeper violet ink so it still reads on white.
private val AddItemBorderDark = Color(0x4D8B5CF6)
private val AddItemFillDark = Color(0x66121520)
private val AddItemPlusFillDark = Color(0x338B5CF6)
private val AddItemIconDark = Color(0xFFC4B5FD)
private val AddItemInkDark = Color(0xFFDDD6FE)
private val AddItemBorderLight = Color(0x738B5CF6)
private val AddItemFillLight = Color(0x0F8B5CF6)
private val AddItemPlusFillLight = Color(0x1F7C3AED)
private val AddItemIconLight = Color(0xFF7C3AED)
private val AddItemInkLight = Color(0xFF6D28D9)

// The mockup's "0 ITEMS" counter pill and its muted "sub-expenses" caption.
private val ItemsPillFillDark = Color(0x992E1065)
private val ItemsPillBorderDark = Color(0x665B21B6)
private val ItemsPillInkDark = Color(0xFFC4B5FD)
private val ItemsPillFillLight = Color(0xFFEDE9FE)
private val ItemsPillBorderLight = Color(0x99C4B5FD)
private val ItemsPillInkLight = Color(0xFF6D28D9)
private val SubExpensesInkDark = Color(0xFF94A3B8)
private val SubExpensesInkLight = Color(0xFF64748B)

@Composable
fun ItemizedCalculatorScreen(
    viewModel: ItemizedCalculatorViewModel,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    initialNote: String? = null,
    initialMode: CalculatorMode = CalculatorMode.ITEMIZED,
    onBackClick: () -> Unit = {},
    onApplyToNoteClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modes = CalculatorMode.entries
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = modes.indexOf(initialMode).coerceAtLeast(0),
        pageCount = { modes.size }
    )
    val context = LocalContext.current
    var showHistorySheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialNote) {
        viewModel.initialize(initialNote)
    }

    LaunchedEffect(initialMode) {
        viewModel.setMode(initialMode)
    }

    // Sync from ViewModel to Pager
    LaunchedEffect(uiState.selectedMode) {
        val targetPage = modes.indexOf(uiState.selectedMode)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Sync from Pager to ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.setMode(modes[page])
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = Dimens.HeaderSpacing, bottom = 14.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -40f) {
                        showHistorySheet = true
                    }
                }
            },
    ) {
        val enter = rememberSectionEnterAlphas(2)
        AppHeader(
            title = stringResource(id = R.string.label_itemized_calculator),
            onBackClick = onBackClick,
            modifier = Modifier.alpha(enter[0]),
            actions = {
                CalculatorHistoryHeaderIcon(
                    onClick = { showHistorySheet = true }
                )
            }
        )

        Column(modifier = Modifier.alpha(enter[1]).weight(1f)) {
        AnimatedTabSwitcher(
            items = modes.map { TabItem(it, stringResource(it.titleRes)) },
            selectedItemId = uiState.selectedMode,
            onItemSelected = { mode ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(modes.indexOf(mode))
                }
            }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .clipToBounds(),
            userScrollEnabled = !uiState.isAddingItem,
            beyondViewportPageCount = 0
        ) { page ->
            when (modes[page]) {
                CalculatorMode.ITEMIZED -> {
                    ItemizedCalculatorContent(
                        // The switcher sits 10dp above the total card: the card brings 8dp
                        // of its own top inset for the shadow, so the page adds the rest.
                        modifier = Modifier.fillMaxSize().padding(top = 2.dp).clipToBounds(),
                        items = uiState.items,
                        currencyId = currencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        totalAmount = uiState.totalAmount,
                        isAddingItem = uiState.isAddingItem,
                        descriptionInput = uiState.descriptionInput,
                        amountInput = uiState.amountInput,
                        canAddItem = uiState.canAddItem,
                        isEditingItem = uiState.editingItemId != null,
                        onDeleteItem = viewModel::deleteItem,
                        onEditItem = viewModel::startEditingItem,
                        onDescriptionChange = viewModel::updateDescriptionInput,
                        onAmountChange = viewModel::updateAmountInput,
                        onStartAdding = viewModel::startAddingItem,
                        onCancelAdding = viewModel::cancelAddingItem,
                        onAddItem = viewModel::addItem,
                        onApplyToNoteClick = {
                            val (amount, note) = viewModel.getFinalResult(currencyId, amountFormatPreferences)
                            onApplyToNoteClick(amount, note)
                        }
                    )
                }

                CalculatorMode.NORMAL -> {
                    NormalCalculatorContent(
                        modifier = Modifier.fillMaxSize().padding(top = 18.dp),
                        display = uiState.normalDisplay,
                        previewResult = viewModel.calculatePreview(),
                        expression = viewModel.buildExpression(),
                        isEvaluated = uiState.shouldResetNormalDisplay,
                        onAction = viewModel::handleNormalAction,
                        onSelectionChanged = viewModel::setSelectionFromDisplay,
                        cursorDisplayPosition = viewModel.getDisplayPositionFromCursor()
                    )
                }
            }
        }
        }
    }

    if (showHistorySheet) {
        CalculatorHistorySheet(
            entries = uiState.historyEntries,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            onDismiss = { showHistorySheet = false },
            onTapExpression = { expression ->
                viewModel.restoreHistoryExpression(expression)
                showHistorySheet = false
            },
            onTapResult = { result ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        context.getString(R.string.label_history),
                        result
                    )
                )
                Toast.makeText(context, context.getString(R.string.toast_history_copied), Toast.LENGTH_SHORT).show()
                viewModel.selectHistoryResult(result)
                showHistorySheet = false
            },
            onDeleteEntry = viewModel::deleteHistoryEntry,
            onClearHistory = viewModel::clearHistory
        )
    }
}

@Composable
private fun CalculatorHistoryHeaderIcon(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (MaterialTheme.colorScheme.isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = stringResource(id = R.string.content_desc_calculator_history),
                tint = MaterialTheme.colorScheme.accentInk,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ItemizedCalculatorContent(
    modifier: Modifier = Modifier,
    items: List<CalculatorLineItem>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    totalAmount: Double,
    isAddingItem: Boolean,
    descriptionInput: String,
    amountInput: String,
    canAddItem: Boolean,
    isEditingItem: Boolean,
    onDeleteItem: (Int) -> Unit,
    onEditItem: (CalculatorLineItem) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onStartAdding: () -> Unit,
    onCancelAdding: () -> Unit,
    onAddItem: () -> Unit,
    onApplyToNoteClick: () -> Unit
) {
    val canApply = items.isNotEmpty()
    val scrollState = rememberScrollState()

    LaunchedEffect(isAddingItem) {
        if (isAddingItem) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TotalAmountCard(
                totalAmount = totalAmount,
                currencyId = currencyId,
                amountFormatPreferences = amountFormatPreferences
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.label_breakdown),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(id = R.string.label_breakdown_sub_expenses),
                        color = if (MaterialTheme.colorScheme.isDark) SubExpensesInkDark else SubExpensesInkLight,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                }

                val isDark = MaterialTheme.colorScheme.isDark
                Text(
                    text = stringResource(id = R.string.label_items_count_formatted, items.size),
                    color = if (isDark) ItemsPillInkDark else ItemsPillInkLight,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isDark) ItemsPillFillDark else ItemsPillFillLight)
                        .border(
                            1.dp,
                            if (isDark) ItemsPillBorderDark else ItemsPillBorderLight,
                            CircleShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEach { item ->
                    BreakdownItemCard(
                        item = item,
                        currencyId = currencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        onDeleteClick = { onDeleteItem(item.id) },
                        onEditClick = { onEditItem(item) }
                    )
                }
            }

            if (isAddingItem) {
                Dialog(onDismissRequest = onCancelAdding) {
                    AddItemInputCard(
                        description = descriptionInput,
                        amount = amountInput,
                        canAddItem = canAddItem,
                        onDescriptionChange = onDescriptionChange,
                        onAmountChange = onAmountChange,
                        onCancel = onCancelAdding,
                        onAddClick = onAddItem,
                        isEditing = isEditingItem
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AddNewItemButton(onClick = onStartAdding)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        ApplyToNoteButton(
            enabled = canApply,
            onClick = onApplyToNoteClick
        )
    }
}

@Composable
private fun NormalCalculatorContent(
    modifier: Modifier = Modifier,
    display: String,
    previewResult: String,
    expression: String?,
    isEvaluated: Boolean = false,
    onAction: (String) -> Unit,
    onSelectionChanged: (Int, Int) -> Unit = { _, _ -> },
    cursorDisplayPosition: Int = 0
) {
    // Landscape / short windows: stack display and keypad SIDE BY SIDE so the
    // keypad keeps its full height instead of being pushed off-screen by the
    // fixed-height keys (roadmap: phone-landscape support).
    val isLandscape = LocalAppWindowInfo.current.isHeightCompact

    if (isLandscape) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            NormalCalculatorDisplay(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxHeight(),
                resultValue = previewResult,
                expression = expression,
                isEvaluated = isEvaluated,
                compact = true,
                onSelectionChanged = onSelectionChanged,
                cursorDisplayPosition = cursorDisplayPosition
            )
            CalculatorKeypad(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight(),
                compact = true,
                hasInput = !expression.isNullOrEmpty(),
                onAction = onAction
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            NormalCalculatorDisplay(
                resultValue = previewResult,
                expression = expression,
                isEvaluated = isEvaluated,
                onSelectionChanged = onSelectionChanged,
                cursorDisplayPosition = cursorDisplayPosition
            )
            CalculatorKeypad(
                modifier = Modifier.fillMaxWidth(),
                hasInput = !expression.isNullOrEmpty(),
                onAction = onAction
            )
        }
    }
}

/**
 * The 4×4 calculator keypad.
 *
 * [compact] (phone landscape / short windows): the keypad renders SIDE BY SIDE
 * with the display and its rows share the available height with small minimums
 * so every key stays visible and tappable. Normal (tall) screens keep the
 * classic fixed 72dp keys with the "=" column flexing.
 */

private fun calculatorDigitIcon(digit: String): ImageVector = when (digit) {
    "0" -> PhosphorIcons.Bold.NumberZero
    "1" -> PhosphorIcons.Bold.NumberOne
    "2" -> PhosphorIcons.Bold.NumberTwo
    "3" -> PhosphorIcons.Bold.NumberThree
    "4" -> PhosphorIcons.Bold.NumberFour
    "5" -> PhosphorIcons.Bold.NumberFive
    "6" -> PhosphorIcons.Bold.NumberSix
    "7" -> PhosphorIcons.Bold.NumberSeven
    "8" -> PhosphorIcons.Bold.NumberEight
    else -> PhosphorIcons.Bold.NumberNine
}

@Composable
private fun CalculatorKeypad(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    hasInput: Boolean = false,
    onAction: (String) -> Unit
) {
    val rowSpacing = if (compact) 8.dp else 12.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        // Row 1: AC, ⌫, %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (compact) {
                        Modifier.weight(1f).heightIn(min = 32.dp)
                    } else {
                        Modifier.height(52.dp)
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                // One key, two labels, the way MIUI and Samsung calculators do
                // it: AC while there is nothing to clear, C once the expression
                // has content. Both only clear the expression - the calculation
                // history is untouched - so the label is the only difference.
                label = stringResource(id = if (hasInput) R.string.label_c else R.string.label_ac),
                accent = true,
                onClick = { onAction("AC") }
            )
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                // The icon carries no text, so `label` becomes the accessibility
                // description here.
                label = stringResource(id = R.string.content_desc_backspace),
                icon = Icons.AutoMirrored.Filled.Backspace,
                accent = true,
                onClick = { onAction("BACKSPACE") },
                // Long press is the same clear as C above.
                onLongClick = { onAction("AC") }
            )
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = "%",
                accent = true,
                onClick = { onAction("%") }
            )
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = "÷",
                icon = PhosphorIcons.Bold.Divide,
                accent = true,
                onClick = { onAction("/") }
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (compact) {
                        Modifier.weight(1f).heightIn(min = 32.dp)
                    } else {
                        Modifier.height(52.dp)
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("7", "8", "9").forEach { value ->
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = value,
                    icon = calculatorDigitIcon(value),
                    onClick = { onAction(value) }
                )
            }
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = "×",
                icon = PhosphorIcons.Bold.X,
                accent = true,
                onClick = { onAction("*") }
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (compact) {
                        Modifier.weight(1f).heightIn(min = 32.dp)
                    } else {
                        Modifier.height(52.dp)
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("4", "5", "6").forEach { value ->
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = value,
                    icon = calculatorDigitIcon(value),
                    onClick = { onAction(value) }
                )
            }
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = "−",
                icon = PhosphorIcons.Bold.Minus,
                accent = true,
                onClick = { onAction("-") }
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (compact) {
                        Modifier.weight(1f).heightIn(min = 32.dp)
                    } else {
                        Modifier.height(52.dp)
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("1", "2", "3").forEach { value ->
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = value,
                    icon = calculatorDigitIcon(value),
                    onClick = { onAction(value) }
                )
            }
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = "+",
                icon = PhosphorIcons.Bold.Plus,
                accent = true,
                onClick = { onAction("+") }
            )
        }

        // Row 5: (, ), 0, .
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (compact) {
                        Modifier.weight(1f).heightIn(min = 32.dp)
                    } else {
                        Modifier.height(52.dp)
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = "(",
                pill = true,
                // MIUI smart bracket: opens a group, closes the open one, or
                // inserts "x(" after a number so the multiplication is explicit.
                // The separate ")" key still force-closes.
                onClick = { onAction("BRACKET") }
            )
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = ")",
                pill = true,
                onClick = { onAction(")") }
            )
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = "0",
                icon = calculatorDigitIcon("0"),
                onClick = { onAction("0") }
            )
            CalculatorKeyButton(
                modifier = Modifier.weight(1f),
                label = ".",
                onClick = { onAction(".") }
            )
        }

        // Row 6: equals spans the full row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (compact) {
                        Modifier.weight(1f).heightIn(min = 32.dp)
                    } else {
                        Modifier.height(52.dp)
                    }
                )
        ) {
            CalculatorKeyButton(
                modifier = Modifier.fillMaxWidth(),
                label = "=",
                icon = PhosphorIcons.Bold.Equals,
                primary = true,
                onClick = { onAction("=") }
            )
        }

    }
}

/**
 * Glyphs that are expression structure rather than operands. These are exactly
 * the symbols [CalculatorToken.Operator] renders (- U+2212, × U+00D7, ÷ U+00F7,
 * +), and none of them can appear inside a [CalculatorToken.Number], so the
 * scan below is exact.
 */
private val ExpressionOperatorGlyphs = setOf('+', '\u2212', '\u00D7', '\u00F7')

/**
 * Renders [text] with its operator glyphs in [operatorColor], leaving digits
 * and brackets in the field's base ink.
 */
private fun buildExpressionWithAccentOperators(
    text: String,
    operatorColor: Color
): AnnotatedString = buildAnnotatedString {
    append(text)
    text.forEachIndexed { index, char ->
        if (char in ExpressionOperatorGlyphs) {
            addStyle(SpanStyle(color = operatorColor), index, index + 1)
        }
    }
}

@Composable
private fun NormalCalculatorDisplay(
    modifier: Modifier = Modifier,
    resultValue: String,
    expression: String?,
    isEvaluated: Boolean = false,
    compact: Boolean = false,
    onSelectionChanged: (Int, Int) -> Unit = { _, _ -> },
    cursorDisplayPosition: Int = 0
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // NOTE: there is no public per-view API to block the IME for a Compose
    // text field — View.setShowSoftInputOnFocus is TextView-only, and Compose
    // renders into an AndroidComposeView (not a TextView). The preventive
    // suppression here is `readOnly = true` on the BasicTextField below:
    // foundation gates its keyboard-show-on-tap on !readOnly (tapToFocus).
    // The hide() guards underneath stay as a safety net for the focus path.

    // SideEffect runs after EVERY successful composition - most aggressive approach
    SideEffect {
        if (isFocused) {
            keyboardController?.hide()
        }
    }

    // Aggressively suppress the keyboard: hide it immediately on focus
    // and keep hiding it whenever the field is focused.
    LaunchedEffect(isFocused) {
        if (isFocused) {
            keyboardController?.hide()
        }
    }



    // Watch for any interaction and suppress keyboard
    val isInteractionFocused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(isInteractionFocused) {
        if (isInteractionFocused) {
            keyboardController?.hide()
        }
    }

    val rawExprText = expression ?: resultValue
    val expressionText = when {
        rawExprText.isBlank() || rawExprText == "0" -> "0"
        rawExprText.startsWith("=") -> rawExprText.removePrefix("=").trim()
        else -> rawExprText
    }
    val showEqualsOnResult = resultValue.isNotBlank() &&
        expressionText != "0" &&
        expressionText.isNotBlank()
    val resultText = if (showEqualsOnResult) "= $resultValue" else resultValue

    var editingAfterEquals by remember { mutableStateOf(false) }
    LaunchedEffect(isEvaluated) {
        if (isEvaluated) editingAfterEquals = false
    }
    val resultEmphasized = isEvaluated && !editingAfterEquals
    val largeSize = if (compact) 22f else 32f
    val smallSize = if (compact) 18f else 24f
    val exprFontSizeFloat by animateFloatAsState(
        targetValue = if (resultEmphasized) smallSize else largeSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ExprFontSize"
    )
    val exprFontSize = exprFontSizeFloat.sp
    val exprColor = MaterialTheme.colorScheme.onSurface

    // Operators carry the accent ink so the expression's structure reads at a
    // glance; digits and brackets keep the neutral onSurface ink.
    val operatorInk = MaterialTheme.colorScheme.accentInk
    val expressionAnnotated = remember(expressionText, operatorInk) {
        buildExpressionWithAccentOperators(expressionText, operatorInk)
    }

    val resultFontSizeFloat by animateFloatAsState(
        targetValue = if (resultEmphasized) largeSize else smallSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ResultFontSize"
    )
    val resultFontSize = resultFontSizeFloat.sp

    val resultColor by animateColorAsState(
        targetValue = if (isEvaluated) {
            MaterialTheme.colorScheme.accentInk
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        label = "ResultColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (compact) 8.dp else 12.dp, top = if (compact) 8.dp else 12.dp, end = if (compact) 8.dp else 12.dp, bottom = if (compact) 4.dp else 6.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            // TOP: Full Expression Line
            // No clickable on the parent Box — let BasicTextField handle taps
            // directly so the cursor lands exactly where the user touched.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (compact) 2.dp else 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                // Keep a persistent TextFieldValue so the cursor position is
                // preserved across recompositions (tap-to-position works).
                var textFieldValue by remember {
                    mutableStateOf(
                        TextFieldValue(
                            annotatedString = expressionAnnotated,
                            selection = TextRange(expressionText.length)
                        )
                    )
                }
                // Sync text from the ViewModel. A ViewModel-driven edit means
                // the model has already deleted or overwritten the highlighted
                // run, so the caret collapses onto the model's caret — which
                // also keeps a multi-token deletion from leaving a stale range
                // pointing past the end of the shorter text.
                if (textFieldValue.text != expressionText) {
                    textFieldValue = TextFieldValue(
                        annotatedString = expressionAnnotated,
                        selection = TextRange(
                            cursorDisplayPosition.coerceIn(0, expressionText.length)
                        )
                    )
                } else if (textFieldValue.annotatedString != expressionAnnotated) {
                    // Same text, new styling (theme or accent switch): update the
                    // spans in place so the caret and any live selection survive.
                    textFieldValue = textFieldValue.copy(annotatedString = expressionAnnotated)
                }
                // Caret-only moves (keypad cursor keys, a tap on the display)
                // still have to reach the field. A live drag-selection is left
                // untouched: the ViewModel already owns that range, so the
                // user's highlight survives until they press a key.
                LaunchedEffect(cursorDisplayPosition) {
                    // Clamped: the ViewModel reports a display offset that can sit
                    // past the end while the expression is being rebuilt, and an
                    // out-of-range TextRange leaves the caret undefined.
                    val target = cursorDisplayPosition.coerceIn(0, textFieldValue.text.length)
                    if (textFieldValue.selection.collapsed &&
                        textFieldValue.selection.start != target
                    ) {
                        textFieldValue = textFieldValue.copy(selection = TextRange(target))
                    }
                }
                // Real blink: ON 500ms / OFF 500ms, restarted by every edit or
                // caret move and stopped as soon as the field loses focus.
                // (The previous version switched the caret on once and then left
                // it off forever, so it read as a single flash.)
                var showCursor by remember { mutableStateOf(false) }
                val caretColor = MaterialTheme.colorScheme.accentInk
                var caretLayout by remember { mutableStateOf<TextLayoutResult?>(null) }
                LaunchedEffect(
                    isFocused,
                    textFieldValue.text,
                    textFieldValue.selection,
                    cursorDisplayPosition
                ) {
                    if (!isFocused) {
                        showCursor = false
                        return@LaunchedEffect
                    }
                    while (true) {
                        showCursor = true
                        delay(500)
                        showCursor = false
                        delay(500)
                    }
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        // Always update so cursor taps are honoured, even when
                        // only the selection changes. Both ends of the range
                        // are forwarded so a drag-selection survives into the
                        // ViewModel: a following editing key overwrites it and
                        // BACKSPACE deletes the whole highlighted run.
                        if (newValue.selection != textFieldValue.selection) {
                            onSelectionChanged(
                                newValue.selection.start,
                                newValue.selection.end
                            )
                        }
                        textFieldValue = newValue
                    },
                    // Read-only, not disabled: taps still focus the field and
                    // still deliver selection changes through onValueChange
                    // (that is all this cursor-positioning code consumes), but
                    // Compose never requests the IME for a read-only field.
                    readOnly = true,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = exprColor,
                        fontWeight = if (isEvaluated) FontWeight.Bold else FontWeight.Medium,
                        fontSize = exprFontSize,
                        textAlign = TextAlign.End
                    ),
                    // Foundation draws no caret for a read-only field - it gates
                    // its cursor on `enabled && !readOnly`
                    // (CoreTextFieldKt: cursor(state, value, offsetMapping,
                    // cursorBrush, enabled && !readOnly && ...)). This brush is
                    // therefore inert and the blinking caret below is drawn by
                    // us; it is kept so the field still behaves correctly should
                    // it ever become editable again.
                    cursorBrush = SolidColor(caretColor),
                    interactionSource = interactionSource,
                    onTextLayout = { result -> caretLayout = result },
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterEnd) {
                            // Hand-drawn caret: `readOnly = true` keeps the IME
                            // away and Compose draws no cursor for such a field.
                            // It is drawn by the box that WRAPS the inner text
                            // field so that both share ONE coordinate space. That
                            // matters: the decoration box places the inner field
                            // with ContentAlignment.End, while getHorizontalPosition
                            // is expressed in the inner field's own space, so a
                            // plain sibling of the decoration box drew the caret
                            // shifted by the inner field's own origin - which
                            // parked it in the blank area left of the expression.
                            Box(
                                modifier = Modifier.drawWithContent {
                                    drawContent()
                                    val layout = caretLayout
                                    if (showCursor &&
                                        textFieldValue.selection.collapsed &&
                                        layout != null &&
                                        layout.lineCount > 0
                                    ) {
                                        val offset = textFieldValue.selection.start
                                            .coerceIn(0, textFieldValue.text.length)
                                        val line = layout.getLineForOffset(offset)
                                        val stroke = 2.dp.toPx()
                                        // Same clamp foundation applies to its own
                                        // caret, so a caret at the end of a full
                                        // line cannot leave the field.
                                        val x = layout
                                            .getHorizontalPosition(
                                                offset,
                                                usePrimaryDirection = true
                                            )
                                            .coerceIn(stroke / 2, size.width - stroke / 2)
                                        val inset = 3.dp.toPx()
                                        drawLine(
                                            color = caretColor,
                                            start = Offset(x, layout.getLineTop(line) + inset),
                                            end = Offset(x, layout.getLineBottom(line) - inset),
                                            strokeWidth = stroke,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            ) {
                                innerTextField()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            isFocused = state.isFocused
                            if (state.isFocused && isEvaluated) {
                                editingAfterEquals = true
                            }
                            // Immediately hide keyboard on any focus change
                            if (state.isFocused) {
                                keyboardController?.hide()
                            }
                        }
                )
            }

            if (showEqualsOnResult) {
                Spacer(modifier = Modifier.height(if (compact) 0.dp else 2.dp))
                Text(
                    text = resultText,
                    color = resultColor,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = if (isEvaluated) FontWeight.Bold else FontWeight.Medium,
                        fontSize = resultFontSize,
                        textAlign = TextAlign.End
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalculatorKeyButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    accent: Boolean = false,
    primary: Boolean = false,
    pill: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val shape = when {
        primary -> RoundedCornerShape(34.dp)
        pill -> RoundedCornerShape(24.dp)
        else -> CircleShape
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keyPressScale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(
                if (primary) Modifier.background(brush = brandGradient()) else Modifier
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onLongClick = onLongClick,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    primary -> MaterialTheme.colorScheme.onCta
                    accent -> MaterialTheme.colorScheme.accentInk
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(28.dp)
            )
        } else {
            Text(
                text = label.orEmpty(),
                // The mock's keypad rule is "neutral keys, purple only on operators", so
                // the operator is told apart by its glyph rather than by its surface. The
                // grey key fill above stays as a second cue; this is the one the spec names.
                color = when {
                    primary -> MaterialTheme.colorScheme.onCta
                    accent -> MaterialTheme.colorScheme.accentInk
                    else -> MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (primary) 28.sp else 24.sp
                )
            )
        }
    }
}

@Composable
private fun TotalAmountCard(
    totalAmount: Double,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val shape = RoundedCornerShape(16.dp)
    // Dark takes the mockup's violet hero; light keeps the standard white card with the
    // brand currency glyph so the amount still reads on a bright page.
    val cardChrome = if (isDark) {
        Modifier
            .shadow(
                elevation = AppCardDefaults.Elevation,
                shape = shape,
                clip = false,
                ambientColor = CardShadowAmbientLight,
                spotColor = CardShadowSpotLight
            )
            .clip(shape)
            .background(breakdownHeroFill())
            .border(1.dp, TotalHeroBorder, shape)
    } else {
        val lightCard = AppCardDefaults.colors()
        Modifier
            .shadow(
                elevation = AppCardDefaults.Elevation,
                shape = shape,
                clip = false,
                ambientColor = CardShadowAmbientLight,
                spotColor = CardShadowSpotLight
            )
            .clip(shape)
            .background(lightCard.containerColor)
            .then(
                lightCard.border?.let { Modifier.border(it, shape) } ?: Modifier
            )
    }

    val fullAmount = formatCurrencyValue(totalAmount, currencyId, amountFormatPreferences)
    val symbol = getCurrency(currencyId).currencySymbol
    val symbolStart = fullAmount.indexOf(symbol)
    val amountText = buildAnnotatedString {
        if (symbolStart < 0) {
            append(fullAmount)
        } else {
            append(fullAmount.substring(0, symbolStart))
            // A thin space keeps the currency glyph off the digits, as the mockup's
            // space-x-1 + mr-0.5 do, whichever side the symbol sits on.
            if (symbolStart > 0) append("\u2009")
            pushStyle(
                SpanStyle(
                    color = if (isDark) TotalHeroCurrency else MaterialTheme.colorScheme.accentInk,
                    fontSize = if (isDark) 34.sp else 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            append(symbol)
            pop()
            if (symbolStart == 0) append("\u2009")
            append(fullAmount.substring(symbolStart + symbol.length))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .then(cardChrome)
            .padding(horizontal = 18.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.label_total_amount),
                color = if (isDark) TotalHeroLabel else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = amountText,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isDark) 44.sp else MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = if (isDark) FontWeight.Black else MaterialTheme.typography.headlineLarge.fontWeight,
                    letterSpacing = if (isDark) (-1).sp else MaterialTheme.typography.headlineLarge.letterSpacing
                )
            )
        }
    }
}

@Composable
private fun BreakdownItemCard(
    item: CalculatorLineItem,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.isDark
    val highlightedBorderColor = colorScheme.accentInk
    // Light flattens every row to the shared card. The row being edited keeps the violet
    // lift and the 2dp ring it announces itself with, so the highlight still reads on
    // white; dark keeps its gradient fill and its lift exactly as they were.
    val lightCard = AppCardDefaults.colors()
    val cardChrome = if (isDark) {
        Modifier
            .shadow(
                elevation = if (item.highlighted) 18.dp else AppCardDefaults.Elevation,
                shape = shape,
                clip = false,
                ambientColor = if (item.highlighted) colorScheme.accentInk.copy(alpha = 0.14f) else CardShadowAmbientLight,
                spotColor = if (item.highlighted) colorScheme.secondary.copy(alpha = 0.12f) else CardShadowSpotLight
            )
            .clip(shape)
            .background(breakdownHeroFill())
            .then(
                if (item.highlighted) Modifier else Modifier.border(1.dp, TotalHeroBorder, shape)
            )
    } else {
        Modifier
            .shadow(
                elevation = if (item.highlighted) 18.dp else AppCardDefaults.Elevation,
                shape = shape,
                clip = false,
                ambientColor = if (item.highlighted) colorScheme.accentInk.copy(alpha = 0.14f) else CardShadowAmbientLight,
                spotColor = if (item.highlighted) colorScheme.secondary.copy(alpha = 0.12f) else CardShadowSpotLight
            )
            .clip(shape)
            .background(lightCard.containerColor)
            .then(
                if (item.highlighted) {
                    Modifier
                } else {
                    lightCard.border?.let { Modifier.border(border = it, shape = shape) } ?: Modifier
                }
            )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardChrome)
            .drawBehind {
                if (item.highlighted) {
                    drawRoundRect(
                        color = highlightedBorderColor,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column 1: the labels on top, their values underneath, one row per half of the
            // block and each row's content centred in its own half. The halves are equal, so
            // the block's height still comes from the 28.dp actions beside it.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.label_description_caps),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = stringResource(id = R.string.label_amount_caps),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.description,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )

                    // No trailing inset: the amount now shares the AMOUNT label's right edge,
                    // which is this column's edge rather than the card's.
                    Text(
                        text = formatCurrencyValue(item.amount, currencyId, amountFormatPreferences),
                        color = MaterialTheme.colorScheme.onSurface,
                        // One line and never ellipsized: this fixed-width child is measured
                        // before the weighted description, so it always has room and the
                        // description is the one that gives way.
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 14.dp)
                    )
                }
            }

            // Column 2: edit over delete, level with the rows they act on.
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BreakdownRowAction(
                    icon = Icons.Rounded.Edit,
                    contentDescription = stringResource(
                        id = R.string.content_desc_edit_item,
                        item.description
                    ),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onEditClick
                )

                BreakdownRowAction(
                    icon = Icons.Rounded.Close,
                    contentDescription = stringResource(
                        id = R.string.content_desc_delete_item,
                        item.description
                    ),
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

/**
 * One 28.dp round action on a breakdown row. The wash and the glyph travel together, so a
 * caller cannot pair a delete glyph with the neutral wash the edit action wears.
 */
@Composable
private fun BreakdownRowAction(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AddItemInputCard(
    description: String,
    amount: String,
    canAddItem: Boolean,
    isEditing: Boolean,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCancel: () -> Unit,
    onAddClick: () -> Unit
) {
    val dark = MaterialTheme.colorScheme.isDark
    val shape = RoundedCornerShape(16.dp)
    // Same fill as Home Spent Today: #101019 in dark, standard light card.
    val cardColors = AppCardDefaults.colors(
        darkContainer = Color(0xFF101019)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(shape)
            .background(cardColors.containerColor)
            .then(
                cardColors.border?.let { Modifier.border(it, shape) } ?: Modifier
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = ModernConfirmBrandShadow,
                        spotColor = ModernConfirmBrandShadow
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(brandGradient()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = stringResource(id = R.string.label_new_item),
                color = if (dark) NamedCreateTitleDark else ModernConfirmTitleLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        ItemizedTextField(
            value = description,
            label = stringResource(id = R.string.label_description),
            placeholder = stringResource(id = R.string.placeholder_description_ex),
            onValueChange = onDescriptionChange
        )

        ItemizedTextField(
            value = amount,
            label = stringResource(id = R.string.label_amount),
            placeholder = stringResource(id = R.string.placeholder_amount_zero),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            onValueChange = onAmountChange
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryActionButton(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.label_cancel_1),
                onClick = onCancel
            )

            PrimaryActionButton(
                modifier = Modifier.weight(1f),
                label = stringResource(id = if (isEditing) R.string.label_update_action else R.string.label_add_item),
                enabled = canAddItem,
                onClick = onAddClick
            )
        }
    }
}

@Composable
private fun ItemizedTextField(
    value: String,
    label: String,
    placeholder: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit
) {
    val dark = MaterialTheme.colorScheme.isDark
    val fieldFill = AppCardDefaults.colors(
        darkContainer = MaterialTheme.colorScheme.manageCategoryCard,
        darkBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.manageCategoryCardBorder)
    ).containerColor
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        label = {
            Text(
                text = label,
                color = if (dark) NamedCreateCaptionDark else NamedCreateCaptionLight,
                fontSize = 12.sp
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = NamedCreatePlaceholder,
                fontSize = 14.sp
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fieldFill,
            unfocusedContainerColor = fieldFill,
            focusedBorderColor = if (dark) NamedCreateFieldBorderDark else NamedCreateFieldBorderLight,
            unfocusedBorderColor = if (dark) NamedCreateFieldBorderDark else NamedCreateFieldBorderLight,
            cursorColor = MaterialTheme.colorScheme.accentInk,
            focusedTextColor = if (dark) NamedCreateTitleDark else ModernConfirmTitleLight,
            unfocusedTextColor = if (dark) NamedCreateTitleDark else ModernConfirmTitleLight,
            focusedLabelColor = if (dark) NamedCreateCaptionDark else NamedCreateCaptionLight,
            unfocusedLabelColor = if (dark) NamedCreateCaptionDark else NamedCreateCaptionLight
        )
    )
}

@Composable
private fun AddNewItemButton(onClick: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.isDark
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (isDark) AddItemBorderDark else AddItemBorderLight
    val fillColor = if (isDark) AddItemFillDark else AddItemFillLight
    val plusFill = if (isDark) AddItemPlusFillDark else AddItemPlusFillLight
    val iconTint = if (isDark) AddItemIconDark else AddItemIconLight
    val labelColor = if (isDark) AddItemInkDark else AddItemInkLight

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .heightIn(min = 72.dp)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                        cap = StrokeCap.Round
                    )
                )
            }
            .clip(shape)
            .background(fillColor)
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(plusFill)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(id = R.string.content_desc_add_item),
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(id = R.string.label_add_new_item),
            color = labelColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        )
    }
}

@Composable
private fun SecondaryActionButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit
) {
    val dark = MaterialTheme.colorScheme.isDark
    val fieldFill = AppCardDefaults.colors(
        darkContainer = MaterialTheme.colorScheme.manageCategoryCard,
        darkBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.manageCategoryCardBorder)
    ).containerColor
    val fieldBorder = if (dark) NamedCreateFieldBorderDark else NamedCreateFieldBorderLight
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(fieldFill)
            .border(1.dp, fieldBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (dark) NamedCreateTitleDark else ModernConfirmTitleLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PrimaryActionButton(
    modifier: Modifier = Modifier,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = ModernConfirmBrandShadow,
                spotColor = ModernConfirmBrandShadow
            )
            .clip(RoundedCornerShape(12.dp))
            .background(brush = brandGradient())
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBrandGradient,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBrandGradient,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ApplyToNoteButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.55f)
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.28f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(brush = brandGradient())
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(id = R.string.label_apply_to_note),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(id = R.string.label_apply_to_note),
                color = MaterialTheme.colorScheme.onCta,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun CalculatorHistorySheet(
    entries: List<CalculatorHistoryEntry>,
    dateFormatPattern: String,
    timeFormat: String,
    onDismiss: () -> Unit,
    onTapExpression: (String) -> Unit,
    onTapResult: (String) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onClearHistory: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var selectedLongPressEntry by remember { mutableStateOf<CalculatorHistoryEntry?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f),
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_history),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                if (entries.isNotEmpty()) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(id = R.string.label_clear_history),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.msg_calculator_history_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries) { entry ->
                        CalculatorHistoryEntryRow(
                            entry = entry,
                            dateFormatPattern = dateFormatPattern,
                            timeFormat = timeFormat,
                            onTapExpression = onTapExpression,
                            onTapResult = onTapResult,
                            onLongPress = { selectedLongPressEntry = it }
                        )
                    }
                }
                Text(
                    text = stringResource(id = R.string.msg_history_long_press_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showDeleteConfirm) {
        ModernConfirmationDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = stringResource(id = R.string.label_clear_history),
            text = stringResource(id = R.string.msg_clear_history_confirm),
            confirmButtonText = stringResource(id = R.string.label_delete_confirm),
            onConfirm = {
                showDeleteConfirm = false
                onClearHistory()
            },
            headerIcon = Icons.Filled.Delete,
            dismissButtonText = stringResource(id = R.string.label_cancel_confirm),
            onDismiss = { showDeleteConfirm = false }
        )
    }

    selectedLongPressEntry?.let { entry ->
        Dialog(onDismissRequest = { selectedLongPressEntry = null }) {
            QuickActionCard(
                title = stringResource(R.string.title_quick_action),
                caption = stringResource(R.string.msg_history_quick_action_caption),
                options = listOf(
                    QuickActionOption("copy_expr", stringResource(R.string.label_copy_expression), Icons.Filled.ContentCopy),
                    QuickActionOption("copy_result", stringResource(R.string.label_copy_result), Icons.Filled.ContentCopy),
                    QuickActionOption("copy_both", stringResource(R.string.label_copy_expression_and_result), Icons.Filled.ContentCopy),
                    QuickActionOption("delete", stringResource(R.string.label_delete_entry), Icons.Filled.Delete)
                ),
                selectedId = null,
                onSelect = { id ->
                    when (id) {
                        "copy_expr" -> {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.label_copy_expression), entry.expression))
                            Toast.makeText(context, context.getString(R.string.toast_expression_copied), Toast.LENGTH_SHORT).show()
                            selectedLongPressEntry = null
                            onDismiss()
                        }
                        "copy_result" -> {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.label_copy_result), entry.result))
                            Toast.makeText(context, context.getString(R.string.toast_result_copied), Toast.LENGTH_SHORT).show()
                            selectedLongPressEntry = null
                            onDismiss()
                        }
                        "copy_both" -> {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText(
                                    context.getString(R.string.label_copy_expression_and_result),
                                    "${entry.expression} = ${entry.result}"
                                )
                            )
                            Toast.makeText(context, context.getString(R.string.toast_history_copied), Toast.LENGTH_SHORT).show()
                            selectedLongPressEntry = null
                            onDismiss()
                        }
                        "delete" -> {
                            onDeleteEntry(entry.timestampMillis)
                            Toast.makeText(context, context.getString(R.string.toast_entry_deleted), Toast.LENGTH_SHORT).show()
                            selectedLongPressEntry = null
                        }
                    }
                },
                headerIcon = Icons.Rounded.History
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalculatorHistoryEntryRow(
    entry: CalculatorHistoryEntry,
    dateFormatPattern: String,
    timeFormat: String,
    onTapExpression: (String) -> Unit,
    onTapResult: (String) -> Unit,
    onLongPress: (CalculatorHistoryEntry) -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (MaterialTheme.colorScheme.isDark) standardCardGradient() else SolidColor(MaterialTheme.colorScheme.surfaceVariant))
            .combinedClickable(
                onClick = { onTapResult(entry.result) },
                onLongClick = { onLongPress(entry) }
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entry.expression,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTapExpression(entry.expression) }
                    .padding(vertical = 2.dp, horizontal = 4.dp)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTapResult(entry.result) }
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "=",
                    color = MaterialTheme.colorScheme.accentInk,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = entry.result,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = getDateTime(entry.timestampMillis, dateFormatPattern, timeFormat),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        IconButton(
            onClick = { onTapResult(entry.result) },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = stringResource(id = R.string.content_desc_copy_history),
                tint = MaterialTheme.colorScheme.accentInk,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
private fun ItemizedCalculatorScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        // ItemizedCalculatorScreen requires a ViewModel, so we can't easily preview it without a mock or dummy
        AddItemInputCard(
            description = "appy fizz",
            amount = "1000",
            canAddItem = true,
            isEditing = false,
            onDescriptionChange = {},
            onAmountChange = {},
            onCancel = {},
            onAddClick = {}
        )
    }
}

@Preview(name = "Normal tab light", showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun CalculatorNormalTabPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        CalculatorNormalScreenPreviewBody()
    }
}

@Preview(
    name = "Normal tab dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun CalculatorNormalTabPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        CalculatorNormalScreenPreviewBody()
    }
}

@Composable
private fun CalculatorNormalScreenPreviewBody() {
    CompositionLocalProvider(
        LocalAppWindowInfo provides AppWindowInfo(
            width = AppWindowSize.Compact,
            height = AppWindowHeight.Expanded
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = Dimens.HeaderSpacing, bottom = 14.dp)
        ) {
            AppHeader(
                title = stringResource(id = R.string.label_itemized_calculator),
                onBackClick = {},
                actions = {
                    CalculatorHistoryHeaderIcon(onClick = {})
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                AnimatedTabSwitcher(
                    items = CalculatorMode.entries.map { TabItem(it, stringResource(it.titleRes)) },
                    selectedItemId = CalculatorMode.NORMAL,
                    onItemSelected = {}
                )
                NormalCalculatorContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp),
                    display = "142",
                    previewResult = "142",
                    expression = "100+42",
                    isEvaluated = false,
                    onAction = {},
                    onSelectionChanged = { _, _ -> },
                    cursorDisplayPosition = 7
                )
            }
        }
    }
}
