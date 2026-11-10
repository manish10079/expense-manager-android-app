package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Tags
import com.composables.icons.lucide.Wallet
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.RotateCw
import com.composables.icons.lucide.ArrowDown
import com.composables.icons.lucide.ArrowUp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.cloud.AiParseContract
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.paymentColor
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.models.CurrencyPosition
import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatNumberValue
import com.mknlabs.expensetracker.utils.getCategoryIcon
import com.mknlabs.expensetracker.utils.getCurrency
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.delay

internal enum class ReviewPicker { Type, Category, Payment, Fund, Tags, Date, Time }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParsedDetailCard(
    item: AiParsedReviewItem,
    dark: Boolean,
    currencyId: Int,
    showBackToList: Boolean,
    onBackToList: () -> Unit,
    onRetry: (() -> Unit)?,
    onItemChange: (AiParsedReviewItem) -> Unit,
    funds: List<Fund> = emptyList(),
    tags: List<Tag> = emptyList(),
    onAdd: () -> Unit,
    compactEmbed: Boolean = false,
) {
    var picker by remember { mutableStateOf<ReviewPicker?>(null) }
    val noteFocus = remember { FocusRequester() }
    val amountFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var amountDraft by remember(item.id) {
        mutableStateOf(reviewAmountDraft(item.amount))
    }

    Column(
        modifier = if (compactEmbed) {
            Modifier.padding(top = 8.dp)
        } else {
            Modifier.padding(horizontal = 17.dp, vertical = 20.dp)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ReviewCardCorner))
                .background(
                    Brush.linearGradient(
                        colors = if (dark) {
                            listOf(Color(0xFF4F46E5), Color(0xFF4338CA), Color(0xFF5B21B6))
                        } else {
                            listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF6D28D9))
                        },
                    ),
                )
                .padding(vertical = 17.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.label_total_amount),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 14.08.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = Color(0xFFC7D2FE),
                ),
            )
            val currency = getCurrency(currencyId)
            val amountFigureStyle = MaterialTheme.typography.headlineSmall.copy(
                fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.173f,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            val amountSymbolStyle = amountFigureStyle.copy(
                fontSize = amountFigureStyle.fontSize * 0.55f,
                color = Color(0xFFC7D2FE),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        amountFocus.requestFocus()
                        keyboard?.show()
                    },
            ) {
                if (currency.position == CurrencyPosition.PREFIX) {
                    Text(text = currency.currencySymbol, style = amountSymbolStyle)
                }
                BasicTextField(
                    value = amountDraft,
                    onValueChange = { raw ->
                        val filtered = filterReviewAmountInput(raw)
                        amountDraft = filtered
                        parseReviewAmount(filtered)?.let { major ->
                            onItemChange(
                                item.copy(
                                    amount = major,
                                    amountMinor = major.toMinorUnits(),
                                )
                            )
                        }
                    },
                    textStyle = amountFigureStyle,
                    cursorBrush = SolidColor(Color.White),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .focusRequester(amountFocus)
                        .onFocusChanged { focus ->
                            if (!focus.isFocused) {
                                parseReviewAmount(amountDraft)?.let {
                                    amountDraft = reviewAmountDraft(it)
                                } ?: run {
                                    amountDraft = reviewAmountDraft(item.amount)
                                }
                            }
                        },
                )
                if (currency.position == CurrencyPosition.POSTFIX) {
                    Text(text = currency.currencySymbol, style = amountSymbolStyle)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetaRow(
                dark = dark,
                icon = if (item.typeId == AiParseContract.TYPE_INCOME) Lucide.ArrowUp else Lucide.ArrowDown,
                iconTint = if (item.typeId == AiParseContract.TYPE_INCOME) {
                    MaterialTheme.colorScheme.income
                } else {
                    MaterialTheme.colorScheme.expense
                },
                label = stringResource(R.string.label_category_type_section),
                value = stringResource(
                    if (item.typeId == AiParseContract.TYPE_INCOME) {
                        R.string.label_income_title
                    } else {
                        R.string.label_expense_title
                    },
                ),
                valueColor = if (item.typeId == AiParseContract.TYPE_INCOME) {
                    MaterialTheme.colorScheme.income
                } else {
                    MaterialTheme.colorScheme.expense
                },
                onClick = { picker = ReviewPicker.Type },
            )
            MetaRow(
                dark = dark,
                icon = reviewCategoryIcon(item),
                iconTint = MaterialTheme.colorScheme.categoryColor(reviewCategoryId(item)),
                label = stringResource(R.string.label_category),
                value = item.categoryName.ifBlank { "—" },
                onClick = { picker = ReviewPicker.Category },
            )
            MetaRow(
                dark = dark,
                icon = reviewPaymentIcon(item),
                iconTint = MaterialTheme.colorScheme.paymentColor(reviewPaymentId(item)),
                label = stringResource(R.string.label_payment_mode),
                value = item.paymentName.ifBlank { "—" },
                showDot = true,
                onClick = { picker = ReviewPicker.Payment },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaStackCard(
                    dark = dark,
                    icon = Lucide.Wallet,
                    iconTint = MaterialTheme.colorScheme.accentInk,
                    label = stringResource(R.string.title_funds),
                    value = item.fundName.ifBlank { "—" },
                    modifier = Modifier.weight(1f),
                    onClick = { picker = ReviewPicker.Fund },
                )
                MetaStackCard(
                    dark = dark,
                    icon = Lucide.Tags,
                    iconTint = MaterialTheme.colorScheme.accentInk,
                    label = stringResource(R.string.title_tags),
                    value = tags.filter { it.id in item.tagIds }.joinToString { it.name }.ifBlank { "—" },
                    modifier = Modifier.weight(1f),
                    onClick = { picker = ReviewPicker.Tags },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaStackCard(
                    dark = dark,
                    icon = Lucide.Calendar,
                    iconTint = MaterialTheme.colorScheme.accentInk,
                    label = stringResource(R.string.label_date),
                    value = formatReviewDate(item.createdAt),
                    modifier = Modifier.weight(1f),
                    onClick = { picker = ReviewPicker.Date },
                )
                MetaStackCard(
                    dark = dark,
                    icon = Lucide.Clock,
                    iconTint = MaterialTheme.colorScheme.accentInk,
                    label = stringResource(R.string.label_time),
                    value = formatReviewTime(item.createdAt),
                    modifier = Modifier.weight(1f),
                    onClick = { picker = ReviewPicker.Time },
                )
            }
        }

        Spacer(Modifier.height(7.dp))
        Column(
            modifier = reviewMetaCardModifier(
                dark,
                Modifier
                    .fillMaxWidth()
                    .reviewClickable {
                        noteFocus.requestFocus()
                        keyboard?.show()
                    },
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Lucide.FileText,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.accentInk,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.label_note),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.173f,
                        color = if (dark) ReviewDarkMuted else ReviewLightMuted,
                    ),
                )
            }
            Spacer(Modifier.height(6.dp))
            val noteStyle = MaterialTheme.typography.labelMedium.copy(
                fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.173f,
                fontWeight = FontWeight.SemiBold,
                color = if (dark) ReviewDarkTitle else ReviewLightTitle,
            )
            val twoLineHeight = with(LocalDensity.current) {
                (noteStyle.fontSize * 2.4f).toDp()
            }
            BasicTextField(
                value = item.note,
                onValueChange = { onItemChange(item.copy(note = it)) },
                textStyle = noteStyle,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.accentInk),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = twoLineHeight)
                    .verticalScroll(rememberScrollState())
                    .focusRequester(noteFocus),
                decorationBox = { inner ->
                    if (item.note.isBlank()) {
                        Text("—", style = noteStyle)
                    }
                    inner()
                },
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!compactEmbed && showBackToList) {
                ReviewGhostButton(
                    text = stringResource(R.string.label_cancel),
                    dark = dark,
                    modifier = Modifier.weight(1f),
                    onClick = onBackToList,
                )
            }
            if (!compactEmbed && onRetry != null) {
                ReviewGhostButton(
                    text = stringResource(R.string.label_retry),
                    dark = dark,
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Lucide.RotateCw, null, Modifier.size(12.dp)) },
                    onClick = onRetry,
                )
            }
            Button(
                onClick = onAdd,
                enabled = item.categoryId > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(ReviewCardCorner),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onCta,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(ReviewCardCorner))
                        .background(ReviewBrandGradient)
                        .padding(horizontal = 8.dp, vertical = 14.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Lucide.Plus, null, Modifier.size(12.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.label_add_action),
                            color = Color.White,
                            fontSize = 14.08.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
        when (picker) {
            ReviewPicker.Type -> ReviewChoiceSheet(
                title = stringResource(R.string.label_category_type_section),
                dark = dark,
                onDismiss = { picker = null },
            ) {
                items(
                    listOf(AiParseContract.TYPE_EXPENSE, AiParseContract.TYPE_INCOME),
                    key = { it },
                ) { typeId ->
                    val income = typeId == AiParseContract.TYPE_INCOME
                    ReviewChoiceRow(
                        dark = dark,
                        icon = if (income) Lucide.ArrowUp else Lucide.ArrowDown,
                        iconTint = if (income) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
                        label = stringResource(
                            if (income) R.string.label_income_title else R.string.label_expense_title,
                        ),
                        selected = typeId == item.typeId,
                        onClick = {
                            val cat = categoryMap.values.firstOrNull {
                                !it.isDeleted && it.transactionTypeId == typeId
                            }
                            onItemChange(
                                item.copy(
                                    typeId = typeId,
                                    categoryId = cat?.id ?: item.categoryId,
                                    categoryName = cat?.name ?: item.categoryName,
                                ),
                            )
                            picker = null
                        },
                    )
                }
            }
            ReviewPicker.Category -> ReviewChoiceSheet(
                title = stringResource(R.string.label_select_category),
                dark = dark,
                onDismiss = { picker = null },
            ) {
                val cats = categoryMap.values.filter {
                    !it.isDeleted && it.transactionTypeId == item.typeId
                }.ifEmpty { categoryMap.values.filter { !it.isDeleted } }
                items(cats, key = { it.id }) { cat ->
                    ReviewChoiceRow(
                        dark = dark,
                        icon = cat.icon,
                        iconTint = MaterialTheme.colorScheme.categoryColor(cat.id),
                        label = cat.name,
                        selected = cat.id == item.categoryId,
                        onClick = {
                            onItemChange(item.copy(categoryId = cat.id, categoryName = cat.name))
                            picker = null
                        },
                    )
                }
            }
            ReviewPicker.Payment -> ReviewChoiceSheet(
                title = stringResource(R.string.label_payment_type_item),
                dark = dark,
                onDismiss = { picker = null },
            ) {
                items(paymentTypeMap.values.toList(), key = { it.id }) { pay ->
                    ReviewChoiceRow(
                        dark = dark,
                        icon = pay.icon,
                        iconTint = MaterialTheme.colorScheme.paymentColor(pay.id),
                        label = pay.name,
                        selected = pay.id == item.paymentTypeId,
                        onClick = {
                            onItemChange(item.copy(paymentTypeId = pay.id, paymentName = pay.name))
                            picker = null
                        },
                    )
                }
            }
            ReviewPicker.Fund -> ReviewChoiceSheet(
                title = stringResource(R.string.title_funds),
                dark = dark,
                onDismiss = { picker = null },
            ) {
                item(key = "none") {
                    ReviewChoiceRow(
                        dark = dark,
                        icon = Lucide.Wallet,
                        iconTint = MaterialTheme.colorScheme.accentInk,
                        label = "—",
                        selected = item.fundId == null,
                        onClick = {
                            onItemChange(item.copy(fundId = null, fundName = ""))
                            picker = null
                        },
                    )
                }
                items(funds.filter { !it.isDeleted && !it.isArchived }, key = { it.id }) { fund ->
                    ReviewChoiceRow(
                        dark = dark,
                        icon = fund.icon,
                        iconTint = MaterialTheme.colorScheme.accentInk,
                        label = fund.name,
                        selected = fund.id == item.fundId,
                        onClick = {
                            onItemChange(item.copy(fundId = fund.id, fundName = fund.name))
                            picker = null
                        },
                    )
                }
            }
            ReviewPicker.Tags -> ReviewChoiceSheet(
                title = stringResource(R.string.title_tags),
                dark = dark,
                onDismiss = { picker = null },
            ) {
                items(tags.filter { !it.isDeleted }, key = { it.id }) { tag ->
                    ReviewChoiceRow(
                        dark = dark,
                        icon = Lucide.Tags,
                        iconTint = MaterialTheme.colorScheme.accentInk,
                        label = tag.name,
                        selected = tag.id in item.tagIds,
                        onClick = {
                            val next = if (tag.id in item.tagIds) item.tagIds - tag.id else item.tagIds + tag.id
                            onItemChange(item.copy(tagIds = next))
                        },
                    )
                }
            }
            ReviewPicker.Date -> WheelDateTimePickerModal(
                mode = WheelPickerMode.SINGLE_DATE,
                initialStartMillis = item.createdAt ?: System.currentTimeMillis(),
                onDismissRequest = { picker = null },
                onConfirm = { start, _ ->
                    onItemChange(item.copy(createdAt = mergeReviewDate(item.createdAt, start)))
                    picker = null
                },
            )
            ReviewPicker.Time -> WheelDateTimePickerModal(
                mode = WheelPickerMode.SINGLE_TIME,
                initialStartMillis = item.createdAt ?: System.currentTimeMillis(),
                onDismissRequest = { picker = null },
                onConfirm = { start, _ ->
                    onItemChange(item.copy(createdAt = mergeReviewTime(item.createdAt, start)))
                    picker = null
                },
            )
            null -> Unit
        }
    }
}

