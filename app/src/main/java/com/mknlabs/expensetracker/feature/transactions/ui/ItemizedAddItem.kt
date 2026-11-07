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
internal fun AddItemInputCard(
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
internal fun ItemizedTextField(
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
internal fun AddNewItemButton(onClick: () -> Unit) {
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

