package com.mknlabs.expensetracker.feature.budget.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppIconBox
import com.mknlabs.expensetracker.core.ui.components.AvatarRingBrush
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.categoryTint
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.switchOnThumb
import com.mknlabs.expensetracker.core.ui.theme.switchOnTick
import com.mknlabs.expensetracker.core.ui.theme.switchOnTrack
import com.mknlabs.expensetracker.core.ui.theme.track
import com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus
import com.mknlabs.expensetracker.models.InstallmentStatus
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.models.RecurringType
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.utils.UiText

/**
 * The budget card's three headline labels run 10% under their type-scale role: at full
 * size the category name, the amount pair and the SPENT / LIMIT caption crowd each other
 * on a narrow card.
 */
internal const val BUDGET_CARD_TEXT_SCALE = 0.9f

// Flat fill from the analytics Daily Avg / Savings cards: #13151F in dark, white in light.
internal val BudgetCardFillDark = Color(0xFF13151F)
internal val BudgetCardFillLight = Color(0xFFFFFFFF)

@Composable
internal fun CategoryBudgetCard(
    budget: BudgetCategoryBudgetUi,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    // The card's ink, through the scheme both times: the theme's own accent ink while the
    // budget holds, amber once it is past its limit. The rail below the header reads it as one
    // value, so its lit edge can never disagree with the state the card is in. The rail ramps
    // it, like every other bar in the app.
    val isOverLimit = budget.spentAmount >= budget.limitAmount
    val cardInk = if (isOverLimit) {
        MaterialTheme.colorScheme.expense
    } else {
        MaterialTheme.colorScheme.accentInk
    }
    // The avatar ring's indigo → violet → fuchsia ramp, the same in both themes.
    val progressAccent: Brush = AvatarRingBrush
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            // The recurring rule card's chrome: its fill and its single brand hairline,
            // the same in every state, so the two card types sitting in the same list read
            // as one family. Health is carried by the progress bar and the status labels
            // instead of by the container.
            .background(if (MaterialTheme.colorScheme.isDark) BudgetCardFillDark else BudgetCardFillLight)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = budget.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize * BUDGET_CARD_TEXT_SCALE
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.label_budget_categories_info),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(onClick = onInfoClick)
                    )
                }
                if (budget.remainingEdits != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (budget.remainingEdits == 0) stringResource(id = R.string.label_history_locked) else stringResource(id = R.string.label_edits_left_formatted, budget.remainingEdits),
                        color = if (budget.remainingEdits == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // The spent / limit pair prints in the title's own ink -- `onSurface`,
                // the token the category name above it reads -- so the two stay one pair
                // in safe, near limit and overspent alike. Health is left to the rail and
                // the labels beside it rather than recolouring the card's second line of
                // identity in all three states.
                Text(
                    text = budget.summaryLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize * BUDGET_CARD_TEXT_SCALE
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = budget.totalCaption.asString(),
                    color = if (isOverLimit) cardInk else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        fontSize = MaterialTheme.typography.labelMedium.fontSize * BUDGET_CARD_TEXT_SCALE
                    )
                )
            }
        }

        BudgetProgressBar(
            progress = budget.progressFraction,
            accent = progressAccent,
            // The goals card's bar: a slimmer rail sitting on the dedicated track token.
            height = 8.dp,
            trackColor = MaterialTheme.colorScheme.track
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Two pills: the verdict, and the money left as a share of the limit --
            // "Safe" beside "Rs 3,800 (48%) Left". Each is filled with its own ink at 20%, the
            // recipe the recurring card's meta chips already use, so the two card types keep
            // one chrome. The status word stays in the muted ink the SPENT / LIMIT label
            // carries; the figure keeps full ink, which is what makes it the readout. Past the
            // limit both go negative -- "-Rs 2,000 (-20%)" -- rather than flipping to an
            // amount over, so one sign says where the budget stands in every state.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RecurringMetaChip(
                    label = budget.statusCaption.asString(),
                    accent = MaterialTheme.colorScheme.onSurfaceVariant,
                    fillAlpha = 0.2f
                )

                RecurringMetaChip(
                    label = stringResource(
                        id = R.string.format_remaining_pill,
                        budget.remainingLabel,
                        budget.remainingPercent
                    ),
                    accent = if (isOverLimit) cardInk else MaterialTheme.colorScheme.onSurface,
                    fillAlpha = 0.2f
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BudgetCardAction(
                    icon = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.label_edit),
                    // The recurring rule card inks its edit action with the neutral
                    // variant rather than brand, which keeps the row's only loud glyph
                    // the destructive one.
                    accent = MaterialTheme.colorScheme.onSurfaceVariant,
                    isLocked = !budget.canEdit,
                    filled = false,
                    onClick = { if (budget.canEdit) onEditClick() }
                )

                Spacer(modifier = Modifier.width(8.dp))

                BudgetCardAction(
                    icon = Icons.Rounded.Close,
                    contentDescription = stringResource(id = R.string.label_delete),
                    accent = MaterialTheme.colorScheme.error,
                    isLocked = !budget.canEdit,
                    filled = false,
                    onClick = { if (budget.canEdit) onDeleteClick() }
                )
            }
        }
    }
}

