package com.mknlabs.expensetracker.feature.profile.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DOB_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.models.avatarInitials
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.core.ui.components.*
import com.mknlabs.expensetracker.core.ui.components.input.InputFieldCard
import com.mknlabs.expensetracker.core.ui.components.input.InputType
import com.mknlabs.expensetracker.core.ui.models.SelectionItem
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.CardShape16
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.profileBadgeDate
import com.mknlabs.expensetracker.core.ui.theme.profileBadgeEmail
import com.mknlabs.expensetracker.core.ui.theme.profileBadgeGender
import com.mknlabs.expensetracker.core.ui.theme.profileBadgeName
import com.mknlabs.expensetracker.core.ui.theme.profileBadgePhone
import com.mknlabs.expensetracker.core.ui.theme.profilePhotoDelete
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.genderRowSelected
import com.mknlabs.expensetracker.core.ui.theme.profileCard
import com.mknlabs.expensetracker.core.ui.theme.profilePhotoEdit
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.EnvelopeSimple
import com.adamglin.phosphoricons.regular.GenderFemale
import com.adamglin.phosphoricons.regular.GenderMale
import com.adamglin.phosphoricons.regular.GenderNeuter
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Phone
import com.adamglin.phosphoricons.regular.SealCheck
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.User
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.ProfilePhotoManager
import androidx.compose.material.icons.rounded.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.feature.auth.ui.AuthViewModel
import com.mknlabs.expensetracker.feature.auth.ui.UpdateEmailUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.SolidColor

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.focus.onFocusChanged

private const val PROFILE_PHOTO_MIME_TYPE = "image/*"

