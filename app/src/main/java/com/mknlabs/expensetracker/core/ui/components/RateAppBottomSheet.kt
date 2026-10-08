package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.Star
import com.adamglin.phosphoricons.regular.Star
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.utils.PlayStoreLink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateAppBottomSheet(
    onDismiss: () -> Unit,
    onRated: () -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ratingState = rememberSaveable { mutableIntStateOf(0) }
    var rating by ratingState
    val canSubmit = rating > 0

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.title_rate_prompt),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.label_how_would_you_rate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            val density = LocalDensity.current
            var starRowWidthPx by remember { mutableFloatStateOf(0f) }
            var dragAccumPx by remember { mutableFloatStateOf(0f) }
            val starStepPx = if (starRowWidthPx > 0f) {
                starRowWidthPx / 5f
            } else {
                with(density) { 36.dp.toPx() }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .onSizeChanged { starRowWidthPx = it.width.toFloat() }
                    .pointerInput(starStepPx) {
                        detectHorizontalDragGestures(
                            onDragEnd = { dragAccumPx = 0f },
                            onDragCancel = { dragAccumPx = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumPx += dragAmount
                                while (dragAccumPx >= starStepPx) {
                                    ratingState.intValue =
                                        (ratingState.intValue + 1).coerceAtMost(5)
                                    dragAccumPx -= starStepPx
                                }
                                while (dragAccumPx <= -starStepPx) {
                                    ratingState.intValue =
                                        (ratingState.intValue - 1).coerceAtLeast(0)
                                    dragAccumPx += starStepPx
                                }
                            }
                        )
                    }
            ) {
                (1..5).forEach { star ->
                    val filled = star <= rating
                    Icon(
                        imageVector = if (filled) PhosphorIcons.Fill.Star else PhosphorIcons.Regular.Star,
                        contentDescription = stringResource(R.string.cd_rate_star, star),
                        tint = if (filled) MaterialTheme.colorScheme.featureGateLock
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = brandGradient(alpha = if (canSubmit) 1f else 0.45f))
                    .clickable(enabled = canSubmit) {
                        onRated()
                        PlayStoreLink.openPlayStore(context)
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.label_submit_rating),
                    color = MaterialTheme.colorScheme.onBrandGradient,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
