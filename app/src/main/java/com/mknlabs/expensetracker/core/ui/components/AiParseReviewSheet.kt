package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Check
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

private val BrandStart = Color(0xFF6A4DFF)
private val BrandEnd = Color(0xFF6750A4)
private val BrandGradient = Brush.linearGradient(listOf(BrandStart, BrandEnd))
private val Emerald = Color(0xFF10B981)

private val LightCanvas = Color(0xFFF1F5F9)
private val LightBorder = Color(0xE6E2E8F0)
private val LightTitle = Color(0xFF1E293B)
private val LightMuted = Color(0xFF64748B)
private val LightFaint = Color(0xFF94A3B8)
private val LightPanel = Color(0xFFFFFFFF)
private val LightRow = Color(0xFFF1F5F9)

private val DarkCanvas = Color(0xFF09090B)
private val DarkBorder = Color(0xE627272A)
private val DarkTitle = Color(0xFFF4F4F5)
private val DarkMuted = Color(0xFFA1A1AA)
private val DarkFaint = Color(0xFF71717A)
private val DarkPanel = Color(0xE618181B)
private val DarkRow = Color(0xFF27272A)
private val ReviewCardCorner = 12.dp

@Composable
fun AiParseReviewSheet(
    items: List<AiParsedReviewItem>,
    onDismissRequest: () -> Unit,
    onAdd: (AiParsedReviewItem) -> Unit,
    onRetry: (() -> Unit)? = null,
    sourceLabel: String? = null,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    funds: List<Fund> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    if (items.isEmpty()) return
    val dark = MaterialTheme.colorScheme.isDark
    var selectedId by remember(items.map { it.id }) {
        mutableStateOf(if (items.size == 1) items.first().id else null)
    }
    var savedItem by remember { mutableStateOf<AiParsedReviewItem?>(null) }
    var drafts by remember(items.map { it.id }) { mutableStateOf(items) }
    val selected = drafts.firstOrNull { it.id == selectedId }
    fun updateDraft(updated: AiParsedReviewItem) {
        drafts = drafts.map { if (it.id == updated.id) updated else it }
    }

    if (savedItem == null) Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = if (dark) DarkCanvas else LightCanvas,
            border = BorderStroke(1.dp, if (dark) DarkBorder else LightBorder),
            shadowElevation = 20.dp,
        ) {
            when {
                selected == null -> BatchTransactionList(
                    items = drafts,
                    dark = dark,
                    currencyId = currencyId,
                    onSelect = { selectedId = it.id },
                    onRetry = onRetry,
                )
                else -> ParsedDetailCard(
                    item = selected,
                    dark = dark,
                    currencyId = currencyId,
                    showBackToList = items.size > 1,
                    onBackToList = { selectedId = null },
                    onRetry = onRetry,
                    onItemChange = { updateDraft(it) },
                    funds = funds,
                    tags = tags,
                    onAdd = {
                        savedItem = selected
                    },
                )
            }
        }
        }
    }

    val confirmation = savedItem
    if (confirmation != null) {
        TransactionSavedDialog(
            item = confirmation,
            currencyId = currencyId,
            onFinished = {
                onAdd(confirmation)
                savedItem = null
            },
        )
    }
}



@Composable
private fun BatchTransactionList(
    items: List<AiParsedReviewItem>,
    dark: Boolean,
    currencyId: Int,
    onSelect: (AiParsedReviewItem) -> Unit,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (dark) DarkCanvas else LightCanvas)
            .padding(horizontal = 17.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.label_ai_batch_transactions),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (dark) DarkTitle else LightTitle,
                    ),
                )
                Text(
                    text = stringResource(R.string.label_ai_items_extracted, items.size),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.35.sp,
                        color = if (dark) DarkMuted else LightMuted,
                    ),
                )
            }
            Text(
                text = stringResource(R.string.label_ai_batch_badge, items.size),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(BrandStart.copy(alpha = if (dark) 0.12f else 0.08f))
                    .border(
                        1.dp,
                        BrandStart.copy(alpha = if (dark) 0.25f else 0.22f),
                        RoundedCornerShape(50),
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (dark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                ),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                BatchRow(item = item, dark = dark, currencyId = currencyId, onClick = { onSelect(item) })
            }
        }
        if (onRetry != null) {
            Spacer(Modifier.height(10.dp))
            ReviewGhostButton(
                text = stringResource(R.string.label_retry),
                dark = dark,
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Lucide.RotateCw, null, Modifier.size(12.dp)) },
                onClick = onRetry,
            )
        }
    }
}