@Composable
internal fun BudgetCardAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String? = null,
    contentDescription: String? = null,
    accent: Color = MaterialTheme.colorScheme.accentInk,
    isLocked: Boolean = false,
    enabled: Boolean = true,
    filled: Boolean = true,
    onClick: () -> Unit
) {
    val finalAccent = if (isLocked) MaterialTheme.colorScheme.outline else accent
    val backgroundAlpha = if (isLocked || !enabled) 0.16f else 0.12f
    val borderAlpha = if (isLocked || !enabled) 0.35f else 0.22f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (filled) {
                    Modifier
                        .background(finalAccent.copy(alpha = backgroundAlpha))
                        .border(
                            width = 1.dp,
                            color = finalAccent.copy(alpha = borderAlpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = if (label != null) 12.dp else if (filled) 10.dp else 4.dp,
                vertical = if (filled) 8.dp else 4.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription ?: label,
                    tint = if (isLocked || !enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else finalAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (label != null) {
                if (icon != null) Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = if (isLocked || !enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else finalAccent,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            if (isLocked) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.content_desc_locked_formatted, contentDescription ?: label ?: ""),
                    // The gate's own ink, not the dimmed accent of the control it sits in: this
                    // is the same gold the analytics period pill and the other locked controls
                    // mark Pro-only options with, so "locked" looks the same wherever it appears.
                    tint = MaterialTheme.colorScheme.featureGateLock,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}


@Composable
internal fun BudgetActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundBrush = if (enabled) {
        brandGradient()
    } else {
        Brush.horizontalGradient(
            colors = listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outlineVariant)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundBrush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let { iconVector ->
            Icon(
                imageVector = iconVector,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onCta,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onCta,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        )
    }
}

@Composable
internal fun RecurringExpenseCard(
    expense: BudgetRecurringExpenseUi,
    onEnabledChange: (Boolean) -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    /** Non-null only for an EMI rule with slots â€” opens the installment ledger. */
    onLedgerClick: (() -> Unit)? = null
) {
    val isUrgent = expense.isEnabled && (expense.accent == BudgetAccent.Overspent || expense.accent == BudgetAccent.Warning)
    val categoryInk = MaterialTheme.colorScheme.onSurfaceVariant
    val titleColor = if (expense.isEnabled) MaterialTheme.colorScheme.onSurface else categoryInk
    val frequencyAccent = if (expense.isEnabled) MaterialTheme.colorScheme.income else categoryInk
    val installmentAccent = if (expense.isEnabled) MaterialTheme.colorScheme.expense else categoryInk
    // A countdown (due today / tomorrow / in N days) borrows the installment pill's
    // ink so an approaching date stands out; a calendar date stays neutral.
    val dueColor = when {
        !expense.isEnabled -> categoryInk
        expense.isDueSoon -> installmentAccent
        else -> MaterialTheme.colorScheme.onSurface
    }
    val chipFillAlpha = if (expense.isEnabled) 0.07f else 0.14f
    val categoryColor = categoryTint(
        categoryId = expense.categoryId,
        colorHex = expense.colorHex
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(if (MaterialTheme.colorScheme.isDark) BudgetCardFillDark else BudgetCardFillLight)
            // One edge for every state: the normal rule's brand hairline. A rule that is
            // due soon, overdue, daily, yearly or switched off no longer repaints its
            // outline, so state is told by the sparkle, the due ink and the chips instead.
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.18f),
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val badgeFill = if (MaterialTheme.colorScheme.isDark) 0.10f else 0.10f
            val badgeEdge = if (MaterialTheme.colorScheme.isDark) 0.20f else 0.28f
            val iconTint = if (expense.isEnabled) categoryColor else categoryColor.copy(alpha = 0.45f)
            AppIconBox(
                icon = expense.icon,
                contentDescription = expense.categoryLabel,
                size = 44.dp,
                iconSize = 20.dp,
                tint = iconTint,
                backgroundColor = iconTint.copy(alpha = badgeFill),
                border = BorderStroke(1.dp, iconTint.copy(alpha = badgeEdge)),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.title,
                        color = titleColor,
                        style = MaterialTheme.typography.titleSmall
                            .copy(
                           fontWeight = FontWeight.Medium
                        )
                    )
                    
                    if (isUrgent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = stringResource(id = R.string.content_desc_upcoming),
                            tint = budgetAccentColor(expense.accent),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = expense.amountLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                )
            }

            Switch(
                modifier = Modifier.scale(0.9f),
                checked = expense.isEnabled,
                onCheckedChange = onEnabledChange,
                thumbContent = if (expense.isEnabled) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.switchOnTick
                        )
                    }
                } else {
                    null
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.switchOnThumb,
                    checkedTrackColor = MaterialTheme.colorScheme.switchOnTrack,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecurringMetaChip(
                label = expense.categoryLabel,
                accent = categoryInk
            )
            RecurringMetaChip(
                label = expense.frequencyLabel,
                accent = frequencyAccent,
                fillAlpha = chipFillAlpha
            )
            Spacer(modifier = Modifier.weight(1f))
            RecurringMetaChip(
                label = stringResource(id = R.string.label_installments_formatted, expense.currentInstallment, expense.totalInstallments),
                accent = installmentAccent,
                fillAlpha = chipFillAlpha
            )
        }

        val totalSlots = expense.totalInstallments
        if (totalSlots > 0) {
            val paidSlots = (
                if (expense.isInstallment) expense.installmentPaidCount else expense.currentInstallment
            ).coerceIn(0, totalSlots)
            val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f)
            val paidColor = if (expense.isEnabled) categoryColor else categoryColor.copy(alpha = 0.45f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(totalSlots) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (index < paidSlots) paidColor else trackColor)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.dueLabel.asString(),
                        color = dueColor,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    
                    if (expense.dueLabel.asString()
                            .equals(stringResource(id = R.string.label_due_today), ignoreCase = true)
                    ) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = expense.sourceDateLabel.asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (onLedgerClick != null) {
                    BudgetCardAction(
                        icon = Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(id = R.string.action_view_installments),
                        accent = MaterialTheme.colorScheme.onSurfaceVariant,
                        filled = false,
                        onClick = onLedgerClick
                    )
                }

                BudgetCardAction(
                    icon = if (expense.notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                    contentDescription = if (expense.notificationsEnabled) stringResource(id = R.string.label_mute_recurring_title) else stringResource(id = R.string.label_notifications),
                    accent = MaterialTheme.colorScheme.onSurfaceVariant,
                    filled = false,
                    onClick = {
                        if (expense.notificationsEnabled) {
                            onNotificationsEnabledChange(false)
                        } else {
                            onNotificationsEnabledChange(true)
                        }
                    }
                )

                GatedAction(
                    feature = Feature.RECURRING_RULE_EDIT,
                    displayName = stringResource(id = R.string.label_edit_recurring_rule),
                    onAction = onEditClick
                ) { status, onClick ->
                    BudgetCardAction(
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.label_edit),
                        accent = MaterialTheme.colorScheme.onSurfaceVariant,
                        isLocked = status !is AccessStatus.Granted,
                        filled = false,
                        onClick = onClick
                    )
                }

                BudgetCardAction(
                    icon = Icons.Rounded.Close,
                    contentDescription = stringResource(id = R.string.label_delete),
                    accent = MaterialTheme.colorScheme.error,
                    filled = false,
                    onClick = onDeleteClick
                )
            }
        }
        }
    }
}

