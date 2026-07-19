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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.utils.toTitleCase

import androidx.compose.runtime.remember

@Composable
fun ProfileCard(
    name: String,
    email: String,
    initials: String,
    photoUri: String? = null,
    userTier: UserTier = UserTier.FREE,
    proExpiryTimestamp: Long = 0L,
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
                initials = initials,
                photoUri = photoUri,
                size = 64.dp,
                textSize = 20.sp,
                showGlow = false,
                showBorder = true,
                backgroundColor = colorScheme.primary.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val isPremium = userTier == UserTier.PREMIUM
                val badgeLabel = if (isPremium) {
                    val formattedDate = remember(proExpiryTimestamp) {
                        try {
                            java.text.SimpleDateFormat("dd/MM/yy HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(proExpiryTimestamp))
                        } catch (e: Exception) {
                            ""
                        }
                    }
                    stringResource(com.mknlabs.expensetracker.R.string.label_pro_expiry_format, formattedDate)
                } else {
                    stringResource(com.mknlabs.expensetracker.R.string.label_guest_user)
                }

                UserBadge(
                    label = badgeLabel,
                    type = if (isPremium) UserBadgeType.PREMIUM else UserBadgeType.GUEST,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = name.toTitleCase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (email.isNotEmpty()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
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
                initials = "JD",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
