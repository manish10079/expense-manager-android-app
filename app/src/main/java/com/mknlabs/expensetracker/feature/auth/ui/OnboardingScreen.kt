package com.mknlabs.expensetracker.feature.auth.ui

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppSelectionSheet
import com.mknlabs.expensetracker.core.ui.models.SelectionItem
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.data.constants.SYSTEM_LANGUAGE_CODE
import com.mknlabs.expensetracker.feature.settings.ui.PreferencesViewModel
import com.mknlabs.expensetracker.utils.AppLocaleHelper
import com.mknlabs.expensetracker.utils.findFragmentActivity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinish: (name: String, gender: String, dobMillis: Long?) -> Unit = { _, _, _ -> },
    onSignUpSuccess: (() -> Unit)? = null,
    authViewModel: AuthViewModel = hiltViewModel(),
    preferencesViewModel: PreferencesViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val returningUserProfile by authViewModel.returningUserProfile.collectAsStateWithLifecycle()
    val preferencesUiState by preferencesViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findFragmentActivity()

    OnboardingScreenContent(
        currentUser = currentUser,
        returningUserProfile = returningUserProfile,
        onFinish = onFinish,
        onSignUpSuccess = onSignUpSuccess,
        onCancelGuestSignIn = { authViewModel.cancelGuestSignIn() },
        onResetAuthState = { authViewModel.resetState() },
        onFetchReturningProfile = { uid -> authViewModel.fetchReturningUserProfile(uid) },
        onResetReturningProfile = { authViewModel.resetReturningUserProfile() },
        initialLanguageCode = preferencesUiState.selectedLanguageCode,
        onApplyLanguage = { code, onApplied ->
            preferencesViewModel.selectLanguage(code, activity, onApplied)
        },
        authSection = { onAuthSuccess, onGuestContinue, onSignUpSuccessInner ->
            AuthRoute(
                viewModel = authViewModel,
                onAuthSuccess = onAuthSuccess,
                onGuestContinue = onGuestContinue,
                onSignUpSuccess = onSignUpSuccessInner
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun OnboardingScreenContent(
    initialPage: Int = 0,
    initialLanguageCode: String = SYSTEM_LANGUAGE_CODE,
    currentUser: com.google.firebase.auth.FirebaseUser? = null,
    returningUserProfile: ReturningUserProfile? = null,
    onFinish: (name: String, gender: String, dobMillis: Long?) -> Unit = { _, _, _ -> },
    onSignUpSuccess: (() -> Unit)? = null,
    onCancelGuestSignIn: () -> Unit = {},
    onResetAuthState: () -> Unit = {},
    onFetchReturningProfile: (uid: String) -> Unit = {},
    onResetReturningProfile: () -> Unit = {},
    onApplyLanguage: (String, () -> Unit) -> Unit = { _, onApplied -> onApplied() },
    authSection: (@Composable (onAuthSuccess: () -> Unit, onGuestContinue: () -> Unit, onSignUpSuccess: () -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    AppLocaleHelper.appliedLanguageTags
    val onboardingPages = rememberOnboardingPages()
    var currentPage by rememberSaveable { mutableIntStateOf(initialPage) }
    val page = onboardingPages[currentPage]
    val isLanguagePage = currentPage == PAGE_LANGUAGE
    val isAuthPage = currentPage == PAGE_AUTH
    val isSetupPage = currentPage == PAGE_SETUP
    val isWelcomeBackPage = currentPage == PAGE_WELCOME_BACK
    val isPromoPage = currentPage == PAGE_PROMO
    val scrollState = rememberScrollState()
    WindowInsets.isImeVisible

    LaunchedEffect(currentPage) {
        scrollState.scrollTo(0)
    }

    var selectedLanguageCode by remember { mutableStateOf(initialLanguageCode) }
    LaunchedEffect(initialLanguageCode) {
        selectedLanguageCode = initialLanguageCode
    }
    var userName by remember { mutableStateOf("") }
    var userGender by remember { mutableStateOf("") }
    var isGenderPickerVisible by remember { mutableStateOf(false) }
    val genderPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val nameFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (!user.isAnonymous && userName.isEmpty()) {
                userName = user.displayName ?: ""
            }
        }
    }

    LaunchedEffect(currentUser, currentPage) {
        if (currentPage != PAGE_AUTH) return@LaunchedEffect
        val user = currentUser ?: return@LaunchedEffect
        if (user.isAnonymous) {
            currentPage = PAGE_SETUP
        } else {
            Log.d("Onboarding", "Existing user detected — fetching profile from Firestore.")
            onFetchReturningProfile(user.uid)
        }
    }

    LaunchedEffect(returningUserProfile, currentPage) {
        if (currentPage != PAGE_AUTH) return@LaunchedEffect
        val profile = returningUserProfile ?: return@LaunchedEffect
        when (resolveReturningUserStep(profile)) {
            ReturningUserStep.WELCOME_BACK -> {
                userName = profile.fullName
                userGender = profile.gender
                Log.d("Onboarding", "Returning user with complete profile — showing WelcomeBack.")
                currentPage = PAGE_WELCOME_BACK
            }
            ReturningUserStep.SETUP_PROFILE -> {
                Log.d("Onboarding", "Returning user — missing name/gender, going to setup page.")
                currentPage = PAGE_SETUP
            }
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

    val onCompleteInternal: () -> Unit = {
        onFinish(userName, userGender, null)
    }
    var promoReturnPage by rememberSaveable { mutableIntStateOf(PAGE_WELCOME_BACK) }
    val finishOnboarding: () -> Unit = {
        onResetReturningProfile()
        onCompleteInternal()
    }
    val openPromoOrFinish: (Int) -> Unit = { fromPage ->
        if (shouldOfferOnboardingPromo(returningUserProfile, System.currentTimeMillis())) {
            promoReturnPage = fromPage
            currentPage = PAGE_PROMO
        } else {
            finishOnboarding()
        }
    }

    BackHandler(enabled = currentPage > 0) {
        if (currentPage == PAGE_PROMO) {
            currentPage = promoReturnPage
            return@BackHandler
        }
        if (currentPage == PAGE_WELCOME_BACK) {
            onResetReturningProfile()
        }
        onCancelGuestSignIn()
        onResetAuthState()
        currentPage -= 1
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeightDp = configuration.screenHeightDp.dp

    val illustrationHeight = when {
        isLandscape -> 150.dp
        screenHeightDp < 680.dp -> 210.dp
        screenHeightDp < 800.dp -> 260.dp
        else -> 320.dp
    }
    val illustrationScale = when {
        isLandscape -> 0.68f
        screenHeightDp < 680.dp -> 0.78f
        screenHeightDp < 800.dp -> 0.88f
        else -> 1f
    }

    val nameStr = stringResource(id = R.string.label_name_capitalized)
    val msgProvide = stringResource(id = R.string.msg_please_provide_your_val, "%s")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
    ) {
        AmbientBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isWelcomeBackPage) Modifier
                    else Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(
                            top = if (isLandscape) 8.dp else Dimens.HeaderSpacing,
                            bottom = if (isLandscape) 8.dp else 16.dp,
                        )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .then(if (isLanguagePage || isWelcomeBackPage) Modifier else Modifier.verticalScroll(scrollState)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isSetupPage && !isAuthPage && !isWelcomeBackPage && !isPromoPage && !isLanguagePage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(illustrationHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = currentPage,
                            label = "onboarding_illustration"
                        ) { pageIndex ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(illustrationHeight)
                                    .scale(illustrationScale)
                            ) {
                                onboardingPages[pageIndex].illustration(this)
                            }
                        }
                    }
                } else if (!isWelcomeBackPage) {
                    Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 48.dp))
                }

                if (isPromoPage) {
                    Spacer(modifier = Modifier.weight(1f))
                    OnboardingPromoPage(onFinished = finishOnboarding)
                } else if (isWelcomeBackPage) {
                    WelcomeBackPage(
                        userName = userName,
                        userEmail = currentUser?.email.orEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        onContinue = { openPromoOrFinish(PAGE_WELCOME_BACK) }
                    )
                } else if (isLanguagePage) {
                    OnboardingLanguagePage(
                        selectedCode = selectedLanguageCode,
                        onSelect = { selectedLanguageCode = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.ScreenPadding)
                    )
                } else {
                    AnimatedContent(
                        targetState = currentPage,
                        label = "onboarding_copy",
                        modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                    ) { pageIndex ->
                        val current = onboardingPages[pageIndex]
                        val isAuthOnThisPageIndex = pageIndex == PAGE_AUTH
                        val isSetupOnThisPageIndex = pageIndex == PAGE_SETUP

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isSetupOnThisPageIndex) {
                                OnboardingSetupPage(
                                    title = current.title,
                                    titleFontSize = current.titleFontSize,
                                    titleLineHeight = current.titleLineHeight,
                                    isAnonymous = currentUser?.isAnonymous ?: true,
                                    userName = userName,
                                    onUserNameChange = { userName = it },
                                    userGender = userGender,
                                    maleLabel = maleLabel,
                                    femaleLabel = femaleLabel,
                                    nonBinaryLabel = nonBinaryLabel,
                                    preferNotToSayLabel = preferNotToSayLabel,
                                    nameFocusRequester = nameFocusRequester,
                                    onPrivacyInfoClick = {
                                        toastMessage = context.getString(R.string.msg_privacy_info)
                                    },
                                    onGenderClick = { isGenderPickerVisible = true }
                                )
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

                                Spacer(modifier = Modifier.height(if (isAuthOnThisPageIndex) 16.dp else 32.dp))
                            }

                            if (isAuthOnThisPageIndex) {
                                if (authSection != null) {
                                    authSection(
                                        {
                                            Log.d("Onboarding", "Auth SUCCESS callback triggered.")
                                            if (currentUser?.isAnonymous == true) {
                                                currentPage = PAGE_SETUP
                                            }
                                        },
                                        {
                                            Log.d("Onboarding", "Guest continue triggered.")
                                            currentPage = PAGE_SETUP
                                        },
                                        {
                                            Log.d("Onboarding", "Sign up success callback triggered.")
                                            onSignUpSuccess?.invoke()
                                            if (currentUser?.isAnonymous == true) {
                                                currentPage = PAGE_SETUP
                                            }
                                        }
                                    )
                                } else {
                                    Text(
                                        text = stringResource(id = R.string.msg_auth_content_preview_placeholder),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else if (!isSetupOnThisPageIndex) {
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
                }

                if (!isWelcomeBackPage) {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (!isAuthPage && !isWelcomeBackPage && !isPromoPage) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryOnboardingButton(
                        label = page.actionLabel,
                        enabled = true,
                        compact = isLanguagePage,
                        onClick = {
                            when {
                                currentPage == PAGE_WELCOME_BACK -> {
                                    onResetReturningProfile()
                                    onCompleteInternal()
                                }
                                currentPage == PAGE_SETUP -> {
                                    val missingFields = mutableListOf<String>()
                                    if (userName.trim().isEmpty()) missingFields.add(nameStr)

                                    if (missingFields.isNotEmpty()) {
                                        toastMessage = msgProvide.replace("%s", missingFields.joinToString(", "))
                                    } else if (currentUser != null && !currentUser.isAnonymous) {
                                        openPromoOrFinish(PAGE_SETUP)
                                    } else {
                                        onCompleteInternal()
                                    }
                                }
                                currentPage == PAGE_LANGUAGE -> {
                                    currentPage = PAGE_TRACK
                                    onApplyLanguage(selectedLanguageCode) {}
                                }
                                else -> currentPage += 1
                            }
                        }
                    )
                    if (currentPage in MARKETING_FIRST..MARKETING_LAST) {
                        Spacer(modifier = Modifier.height(20.dp))
                        BottomControls(
                            pageCount = 4,
                            currentPage = currentPage - 1,
                            showSkip = true,
                            onPreviousClick = {
                                if (currentPage > 0) {
                                    currentPage -= 1
                                }
                            },
                            onNextClick = {
                                if (currentPage < MARKETING_LAST) {
                                    currentPage += 1
                                } else {
                                    currentPage = PAGE_AUTH
                                }
                            },
                            onSkipClick = { currentPage = PAGE_AUTH }
                        )
                    }
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
            onDismiss = {
                isGenderPickerVisible = false
                focusManager.clearFocus()
            },
            onItemSelected = { selectedGender ->
                userGender = selectedGender
                isGenderPickerVisible = false
                focusManager.clearFocus()
            }
        )
    }
}
