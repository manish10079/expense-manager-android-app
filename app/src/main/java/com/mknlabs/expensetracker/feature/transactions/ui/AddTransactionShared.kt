package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.theme.CardShadowAmbientLight
import com.mknlabs.expensetracker.core.ui.theme.CardShadowSpotLight
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.formCard
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.utils.formatDate
import java.math.BigDecimal

@Composable
internal fun SectionHeader(
    title: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = title,
        color = color,
        style = MaterialTheme.typography.labelSmall
    )
}

/** A fund's stored `#RRGGBB`, or null when it is absent or malformed. */
internal fun fundChipColor(colorHex: String): Color? =
    runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrNull()

@Composable
internal fun <T> ChoiceChipRow(
    items: List<T>,
    selectedId: Int,
    compact: Boolean,
    getId: (T) -> Int,
    getLabel: (T) -> String,
    getIcon: (T) -> ImageVector,
    /**
     * The row's identity colour, resolved by the caller Ã¢â‚¬â€ which is what decides whether this
     * is a category or a payment method, since both are numbered from 1 and their palettes
     * are separate. Null leaves the chip on the plain surface ink.
     */
    getColor: ((T) -> Color)? = null,
    onItemSelected: (Int) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)) {
        items(items, key = getId) { item ->
            ChoiceChip(
                label = getLabel(item),
                icon = getIcon(item),
                identityColor = getColor?.invoke(item),
                isSelected = getId(item) == selectedId,
                compact = compact,
                onClick = { onItemSelected(getId(item)) }
            )
        }
    }
}

@Composable
internal fun ChoiceChip(
    label: String,
    icon: ImageVector,
    identityColor: Color?,
    isSelected: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.widthIn(min = if (compact) 72.dp else 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 46.dp else 52.dp)
                .shadow(
                    elevation = if (isSelected) 22.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.26f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f)
                )
                .clip(CircleShape)
                // Unselected wash matches the transaction-card icon (10% light / 14% dark).
                // Selected fill is the screen background; ring and glyph use accent ink.
                .background(
                    brush = when {
                        isSelected -> SolidColor(MaterialTheme.colorScheme.background)
                        identityColor != null -> SolidColor(MaterialTheme.colorScheme.categorySoft(identityColor))
                        MaterialTheme.colorScheme.isDark -> standardCardGradient()
                        else -> SolidColor(MaterialTheme.colorScheme.surfaceVariant)
                    }
                )
                .then(
                    if (isSelected) {
                        Modifier.border(1.dp, brandGradient(), CircleShape)
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                // The identity colour reaches the glyph and the tile behind it, the two steps
                // every other glyph tile in the app takes. Selection moves the glyph to the
                // accent ink and rings the tile Ã¢â‚¬â€ the pair the icon picker uses for its chosen
                // tile Ã¢â‚¬â€ so a chip reads as chosen against a grid of tinted siblings.
                tint = when {
                    isSelected -> Color.White
                    identityColor != null -> identityColor
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .size(if (compact) 18.dp else 20.dp)
                    .then(
                        if (isSelected) {
                            val selectedRamp = brandGradient()
                            Modifier
                                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                .drawWithCache {
                                    onDrawWithContent {
                                        drawContent()
                                        drawRect(brush = selectedRamp, blendMode = BlendMode.SrcIn)
                                    }
                                }
                        } else {
                            Modifier
                        }
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                fontSize = if (compact) 10.sp else 11.sp
            )
        )
    }
}

