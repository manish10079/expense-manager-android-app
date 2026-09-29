package com.mknlabs.expensetracker.feature.auth.ui

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
import android.content.res.Configuration
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Transgender
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalUriHandler
import com.mknlabs.expensetracker.core.ui.theme.disabled
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.models.CURRENT_TERMS_VERSION
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppSelectionSheet
import com.mknlabs.expensetracker.core.ui.components.ProfileAvatar
import com.mknlabs.expensetracker.core.ui.components.UserBadge
import com.mknlabs.expensetracker.core.ui.components.UserBadgeType
import com.mknlabs.expensetracker.core.ui.components.input.InputFieldCard
import com.mknlabs.expensetracker.core.ui.components.input.InputType
import com.mknlabs.expensetracker.core.ui.models.SelectionItem
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.surfaceGradient
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinish: (name: String, gender: String, dobMillis: Long?) -> Unit = { _, _, _ -> },
    onSignUpSuccess: (() -> Unit)? = null,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val returningUserProfile by authViewModel.returningUserProfile.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    OnboardingScreenContent(
        currentUser = currentUser,
        returningUserProfile = returningUserProfile,
        onFinish = onFinish,
        onSignUpSuccess = onSignUpSuccess,
        onCancelGuestSignIn = { authViewModel.cancelGuestSignIn() },
        onResetAuthState = { authViewModel.resetState() },
        onFetchReturningProfile = { uid -> authViewModel.fetchReturningUserProfile(uid) },
        onResetReturningProfile = { authViewModel.resetReturningUserProfile() },
        authSection = { onAuthSuccess, onGuestContinue, onSignUpSuccess ->
            AuthRoute(
                viewModel = authViewModel,
                onAuthSuccess = onAuthSuccess,
                onGuestContinue = onGuestContinue,
                onSignUpSuccess = onSignUpSuccess
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OnboardingScreenContent(
    initialPage: Int = 0,
    currentUser: com.google.firebase.auth.FirebaseUser? = null,
    returningUserProfile: ReturningUserProfile? = null,
    onFinish: (name: String, gender: String, dobMillis: Long?) -> Unit = { _, _, _ -> },
    onSignUpSuccess: (() -> Unit)? = null,
    onCancelGuestSignIn: () -> Unit = {},
    onResetAuthState: () -> Unit = {},
    onFetchReturningProfile: (uid: String) -> Unit = {},
    onResetReturningProfile: () -> Unit = {},
    authSection: (@Composable (onAuthSuccess: () -> Unit, onGuestContinue: () -> Unit, onSignUpSuccess: () -> Unit) -> Unit)? = null
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
            // Page 4: Terms & Conditions Consent Gate (Placed immediately before AuthGate)
            OnboardingPage(
                title = context.getString(R.string.title_terms_conditions),
                description = context.getString(R.string.desc_terms_conditions_requirement),
                actionLabel = context.getString(R.string.label_continue),
                titleFontSize = 28.sp,
                titleLineHeight = 34.sp,
                illustration = { /* No big illustration for legal consent page */ }
            ),
            // Page 5: Secure Your Account / Auth Gate
            OnboardingPage(
                title = context.getString(R.string.title_secure_your_account),
                description = context.getString(R.string.desc_sync_and_premium_features),
                actionLabel = context.getString(R.string.label_next),
                titleFontSize = 34.sp,
                titleLineHeight = 40.sp,
                illustration = { SecureTrackerIllustration() } 
            ),
            // Page 6: Setup Profile
            OnboardingPage(
                title = context.getString(R.string.title_lets_get_started),
                description = context.getString(R.string.desc_tell_us_about_yourself),
                actionLabel = context.getString(R.string.label_get_started),
                illustration = { /* No illustration for setup page */ }
            ),
            // Page 7: WelcomeBack — shown to returning users who already have a complete profile
            OnboardingPage(
                title = "",   // Title rendered separately in WelcomeBackPage composable
                description = "",
                actionLabel = context.getString(R.string.label_continue),
                illustration = { }
            )
        )
    }
    var currentPage by remember { mutableIntStateOf(initialPage) }
    val page = onboardingPages[currentPage]
    val isConsentPage = currentPage == 4
    val isAuthPage = currentPage == 5
    val isSetupPage = currentPage == 6
    val isWelcomeBackPage = currentPage == 7
    val scrollState = rememberScrollState()
    val isKeyboardVisible = WindowInsets.isImeVisible

    // Terms & Conditions consent state
    var isTermsAccepted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Reset scroll position on page change
    LaunchedEffect(currentPage) {
        scrollState.scrollTo(0)
    }

    // Setup state
    var userName by remember { mutableStateOf("") }
    var userGender by remember { mutableStateOf("") }
    var isGenderPickerVisible by remember { mutableStateOf(false) }
    val genderPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val nameFocusRequester = remember { FocusRequester() }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // Auto-fill name if user logged in via social provider
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (!user.isAnonymous && userName.isEmpty()) {
                userName = user.displayName ?: ""
            }
        }
    }

    // Smart routing after auth: fetch the existing Firestore profile and skip any
    // setup steps the returning user has already completed. Keyed on currentPage
    // too so the routing also applies when the user is already signed in and walks
    // back to the auth page (page 5) — not only right after the sign-in tap.
    //
    LaunchedEffect(currentUser, currentPage) {
        if (currentPage != 5) return@LaunchedEffect
        val user = currentUser ?: return@LaunchedEffect
        if (user.isAnonymous) {
            // Anonymous / guest user — go straight to setup profile page
            currentPage = 6
        } else {
            Log.d("Onboarding", "Existing user detected — fetching profile from Firestore.")
            onFetchReturningProfile(user.uid)
        }
    }

    // Once we have the returning profile, decide where to land
    LaunchedEffect(returningUserProfile, currentPage) {
        if (currentPage != 5) return@LaunchedEffect // only act right after auth page
        val profile = returningUserProfile ?: return@LaunchedEffect
        when (resolveReturningUserStep(profile)) {
            ReturningUserStep.WELCOME_BACK -> {
                // Pre-fill local state from existing data so onFinish has correct values
                userName = profile.fullName
                userGender = profile.gender
                Log.d("Onboarding", "Returning user with complete profile — showing WelcomeBack.")
                currentPage = 7
            }
            ReturningUserStep.SETUP_PROFILE -> {
                Log.d("Onboarding", "Returning user — missing name/gender, going to setup page.")
                currentPage = 6
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

    BackHandler(enabled = currentPage > 0) {
        if (currentPage == 7) {
            // Returning from WelcomeBack — reset profile fetch so we don't re-enter
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

    LaunchedEffect(currentPage) {
        scrollState.scrollTo(0)
    }

    val nameStr = stringResource(id = R.string.label_name_capitalized)
    val genderStr = stringResource(id = R.string.label_gender_capitalized)
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
                .imePadding()
                .padding(top = if (isLandscape) 8.dp else Dimens.HeaderSpacing, bottom = if (isLandscape) 8.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Content Area (Weight 1 pushes footer down, max width keeps it centered on tablets)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration Section
                if (!isConsentPage && !isSetupPage && !isAuthPage && !isWelcomeBackPage) {
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
                } else {
                    Spacer(modifier = Modifier.height(if (isConsentPage) 12.dp else if (isLandscape) 16.dp else 48.dp))
                }

                // Title & Description Section
                if (isWelcomeBackPage) {
                    // WelcomeBack takes over the full content area
                    WelcomeBackPage(
                        userName = userName,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        onContinue = {
                            onResetReturningProfile()
                            onCompleteInternal()
                        }
                    )
                } else {
                AnimatedContent(
                    targetState = currentPage,
                    label = "onboarding_copy",
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)
                ) { pageIndex ->
                    val current = onboardingPages[pageIndex]
                    val isConsentOnThisPageIndex = pageIndex == 4
                    val isAuthOnThisPageIndex = pageIndex == 5
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

                            Spacer(modifier = Modifier.height(if (isAuthOnThisPageIndex || isConsentOnThisPageIndex) 16.dp else 32.dp))
                        }

                        if (isConsentOnThisPageIndex) {
                            Text(
                                text = current.description,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                modifier = Modifier.fillMaxWidth(0.92f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TermsAndConditionsConsentContent(
                                isTermsAccepted = isTermsAccepted,
                                onTermsAcceptedChange = { isTermsAccepted = it }
                            )
                        } else if (isAuthOnThisPageIndex) {
                            if (authSection != null) {
                                authSection(
                                    {
                                        Log.d("Onboarding", "Auth SUCCESS callback triggered.")
                                        if (currentUser?.isAnonymous == true) {
                                            currentPage = 6
                                        }
                                    },
                                    {
                                        Log.d("Onboarding", "Guest continue triggered.")
                                        currentPage = 6
                                    },
                                    {
                                        Log.d("Onboarding", "Sign up success callback triggered.")
                                        onSignUpSuccess?.invoke()
                                        if (currentUser?.isAnonymous == true) {
                                            currentPage = 6
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
                        } else if (isSetupOnThisPageIndex) {
                            // Auto-focus the name field when the setup page first appears
                            LaunchedEffect(Unit) {
                                nameFocusRequester.requestFocus()
                            }
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
                                    placeholder = stringResource(id = R.string.placeholder_enter_name),
                                    modifier = Modifier.focusRequester(nameFocusRequester)
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
                } // end AnimatedContent
                } // end if(isWelcomeBackPage) else
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (!isAuthPage && !isWelcomeBackPage) {
                // Fixed Footer Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.ScreenPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryOnboardingButton(
                        label = page.actionLabel,
                        enabled = if (isConsentPage) isTermsAccepted else true,
                        onClick = {
                            when {
                                currentPage == 7 -> { // WelcomeBack Page
                                    onResetReturningProfile()
                                    onCompleteInternal()
                                }
                                currentPage == 6 -> { // Setup Page (name/gender)
                                    val missingFields = mutableListOf<String>()
                                    if (userName.trim().isEmpty()) missingFields.add(nameStr)

                                    if (missingFields.isNotEmpty()) {
                                        toastMessage = msgProvide.replace("%s", missingFields.joinToString(", "))
                                    } else {
                                        // Finish directly — the WelcomeBack page is reserved for
                                        // returning users with a complete cloud profile.
                                        onCompleteInternal()
                                    }
                                }
                                currentPage == 4 -> { // Consent Page
                                    if (isTermsAccepted) {
                                        val consentTimestamp = System.currentTimeMillis()
                                        coroutineScope.launch {
                                            UserProfileDataStore.updateUserProfile(context) { profile ->
                                                profile.copy(
                                                    termsAcceptedAt = if (profile.termsAcceptedAt == 0L) consentTimestamp else profile.termsAcceptedAt,
                                                    termsVersion = if (profile.termsVersion.isBlank()) CURRENT_TERMS_VERSION else profile.termsVersion
                                                )
                                            }
                                        }
                                        currentPage = 5
                                    }
                                }
                                else -> currentPage += 1
                            }
                        }
                    )
                    if (currentPage < 4) {
                        Spacer(modifier = Modifier.height(20.dp))
                        BottomControls(
                            pageCount = 4,
                            currentPage = currentPage,
                            showSkip = true,
                            onPreviousClick = {
                                if (currentPage > 0) {
                                    currentPage -= 1
                                }
                            },
                            onNextClick = {
                                if (currentPage < 3) {
                                    currentPage += 1
                                } else if (currentPage == 3) {
                                    currentPage = 4
                                }
                            },
                            onSkipClick = { currentPage = 4 }
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
                // Clear focus so the keyboard doesn't reappear after the sheet closes
                focusManager.clearFocus()
            },
            onItemSelected = { selectedGender ->
                userGender = selectedGender
                isGenderPickerVisible = false
                // Clear focus so the keyboard doesn't reappear after selection
                focusManager.clearFocus()
            }
        )
    }
}

@Composable
private fun WelcomeBackPage(
    userName: String,
    modifier: Modifier = Modifier,
    onContinue: (() -> Unit)? = null
) {
    val firstName = userName.trim().substringBefore(" ").ifBlank {
        userName.ifBlank { stringResource(R.string.label_welcome_back_fallback) }
    }

    // Staggered entrance animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val waveAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "wave_alpha"
    )
    val greetingOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 24.dp,
        animationSpec = tween(700, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "greeting_offset"
    )
    val greetingAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "greeting_alpha"
    )
    val subAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "sub_alpha"
    )
    val subOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 20.dp,
        animationSpec = tween(700, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "sub_offset"
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, delayMillis = 500, easing = FastOutSlowInEasing),
        label = "button_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenPadding)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Wave emoji
        Text(
            text = "\uD83D\uDC4B",
            fontSize = 56.sp,
            modifier = Modifier.graphicsLayer(alpha = waveAlpha)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // "Welcome back," line
        Text(
            text = stringResource(R.string.label_welcome_back_comma),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            ),
            modifier = Modifier.graphicsLayer(
                alpha = greetingAlpha,
                translationY = greetingOffset.value
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // User's first name, large and bold with gradient
        Text(
            text = firstName,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 40.sp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.accentInk,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
            modifier = Modifier.graphicsLayer(
                alpha = greetingAlpha,
                translationY = greetingOffset.value
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Sub-message
        Text(
            text = stringResource(R.string.msg_welcome_back_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 26.sp
            ),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .graphicsLayer(
                    alpha = subAlpha,
                    translationY = subOffset.value
                )
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (onContinue != null) {
            Box(modifier = Modifier.graphicsLayer(alpha = buttonAlpha)) {
                PrimaryOnboardingButton(
                    label = stringResource(R.string.label_continue),
                    onClick = onContinue
                )
            }
        }
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
                        MaterialTheme.colorScheme.accentInk.copy(alpha = 0.18f),
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
    val isConsentPage = currentPage == 4
    val isAuthPage = currentPage == 5
    val isSetupPage = currentPage == 6
    val isWelcomeBackPage = currentPage == 7

    if (isAuthPage || isSetupPage || isWelcomeBackPage) {
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
                        fontSize = 11.sp
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
                        fontSize = 11.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onSkipClick)
                        .padding(vertical = 12.dp, horizontal = 8.dp)
                )
            } else {
                Text(
                    text = stringResource(id = R.string.label_next_caps),
                    color = MaterialTheme.colorScheme.accentInk,
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
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "onboarding_button_scale"
    )

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val buttonHeight = if (isLandscape) 56.dp else 74.dp

    val buttonModifier = if (enabled) {
        Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(999.dp),
                ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f)
            )
    } else {
        Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .scale(scale)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = buttonModifier,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onCta,
            disabledContentColor = MaterialTheme.colorScheme.disabled
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) brandGradient() else Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    ),
                    shape = RoundedCornerShape(999.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (enabled) MaterialTheme.colorScheme.onCta else MaterialTheme.colorScheme.disabled,
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
                        if (selected) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.outline
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
                    tint = MaterialTheme.colorScheme.onCta,
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
                    ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.26f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                )
                .clip(CircleShape)
                .background(brush = brandGradient()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onCta,
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
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                )
            )
        }

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 16.sp
            ),
            textAlign = TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
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
        ProfileAvatar()
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

@Composable
private fun TermsAndConditionsConsentContent(
    isTermsAccepted: Boolean,
    onTermsAcceptedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isWideScreen = configuration.screenWidthDp >= 600
    val termsUrl = stringResource(R.string.url_terms_conditions)
    val privacyUrl = stringResource(R.string.url_privacy_policy)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bullet highlights (Adaptive 2-column layout on wide screens/tablets, single column on phones)
        if (isWideScreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConsentHighlightCard(
                    icon = Icons.Filled.PhoneAndroid,
                    title = stringResource(R.string.label_bullet_app_usage_title),
                    description = stringResource(R.string.label_bullet_app_usage_desc),
                    modifier = Modifier.weight(1f)
                )
                ConsentHighlightCard(
                    icon = Icons.Filled.Security,
                    title = stringResource(R.string.label_bullet_privacy_title),
                    description = stringResource(R.string.label_bullet_privacy_desc),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConsentHighlightCard(
                    icon = Icons.Filled.Star,
                    title = stringResource(R.string.label_bullet_subscriptions_title),
                    description = stringResource(R.string.label_bullet_subscriptions_desc),
                    modifier = Modifier.weight(1f)
                )
                ConsentHighlightCard(
                    icon = Icons.Filled.Refresh,
                    title = stringResource(R.string.label_bullet_cancellation_title),
                    description = stringResource(R.string.label_bullet_cancellation_desc),
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConsentHighlightCard(
                    icon = Icons.Filled.PhoneAndroid,
                    title = stringResource(R.string.label_bullet_app_usage_title),
                    description = stringResource(R.string.label_bullet_app_usage_desc)
                )
                ConsentHighlightCard(
                    icon = Icons.Filled.Security,
                    title = stringResource(R.string.label_bullet_privacy_title),
                    description = stringResource(R.string.label_bullet_privacy_desc)
                )
                ConsentHighlightCard(
                    icon = Icons.Filled.Star,
                    title = stringResource(R.string.label_bullet_subscriptions_title),
                    description = stringResource(R.string.label_bullet_subscriptions_desc)
                )
                ConsentHighlightCard(
                    icon = Icons.Filled.Refresh,
                    title = stringResource(R.string.label_bullet_cancellation_title),
                    description = stringResource(R.string.label_bullet_cancellation_desc)
                )
            }
        }

        // Scrollable Terms Pane
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                text = stringResource(R.string.title_legal),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.accentInk
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = if (isLandscape) 90.dp else 130.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_terms_clause_1),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.label_terms_clause_2),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.label_terms_clause_3),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.label_terms_clause_4),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.label_terms_clause_5),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Links Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.title_terms_conditions),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.accentInk,
                modifier = Modifier.clickable {
                    try {
                        uriHandler.openUri(termsUrl)
                    } catch (_: Exception) {}
                }
            )
            Text(
                text = "  •  ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = stringResource(R.string.title_privacy_policy),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.accentInk,
                modifier = Modifier.clickable {
                    try {
                        uriHandler.openUri(privacyUrl)
                    } catch (_: Exception) {}
                }
            )
        }

        // Interactive Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onTermsAcceptedChange(!isTermsAccepted) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isTermsAccepted,
                onCheckedChange = onTermsAcceptedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.accentInk,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.label_agree_terms_checkbox),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ConsentHighlightCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.accentInk,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSE PREVIEWS — ALL ONBOARDING SCREENS (DARK, LIGHT, LANDSCAPE, TABLET)
