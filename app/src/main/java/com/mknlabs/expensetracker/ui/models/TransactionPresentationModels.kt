package com.mknlabs.expensetracker.ui.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.models.Transaction

@Immutable
data class TransactionCardItemUi(
    val id: String,
    val transaction: Transaction,
    val note: String,
    val transactionDate: String,
    val transactionTime: String,
    val amount: String,
    val icon: ImageVector,
    val transactionTypeId: Int,
    val paymentType: String,
    val categoryLabel: String
)

sealed interface TransactionListItemUi {

    @Immutable
    data class Header(
        val id: String,
        val timestamp: Long,
        val dayLabel: String,
        val dateLabel: String
    ) : TransactionListItemUi

    @Immutable
    data class TransactionRow(
        val card: TransactionCardItemUi
    ) : TransactionListItemUi
}