@Composable
internal fun SelectionInfoCard(
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector,
    label: String,
    value: String,
    isPlaceholder: Boolean = false,
    highlighted: Boolean = false,
    compact: Boolean,
    /** Overrides the state-driven glyph ink; null keeps the accent/placeholder rules. */
    iconTint: Color? = null,
    /** When set the icon sits in a 12 dp-cornered tile of this colour (the mockup tile). */
    iconContainerColor: Color? = null,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val animatedBorderColor by animateColorAsState(
        targetValue = if (highlighted) Color.Transparent 
                     else colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "selection_card_border"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader(title = label)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 48.dp else 56.dp)
                .shadow(
                    elevation = if (highlighted) 12.dp else 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    // The highlighted tile is the enabled-recurring one, and its violet lift
                    // is that state's message, so light keeps the tint; the plain tile takes
                    // the card spec's own shadow.
                    ambientColor = if (colorScheme.isDark || highlighted) colorScheme.accentInk.copy(alpha = if (highlighted) 0.15f else 0.06f) else CardShadowAmbientLight,
                    spotColor = if (colorScheme.isDark || highlighted) colorScheme.secondary.copy(alpha = if (highlighted) 0.15f else 0.06f) else CardShadowSpotLight
                )
                .clip(RoundedCornerShape(20.dp))
                .background(SolidColor(colorScheme.formCard))
                .border(
                    width = if (highlighted) 0.dp else 1.dp,
                    color = if (highlighted) Color.Transparent else animatedBorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(onClick = onClick)
                .padding(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 8.dp else 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val resolvedIconTint = iconTint ?: when {
                highlighted -> colorScheme.accentInk
                isPlaceholder -> colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else -> colorScheme.accentInk
            }
            if (iconContainerColor == null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = label,
                    tint = resolvedIconTint,
                    modifier = Modifier.size(if (compact) 18.dp else 20.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = label,
                        tint = resolvedIconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = value,
                color = if (isPlaceholder) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (highlighted) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = if (compact) 14.sp else 15.sp,
                    fontStyle = if (isPlaceholder) FontStyle.Italic else FontStyle.Normal,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun AddTransactionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    label: String,
    selectedCategory: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.55f)
            .shadow(
                elevation = 26.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.38f)
            )
            .clip(shape)
            .background(brush = brandGradient())
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onCta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
            )


        }
    }
}

@Composable

internal fun updateAmountInput(
    current: String,
    pressedKey: String
): String {
    return when (pressedKey) {
        "delete" -> {
            val updated = current.dropLast(1)
            if (updated.isBlank()) "0" else updated
        }

        "." -> {
            if (current.contains(".")) {
                current
            } else {
                "$current."
            }
        }

        else -> {
            val digitsAfterDecimal = if (current.contains(".")) {
                current.substringAfter(".").length
            } else {
                0
            }

            if (digitsAfterDecimal >= 2) {
                current
            } else if (current == "0") {
                if (pressedKey == "0") current else pressedKey
            } else {
                (current + pressedKey).take(12)
            }
        }
    }
}

internal fun formatTransactionDate(
    dateInMillis: Long,
    dateFormatPattern: String
): String {
    return formatDate(dateInMillis, dateFormatPattern)
}

internal fun formatEditableAmount(amount: Double): String {
    return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()
}

/** Major-unit amount input for the EMI plan fields (digits and one decimal point only). */
@Composable
internal fun EmiAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            // Same 12-character ceiling as the transaction amount field, which
            // also keeps the minor-unit conversion far away from Long overflow.
            if (input.length <= 12 &&
                (input.isEmpty() || (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1))
            ) {
                onValueChange(input)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = AppOutlinedFieldDefaults.shape,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        supportingText = supportingText?.let { { Text(it) } },
        colors = AppOutlinedFieldDefaults.colors()
    )
}

internal fun validateAmountChange(newValue: String, current: String): String {
    if (newValue.isEmpty()) return "0"
    if (newValue.length > 12) return current

    val dotCount = newValue.count { it == '.' }
    if (dotCount > 1) return current

    if (newValue.any { !it.isDigit() && it != '.' }) return current

    if (newValue.contains(".")) {
        val decimals = newValue.substringAfter(".")
        if (decimals.length > 2) return current
    }

    if (newValue.startsWith("0") && newValue.length > 1 && newValue[1] != '.') {
        val sanitized = newValue.dropWhile { it == '0' }
        return if (sanitized.isEmpty()) "0" else sanitized
    }

    return newValue
}
