package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.launch

@Composable
internal fun IosNotificationEnter(
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val startPx = with(LocalDensity.current) { 80.dp.toPx() }
    val dropY = remember { Animatable(-startPx) }
    val fade = remember { Animatable(0f) }
    val scale = remember { Animatable(0.86f) }
    LaunchedEffect(Unit) {
        dropY.snapTo(-startPx)
        fade.snapTo(0f)
        scale.snapTo(0.86f)
        delay(delayMillis.toLong())
        launch { fade.animateTo(1f, tween(320)) }
        launch {
            scale.animateTo(
                1f,
                spring(
                    dampingRatio = 0.55f,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        dropY.animateTo(
            0f,
            spring(
                dampingRatio = 0.55f,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }
    Box(
        modifier = Modifier.graphicsLayer {
            translationY = dropY.value
            alpha = fade.value
            scaleX = scale.value
            scaleY = scale.value
        },
    ) {
        content()
    }
}

@Composable
internal fun BatchTransactionList(
    items: List<AiParsedReviewItem>,
    dark: Boolean,
    currencyId: Int,
    onSelect: (AiParsedReviewItem) -> Unit = {},
    onRetry: (() -> Unit)? = null,
    onItemChange: (AiParsedReviewItem) -> Unit = {},
    onDismiss: () -> Unit = {},
    onAddAll: () -> Unit = {},
    onAddItem: (AiParsedReviewItem) -> Unit = {},
    funds: List<Fund> = emptyList(),
    tags: List<Tag> = emptyList(),
    showBatchActions: Boolean = false,
) {
    var expandedId by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (dark) ReviewDarkCanvas else ReviewLightCanvas)
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
                        color = if (dark) ReviewDarkTitle else ReviewLightTitle,
                    ),
                )
                Text(
                    text = stringResource(R.string.label_ai_items_extracted, items.size),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.35.sp,
                        color = if (dark) ReviewDarkMuted else ReviewLightMuted,
                    ),
                )
            }
            Text(
                text = stringResource(R.string.label_ai_batch_badge, items.size),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ReviewBrandStart.copy(alpha = if (dark) 0.12f else 0.08f))
                    .border(
                        1.dp,
                        ReviewBrandStart.copy(alpha = if (dark) 0.25f else 0.22f),
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
        val listScroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .then(
                    if (items.size > 5) {
                        Modifier
                            .height(392.dp)
                            .verticalScroll(listScroll)
                    } else {
                        Modifier
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEachIndexed { index, item ->
                BatchRow(
                    item = item,
                    dark = dark,
                    currencyId = currencyId,
                    enterIndex = index,
                    expanded = expandedId == item.id,
                    onClick = {
                        expandedId = if (expandedId == item.id) null else item.id
                    },
                    onItemChange = onItemChange,
                    onAdd = { onAddItem(item) },
                    funds = funds,
                    tags = tags,
                )
            }
        }
        if (showBatchActions) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onRetry != null) {
                    ReviewGhostButton(
                        text = stringResource(R.string.label_retry),
                        dark = dark,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        icon = { Icon(Lucide.RotateCw, null, Modifier.size(12.dp)) },
                        onClick = onRetry,
                    )
                }
                val canAddAll = items.isNotEmpty() && items.all { it.categoryId > 0 }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(ReviewCardCorner))
                        .background(ReviewBrandGradient)
                        .then(
                            if (canAddAll) Modifier.reviewClickable(onClick = onAddAll)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.label_add_all),
                        color = Color.White.copy(alpha = if (canAddAll) 1f else 0.5f),
                        fontSize = 12.90.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        } else if (onRetry != null) {
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
internal fun BatchRow(
    item: AiParsedReviewItem,
    dark: Boolean,
    currencyId: Int,
    onClick: () -> Unit,
    enterIndex: Int = 0,
    expanded: Boolean = false,
    onItemChange: (AiParsedReviewItem) -> Unit = {},
    onAdd: () -> Unit = {},
    funds: List<Fund> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    val expense = item.typeId != AiParseContract.TYPE_INCOME
    val amount = formatReviewAmount(item.amount, currencyId)
    IosNotificationEnter(delayMillis = enterIndex * 140) {
    Column(
        modifier = reviewMetaCardModifier(dark, Modifier.fillMaxWidth()),
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .reviewClickable(onClick = onClick),
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
                            color = if (dark) ReviewDarkTitle else ReviewLightTitle,
                        ),
                    )
                    Text(
                        text = formatReviewDate(item.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.5.sp,
                            color = if (dark) ReviewDarkFaint else ReviewLightFaint,
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
                        } else if (dark) ReviewDarkTitle else ReviewLightTitle,
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
            Icon(
                Lucide.ChevronDown,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(16.dp)
                    .graphicsLayer { rotationZ = if (expanded) 180f else 0f },
                tint = if (dark) ReviewDarkMuted else ReviewLightMuted,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            ParsedDetailCard(
                item = item,
                dark = dark,
                currencyId = currencyId,
                showBackToList = false,
                onBackToList = {},
                onRetry = null,
                onItemChange = onItemChange,
                funds = funds,
                tags = tags,
                onAdd = onAdd,
                compactEmbed = true,
            )
        }
    }
    }
}