/**
 * Every state [RecurringExpenseCard] can be in, so the light and dark hosts below can
 * show the whole matrix on one screen instead of across a dozen separate previews.
 *
 * Order runs from the calm states to the loud ones: a plain calendar date, the countdown
 * labels, the frequency accents, a muted rule, a switched-off rule, and an EMI with its
 * ledger affordance.
 */
internal fun recurringExpenseCardStates(): List<BudgetRecurringExpenseUi> = listOf(
    previewRecurringExpense(
        id = "preview-regular",
        title = "Rent",
        dueLabel = "01 Oct",
        accent = BudgetAccent.Primary
    ),
    // Due today: countdown ink plus the red dot.
    previewRecurringExpense(
        id = "preview-due-today",
        title = "Internet",
        amountLabel = "Rs 999 / Month",
        dueLabel = "Due Today",
        accent = BudgetAccent.Overspent,
        isDueSoon = true
    ),
    previewRecurringExpense(
        id = "preview-due-soon",
        title = "Insurance",
        amountLabel = "Rs 2,400 / Month",
        dueLabel = "In 2 days",
        accent = BudgetAccent.Warning,
        isDueSoon = true
    ),
    previewRecurringExpense(
        id = "preview-daily",
        title = "Coffee",
        amountLabel = "Rs 150 / Day",
        frequency = RecurringFrequency.Daily,
        frequencyLabel = "Daily",
        dueLabel = "Tomorrow",
        accent = BudgetAccent.Daily,
        isDueSoon = true
    ),
    previewRecurringExpense(
        id = "preview-weekly",
        title = "Groceries",
        amountLabel = "Rs 3,000 / Week",
        frequency = RecurringFrequency.Weekly,
        frequencyLabel = "Weekly",
        dueLabel = "Fri 03 Oct",
        accent = BudgetAccent.Warning
    ),
    previewRecurringExpense(
        id = "preview-yearly",
        title = "Domain",
        amountLabel = "Rs 1,099 / Year",
        frequency = RecurringFrequency.Yearly,
        frequencyLabel = "Yearly",
        dueLabel = "12 Mar",
        accent = BudgetAccent.Yearly
    ),
    // Notifications off: the bell action flips to the muted glyph.
    previewRecurringExpense(
        id = "preview-muted",
        title = "Streaming",
        amountLabel = "Rs 199 / Month",
        dueLabel = "20 Oct",
        accent = BudgetAccent.Primary,
        notificationsEnabled = false
    ),
    // Switched off: every ink on the card drops to the muted variant.
    previewRecurringExpense(
        id = "preview-disabled",
        title = "Gym",
        amountLabel = "Rs 1,200 / Month",
        dueLabel = "12 Oct",
        accent = BudgetAccent.Disabled,
        isEnabled = false
    ),
    // EMI: adds the installment chip row and the ledger action.
    previewRecurringExpense(
        id = "preview-emi",
        title = "Phone EMI",
        amountLabel = "Rs 4,500 / Month",
        categoryLabel = "Electronics",
        dueLabel = "15 Oct",
        accent = BudgetAccent.Primary,
        isInstallment = true
    )
)