// The field badges are 12dp rounded squares in the mockup, not the shared accent circles the
// rest of the app's input fields wear.
private val ProfileBadgeShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onBackClick: () -> Unit = {},
    onSaveClick: (UserProfile) -> Unit = {},
    onPrepareForExternalActivity: () -> Unit = {},
    isAdsEnabled: Boolean = false,
    onEmailUpdateSuccess: () -> Unit = {}
) {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val userTier by monetizationViewModel.userTier.collectAsStateWithLifecycle()

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val dbCountryCodes by profileViewModel.countryCodes.collectAsStateWithLifecycle()

    val authViewModel: AuthViewModel = hiltViewModel()
    val updateEmailUiState by authViewModel.updateEmailState.collectAsStateWithLifecycle()

    LaunchedEffect(updateEmailUiState) {
        if (updateEmailUiState is UpdateEmailUiState.Success) {
            onEmailUpdateSuccess()
            authViewModel.resetUpdateEmailState()
        }
    }

    ProfileScreenContent(
        userProfile = userProfile,
        isAdsEnabled = isAdsEnabled,
        userTier = userTier,
        dbCountryCodes = dbCountryCodes,
        updateEmailUiState = updateEmailUiState,
        onBackClick = onBackClick,
        onSaveClick = onSaveClick,
        onPrepareForExternalActivity = onPrepareForExternalActivity,
        onUpdateEmailInitiate = { newEmail, password -> authViewModel.initiateEmailUpdate(newEmail, password) },
        onUpdateEmailCheckStatus = { authViewModel.checkEmailUpdateVerification() },
        onUpdateEmailResend = { newEmail -> authViewModel.resendEmailUpdateVerification(newEmail) },
        onUpdateEmailReset = { authViewModel.resetUpdateEmailState() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreenContent(
    userProfile: UserProfile,
    isAdsEnabled: Boolean,
    userTier: UserTier,
    dbCountryCodes: List<com.mknlabs.expensetracker.models.CountryCode>,
    updateEmailUiState: UpdateEmailUiState,
    onBackClick: () -> Unit,
    onSaveClick: (UserProfile) -> Unit,
    onPrepareForExternalActivity: () -> Unit,
    onUpdateEmailInitiate: (String, String) -> Unit,
    onUpdateEmailCheckStatus: () -> Unit,
    onUpdateEmailResend: (String) -> Unit,
    onUpdateEmailReset: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val initialPhotoUri = userProfile.photoUri
    val selectGenderPlaceholder = stringResource(id = R.string.placeholder_select_gender)
    val guestUserPlaceholder = stringResource(id = R.string.placeholder_guest_user)
    val unableToLoadMsg = stringResource(id = R.string.msg_unable_to_load_photo)

    var selectedCountryCode by rememberSaveable { mutableStateOf("+91") }
    var localPhoneNumber by rememberSaveable { mutableStateOf("") }
    var isCountryPickerVisible by rememberSaveable { mutableStateOf(false) }
    var countrySearchQuery by rememberSaveable { mutableStateOf("") }
    var lastParsedPhone by remember { mutableStateOf<String?>(null) }
    var hasAppliedDbCountryCodes by remember { mutableStateOf(false) }

    var fullName by rememberSaveable(userProfile) { mutableStateOf(userProfile.fullName) }
    var dateOfBirthMillis by rememberSaveable(userProfile) { mutableStateOf(userProfile.dateOfBirthMillis) }
    var gender by rememberSaveable(userProfile) {
        mutableStateOf(userProfile.gender.takeUnless { it == "Select Gender" }.orEmpty())
    }
    var photoUri by rememberSaveable(userProfile) { mutableStateOf(userProfile.photoUri) }
    var isGenderPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isPhotoProcessing by remember { mutableStateOf(false) }

    // Explicitly sync state if userProfile changes or database country codes load
    LaunchedEffect(userProfile, dbCountryCodes) {
        fullName = userProfile.fullName
        dateOfBirthMillis = userProfile.dateOfBirthMillis
        gender = userProfile.gender.takeUnless { it == "Select Gender" }.orEmpty()
        photoUri = userProfile.photoUri

        if (lastParsedPhone != userProfile.phoneNumber || (dbCountryCodes.isNotEmpty() && !hasAppliedDbCountryCodes)) {
            val (code, number) = parsePhoneNumber(userProfile.phoneNumber, dbCountryCodes)
            selectedCountryCode = code
            localPhoneNumber = number
            lastParsedPhone = userProfile.phoneNumber
            if (dbCountryCodes.isNotEmpty()) {
                hasAppliedDbCountryCodes = true
            }
        }
    }

    val maleLabel = stringResource(id = R.string.label_male)
    val femaleLabel = stringResource(id = R.string.label_female)
    val nonBinaryLabel = stringResource(id = R.string.label_non_binary)
    val preferNotToSayLabel = stringResource(id = R.string.label_prefer_not_to_say)

    val genderItems = remember(maleLabel, femaleLabel, nonBinaryLabel, preferNotToSayLabel) {
        listOf(
            SelectionItem(id = "Male", title = maleLabel, leadingIcon = PhosphorIcons.Regular.GenderMale),
            SelectionItem(id = "Female", title = femaleLabel, leadingIcon = PhosphorIcons.Regular.GenderFemale),
            SelectionItem(id = "Non-binary", title = nonBinaryLabel, leadingIcon = PhosphorIcons.Regular.GenderNeuter),
            SelectionItem(id = "Prefer not to say", title = preferNotToSayLabel, leadingIcon = PhosphorIcons.Regular.User)
        )
    }

    val displayGender = remember(gender, maleLabel, femaleLabel, nonBinaryLabel, preferNotToSayLabel) {
        when (gender) {
            "Male" -> maleLabel
            "Female" -> femaleLabel
            "Non-binary" -> nonBinaryLabel
            "Prefer not to say" -> preferNotToSayLabel
            else -> gender
        }
    }

    var pendingSaveProfile by remember { mutableStateOf<UserProfile?>(null) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (pendingSaveProfile != null) 0f else 1f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        finishedListener = { value ->
            val saved = pendingSaveProfile
            if (saved != null && value == 0f) {
                onSaveClick(saved)
            }
        },
        label = "editProfileSaveFade"
    )

    val genderPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { selectedUri ->
        if (selectedUri == null) {
            return@rememberLauncherForActivityResult
        }

        coroutineScope.launch {
            isPhotoProcessing = true
            val previousUnsavedPhotoUri = photoUri.takeIf { it != initialPhotoUri }
            val savedUri = ProfilePhotoManager.localizePhoto(
                context = context,
                sourceUri = selectedUri
            )
            if (savedUri != null) {
                previousUnsavedPhotoUri?.let { ProfilePhotoManager.deleteManagedPhoto(it) }
                photoUri = savedUri
            } else {
                Toast.makeText(
                    context,
                    unableToLoadMsg,
                    Toast.LENGTH_SHORT
                ).show()
            }
            isPhotoProcessing = false
        }
    }

    // Each field's badge carries its own hue in the mockup rather than one brand tint: blue
    // for email, purple for the name, emerald for the phone, amber for the date, cyan for the
    // gender. The theme picks the pair, so light and dark never cross inks and washes.
    val colorScheme = MaterialTheme.colorScheme
    val emailBadge = colorScheme.profileBadgeEmail
    val nameBadge = colorScheme.profileBadgeName
    val phoneBadge = colorScheme.profileBadgePhone
    val dateBadge = colorScheme.profileBadgeDate
    val genderBadge = colorScheme.profileBadgeGender

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .alpha(contentAlpha)
    ) {
        val enter = rememberSectionEnterAlphas(2)
        AppHeader(
            title = stringResource(id = R.string.title_edit_profile),
            onBackClick = onBackClick,
            modifier = Modifier.padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding).alpha(enter[0])
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, bottom = 22.dp)
                .alpha(enter[1]),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfilePhotoSection(
                gender = gender,
                photoUri = photoUri,
                isPhotoProcessing = isPhotoProcessing,
                onSelectPhoto = { photoPickerLauncher.launch(PROFILE_PHOTO_MIME_TYPE) },
                onRemovePhoto = {
                    photoUri
                        ?.takeIf { it != initialPhotoUri }
                        ?.let(ProfilePhotoManager::deleteManagedPhoto)
                    photoUri = null
                },
                onPrepareForExternalActivity = onPrepareForExternalActivity,
                userTier = userTier,
                isAnonymous = userProfile.authProvider.isBlank() || userProfile.authProvider == "anonymous"
            )

            // Trimmed so the photo row sits nearer the first card: with no ad slot between
            // them the two spacers are what the eye reads as one gap, and 24 + 18 left the
            // email card pushed a good way down the screen.
            Spacer(modifier = Modifier.height(14.dp))

            AdContainer(isAdsEnabled = isAdsEnabled) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (userProfile.emailAddress.isNotBlank()) {
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val isGoogleAccount = firebaseUser?.providerData?.any { it.providerId == "google.com" } == true
                val isEmailVerified = firebaseUser?.isEmailVerified == true || isGoogleAccount
                var showVerificationSheet by remember { mutableStateOf(false) }
                var showUpdateEmailSheet by remember { mutableStateOf(false) }
                var emailVerifiedState by remember { mutableStateOf(isEmailVerified) }

                // Refresh verification state when returning from bottom sheet
                LaunchedEffect(showVerificationSheet) {
                    if (!showVerificationSheet && firebaseUser != null) {
                        try {
                            firebaseUser.reload().await()
                            emailVerifiedState = firebaseUser.isEmailVerified || isGoogleAccount
                        } catch (_: Exception) { }
                    }
                }

                // Lifecycle-aware: check email verification when returning from email client
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME && showUpdateEmailSheet) {
                            onUpdateEmailCheckStatus()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // The card that groups the account fields. It is chrome rather than a
                // message, so light takes the shared white card; dark keeps the surface, the
                // quarter-strength outline and the 8dp lift it has always had.
                AppCard(
                    // The mockup's card spec: a 16dp corner on the Obsidian card colour in dark
                    // and on white in light, in place of the app's own 28dp dark card.
                    shape = CardShape16,
                    colors = AppCardDefaults.colors(
                        darkContainer = colorScheme.profileCard,
                        darkBorder = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ),
                    elevation = if (MaterialTheme.colorScheme.isDark) 8.dp
                    else AppCardDefaults.Elevation
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        InputFieldCard(
                            title = stringResource(id = R.string.label_email_address_caps),
                            value = userProfile.emailAddress,
                            onValueChange = {},
                            inputType = InputType.Text,
                            leadingIcon = PhosphorIcons.Regular.EnvelopeSimple,
                            leadingIconTint = emailBadge.ink,
                            leadingIconBackground = emailBadge.wash,
                            leadingIconShape = ProfileBadgeShape,
                            cardShape = CardShape16,
                            cardContainer = colorScheme.profileCard,
                            placeholder = "",
                            isEnabled = false,
                            trailingContent = if (emailVerifiedState) {
                                {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.SealCheck,
                                        contentDescription = stringResource(id = R.string.content_desc_email_verified),
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.accentInk
                                    )
                                }
                            } else null
                        )

                        HorizontalDivider(
                            // The rule still separates the two fields on the card; in light it
                            // is the spec's hairline instead of a fifth-strength wash that
                            // disappeared into the white.
                            color = if (MaterialTheme.colorScheme.isDark) {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )

                        if (isGoogleAccount) {
                            // Google auth → show signed-in label (no email edit)
                            Text(
                                text = stringResource(id = R.string.label_signed_in_with_google),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                textAlign = TextAlign.Center
                            )
                        } else if (!emailVerifiedState) {
                            // Email not verified → Verify Email button
                            AppTextButton(
                                onClick = {
                                    firebaseUser?.sendEmailVerification()
                                    showVerificationSheet = true
                                    Toast.makeText(context, context.getString(R.string.toast_verification_email_sent), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.EnvelopeSimple,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.btn_verify_email),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        } else {
                            // Email verified (non-Google) → Update Email button
                            AppTextButton(
                                onClick = { showUpdateEmailSheet = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.PencilSimple,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.btn_update_email),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                if (showVerificationSheet) {
                    com.mknlabs.expensetracker.feature.auth.ui.VerificationBottomSheet(
                        email = userProfile.emailAddress,
                        onDismiss = { showVerificationSheet = false }
                    )
                }

                if (showUpdateEmailSheet) {
                    UpdateEmailBottomSheet(
                        currentEmail = userProfile.emailAddress,
                        uiState = updateEmailUiState,
                        onInitiateUpdate = onUpdateEmailInitiate,
                        onCheckStatus = onUpdateEmailCheckStatus,
                        onResend = onUpdateEmailResend,
                        onDismiss = {
                            showUpdateEmailSheet = false
                            onUpdateEmailReset()
                        },
                        onReset = onUpdateEmailReset
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            InputFieldCard(
                title = stringResource(id = R.string.label_full_name_caps),
                value = fullName,
                onValueChange = { fullName = it },
                inputType = InputType.Text,
                leadingIcon = PhosphorIcons.Regular.User,
                leadingIconTint = nameBadge.ink,
                leadingIconBackground = nameBadge.wash,
                leadingIconShape = ProfileBadgeShape,
                cardShape = CardShape16,
                cardContainer = colorScheme.profileCard,
                placeholder = stringResource(id = R.string.placeholder_guest_user)
            )

            Spacer(modifier = Modifier.height(18.dp))

            PhoneInputFieldCard(
                title = stringResource(id = R.string.label_phone_number_caps),
                phoneNumber = localPhoneNumber,
                onPhoneNumberChange = { localPhoneNumber = it },
                selectedCountryCode = selectedCountryCode,
                onCountryCodeClick = { isCountryPickerVisible = true },
                placeholder = stringResource(id = R.string.placeholder_phone_example),
                cardShape = CardShape16,
                cardContainer = colorScheme.profileCard
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputFieldCard(
                title = stringResource(id = R.string.label_date_of_birth_caps),
                value = dateOfBirthMillis?.let { formatDate(it, DOB_DATE_FORMAT_PATTERN) }.orEmpty(),
                onValueChange = {},
                inputType = InputType.Date,
                leadingIcon = PhosphorIcons.Regular.CalendarBlank,
                leadingIconTint = dateBadge.ink,
                leadingIconBackground = dateBadge.wash,
                leadingIconShape = ProfileBadgeShape,
                cardShape = CardShape16,
                cardContainer = colorScheme.profileCard,
                placeholder = stringResource(id = R.string.placeholder_select_dob),
                onClick = { isDatePickerVisible = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputFieldCard(
                title = stringResource(id = R.string.label_gender_caps),
                value = displayGender,
                onValueChange = {},
                inputType = InputType.Date,
                leadingIcon = genderToIcon(gender),
                leadingIconTint = genderBadge.ink,
                leadingIconBackground = genderBadge.wash,
                leadingIconShape = ProfileBadgeShape,
                cardShape = CardShape16,
                cardContainer = colorScheme.profileCard,
                placeholder = selectGenderPlaceholder,
                onClick = { isGenderPickerVisible = true },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(id = R.string.content_desc_open_gender_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime).only(WindowInsetsSides.Bottom))
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Button(
                onClick = {
                    if (pendingSaveProfile != null) return@Button
                    val updated = userProfile.copy(
                        fullName = fullName.trim().ifBlank { guestUserPlaceholder },
                        emailAddress = userProfile.emailAddress.trim(),
                        phoneNumber = if (localPhoneNumber.trim().isEmpty()) "" else "${selectedCountryCode.trim()}${localPhoneNumber.trim()}",
                        dateOfBirthMillis = dateOfBirthMillis,
                        gender = gender,
                        photoUri = photoUri
                    )
                    if (initialPhotoUri != photoUri) {
                        initialPhotoUri?.let(ProfilePhotoManager::deleteManagedPhoto)
                    }
                    pendingSaveProfile = updated
                },
                enabled = !isPhotoProcessing && pendingSaveProfile == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp, max = 80.dp)
                    // The mockup's CTA is the card's 16dp corner, not the pill this used to be,
                    // so the fill, the shape and the glow all read from the one shape token.
                    .shadow(
                        elevation = 28.dp,
                        shape = CardShape16,
                        ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.34f),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.26f)
                    ),
                shape = CardShape16,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(
                            brush = brandGradient(),
                            shape = CardShape16
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.label_save_changes),
                        color = MaterialTheme.colorScheme.onBrandGradient,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }

    if (isDatePickerVisible) {
        WheelDateTimePickerModal(
            mode = WheelPickerMode.SINGLE_DATE,
            initialStartMillis = dateOfBirthMillis ?: System.currentTimeMillis(),
            onDismissRequest = { isDatePickerVisible = false },
            onConfirm = { pickedDateMillis, _ ->
                dateOfBirthMillis = datePickerSelectionToLocalDateTimestamp(
                    selectedDateMillis = pickedDateMillis,
                    referenceTimestamp = dateOfBirthMillis,
                    isInputUtc = false
                )
                isDatePickerVisible = false
            }
        )
    }

    if (isGenderPickerVisible) {
        AppSelectionSheet(
            title = stringResource(id = R.string.placeholder_select_gender),
            description = stringResource(id = R.string.desc_choose_gender_label),
            items = genderItems,
            selectedId = gender,
            sheetState = genderPickerSheetState,
            // The mockup paints this sheet on the edit profile card colour, not on the app's
            // own sheet token, so it is the one selection sheet that overrides the fill.
            containerColor = colorScheme.profileSheet,
            // ...and its four rows on the mockup's own card colours, #191D2C / #28253D in dark
            // and #F8FAFC / #F5F3FF in light, rather than the accent wash the app rows use.
            rowColors = SelectionRowColors(
                container = colorScheme.genderRow,
                selectedContainer = colorScheme.genderRowSelected
            ),
            onDismiss = { isGenderPickerVisible = false },
            onItemSelected = { selectedGender ->
                gender = selectedGender
                isGenderPickerVisible = false
            }
        )
    }

    if (isCountryPickerVisible) {
        val filteredCountryCodes = remember(dbCountryCodes, countrySearchQuery) {
            if (countrySearchQuery.isBlank()) {
                dbCountryCodes
            } else {
                dbCountryCodes.filter {
                    it.country.contains(countrySearchQuery, ignoreCase = true) ||
                    it.dialCode.contains(countrySearchQuery)
                }
            }
        }

        val countrySelectionItems = remember(filteredCountryCodes) {
            filteredCountryCodes.map { country ->
                SelectionItem(
                    id = country,
                    title = country.country,
                    leadingText = country.dialCode
                )
            }
        }

        val countryPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        AppSelectionSheet(
            title = stringResource(id = R.string.title_select_country),
            onDismiss = {
                isCountryPickerVisible = false
                countrySearchQuery = ""
            },
            items = countrySelectionItems,
            selectedId = dbCountryCodes.find { it.dialCode == selectedCountryCode },
            onItemSelected = { country ->
                selectedCountryCode = country.dialCode
                isCountryPickerVisible = false
                countrySearchQuery = ""
            },
            showSearch = true,
            searchQuery = countrySearchQuery,
            onSearchQueryChange = { countrySearchQuery = it },
            searchPlaceholder = stringResource(id = R.string.placeholder_search_country),
            sheetState = countryPickerSheetState
        )
    }
}

@Composable
private fun ProfilePhotoSection(
    gender: String,
    photoUri: String?,
    isPhotoProcessing: Boolean,
    onSelectPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onPrepareForExternalActivity: () -> Unit = {},
    userTier: UserTier = UserTier.FREE,
    isAnonymous: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    // The mockup's photo actions: violet for the pencil, rose for the trash, each on its own
    // wash and with no hairline around it in either theme.
    val editBadge = colorScheme.profilePhotoEdit
    val deleteBadge = colorScheme.profilePhotoDelete
    val isDark = colorScheme.isDark
    // 44dp in dark, 40dp in light, as the two supplied screens draw them. The button keeps
    // its 48dp touch target around the smaller circle.
    val actionSize = if (isDark) 44.dp else 40.dp
    // The mockup's ring is smaller than the 150dp this used to draw: 128dp in dark, 112dp in
    // light, which also leaves the five field cards closer to the fold on a 412 x 915 screen.
    val avatarSize = if (isDark) 128.dp else 112.dp

    ProfileAvatar(
        gender = gender,
        size = avatarSize,
        photoUri = photoUri,
        showBadge = false,
        userTier = userTier,
        isAnonymous = isAnonymous,
        modifier = Modifier
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onPrepareForExternalActivity()
                onSelectPhoto()
            },
            enabled = !isPhotoProcessing,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(actionSize)
                    .clip(CircleShape)
                    .background(editBadge.wash),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.PencilSimple,
                    contentDescription = if (photoUri == null) stringResource(id = R.string.content_desc_add_photo) else stringResource(id = R.string.content_desc_edit_photo),
                    tint = editBadge.ink,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        IconButton(
            onClick = onRemovePhoto,
            enabled = photoUri != null && !isPhotoProcessing,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(actionSize)
                    .clip(CircleShape)
                    .background(deleteBadge.wash),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Trash,
                    contentDescription = stringResource(id = R.string.content_desc_delete_photo),
                    tint = if (photoUri != null && !isPhotoProcessing) {
                        deleteBadge.ink
                    } else {
                        deleteBadge.ink.copy(alpha = 0.65f)
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


private fun genderToIcon(gender: String): ImageVector {
    return when (gender) {
        "Male" -> PhosphorIcons.Regular.GenderMale
        "Female" -> PhosphorIcons.Regular.GenderFemale
        "Non-binary" -> PhosphorIcons.Regular.GenderNeuter
        "Prefer not to say" -> PhosphorIcons.Regular.User
        else -> PhosphorIcons.Regular.GenderNeuter
    }
}


private fun String.avatarLetters(): String {
    val profile = UserProfile(
        fullName = this,
        emailAddress = "",
        phoneNumber = "",
        dateOfBirthMillis = null,
        gender = "",
        accountCreatedMillis = 0L,
        accountTier = "",
        photoUri = null
    )
    return profile.avatarInitials()
}

@Preview(
    name = "Profile Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun ProfileScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        ProfileScreenContent(
            userProfile = defaultUserProfile,
            isAdsEnabled = true,
            userTier = UserTier.PREMIUM,
            dbCountryCodes = emptyList(),
            updateEmailUiState = UpdateEmailUiState.Idle,
            onBackClick = {},
            onSaveClick = {},
            onPrepareForExternalActivity = {},
            onUpdateEmailInitiate = { _, _ -> },
            onUpdateEmailCheckStatus = {},
            onUpdateEmailResend = {},
            onUpdateEmailReset = {}
        )
    }
}

@Composable
private fun PhoneInputFieldCard(
    title: String,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    selectedCountryCode: String,
    onCountryCodeClick: () -> Unit,
    placeholder: String,
    // The mockup's card spec, passed in by the screen so this second implementation of the
    // field cannot drift away from the shared one on the rows above it.
    cardShape: Shape,
    cardContainer: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = cardContainer
    val accent = colorScheme.accentInk
    val onSurface = colorScheme.onSurface
    val onSurfaceVariant = colorScheme.onSurfaceVariant
    // Hoisted out of the draw lambda's reach: the phone field is a second implementation of
    // the shared field, so it reads the same badge pair the shared card does.
    val phoneBadge = colorScheme.profileBadgePhone
    // The same field spec the shared InputFieldCard follows: light takes the 16dp white
    // field on the hairline outline, lit in brand purple while it holds the cursor, and
    // dark keeps the 28dp, the quarter-strength edge and the 8dp lift. This is a second
    // implementation of that field - it carries the country-code selector the shared one
    // has no slot for - so it has to be kept in step by hand.
    val isDark = colorScheme.isDark
    var isFocused by remember { mutableStateOf(false) }
    val containerShape = cardShape
    val borderColor = if (isDark) {
        colorScheme.outlineVariant.copy(alpha = 0.4f)
    } else {
        if (isFocused) accent else colorScheme.outline
    }
    val focusManager = LocalFocusManager.current

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.hasFocus },
        shape = containerShape,
        colors = if (isDark) {
            AppCardDefaults.colors(
                darkContainer = containerColor,
                darkBorder = BorderStroke(width = 1.dp, color = borderColor),
            )
        } else {
            AppCardColors(
                containerColor = containerColor,
                contentColor = onSurface,
                border = BorderStroke(width = 1.dp, color = borderColor),
            )
        },
        elevation = if (isDark) 8.dp else AppCardDefaults.Elevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(ProfileBadgeShape)
                    .background(phoneBadge.wash),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Phone,
                    contentDescription = null,
                    tint = phoneBadge.ink,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Code selector
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCountryCodeClick() }
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCountryCode,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.title_select_country),
                            tint = accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Divider line
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(18.dp)
                            .width(1.dp)
                            .background(colorScheme.outlineVariant)
                    )

                    // Phone input
                    BasicTextField(
                        value = phoneNumber,
                        onValueChange = onPhoneNumberChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = onSurface
                        ),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (phoneNumber.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = onSurfaceVariant.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
        }
    }
}

private fun parsePhoneNumber(
    fullNumber: String,
    countryCodes: List<com.mknlabs.expensetracker.models.CountryCode>
): Pair<String, String> {
    val cleanNumber = fullNumber.trim()
    if (cleanNumber.isBlank()) {
        return Pair("+91", "")
    }

    // Find the longest matching dial code that prefix matches the full number
    val matchingCode = countryCodes
        .filter { cleanNumber.startsWith(it.dialCode) }
        .maxByOrNull { it.dialCode.length }

    return if (matchingCode != null) {
        val remaining = cleanNumber.substring(matchingCode.dialCode.length)
        Pair(matchingCode.dialCode, remaining)
    } else {
        // Fallback for +91 or other common prefixes if dbCountryCodes hasn't loaded yet
        if (cleanNumber.startsWith("+91")) {
            Pair("+91", cleanNumber.removePrefix("+91"))
        } else if (cleanNumber.startsWith("+1")) {
            Pair("+1", cleanNumber.removePrefix("+1"))
        } else if (cleanNumber.startsWith("+")) {
            // Find if starts with + followed by 1 to 4 digits
            val plusMatch = Regex("^\\+\\d{1,4}").find(cleanNumber)
            if (plusMatch != null) {
                val code = plusMatch.value
                val remaining = cleanNumber.substring(code.length)
                Pair(code, remaining)
            } else {
                Pair("+91", cleanNumber)
            }
        } else {
            Pair("+91", cleanNumber)
        }
    }
}
