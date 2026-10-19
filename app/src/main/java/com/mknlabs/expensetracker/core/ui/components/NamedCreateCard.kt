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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBrandShadow
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmTitleLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateCaptionDark
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateCaptionLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateFieldBorderDark
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateFieldBorderLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreatePlaceholder
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateTitleDark
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCardBorder
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient

private val CardShape = RoundedCornerShape(16.dp)
private val IconShape = RoundedCornerShape(12.dp)
private val ControlShape = RoundedCornerShape(12.dp)

/**
 * HTML “New Rule” card: brand + icon, title, caption, name field, full-width Create.
 */
@Composable
fun NamedCreateCard(
    title: String,
    caption: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    headerIcon: ImageVector = Icons.Filled.Add,
    confirmEnabled: Boolean = true,
    showField: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null
) {
    val dark = MaterialTheme.colorScheme.isDark
    // Same card as ModernConfirmationDialog: the Manage Categories fill and hairline in
    // dark, the standard card in light. The name field reads its fill from here too.
    val cardColors = AppCardDefaults.colors(
        darkContainer = Color(0xFF101019),
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
                    .shadow(
                        elevation = 6.dp,
                        shape = IconShape,
                        ambientColor = ModernConfirmBrandShadow,
                        spotColor = ModernConfirmBrandShadow
                    )
                    .clip(IconShape)
                    .background(brandGradient()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = null,
                    tint = Color.White,
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
        if (showField) {
        val fieldFill = cardColors.containerColor
        val fieldBorder = if (dark) NamedCreateFieldBorderDark else NamedCreateFieldBorderLight
        val fieldInk = if (dark) NamedCreateTitleDark else ModernConfirmTitleLight
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = fieldInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.accentInk),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (confirmEnabled) onConfirm() }),
            modifier = Modifier
                .fillMaxWidth()
                .clip(ControlShape)
                .background(fieldFill)
                .border(1.dp, fieldBorder, ControlShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = NamedCreatePlaceholder,
                            fontSize = 14.sp
                        )
                    }
                    inner()
                }
            }
        )
        }
        extraContent?.invoke()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = ControlShape,
                    ambientColor = ModernConfirmBrandShadow,
                    spotColor = ModernConfirmBrandShadow
                )
                .clip(ControlShape)
                .background(brandGradient())
                .clickable(enabled = confirmEnabled, role = Role.Button, onClick = onConfirm)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBrandGradient,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = confirmLabel,
                    color = MaterialTheme.colorScheme.onBrandGradient,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(
    name = "Named create — Light",
    showBackground = true,
    widthDp = 280,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun NamedCreateCardPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            NamedCreateCard(
                title = "New Rule",
                caption = "Give this rule a short name so it is easy to find later.",
                value = "",
                onValueChange = {},
                placeholder = "Name this rule…",
                confirmLabel = "Create",
                onConfirm = {}
            )
        }
    }
}

@Preview(
    name = "Named create — Dark",
    showBackground = true,
    widthDp = 280,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NamedCreateCardPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            NamedCreateCard(
                title = "New Rule",
                caption = "Give this rule a short name so it is easy to find later.",
                value = "",
                onValueChange = {},
                placeholder = "Name this rule…",
                confirmLabel = "Create",
                onConfirm = {}
            )
        }
    }
}
