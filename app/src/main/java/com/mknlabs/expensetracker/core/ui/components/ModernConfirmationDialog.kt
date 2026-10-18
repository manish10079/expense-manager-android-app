package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBodyDark
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBodyLight
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBrandShadow
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmCardShadowDark
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmCardShadowLight
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmCloseFillDark
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmDeleteFill
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmCloseFillLight
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmCloseGlyph
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmEmphasisDark
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmEmphasisLight
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmTitleDark
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmTitleLight
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCard
import com.mknlabs.expensetracker.core.ui.theme.manageCategoryCardBorder

private val ModernConfirmCardShape = RoundedCornerShape(24.dp)
private val ModernConfirmButtonShape = RoundedCornerShape(12.dp)
private val ModernConfirmIconShape = RoundedCornerShape(16.dp)

/**
 * Modern confirmation dialog from the HTML showcase (Modern variant).
 *
 * Callers pass header icon, title, subtitle, body, and button labels. Not wired to
 * any screen — drop-in replica for light and dark.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: AnnotatedString,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    headerIcon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    dismissButtonText: String? = null,
    onDismiss: () -> Unit = onDismissRequest,
    headerIconContentDescription: String? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    showDismissButton: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null
) {
    val cancelLabel = dismissButtonText ?: stringResource(id = R.string.action_cancel)

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = dismissOnClickOutside,
            dismissOnBackPress = dismissOnBackPress
        )
    ) {
        ModernConfirmationDialogCard(
            title = title,
            text = text,
            confirmButtonText = confirmButtonText,
            onConfirm = onConfirm,
            headerIcon = headerIcon,
            modifier = modifier,
            subtitle = subtitle,
            dismissButtonText = cancelLabel,
            onDismiss = onDismiss,
            onClose = onDismissRequest,
            headerIconContentDescription = headerIconContentDescription,
            showDismissButton = showDismissButton,
            extraContent = extraContent
        )
    }
}

@Composable
private fun ModernConfirmationDialogCard(
    title: String,
    text: AnnotatedString,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    headerIcon: ImageVector,
    dismissButtonText: String,
    onDismiss: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    headerIconContentDescription: String? = null,
    showDismissButton: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.isDark
    // Dark adopts the Manage Categories card fill (and its hairline), so every dialog sits
    // on the same card surface as the list that raised it; light keeps the standard card.
    val cardColors = AppCardDefaults.colors(
        darkContainer = MaterialTheme.colorScheme.manageCategoryCard,
        darkBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.manageCategoryCardBorder)
    )
    val titleColor = if (isDark) ModernConfirmTitleDark else ModernConfirmTitleLight
    val bodyColor = if (isDark) ModernConfirmBodyDark else ModernConfirmBodyLight
    val closeFill = if (isDark) ModernConfirmCloseFillDark else ModernConfirmCloseFillLight
    val cardShadow = if (isDark) ModernConfirmCardShadowDark else ModernConfirmCardShadowLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .widthIn(max = 448.dp)
            .shadow(
                elevation = 24.dp,
                shape = ModernConfirmCardShape,
                ambientColor = cardShadow,
                spotColor = cardShadow
            )
            .clip(ModernConfirmCardShape)
            .background(cardColors.containerColor)
            .then(
                if (cardColors.border == null) Modifier
                else Modifier.border(cardColors.border, ModernConfirmCardShape)
            )
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(32.dp)
                .clip(CircleShape)
                .background(closeFill)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onClose
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(id = R.string.desc_close),
                tint = ModernConfirmCloseGlyph,
                modifier = Modifier.size(14.dp)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(end = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = ModernConfirmIconShape,
                            ambientColor = ModernConfirmBrandShadow,
                            spotColor = ModernConfirmBrandShadow
                        )
                        .clip(ModernConfirmIconShape)
                        .background(brandGradient()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = headerIcon,
                        contentDescription = headerIconContentDescription,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        color = titleColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            color = MaterialTheme.colorScheme.accentInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = text,
                color = bodyColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 22.sp
            )
            if (extraContent != null) {
                Spacer(modifier = Modifier.height(16.dp))
                CompositionLocalProvider(LocalContentColor provides bodyColor) {
                    extraContent()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showDismissButton) {
                    ModernGradientAction(
                        text = dismissButtonText,
                        brush = brandGradient(),
                        shadow = ModernConfirmBrandShadow,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                }
                ModernGradientAction(
                    text = confirmButtonText,
                    fill = ModernConfirmDeleteFill,
                    shadow = ModernConfirmDeleteFill.copy(alpha = 0.3f),
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ModernConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    headerIcon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    dismissButtonText: String? = null,
    onDismiss: () -> Unit = onDismissRequest,
    headerIconContentDescription: String? = null,
    emphasizedPhrase: String? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    showDismissButton: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val emphasis = if (isDark) {
        ModernConfirmEmphasisDark
    } else {
        ModernConfirmEmphasisLight
    }
    ModernConfirmationDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = emphasizePhrase(text, emphasizedPhrase, emphasis),
        confirmButtonText = confirmButtonText,
        onConfirm = onConfirm,
        headerIcon = headerIcon,
        modifier = modifier,
        subtitle = subtitle,
        dismissButtonText = dismissButtonText,
        onDismiss = onDismiss,
        headerIconContentDescription = headerIconContentDescription,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        showDismissButton = showDismissButton,
        extraContent = extraContent
    )
}

@Composable
private fun ModernGradientAction(
    text: String,
    shadow: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    brush: Brush? = null,
    fill: Color? = null
) {
    val shape = ModernConfirmButtonShape
    val painted = if (brush != null) {
        Modifier.background(brush)
    } else {
        Modifier.background(fill ?: Color.Transparent)
    }
    Box(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = shape, ambientColor = shadow, spotColor = shadow)
            .clip(shape)
            .then(painted)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun emphasizePhrase(
    text: String,
    phrase: String?,
    emphasisColor: Color
): AnnotatedString {
    if (phrase.isNullOrBlank()) return AnnotatedString(text)
    val start = text.indexOf(phrase)
    if (start < 0) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, start))
        withStyle(SpanStyle(color = emphasisColor, fontWeight = FontWeight.SemiBold)) {
            append(phrase)
        }
        append(text.substring(start + phrase.length))
    }
}

@Preview(
    name = "Modern confirm — Light",
    showBackground = true,
    widthDp = 400,
    heightDp = 420,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun ModernConfirmationDialogPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        ModernConfirmationDialogPreviewCanvas(scrim = Color(0xFFF1F5F9))
    }
}

@Preview(
    name = "Modern confirm — Dark",
    showBackground = true,
    widthDp = 400,
    heightDp = 420,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF020617
)
@Composable
private fun ModernConfirmationDialogPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        ModernConfirmationDialogPreviewCanvas(scrim = Color(0xFF020617))
    }
}

@Composable
private fun ModernConfirmationDialogPreviewCanvas(scrim: Color) {
    val isDark = MaterialTheme.colorScheme.isDark
    val emphasis = if (isDark) {
        ModernConfirmEmphasisDark
    } else {
        ModernConfirmEmphasisLight
    }
    val body = emphasizePhrase(
        text = "This removes the saved message from your active inbox rules. Any transactions you already added will stay completely untouched.",
        phrase = "completely untouched",
        emphasisColor = emphasis
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scrim),
        contentAlignment = Alignment.Center
    ) {
        ModernConfirmationDialogCard(
            title = "Delete detection?",
            subtitle = "Irreversible inbox action",
            text = body,
            confirmButtonText = "Delete Item",
            onConfirm = {},
            headerIcon = Icons.Rounded.Delete,
            dismissButtonText = "Cancel",
            onDismiss = {},
            onClose = {}
        )
    }
}
