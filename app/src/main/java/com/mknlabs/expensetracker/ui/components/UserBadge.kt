package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class UserBadgeType {
    GUEST,
    MEMBER,
    PREMIUM
}

@Composable
fun UserBadge(
    label: String,
    type: UserBadgeType,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val (containerColor, contentColor) = when (type) {
        UserBadgeType.GUEST -> 
            colorScheme.surfaceVariant to colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        UserBadgeType.MEMBER -> 
            colorScheme.secondaryContainer.copy(alpha = 0.4f) to colorScheme.primary
        UserBadgeType.PREMIUM -> 
            colorScheme.primary to colorScheme.onPrimary
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
