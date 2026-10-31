package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ai.cloud.AiParseContract
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.core.ui.theme.sheet
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiParseReviewSheet(
    items: List<AiParsedReviewItem>,
    onDismissRequest: () -> Unit,
    onAdd: (AiParsedReviewItem) -> Unit,
    onEdit: (AiParsedReviewItem) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.sheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.label_sms_inbox_edit_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    AiParseReviewCard(
                        item = item,
                        onAdd = { onAdd(item) },
                        onEdit = { onEdit(item) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun AiParseReviewCard(
    item: AiParsedReviewItem,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
) {
    val typeLabel = if (item.typeId == AiParseContract.TYPE_INCOME) {
        stringResource(R.string.label_income_title)
    } else {
        stringResource(R.string.label_expense_title)
    }
    val amountText = BigDecimal.valueOf(item.amount).stripTrailingZeros().toPlainString()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$typeLabel · $amountText",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = item.categoryName,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = item.paymentName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (item.note.isNotBlank()) {
            Text(
                text = item.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onAdd,
                modifier = Modifier.weight(1f),
                enabled = item.categoryId > 0,
            ) {
                Text(stringResource(R.string.label_add_action))
            }
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.label_edit))
            }
        }
    }
}
