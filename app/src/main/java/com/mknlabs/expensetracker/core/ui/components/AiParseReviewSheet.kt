package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.RotateCw
import com.composables.icons.lucide.ShoppingBag
import com.composables.icons.lucide.Tag
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.cloud.AiParseContract
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.math.BigDecimal
import java.text.DateFormat
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

@Composable
fun AiParseReviewSheet(
    items: List<AiParsedReviewItem>,
    onDismissRequest: () -> Unit,
    onAdd: (AiParsedReviewItem) -> Unit,
    onEdit: (AiParsedReviewItem) -> Unit,
    onRetry: (() -> Unit)? = null,
    sourceLabel: String? = null,
) {
    if (items.isEmpty()) return
    val dark = MaterialTheme.colorScheme.isDark
    var selectedId by remember(items.map { it.id }) {
        mutableStateOf(if (items.size == 1) items.first().id else null)
    }
    var editing by remember { mutableStateOf(false) }
    var savedItem by remember { mutableStateOf<AiParsedReviewItem?>(null) }
    val selected = items.firstOrNull { it.id == selectedId }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (dark) DarkCanvas else LightCanvas,
            border = BorderStroke(1.dp, if (dark) DarkBorder else LightBorder),
            shadowElevation = 20.dp,
        ) {
            when {
                selected == null -> BatchTransactionList(
                    items = items,
                    dark = dark,
                    onSelect = { selectedId = it.id },
                )
                editing -> ParsedEditOverlay(
                    item = selected,
                    dark = dark,
                    onCancel = { editing = false },
                    onSave = { updated ->
                        onEdit(updated)
                        editing = false
                    },
                )
                else -> ParsedDetailCard(
                    item = selected,
                    dark = dark,
                    sourceLabel = sourceLabel,
                    showBackToList = items.size > 1,
                    onBackToList = { selectedId = null },
                    onRetry = onRetry,
                    onEdit = { editing = true },
                    onAdd = {
                        savedItem = selected
                    },
                )
            }
        }
    }

    val confirmation = savedItem
    if (confirmation != null) {
        TransactionSavedDialog(
            item = confirmation,
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
    onSelect: (AiParsedReviewItem) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
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
                        fontSize = 11.sp,
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
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (dark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                ),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                BatchRow(item = item, dark = dark, onClick = { onSelect(item) })
            }
        }
    }
}

@Composable
private fun BatchRow(
    item: AiParsedReviewItem,
    dark: Boolean,
    onClick: () -> Unit,
) {
    val expense = item.typeId != AiParseContract.TYPE_INCOME
    val amount = formatReviewAmount(item.amount)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (dark) DarkPanel else LightPanel,
        border = BorderStroke(1.dp, if (dark) DarkBorder else LightBorder),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (dark) DarkRow else LightRow),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Lucide.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (dark) DarkMuted else LightMuted,
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
                            fontSize = 10.sp,
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
                        fontSize = 9.sp,
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
}

