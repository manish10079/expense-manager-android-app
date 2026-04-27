package com.mkn0079.expensetracker.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.avatarInitials
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.ui.components.ProfileAvatar
import com.mkn0079.expensetracker.ui.components.TodaySpendingCard
import com.mkn0079.expensetracker.ui.components.TotalBalanceCard
import com.mkn0079.expensetracker.ui.components.TransactionCard
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.viewmodels.HomeViewModel
import com.mkn0079.expensetracker.ui.viewmodels.HomeScreenUiState
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    userProfile: UserProfile = defaultUserProfile,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    categories: List<CategoryType> = emptyList(),
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    onViewAllClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTodaySpendingClick: () -> Unit = {}
) {
    val homeViewModel: HomeViewModel = viewModel()
    androidx.compose.runtime.LaunchedEffect(
        userProfile,
        currencyId,
        amountFormatPreferences,
        timeFormat,
        categories,
        transactionCardCustomizationSettings
    ) {
        homeViewModel.updateInputs(
            userProfile = userProfile,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            timeFormat = timeFormat,
            categories = categories,
            customizationSettings = transactionCardCustomizationSettings
        )
    }
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        userProfile = userProfile,
        uiState = uiState,
        onViewAllClick = onViewAllClick,
        onTransactionClick = onTransactionClick,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        onTodaySpendingClick = onTodaySpendingClick,
        onToggleBalanceVisibility = homeViewModel::toggleBalanceVisibility
    )
}

@Composable
private fun HomeScreenContent(
    userProfile: UserProfile,
    uiState: HomeScreenUiState,
    onViewAllClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTodaySpendingClick: () -> Unit,
    onToggleBalanceVisibility: () -> Unit
) {
    val profileAvatarGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.86f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 15.dp, end = 15.dp, top = 15.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(
                        initials = userProfile.avatarInitials(),
                        size = 60.dp,
                        textSize = 18.sp,
                        photoUri = userProfile.photoUri,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        borderBrush = profileAvatarGradient,
                        placeholderIconBrush = profileAvatarGradient,
                        modifier = Modifier.clickable(onClick = onProfileClick)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hi, ${uiState.greetingName} 👋",
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Track every move with confidence.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                SettingsButton(onClick = onSettingsClick)
            }

            Spacer(modifier = Modifier.height(18.dp))

            TotalBalanceCard(
                totalBalance = uiState.totalBalance,
                previousMonthBalance = uiState.previousMonthBalance,
                income = uiState.totalIncome,
                expense = uiState.totalExpense,
                isBalanceHidden = uiState.isBalanceHidden,
                onToggleVisibility = onToggleBalanceVisibility
            )

            Spacer(modifier = Modifier.height(14.dp))

            TodaySpendingCard(
                amount = uiState.todaySpending,
                onClick = onTodaySpendingClick
            )

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.9f else 1f,
                    animationSpec = tween(150)
                )
                val scope = rememberCoroutineScope()

                Text(
                    text = "VIEW ALL",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
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
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
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
                        transactionTypeId = card.transactionTypeId,
                        paymentType = card.paymentType,
                        showTypeLabel = uiState.customizationSettings.showIncomeExpenseLabels,
                        showTransactionDate = uiState.customizationSettings.showTransactionDate,
                        showPaymentMethod = uiState.customizationSettings.showPaymentMethod,
                        showTransactionTime = uiState.customizationSettings.showTransactionTime,
                        showCategoryIcon = uiState.customizationSettings.showCategoryIcon,
                        onClick = { onTransactionClick(card.transaction) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        HomeScreenContent(
            userProfile = defaultUserProfile,
            uiState = HomeScreenUiState(),
            onViewAllClick = {},
            onTransactionClick = {},
            onProfileClick = {},
            onSettingsClick = {},
            onTodaySpendingClick = {},
            onToggleBalanceVisibility = {}
        )
    }
}
@Composable
fun SettingsButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(150)
    )
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(scale)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .clickable {
                isPressed = true
                onClick()
                scope.launch {
                    delay(150)
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}
