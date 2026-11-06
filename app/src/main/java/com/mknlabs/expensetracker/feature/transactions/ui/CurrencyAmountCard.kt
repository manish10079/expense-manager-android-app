package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.CardShadowAmbientLight
import com.mknlabs.expensetracker.core.ui.theme.CardShadowSpotLight
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.models.CurrencyPosition
import com.mknlabs.expensetracker.utils.getCurrency

// The mock's amount card: white in light, a vertical #171B2B → #121524 wash in dark.
internal val AmountCardDarkTop = Color(0xFF171B2B)
internal val AmountCardDarkBottom = Color(0xFF121524)
internal val AmountCardLight = Color(0xFFFFFFFF)
internal val AmountCardGradient = Brush.verticalGradient(
    listOf(AmountCardDarkTop, AmountCardDarkBottom)
)

// The mock's corner tint: a circle pushed past the card's top-right corner, its
// colour following the selected type.
internal val AmountGlowRadius = 160.dp
internal const val AmountGlowAlpha = 0.10f

@Composable
internal fun CurrencyAmountCard(
    amountText: String,
    currencyId: Int,
    selectedTransactionTypeId: Int,
    compact: Boolean,
    footerContent: (@Composable () -> Unit)? = null,
    focusRequester: FocusRequester,
    canFocus: Boolean = true,
    onAmountChange: (String) -> Unit,
    onClick: () -> Unit = {},
    onImeNext: () -> Unit = {},
    onCalculatorClick: () -> Unit = {}
) {
    val shape = RoundedCornerShape(if (compact) 20.dp else 24.dp)
    val currency = getCurrency(currencyId)
    // Mirrors the selected Income/Expense tab so the figure and its currency
    // symbol read as part of the same choice.
    val amountColor = if (selectedTransactionTypeId == incomeTypeId) {
        MaterialTheme.colorScheme.income
    } else {
        MaterialTheme.colorScheme.expense
    }

    // A lone "0" is the untouched state rather than a figure the user typed, so it is
    // drawn as a placeholder: the same income/expense ink, at reduced strength. Keeping
    // the hue rather than a neutral grey is what tells the user which colour the amount
    // will take before they type; once a real value arrives the ink returns to full
    // strength. The caret keeps the full ink so it stays legible over the dimmed digits.
    val isPlaceholder = amountText == "0"
    val amountInk = if (isPlaceholder) amountColor.copy(alpha = 0.35f) else amountColor

    // The figure and its currency share one size and both sit at regular weight: they read
    // as a single line of text rather than a bold headline with a smaller symbol pinned to
    // it. Declared once so the two can never drift apart.
    val amountTextStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Normal,
        fontSize = if (compact) 22.sp else 24.sp,
        lineHeight = if (compact) 26.sp else 28.sp,
        color = amountInk
    )

    val density = LocalDensity.current
    val labelTranslationY = with(density) { 2.dp.toPx() }
    // Side of the calculator button on the amount row. The row mirrors it with a spacer of
    // the same size, so this one value governs both the button and the figure's centre.
    val calculatorSize = if (compact) 36.dp else 40.dp
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // The card keeps a minimum height for the untouched state; when the quick
            // favorites footer is present the content grows past it instead of clipping.
            .heightIn(min = if (compact) 90.dp else 104.dp)
            .shadow(
                elevation = if (MaterialTheme.colorScheme.isDark) 8.dp else 12.dp,
                shape = shape,
                ambientColor = if (MaterialTheme.colorScheme.isDark) MaterialTheme.colorScheme.accentInk.copy(alpha = 0.1f) else CardShadowAmbientLight,
                spotColor = if (MaterialTheme.colorScheme.isDark) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else CardShadowSpotLight
            )
            .clip(shape)
            .background(
                if (MaterialTheme.colorScheme.isDark) AmountCardGradient else SolidColor(AmountCardLight)
            )
            .border(
                width = 1.dp,
                color = if (MaterialTheme.colorScheme.isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .drawBehind {
                val radiusPx = AmountGlowRadius.toPx()
                // The tint radiates from the card's top-right corner.
                val glowCenter = Offset(x = size.width, y = 0f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(amountColor.copy(alpha = AmountGlowAlpha), Color.Transparent),
                        center = glowCenter,
                        radius = radiusPx
                    ),
                    radius = radiusPx,
                    center = glowCenter
                )
            }
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 16.dp else 20.dp, // Tightened horizontal padding
                    vertical = if (compact) 10.dp else 12.dp    // Tightened vertical padding
                ),
            // SpaceBetween keeps the label and figure apart in the short state; with the
            // quick-favorites footer the card wraps its content, so pack it and keep the
            // footer against the card's bottom padding.
            verticalArrangement = if (footerContent == null) Arrangement.SpaceBetween else Arrangement.Top
        ) {
            // Top Section
            Column {
                Text(
                    text = stringResource(R.string.label_enter_amount),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // 10% up on the labelSmall token, computed rather than hardcoded so the
                    // user's font-scale setting still applies.
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.1f
                    ),
                    modifier = Modifier
                        .graphicsLayer { translationY = labelTranslationY }
                )

                Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))
            }

            // Middle/Bottom Section: Amount Display. The symbol and figure are left-aligned
            // to the card's own padding, so typing grows the amount away from the currency
            // instead of sliding the pair around a centre. The calculator sits at the card's
            // trailing edge — it edits this figure, so it belongs beside the currency rather
            // than in the note row below.
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currency.position == CurrencyPosition.PREFIX) {
                    Text(
                        text = currency.currencySymbol,
                        color = amountColor,
                        style = amountTextStyle,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = amountText,
                        color = Color.Transparent,
                        style = amountTextStyle.copy(color = Color.Transparent),
                        maxLines = 1
                    )
                    var textFieldValue by remember {
                        mutableStateOf(
                            TextFieldValue(
                                text = amountText,
                                selection = TextRange(amountText.length)
                            )
                        )
                    }
                    // Sync text from parent (favorite, voice, calculator). Typing already
                    // writes textFieldValue first, so a mismatch here is always an external
                    // replace — put the caret after the last digit.
                    LaunchedEffect(amountText) {
                        if (textFieldValue.text != amountText) {
                            textFieldValue = TextFieldValue(
                                text = amountText,
                                selection = TextRange(amountText.length)
                            )
                        }
                    }
                    val amountSelectionColors = TextSelectionColors(
                        handleColor = amountColor,
                        backgroundColor = amountColor.copy(alpha = 0.3f)
                    )
                    CompositionLocalProvider(LocalTextSelectionColors provides amountSelectionColors) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            val validated = validateAmountChange(
                                newValue.text, textFieldValue.text
                            )
                            val selection = when {
                                validated == "0" && newValue.text.isEmpty() ->
                                    TextRange(validated.length)
                                else -> TextRange(
                                    newValue.selection.start.coerceAtMost(validated.length),
                                    newValue.selection.end.coerceAtMost(validated.length)
                                )
                            }
                            textFieldValue = TextFieldValue(
                                text = validated,
                                selection = selection
                            )
                            // Only propagate when the text actually changed
                            if (validated != amountText) {
                                onAmountChange(validated)
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .focusProperties { this.canFocus = canFocus }
                            .fillMaxWidth(),
                        textStyle = amountTextStyle.copy(textAlign = TextAlign.Start),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { onImeNext() }
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(amountColor)
                    )
                    }
                }

                if (currency.position == CurrencyPosition.POSTFIX) {
                    Text(
                        text = currency.currencySymbol,
                        color = amountColor,
                        style = amountTextStyle,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            if (footerContent != null) {
                // No divider above the quick-favorites row: the card fill and the label
                // already separate them, and a hairline reads as a stray line in light mode.
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    footerContent()
                }
            }
            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}