@Composable
private fun ParsedDetailCard(
    item: AiParsedReviewItem,
    dark: Boolean,
    sourceLabel: String?,
    showBackToList: Boolean,
    onBackToList: () -> Unit,
    onRetry: (() -> Unit)?,
    onEdit: () -> Unit,
    onAdd: () -> Unit,
) {
    val expense = item.typeId != AiParseContract.TYPE_INCOME
    val pulse = rememberInfiniteTransition(label = "badge")
    val badgeScale by pulse.animateFloat(
        1f,
        0.98f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "badge-scale",
    )
    Column(modifier = Modifier.padding(20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (dark) DarkRow else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Lucide.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (dark) DarkMuted else Color(0xFF334155),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = merchantOf(item),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (dark) DarkTitle else LightTitle,
                        ),
                    )
                    if (!sourceLabel.isNullOrBlank()) {
                        Text(
                            text = sourceLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = if (dark) DarkFaint else LightFaint,
                            ),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .scale(badgeScale)
                    .clip(RoundedCornerShape(50))
                    .background(
                        MaterialTheme.colorScheme.let {
                            if (expense) it.expense.copy(alpha = if (dark) 0.12f else 0.08f)
                            else it.income.copy(alpha = if (dark) 0.12f else 0.08f)
                        },
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.let {
                            if (expense) it.expense.copy(alpha = 0.25f) else it.income.copy(alpha = 0.25f)
                        },
                        RoundedCornerShape(50),
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (expense) MaterialTheme.colorScheme.expense
                            else MaterialTheme.colorScheme.income,
                        ),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        if (expense) R.string.label_expense_title else R.string.label_income_title,
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (expense) MaterialTheme.colorScheme.expense
                        else MaterialTheme.colorScheme.income,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (dark) DarkPanel else Color(0xD9FFFFFF))
                .border(1.dp, if (dark) DarkBorder else LightBorder, RoundedCornerShape(12.dp))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.label_total_amount),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = if (dark) DarkFaint else LightFaint,
                ),
            )
            Text(
                text = formatReviewAmount(item.amount),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (dark) DarkTitle else Color(0xFF0F172A),
                ),
            )
        }

        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (dark) DarkPanel else LightPanel)
                .border(1.dp, if (dark) DarkBorder else LightBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MetaRow(
                dark = dark,
                icon = Lucide.Tag,
                label = stringResource(R.string.label_category),
                value = item.categoryName.ifBlank { "—" },
                pill = true,
            )
            MetaRow(
                dark = dark,
                icon = Lucide.CreditCard,
                label = stringResource(R.string.label_payment_mode),
                value = item.paymentName.ifBlank { "—" },
                showDot = true,
            )
            MetaRow(
                dark = dark,
                icon = Lucide.Calendar,
                label = stringResource(R.string.label_date),
                value = formatReviewDate(item.createdAt),
            )
            MetaRow(
                dark = dark,
                icon = Lucide.Clock,
                label = stringResource(R.string.label_time),
                value = formatReviewTime(item.createdAt),
                showDivider = false,
            )
        }

        Spacer(Modifier.height(12.dp))
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
            ReviewGhostButton(
                text = stringResource(R.string.label_edit),
                dark = dark,
                modifier = Modifier.weight(1f),
                icon = { Icon(Lucide.Pencil, null, Modifier.size(12.dp)) },
                onClick = onEdit,
            )
            Button(
                onClick = onAdd,
                enabled = item.categoryId > 0,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onCta,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandGradient)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Lucide.Plus, null, Modifier.size(12.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.label_add_action),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
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
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (dark) DarkBorder else Color(0xCCCBD5E1)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (dark) DarkMuted else Color(0xFF334155),
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp, horizontal = 6.dp),
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(4.dp))
        }
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun MetaRow(
    dark: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    pill: Boolean = false,
    showDot: Boolean = false,
    showDivider: Boolean = true,
) {
    Column {
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
                    tint = if (dark) DarkFaint else LightFaint,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(
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
                        fontWeight = FontWeight.SemiBold,
                        color = if (dark) DarkTitle else LightTitle,
                    ),
                )
            }
        }
        if (showDivider) {
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(if (dark) DarkBorder else Color(0xFFF1F5F9)),
            )
        }
    }
}

@Composable
private fun ParsedEditOverlay(
    item: AiParsedReviewItem,
    dark: Boolean,
    onCancel: () -> Unit,
    onSave: (AiParsedReviewItem) -> Unit,
) {
    var merchant by remember(item.id) { mutableStateOf(merchantOf(item)) }
    var amount by remember(item.id) {
        mutableStateOf(BigDecimal.valueOf(item.amount).stripTrailingZeros().toPlainString())
    }
    var category by remember(item.id) { mutableStateOf(item.categoryName) }
    var payment by remember(item.id) { mutableStateOf(item.paymentName) }
    Column(modifier = Modifier.padding(20.dp)) {
        Text(
            text = stringResource(R.string.label_ai_edit_details),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (dark) Color(0xFF818CF8) else Color(0xFF4F46E5),
            ),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = merchant,
            onValueChange = { merchant = it },
            label = { Text(stringResource(R.string.label_merchant)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text(stringResource(R.string.label_amount)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text(stringResource(R.string.label_category)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = payment,
            onValueChange = { payment = it },
            label = { Text(stringResource(R.string.label_payment_mode)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.label_cancel))
            }
            Button(
                onClick = {
                    val parsed = amount.toDoubleOrNull() ?: return@Button
                    onSave(
                        item.copy(
                            amount = parsed,
                            amountMinor = parsed.toMinorUnits(),
                            note = merchant,
                            categoryName = category,
                            paymentName = payment,
                        ),
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.cta),
            ) {
                Text(stringResource(R.string.label_ai_save_details), color = MaterialTheme.colorScheme.onCta)
            }
        }
    }
}

@Composable
private fun TransactionSavedDialog(
    item: AiParsedReviewItem,
    onFinished: () -> Unit,
) {
    LaunchedEffect(item.id) {
        delay(1400)
        onFinished()
    }
    Dialog(onDismissRequest = onFinished) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xE61E293B)),
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
                    color = Color(0xFFF1F5F9),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Text(
                    merchantOf(item),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    formatReviewAmount(item.amount),
                    color = Emerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E293B)))
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Emerald))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.msg_ai_added_to_budget),
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

private fun merchantOf(item: AiParsedReviewItem): String =
    item.note.ifBlank { item.categoryName }.ifBlank { "—" }

private fun formatReviewAmount(amount: Double): String =
    BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()

private fun formatReviewDate(createdAt: Long?): String {
    if (createdAt == null) return "—"
    return DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(createdAt))
}

private fun formatReviewTime(createdAt: Long?): String {
    if (createdAt == null) return "—"
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(createdAt))
}
