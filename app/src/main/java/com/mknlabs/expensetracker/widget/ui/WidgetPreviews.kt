package com.mknlabs.expensetracker.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview as GlancePreview
import com.mknlabs.expensetracker.widget.model.WidgetRecentItem
import com.mknlabs.expensetracker.widget.model.WidgetUiState

private const val PREVIEW_2X2 = 175

private val previewMonthUiState = WidgetUiState(
    monthLabel = "Nov Summary",
    monthIncomeMinor = 8_500_000L,
    monthExpenseMinor = 3_420_000L,
    currencySymbol = "₹"
)

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_2X2, heightDp = PREVIEW_2X2)
@Composable
internal fun MonthSummaryPreview() {
    GlanceTheme {
        Widget2x2SummaryContent(previewMonthUiState, forceDark = true)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_2X2, heightDp = PREVIEW_2X2)
@Composable
internal fun MonthSummaryDarkPreview() {
    GlanceTheme {
        Widget2x2SummaryContent(previewMonthUiState, forceDark = true)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_2X2, heightDp = PREVIEW_2X2)
@Composable
internal fun MonthSummaryLightPreview() {
    GlanceTheme {
        Widget2x2SummaryContent(previewMonthUiState, forceDark = false)
    }
}

private const val PREVIEW_4X2_W = 320
private const val PREVIEW_4X2_H = 175

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_4X2_W, heightDp = PREVIEW_4X2_H)
@Composable
internal fun AddTransactionDarkPreview() {
    GlanceTheme {
        Widget4x2Content(previewMonthUiState, forceDark = true)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_4X2_W, heightDp = PREVIEW_4X2_H)
@Composable
internal fun AddTransactionLightPreview() {
    GlanceTheme {
        Widget4x2Content(previewMonthUiState, forceDark = false)
    }
}

private const val PREVIEW_4X4 = 320

private val preview4x4UiState = WidgetUiState(
    monthIncomeMinor = 12_000_000L,
    monthExpenseMinor = 4_280_000L,
    monthBudgetCapMinor = 12_000_000L,
    currencySymbol = "₹",
    recentItems = listOf(
        WidgetRecentItem(
            title = "Freelance Payment",
            subtitle = "Nov 06 • Bank Transfer",
            amountText = "+ ₹25,000",
            isIncome = true
        ),
        WidgetRecentItem(
            title = "Electronic Store",
            subtitle = "Nov 05 • UPI OCR",
            amountText = "- ₹4,200",
            isIncome = false
        )
    )
)

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_4X4, heightDp = PREVIEW_4X4)
@Composable
internal fun MonthOverviewDarkPreview() {
    GlanceTheme {
        Widget4x4Content(preview4x4UiState, forceDark = true)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_4X4, heightDp = PREVIEW_4X4)
@Composable
internal fun MonthOverviewLightPreview() {
    GlanceTheme {
        Widget4x4Content(preview4x4UiState, forceDark = false)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_4X4, heightDp = PREVIEW_4X4)
@Composable
internal fun AnalyticsBreakdownDarkPreview() {
    GlanceTheme {
        DonutBreakdownPreviewContent(forceDark = true)
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@GlancePreview(widthDp = PREVIEW_4X4, heightDp = PREVIEW_4X4)
@Composable
internal fun AnalyticsBreakdownLightPreview() {
    GlanceTheme {
        DonutBreakdownPreviewContent(forceDark = false)
    }
}


