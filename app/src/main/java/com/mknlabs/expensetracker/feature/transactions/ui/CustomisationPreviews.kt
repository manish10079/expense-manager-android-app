package com.mknlabs.expensetracker.feature.transactions.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import android.content.res.Configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Today
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowDown
import com.adamglin.phosphoricons.regular.ArrowUp
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.CalendarDots
import com.adamglin.phosphoricons.regular.Clock
import com.adamglin.phosphoricons.regular.ListChecks
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.TextAa
import com.adamglin.phosphoricons.regular.Wallet
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.AddTransactionUiPattern
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowHeight
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowInfo
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowSize
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import androidx.window.core.layout.WindowSizeClass
import com.mknlabs.expensetracker.core.ui.components.TransactionCard
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatAmount
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.formatTime
import com.mknlabs.expensetracker.utils.getPaymentTypeName
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.AccessStatus

import com.mknlabs.expensetracker.core.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.components.SettingsGroup
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupHeader
import com.mknlabs.expensetracker.core.ui.components.AppSelectionSheet
import com.mknlabs.expensetracker.core.ui.components.TransactionPeriodFilter
import com.mknlabs.expensetracker.core.ui.components.transactionPeriodFilterOrDefault
import com.mknlabs.expensetracker.data.constants.defaultAppSettings
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore

import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.core.ui.models.SelectionItem
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.time.Duration.Companion.milliseconds

@Preview(
    name = "Card Settings - Smartphone Light",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
internal fun CustomisationScreenLightPreview() {
    TransactionCardCustomizePreviewContent(
        darkTheme = false,
        windowInfo = PREVIEW_HANDSET_WINDOW_INFO
    )
}

@Preview(
    name = "Card Settings - Smartphone Dark",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
internal fun CustomisationScreenDarkPreview() {
    TransactionCardCustomizePreviewContent(
        darkTheme = true,
        windowInfo = PREVIEW_HANDSET_WINDOW_INFO
    )
}

// Multi-config adaptive preview: verifies the stacked (compact) vs two-column
// (landscape/wide) layouts of the Card Settings screen (roadmap Milestone 5).
@Preview(name = "Card Settings - Multi-Config", showBackground = true)
@PreviewScreenSizes
@Composable
internal fun CustomisationScreenMultiConfigPreview() {
    TransactionCardCustomizePreviewContent(darkTheme = true)
}

@Composable
internal fun TransactionCardCustomizePreviewContent(
    darkTheme: Boolean,
    windowInfo: AppWindowInfo? = null
) {
    // The screen branches on LocalAppWindowInfo, which only the app root provides. A static
    // preview installs no provider, so it fell back to the local's default -- compact width
    // AND compact height, which reads as phone LANDSCAPE, not portrait -- and every preview,
    // the 412dp handset pair included, drew the two-column layout instead of the phone's
    // single column. A preview that knows its own frame passes the classes for it; one that
    // covers several frames (the multi-config preview) leaves this null and gets the classes
    // its current frame implies, so a wide frame still splits into two.
    val configuration = LocalConfiguration.current
    val frameWindowInfo = remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        previewAppWindowInfo(
            widthDp = configuration.screenWidthDp,
            heightDp = configuration.screenHeightDp
        )
    }
    val resolvedWindowInfo = windowInfo ?: frameWindowInfo
    ExpenseTrackerTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalAppWindowInfo provides resolvedWindowInfo) {
            TransactionCardCustomizeContent(
                settings = TransactionCardCustomizationSettings(),
                currencyId = DEFAULT_CURRENCY_ID,
                amountFormatPreferences = defaultAmountFormatPreferences,
                dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
                timeFormat = DEFAULT_TIME_FORMAT,
                previewTransactions = transactionList.take(2),
                isAdsEnabled = false,
                isProUser = true,
                isTransactionTimeProGranted = true,
                isDateSeparatorsProGranted = true,
                isPaymentMethodProGranted = true,
                isListSummariesProGranted = true,
                onSettingsChange = {},
                onBackClick = {}
            )
        }
    }
}

/**
 * The size classes a preview frame implies, mapped at the same breakpoints
 * `rememberAppWindowInfo()` reads at runtime -- a preview has no real window to ask, only the
 * width and height its `@Preview` frame was given.
 */
internal fun previewAppWindowInfo(widthDp: Int, heightDp: Int): AppWindowInfo = AppWindowInfo(
    width = when {
        widthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> AppWindowSize.ExtraLarge
        widthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> AppWindowSize.Large
        widthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> AppWindowSize.Expanded
        widthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> AppWindowSize.Medium
        else -> AppWindowSize.Compact
    },
    height = when {
        heightDp >= WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND -> AppWindowHeight.Expanded
        heightDp >= WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND -> AppWindowHeight.Medium
        else -> AppWindowHeight.Compact
    }
)

