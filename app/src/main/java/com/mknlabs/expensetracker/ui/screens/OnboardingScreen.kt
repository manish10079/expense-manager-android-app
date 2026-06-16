package com.mknlabs.expensetracker.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.collectIsPressedAsState
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Transgender
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.ui.components.AppSelectionSheet
import com.mknlabs.expensetracker.ui.components.ProfileAvatar
import com.mknlabs.expensetracker.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.ui.components.UserBadge
import com.mknlabs.expensetracker.ui.components.UserBadgeType
import com.mknlabs.expensetracker.ui.components.input.InputFieldCard
import com.mknlabs.expensetracker.ui.components.input.InputType
import com.mknlabs.expensetracker.ui.models.SelectionItem
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.surfaceGradient
import com.mknlabs.expensetracker.ui.viewmodels.AuthViewModel
import com.mknlabs.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mknlabs.expensetracker.utils.formatDate

private data class OnboardingPage(
    val title: String,
    val description: String,
    val actionLabel: String,
    val accentedText: String? = null,
    val titleFontSize: TextUnit = 42.sp,
    val titleLineHeight: TextUnit = 48.sp,
    val supportingContent: (@Composable () -> Unit)? = null,
    val illustration: @Composable (BoxScope.() -> Unit)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: (name: String, gender: String, dobMillis: Long?, financialGoal: String) -> Unit = { _, _, _, _ -> },
    onSignUpSuccess: (() -> Unit)? = null,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val onboardingPages = remember {
        listOf(
            OnboardingPage(
                title = context.getString(R.string.title_track_expensesneasily),
                description = context.getString(R.string.desc_log_daily_spending),
                actionLabel = context.getString(R.string.label_next),
                illustration = { ExpenseCardIllustration() }
            ),
            OnboardingPage(
                title = context.getString(R.string.title_secure_private),
                description = context.getString(R.string.desc_financial_data_secure),
                actionLabel = context.getString(R.string.label_next),
                illustration = { SecureTrackerIllustration() }
            ),
            OnboardingPage(
                title = context.getString(R.string.title_visual_analytics),
                description = context.getString(R.string.desc_visualize_spending),
                actionLabel = context.getString(R.string.label_next),
                illustration = { AnalyticsIllustration() }
            ),
            OnboardingPage(
                title = context.getString(R.string.title_premium_by_designnprivate_by_n),
                description = context.getString(R.string.desc_modern_finance),
                actionLabel = context.getString(R.string.label_continue),
                accentedText = context.getString(R.string.label_private_by_nature),
                titleFontSize = 34.sp,
                titleLineHeight = 40.sp,
                supportingContent = { PremiumBenefitCards() },
                illustration = { PremiumPrivacyIllustration() }
            ),
            OnboardingPage(
                title = context.getString(R.string.title_secure_your_account),
                description = context.getString(R.string.desc_sync_and_premium_features),
                actionLabel = context.getString(R.string.label_next),
                illustration = { SecureTrackerIllustration() } 
            ),
            OnboardingPage(
                title = context.getString(R.string.label_financial_goal_title),
                description = context.getString(R.string.label_financial_zen),
                actionLabel = context.getString(R.string.label_next),
                illustration = { GoalIllustration() }
            ),
            OnboardingPage(
                title = context.getString(R.string.title_lets_get_started),
                description = context.getString(R.string.desc_tell_us_about_yourself),
                actionLabel = context.getString(R.string.label_get_started),
                illustration = { /* No illustration for setup page */ }
            )
        )
    }
    var currentPage by remember { mutableIntStateOf(0) }
    val page = onboardingPages[currentPage]
    val isAuthPage = currentPage == 4
    val isGoalPage = currentPage == 5
    val isSetupPage = currentPage == 6

    // Setup state
    var userName by remember { mutableStateOf("") }
    var userGender by remember { mutableStateOf("") }
    var userDobMillis by remember { mutableLongStateOf(0L) }
    var userFinancialGoal by remember { mutableStateOf("") }
    var isGenderPickerVisible by remember { mutableStateOf(false) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    val genderPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Auto-fill name if user logged in via social provider
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (!user.isAnonymous && userName.isEmpty()) {
                userName = user.displayName ?: ""
            }
        }
    }

    // Force redirection to Setup Page once user is detected
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentPage == 4) {
            Log.d("Onboarding", "User detected! Force-advancing to Goal Page.")
            currentPage = 5
        }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context.applicationContext, toastMessage, Toast.LENGTH_LONG).show()
            toastMessage = null
        }
    }

    val maleLabel = stringResource(id = R.string.label_male)
    val femaleLabel = stringResource(id = R.string.label_female)
    val nonBinaryLabel = stringResource(id = R.string.label_non_binary)
    val preferNotToSayLabel = stringResource(id = R.string.label_prefer_not_to_say)
    val genderOptions = listOf(maleLabel, femaleLabel, nonBinaryLabel, preferNotToSayLabel)
    val genderItems = remember(maleLabel, femaleLabel, nonBinaryLabel, preferNotToSayLabel) {
        genderOptions.map { option ->
            SelectionItem(
                id = option,
                title = option,
                leadingIcon = genderToIcon(option, maleLabel, femaleLabel, nonBinaryLabel, preferNotToSayLabel)
            )
        }
    }

    val goalHome = stringResource(id = R.string.label_goal_home)
    val goalTravel = stringResource(id = R.string.label_goal_travel)
    val goalDebt = stringResource(id = R.string.label_goal_debt)
    val goalRetirement = stringResource(id = R.string.label_goal_retirement)
    val goalSavings = stringResource(id = R.string.label_goal_savings)
    val goalCar = stringResource(id = R.string.label_goal_car)
    val goalEducation = stringResource(id = R.string.label_goal_education)
    val goalBusiness = stringResource(id = R.string.label_goal_business)
    val goalInvest = stringResource(id = R.string.label_goal_invest)
    val goalOther = stringResource(id = R.string.label_goal_other)
    
    val goalOptions = listOf(goalHome, goalTravel, goalDebt, goalRetirement, goalSavings, goalCar, goalEducation, goalBusiness, goalInvest, goalOther)
    val goalIcons = listOf(
        Icons.Filled.Money, Icons.Filled.Analytics, Icons.Filled.Security, Icons.Filled.Savings, 
        Icons.Filled.Analytics, Icons.Filled.Money, Icons.Filled.Analytics, Icons.Filled.Money, 
        Icons.Filled.Analytics, Icons.Filled.Money
    )

    var customGoalText by remember { mutableStateOf("") }

    val onCompleteInternal: () -> Unit = {
        val finalGoal = if (userFinancialGoal == goalOther) customGoalText else userFinancialGoal
        onFinish(userName, userGender, userDobMillis, finalGoal)
    }

    BackHandler(enabled = currentPage > 0) {
        authViewModel.cancelGuestSignIn()
        authViewModel.resetState()
        currentPage -= 1
    }

    val nameStr = stringResource(id = R.string.label_name_capitalized)
    val genderStr = stringResource(id = R.string.label_gender_capitalized)
    val dobStr = stringResource(id = R.string.label_dob_capitalized)
    val msgProvide = stringResource(id = R.string.msg_please_provide_your_val, "%s")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AmbientBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = Dimens.HeaderSpacing, bottom = 16.dp)
        ) {
            // Main Content Area (Weight 1 pushes footer down)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (isAuthPage || isSetupPage || isGoalPage) Modifier.verticalScroll(rememberScrollState())
                        else Modifier
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration Section
                if (!isSetupPage && !isAuthPage && !isGoalPage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentPage,
                            label = "onboarding_illustration"
                        ) { pageIndex ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp)
                            ) {
                                onboardingPages[pageIndex].illustration(this)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }

                // Title & Description Section
                AnimatedContent(
                    targetState = currentPage,
                    label = "onboarding_copy",
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                ) { pageIndex ->
                    val current = onboardingPages[pageIndex]
                    val isAuthOnThisPageIndex = pageIndex == 4
                    val isGoalOnThisPageIndex = pageIndex == 5
                    val isSetupOnThisPageIndex = pageIndex == 6

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSetupOnThisPageIndex) {
                            val isAnonymous = currentUser?.isAnonymous ?: true
                            
                            if (isAnonymous) {
                                UserBadge(
                                    label = stringResource(R.string.label_guest_user),
                                    type = UserBadgeType.GUEST,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = current.title,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = current.titleFontSize,
                                        lineHeight = current.titleLineHeight
                                    )
                                )
                                
                                Spacer(Modifier.width(8.dp))
                                
                                IconButton(
                                    onClick = { toastMessage = context.getString(R.string.msg_privacy_info) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = stringResource(R.string.cd_privacy_info),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        } else {
                            Text(
                                text = buildAnnotatedString {
                                    if (current.accentedText.isNullOrEmpty() || !current.title.contains(current.accentedText)) {
                                        append(current.title)
                                    } else {
                                        val accentStart = current.title.indexOf(current.accentedText)
                                        append(current.title.substring(0, accentStart))
                                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                                            append(current.accentedText)
                                        }
                                        append(current.title.substring(accentStart + current.accentedText.length))
                                    }
                                },
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = current.titleFontSize,
                                    lineHeight = current.titleLineHeight
                                )
                            )

                            Spacer(modifier = Modifier.height(if (isAuthOnThisPageIndex) 24.dp else 32.dp))
                        }

                        if (isAuthOnThisPageIndex) {
                            AuthContent(
                                viewModel = authViewModel,
                                onAuthSuccess = {
                                    Log.d("Onboarding", "Auth SUCCESS callback triggered.")
                                    currentPage = 5
                                },
                                onGuestContinue = {
                                    Log.d("Onboarding", "Guest continue triggered.")
                                    currentPage = 5
                                },
                                onSignUpSuccess = {
                                    Log.d("Onboarding", "Sign up success callback triggered.")
                                    onSignUpSuccess?.invoke()
                                    currentPage = 5
                                }
                            )
                        } else if (isGoalOnThisPageIndex) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                goalOptions.forEachIndexed { index, goal ->
                                    val isSelected = userFinancialGoal == goal
                                    val isOther = goal == goalOther

                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                                .clickable { 
                                                    userFinancialGoal = goal 
                                                }
                                                .padding(horizontal = 20.dp, vertical = 18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = goalIcons[index],
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(Modifier.width(16.dp))
                                            Text(
                                                text = goal,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(Modifier.weight(1f))
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        if (isOther && isSelected) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            InputFieldCard(
                                                title = "",
                                                value = customGoalText,
                                                onValueChange = { customGoalText = it },
                                                inputType = InputType.Text,
                                                placeholder = stringResource(id = R.string.placeholder_enter_custom_goal),
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (isSetupOnThisPageIndex) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InputFieldCard(
                                    title = stringResource(id = R.string.label_full_name),
                                    value = userName,
                                    onValueChange = { userName = it },
                                    inputType = InputType.Text,
                                    leadingIcon = Icons.Rounded.Person,
                                    placeholder = stringResource(id = R.string.placeholder_enter_name)
                                )

                                InputFieldCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(id = R.string.label_gender),
                                    value = userGender,
                                    onValueChange = {},
                                    inputType = InputType.Date,
                                    leadingIcon = genderToIcon(userGender, maleLabel, femaleLabel, nonBinaryLabel, preferNotToSayLabel),
                                    placeholder = stringResource(id = R.string.label_select_gender),
                                    onClick = { isGenderPickerVisible = true },
                                    trailingContent = {
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )

                                InputFieldCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(id = R.string.label_date_of_birth),
                                    value = if (userDobMillis == 0L) "" else formatDate(userDobMillis, DEFAULT_DATE_FORMAT_PATTERN),
                                    onValueChange = {},
                                    inputType = InputType.Date,
                                    leadingIcon = Icons.Filled.CalendarMonth,
                                    placeholder = stringResource(id = R.string.placeholder_select_dob),
                                    onClick = { isDatePickerVisible = true }
                                )
                            }
                        } else {
                            Text(
                                text = current.description,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 27.sp
                                ),
                                modifier = Modifier.fillMaxWidth(0.88f)
                            )

                            current.supportingContent?.let { content ->
                                Spacer(modifier = Modifier.height(28.dp))
                                content()
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (!isAuthPage) {
                // Fixed Footer Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryOnboardingButton(
                        label = page.actionLabel,
                        onClick = {
                            val lastIndex = onboardingPages.lastIndex
                            
                            if (currentPage == 5) { // Goal Page
                                val isOther = userFinancialGoal == goalOther
                                if (userFinancialGoal.isNotEmpty() && (!isOther || customGoalText.trim().isNotEmpty())) {
                                    currentPage += 1
                                } else {
                                    toastMessage = if (isOther) context.getString(R.string.placeholder_enter_custom_goal) 
                                                   else context.getString(R.string.label_financial_goal_title)
                                }
                            } else if (currentPage == lastIndex) {
                                val missingFields = mutableListOf<String>()
                                if (userName.trim().isEmpty()) missingFields.add(nameStr)
                                if (userGender.trim().isEmpty()) missingFields.add(genderStr)
                                if (userDobMillis == 0L) missingFields.add(dobStr)

                                if (missingFields.isEmpty()) {
                                    onCompleteInternal()
                                } else {
                                    toastMessage = msgProvide.replace("%s", missingFields.joinToString(", "))
                                }
                            } else {
                                currentPage += 1
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(26.dp))

                    BottomControls(
                        pageCount = onboardingPages.size,
                        currentPage = currentPage,
                        showSkip = currentPage < 4,
                        onPreviousClick = {
                            if (currentPage > 0) {
                                currentPage -= 1
                            }
                        },
                        onNextClick = {
                            if (currentPage < onboardingPages.lastIndex) {
                                currentPage += 1
                            }
                        },
                        onSkipClick = { currentPage = 4 }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (isGenderPickerVisible) {
        AppSelectionSheet(
            title = stringResource(id = R.string.label_select_gender),
            description = stringResource(id = R.string.label_choose_the_gender_label_that_b),
            items = genderItems,
            selectedId = userGender,
            sheetState = genderPickerSheetState,
            onDismiss = { isGenderPickerVisible = false },
            onItemSelected = { selectedGender ->
                userGender = selectedGender
                isGenderPickerVisible = false
            }
        )
    }

    if (isDatePickerVisible) {
        WheelDateTimePickerModal(
            mode = WheelPickerMode.SINGLE_DATE,
            initialStartMillis = if (userDobMillis == 0L) System.currentTimeMillis() else userDobMillis,
            onDismissRequest = { isDatePickerVisible = false },
            onConfirm = { pickedDateMillis, _ ->
                userDobMillis = datePickerSelectionToLocalDateTimestamp(
                    selectedDateMillis = pickedDateMillis,
                    referenceTimestamp = if (userDobMillis == 0L) null else userDobMillis,
                    isInputUtc = false
                )
                isDatePickerVisible = false
            }
        )
    }
}

@Composable
private fun BoxScope.AmbientBackdrop() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        MaterialTheme.colorScheme.background
                    ),
                    center = Offset(0.5f, 0.38f),
                    radius = 1200f
                )
            )
    )
}

