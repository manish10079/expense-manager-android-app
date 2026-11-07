package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
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
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta

import com.mknlabs.expensetracker.utils.AppLocaleHelper
import com.mknlabs.expensetracker.utils.localizeWesternDigits
import com.mknlabs.expensetracker.utils.usesNativeDigits

import kotlinx.coroutines.delay

@Composable
internal fun NormalCalculatorContent(
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

internal fun calculatorDigitIcon(digit: String): ImageVector = when (digit) {
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
internal fun CalculatorKeypad(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    hasInput: Boolean = false,
    onAction: (String) -> Unit
) {
    AppLocaleHelper.appliedLanguageTags
    val languageTag = AppLocaleHelper.getCurrentLanguageTag()
    val useNativeDigits = usesNativeDigits(languageTag)
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
                    label = localizeWesternDigits(value, languageTag),
                    icon = if (useNativeDigits) null else calculatorDigitIcon(value),
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
                    label = localizeWesternDigits(value, languageTag),
                    icon = if (useNativeDigits) null else calculatorDigitIcon(value),
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
                    label = localizeWesternDigits(value, languageTag),
                    icon = if (useNativeDigits) null else calculatorDigitIcon(value),
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
                label = localizeWesternDigits("0", languageTag),
                icon = if (useNativeDigits) null else calculatorDigitIcon("0"),
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
internal val ExpressionOperatorGlyphs = setOf('+', '\u2212', '\u00D7', '\u00F7')

/**
 * Renders [text] with its operator glyphs in [operatorColor], leaving digits
 * and brackets in the field's base ink.
 */
internal fun buildExpressionWithAccentOperators(
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
internal fun NormalCalculatorDisplay(
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

    AppLocaleHelper.appliedLanguageTags
    val languageTag = AppLocaleHelper.getCurrentLanguageTag()
    val rawExprText = expression ?: resultValue
    val asciiExpressionText = when {
        rawExprText.isBlank() || rawExprText == "0" -> "0"
        rawExprText.startsWith("=") -> rawExprText.removePrefix("=").trim()
        else -> rawExprText
    }
    val expressionText = localizeWesternDigits(asciiExpressionText, languageTag)
    val localizedResult = localizeWesternDigits(resultValue, languageTag)
    val showEqualsOnResult = resultValue.isNotBlank() &&
        asciiExpressionText != "0" &&
        expressionText.isNotBlank()
    val resultText = if (showEqualsOnResult) "= $localizedResult" else localizedResult

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
internal fun CalculatorKeyButton(
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