@Preview(
    name = "RecurringExpenseCard States Light",
    showBackground = true,
    widthDp = 412,
    heightDp = 1800
)
@Composable
internal fun RecurringExpenseCardStatesLightPreview() {
    RecurringExpenseCardStatesHost()
}

@Preview(
    name = "RecurringExpenseCard States Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 412,
    heightDp = 1800
)
@Composable
internal fun RecurringExpenseCardStatesDarkPreview() {
    RecurringExpenseCardStatesHost()
}

/**
 * The shared light/dark host: one scrolling column holding every card state. The scroll
 * matters in interactive mode (the column is taller than the preview) and is harmless in a
 * static render, which simply clips at the preview's height.
 */
@Composable
internal fun RecurringExpenseCardStatesHost() {
    val states = remember { recurringExpenseCardStates() }
    ExpenseTrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                states.forEach { state ->
                    RecurringExpenseCard(
                        expense = state,
                        onEnabledChange = {},
                        onNotificationsEnabledChange = {},
                        onEditClick = {},
                        onDeleteClick = {},
                        onLedgerClick = if (state.isInstallment) ({}) else null
                    )
                }
            }
        }
    }
}

internal fun previewRecurringExpense(
    id: String = "preview-recurring",
    title: String,
    amountLabel: String = "Rs 12,000 / Month",
    categoryLabel: String = "Housing",
    frequency: RecurringFrequency = RecurringFrequency.Monthly,
    frequencyLabel: String = "Monthly",
    dueLabel: String,
    sourceDateLabel: String = "01 Jan 2026",
    accent: BudgetAccent,
    isEnabled: Boolean = true,
    notificationsEnabled: Boolean = true,
    isInstallment: Boolean = false,
    isDueSoon: Boolean = false
): BudgetRecurringExpenseUi = BudgetRecurringExpenseUi(
    id = id,
    transactionId = "tx-$id",
    title = title,
    amountLabel = amountLabel,
    categoryLabel = categoryLabel,
    frequency = frequency,
    frequencyLabel = frequencyLabel,
    repeatCount = 12,
    currentInstallment = 3,
    totalInstallments = 12,
    sourceDateLabel = UiText.dynamic(sourceDateLabel),
    dueLabel = UiText.dynamic(dueLabel),
    isDueSoon = isDueSoon,
    dueAmountLabel = amountLabel,
    icon = Icons.Filled.DateRange,
    accent = accent,
    nextDueAt = 0L,
    isEnabled = isEnabled,
    notificationsEnabled = notificationsEnabled,
    isInstallment = isInstallment,
    recurringType = if (isInstallment) RecurringType.INSTALLMENT else RecurringType.REGULAR,
    installmentTotalAmount = if (isInstallment) 54000.0 else 0.0,
    installmentPerAmount = if (isInstallment) 4500.0 else 0.0,
    installmentPaidCount = if (isInstallment) 3 else 0,
    installmentRemainingLabel = if (isInstallment) "Rs 40,500 left" else "",
    installmentProgressFraction = if (isInstallment) 0.25f else 0f,
    installmentPlanStatus = if (isInstallment) InstallmentStatus.ACTIVE else null,
    firstDueAt = 0L,
    slots = if (isInstallment) {
        listOf(
            InstallmentSlotUi(
                id = "slot-1",
                index = 4,
                dueAt = 0L,
                amountLabel = "Rs 4,500",
                status = InstallmentOccurrenceStatus.PENDING,
                paidAt = null
            )
        )
    } else {
        emptyList()
    }
)
@Preview(name = "CategoryBudgetCard Light", showBackground = true)
@Preview(name = "CategoryBudgetCard Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CategoryBudgetCardPreview() {
    ExpenseTrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // The labels below are the ones the view model actually produces for a
                // healthy budget: Safe beside the SPENT / LIMIT caption.
                CategoryBudgetCard(
                    budget = BudgetCategoryBudgetUi(
                        id = "preview-food",
                        categoryId = 1,
                        title = "Food",
                        summaryLabel = "Rs 4,200 / Rs 8,000",
                        remainingLabel = "Rs 3,800",
                        remainingPercent = 48,
                        statusCaption = UiText.res(R.string.label_safe),
                        totalCaption = UiText.res(R.string.label_spent_limit),
                        progressFraction = 0.525f,
                        spentAmount = 4200.0,
                        limitAmount = 8000.0,
                        icon = Icons.Filled.DateRange,
                        accent = BudgetAccent.Primary
                    ),
                    onEditClick = {},
                    onDeleteClick = {},
                    onInfoClick = {}
                )
                // Near limit: the Near Limit caption and the amber rail.
                CategoryBudgetCard(
                    budget = BudgetCategoryBudgetUi(
                        id = "preview-near",
                        categoryId = 3,
                        title = "Travel",
                        summaryLabel = "Rs 10,500 / Rs 12,000",
                        remainingLabel = "Rs 1,500",
                        remainingPercent = 13,
                        statusCaption = UiText.res(R.string.label_near_limit),
                        totalCaption = UiText.res(R.string.label_spent_limit),
                        progressFraction = 0.875f,
                        spentAmount = 10500.0,
                        limitAmount = 12000.0,
                        icon = Icons.Filled.DateRange,
                        accent = BudgetAccent.Warning
                    ),
                    onEditClick = {},
                    onDeleteClick = {},
                    onInfoClick = {}
                )
                // Overspent: the Overspent caption and EXCEEDED.
                CategoryBudgetCard(
                    budget = BudgetCategoryBudgetUi(
                        id = "preview-over",
                        categoryId = 2,
                        title = "Shopping",
                        summaryLabel = "Rs 12,000 / Rs 10,000",
                        remainingLabel = "-Rs 2,000",
                        remainingPercent = -20,
                        statusCaption = UiText.res(R.string.label_budget_status_label),
                        totalCaption = UiText.res(R.string.label_exceeded),
                        progressFraction = 1f,
                        spentAmount = 12000.0,
                        limitAmount = 10000.0,
                        icon = Icons.Filled.DateRange,
                        accent = BudgetAccent.Overspent
                    ),
                    onEditClick = {},
                    onDeleteClick = {},
                    onInfoClick = {}
                )
            }
        }
    }
}
