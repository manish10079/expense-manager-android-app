package com.mknlabs.expensetracker.feature.goals.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AvatarRingBrush
import com.mknlabs.expensetracker.core.ui.components.BrandAddFab
import com.mknlabs.expensetracker.core.ui.components.IconPickerGrid
import com.mknlabs.expensetracker.core.ui.components.BrandAddFabDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.NamedCreateCard
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.categoryIconOptions
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.GoalFundEntry
import com.mknlabs.expensetracker.core.ui.components.AppDialogConfirmButton
import com.mknlabs.expensetracker.core.ui.components.AppDialogDefaults
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppDialogDismissButton
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.CategoryColorRow
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.core.ui.models.CategoryIconOption
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.identityColor
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.track
import com.mknlabs.expensetracker.core.ui.theme.GoalProgressHigh
import com.mknlabs.expensetracker.core.ui.theme.flatCard
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.profileSheet

import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mknlabs.expensetracker.utils.daysUntilTimestamp
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatDate
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import java.util.Locale

@Composable
fun GoalItem(
    goal: Goal,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    isHistoryExpanded: Boolean,
    fundEntries: List<GoalFundEntry>,
    onFund: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFundHistory: () -> Unit
) {
    // Resolve the picked icon from the same catalog the picker uses; fall back to
    // the registry-resolved icon for legacy keys not in the catalog.
    val goalIcon = remember(goal.iconKey) {
        categoryIconOptions.firstOrNull { it.id == goal.iconKey }?.icon ?: goal.icon
    }

    val colorScheme = MaterialTheme.colorScheme
    val cardShape = RoundedCornerShape(24.dp)
    val cardChrome = Modifier
        .clip(cardShape)
        // The home screen's Spent Today fill (#13151F / white), so the two card families
        // share one surface instead of the goals card sitting on a grey wash.
        .background(colorScheme.flatCard)
        .border(
            width = 1.dp,
            color = colorScheme.accentInk.copy(alpha = 0.18f),
            shape = cardShape
        )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardChrome)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val identity = MaterialTheme.colorScheme.identityColor(goal.colorHex)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(identity.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = goalIcon,
                            contentDescription = stringResource(R.string.cd_goal_icon),
                            tint = identity,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoalCardAction(
                        icon = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.cd_edit_goal),
                        accent = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onEdit
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    GoalCardAction(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(R.string.cd_add_funds),
                        accent = MaterialTheme.colorScheme.accentInk,
                        onClick = onFund
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    GoalCardAction(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_delete_goal),
                        accent = MaterialTheme.colorScheme.error,
                        onClick = onDelete
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            GoalProgressBar(progress = goal.progress)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (goal.isCompleted) {
                        stringResource(R.string.label_goal_completed)
                    } else {
                        stringResource(R.string.format_goal_progress, (goal.progress * 100).toInt())
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (goal.isCompleted) {
                        GoalProgressHigh
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (goal.isCompleted) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = stringResource(
                        R.string.format_goal_amount_status,
                        formatCurrencyValue(goal.currentAmountMinor / 100.0, currencyId, amountFormatPreferences),
                        formatCurrencyValue(goal.targetAmountMinor / 100.0, currencyId, amountFormatPreferences)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            // Deadline + Fund History in the same row
            if (!goal.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: deadline text (if set)
                    goal.deadlineAt?.let { deadline ->
                        val daysLeft = daysUntilTimestamp(deadline)
                        val (deadlineText, deadlineColor) = when {
                            daysLeft < 0 -> stringResource(
                                R.string.format_days_overdue, -daysLeft
                            ) to MaterialTheme.colorScheme.error

                            daysLeft == 0L -> stringResource(R.string.label_goal_due_today) to MaterialTheme.colorScheme.error
                            daysLeft == 1L -> stringResource(R.string.label_one_day_left) to MaterialTheme.colorScheme.error
                            else -> stringResource(R.string.format_days_left, daysLeft) to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = deadlineText,
                            style = MaterialTheme.typography.bodySmall,
                            color = deadlineColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If no deadline, push fund history to the start
                    if (goal.deadlineAt == null) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    // Right: Fund History toggle
                    GoalFundHistoryInline(
                        entryCount = fundEntries.size,
                        isExpanded = isHistoryExpanded,
                        onClick = onToggleFundHistory
                    )
                }
            }
            // Completed goals: still show fund history
            if (goal.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                GoalFundHistoryInline(
                    entryCount = fundEntries.size,
                    isExpanded = isHistoryExpanded,
                    onClick = onToggleFundHistory
                )
            }

            // Collapsible Fund History
            AnimatedVisibility(
                visible = isHistoryExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                GoalFundHistorySection(
                    entries = fundEntries,
                    currencyId = currencyId,
                    amountFormatPreferences = amountFormatPreferences,
                    dateFormatPattern = dateFormatPattern
                )
            }
        }
    }
}

/** Formats a minor-unit amount as plain input text (e.g. 250000 -> "2500", 250050 -> "2500.50"). */
@Composable
internal fun GoalCardAction(
    icon: ImageVector,
    contentDescription: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
internal fun GoalFundHistoryInline(
    entryCount: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = stringResource(R.string.desc_toggle_fund_history),
            tint = colorScheme.accentInk,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.label_fund_history_inline),
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        if (entryCount > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(
                    if (entryCount == 1) R.string.format_fund_entries_one
                    else R.string.format_fund_entries_count,
                    entryCount
                ),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * The plans screen's budget-card bar, reused here: the avatar ring's indigo → violet →
 * fuchsia ramp in both themes, on the same 8 dp rail and track token, so the goal card
 * and the budget card read their progress with one ramp instead of two colour stories.
 * The fraction animates on the same curve the budget bar uses.
 */
@Composable
internal fun GoalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.track
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "goal_progress_animation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(8.dp)
                .clip(CircleShape)
                .background(AvatarRingBrush)
        )
    }
}

@Composable
internal fun GoalFundHistorySection(
    entries: List<GoalFundEntry>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.label_no_fund_history),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 26.dp, top = 4.dp, bottom = 4.dp)
            )
        } else {
            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 26.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Colored dot indicator
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GoalProgressHigh)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Formatted amount
                    Text(
                        text = formatCurrencyValue(
                            entry.amountMinor / 100.0,
                            currencyId,
                            amountFormatPreferences
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    // Date
                    Text(
                        text = stringResource(
                            R.string.label_funded_on,
                            formatDate(entry.fundedAt, dateFormatPattern)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
