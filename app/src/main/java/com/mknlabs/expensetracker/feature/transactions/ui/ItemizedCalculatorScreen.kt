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
internal fun ItemizedCalculatorContent(
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