@Composable
internal fun ReviewGhostButton(
    text: String,
    dark: Boolean,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = reviewMetaCardModifier(dark, modifier).reviewClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text,
                fontSize = 12.90.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                color = if (dark) ReviewDarkMuted else Color(0xFF334155),
            )
        }
    }
}


@Composable
internal fun Modifier.reviewClickable(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )

@Composable
internal fun reviewMetaCardModifier(dark: Boolean, modifier: Modifier = Modifier): Modifier {
    val shape = RoundedCornerShape(ReviewCardCorner)
    return modifier
        .clip(shape)
        .background(if (dark) Color(0x661E293B) else Color(0xFFF8FAFC))
        .border(1.dp, if (dark) Color(0xCC1E293B) else Color(0xFFF1F5F9), shape)
        .padding(horizontal = 12.dp, vertical = 15.dp)
}

@Composable
internal fun MetaRow(
    dark: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    pill: Boolean = false,
    showDot: Boolean = false,
    iconTint: Color? = null,
    valueColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = reviewMetaCardModifier(
            dark,
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.reviewClickable(onClick = onClick) else Modifier),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = iconTint ?: if (dark) ReviewDarkFaint else ReviewLightFaint,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.173f,
                        color = if (dark) ReviewDarkMuted else ReviewLightMuted,
                    ),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDot) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ReviewBrandStart),
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = value,
                    modifier = if (pill) {
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (dark) ReviewDarkRow else ReviewLightRow)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    } else {
                        Modifier
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.173f,
                        fontWeight = FontWeight.SemiBold,
                        color = valueColor ?: if (dark) ReviewDarkTitle else ReviewLightTitle,
                    ),
                )
                if (onClick != null) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Lucide.ChevronDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (dark) ReviewDarkMuted else ReviewLightMuted,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MetaStackCard(
    dark: Boolean,
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = reviewMetaCardModifier(
            dark,
            modifier.then(if (onClick != null) Modifier.reviewClickable(onClick = onClick) else Modifier),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = iconTint ?: if (dark) ReviewDarkFaint else ReviewLightFaint,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.173f,
                    color = if (dark) ReviewDarkMuted else ReviewLightMuted,
                ),
            )
            if (onClick != null) {
                Icon(
                    Lucide.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (dark) ReviewDarkMuted else ReviewLightMuted,
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.173f,
                fontWeight = FontWeight.SemiBold,
                color = if (dark) ReviewDarkTitle else ReviewLightTitle,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewChoiceSheet(
    title: String,
    dark: Boolean,
    onDismiss: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (dark) MaterialTheme.colorScheme.profileSheet else MaterialTheme.colorScheme.sheet,
        contentColor = if (dark) ReviewDarkTitle else ReviewLightTitle,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (dark) ReviewDarkTitle else ReviewLightTitle,
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content,
            )
        }
    }
}

@Composable
internal fun ReviewChoiceRow(
    dark: Boolean,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = reviewMetaCardModifier(
            dark,
            Modifier
                .fillMaxWidth()
                .reviewClickable(onClick = onClick),
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = iconTint)
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (dark) ReviewDarkTitle else ReviewLightTitle,
            ),
        )
    }
}

