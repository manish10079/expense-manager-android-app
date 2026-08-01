package com.mknlabs.expensetracker.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.utils.toTitleCase

import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.painterResource

@Composable
fun ProfileCard(
    name: String,
    email: String,
    gender: String = "",
    photoUri: String? = null,
    userTier: UserTier = UserTier.FREE,
    proExpiryTimestamp: Long = 0L,
    isAnonymous: Boolean = false,
    isSyncing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar Section
            ProfileAvatar(
                gender = gender,
                photoUri = photoUri,
                size = 64.dp,
                showGlow = false,
                showBorder = true,
                backgroundColor = colorScheme.primary.copy(alpha = 0.1f),
                userTier = userTier,
                isSyncing = isSyncing,
                isAnonymous = isAnonymous
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val isPremium = userTier == UserTier.PREMIUM && !isAnonymous


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isPremium) {
                        Icon(
                            painter = painterResource( com.mknlabs.expensetracker.R.drawable.ic_crown),
                            contentDescription = "Pro",
                            tint = colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(18.dp)
                        )
                    }

                    Text(
                        text = name.toTitleCase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                    Spacer(Modifier.padding(top = 5.dp))
                val subtext = if (isAnonymous) {
                    stringResource(com.mknlabs.expensetracker.R.string.label_tap_to_sync)
                } else {
                    email
                }

                if (subtext.isNotEmpty()) {
                    Text(
                        text = subtext,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isAnonymous) MaterialTheme.colorScheme.primary else colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }


        }
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileCardPreview() {
    ExpenseTrackerTheme {
        Surface {
            ProfileCard(
                name = "Johnathan Doe",
                email = "john.doe@example.com",
                gender = "Male",
                userTier = UserTier.PREMIUM,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
