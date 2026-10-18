package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmTitleLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateCaptionDark
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateCaptionLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateFieldBorderDark
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateFieldBorderLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateTitleDark
import com.mknlabs.expensetracker.core.ui.theme.QuickActionAmberFill
import com.mknlabs.expensetracker.core.ui.theme.QuickActionAmberInkDark
import com.mknlabs.expensetracker.core.ui.theme.QuickActionAmberInkLight
import com.mknlabs.expensetracker.core.ui.theme.QuickActionOptionInkDark
import com.mknlabs.expensetracker.core.ui.theme.QuickActionOptionInkLight
import com.mknlabs.expensetracker.core.ui.theme.QuickActionSelectedFill
import com.mknlabs.expensetracker.core.ui.theme.QuickActionSelectedInk
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCard
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCardBorder

private val CardShape = RoundedCornerShape(16.dp)
private val IconShape = RoundedCornerShape(12.dp)
private val OptionShape = RoundedCornerShape(12.dp)

data class QuickActionOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)

/**
 * HTML “Quick Action” card: amber sliders badge, title, caption, stacked option rows.
 */
@Composable
fun QuickActionCard(
    title: String,
    caption: String,
    options: List<QuickActionOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    headerIcon: ImageVector = Icons.Filled.Tune
) {
    val dark = MaterialTheme.colorScheme.isDark
    // Same card as ModernConfirmationDialog: the Manage Categories fill and hairline in
    // dark, the standard card in light.
    val cardColors = AppCardDefaults.colors(
        darkContainer = MaterialTheme.colorScheme.manageCategoryCard,
        darkBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.manageCategoryCardBorder)
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(cardColors.containerColor)
            .then(
                cardColors.border?.let { Modifier.border(it, CardShape) } ?: Modifier
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(IconShape)
                    .background(QuickActionAmberFill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = null,
                    tint = if (dark) QuickActionAmberInkDark else QuickActionAmberInkLight,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                color = if (dark) NamedCreateTitleDark else ModernConfirmTitleLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = caption,
            color = if (dark) NamedCreateCaptionDark else NamedCreateCaptionLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 18.sp
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val selected = option.id == selectedId
                val fill = if (selected) {
                    QuickActionSelectedFill
                } else {
                    cardColors.containerColor
                }
                val stroke = when {
                    selected -> QuickActionSelectedInk
                    dark -> NamedCreateFieldBorderDark
                    else -> NamedCreateFieldBorderLight
                }
                val ink = when {
                    selected -> QuickActionSelectedInk
                    dark -> QuickActionOptionInkDark
                    else -> QuickActionOptionInkLight
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(OptionShape)
                        .background(fill)
                        .border(1.dp, stroke, OptionShape)
                        .clickable(role = Role.Button) { onSelect(option.id) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = null,
                        tint = ink,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = option.label,
                        color = ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickActionCardPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        QuickActionCardPreviewBody()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun QuickActionCardPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        QuickActionCardPreviewBody()
    }
}

@Composable
private fun QuickActionCardPreviewBody() {
    QuickActionCard(
        title = "Quick Action",
        caption = "Pick where this should apply. You can change it anytime.",
        options = listOf(
            QuickActionOption("inbox", "Inbox", Icons.Filled.Inbox),
            QuickActionOption("filter", "Filter", Icons.Filled.FilterList),
            QuickActionOption("alert", "Alert", Icons.Filled.Notifications)
        ),
        selectedId = "inbox",
        onSelect = {},
        modifier = Modifier.padding(16.dp)
    )
}