@Composable
private fun BatchRow(
    item: AiParsedReviewItem,
    dark: Boolean,
    currencyId: Int,
    onClick: () -> Unit,
) {
    val expense = item.typeId != AiParseContract.TYPE_INCOME
    val amount = formatReviewAmount(item.amount, currencyId)
    Row(
        modifier = reviewMetaCardModifier(
            dark,
            Modifier
                .fillMaxWidth()
                .reviewClickable(onClick = onClick),
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val catId = reviewCategoryId(item)
                val catColor = MaterialTheme.colorScheme.categoryColor(catId)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.categorySoft(catColor)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        reviewCategoryIcon(item),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = catColor,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = merchantOf(item),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (dark) DarkTitle else LightTitle,
                        ),
                    )
                    Text(
                        text = formatReviewDate(item.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            color = if (dark) DarkFaint else LightFaint,
                        ),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (!expense) {
                            MaterialTheme.colorScheme.income
                        } else if (dark) DarkTitle else LightTitle,
                    ),
                )
                Text(
                    text = stringResource(
                        if (expense) R.string.label_expense_title else R.string.label_income_title,
                    ).uppercase(),
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (expense) {
                                MaterialTheme.colorScheme.expense.copy(alpha = if (dark) 0.12f else 0.08f)
                            } else {
                                MaterialTheme.colorScheme.income.copy(alpha = if (dark) 0.12f else 0.08f)
                            },
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.65.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (expense) {
                            MaterialTheme.colorScheme.expense
                        } else {
                            MaterialTheme.colorScheme.income
                        },
                    ),
                )
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParsedDetailCard(
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
) {
    var picker by remember { mutableStateOf<ReviewPicker?>(null) }
    val noteFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.padding(horizontal = 17.dp, vertical = 20.dp)) {
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
            val amountNumber = formatNumberValue(kotlin.math.abs(item.amount))
            val amountFigureStyle = MaterialTheme.typography.headlineSmall.copy(
                fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.173f,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            val amountSymbolStyle = amountFigureStyle.copy(
                fontSize = amountFigureStyle.fontSize * 0.55f,
                color = Color(0xFFC7D2FE),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currency.position == CurrencyPosition.PREFIX) {
                    Text(text = currency.currencySymbol, style = amountSymbolStyle)
                }
                Text(text = amountNumber, style = amountFigureStyle)
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
                        color = if (dark) DarkMuted else LightMuted,
                    ),
                )
            }
            Spacer(Modifier.height(6.dp))
            val noteStyle = MaterialTheme.typography.labelMedium.copy(
                fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.173f,
                fontWeight = FontWeight.SemiBold,
                color = if (dark) DarkTitle else LightTitle,
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
            if (showBackToList) {
                ReviewGhostButton(
                    text = stringResource(R.string.label_cancel),
                    dark = dark,
                    modifier = Modifier.weight(1f),
                    onClick = onBackToList,
                )
            }
            if (onRetry != null) {
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
                        .background(BrandGradient)
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
private fun ReviewGhostButton(
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
                color = if (dark) DarkMuted else Color(0xFF334155),
            )
        }
    }
}


@Composable
private fun Modifier.reviewClickable(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )

@Composable
private fun reviewMetaCardModifier(dark: Boolean, modifier: Modifier = Modifier): Modifier {
    val shape = RoundedCornerShape(ReviewCardCorner)
    return modifier
        .clip(shape)
        .background(if (dark) Color(0x661E293B) else Color(0xFFF8FAFC))
        .border(1.dp, if (dark) Color(0xCC1E293B) else Color(0xFFF1F5F9), shape)
        .padding(horizontal = 12.dp, vertical = 15.dp)
}

