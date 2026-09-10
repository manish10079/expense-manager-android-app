package com.mknlabs.expensetracker.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages static and dynamic app shortcuts for the expense tracker.
 * Static shortcuts are declared in shortcuts.xml.
 * Dynamic shortcuts are updated based on user context (favorites, recent transactions).
 */
@Singleton
class AppShortcutManager @Inject constructor(
    private val transactionRepository: TransactionRepository
) {

    companion object {
        private const val MAX_DYNAMIC_SHORTCUTS = 4

        // Intent action constants
        const val ACTION_ADD_TRANSACTION = "com.mknlabs.expensetracker.action.ADD_TRANSACTION"
        const val ACTION_VIEW_ANALYTICS = "com.mknlabs.expensetracker.action.VIEW_ANALYTICS"
        const val ACTION_SPEAK_TO_ADD = "com.mknlabs.expensetracker.action.SPEAK_TO_ADD"
        const val ACTION_COPY_BUDGET = "com.mknlabs.expensetracker.action.COPY_BUDGET"

        // Intent extra keys
        const val EXTRA_TRANSACTION_TYPE_ID = "transaction_type_id"
        const val EXTRA_TRANSACTION_ID = "transaction_id"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_CATEGORY_ID = "category_id"
        const val EXTRA_NOTE = "note"
    }

    /**
     * Push a dynamic shortcut for adding a favorite transaction.
     */
    fun pushFavoriteTransactionShortcut(
        context: Context,
        transactionId: String,
        amount: String,
        categoryName: String,
        categoryId: Int,
        transactionTypeId: Int
    ) {
        val label = if (transactionTypeId == 1) {
            "+$amount $categoryName"
        } else {
            "-$amount $categoryName"
        }

        val intent = Intent(context, com.mknlabs.expensetracker.MainActivity::class.java).apply {
            action = ACTION_ADD_TRANSACTION
            putExtra(EXTRA_TRANSACTION_TYPE_ID, transactionTypeId)
            putExtra(EXTRA_TRANSACTION_ID, transactionId)
            putExtra(EXTRA_AMOUNT, amount)
            putExtra(EXTRA_CATEGORY_ID, categoryId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcut = ShortcutInfoCompat.Builder(context, "fav_$transactionId")
            .setShortLabel(label.take(10))
            .setLongLabel(label.take(25))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_add_expense))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    /**
     * Push a dynamic shortcut for a most-used category.
     */
    fun pushMostUsedCategoryShortcut(
        context: Context,
        categoryName: String,
        categoryId: Int,
        transactionTypeId: Int
    ) {
        val label = if (transactionTypeId == 1) {
            "Add $categoryName"
        } else {
            "Add $categoryName"
        }

        val intent = Intent(context, com.mknlabs.expensetracker.MainActivity::class.java).apply {
            action = ACTION_ADD_TRANSACTION
            putExtra(EXTRA_TRANSACTION_TYPE_ID, transactionTypeId)
            putExtra(EXTRA_CATEGORY_ID, categoryId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcut = ShortcutInfoCompat.Builder(context, "cat_$categoryId")
            .setShortLabel(label.take(10))
            .setLongLabel(label.take(25))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_add_expense))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    /**
     * Refresh dynamic shortcuts based on current transaction data.
     * Called after transactions are added, favorites change, or month rolls over.
     */
    suspend fun refreshDynamicShortcuts(context: Context) {
        try {
            val transactions = transactionRepository.observeActiveTransactions().first()

            // Find most used categories
            val categoryCounts = transactions
                .groupBy { it.categoryId }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(2)

            for ((categoryId, _) in categoryCounts) {
                val categoryTransactions = transactions.filter { it.categoryId == categoryId }
                val mostCommonTypeId = categoryTransactions
                    .groupBy { it.transactionTypeId }
                    .maxByOrNull { it.value.size }?.key ?: 2

                val intent = Intent(context, com.mknlabs.expensetracker.MainActivity::class.java).apply {
                    action = ACTION_ADD_TRANSACTION
                    putExtra(EXTRA_TRANSACTION_TYPE_ID, mostCommonTypeId)
                    putExtra(EXTRA_CATEGORY_ID, categoryId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                val categoryName = when (categoryId) {
                    1 -> "Food"
                    2 -> "Transport"
                    3 -> "Shopping"
                    4 -> "Bills"
                    5 -> "Entertainment"
                    6 -> "Health"
                    7 -> "Education"
                    8 -> "Fuel"
                    else -> "Other"
                }

                val shortcut = ShortcutInfoCompat.Builder(context, "dynamic_cat_$categoryId")
                    .setShortLabel("Add $categoryName".take(10))
                    .setLongLabel("Add $categoryName".take(25))
                    .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_add_expense))
                    .setIntent(intent)
                    .build()

                ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
            }
        } catch (e: Exception) {
            // Silently fail - shortcuts are non-critical
        }
    }
}
