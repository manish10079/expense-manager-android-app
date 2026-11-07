package com.mknlabs.expensetracker.feature.smsinbox.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import androidx.compose.material3.FilterChipDefaults
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.chip
import com.mknlabs.expensetracker.core.ui.theme.chipInkOff
import com.mknlabs.expensetracker.core.ui.theme.chipOutline
import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.chipSelectedInk
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.components.PeriodChipDefaults
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.sms.SmsConfidence
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.TransactionDateHeader
import com.mknlabs.expensetracker.core.ui.horizontalSwipe
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.BadgeOnColor
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.transparent
import com.mknlabs.expensetracker.utils.UiText
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@Composable
internal fun SearchField(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Dimens.ScreenPadding, end = Dimens.ScreenPadding, bottom = 8.dp),
        singleLine = true,
        placeholder = { Text(stringResource(id = R.string.label_sms_inbox_search_hint)) },
        shape = AppOutlinedFieldDefaults.shape,
        colors = AppOutlinedFieldDefaults.colors()
    )
}

@Composable
internal fun FilterRow(selected: SmsInboxFilter, onFilterSelected: (SmsInboxFilter) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val selectedFill = brandGradient()
    val chipShape = RoundedCornerShape(percent = 50)
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SmsInboxFilter.entries) { filter ->
            val isSelected = filter == selected
            Text(
                text = filter.label(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) scheme.onBrandGradient else scheme.chipInkOff,
                maxLines = 1,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(chipShape)
                    .then(
                        if (isSelected) {
                            Modifier.background(selectedFill)
                        } else {
                            Modifier
                                .background(PeriodChipDefaults.unselectedContainer)
                                .border(1.dp, PeriodChipDefaults.unselectedBorder, chipShape)
                        }
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Two dismissible strips: tap-to-add, then swipe-to-delete.
 */
@Composable
internal fun InboxHint() {
    val context = LocalContext.current
    val persisted by AppSettingsDataStore.smsInboxHintDismissedFlow(context)
        .collectAsStateWithLifecycle(initialValue = AppSettingsDataStore.SmsInboxHintDismissed())
    var hideTap by remember { mutableStateOf(false) }
    var hideSwipe by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val showTapHint = !persisted.tap && !hideTap
    val showSwipeHint = !persisted.swipe && !hideSwipe
    if (!showTapHint && !showSwipeHint) return

    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showTapHint) {
            HintStrip(
                icon = Icons.Outlined.TouchApp,
                iconTint = scheme.accentInk,
                text = stringResource(id = R.string.label_sms_inbox_card_hint),
                containerColor = scheme.accentSoft,
                onDismiss = {
                    hideTap = true
                    scope.launch { AppSettingsDataStore.dismissSmsInboxTapHint(context) }
                }
            )
        }
        if (showSwipeHint) {
            HintStrip(
                icon = Icons.Outlined.SwapHoriz,
                iconTint = scheme.onSurfaceVariant,
                text = stringResource(id = R.string.label_sms_inbox_swipe_hint),
                containerColor = scheme.chip,
                onDismiss = {
                    hideSwipe = true
                    scope.launch { AppSettingsDataStore.dismissSmsInboxSwipeHint(context) }
                }
            )
        }
    }
}

@Composable
internal fun HintStrip(
    icon: ImageVector,
    iconTint: Color,
    text: String,
    containerColor: Color,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(start = 12.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(id = R.string.desc_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