@Composable
private fun MetaRow(
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
                    tint = iconTint ?: if (dark) DarkFaint else LightFaint,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.173f,
                        color = if (dark) DarkMuted else LightMuted,
                    ),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDot) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BrandStart),
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = value,
                    modifier = if (pill) {
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (dark) DarkRow else LightRow)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    } else {
                        Modifier
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = MaterialTheme.typography.labelMedium.fontSize * 1.173f,
                        fontWeight = FontWeight.SemiBold,
                        color = valueColor ?: if (dark) DarkTitle else LightTitle,
                    ),
                )
                if (onClick != null) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Lucide.ChevronDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (dark) DarkMuted else LightMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaStackCard(
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
                tint = iconTint ?: if (dark) DarkFaint else LightFaint,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * 1.173f,
                    color = if (dark) DarkMuted else LightMuted,
                ),
            )
            if (onClick != null) {
                Icon(
                    Lucide.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (dark) DarkMuted else LightMuted,
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
                color = if (dark) DarkTitle else LightTitle,
            ),
        )
    }
}

@Composable
fun TransactionSavedDialog(
    item: AiParsedReviewItem,
    currencyId: Int,
    onFinished: () -> Unit,
) {
    LaunchedEffect(item.id) {
        delay(1400)
        onFinished()
    }
    Dialog(onDismissRequest = onFinished) {
        TransactionSavedCard(
            item = item,
            currencyId = currencyId,
            dark = MaterialTheme.colorScheme.isDark,
        )
    }
}

@Composable
private fun TransactionSavedCard(
    item: AiParsedReviewItem,
    currencyId: Int,
    dark: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (dark) DarkCanvas else LightCanvas,
        border = BorderStroke(1.dp, if (dark) DarkBorder else LightBorder),
        shadowElevation = 24.dp,
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.5.dp, Emerald, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Lucide.Check, null, tint = Emerald, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.label_ai_transaction_saved),
                color = if (dark) DarkTitle else LightTitle,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.6.sp,
            )
            Text(
                merchantOf(item),
                color = if (dark) DarkMuted else LightMuted,
                fontSize = 10.2.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                formatReviewAmount(item.amount, currencyId),
                color = Emerald,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(if (dark) DarkBorder else LightBorder),
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(Emerald))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.msg_ai_added_to_budget),
                    color = if (dark) DarkFaint else LightFaint,
                    fontSize = 9.35.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun reviewPaymentId(item: AiParsedReviewItem): Int {
    if (item.paymentTypeId > 0) return item.paymentTypeId
    val name = item.paymentName.trim()
    if (name.isEmpty()) return 0
    return paymentTypeMap.values.firstOrNull { it.name.equals(name, ignoreCase = true) }?.id ?: 0
}

private fun reviewPaymentIcon(item: AiParsedReviewItem): ImageVector =
    paymentTypeMap[reviewPaymentId(item)]?.icon ?: getCategoryIcon(0)

private fun reviewCategoryId(item: AiParsedReviewItem): Int {
    if (item.categoryId > 0) return item.categoryId
    val name = item.categoryName.trim()
    if (name.isEmpty()) return 0
    return categoryMap.values.firstOrNull { it.name.equals(name, ignoreCase = true) }?.id ?: 0
}

private fun reviewCategoryIcon(item: AiParsedReviewItem): ImageVector =
    getCategoryIcon(reviewCategoryId(item))


private enum class ReviewPicker { Type, Category, Payment, Fund, Tags, Date, Time }

private fun mergeReviewDate(base: Long?, picked: Long): Long {
    val dest = Calendar.getInstance().apply { timeInMillis = base ?: System.currentTimeMillis() }
    val src = Calendar.getInstance().apply { timeInMillis = picked }
    dest.set(Calendar.YEAR, src.get(Calendar.YEAR))
    dest.set(Calendar.MONTH, src.get(Calendar.MONTH))
    dest.set(Calendar.DAY_OF_MONTH, src.get(Calendar.DAY_OF_MONTH))
    return dest.timeInMillis
}

