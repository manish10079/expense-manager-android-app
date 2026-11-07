package com.mknlabs.expensetracker.feature.home.ui


import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.track

import android.content.res.Configuration
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.ChatCircle
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.PiggyBank
import com.adamglin.phosphoricons.regular.X
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.avatarInitials
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.hasPhoneNumber
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.core.ui.components.*
import com.mknlabs.expensetracker.core.ui.HomeFirstRunPromptGate
import com.mknlabs.expensetracker.core.ui.components.LocalSplashGone
import com.mknlabs.expensetracker.core.ui.components.MissingPermissionsBottomSheet
import com.mknlabs.expensetracker.core.ui.components.RateAppBottomSheet
import com.mknlabs.expensetracker.core.ui.components.ReferOfferBottomSheet
import com.mknlabs.expensetracker.data.local.ReferPromptStore
import com.mknlabs.expensetracker.data.local.RateAppPromptStore
import com.mknlabs.expensetracker.data.local.SmsInboxStaleRemindStore
import com.mknlabs.expensetracker.utils.RateAppPrompt
import com.mknlabs.expensetracker.utils.SmsInboxStaleRemind
import kotlinx.coroutines.delay
import com.mknlabs.expensetracker.utils.LaunchSheetPick
import kotlinx.coroutines.launch
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnLight
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupUiState
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupViewModel
import com.mknlabs.expensetracker.utils.DeviceVendorUtils
import com.mknlabs.expensetracker.utils.findFragmentActivity
import com.mknlabs.expensetracker.utils.PermissionNudge
import com.mknlabs.expensetracker.data.local.PermissionNudgeState
import com.mknlabs.expensetracker.data.local.PermissionNudgeStore
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.AdPlacement

@Composable
internal fun RecentActivitiesHeader(onViewAllClick: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.isDark
    val titleColor = MaterialTheme.colorScheme.onBackground
    val actionColor = if (isDark) NavOnDark else NavOnLight

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.label_recent),
            color = titleColor,
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        )

        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.9f else 1f,
            animationSpec = tween(150)
        )
        val scope = rememberCoroutineScope()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .scale(scale)
                .clickable {
                    isPressed = true
                    onViewAllClick()

                    scope.launch {
                        delay(150)
                        isPressed = false
                    }
                }
        ) {
            Text(
                text = stringResource(id = R.string.label_view_all),
                color = actionColor,
                style = MaterialTheme.typography.labelMedium
            )
            Icon(
                imageVector = PhosphorIcons.Regular.CaretRight,
                contentDescription = null,
                tint = actionColor,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
internal fun ColumnScope.HomeTransactionsList(
    uiState: HomeScreenUiState,
    isProUser: Boolean,
    onTransactionClick: (Transaction) -> Unit,
    state: LazyListState,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    LazyColumn(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = bottomPadding)
    ) {
        items(
            items = uiState.recentTransactions,
            key = { card -> card.transaction.id },
            contentType = { "transaction" }
        ) { card ->
            TransactionCard(
                note = card.note,
                transactionDate = card.transactionDate,
                transactionTime = card.transactionTime,
                amount = card.amount,
                icon = card.icon,
                categoryId = card.transaction.categoryId,
                categoryColorHex = card.categoryColorHex,
                transactionTypeId = card.transactionTypeId,
                paymentType = card.paymentType,
                categoryLabel = card.categoryLabel,
                showTypeLabel = uiState.customizationSettings.showIncomeExpenseLabels,
                showTransactionDate = uiState.customizationSettings.showTransactionDate,
                showPaymentMethod = uiState.customizationSettings.showPaymentMethod,
                showTransactionTime = uiState.customizationSettings.showTransactionTime,
                showCategoryIcon = uiState.customizationSettings.showCategoryIcon,
                showCategoryLabel = uiState.customizationSettings.showCategoryLabel,
                tags = card.tags,
                fundName = card.fundName,
                showNoteTooltip = isProUser,
                isProUser = isProUser,
                isRecurring = card.isRecurring,
                onClick = { onTransactionClick(card.transaction) }
            )
        }
    }
}


