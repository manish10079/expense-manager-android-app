package com.mknlabs.expensetracker.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DOB_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.models.avatarInitials
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.ui.components.*
import com.mknlabs.expensetracker.ui.components.input.InputFieldCard
import com.mknlabs.expensetracker.ui.components.input.InputType
import com.mknlabs.expensetracker.ui.models.SelectionItem
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.ProfilePhotoManager
import androidx.compose.material.icons.rounded.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.ui.viewmodels.AuthViewModel
import com.mknlabs.expensetracker.ui.viewmodels.UpdateEmailUiState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.ui.viewmodels.ProfileViewModel
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

private const val PROFILE_PHOTO_MIME_TYPE = "image/*"

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
            SelectionItem(id = "Male", title = maleLabel, leadingIcon = Icons.Rounded.Male),
            SelectionItem(id = "Female", title = femaleLabel, leadingIcon = Icons.Rounded.Female),
            SelectionItem(id = "Non-binary", title = nonBinaryLabel, leadingIcon = Icons.Rounded.Transgender),
            SelectionItem(id = "Prefer not to say", title = preferNotToSayLabel, leadingIcon = Icons.Rounded.Person)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        AppHeader(
            title = stringResource(id = R.string.title_edit_profile),
            onBackClick = onBackClick,
            modifier = Modifier.padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 10.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, top = 4.dp, bottom = 22.dp),
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
                isAnonymous = userProfile.authProvider == "anonymous"
            )

            Spacer(modifier = Modifier.height(24.dp))

            AdContainer(isAdsEnabled = isAdsEnabled) {
                NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
            }

            Spacer(modifier = Modifier.height(18.dp))

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

                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        InputFieldCard(
                            title = stringResource(id = R.string.label_email_address_caps),
                            value = userProfile.emailAddress,
                            onValueChange = {},
                            inputType = InputType.Text,
                            leadingIcon = Icons.Rounded.Email,
                            placeholder = "",
                            isEnabled = false,
                            trailingContent = if (emailVerifiedState) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = stringResource(id = R.string.content_desc_email_verified),
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else null
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )

                        if (!emailVerifiedState) {
                            // Email not verified → Verify Email button
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    firebaseUser?.sendEmailVerification()
                                    showVerificationSheet = true
                                    Toast.makeText(context, context.getString(R.string.toast_verification_email_sent), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Email,
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
                            // Email verified → Update Email button
                            androidx.compose.material3.TextButton(
                                onClick = { showUpdateEmailSheet = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
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
                    com.mknlabs.expensetracker.ui.screens.VerificationBottomSheet(
                        email = userProfile.emailAddress,
                        onDismiss = { showVerificationSheet = false }
                    )
                }

                if (showUpdateEmailSheet) {
                    com.mknlabs.expensetracker.ui.screens.UpdateEmailBottomSheet(
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
                leadingIcon = Icons.Rounded.Person,
                placeholder = stringResource(id = R.string.placeholder_guest_user)
            )

            Spacer(modifier = Modifier.height(18.dp))

            PhoneInputFieldCard(
                title = stringResource(id = R.string.label_phone_number_caps),
                phoneNumber = localPhoneNumber,
                onPhoneNumberChange = { localPhoneNumber = it },
                selectedCountryCode = selectedCountryCode,
                onCountryCodeClick = { isCountryPickerVisible = true },
                placeholder = stringResource(id = R.string.placeholder_phone_example)
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputFieldCard(
                title = stringResource(id = R.string.label_date_of_birth_caps),
                value = dateOfBirthMillis?.let { formatDate(it, DOB_DATE_FORMAT_PATTERN) }.orEmpty(),
                onValueChange = {},
                inputType = InputType.Date,
                leadingIcon = Icons.Rounded.CalendarMonth,
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
                    onSaveClick(
                        userProfile.copy(
                            fullName = fullName.trim().ifBlank { guestUserPlaceholder },
                            emailAddress = userProfile.emailAddress.trim(),
                            phoneNumber = if (localPhoneNumber.trim().isEmpty()) "" else "${selectedCountryCode.trim()}${localPhoneNumber.trim()}",
                            dateOfBirthMillis = dateOfBirthMillis,
                            gender = gender,
                            photoUri = photoUri
                        )
                    )
                    if (initialPhotoUri != photoUri) {
                        initialPhotoUri?.let(ProfilePhotoManager::deleteManagedPhoto)
                    }
                },
                enabled = !isPhotoProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(999.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.26f)
                    ),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(999.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.label_save_changes),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
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
    val photoActionContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
    val photoActionIconColor = MaterialTheme.colorScheme.secondary

    ProfileAvatar(
        gender = gender,
        size = 150.dp,
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
            modifier = Modifier
                .clip(CircleShape)
                .background(photoActionContainerColor)
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = if (photoUri == null) stringResource(id = R.string.content_desc_add_photo) else stringResource(id = R.string.content_desc_edit_photo),
                tint = photoActionIconColor
            )
        }

        IconButton(
            onClick = onRemovePhoto,
            enabled = photoUri != null && !isPhotoProcessing,
            modifier = Modifier
                .clip(CircleShape)
                .background(photoActionContainerColor)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.content_desc_delete_photo),
                tint = if (photoUri != null && !isPhotoProcessing) {
                    photoActionIconColor
                } else {
                    photoActionIconColor.copy(alpha = 0.65f)
                }
            )
        }
    }
}


private fun genderToIcon(gender: String): ImageVector {
    return when (gender) {
        "Male" -> Icons.Rounded.Male
        "Female" -> Icons.Rounded.Female
        "Non-binary" -> Icons.Rounded.Transgender
        "Prefer not to say" -> Icons.Rounded.Person
        else -> Icons.Rounded.Transgender
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
    placeholder: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = colorScheme.surface
    val primary = colorScheme.primary
    val onSurface = colorScheme.onSurface
    val onSurfaceVariant = colorScheme.onSurfaceVariant
    val containerShape = RoundedCornerShape(28.dp)
    val borderColor = colorScheme.outlineVariant.copy(alpha = 0.4f)
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = containerShape,
        color = containerColor,
        border = BorderStroke(width = 1.dp, color = borderColor),
        shadowElevation = 8.dp
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
                    .clip(CircleShape)
                    .background(primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = null,
                    tint = primary,
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
                                color = primary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.title_select_country),
                            tint = primary,
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
                        textStyle = LocalTextStyle.current.copy(
                            color = onSurface,
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(primary),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (phoneNumber.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 16.sp
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
