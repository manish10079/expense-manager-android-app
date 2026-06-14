package com.mknlabs.expensetracker.domain.repository

import android.net.Uri

interface DataManagementRepository {
    suspend fun backupDatabase(uri: Uri)

    suspend fun restoreDatabase(uri: Uri)

    suspend fun exportJson(uri: Uri): JsonExportResult

    suspend fun importJson(uri: Uri): JsonImportResult
}

data class JsonExportResult(
    val exportedTransactions: Int,
    val exportedBudgets: Int,
    val exportedRecurringRules: Int,
    val exportedCategories: Int,
    val exportedPaymentMethods: Int,
    val exportedGoals: Int
)

data class JsonImportResult(
    val importedTransactions: Int,
    val skippedTransactions: Int,
    val importedBudgets: Int,
    val skippedBudgets: Int,
    val importedRecurringRules: Int,
    val skippedRecurringRules: Int,
    val importedCategories: Int,
    val skippedCategories: Int,
    val importedPaymentMethods: Int,
    val skippedPaymentMethods: Int,
    val importedGoals: Int,
    val skippedGoals: Int
)
