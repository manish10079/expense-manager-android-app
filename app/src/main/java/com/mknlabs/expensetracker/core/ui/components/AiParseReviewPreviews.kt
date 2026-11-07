package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Tags
import com.composables.icons.lucide.Wallet
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.RotateCw
import com.composables.icons.lucide.ArrowDown
import com.composables.icons.lucide.ArrowUp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.cloud.AiParseContract
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.paymentColor
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.models.CurrencyPosition
import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatNumberValue
import com.mknlabs.expensetracker.utils.getCategoryIcon
import com.mknlabs.expensetracker.utils.getCurrency
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.math.BigDecimal
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.delay

@Composable
internal fun PreviewParsedDetailCardLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = ReviewLightCanvas,
            border = BorderStroke(1.dp, ReviewLightBorder),
        ) {
            ParsedDetailCard(
                item = previewReviewItem(),
                dark = false,
                currencyId = DEFAULT_CURRENCY_ID,
                showBackToList = false,
                onBackToList = {},
                onRetry = {},
                onItemChange = {},
                onAdd = {},
            )
        }
    }
}

@Preview(name = "Parsed card - Dark", showBackground = true, backgroundColor = 0xFF09090B)
@Composable
internal fun PreviewParsedDetailCardDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = ReviewDarkCanvas,
            border = BorderStroke(1.dp, ReviewDarkBorder),
        ) {
            ParsedDetailCard(
                item = previewReviewItem(),
                dark = true,
                currencyId = DEFAULT_CURRENCY_ID,
                showBackToList = false,
                onBackToList = {},
                onRetry = {},
                onItemChange = {},
                onAdd = {},
            )
        }
    }
}

@Preview(name = "Batch list - Light", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
internal fun PreviewBatchTransactionListLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = ReviewLightCanvas,
            border = BorderStroke(1.dp, ReviewLightBorder),
        ) {
            BatchTransactionList(
                items = listOf(
                    previewReviewItem(),
                    previewReviewItem(id = "preview-2", note = "Metro", amount = 40.0, categoryName = "Transport"),
                ),
                dark = false,
                currencyId = DEFAULT_CURRENCY_ID,
                onSelect = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Batch list - Dark", showBackground = true, backgroundColor = 0xFF09090B)
@Composable
internal fun PreviewBatchTransactionListDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            color = ReviewDarkCanvas,
            border = BorderStroke(1.dp, ReviewDarkBorder),
        ) {
            BatchTransactionList(
                items = listOf(
                    previewReviewItem(),
                    previewReviewItem(id = "preview-2", note = "Metro", amount = 40.0, categoryName = "Transport"),
                ),
                dark = true,
                currencyId = DEFAULT_CURRENCY_ID,
                onSelect = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Saved dialog - Light", showBackground = true, backgroundColor = 0xFFF1F5F9)
@Composable
internal fun PreviewTransactionSavedCardLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            TransactionSavedCard(
                dark = false,
            )
        }
    }
}

@Preview(name = "Saved dialog - Dark", showBackground = true, backgroundColor = 0xFF09090B)
@Composable
internal fun PreviewTransactionSavedCardDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            TransactionSavedCard(
                dark = true,
            )
        }
    }
}