private fun mergeReviewTime(base: Long?, picked: Long): Long {
    val dest = Calendar.getInstance().apply { timeInMillis = base ?: System.currentTimeMillis() }
    val src = Calendar.getInstance().apply { timeInMillis = picked }
    dest.set(Calendar.HOUR_OF_DAY, src.get(Calendar.HOUR_OF_DAY))
    dest.set(Calendar.MINUTE, src.get(Calendar.MINUTE))
    dest.set(Calendar.SECOND, 0)
    dest.set(Calendar.MILLISECOND, 0)
    return dest.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewChoiceSheet(
    title: String,
    dark: Boolean,
    onDismiss: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (dark) MaterialTheme.colorScheme.profileSheet else MaterialTheme.colorScheme.sheet,
        contentColor = if (dark) DarkTitle else LightTitle,
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
                color = if (dark) DarkTitle else LightTitle,
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
private fun ReviewChoiceRow(
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
                color = if (dark) DarkTitle else LightTitle,
            ),
        )
    }
}

private fun merchantOf(item: AiParsedReviewItem): String =
    item.note.ifBlank { item.categoryName }.ifBlank { "—" }

private fun formatReviewAmount(amount: Double, currencyId: Int): String =
    formatCurrencyValue(amount = amount, currencyId = currencyId)

private fun formatReviewDate(createdAt: Long?): String {
    if (createdAt == null) return "—"
    return DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(createdAt))
}

private fun formatReviewTime(createdAt: Long?): String {
    if (createdAt == null) return "—"
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(createdAt))
}

private fun previewReviewItem(
    id: String = "preview-1",
    note: String = "Coffee",
    amount: Double = 249.5,
    categoryName: String = "Food",
): AiParsedReviewItem = AiParsedReviewItem(
    id = id,
    amount = amount,
    amountMinor = 24950,
    categoryName = categoryName,
    categoryId = 1,
    paymentName = "UPI",
    paymentTypeId = 1,
    note = note,
    typeId = AiParseContract.TYPE_EXPENSE,
    createdAt = 1_704_067_200_000L,
    fundId = null,
    fundName = "",
    tagIds = emptyList(),
)

@Preview(name = "Parsed card - Light", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun PreviewParsedDetailCardLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = LightCanvas,
            border = BorderStroke(1.dp, LightBorder),
        ) {
            ParsedDetailCard(
                item = previewReviewItem(),
                dark = false,
                currencyId = DEFAULT_CURRENCY_ID,
                showBackToList = false,
                onBackToList = {},
                onRetry = {},
                onItemChange = {},
                onAdd = {},
            )
        }
    }
}

@Preview(name = "Parsed card - Dark", showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun PreviewParsedDetailCardDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = DarkCanvas,
            border = BorderStroke(1.dp, DarkBorder),
        ) {
            ParsedDetailCard(
                item = previewReviewItem(),
                dark = true,
                currencyId = DEFAULT_CURRENCY_ID,
                showBackToList = false,
                onBackToList = {},
                onRetry = {},
                onItemChange = {},
                onAdd = {},
            )
        }
    }
}

@Preview(name = "Batch list - Light", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun PreviewBatchTransactionListLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = LightCanvas,
            border = BorderStroke(1.dp, LightBorder),
        ) {
            BatchTransactionList(
                items = listOf(
                    previewReviewItem(),
                    previewReviewItem(id = "preview-2", note = "Metro", amount = 40.0, categoryName = "Transport"),
                ),
                dark = false,
                currencyId = DEFAULT_CURRENCY_ID,
                onSelect = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Batch list - Dark", showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun PreviewBatchTransactionListDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = DarkCanvas,
            border = BorderStroke(1.dp, DarkBorder),
        ) {
            BatchTransactionList(
                items = listOf(
                    previewReviewItem(),
                    previewReviewItem(id = "preview-2", note = "Metro", amount = 40.0, categoryName = "Transport"),
                ),
                dark = true,
                currencyId = DEFAULT_CURRENCY_ID,
                onSelect = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Saved dialog - Light", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
private fun PreviewTransactionSavedCardLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            TransactionSavedCard(
                item = previewReviewItem(),
                currencyId = DEFAULT_CURRENCY_ID,
                dark = false,
            )
        }
    }
}

@Preview(name = "Saved dialog - Dark", showBackground = true, backgroundColor = 0xFF09090B)
@Composable
private fun PreviewTransactionSavedCardDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            TransactionSavedCard(
                item = previewReviewItem(),
                currencyId = DEFAULT_CURRENCY_ID,
                dark = true,
            )
        }
    }
}
