package com.mkn0079.expensetracker.ui.screens

import android.content.Context
import android.net.Uri
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
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.avatarInitials
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.ui.components.*
import com.mkn0079.expensetracker.ui.components.input.InputFieldCard
import com.mkn0079.expensetracker.ui.components.input.InputType
import com.mkn0079.expensetracker.ui.models.SelectionItem
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mkn0079.expensetracker.utils.formatDate
import androidx.compose.material.icons.rounded.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private const val GenderPlaceholder = "Select Gender"
private val genderOptions = listOf("Male", "Female", "Non-binary", "Prefer not to say")
private const val PROFILE_PHOTO_MIME_TYPE = "image/*"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    onBackClick: () -> Unit = {},
    onSaveClick: (UserProfile) -> Unit = {},
    onPrepareForExternalActivity: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val initialPhotoUri = userProfile.photoUri
    var fullName by rememberSaveable(userProfile) { mutableStateOf(userProfile.fullName) }
    var emailAddress by rememberSaveable(userProfile) { mutableStateOf(userProfile.emailAddress) }
    var phoneNumber by rememberSaveable(userProfile) { mutableStateOf(userProfile.phoneNumber) }
    var dateOfBirthMillis by rememberSaveable(userProfile) { mutableStateOf(userProfile.dateOfBirthMillis) }
    var gender by rememberSaveable(userProfile) {
        mutableStateOf(userProfile.gender.takeUnless { it == GenderPlaceholder }.orEmpty())
    }
    var photoUri by rememberSaveable(userProfile) { mutableStateOf(userProfile.photoUri) }
    var isGenderPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isPhotoProcessing by remember { mutableStateOf(false) }

    val genderItems = remember {
        genderOptions.map { option ->
            SelectionItem(
                id = option,
                title = option,
                leadingIcon = genderToIcon(option)
            )
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
            val savedUri = copyProfilePhotoToInternalStorage(
                context = context,
                sourceUri = selectedUri
            )
            if (savedUri != null) {
                previousUnsavedPhotoUri?.let { deleteManagedProfilePhoto(it) }
                photoUri = savedUri
            } else {
                Toast.makeText(
                    context,
                    "Unable to load that image. Please try another photo.",
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
            title = "Edit Profile",
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
                initials = fullName.avatarLetters(),
                photoUri = photoUri,
                isPhotoProcessing = isPhotoProcessing,
                onSelectPhoto = { photoPickerLauncher.launch(PROFILE_PHOTO_MIME_TYPE) },
                onRemovePhoto = {
                    photoUri
                        ?.takeIf { it != initialPhotoUri }
                        ?.let(::deleteManagedProfilePhoto)
                    photoUri = null
                },
                onPrepareForExternalActivity = onPrepareForExternalActivity
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputFieldCard(
                title = "FULL NAME",
                value = fullName,
                onValueChange = { fullName = it },
                inputType = InputType.Text,
                leadingIcon = Icons.Rounded.Person,
                placeholder = "Guest User"
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputFieldCard(
                title = "EMAIL ADDRESS",
                value = emailAddress,
                onValueChange = { emailAddress = it },
                inputType = InputType.Email,
                leadingIcon = Icons.Rounded.Email,
                placeholder = "alex.j@example.com"
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputFieldCard(
                title = "PHONE NUMBER",
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                inputType = InputType.Phone,
                leadingIcon = Icons.Rounded.Call,
                placeholder = "+1234 567 8900"
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputFieldCard(
                title = "DATE OF BIRTH",
                value = dateOfBirthMillis?.let { formatDate(it, dateFormatPattern) }.orEmpty(),
                onValueChange = {},
                inputType = InputType.Date,
                leadingIcon = Icons.Rounded.CalendarMonth,
                placeholder = "Select Date of Birth",
                onClick = { isDatePickerVisible = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            InputFieldCard(
                title = "GENDER",
                value = gender,
                onValueChange = {},
                inputType = InputType.Date,
                leadingIcon = genderToIcon(gender),
                placeholder = GenderPlaceholder,
                onClick = { isGenderPickerVisible = true },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Open gender options",
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
                            fullName = fullName.trim().ifBlank { "Guest User" },
                            emailAddress = emailAddress.trim(),
                            phoneNumber = phoneNumber.trim(),
                            dateOfBirthMillis = dateOfBirthMillis,
                            gender = gender,
                            photoUri = photoUri
                        )
                    )
                    if (initialPhotoUri != photoUri) {
                        initialPhotoUri?.let(::deleteManagedProfilePhoto)
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
                        text = "Save Changes",
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
                    referenceTimestamp = dateOfBirthMillis
                )
                isDatePickerVisible = false
            }
        )
    }

    if (isGenderPickerVisible) {
        AppSelectionSheet(
            title = "Select Gender",
            description = "Choose the gender label that best fits your profile.",
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
}

@Composable
private fun ProfilePhotoSection(
    initials: String,
    photoUri: String?,
    isPhotoProcessing: Boolean,
    onSelectPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onPrepareForExternalActivity: () -> Unit
) {
    val photoActionContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
    val photoActionIconColor = MaterialTheme.colorScheme.secondary

    ProfileAvatar(
        initials = initials,
        size = 150.dp,
        textSize = 38.sp,
        photoUri = photoUri,
        showBadge = false,
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
                contentDescription = if (photoUri == null) "Add photo" else "Edit photo",
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
                contentDescription = "Delete photo",
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
        memberSinceLabel = "",
        accountTier = "",
        photoUri = null
    )
    return profile.avatarInitials()
}

private suspend fun copyProfilePhotoToInternalStorage(
    context: Context,
    sourceUri: Uri
): String? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, "profile_photos").apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            val targetFile = File(directory, "profile_${UUID.randomUUID()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null

            Uri.fromFile(targetFile).toString()
        }.getOrNull()
    }
}

private fun deleteManagedProfilePhoto(uriString: String) {
    runCatching {
        val uri = Uri.parse(uriString)
        if (uri.scheme != "file") {
            return
        }

        val file = uri.path?.let(::File) ?: return
        if (file.parentFile?.name == "profile_photos" && file.exists()) {
            file.delete()
        }
    }
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
        ProfileScreen(
            userProfile = defaultUserProfile
        )
    }
}
