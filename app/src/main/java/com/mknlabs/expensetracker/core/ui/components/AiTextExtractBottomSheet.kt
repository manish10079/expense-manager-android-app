package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X
import com.adamglin.phosphoricons.regular.XCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.sheet
import kotlinx.coroutines.delay

private const val ExtractTextMaxLength = 2000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTextExtractBottomSheet(
    onDismissRequest: () -> Unit,
    onExtract: (String, com.mknlabs.expensetracker.domain.models.ParsedVoiceTransaction?) -> Unit,
    initialText: String = ""
) {
    val extractViewModel: com.mknlabs.expensetracker.ai.AiTextExtractViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    var isExtracting by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }
    val canExtract = fieldValue.text.isNotBlank() && !isExtracting

    LaunchedEffect(Unit) {
        delay(300)
        fieldValue = fieldValue.copy(selection = TextRange(fieldValue.text.length))
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (MaterialTheme.colorScheme.isDark) {
            MaterialTheme.colorScheme.profileSheet
        } else {
            MaterialTheme.colorScheme.sheet
        },
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.accentInk,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(22.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.title_ai_extract_from_text),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.accentInk
                            )
                        )
                        Text(
                            text = stringResource(R.string.desc_ai_extract_from_text),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = stringResource(R.string.desc_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = fieldValue,
                    onValueChange = { newValue ->
                        if (newValue.text.length <= ExtractTextMaxLength) {
                            fieldValue = newValue
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp)
                        .focusRequester(focusRequester)
                        .border(
                            width = 1.dp,
                            color = if (MaterialTheme.colorScheme.isDark) {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(16.dp)
                        ),
                    placeholder = {
                        Text(
                            stringResource(R.string.placeholder_ai_extract_from_text),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    trailingIcon = {
                        if (fieldValue.text.isNotEmpty()) {
                            IconButton(onClick = { fieldValue = TextFieldValue("") }) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.XCircle,
                                    contentDescription = stringResource(R.string.desc_clear_note),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (MaterialTheme.colorScheme.isDark) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        unfocusedContainerColor = if (MaterialTheme.colorScheme.isDark) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.accentInk
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    text = "${fieldValue.text.length}/$ExtractTextMaxLength",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = MaterialTheme.colorScheme.accentInk.copy(alpha = 0.25f),
                        spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(brandGradient(alpha = if (canExtract) 1f else 0.45f))
                    .clickable(enabled = canExtract) {
                        val text = fieldValue.text.trim()
                        isExtracting = true
                        extractViewModel.parse(text) { parsed ->
                            isExtracting = false
                            onExtract(text, parsed)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isExtracting) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onCta,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.action_ai_extract),
                        color = MaterialTheme.colorScheme.onCta,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
