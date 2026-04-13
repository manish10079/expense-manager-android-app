package com.mkn0079.expensetracker.data.legacy

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabase
import com.mkn0079.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer
import com.mkn0079.expensetracker.data.local.room.entities.TransactionEntity
import com.mkn0079.expensetracker.models.SyncState
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

private const val INCOME_TRANSACTION_TYPE_ID = 1
private const val EXPENSE_TRANSACTION_TYPE_ID = 2
private const val FALLBACK_INCOME_CATEGORY_ID = 105
private const val FALLBACK_EXPENSE_CATEGORY_ID = 23
private const val FALLBACK_PAYMENT_METHOD_ID = 5

data class LegacyImportResult(
    val totalTransactions: Int,
    val importedTransactions: Int,
    val skippedTransactions: Int
)

class LegacyImportRepository(
    private val context: Context
) {
    private val appContext = context.applicationContext
    private val database = ExpenseTrackerDatabase.getInstance(appContext)
    private val transactionDao = database.transactionDao()

    suspend fun importBackup(uri: Uri): LegacyImportResult {
        ExpenseTrackerDatabaseInitializer.initialize(appContext)

        val backupJson = appContext.contentResolver.openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("Unable to open selected legacy backup file.")
        val transactionsJson = JSONObject(backupJson).getJSONArray("transactions")
        val transactionsToImport = mutableListOf<TransactionEntity>()
        var skippedTransactions = 0

        database.withTransaction {
            for (index in 0 until transactionsJson.length()) {
                val transactionJson = transactionsJson.getJSONObject(index)
                val transaction = runCatching {
                    transactionJson.toTransactionEntity()
                }.getOrNull()
                if (transaction == null || transactionDao.getById(transaction.id) != null) {
                    skippedTransactions++
                } else {
                    transactionsToImport += transaction
                }
            }

            if (transactionsToImport.isNotEmpty()) {
                transactionDao.upsertAll(transactionsToImport)
            }
        }

        return LegacyImportResult(
            totalTransactions = transactionsJson.length(),
            importedTransactions = transactionsToImport.size,
            skippedTransactions = skippedTransactions
        )
    }

    private fun JSONObject.toTransactionEntity(): TransactionEntity? {
        val legacyId = optString("uuid")
            .takeIf { it.isNotBlank() }
            ?: optLong("id", 0L).takeIf { it > 0L }?.let { "legacy-$it" }
            ?: return null
        val occurredAt = optLong("timestamp", 0L).takeIf { it > 0L } ?: return null
        val transactionTypeId = when (optString("type").trim().lowercase()) {
            "income" -> INCOME_TRANSACTION_TYPE_ID
            else -> EXPENSE_TRANSACTION_TYPE_ID
        }

        return TransactionEntity(
            id = legacyId,
            note = optString("notes").trim(),
            amountMinor = optAmountMinor(),
            occurredAt = occurredAt,
            createdAt = occurredAt,
            updatedAt = System.currentTimeMillis(),
            transactionTypeId = transactionTypeId,
            categoryId = resolveCategoryId(
                categoryName = optString("category"),
                transactionTypeId = transactionTypeId
            ),
            paymentMethodId = resolvePaymentMethodId(optString("source")),
            isDeleted = false,
            syncState = SyncState.LOCAL_ONLY,
            contentHash = optString("hash").takeIf { it.isNotBlank() },
            sourceRecurringRuleId = null
        )
    }

    private fun JSONObject.optAmountMinor(): Long {
        return BigDecimal(get("amount").toString())
            .setScale(2, RoundingMode.HALF_UP)
            .multiply(BigDecimal("100"))
            .longValueExact()
    }

    private fun resolveCategoryId(
        categoryName: String,
        transactionTypeId: Int
    ): Int {
        val normalizedCategoryName = categoryName.normalizedKey()
        return categoryMap.values.firstOrNull { category ->
            category.transactionTypeId == transactionTypeId &&
                category.name.normalizedKey() == normalizedCategoryName
        }?.id ?: if (transactionTypeId == INCOME_TRANSACTION_TYPE_ID) {
            FALLBACK_INCOME_CATEGORY_ID
        } else {
            FALLBACK_EXPENSE_CATEGORY_ID
        }
    }

    private fun resolvePaymentMethodId(sourceName: String): Int {
        val normalizedSourceName = sourceName.normalizedKey()
        return paymentTypeMap.values.firstOrNull { paymentMethod ->
            paymentMethod.name.normalizedKey() == normalizedSourceName
        }?.id ?: FALLBACK_PAYMENT_METHOD_ID
    }

    private fun String.normalizedKey(): String {
        return trim().lowercase()
    }
}