// ═══════════════════════════════════════════════════════════════════════════════

// ── Screen 1: Track Expenses Easily ──────────────────────────────────────────
@Preview(
    name = "1. Track Expenses - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage1_TrackExpenses_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 0)
    }
}

@Preview(
    name = "1. Track Expenses - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage1_TrackExpenses_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 0)
    }
}

// ── Screen 2: Secure & Private ───────────────────────────────────────────────
@Preview(
    name = "2. Secure & Private - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage2_SecurePrivate_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 1)
    }
}

@Preview(
    name = "2. Secure & Private - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage2_SecurePrivate_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 1)
    }
}

// ── Screen 3: Visual Analytics ───────────────────────────────────────────────
@Preview(
    name = "3. Visual Analytics - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage3_VisualAnalytics_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 2)
    }
}

@Preview(
    name = "3. Visual Analytics - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage3_VisualAnalytics_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 2)
    }
}

// ── Screen 4: Premium by Design, Private by Nature ───────────────────────────
@Preview(
    name = "4. Premium & Privacy - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage4_PremiumPrivacy_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 3)
    }
}

@Preview(
    name = "4. Premium & Privacy - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage4_PremiumPrivacy_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 3)
    }
}

// ── Screen 5: Terms & Conditions Consent Gate ────────────────────────────────
@Preview(
    name = "5. Terms & Conditions - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage5_TermsConditions_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 4)
    }
}

