package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CalculatorLineItem
import com.adamglin.phosphoricons.Bold
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.theme.CardShadowAmbientLight
import com.mknlabs.expensetracker.core.ui.theme.CardShadowSpotLight
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBrandShadow
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmTitleLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateCaptionDark
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateCaptionLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateFieldBorderDark
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateFieldBorderLight
import com.mknlabs.expensetracker.core.ui.theme.NamedCreatePlaceholder
import com.mknlabs.expensetracker.core.ui.theme.NamedCreateTitleDark
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.breakdownHeroFill

import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.getCurrency

import androidx.compose.ui.window.Dialog


@Composable
internal fun BreakdownItemCard(
    item: CalculatorLineItem,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.isDark
    val highlightedBorderColor = colorScheme.accentInk
    // Light flattens every row to the shared card. The row being edited keeps the violet
    // lift and the 2dp ring it announces itself with, so the highlight still reads on
    // white; dark keeps its gradient fill and its lift exactly as they were.
    val lightCard = AppCardDefaults.colors()
    val cardChrome = if (isDark) {
        Modifier
            .shadow(
                elevation = if (item.highlighted) 18.dp else AppCardDefaults.Elevation,
                shape = shape,
                clip = false,
                ambientColor = if (item.highlighted) colorScheme.accentInk.copy(alpha = 0.14f) else CardShadowAmbientLight,
                spotColor = if (item.highlighted) colorScheme.secondary.copy(alpha = 0.12f) else CardShadowSpotLight
            )
            .clip(shape)
            .background(Color(0xFF101019))
            .then(
                if (item.highlighted) Modifier else Modifier.border(1.dp, TotalHeroBorder, shape)
            )
    } else {
        Modifier
            .shadow(
                elevation = if (item.highlighted) 18.dp else AppCardDefaults.Elevation,
                shape = shape,
                clip = false,
                ambientColor = if (item.highlighted) colorScheme.accentInk.copy(alpha = 0.14f) else CardShadowAmbientLight,
                spotColor = if (item.highlighted) colorScheme.secondary.copy(alpha = 0.12f) else CardShadowSpotLight
            )
            .clip(shape)
            .background(lightCard.containerColor)
            .then(
                if (item.highlighted) {
                    Modifier
                } else {
                    lightCard.border?.let { Modifier.border(border = it, shape = shape) } ?: Modifier
                }
            )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardChrome)
            .drawBehind {
                if (item.highlighted) {
                    drawRoundRect(
                        color = highlightedBorderColor,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column 1: the labels on top, their values underneath, one row per half of the
            // block and each row's content centred in its own half. The halves are equal, so
            // the block's height still comes from the 28.dp actions beside it.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.label_description_caps),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = stringResource(id = R.string.label_amount_caps),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.description,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )

                    // No trailing inset: the amount now shares the AMOUNT label's right edge,
                    // which is this column's edge rather than the card's.
                    Text(
                        text = formatCurrencyValue(item.amount, currencyId, amountFormatPreferences),
                        color = MaterialTheme.colorScheme.onSurface,
                        // One line and never ellipsized: this fixed-width child is measured
                        // before the weighted description, so it always has room and the
                        // description is the one that gives way.
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 14.dp)
                    )
                }
            }

            // Column 2: edit over delete, level with the rows they act on.
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BreakdownRowAction(
                    icon = Icons.Rounded.Edit,
                    contentDescription = stringResource(
                        id = R.string.content_desc_edit_item,
                        item.description
                    ),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onEditClick
                )

                BreakdownRowAction(
                    icon = Icons.Rounded.Close,
                    contentDescription = stringResource(
                        id = R.string.content_desc_delete_item,
                        item.description
                    ),
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

/**
 * One 28.dp round action on a breakdown row. The wash and the glyph travel together, so a
 * caller cannot pair a delete glyph with the neutral wash the edit action wears.
 */
@Composable
internal fun BreakdownRowAction(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

