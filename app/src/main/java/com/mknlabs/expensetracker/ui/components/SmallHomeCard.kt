package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.standardCardGradient

@Composable
fun SmallHomeCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.CardRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = shape
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = brandGradient(),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