@Preview(
    name = "5. Terms & Conditions - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage5_TermsConditions_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 4)
    }
}

@Preview(
    name = "5. Terms & Conditions - Landscape",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=915dp,height=412dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage5_TermsConditions_Landscape() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 4)
    }
}

@Preview(
    name = "5. Terms & Conditions - Tablet",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
@Composable
private fun PreviewOnboardingPage5_TermsConditions_Tablet() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 4)
    }
}

// ── Screen 6: Secure Your Account / Auth Gate ────────────────────────────────
@Preview(
    name = "6. Auth Gate - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage6_Auth_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 5)
    }
}

@Preview(
    name = "6. Auth Gate - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage6_Auth_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 5)
    }
}

// ── Screen 7: Profile Setup ──────────────────────────────────────────────────
@Preview(
    name = "7. Profile Setup - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage7_ProfileSetup_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(initialPage = 6)
    }
}

@Preview(
    name = "7. Profile Setup - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage7_ProfileSetup_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(initialPage = 6)
    }
}

// ── Screen 8: Welcome Back (Returning Cloud User) ────────────────────────────
@Preview(
    name = "8. Welcome Back - Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage8_WelcomeBack_Dark() {
    ExpenseTrackerTheme(darkTheme = true) {
        OnboardingScreenContent(
            initialPage = 7,
            returningUserProfile = ReturningUserProfile(
                fullName = "Alex Morgan",
                gender = "Non-Binary"
            )
        )
    }
}

@Preview(
    name = "8. Welcome Back - Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun PreviewOnboardingPage8_WelcomeBack_Light() {
    ExpenseTrackerTheme(darkTheme = false) {
        OnboardingScreenContent(
            initialPage = 7,
            returningUserProfile = ReturningUserProfile(
                fullName = "Alex Morgan",
                gender = "Non-Binary"
            )
        )
    }
}
