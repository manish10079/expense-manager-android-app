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
fun DeleteGoalDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModernConfirmationDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.title_delete_goal),
        text = stringResource(R.string.msg_delete_goal_confirm, goalName),
        confirmButtonText = stringResource(R.string.label_delete_1),
        onConfirm = onConfirm,
        headerIcon = Icons.Filled.Delete,
        dismissButtonText = stringResource(R.string.label_cancel_1),
        onDismiss = onDismiss
    )
}

@Composable
fun FundGoalDialog(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amountInput by rememberSaveable { mutableStateOf("") }
    val fundAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isSaveEnabled = fundAmount > 0.0

    Dialog(onDismissRequest = onDismiss) {
        NamedCreateCard(
            title = stringResource(R.string.title_fund_goal),
            caption = stringResource(R.string.msg_fund_goal_caption),
            value = amountInput,
            onValueChange = { updatedValue ->
                amountInput = updatedValue.filter { it.isDigit() || it == '.' }
            },
            placeholder = stringResource(R.string.label_fund_amount),
            confirmLabel = stringResource(R.string.label_add_action),
            onConfirm = { if (isSaveEnabled) onSave(fundAmount) },
            headerIcon = Icons.Filled.Savings,
            confirmEnabled = isSaveEnabled
        )
    }
}
