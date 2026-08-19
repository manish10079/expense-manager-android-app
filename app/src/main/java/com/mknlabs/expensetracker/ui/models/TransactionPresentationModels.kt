package com.mknlabs.expensetracker.ui.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.monetization.AdPlacement

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
    val categoryLabel: String,
    val isRecurring: Boolean = false
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

    @Immutable
    data class SummaryCard(
        val id: String,
        val totalIncome: String,
        val totalExpense: String,
        val periodLabel: String? = null
    ) : TransactionListItemUi

    /**
     * A dedicated ad slot in the list (Phase 2, ADS_UI_JANK_FIX_PLAN §5).
     * Emitted as its own list entry with a stable key (e.g. "ad_5") so the ad's
     * composition is independent of transaction-card recompositions and Compose
     * recycles its AndroidView across scroll entries instead of re-inflating it.
     *
     * [placement] selects which AdMob unit renders in the slot: the list alternates
     * between [AdPlacement.TRANSACTIONS_LIST] and [AdPlacement.TRANSACTIONS_LIST_2]
     * so both units get equal exposure (and separate AdMob analytics).
     */
    @Immutable
    data class Ad(
        val id: String,
        val placement: AdPlacement
    ) : TransactionListItemUi
}