@Composable
private fun BottomControls(
    pageCount: Int,
    currentPage: Int,
    showSkip: Boolean = true,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    val isAuthPage = currentPage == 4
    val isGoalPage = currentPage == 5
    val isSetupPage = currentPage == 6

    if (isAuthPage || isSetupPage || isGoalPage) {
        Spacer(modifier = Modifier.height(56.dp)) 
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (currentPage > 0 && !isAuthPage) {
                Text(
                    text = stringResource(id = R.string.label_prev),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onPreviousClick)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }
        }

        PageIndicator(
            pageCount = pageCount,
            currentPage = currentPage,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (showSkip) {
                Text(
                    text = stringResource(id = R.string.label_skip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onSkipClick)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
            } else {
                Text(
                    text = stringResource(id = R.string.label_next_caps),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onNextClick)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PrimaryOnboardingButton(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "onboarding_button_scale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .scale(scale)
            .shadow(
                elevation = 16.dp, // Reduced from 34dp
                shape = RoundedCornerShape(999.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
            ),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = brandGradient(),
                    shape = RoundedCornerShape(999.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val dotScale by animateFloatAsState(
                targetValue = if (selected) 1.2f else 0.85f,
                animationSpec = tween(220),
                label = "onboarding_dot_scale"
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    .alpha(if (selected) 1f else 0.7f)
            )
        }
    }
}

@Composable
private fun BoxScope.ExpenseCardIllustration() {
    FloatingCircle(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 10.dp, end = 10.dp),
        size = 110.dp,
        icon = Icons.Filled.CurrencyBitcoin,
        iconTint = MaterialTheme.colorScheme.secondary
    )

    FloatingCircle(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(top = 124.dp),
        size = 110.dp,
        icon = Icons.Filled.Money,
        iconTint = MaterialTheme.colorScheme.tertiary
    )

    FloatingCircle(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(top = 140.dp, end = 8.dp),
        size = 100.dp,
        icon = Icons.Filled.Savings,
        iconTint = MaterialTheme.colorScheme.onSurface
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(top = 8.dp)
            .fillMaxWidth(0.72f)
            .height(230.dp)
            .graphicsLayer {
                rotationZ = -4f
            }
            .clip(RoundedCornerShape(38.dp))
            .background(
                brush = surfaceGradient()
            )
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.label_expense_tracker_caps),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            ),
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Box(
            modifier = Modifier
                .padding(top = 18.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Box(
                modifier = Modifier
                    .width(82.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = R.string.label_secure_logging),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 2.2.sp,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun BoxScope.SecureTrackerIllustration() {
    FloatingCircle(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(top = 110.dp, start = 10.dp),
        size = 74.dp,
        icon = Icons.Filled.Key,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    )

    FloatingCircle(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 52.dp, end = 54.dp),
        size = 84.dp,
        icon = Icons.Filled.Security,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.82f)
            .height(270.dp)
            .clip(RoundedCornerShape(42.dp))
            .background(
                brush = surfaceGradient()
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(152.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0f))
            )

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(CircleShape)
                    .background(
                        brush = brandGradient()
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.label_encrypted_mode),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.2.sp,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = stringResource(id = R.string.label_system_active),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BoxScope.AnalyticsIllustration() {
    Box(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(start = 12.dp, top = 36.dp)
            .size(90.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .borderGlowCircle(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f), 0.08f)
        )
    }

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.82f)
            .height(300.dp)
            .clip(RoundedCornerShape(40.dp))
            .background(
                brush = surfaceGradient()
            )
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.label_growth_index),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium.copy(
                letterSpacing = 1.4.sp,
                fontSize = 13.sp
            )
        )

        Text(
            text = stringResource(id = R.string.label_248),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            ),
            modifier = Modifier.padding(top = 34.dp)
        )

        Icon(
            imageVector = Icons.Filled.SsidChart,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(88.dp, 144.dp, 64.dp, 122.dp).forEachIndexed { index, height ->
                val isActive = index == 1
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(height)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                        .background(
                            if (isActive) {
                                brandGradient()
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 18.dp, bottom = 22.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = stringResource(id = R.string.label_accuracy),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        letterSpacing = 1.8.sp,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(id = R.string.label_high_fidelity),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BoxScope.PremiumPrivacyIllustration() {
    FloatingCircle(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 44.dp, end = 34.dp),
        size = 76.dp,
        icon = Icons.Filled.Lock,
        iconTint = MaterialTheme.colorScheme.secondary
    )

    FloatingCircle(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 24.dp, top = 86.dp),
        size = 84.dp,
        icon = Icons.Filled.Savings,
        iconTint = MaterialTheme.colorScheme.tertiary
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.64f)
            .height(250.dp)
            .clip(RoundedCornerShape(42.dp))
            .background(
                brush = surfaceGradient()
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(118.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
private fun PremiumBenefitCards() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BenefitCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.CloudOff,
            iconTint = MaterialTheme.colorScheme.secondary,
            title = stringResource(id = R.string.label_architecture),
            value = stringResource(id = R.string.label_100_offline)
        )

        BenefitCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.CheckCircle,
            iconTint = MaterialTheme.colorScheme.tertiary,
            title = stringResource(id = R.string.label_access),
            value = stringResource(id = R.string.label_full_control)
        )
    }
}

@Composable
private fun BenefitCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 2.sp,
                    fontSize = 10.sp
                )
            )

            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 18.sp
                ),
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FloatingCircle(
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    icon: ImageVector,
    iconTint: Color
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(size * 0.34f)
        )
    }
}

@Composable
private fun Modifier.borderGlowCircle(
    color: Color,
    alpha: Float
): Modifier = this.then(
    Modifier
        .rotate(0f)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha),
                    MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                )
            ),
            shape = CircleShape
        )
)

@Composable
private fun BoxScope.GoalIllustration() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(240.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Savings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(100.dp)
        )
    }
}

@Composable
private fun BoxScope.SetupIllustration() {
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(240.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        ProfileAvatar(
            initials = "U",
            size = 200.dp,
            textSize = 54.sp
        )
    }
}

private fun genderToIcon(gender: String, male: String, female: String, nonBinary: String, preferNotToSay: String): ImageVector {
    return when (gender) {
        male -> Icons.Rounded.Male
        female -> Icons.Rounded.Female
        nonBinary -> Icons.Rounded.Transgender
        preferNotToSay -> Icons.Rounded.Person
        else -> Icons.Rounded.Transgender
    }
}

@Preview(
    name = "Onboarding Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun OnboardingScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreen()
    }
}
