package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Transgender
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.UserBadge
import com.mknlabs.expensetracker.core.ui.components.UserBadgeType
import com.mknlabs.expensetracker.core.ui.components.input.InputFieldCard
import com.mknlabs.expensetracker.core.ui.components.input.InputType

internal fun genderToIcon(
    gender: String,
    male: String,
    female: String,
    nonBinary: String,
    preferNotToSay: String,
): ImageVector = when (gender) {
    male -> Icons.Rounded.Male
    female -> Icons.Rounded.Female
    nonBinary -> Icons.Rounded.Transgender
    preferNotToSay -> Icons.Rounded.Person
    else -> Icons.Rounded.Transgender
}

@Composable
internal fun OnboardingSetupPage(
    title: String,
    titleFontSize: TextUnit,
    titleLineHeight: TextUnit,
    isAnonymous: Boolean,
    userName: String,
    onUserNameChange: (String) -> Unit,
    userGender: String,
    maleLabel: String,
    femaleLabel: String,
    nonBinaryLabel: String,
    preferNotToSayLabel: String,
    nameFocusRequester: FocusRequester,
    onPrivacyInfoClick: () -> Unit,
    onGenderClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = titleFontSize,
                    lineHeight = titleLineHeight
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onPrivacyInfoClick,
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
                onValueChange = onUserNameChange,
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
                leadingIcon = genderToIcon(
                    userGender,
                    maleLabel,
                    femaleLabel,
                    nonBinaryLabel,
                    preferNotToSayLabel
                ),
                placeholder = stringResource(id = R.string.label_select_gender),
                onClick = onGenderClick,
                trailingContent = {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}
