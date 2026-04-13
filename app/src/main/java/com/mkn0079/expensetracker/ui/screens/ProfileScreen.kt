package com.mkn0079.expensetracker.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.avatarInitials
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.ui.components.ProfileAvatar
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurpleGlow
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mkn0079.expensetracker.utils.formatDate
import com.mkn0079.expensetracker.utils.localDateTimestampToDatePickerSelection
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
    onSaveClick: (UserProfile) -> Unit = {}
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
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ProfileHeader(
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp),
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
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = fullName.ifBlank { "Guest User" },
                color = Color(0xFFF1EDF8),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.height(16.dp))

            ProfileTextFieldCard(
                label = "FULL NAME",
                value = fullName,
                leadingIcon = Icons.Filled.Person,
                placeholder = "Guest User",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                onValueChange = { fullName = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            ProfileTextFieldCard(
                label = "EMAIL ADDRESS",
                value = emailAddress,
                leadingIcon = Icons.Filled.Email,
                placeholder = "alex.j@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = { emailAddress = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            ProfileTextFieldCard(
                label = "PHONE NUMBER",
                value = phoneNumber,
                leadingIcon = Icons.Filled.Call,
                placeholder = "+1234 567 8900",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                onValueChange = { phoneNumber = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            ReadOnlyFieldCard(
                label = "DATE OF BIRTH",
                value = dateOfBirthMillis?.let { formatDate(it, dateFormatPattern) }.orEmpty(),
                leadingIcon = Icons.Filled.CalendarMonth,
                placeholder = "Select Date of Birth",
                onClick = { isDatePickerVisible = true }
            )

            Spacer(modifier = Modifier.height(18.dp))

            GenderFieldCard(
                label = "GENDER",
                value = gender,
                placeholder = GenderPlaceholder,
                onClick = { isGenderPickerVisible = true }
            )

            Spacer(modifier = Modifier.height(28.dp))

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
                    .height(82.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(999.dp),
                        ambientColor = PurplePrimary.copy(alpha = 0.34f),
                        spotColor = PurpleGlow.copy(alpha = 0.26f)
                    ),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF7C4DFF),
                                    Color(0xFFC8B1FF)
                                )
                            ),
                            shape = RoundedCornerShape(999.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save Changes",
                        color = Color(0xFF24114C),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }

    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateOfBirthMillis?.let(::localDateTimestampToDatePickerSelection)
        )
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateOfBirthMillis = datePickerState.selectedDateMillis
                            ?.let(::datePickerSelectionToLocalDateTimestamp)
                            ?: dateOfBirthMillis
                        isDatePickerVisible = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PurpleAccent)
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isDatePickerVisible = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB7B0C8))
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF1E1E20),
                    titleContentColor = Color(0xFFF1EDF8),
                    headlineContentColor = Color(0xFFF1EDF8),
                    weekdayContentColor = Color(0xFFBEB6D1),
                    subheadContentColor = Color(0xFFBEB6D1),
                    navigationContentColor = PurpleAccent,
                    yearContentColor = Color(0xFFDDD6EC),
                    currentYearContentColor = PurpleAccent,
                    selectedYearContentColor = Color(0xFF24114C),
                    selectedYearContainerColor = PurplePrimary,
                    dayContentColor = Color(0xFFECE6F7),
                    disabledDayContentColor = Color(0xFF6A6477),
                    selectedDayContentColor = Color(0xFF24114C),
                    selectedDayContainerColor = PurplePrimary,
                    todayContentColor = PurpleAccent,
                    todayDateBorderColor = PurpleAccent,
                    dividerColor = Color.White.copy(alpha = 0.08f)
                )
            )
        }
    }

    if (isGenderPickerVisible) {
        GenderPickerSheet(
            selectedGender = gender,
            sheetState = genderPickerSheetState,
            onDismiss = { isGenderPickerVisible = false },
            onGenderSelected = { selectedGender ->
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
    onRemovePhoto: () -> Unit
) {
    ProfileAvatar(
        initials = initials,
        size = 132.dp,
        textSize = 34.sp,
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
            onClick = onSelectPhoto,
            enabled = !isPhotoProcessing,
            modifier = Modifier
                .clip(CircleShape)
                .background(PurpleAccent.copy(alpha = 0.14f))
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = if (photoUri == null) "Add photo" else "Edit photo",
                tint = PurpleAccent
            )
        }

        IconButton(
            onClick = onRemovePhoto,
            enabled = photoUri != null && !isPhotoProcessing,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete photo",
                tint = if (photoUri != null && !isPhotoProcessing) {
                    Color(0xFFB7B0C8)
                } else {
                    Color(0xFF6A6477)
                }
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = PurpleAccent
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "Profile",
            color = PurplePrimary,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        )
    }
}

@Composable
private fun ProfileTextFieldCard(
    label: String,
    value: String,
    leadingIcon: ImageVector,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF1E1E20))
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFCAC2DE),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.1.sp,
                fontSize = 12.sp
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = PurpleAccent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = keyboardOptions,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFFEDE8F7),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF676272),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 18.sp
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun ReadOnlyFieldCard(
    label: String,
    value: String,
    leadingIcon: ImageVector,
    placeholder: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF1E1E20))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFCAC2DE),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.1.sp,
                fontSize = 12.sp
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = label,
                tint = PurpleAccent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) Color(0xFF676272) else Color(0xFF5E5770),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            )
        }
    }
}

@Composable
private fun GenderFieldCard(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    val genderIcon = genderToIcon(value)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF1E1E20))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFCAC2DE),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.1.sp,
                fontSize = 12.sp
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = genderIcon,
                contentDescription = label,
                tint = PurpleAccent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = value.ifBlank { placeholder },
                color = if (value.isBlank()) Color(0xFF676272) else Color(0xFFEDE8F7),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Open gender options",
                tint = Color(0xFF777184)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderPickerSheet(
    selectedGender: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onGenderSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select Gender",
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Choose the gender label that best fits your profile.",
                color = Color(0xFF968EA8),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = genderOptions,
                    key = { option -> option }
                ) { option ->
                    GenderPickerRow(
                        option = option,
                        isSelected = option == selectedGender,
                        onClick = { onGenderSelected(option) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun GenderPickerRow(
    option: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val genderIcon = genderToIcon(option)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) {
                    PurplePrimary.copy(alpha = 0.18f)
                } else {
                    Color(0xFF1A1A1E)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) {
                        PurpleAccent.copy(alpha = 0.18f)
                    } else {
                        Color(0xFF232326)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = genderIcon,
                contentDescription = option,
                tint = if (isSelected) PurpleAccent else Color(0xFFF0EBF7),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = option,
            color = Color(0xFFF0EBF7),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Text(
                text = "Selected",
                color = PurpleAccent,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

private fun genderToIcon(gender: String): ImageVector {
    return when (gender) {
        "Male" -> Icons.Filled.Male
        "Female" -> Icons.Filled.Female
        "Non-binary" -> Icons.Filled.Transgender
        "Prefer not to say" -> Icons.Filled.Person
        else -> Icons.Filled.Transgender
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
    backgroundColor = 0xFF0A0A0A,
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
