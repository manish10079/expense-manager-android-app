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
internal fun PhoneInputFieldCard(
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

internal fun parsePhoneNumber(
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
