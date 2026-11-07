package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowHeight
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowInfo
import com.mknlabs.expensetracker.core.ui.adaptive.AppWindowSize
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import androidx.compose.material3.MaterialTheme
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT

@Composable
fun CalculatorScreen(
    viewModel: ItemizedCalculatorViewModel,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showHistorySheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = Dimens.HeaderSpacing, bottom = 14.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -40f) {
                        showHistorySheet = true
                    }
                }
            },
    ) {
        val enter = rememberSectionEnterAlphas(2)
        AppHeader(
            title = stringResource(id = R.string.title_home_calculator),
            onBackClick = onBackClick,
            modifier = Modifier.alpha(enter[0]),
            actions = {
                CalculatorHistoryHeaderIcon(onClick = { showHistorySheet = true })
            }
        )

        NormalCalculatorContent(
            modifier = Modifier
                .alpha(enter[1])
                .weight(1f)
                .fillMaxSize()
                .padding(top = 18.dp),
            display = uiState.normalDisplay,
            previewResult = viewModel.calculatePreview(),
            expression = viewModel.buildExpression(),
            isEvaluated = uiState.shouldResetNormalDisplay,
            onAction = viewModel::handleNormalAction,
            onSelectionChanged = viewModel::setSelectionFromDisplay,
            cursorDisplayPosition = viewModel.getDisplayPositionFromCursor()
        )
    }

    if (showHistorySheet) {
        CalculatorHistorySheet(
            entries = uiState.historyEntries,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            onDismiss = { showHistorySheet = false },
            onTapExpression = { expression ->
                viewModel.restoreHistoryExpression(expression)
                showHistorySheet = false
            },
            onTapResult = { result ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                        context.getString(R.string.label_history),
                        result
                    )
                )
                Toast.makeText(context, context.getString(R.string.toast_history_copied), Toast.LENGTH_SHORT).show()
                viewModel.selectHistoryResult(result)
                showHistorySheet = false
            },
            onDeleteEntry = viewModel::deleteHistoryEntry,
            onClearHistory = viewModel::clearHistory
        )
    }
}

@Preview(name = "Normal tab light", showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun CalculatorNormalTabPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        CalculatorNormalScreenPreviewBody()
    }
}

@Preview(
    name = "Normal tab dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 740
)
@Composable
private fun CalculatorNormalTabPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        CalculatorNormalScreenPreviewBody()
    }
}

@Composable
private fun CalculatorNormalScreenPreviewBody() {
    CompositionLocalProvider(
        LocalAppWindowInfo provides AppWindowInfo(
            width = AppWindowSize.Compact,
            height = AppWindowHeight.Expanded
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = Dimens.HeaderSpacing, bottom = 14.dp)
        ) {
            AppHeader(
                title = stringResource(id = R.string.title_home_calculator),
                onBackClick = {},
                actions = {
                    CalculatorHistoryHeaderIcon(onClick = {})
                }
            )
            Column(modifier = Modifier.weight(1f)) {
                NormalCalculatorContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp),
                    display = "142",
                    previewResult = "142",
                    expression = "100+42",
                    isEvaluated = false,
                    onAction = {},
                    onSelectionChanged = { _, _ -> },
                    cursorDisplayPosition = 7
                )
            }
        }
    }
}
