package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.ui.theme.brandGradient

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
    val isPremium = type == UserBadgeType.PREMIUM
    
    val (containerColor, contentColor) = when (type) {
        UserBadgeType.GUEST -> 
            colorScheme.surfaceVariant to colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        UserBadgeType.MEMBER -> 
            colorScheme.secondaryContainer.copy(alpha = 0.4f) to colorScheme.primary
        UserBadgeType.PREMIUM -> 
            Color.Transparent to colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .background(
                brush = if (isPremium) brandGradient() else Brush.linearGradient(listOf(containerColor, containerColor)),
                shape = RoundedCornerShape(999.dp)
            )
    ) {
        Text(
            text = if (isPremium) label else label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
