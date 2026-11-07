package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CalculatorLineItem
import com.adamglin.phosphoricons.Bold
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
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
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.breakdownHeroFill

import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.getCurrency

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
    initialNote: String? = null,
    onBackClick: () -> Unit = {},
    onOpenCalculator: () -> Unit = {},
    onApplyToNoteClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialNote) {
        viewModel.initialize(initialNote)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = Dimens.HeaderSpacing, bottom = 14.dp)
    ) {
        val enter = rememberSectionEnterAlphas(2)
        AppHeader(
            title = stringResource(id = R.string.label_itemized_calculator),
            onBackClick = onBackClick,
            modifier = Modifier.alpha(enter[0]),
            actions = {
                OpenCalculatorHeaderIcon(onClick = onOpenCalculator)
            }
        )

        ItemizedCalculatorContent(
            modifier = Modifier
                .alpha(enter[1])
                .weight(1f)
                .fillMaxSize()
                .padding(top = 2.dp)
                .clipToBounds(),
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
}


@Composable
internal fun OpenCalculatorHeaderIcon(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(48.dp),
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
                imageVector = Icons.Filled.Calculate,
                contentDescription = stringResource(id = R.string.desc_open_calculator),
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

            AddNewItemButton(onClick = onStartAdding)
            Spacer(modifier = Modifier.height(8.dp))
        }

        ApplyToNoteButton(
            enabled = canApply,
            onClick = onApplyToNoteClick
        )
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
            .background(Color(0xFF101019))
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
        darkContainer = Color(0xFF101019)
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
            .fillMaxWidth()
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
        darkContainer = Color(0xFF101019)
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
