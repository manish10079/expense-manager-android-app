package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CalculatorLineItem
import com.mknlabs.expensetracker.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.models.TabItem
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.viewmodels.CalculatorMode
import com.mknlabs.expensetracker.ui.viewmodels.ItemizedCalculatorViewModel
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch

@Composable
fun ItemizedCalculatorScreen(
    viewModel: ItemizedCalculatorViewModel,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    initialNote: String? = null,
    onBackClick: () -> Unit = {},
    onApplyToNoteClick: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val modes = CalculatorMode.entries
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = modes.indexOf(uiState.selectedMode).coerceAtLeast(0),
        pageCount = { modes.size }
    )

    LaunchedEffect(initialNote) {
        viewModel.initialize(initialNote)
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        AppHeader(
            title = stringResource(id = R.string.label_itemized_calculator),
            onBackClick = onBackClick
        )

        AnimatedTabSwitcher(
            items = modes.map { TabItem(it, it.title) },
            selectedItemId = uiState.selectedMode,
            onItemSelected = { mode ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(modes.indexOf(mode))
                }
            }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = !uiState.isAddingItem,
            beyondViewportPageCount = 1
        ) { page ->
            when (modes[page]) {
                CalculatorMode.ITEMIZED -> {
                    ItemizedCalculatorContent(
                        modifier = Modifier.fillMaxSize(),
                        items = uiState.items,
                        currencyId = currencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        totalAmount = uiState.totalAmount,
                        isAddingItem = uiState.isAddingItem,
                        descriptionInput = uiState.descriptionInput,
                        amountInput = uiState.amountInput,
                        canAddItem = uiState.canAddItem,
                        onDeleteItem = viewModel::deleteItem,
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
                        modifier = Modifier.fillMaxSize(),
                        display = uiState.normalDisplay,
                        previewResult = viewModel.calculatePreview(),
                        expression = viewModel.buildExpression(),
                        onAction = viewModel::handleNormalAction
                    )
                }
            }
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
    onDeleteItem: (Int) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                Text(
                    text = stringResource(id = R.string.label_breakdown),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Text(
                    text = stringResource(id = R.string.label_items_count_formatted, items.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        onDeleteClick = { onDeleteItem(item.id) }
                    )
                }
            }

            if (isAddingItem) {
                AddItemInputCard(
                    description = descriptionInput,
                    amount = amountInput,
                    canAddItem = canAddItem,
                    onDescriptionChange = onDescriptionChange,
                    onAmountChange = onAmountChange,
                    onCancel = onCancelAdding,
                    onAddClick = onAddItem
                )
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
private fun NormalCalculatorContent(
    modifier: Modifier = Modifier,
    display: String,
    previewResult: String,
    expression: String?,
    onAction: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        NormalCalculatorDisplay(
            resultValue = previewResult,
            expression = expression
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = "AC",
                    onClick = { onAction("AC") }
                )
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    onClick = { onAction("BACKSPACE") }
                )
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = "%",
                    onClick = { onAction("%") }
                )
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = "×",
                    accent = true,
                    onClick = { onAction("*") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("7", "8", "9").forEach { value ->
                    CalculatorKeyButton(
                        modifier = Modifier.weight(1f),
                        label = value,
                        onClick = { onAction(value) }
                    )
                }
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = "−",
                    accent = true,
                    onClick = { onAction("-") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("4", "5", "6").forEach { value ->
                    CalculatorKeyButton(
                        modifier = Modifier.weight(1f),
                        label = value,
                        onClick = { onAction(value) }
                    )
                }
                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = "+",
                    accent = true,
                    onClick = { onAction("+") }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(3f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("1", "2", "3").forEach { value ->
                            CalculatorKeyButton(
                                modifier = Modifier.weight(1f),
                                label = value,
                                onClick = { onAction(value) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalculatorKeyButton(
                            modifier = Modifier.weight(2f),
                            label = "0",
                            pill = true,
                            onClick = { onAction("0") }
                        )
                        CalculatorKeyButton(
                            modifier = Modifier.weight(1f),
                            label = ".",
                            onClick = { onAction(".") }
                        )
                    }
                }

                CalculatorKeyButton(
                    modifier = Modifier.weight(1f),
                    label = "=",
                    primary = true,
                    buttonHeight = 148.dp,
                    onClick = { onAction("=") }
                )
            }
        }
    }
}

@Composable
private fun NormalCalculatorDisplay(
    resultValue: String,
    expression: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                shape = RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 22.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = resultValue,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(standardCardGradient())
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = expression ?: resultValue,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CalculatorKeyButton(
    modifier: Modifier = Modifier,
    label: String? = null,
    icon: ImageVector? = null,
    accent: Boolean = false,
    primary: Boolean = false,
    pill: Boolean = false,
    buttonHeight: androidx.compose.ui.unit.Dp = if (primary) 168.dp else 72.dp,
    onClick: () -> Unit
) {
    val shape = when {
        primary -> RoundedCornerShape(34.dp)
        pill -> RoundedCornerShape(24.dp)
        else -> CircleShape
    }

    Box(
        modifier = modifier
            .height(buttonHeight)
            .shadow(
                elevation = if (primary) 18.dp else if (accent) 10.dp else 0.dp,
                shape = shape,
                ambientColor = if (primary) MaterialTheme.colorScheme.primary.copy(alpha = 0.34f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                spotColor = if (primary) MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(
                brush = when {
                    primary -> Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                    )
                    accent -> Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                    )
                    else -> standardCardGradient()
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = label.orEmpty(),
                color = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(standardCardGradient())
            .padding(horizontal = 18.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.label_total_amount),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatCurrencyValue(totalAmount, currencyId, amountFormatPreferences),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp
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
    onDeleteClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    val highlightedBorderColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (item.highlighted) 18.dp else 0.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = if (item.highlighted) 0.14f else 0f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = if (item.highlighted) 0.12f else 0f)
            )
            .clip(shape)
            .background(standardCardGradient())
            .drawBehind {
                if (item.highlighted) {
                    drawRoundRect(
                        color = highlightedBorderColor,
                        cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.description,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatCurrencyValue(item.amount, currencyId, amountFormatPreferences),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.padding(start = 14.dp, end = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = stringResource(id = R.string.content_desc_delete_item, item.description),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddItemInputCard(
    description: String,
    amount: String,
    canAddItem: Boolean,
    onDescriptionChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCancel: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(standardCardGradient())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.label_new_item),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )
        )

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
                label = stringResource(id = R.string.label_add_item),
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        label = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun AddNewItemButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f)),
                        cap = StrokeCap.Round
                    )
                )
            }
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0f))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(id = R.string.content_desc_add_item),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(id = R.string.label_add_new_item),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
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
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(standardCardGradient())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
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
            .height(54.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
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
            .height(72.dp)
            .alpha(if (enabled) 1f else 0.55f)
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(id = R.string.label_apply_to_note),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(id = R.string.label_apply_to_note),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        }
    }
}

@Preview(
    name = "Itemized Calculator",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun ItemizedCalculatorScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        // ItemizedCalculatorScreen requires a ViewModel, so we can't easily preview it without a mock or dummy
    }
}
