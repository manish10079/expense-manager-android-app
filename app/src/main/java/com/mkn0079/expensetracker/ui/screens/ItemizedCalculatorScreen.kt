package com.mkn0079.expensetracker.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CalculatorLineItem
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurpleGlow
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.ui.viewmodels.CalculatorMode
import com.mkn0079.expensetracker.ui.viewmodels.ItemizedCalculatorViewModel
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.utils.formatCurrencyValue

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

    LaunchedEffect(initialNote) {
        viewModel.initialize(initialNote)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        CalculatorHeader(onBackClick = onBackClick)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )

        CalculatorModeTabs(
            selectedMode = uiState.selectedMode,
            onModeSelected = viewModel::setMode
        )

        when (uiState.selectedMode) {
            CalculatorMode.ITEMIZED -> {
                ItemizedCalculatorContent(
                    modifier = Modifier.weight(1f),
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
                    modifier = Modifier.weight(1f),
                    display = uiState.normalDisplay,
                    previewResult = viewModel.calculatePreview(),
                    expression = viewModel.buildExpression(),
                    onAction = viewModel::handleNormalAction
                )
            }
        }
    }
}

@Composable
private fun CalculatorHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.02f))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color(0xFFE0D7F4),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Itemized Calculator",
            color = PurplePrimary,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
    }
}

@Composable
private fun CalculatorModeTabs(
    selectedMode: CalculatorMode,
    onModeSelected: (CalculatorMode) -> Unit
) {
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val selectedIndex = CalculatorMode.entries.indexOf(selectedMode).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { containerWidthPx = it.width }
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF17171A))
            .padding(4.dp)
    ) {
        val tabWidth = with(density) { (containerWidthPx.toDp() - 8.dp) / CalculatorMode.entries.size }
        
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "calculator_mode_indicator_offset"
        )

        // Sliding indicator (Pill)
        if (containerWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PurplePrimary, Color(0xFFB89AF7))
                        )
                    )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            CalculatorMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF24114C) else Color(0xFFD9D0E8),
                    label = "calculator_mode_text_color"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.title,
                        color = animatedColor,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
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

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
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
                    text = "Breakdown",
                    color = Color(0xFFE7E3EB),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Text(
                    text = "${items.size} ITEMS",
                    color = Color(0xFF9A919F),
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF17131F), Color(0xFF0F0D14))
                )
            )
            .border(
                width = 1.dp,
                color = PurplePrimary.copy(alpha = 0.16f),
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
                color = Color(0xFFB89AF7),
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
                    .background(Color(0xFF16131B))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = expression ?: resultValue,
                    color = Color(0xFFF2EDFF),
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
                ambientColor = if (primary) PurplePrimary.copy(alpha = 0.34f) else PurpleGlow.copy(alpha = 0.10f),
                spotColor = if (primary) PurpleGlow.copy(alpha = 0.28f) else PurplePrimary.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(
                brush = when {
                    primary -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF7D5AF8), Color(0xFF5B2FCB))
                    )
                    accent -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF221736), Color(0xFF171021))
                    )
                    else -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF1F1E22), Color(0xFF18181B))
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (primary) Color(0xFF210A53) else Color(0xFFECE7F6),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = label.orEmpty(),
                color = if (primary) Color(0xFF210A53) else Color(0xFFECE7F6),
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
            .background(Color(0xFF1F1D20))
            .padding(horizontal = 18.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TOTAL AMOUNT",
                color = Color(0xFF9D92AA),
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatCurrencyValue(totalAmount, currencyId, amountFormatPreferences),
                color = Color(0xFFF3F0F4),
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (item.highlighted) 18.dp else 0.dp,
                shape = shape,
                ambientColor = PurplePrimary.copy(alpha = if (item.highlighted) 0.14f else 0f),
                spotColor = PurpleGlow.copy(alpha = if (item.highlighted) 0.12f else 0f)
            )
            .clip(shape)
            .background(Color(0xFF232124))
            .drawBehind {
                if (item.highlighted) {
                    drawRoundRect(
                        color = Color(0xFFD1BCFF),
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
                    text = "DESCRIPTION",
                    color = Color(0xFF9D92AA),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Text(
                    text = "AMOUNT",
                    color = Color(0xFF9D92AA),
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
                    color = Color(0xFFF0EBF5),
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
                    color = Color(0xFFF0EBF5),
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
                        .background(Color.White.copy(alpha = 0.02f))
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete ${item.description}",
                        tint = Color(0xFFA89DB5),
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
            .background(Color(0xFF17171A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "NEW ITEM",
            color = Color(0xFFD4C2FF),
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )
        )

        ItemizedTextField(
            value = description,
            label = "Description",
            placeholder = "Ex. Coffee House",
            onValueChange = onDescriptionChange
        )

        ItemizedTextField(
            value = amount,
            label = "Amount",
            placeholder = "0.00",
            onValueChange = onAmountChange
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryActionButton(
                modifier = Modifier.weight(1f),
                label = "Cancel",
                onClick = onCancel
            )

            PrimaryActionButton(
                modifier = Modifier.weight(1f),
                label = "Add Item",
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
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(
                text = label,
                color = Color(0xFFBEB4CB)
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF7F748C)
            )
        },
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF222024),
            unfocusedContainerColor = Color(0xFF222024),
            focusedBorderColor = PurpleAccent,
            unfocusedBorderColor = Color(0xFF3A3048),
            cursorColor = PurpleAccent,
            focusedTextColor = Color(0xFFF3F0F4),
            unfocusedTextColor = Color(0xFFF3F0F4),
            focusedLabelColor = PurpleAccent,
            unfocusedLabelColor = Color(0xFFBEB4CB)
        )
    )
}

@Composable
private fun AddNewItemButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFF3B3048),
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f)),
                        cap = StrokeCap.Round
                    )
                )
            }
            .clip(shape)
            .background(Color.Transparent)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add item",
            tint = Color(0xFFE9DEF9),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Add New Item",
            color = Color(0xFFE9DEF9),
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
            .background(Color(0xFF26232B))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFFE5DCF7),
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
                    colors = listOf(Color(0xFF7A56F5), Color(0xFFC3AEFF))
                )
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color(0xFF220A53),
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
                ambientColor = PurplePrimary.copy(alpha = 0.28f),
                spotColor = PurpleGlow.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF7A56F5), Color(0xFFC3AEFF))
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
                    .background(Color(0xFF2A1558)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Apply to note",
                    tint = Color(0xFFEFE9FA),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Apply to Note",
                color = Color(0xFF220A53),
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
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun ItemizedCalculatorScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        // ItemizedCalculatorScreen requires a ViewModel, so we can't easily preview it without a mock or dummy
    }
}
