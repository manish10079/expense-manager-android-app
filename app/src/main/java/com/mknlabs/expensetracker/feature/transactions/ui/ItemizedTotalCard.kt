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
internal fun TotalAmountCard(
    totalAmount: Double,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences
) {
    val isDark = MaterialTheme.colorScheme.isDark
    val shape = RoundedCornerShape(16.dp)
    // Dark takes the mockup's violet hero; light keeps the standard white card with the
    // brand currency glyph so the amount still reads on a bright page.
    val cardChrome = if (isDark) {
        Modifier
            .shadow(
                elevation = AppCardDefaults.Elevation,
                shape = shape,
                clip = false,
                ambientColor = CardShadowAmbientLight,
                spotColor = CardShadowSpotLight
            )
            .clip(shape)
            .background(breakdownHeroFill())
            .border(1.dp, TotalHeroBorder, shape)
    } else {
        val lightCard = AppCardDefaults.colors()
        Modifier
            .shadow(
                elevation = AppCardDefaults.Elevation,
                shape = shape,
                clip = false,
                ambientColor = CardShadowAmbientLight,
                spotColor = CardShadowSpotLight
            )
            .clip(shape)
            .background(lightCard.containerColor)
            .then(
                lightCard.border?.let { Modifier.border(it, shape) } ?: Modifier
            )
    }

    val fullAmount = formatCurrencyValue(totalAmount, currencyId, amountFormatPreferences)
    val symbol = getCurrency(currencyId).currencySymbol
    val symbolStart = fullAmount.indexOf(symbol)
    val amountText = buildAnnotatedString {
        if (symbolStart < 0) {
            append(fullAmount)
        } else {
            append(fullAmount.substring(0, symbolStart))
            // A thin space keeps the currency glyph off the digits, as the mockup's
            // space-x-1 + mr-0.5 do, whichever side the symbol sits on.
            if (symbolStart > 0) append("\u2009")
            pushStyle(
                SpanStyle(
                    color = if (isDark) TotalHeroCurrency else MaterialTheme.colorScheme.accentInk,
                    fontSize = if (isDark) 34.sp else 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            append(symbol)
            pop()
            if (symbolStart == 0) append("\u2009")
            append(fullAmount.substring(symbolStart + symbol.length))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .then(cardChrome)
            .padding(horizontal = 18.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.label_total_amount),
                color = if (isDark) TotalHeroLabel else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.2.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = amountText,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = if (isDark) 44.sp else MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = if (isDark) FontWeight.Black else MaterialTheme.typography.headlineLarge.fontWeight,
                    letterSpacing = if (isDark) (-1).sp else MaterialTheme.typography.headlineLarge.letterSpacing
                )
            )
        }
    }
}

