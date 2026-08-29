package com.mknlabs.expensetracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.sms.ParsedSms
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.viewmodels.SmsChangeUiState
import com.mknlabs.expensetracker.ui.viewmodels.SmsChangeViewModel
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.toMajorUnits

private const val INCOME_TYPE_ID = 1

/**
 * Route for the Smart SMS Import "Change" bottom sheet (plan §8 / Phase 4).
 *
 * Owns the [SmsChangeViewModel]: feeds it the [ParsedSms] payload received via
 * the notification's Change action, observes state, and forwards save/dismiss
 * outcomes back to [MainScreen] (which cancels the notification on save).
 */
@Composable
fun SmsChangeRoute(
    parsedSms: ParsedSms,
    categories: List<CategoryType>,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val viewModel: SmsChangeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Fresh sheet session per Change tap — resets selection/note/saved flags.
    LaunchedEffect(parsedSms) { viewModel.load(parsedSms) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    LaunchedEffect(uiState.saveError) {
        if (uiState.saveError) {
            Toast.makeText(
                context,
                context.getString(R.string.msg_sms_save_failed),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.consumeSaveError()
        }
    }

    SmsChangeContent(
        uiState = uiState,
        categories = categories,
        onDismiss = onDismiss,
        onCategorySelected = viewModel::onCategorySelected,
        onNoteChange = viewModel::onNoteChange,
        onSave = viewModel::save
    )
}

/**
 * The lightweight Change sheet: category picker + optional note + Save.
 * Pure UI — no ViewModels or state collection (GEMINI.md route/content split).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsChangeContent(
    uiState: SmsChangeUiState,
    categories: List<CategoryType>,
    onDismiss: () -> Unit,
    onCategorySelected: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val parsed = uiState.parsed ?: return
    val colorScheme = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categoriesForType = remember(parsed.transactionTypeId, categories) {
        categories
            .filter { it.transactionTypeId == parsed.transactionTypeId }
            .sortedBy { it.sortOrder }
    }
    val suggestedName = categoryMap[parsed.categoryId]?.name
        ?: stringResource(R.string.label_other)
    // The parsed amount is in the SMS's own currency (₹/INR by parser design),
    // so we format with the app default rather than the user's display currency
    // — mirroring SmsNotificationManager, which documents this rationale.
    val amountText = formatCurrencyValue(
        amount = parsed.amountMinor.toMajorUnits(),
        currencyId = DEFAULT_CURRENCY_ID,
        amountFormatPreferences = defaultAmountFormatPreferences
    )
    val verb = stringResource(
        if (parsed.transactionTypeId == INCOME_TYPE_ID) {
            R.string.sms_verb_credited
        } else {
            R.string.sms_verb_debited
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: title + amount/verb summary + close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.title_sms_change),
                        color = colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = amountText,
                            color = if (parsed.transactionTypeId == INCOME_TYPE_ID) {
                                colorScheme.primary
                            } else {
                                colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = verb,
                            color = colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                    if (parsed.sender.isNotBlank()) {
                        Text(
                            text = parsed.sender,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.desc_close),
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Category picker
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_choose_category),
                        color = colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = stringResource(R.string.notification_format_sms_import_suggested, suggestedName),
                        color = colorScheme.secondary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(categoriesForType, key = { it.id }) { category ->
                        SmsCategoryChip(
                            category = category,
                            isSelected = category.id == uiState.selectedCategoryId,
                            onClick = { onCategorySelected(category.id) }
                        )
                    }
                }
            }

            // Optional note
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    label = {
                        Text(
                            text = stringResource(R.string.label_optional_note),
                            color = colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.note.isNotEmpty()) {
                            IconButton(onClick = { onNoteChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.desc_clear_note),
                                    tint = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = colorScheme.primary
                    )
                )
                Text(
                    text = "${uiState.note.length}/200",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }

            // Save
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .alpha(if (uiState.isSaving) 0.6f else 1f)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = colorScheme.primary.copy(alpha = 0.25f),
                        spotColor = colorScheme.secondary.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(brandGradient())
                    .clickable(enabled = !uiState.isSaving, onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.label_save),
                        color = colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SmsCategoryChip(
    category: CategoryType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.widthIn(min = 68.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = if (isSelected) 18.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                    spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    if (isSelected) brandGradient() else standardCardGradient()
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = category.name,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
