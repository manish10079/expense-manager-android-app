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

@Composable
internal fun ProfilePhotoSection(
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


internal fun genderToIcon(gender: String): ImageVector {
    return when (gender) {
        "Male" -> PhosphorIcons.Regular.GenderMale
        "Female" -> PhosphorIcons.Regular.GenderFemale
        "Non-binary" -> PhosphorIcons.Regular.GenderNeuter
        "Prefer not to say" -> PhosphorIcons.Regular.User
        else -> PhosphorIcons.Regular.GenderNeuter
    }
}


internal fun String.avatarLetters(): String {
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
